#!/usr/bin/env python3
"""
前端部署脚本 - 智壤卫士项目
在服务器上拉取代码、构建并部署
"""

import paramiko
import datetime
import time

# 配置
HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'
REMOTE_DIR = '/var/www/zrws'
PROJECT_DIR = '/root/workspace/ZRWS/code/html'

def run_command(client, cmd, timeout=60):
    """执行远程命令并返回结果"""
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8')
    err = stderr.read().decode('utf-8')
    return out, err

def deploy():
    """执行部署流程"""
    
    print(f"[1/7] 连接服务器 {HOST}...")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    try:
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print("✓ 连接成功")
    except Exception as e:
        print(f"✗ 连接失败: {e}")
        return False
    
    # 2. 拉取最新代码
    print("[2/7] 拉取最新代码...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && git pull origin main")
    print(out if out else err)
    
    # 3. 安装依赖
    print("[3/7] 安装依赖...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && npm install", timeout=180)
    if err and 'error' in err.lower():
        print(f"✗ 安装失败: {err}")
        return False
    print("✓ 依赖安装完成")
    
    # 4. 构建前端
    print("[4/7] 构建前端项目...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && npm run build", timeout=300)
    if err and 'error' in err.lower():
        print(f"✗ 构建失败: {err}")
        return False
    print("✓ 构建完成")
    
    # 5. 备份旧文件
    print("[5/7] 备份旧文件...")
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = f"{REMOTE_DIR}_backup_{timestamp}"
    run_command(client, f"if [ -d {REMOTE_DIR} ]; then mv {REMOTE_DIR} {backup_dir}; fi")
    print(f"✓ 旧文件已备份到 {backup_dir}")
    
    # 6. 复制构建产物到部署目录
    print("[6/7] 复制构建产物...")
    run_command(client, f"mkdir -p {REMOTE_DIR}")
    run_command(client, f"cp -r {PROJECT_DIR}/dist/* {REMOTE_DIR}/")
    run_command(client, f"chown -R www-data:www-data {REMOTE_DIR}")
    run_command(client, f"chmod -R 755 {REMOTE_DIR}")
    print("✓ 文件已复制并设置权限")
    
    # 7. 验证部署
    print("[7/7] 验证部署...")
    out, err = run_command(client, f"ls -la {REMOTE_DIR}/")
    print(f"部署目录内容:\n{out}")
    
    out, err = run_command(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
    status = out.strip()
    print(f"HTTP 状态码: {status}")
    
    if status == '200':
        print("\n✓✓✓ 部署成功！")
        print(f"访问地址: http://{HOST}/")
    else:
        print(f"\n⚠ HTTP 状态码异常: {status}")
    
    client.close()
    return True

if __name__ == '__main__':
    deploy()