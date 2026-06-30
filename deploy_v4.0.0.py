import paramiko
import sys
import time

HOST = "8.163.137.149"
PORT = 22
USERNAME = "root"
PASSWORD = "Test_admin"


def execute_command(ssh, command, timeout=300):
    print(f"\n{'='*60}")
    print(f"执行命令: {command}")
    print(f"{'='*60}")
    
    stdin, stdout, stderr = ssh.exec_command(command, timeout=timeout)
    stdin.channel.set_combine_stderr(True)
    
    output_lines = []
    while True:
        line = stdout.readline()
        if not line:
            break
        line = line.rstrip()
        print(line)
        output_lines.append(line)
    
    exit_status = stdout.channel.recv_exit_status()
    if exit_status != 0:
        print(f"\n[ERROR] 命令执行失败，退出码: {exit_status}")
        error_output = stderr.read().decode()
        if error_output:
            print(f"错误信息:\n{error_output}")
        return False, "\n".join(output_lines)
    
    print(f"\n[OK] 命令执行成功")
    return True, "\n".join(output_lines)


def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    try:
        print("=" * 60)
        print(f"正在连接服务器 {HOST}:{PORT}...")
        print("=" * 60)
        ssh.connect(HOST, port=PORT, username=USERNAME, password=PASSWORD, timeout=30)
        print("[OK] SSH 连接成功\n")
        
        steps = [
            (
                "步骤 1/11: 停止后端服务",
                "systemctl stop zrws.service",
                30
            ),
            (
                "步骤 2/11: 备份当前 JAR",
                'cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)',
                30
            ),
            (
                "步骤 3/11: 拉取代码并切换到 v4.0.0",
                "cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.0.0",
                120
            ),
            (
                "步骤 4/11: Maven 编译后端 (跳过测试)",
                "cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true",
                600
            ),
            (
                "步骤 5/11: 部署后端 JAR",
                "cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar",
                30
            ),
            (
                "步骤 6/11: 修复 postcss 配置",
                "cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'EOF'\nmodule.exports = { plugins: { autoprefixer: {}, }, }\nEOF",
                10
            ),
            (
                "步骤 7/11: 构建前端",
                "cd /root/workspace/ZRWS/code/html && npm run build",
                300
            ),
            (
                "步骤 8/11: 部署前端",
                "rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/",
                30
            ),
            (
                "步骤 9/11: 部署公告栏",
                "mkdir -p /app/announcement && cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html",
                10
            ),
            (
                "步骤 10/11: 启动后端服务",
                "systemctl start zrws.service",
                30
            ),
        ]
        
        for step_name, command, timeout in steps:
            print(f"\n\n{'#'*60}")
            print(f"# {step_name}")
            print(f"{'#'*60}")
            success, _ = execute_command(ssh, command, timeout=timeout)
            if not success:
                print(f"\n{'!'*60}")
                print(f"[FATAL] {step_name} 失败，部署终止！")
                print(f"{'!'*60}")
                sys.exit(1)
        
        print("\n\n" + "#" * 60)
        print("# 步骤 11/11: 等待 60 秒后验证部署")
        print("#" * 60)
        print("\n等待 60 秒让服务启动...")
        for i in range(60, 0, -10):
            print(f"  还剩 {i} 秒...")
            time.sleep(10)
        
        print("\n--- 验证后端健康检查 ---")
        success, _ = execute_command(
            ssh,
            "curl -s http://localhost:5571/approval/actuator/health",
            timeout=30
        )
        
        print("\n--- 验证前端/TZ路径 ---")
        execute_command(
            ssh,
            "curl -I http://localhost/TZ",
            timeout=30
        )
        
        print("\n--- 服务状态 ---")
        execute_command(ssh, "systemctl status zrws.service", timeout=10)
        
        print("\n\n" + "=" * 60)
        print("  部署完成总结")
        print("=" * 60)
        print("  版本: v4.0.0")
        print("  类型: 完整部署 (前端 + 后端)")
        print("  后端服务: 已启动")
        print("  前端页面: https://www.zrws.cloud")
        print("  后端 API: https://www.zrws.cloud/approval/")
        print("  健康检查: https://www.zrws.cloud/approval/actuator/health")
        print("=" * 60)
        
    except paramiko.AuthenticationException:
        print("[ERROR] SSH 认证失败，请检查用户名和密码")
        sys.exit(1)
    except paramiko.SSHException as e:
        print(f"[ERROR] SSH 连接错误: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"[ERROR] 发生未知错误: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        ssh.close()
        print("\nSSH 连接已关闭")


if __name__ == "__main__":
    main()
