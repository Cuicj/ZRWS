#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智壤卫士 v3.4.0 部署脚本
- 拉取代码并切换到 v3.4.0 tag
- Maven 编译后端
- 部署后端 JAR 并重启服务
- 构建前端
- 部署前端到 Nginx 目录
"""

import paramiko
import time
import sys

# ========== 配置 ==========
HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'
VERSION = 'v3.4.0'

REPO_DIR = '/root/workspace/ZRWS'
JAR_PATH = '/root/workspace/app.jar'
FRONTEND_DIR = '/var/www/zrws'
BACKEND_PORT = 5571


def print_step(msg):
    print(f"\n{'='*60}")
    print(f"  {msg}")
    print(f"{'='*60}\n")


def exec_cmd(client, cmd, timeout=300):
    """执行远程命令，实时输出"""
    print(f"$ {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout, get_pty=True)

    output = []
    while True:
        try:
            line = stdout.readline()
            if not line:
                break
            line = line.rstrip()
            if line:
                print(f"  {line}")
                output.append(line)
        except:
            break

    exit_code = stdout.channel.recv_exit_status()
    err = stderr.read().decode('utf-8', errors='ignore').strip()
    if err and exit_code != 0:
        print(f"  [错误] {err}")

    if exit_code != 0:
        raise RuntimeError(f"命令执行失败 (exit code {exit_code}): {cmd}")

    return output


def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        # ========== Step 1: 连接服务器 ==========
        print_step("Step 1: 连接服务器")
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print(f"✅ 已连接到 {USERNAME}@{HOST}")

        # 检查系统信息
        exec_cmd(client, "uname -a && uptime && df -h /root")

        # ========== Step 2: 拉取代码并切换 Tag ==========
        print_step("Step 2: 拉取代码并切换到 v3.4.0")
        exec_cmd(client, f"cd {REPO_DIR} && git fetch --tags")
        exec_cmd(client, f"cd {REPO_DIR} && git checkout {VERSION}")
        exec_cmd(client, f"cd {REPO_DIR} && git log --oneline -3")

        # ========== Step 3: 备份当前 JAR ==========
        print_step("Step 3: 备份当前 JAR")
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        backup_cmd = f"cp {JAR_PATH} {JAR_PATH}.backup.{timestamp} && ls -lh {JAR_PATH}.backup.* | tail -3"
        try:
            exec_cmd(client, backup_cmd)
        except Exception as e:
            print(f"⚠️  备份警告: {e}")

        # ========== Step 4: 停止后端服务 ==========
        print_step("Step 4: 停止后端服务")
        try:
            exec_cmd(client, "systemctl stop zrws.service")
            time.sleep(3)
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -10")
        except Exception as e:
            print(f"⚠️  停止服务警告: {e}")

        # ========== Step 5: Maven 编译后端 ==========
        print_step("Step 5: Maven 编译后端 (预计 3-5 分钟)")
        exec_cmd(client, f"cd {REPO_DIR}/code/java && mvn clean package -DskipTests", timeout=600)

        # 检查构建产物
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/java/zrws-approval/target/*.jar")

        # ========== Step 6: 部署后端 JAR ==========
        print_step("Step 6: 部署后端 JAR")
        exec_cmd(client, f"cp {REPO_DIR}/code/java/zrws-approval/target/zrws-approval-*.jar {JAR_PATH}")
        exec_cmd(client, f"ls -lh {JAR_PATH}")

        # ========== Step 7: 构建前端 ==========
        print_step("Step 7: 构建前端 (预计 1-2 分钟)")

        # 检查并修复 postcss.config.js (Node 18 需 CJS 格式)
        postcss_check = f"""
        if grep -q 'export default' {REPO_DIR}/code/html/postcss.config.js 2>/dev/null; then
            echo '检测到 ESM 格式，转换为 CJS...'
            cat > {REPO_DIR}/code/html/postcss.config.js << 'POSTCSSEOF'
module.exports = {{
  plugins: {{
    autoprefixer: {{}},
  }},
}}
POSTCSSEOF
        fi
        cat {REPO_DIR}/code/html/postcss.config.js
        """
        exec_cmd(client, postcss_check)

        exec_cmd(client, f"cd {REPO_DIR}/code/html && npm run build", timeout=300)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/html/dist/")

        # ========== Step 8: 部署前端 ==========
        print_step("Step 8: 部署前端到 Nginx 目录")
        exec_cmd(client, f"rm -rf {FRONTEND_DIR}/*")
        exec_cmd(client, f"cp -r {REPO_DIR}/code/html/dist/* {FRONTEND_DIR}/")
        exec_cmd(client, f"ls -la {FRONTEND_DIR}/")
        exec_cmd(client, f"ls {FRONTEND_DIR}/assets/ | head -10")

        # ========== Step 9: 启动后端服务 ==========
        print_step("Step 9: 启动后端服务")
        exec_cmd(client, "systemctl start zrws.service")

        print("\n⏳ 等待 30 秒让服务启动...")
        time.sleep(30)

        # ========== Step 10: 验证部署 ==========
        print_step("Step 10: 验证部署")

        # 后端健康检查
        print("\n--- 后端健康检查 ---")
        try:
            result = exec_cmd(client, f"curl -s http://localhost:{BACKEND_PORT}/approval/actuator/health")
            if '"status":"UP"' in str(result):
                print("✅ 后端健康检查通过")
            else:
                print(f"⚠️  后端状态: {result}")
        except Exception as e:
            print(f"❌ 后端健康检查失败: {e}")

        # 前端检查
        print("\n--- 前端检查 ---")
        try:
            result = exec_cmd(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
            status = result[-1] if result else "000"
            if status == "200":
                print("✅ 前端 HTTP 200 OK")
            else:
                print(f"⚠️  前端 HTTP 状态: {status}")
        except Exception as e:
            print(f"❌ 前端检查失败: {e}")

        # 版本号检查
        print("\n--- 前端版本检查 ---")
        try:
            result = exec_cmd(client, "curl -s http://localhost/ | grep -o 'ZRWS v[0-9.]*' || echo '版本号未找到(可能在JS中)'")
            print(f"  页面内容版本: {result}")
        except Exception as e:
            print(f"⚠️  版本检查: {e}")

        # 查看服务状态
        print("\n--- 后端服务状态 ---")
        try:
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -15")
        except:
            pass

        # ========== 完成 ==========
        print_step("✅ 部署完成!")
        print(f"
        🎉 智壤卫士 {VERSION} 部署完成!

        📌 访问地址:
           前端: https://www.zrws.cloud
           后端: https://www.zrws.cloud/approval/
           Swagger: https://www.zrws.cloud/approval/swagger-ui/index.html

        📌 版本: {VERSION}
        📌 部署时间: {time.strftime('%Y-%m-%d %H:%M:%S')}

        💡 提示: 浏览器强刷新 (Ctrl+F5) 查看最新版本
        """)

    except Exception as e:
        print(f"\n❌ 部署失败: {e}")
        print("\n尝试回滚...")
        try:
            # 尝试恢复服务
            exec_cmd(client, f"ls -t {JAR_PATH}.backup.* 2>/dev/null | head -1")
            print("\n⚠️  请手动回滚或检查错误")
        except:
            pass
        sys.exit(1)

    finally:
        client.close()
        print("\n🔌 已断开 SSH 连接")


if __name__ == '__main__':
    main()
