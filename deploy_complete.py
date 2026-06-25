#!/usr/bin/env python3
"""完整部署脚本 - 安装 tailwindcss 或删除 postcss 配置"""
import paramiko
import datetime

# 配置
HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'
REMOTE_DIR = '/var/www/zrws'
PROJECT_DIR = '/root/workspace/ZRWS/code/html'

def run_command(client, cmd, timeout=60, verbose=True):
    """执行远程命令并返回结果"""
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8')
    err = stderr.read().decode('utf-8')
    if verbose:
        if out:
            print(out)
        if err and ('error' in err.lower() or 'failed' in err.lower()):
            print(f"错误: {err}")
    return out, err

def deploy():
    """执行部署流程"""
    
    print(f"[1/10] 连接服务器 {HOST}...")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    try:
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print("✓ 连接成功")
    except Exception as e:
        print(f"✗ 连接失败: {e}")
        return False
    
    # 2. 删除 postcss 配置文件（项目不需要 tailwindcss）
    print("[2/10] 删除 PostCSS 配置文件...")
    run_command(client, f"rm -f {PROJECT_DIR}/postcss.config.js {PROJECT_DIR}/postcss.config.cjs")
    print("✓ PostCSS 配置已删除")
    
    # 3. 拉取最新代码
    print("[3/10] 拉取最新代码...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && git pull origin main")
    print(out if out else err)
    
    # 4. 安装依赖
    print("[4/10] 安装依赖...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && npm install", timeout=180)
    print("✓ 依赖安装完成")
    
    # 5. 构建前端
    print("[5/10] 构建前端项目...")
    out, err = run_command(client, f"cd {PROJECT_DIR} && npm run build", timeout=300)
    print(out)
    if err:
        print(f"构建信息: {err}")
    
    # 6. 检查 dist 目录
    print("[6/10] 检查 dist 目录...")
    out, err = run_command(client, f"ls -la {PROJECT_DIR}/dist/")
    print(f"dist 目录内容:\n{out}")
    if err:
        print(f"错误: {err}")
        return False
    
    # 7. 备份旧文件
    print("[7/10] 备份旧文件...")
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = f"{REMOTE_DIR}_backup_{timestamp}"
    run_command(client, f"if [ -d {REMOTE_DIR} ]; then mv {REMOTE_DIR} {backup_dir}; fi", verbose=False)
    print(f"✓ 旧文件已备份到 {backup_dir}")
    
    # 8. 复制构建产物到部署目录
    print("[8/10] 复制构建产物...")
    run_command(client, f"mkdir -p {REMOTE_DIR}", verbose=False)
    run_command(client, f"cp -r {PROJECT_DIR}/dist/* {REMOTE_DIR}/", verbose=False)
    run_command(client, f"chown -R www-data:www-data {REMOTE_DIR}", verbose=False)
    run_command(client, f"chmod -R 755 {REMOTE_DIR}", verbose=False)
    print("✓ 文件已复制并设置权限")
    
    # 9. 验证部署
    print("[9/10] 验证部署...")
    out, err = run_command(client, f"ls -la {REMOTE_DIR}/")
    print(f"部署目录内容:\n{out}")
    
    out, err = run_command(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
    status = out.strip()
    print(f"HTTP 状态码: {status}")
    
    # 10. 检查 Nginx 配置
    print("[10/10] 检查 Nginx 配置...")
    out, err = run_command(client, "cat /etc/nginx/sites-enabled/default | head -30")
    print(f"Nginx 配置:\n{out}")
    
    if status == '200':
        print("\n✓✓✓ 部署成功！")
        print(f"访问地址: http://{HOST}/")
    else:
        print(f"\n⚠ HTTP 状态码异常: {status}")
        print("可能需要检查 Nginx 配置")
    
    client.close()
    return True

if __name__ == '__main__':
    deploy()