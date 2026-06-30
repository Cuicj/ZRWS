#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智壤卫士 v4.0.0 部署脚本
使用: python deploy_server.py
"""

import paramiko
import time
import sys

# ========== 配置 ==========
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
        print(f"已连接到 {USERNAME}@{HOST}")

        # 检查系统信息
        exec_cmd(client, "uname -a && uptime && df -h /root")

        # ========== Step 2: 拉取代码并切换 Tag ==========
        print_step("Step 2: 拉取代码并切换到 v4.0.0")
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
            print(f"备份警告: {e}")

        # ========== Step 4: 停止后端服务 ==========
        print_step("Step 4: 停止后端服务")
        try:
            exec_cmd(client, "systemctl stop zrws.service")
            time.sleep(3)
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -10")
        except Exception as e:
            print(f"停止服务警告: {e}")

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

        # ========== Step 9: 部署公告栏到 /TZ ==========
        print_step("Step 9: 部署公告栏到 /TZ 路径")
        exec_cmd(client, f"mkdir -p {ANNOUNCEMENT_DIR}")
        exec_cmd(client, f"cp {REPO_DIR}/公告栏.html {ANNOUNCEMENT_DIR}/公告栏.html")
        exec_cmd(client, f"ls -la {ANNOUNCEMENT_DIR}/")

        # ========== Step 10: 更新 Nginx 配置 ==========
        print_step("Step 10: 更新 Nginx 配置支持 /TZ")

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

    # /TZ 公告栏
    location /TZ {{
        alias {ANNOUNCEMENT_DIR};
        index 公告栏.html;
        try_files $uri $uri/ /TZ/公告栏.html;

        location ~* \\\\.(js|css|png|jpg|gif|ico|svg|woff|woff2)$ {{
            expires 30d;
            add_header Cache-Control "public, immutable";
        }}

        location ~* \\\\.html$ {{
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

        location ~* \\\\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {{
            expires 7d;
            add_header Cache-Control "public, immutable";
        }}

        location ~* \\\\.html$ {{
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }}
    }}
}}
"""

        # 写入临时文件
        exec_cmd(client, f"cat > /tmp/nginx_zrws.conf << 'NGINXEOF'\n{nginx_config}\nNGINXEOF")

        # 备份并替换
        exec_cmd(client, f"cp /etc/nginx/sites-available/zrws /etc/nginx/sites-available/zrws.backup.{timestamp}")
        exec_cmd(client, f"cp /tmp/nginx_zrws.conf /etc/nginx/sites-available/zrws")

        # 测试并重载
        exec_cmd(client, "nginx -t")
        exec_cmd(client, "nginx -s reload")
        print("Nginx 配置已更新")

        # ========== Step 11: 启动后端服务 ==========
        print_step("Step 11: 启动后端服务")
        exec_cmd(client, "systemctl start zrws.service")

        print("\n等待 30 秒让服务启动...")
        time.sleep(30)

        # ========== Step 12: 验证部署 ==========
        print_step("Step 12: 验证部署")

        # 后端健康检查
        print("\n--- 后端健康检查 ---")
        try:
            result = exec_cmd(client, f"curl -s http://localhost:{BACKEND_PORT}/approval/actuator/health")
            if '"status":"UP"' in str(result):
                print("后端健康检查通过")
            else:
                print(f"后端状态: {result}")
        except Exception as e:
            print(f"后端健康检查失败: {e}")

        # 前端检查
        print("\n--- 前端检查 ---")
        try:
            result = exec_cmd(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
            status = result[-1] if result else "000"
            if status == "200":
                print("前端 HTTP 200 OK")
            else:
                print(f"前端 HTTP 状态: {status}")
        except Exception as e:
            print(f"前端检查失败: {e}")

        # 公告栏检查
        print("\n--- 公告栏 /TZ 检查 ---")
        try:
            result = exec_cmd(client, f"curl -s -o /dev/null -w '%{{http_code}}' http://localhost/TZ")
            status = result[-1] if result else "000"
            if status == "200":
                print("公告栏 /TZ HTTP 200 OK")
            else:
                print(f"公告栏 HTTP 状态: {status}")
        except Exception as e:
            print(f"公告栏检查失败: {e}")

        # 版本号检查
        print("\n--- 前端版本检查 ---")
        try:
            result = exec_cmd(client, "curl -s http://localhost/ | grep -o 'ZRWS v[0-9.]*' || echo '版本号未找到(可能在JS中)'")
            print(f"页面内容版本: {result}")
        except Exception as e:
            print(f"版本检查: {e}")

        # 查看服务状态
        print("\n--- 后端服务状态 ---")
        try:
            exec_cmd(client, "systemctl status zrws.service --no-pager | head -15")
        except:
            pass

        # ========== 完成 ==========
        print_step("部署完成!")
        print(f"""
        智壤卫士 {VERSION} 部署完成!

        访问地址:
           主系统:   https://www.zrws.cloud
           公告栏:   https://www.zrws.cloud/TZ
           后端 API: https://www.zrws.cloud/approval/
           Swagger:  https://www.zrws.cloud/approval/swagger-ui/index.html

        版本: {VERSION}
        部署时间: {time.strftime('%Y-%m-%d %H:%M:%S')}

        提示: 浏览器强刷新 (Ctrl+F5) 查看最新版本
        """)

    except Exception as e:
        print(f"\n部署失败: {e}")
        print("\n尝试回滚...")
        try:
            # 尝试恢复服务
            exec_cmd(client, f"ls -t {JAR_PATH}.backup.* 2>/dev/null | head -1")
            print("\n请手动回滚或检查错误")
        except:
            pass
        sys.exit(1)

    finally:
        client.close()
        print("\n已断开 SSH 连接")


if __name__ == '__main__':
    main()
