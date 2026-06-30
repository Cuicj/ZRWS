#!/usr/bin/env python3
"""部署主分支最新代码（强制覆盖本地修改）"""

import paramiko
import time
import sys

HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'

REPO_DIR = '/root/workspace/ZRWS'
JAR_PATH = '/root/workspace/app.jar'
FRONTEND_DIR = '/var/www/zrws'
ANNOUNCEMENT_DIR = '/app/announcement'
BACKEND_PORT = 5571


def print_step(msg):
    print(f"\n{'='*60}")
    print(f"  {msg}")
    print(f"{'='*60}\n")


def exec_cmd(client, cmd, timeout=300):
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
        print_step("Step 1: 连接服务器")
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print(f"已连接到 {USERNAME}@{HOST}")

        exec_cmd(client, "uname -a && uptime && df -h /root")

        # Step 2: 强制切换到主分支（丢弃本地修改）
        print_step("Step 2: 切换到主分支最新代码（强制覆盖）")
        exec_cmd(client, f"cd {REPO_DIR} && git checkout -f main && git pull origin main")
        exec_cmd(client, f"cd {REPO_DIR} && git log --oneline -3")

        # Step 3: 备份
        print_step("Step 3: 备份当前 JAR")
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        exec_cmd(client, f"cp {JAR_PATH} {JAR_PATH}.backup.{timestamp} && ls -lh {JAR_PATH}.backup.* | tail -3")

        # Step 4: 停止服务
        print_step("Step 4: 停止后端服务")
        try:
            exec_cmd(client, "systemctl stop zrws.service")
            time.sleep(3)
        except:
            pass

        # Step 5: Maven 编译
        print_step("Step 5: Maven 编译后端")
        exec_cmd(client, f"cd {REPO_DIR}/code/java && mvn clean package -DskipTests", timeout=600)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/java/zrws-approval/target/*.jar")

        # Step 6: 部署后端 JAR
        print_step("Step 6: 部署后端 JAR")
        exec_cmd(client, f"cp {REPO_DIR}/code/java/zrws-approval/target/zrws-approval-*.jar {JAR_PATH}")
        exec_cmd(client, f"ls -lh {JAR_PATH}")

        # Step 7: 构建前端
        print_step("Step 7: 构建前端")
        exec_cmd(client, f"cd {REPO_DIR}/code/html && npm run build", timeout=300)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/html/dist/")

        # Step 8: 部署前端
        print_step("Step 8: 部署前端")
        exec_cmd(client, f"rm -rf {FRONTEND_DIR}/*")
        exec_cmd(client, f"cp -r {REPO_DIR}/code/html/dist/* {FRONTEND_DIR}/")
        exec_cmd(client, f"ls -la {FRONTEND_DIR}/")

        # Step 9: 部署公告栏
        print_step("Step 9: 部署公告栏")
        exec_cmd(client, f"mkdir -p {ANNOUNCEMENT_DIR}")
        exec_cmd(client, f"cp {REPO_DIR}/公告栏.html {ANNOUNCEMENT_DIR}/公告栏.html")
        exec_cmd(client, f"ls -la {ANNOUNCEMENT_DIR}/")

        # Step 10: Nginx 配置
        print_step("Step 10: 更新 Nginx 配置")
        nginx_config = f"""upstream zrws_backend {{
    server 127.0.0.1:{BACKEND_PORT};
    keepalive 32;
}}

server {{
    listen 80;
    server_name www.zrws.cloud zrws.cloud;
    return 301 https://$server_name$request_uri;
}}

server {{
    listen 443 ssl http2;
    server_name www.zrws.cloud zrws.cloud;
    ssl_certificate /etc/nginx/ssl/zrws.crt;
    ssl_certificate_key /etc/nginx/ssl/zrws.key;

    location /TZ {{
        alias {ANNOUNCEMENT_DIR};
        index 公告栏.html;
        try_files $uri $uri/ /TZ/公告栏.html;
    }}

    location /TZ/api/ {{
        rewrite ^/TZ/api/(.*) /$1 break;
        proxy_pass http://zrws_backend/;
    }}

    location /approval/ {{
        proxy_pass http://zrws_backend/approval/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        client_max_body_size 50m;
    }}

    location / {{
        root {FRONTEND_DIR};
        index index.html;
        try_files $uri $uri/ /index.html;
    }}
}}
"""
        exec_cmd(client, f"cat > /tmp/nginx_zrws.conf << 'NGINXEOF'\n{nginx_config}\nNGINXEOF")
        exec_cmd(client, f"cp /etc/nginx/sites-available/zrws /etc/nginx/sites-available/zrws.backup.{timestamp}")
        exec_cmd(client, f"cp /tmp/nginx_zrws.conf /etc/nginx/sites-available/zrws")
        exec_cmd(client, "nginx -t")
        exec_cmd(client, "nginx -s reload")

        # Step 11: 启动服务
        print_step("Step 11: 启动后端服务")
        exec_cmd(client, "systemctl start zrws.service")
        print("\n等待 30 秒...")
        time.sleep(30)

        # Step 12: 验证
        print_step("Step 12: 验证部署")
        try:
            result = exec_cmd(client, f"curl -s http://localhost:{BACKEND_PORT}/approval/actuator/health")
            if '"status":"UP"' in str(result):
                print("后端健康检查通过")
        except:
            pass

        try:
            result = exec_cmd(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
            print(f"前端 HTTP 状态: {result[-1] if result else '000'}")
        except:
            pass

        try:
            result = exec_cmd(client, f"curl -s -o /dev/null -w '%{{http_code}}' http://localhost/TZ")
            print(f"公告栏 /TZ HTTP 状态: {result[-1] if result else '000'}")
        except:
            pass

        print_step("部署完成!")
        print(f"""
        智壤卫士 主分支最新版本 部署完成!
        访问地址:
           主系统:   https://www.zrws.cloud
           公告栏:   https://www.zrws.cloud/TZ
        """)

    except Exception as e:
        print(f"\n部署失败: {e}")
        sys.exit(1)
    finally:
        client.close()
        print("\n已断开 SSH 连接")


if __name__ == '__main__':
    main()
