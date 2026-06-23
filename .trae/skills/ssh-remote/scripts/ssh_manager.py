#!/usr/bin/env python3
"""
SSH 远程连接管理脚本
用于复杂 SSH 操作场景，如交互式命令、文件传输、批量操作等。

依赖安装：pip install paramiko

使用方式：
  python ssh_manager.py connect --host HOST --user USER [--port PORT] [--key KEY]
  python ssh_manager.py run --host HOST --user USER --cmd "COMMAND"
  python ssh_manager.py upload --host HOST --user USER --local LOCAL --remote REMOTE
  python ssh_manager.py download --host HOST --user USER --remote REMOTE --local LOCAL
  python ssh_manager.py batch --hosts "h1,h2,h3" --user USER --cmd "COMMAND"
  python ssh_manager.py test --host HOST --user USER
  python ssh_manager.py keygen [--type ed25519|rsa] [--bits 4096] [--comment COMMENT]
  python ssh_manager.py tunnel --host HOST --user USER --local-port LP --remote-host RH --remote-port RP
"""

import argparse
import os
import sys
import time
import socket
import json
from pathlib import Path

try:
    import paramiko
except ImportError:
    print("错误: 需要安装 paramiko 库")
    print("请执行: pip install paramiko")
    sys.exit(1)


# ==================== 危险命令检测 ====================

DANGEROUS_PATTERNS = [
    "rm -rf /", "rm -rf /*", "rm -rf *",
    "shutdown", "reboot", "halt", "poweroff",
    "mkfs.", "dd if=", "dd of=/dev/",
    "chmod 777", "chmod -R 777",
    "kill -9", "killall -9",
    "DROP TABLE", "DROP DATABASE", "TRUNCATE",
    ":(){ :|:& };:",  # fork bomb
    "> /dev/sd", "> /dev/nvme",
    "mv / /", "mv /* /",
]

MEDIUM_RISK_PATTERNS = [
    "rm ", "mv /", "cp /",
    "systemctl restart", "systemctl stop",
    "service restart", "service stop",
    "docker restart", "docker stop", "docker rm",
    "iptables", "firewall-cmd", "ufw",
    "crontab", "useradd", "userdel",
    "passwd", "visudo",
]


def check_command_safety(cmd: str) -> dict:
    """检查命令安全级别"""
    cmd_lower = cmd.strip().lower()
    result = {"level": "safe", "risks": []}

    for pattern in DANGEROUS_PATTERNS:
        if pattern.lower() in cmd_lower:
            result["level"] = "dangerous"
            result["risks"].append(f"高危命令: 包含 '{pattern}'")
            break

    if result["level"] == "safe":
        for pattern in MEDIUM_RISK_PATTERNS:
            if pattern.lower() in cmd_lower:
                result["level"] = "medium"
                result["risks"].append(f"中风险命令: 包含 '{pattern}'")
                break

    return result


# ==================== SSH 连接管理 ====================

