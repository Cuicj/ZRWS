#!/usr/bin/env python3
import paramiko
import time
import sys

# 服务器配置
HOST = '8.163.137.149'
PORT = 22
USER = 'root'
PASSWORD = 'Test_admin'

def run_command(ssh, command, timeout=120):
    """执行远程命令并返回输出"""
    print(f"\n>>> 执行命令: {command}")
    stdin, stdout, stderr = ssh.exec_command(command, timeout=timeout)
    output = stdout.read().decode('utf-8')
    error = stderr.read().decode('utf-8')
    if error:
        print(f"错误输出: {error}")
    print(f"输出:\n{output}")
    return output

def main():
    # 创建SSH客户端
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    print(f"正在连接到 {HOST}...")
    client.connect(HOST, port=PORT, username=USER, password=PASSWORD, timeout=30)
    print("连接成功!")
    
    # 1. 拉取GitHub仓库代码并切换到v4.0.0标签
    print("\n" + "="*60)
    print("步骤1: 拉取GitHub仓库代码并切换到v4.0.0标签")
    print("="*60)
    
    # 检查是否已有代码目录
    output = run_command(client, "ls -la /root/ | head -20")
    
    # 克隆或更新仓库
    if 'zrws' in output:
        print("仓库已存在，执行git pull...")
        run_command(client, "cd /root/zrws && git pull origin main && git checkout v4.0.0")
    else:
        print("克隆仓库...")
        run_command(client, "cd /root && git clone https://github.com/your-repo/zrws.git && cd zrws && git checkout v4.0.0")
    
    # 2. Maven编译后端
    print("\n" + "="*60)
    print("步骤2: Maven编译后端")
    print("="*60)
    run_command(client, "cd /root/zrws && mvn clean package -DskipTests", timeout=300)
    
    # 3. 部署前端到/var/www/zrws/
    print("\n" + "="*60)
    print("步骤3: 部署前端到/var/www/zrws/")
    print("="*60)
    run_command(client, "mkdir -p /var/www/zrws")
    run_command(client, "cd /root/zrws && rsync -avz --delete frontend/ root@8.163.137.149:/var/www/zrws/ || cp -r frontend/* /var/www/zrws/")
    
    # 4. 部署公告栏到/app/announcement/
    print("\n" + "="*60)
    print("步骤4: 部署公告栏到/app/announcement/")
    print("="*60)
    run_command(client, "mkdir -p /app/announcement")
    run_command(client, "cd /root/zrws && cp 公告栏/公告栏.html /app/announcement/")
    
    # 5. 更新nginx配置支持/TZ路径
    print("\n" + "="*60)
    print("步骤5: 更新nginx配置支持/TZ路径")
    print("="*60)
    nginx_config = """
    location /TZ {
        alias /var/www/zrws;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    """
    run_command(client, f"cat >> /etc/nginx/conf.d/zrws.conf << 'EOF'\n{nginx_config}\nEOF")
    run_command(client, "nginx -t")
    
    # 6. 重启后端服务并验证
    print("\n" + "="*60)
    print("步骤6: 重启后端服务并验证")
    print("="*60)
    run_command(client, "systemctl restart zrws-backend || pkill -f java && nohup java -jar /root/zrws/backend/target/*.jar &")
    time.sleep(5)
    run_command(client, "systemctl status zrws-backend || ps aux | grep java")
    
    print("\n" + "="*60)
    print("部署完成!")
    print("="*60)
    
    client.close()

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"错误: {e}")
        sys.exit(1)
