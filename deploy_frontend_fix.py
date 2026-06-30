#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
快速部署前端修复 - 仅构建和部署前端
"""

import paramiko
import sys
import time

HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'

REPO_DIR = '/root/workspace/ZRWS'
FRONTEND_DIR = '/var/www/zrws'


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
        print(f"✅ 已连接到 {USERNAME}@{HOST}")

        print_step("Step 2: 同步前端代码到服务器")
        import os
        local_html_dir = r'e:\AIdeom\智壤卫士\code\html'
        
        sftp = client.open_sftp()
        
        def upload_dir(local_dir, remote_dir):
            for root, dirs, files in os.walk(local_dir):
                rel_path = os.path.relpath(root, local_dir)
                remote_path = remote_dir if rel_path == '.' else remote_dir + '/' + rel_path.replace('\\', '/')
                try:
                    sftp.stat(remote_path)
                except:
                    sftp.mkdir(remote_path)
                for file in files:
                    if 'node_modules' in root or 'dist' in root or '.git' in root:
                        continue
                    local_file = os.path.join(root, file)
                    remote_file = remote_path + '/' + file
                    sftp.put(local_file, remote_file)
        
        print("  上传 src/utils/request.js...")
        sftp.put(
            r'e:\AIdeom\智壤卫士\code\html\src\utils\request.js',
            f'{REPO_DIR}/code/html/src/utils/request.js'
        )
        
        print("  上传 src/api/ 目录下的6个API文件...")
        api_files = [
            'geoStandard.js',
            'flowable.js',
            'dataImport.js',
            'report.js',
            'dataExport.js',
            'openApi.js'
        ]
        for f in api_files:
            sftp.put(
                f'e:\\AIdeom\\智壤卫士\\code\\html\\src\\api\\{f}',
                f'{REPO_DIR}/code/html/src/api/{f}'
            )
            print(f"    ✓ {f}")
        
        print("  上传 vite.config.js...")
        sftp.put(
            r'e:\AIdeom\智壤卫士\code\html\vite.config.js',
            f'{REPO_DIR}/code/html/vite.config.js'
        )
        
        sftp.close()
        print("✅ 文件上传完成")

        print_step("Step 3: 构建前端 (预计 1-2 分钟)")
        exec_cmd(client, f"cd {REPO_DIR}/code/html && npm run build", timeout=300)
        exec_cmd(client, f"ls -lh {REPO_DIR}/code/html/dist/")

        print_step("Step 4: 部署前端到 Nginx 目录")
        exec_cmd(client, f"rm -rf {FRONTEND_DIR}/*")
        exec_cmd(client, f"cp -r {REPO_DIR}/code/html/dist/* {FRONTEND_DIR}/")
        exec_cmd(client, f"ls {FRONTEND_DIR}/assets/ | head -10")

        print_step("Step 5: 验证部署")
        result = exec_cmd(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")
        status = result[-1] if result else "000"
        if status == "200":
            print("✅ 前端 HTTP 200 OK")
        else:
            print(f"⚠️  前端 HTTP 状态: {status}")

        print_step("部署完成！")
        print("")
        print("  修复内容:")
        print("  1. 修复 request.js baseURL: /api → /approval/api")
        print("  2. 修复 6个API文件路径前缀问题")
        print("  3. 同步更新 vite.config.js 代理配置")
        print("")
        print("  请刷新浏览器页面 (Ctrl+F5) 查看效果")
        print("")

    except Exception as e:
        print(f"\n❌ 部署失败: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        client.close()
        print("\n🔌 连接已关闭")


if __name__ == '__main__':
    main()