class SSHConnection:
    """SSH 连接管理器"""

    def __init__(self, host, user, port=22, key=None, password=None, timeout=10):
        self.host = host
        self.user = user
        self.port = port
        self.key_path = key
        self.password = password
        self.timeout = timeout
        self.client = None

    def connect(self):
        """建立 SSH 连接"""
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        try:
            if self.key_path:
                key_path = os.path.expanduser(self.key_path)
                private_key = paramiko.Ed25519Key.from_private_key_file(
                    key_path
                ) if key_path.endswith("ed25519") or "ed25519" in key_path else \
                    paramiko.RSAKey.from_private_key_file(key_path)
                self.client.connect(
                    hostname=self.host,
                    port=self.port,
                    username=self.user,
                    pkey=private_key,
                    timeout=self.timeout,
                    look_for_keys=False,
                )
            else:
                self.client.connect(
                    hostname=self.host,
                    port=self.port,
                    username=self.user,
                    password=self.password,
                    timeout=self.timeout,
                    allow_agent=True,
                    look_for_keys=True,
                )
            return True
        except paramiko.AuthenticationException:
            print(f"错误: 认证失败 - 请检查用户名 '{self.user}' 和密钥/密码")
            return False
        except paramiko.SSHException as e:
            print(f"错误: SSH 连接异常 - {e}")
            return False
        except socket.timeout:
            print(f"错误: 连接超时 ({self.timeout}s) - 请检查主机 {self.host}:{self.port} 是否可达")
            return False
        except Exception as e:
            print(f"错误: 连接失败 - {e}")
            return False

    def execute(self, cmd, timeout=30):
        """执行远程命令"""
        if not self.client:
            print("错误: 未连接到远程主机")
            return None

        # 安全检查
        safety = check_command_safety(cmd)
        if safety["level"] == "dangerous":
            print(f"!! 危险命令警告 !!")
            for risk in safety["risks"]:
                print(f"  - {risk}")
            confirm = input("确认执行此命令？(yes/no): ")
            if confirm.lower() != "yes":
                print("已取消执行")
                return None
        elif safety["level"] == "medium":
            print(f"! 中风险命令提示 !")
            for risk in safety["risks"]:
                print(f"  - {risk}")

        try:
            stdin, stdout, stderr = self.client.exec_command(cmd, timeout=timeout)
            exit_code = stdout.channel.recv_exit_status()
            out = stdout.read().decode("utf-8", errors="replace")
            err = stderr.read().decode("utf-8", errors="replace")

            # 截断过长输出
            MAX_LINES = 200
            out_lines = out.split("\n")
            if len(out_lines) > MAX_LINES:
                out = "\n".join(out_lines[:30])
                out += f"\n... (省略 {len(out_lines) - 60} 行) ...\n"
                out += "\n".join(out_lines[-30:])

            return {
                "exit_code": exit_code,
                "stdout": out,
                "stderr": err,
                "host": self.host,
                "command": cmd,
            }
        except socket.timeout:
            print(f"错误: 命令执行超时 ({timeout}s)")
            return None

    def upload(self, local_path, remote_path):
        """上传文件"""
        if not self.client:
            print("错误: 未连接到远程主机")
            return False

        local_path = os.path.expanduser(local_path)
        if not os.path.exists(local_path):
            print(f"错误: 本地文件不存在 - {local_path}")
            return False

        try:
            sftp = self.client.open_sftp()
            # 确保远程目录存在
            remote_dir = os.path.dirname(remote_path)
            if remote_dir:
                try:
                    sftp.stat(remote_dir)
                except FileNotFoundError:
                    # 递归创建目录
                    dirs = remote_dir.split("/")
                    current = ""
                    for d in dirs:
                        current += f"/{d}" if current else d
                        try:
                            sftp.stat(current)
                        except FileNotFoundError:
                            sftp.mkdir(current)

            sftp.put(local_path, remote_path)
            sftp.close()
            print(f"上传成功: {local_path} -> {self.host}:{remote_path}")
            return True
        except Exception as e:
            print(f"上传失败: {e}")
            return False

    def download(self, remote_path, local_path):
        """下载文件"""
        if not self.client:
            print("错误: 未连接到远程主机")
            return False

        local_path = os.path.expanduser(local_path)
        local_dir = os.path.dirname(local_path)
        if local_dir:
            os.makedirs(local_dir, exist_ok=True)

        try:
            sftp = self.client.open_sftp()
            sftp.get(remote_path, local_path)
            sftp.close()
            print(f"下载成功: {self.host}:{remote_path} -> {local_path}")
            return True
        except Exception as e:
            print(f"下载失败: {e}")
            return False

    def close(self):
        """关闭连接"""
        if self.client:
            self.client.close()
            self.client = None


# ==================== 批量操作 ====================

def batch_execute(hosts, user, cmd, port=22, key=None, timeout=30):
    """批量在多台主机上执行命令"""
    results = {}
    for host in hosts:
        host = host.strip()
        if not host:
            continue
        print(f"\n{'='*60}")
        print(f"主机: {host}")
        print(f"{'='*60}")

        conn = SSHConnection(host, user, port=port, key=key)
        if conn.connect():
            result = conn.execute(cmd, timeout=timeout)
            if result:
                results[host] = result
                print(f"退出码: {result['exit_code']}")
                if result["stdout"]:
                    print(f"输出:\n{result['stdout']}")
                if result["stderr"]:
                    print(f"错误:\n{result['stderr']}")
            conn.close()

    # 汇总
    print(f"\n{'='*60}")
    print("执行汇总:")
    success = sum(1 for r in results.values() if r["exit_code"] == 0)
    failed = len(results) - success
    print(f"  成功: {success} / {len(hosts)}")
    print(f"  失败: {failed} / {len(hosts)}")
    for host, r in results.items():
        status = "OK" if r["exit_code"] == 0 else f"FAIL(code={r['exit_code']})"
        print(f"  {host}: {status}")

    return results


# ==================== 连接测试 ====================

def test_connection(host, port=22, timeout=5):
    """测试 SSH 连接可达性"""
    print(f"测试连接: {host}:{port}")
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(timeout)
        start = time.time()
        result = sock.connect_ex((host, port))
        elapsed = time.time() - start
        sock.close()

        if result == 0:
            print(f"  端口开放: 是 (延迟: {elapsed*1000:.0f}ms)")
            return True
        else:
            print(f"  端口开放: 否 (连接被拒绝)")
            return False
    except socket.timeout:
        print(f"  端口开放: 未知 (连接超时 {timeout}s)")
        return False
    except socket.gaierror:
        print(f"  DNS 解析失败: 无法解析主机名 '{host}'")
        return False
    except Exception as e:
        print(f"  连接测试失败: {e}")
        return False


