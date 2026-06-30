#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ZRWS v4.0.0 部署脚本 - 按照 zrws-aliyun-deploy 部署指南
"""

import paramiko
import time
import sys

HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'
VERSION = 'v4.0.0'

REPO_DIR = '/root/workspace/ZRWS'
JAR_PATH = '/root/workspace/app.jar'
FRONTEND_DIR = '/var/www/zrws'
ANNOUNCEMENT_DIR = '/app/announcement'
BACKEND_PORT = 5571


def print_step(step, msg):
    print(f"\n{'='*60}")
    print(f"  [步骤 {step}] {msg}")
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
        # Step 0: 连接服务器
        print_step("0", "连接服务器")
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print(f"✅ 已连接到 {USERNAME}@{HOST}")

        # 检查系统信息
        exec_cmd(client, "uname -a && uptime && df -h /root")

        # Step 1: 停止后端服务并备份
        print_step("1", "停止后端服务并备份JAR")
        try:
            exec_cmd(client, "systemctl stop zrws.service")
            time.sleep(2)
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -5")
        except Exception as e:
            print(f"⚠️  停止服务: {e}")

        timestamp = time.strftime("%Y%m%d_%H%M%S")
        try:
            exec_cmd(client, f"cp {JAR_PATH} {JAR_PATH}.backup.{timestamp}")
            exec_cmd(client, f"ls -lh {JAR_PATH}.backup.* | tail -3")
        except Exception as e:
            print(f"⚠️  备份: {e}")

        # Step 2: 拉取代码并切换版本
        print_step("2", f"拉取代码并切换到 {VERSION}")
        exec_cmd(client, f"cd {REPO_DIR} && git fetch --tags -f")
        exec_cmd(client, f"cd {REPO_DIR} && git checkout {VERSION}")
        exec_cmd(client, f"cd {REPO_DIR} && git log --oneline -3")

        # Step 3: Maven 编译后端
        print_step("3", "Maven 编译后端 (跳过测试)")
        exec_cmd(client, f"cd {REPO_DIR}/code/java && mvn clean install -Dmaven.test.skip=true", timeout=600)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/java/zrws-approval/target/*.jar")

        # Step 4: 部署后端JAR
        print_step("4", "部署后端JAR")
        exec_cmd(client, f"cp {REPO_DIR}/code/java/zrws-approval/target/zrws-approval-*.jar {JAR_PATH}")
        exec_cmd(client, f"ls -lh {JAR_PATH}")

        # Step 5: 修复 postcss 配置并构建前端
        print_step("5", "修复 postcss 配置并构建前端")

        # 修复 postcss.config.js
        postcss_fix = f"""
cat > {REPO_DIR}/code/html/postcss.config.js << 'EOF'
module.exports = {{
  plugins: {{
    autoprefixer: {{}},
  }},
}}
EOF
cat {REPO_DIR}/code/html/postcss.config.js
        """
        exec_cmd(client, postcss_fix)

        # 确保 autoprefixer 已安装
        exec_cmd(client, f"cd {REPO_DIR}/code/html && (npm ls autoprefixer || npm install autoprefixer)")

        # 构建前端
        exec_cmd(client, f"cd {REPO_DIR}/code/html && npm run build", timeout=300)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/html/dist/")

        # Step 6: 部署前端
        print_step("6", "部署前端到 Nginx")
        exec_cmd(client, f"rm -rf {FRONTEND_DIR}/*")
        exec_cmd(client, f"cp -r {REPO_DIR}/code/html/dist/* {FRONTEND_DIR}/")
        exec_cmd(client, f"ls -la {FRONTEND_DIR}/")

        # Step 7: 部署公告栏到 /TZ
        print_step("7", "部署公告栏到 /TZ 路径")
        exec_cmd(client, f"mkdir -p {ANNOUNCEMENT_DIR}")
        exec_cmd(client, f"cp {REPO_DIR}/公告栏.html {ANNOUNCEMENT_DIR}/公告栏.html")
        exec_cmd(client, f"ls -la {ANNOUNCEMENT_DIR}/")

        # Step 8: 更新 Nginx 配置
        print_step("8", "更新 Nginx 配置支持 /TZ")

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
    ssl_session_timeout 1d;
    ssl_protocols TLSv1.2 TLSv1.3;

    # /TZ 公告栏
    location /TZ {{
        alias {ANNOUNCEMENT_DIR};
        index 公告栏.html;
        try_files $uri $uri/ /TZ/公告栏.html;
        
        location ~* \\.(js|css|png|jpg|gif|ico|svg|woff|woff2)$ {{
            expires 30d;
            add_header Cache-Control "public, immutable";
        }}
        
        location ~* \\.html$ {{
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }}
    }}

    # 公告栏 API 代理
    location /TZ/api/ {{
        rewrite ^/TZ/api/(.*) /$1 break;
        proxy_pass http://zrws_backend/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }}

    # 主系统 API
    location /approval/ {{
        proxy_pass http://zrws_backend/approval/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 50m;
    }}

    # 前端
    location / {{
        root {FRONTEND_DIR};
        index index.html;
        try_files $uri $uri/ /index.html;
        
        location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {{
            expires 7d;
            add_header Cache-Control "public, immutable";
        }}
        
        location ~* \\.html$ {{
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }}
    }}
}}
"""
        # 写入 nginx 配置
        exec_cmd(client, f"cp /etc/nginx/sites-available/zrws /etc/nginx/sites-available/zrws.backup.{timestamp}")
        
        # 使用 heredoc 写入
        exec_cmd(client, f"cat > /tmp/nginx_zrws.conf << 'NGINXEOF'\n{nginx_config}\nNGINXEOF")
        exec_cmd(client, f"cp /tmp/nginx_zrws.conf /etc/nginx/sites-available/zrws")
        
        # 测试并重载
        exec_cmd(client, "nginx -t")
        exec_cmd(client, "nginx -s reload")
        print("✅ Nginx 配置已更新")

        # Step 9: 启动后端服务并验证
        print_step("9", "启动后端服务并验证")
        exec_cmd(client, "systemctl start zrws.service")

        print("\n⏳ 等待 60 秒让服务启动...")
        time.sleep(60)

        # 健康检查
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
            print(f"  HTTP 状态: {result}")
        except Exception as e:
            print(f"❌ 前端检查失败: {e}")

        # 公告栏检查
        print("\n--- 公告栏 /TZ 检查 ---")
        try:
            result = exec_cmd(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/TZ")
            status = result[-1] if result else "000"
            print(f"  HTTP 状态: {status}")
            if status == "200" or status == "301":
                print("✅ 公告栏可访问")
        except Exception as e:
            print(f"❌ 公告栏检查失败: {e}")

        # 服务状态
        print("\n--- 服务状态 ---")
        try:
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -15")
        except:
            pass

        # 完成
        print_step("完成", "部署完成!")
        print(f"""
        🎉 智壤卫士 {VERSION} 部署完成!

        📌 访问地址:
           主系统:   https://www.zrws.cloud
           公告栏:   https://www.zrws.cloud/TZ
           后端 API: https://www.zrws.cloud/approval/
           Swagger:  https://www.zrws.cloud/approval/swagger-ui/index.html

        📌 版本: {VERSION}
        📌 部署时间: {time.strftime('%Y-%m-%d %H:%M:%S')}

        💡 提示: 浏览器强刷新 (Ctrl+F5) 查看最新版本
        """)

    except Exception as e:
        print(f"\n❌ 部署失败: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

    finally:
        client.close()
        print("\n🔌 已断开 SSH 连接")


if __name__ == '__main__':
    main()