# ==================== 密钥生成 ====================

def generate_key(key_type="ed25519", bits=4096, comment="", key_path=None):
    """生成 SSH 密钥对"""
    ssh_dir = Path.home() / ".ssh"
    ssh_dir.mkdir(exist_ok=True)

    if key_type == "ed25519":
        key_path = key_path or str(ssh_dir / "id_ed25519")
        key = paramiko.Ed25519Key.generate()
    elif key_type == "rsa":
        key_path = key_path or str(ssh_dir / "id_rsa")
        key = paramiko.RSAKey.generate(bits=bits)
    else:
        print(f"错误: 不支持的密钥类型 '{key_type}'，支持: ed25519, rsa")
        return False

    pub_key_path = key_path + ".pub"
    comment = comment or f"generated@{socket.gethostname()}"

    key.write_private_key_file(key_path)
    pub_key_str = f"{key.get_name()} {key.get_base64()} {comment}"

    with open(pub_key_path, "w") as f:
        f.write(pub_key_str + "\n")

    # 设置权限（仅限 Unix）
    if sys.platform != "win32":
        os.chmod(key_path, 0o600)
        os.chmod(pub_key_path, 0o644)

    print(f"密钥生成成功:")
    print(f"  类型: {key_type}")
    print(f"  私钥: {key_path}")
    print(f"  公钥: {pub_key_path}")
    print(f"  公钥内容: {pub_key_str}")
    return True


# ==================== 端口转发 ====================

def create_tunnel(host, user, local_port, remote_host, remote_port, port=22, key=None):
    """创建本地端口转发隧道"""
    import threading

    transport = None

    def forward(local_sock):
        try:
            chan = transport.open_channel(
                "direct-tcpip",
                (remote_host, remote_port),
                local_sock.getpeername(),
            )
        except Exception as e:
            print(f"隧道通道创建失败: {e}")
            local_sock.close()
            return

        while True:
            data = local_sock.recv(4096)
            if not data:
                break
            chan.send(data)
            try:
                data = chan.recv(4096)
                if not data:
                    break
                local_sock.send(data)
            except Exception:
                break

        chan.close()
        local_sock.close()

    # 建立连接
    conn = SSHConnection(host, user, port=port, key=key)
    if not conn.connect():
        return False
    transport = conn.client.get_transport()

    # 创建本地监听
    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        server_sock.bind(("127.0.0.1", local_port))
        server_sock.listen(5)
    except OSError as e:
        print(f"错误: 无法绑定本地端口 {local_port} - {e}")
        conn.close()
        return False

    print(f"隧道已建立: 127.0.0.1:{local_port} -> {remote_host}:{remote_port} (via {host})")
    print("按 Ctrl+C 停止隧道...")

    try:
        while True:
            client_sock, addr = server_sock.accept()
            t = threading.Thread(target=forward, args=(client_sock,))
            t.daemon = True
            t.start()
    except KeyboardInterrupt:
        print("\n隧道已关闭")
    finally:
        server_sock.close()
        conn.close()

    return True


# ==================== CLI 入口 ====================

def main():
    parser = argparse.ArgumentParser(
        description="SSH 远程连接管理工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    subparsers = parser.add_subparsers(dest="action", help="操作类型")

    # connect - 测试连接
    p_conn = subparsers.add_parser("connect", help="测试 SSH 连接")
    p_conn.add_argument("--host", required=True, help="目标主机")
    p_conn.add_argument("--user", required=True, help="用户名")
    p_conn.add_argument("--port", type=int, default=22, help="SSH 端口 (默认: 22)")
    p_conn.add_argument("--key", help="密钥文件路径")

    # run - 执行命令
    p_run = subparsers.add_parser("run", help="执行远程命令")
    p_run.add_argument("--host", required=True, help="目标主机")
    p_run.add_argument("--user", required=True, help="用户名")
    p_run.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_run.add_argument("--key", help="密钥文件路径")
    p_run.add_argument("--cmd", required=True, help="要执行的命令")
    p_run.add_argument("--timeout", type=int, default=30, help="命令超时秒数 (默认: 30)")
    p_run.add_argument("--json", action="store_true", help="以 JSON 格式输出")

    # upload - 上传文件
    p_up = subparsers.add_parser("upload", help="上传文件到远程")
    p_up.add_argument("--host", required=True, help="目标主机")
    p_up.add_argument("--user", required=True, help="用户名")
    p_up.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_up.add_argument("--key", help="密钥文件路径")
    p_up.add_argument("--local", required=True, help="本地文件路径")
    p_up.add_argument("--remote", required=True, help="远程文件路径")

    # download - 下载文件
    p_down = subparsers.add_parser("download", help="从远程下载文件")
    p_down.add_argument("--host", required=True, help="目标主机")
    p_down.add_argument("--user", required=True, help="用户名")
    p_down.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_down.add_argument("--key", help="密钥文件路径")
    p_down.add_argument("--remote", required=True, help="远程文件路径")
    p_down.add_argument("--local", required=True, help="本地文件路径")

    # batch - 批量执行
    p_batch = subparsers.add_parser("batch", help="批量多主机执行命令")
    p_batch.add_argument("--hosts", required=True, help="主机列表(逗号分隔)")
    p_batch.add_argument("--user", required=True, help="用户名")
    p_batch.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_batch.add_argument("--key", help="密钥文件路径")
    p_batch.add_argument("--cmd", required=True, help="要执行的命令")
    p_batch.add_argument("--timeout", type=int, default=30, help="命令超时秒数")

    # test - 连接测试
    p_test = subparsers.add_parser("test", help="测试主机 SSH 端口可达性")
    p_test.add_argument("--host", required=True, help="目标主机")
    p_test.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_test.add_argument("--timeout", type=int, default=5, help="超时秒数")

    # keygen - 生成密钥
    p_key = subparsers.add_parser("keygen", help="生成 SSH 密钥对")
    p_key.add_argument("--type", choices=["ed25519", "rsa"], default="ed25519", help="密钥类型")
    p_key.add_argument("--bits", type=int, default=4096, help="RSA 密钥位数")
    p_key.add_argument("--comment", default="", help="密钥注释")
    p_key.add_argument("--path", help="密钥保存路径")

    # tunnel - 端口转发
    p_tunnel = subparsers.add_parser("tunnel", help="创建本地端口转发隧道")
    p_tunnel.add_argument("--host", required=True, help="SSH 主机")
    p_tunnel.add_argument("--user", required=True, help="SSH 用户名")
    p_tunnel.add_argument("--port", type=int, default=22, help="SSH 端口")
    p_tunnel.add_argument("--key", help="密钥文件路径")
    p_tunnel.add_argument("--local-port", type=int, required=True, help="本地监听端口")
    p_tunnel.add_argument("--remote-host", default="localhost", help="远程目标主机")
    p_tunnel.add_argument("--remote-port", type=int, required=True, help="远程目标端口")

    args = parser.parse_args()

    if not args.action:
        parser.print_help()
        sys.exit(1)

    if args.action == "connect":
        conn = SSHConnection(args.host, args.user, port=args.port, key=args.key)
        if conn.connect():
            print(f"连接成功: {args.user}@{args.host}:{args.port}")
            result = conn.execute("uname -a && uptime")
            if result:
                print(f"远程系统: {result['stdout'].strip()}")
            conn.close()
        else:
            sys.exit(1)

    elif args.action == "run":
        conn = SSHConnection(args.host, args.user, port=args.port, key=args.key)
        if conn.connect():
            result = conn.execute(args.cmd, timeout=args.timeout)
            if result:
                if args.json:
                    print(json.dumps(result, ensure_ascii=False, indent=2))
                else:
                    if result["stdout"]:
                        print(result["stdout"])
                    if result["stderr"]:
                        print(f"[stderr] {result['stderr']}", file=sys.stderr)
                    sys.exit(result["exit_code"])
            conn.close()
        else:
            sys.exit(1)

    elif args.action == "upload":
        conn = SSHConnection(args.host, args.user, port=args.port, key=args.key)
        if conn.connect():
            conn.upload(args.local, args.remote)
            conn.close()

    elif args.action == "download":
        conn = SSHConnection(args.host, args.user, port=args.port, key=args.key)
        if conn.connect():
            conn.download(args.remote, args.local)
            conn.close()

    elif args.action == "batch":
        hosts = [h.strip() for h in args.hosts.split(",") if h.strip()]
        batch_execute(hosts, args.user, args.cmd, port=args.port, key=args.key, timeout=args.timeout)

    elif args.action == "test":
        success = test_connection(args.host, port=args.port, timeout=args.timeout)
        sys.exit(0 if success else 1)

    elif args.action == "keygen":
        generate_key(key_type=args.type, bits=args.bits, comment=args.comment, key_path=args.path)

    elif args.action == "tunnel":
        create_tunnel(
            args.host, args.user,
            local_port=args.local_port,
            remote_host=args.remote_host,
            remote_port=args.remote_port,
            port=args.port, key=args.key,
        )


if __name__ == "__main__":
    main()
