#!/usr/bin/env python3
# webhook_receiver.py - Webhook 接收器
# 用于接收 GitHub Webhook 并触发自动部署

import os
import json
import subprocess
import hashlib
import hmac
from http.server import HTTPServer, BaseHTTPRequestHandler
from datetime import datetime

# 配置
PORT = 9000
SECRET = os.environ.get('GITHUB_WEBHOOK_SECRET', 'your-secret-key')
DEPLOY_SCRIPT = '/app/ZRWS/deploy/deploy.sh'
LOG_FILE = '/app/logs/webhook.log'

def log(message):
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    log_message = f"[{timestamp}] {message}"
    print(log_message)
    with open(LOG_FILE, 'a') as f:
        f.write(log_message + '\n')

def verify_signature(payload, signature):
    """验证 GitHub Webhook 签名"""
    if not signature:
        return False
    expected_signature = 'sha256=' + hmac.new(
        SECRET.encode(),
        payload,
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(signature, expected_signature)

class WebhookHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        # 获取请求头
        content_length = int(self.headers.get('Content-Length', 0))
        signature = self.headers.get('X-Hub-Signature-256', '')
        event_type = self.headers.get('X-GitHub-Event', '')

        # 读取请求体
        payload = self.rfile.read(content_length)

        # 验证签名
        if not verify_signature(payload, signature):
            log('签名验证失败')
            self.send_response(401)
            self.end_headers()
            return

        # 解析请求
        try:
            data = json.loads(payload)
        except json.JSONDecodeError:
            log('JSON 解析失败')
            self.send_response(400)
            self.end_headers()
            return

        # 处理 push 事件
        if event_type == 'push':
            ref = data.get('ref', '')
            if ref == 'refs/heads/main':
                log(f'收到 push 事件: {data.get("repository", {}).get("name", "")}')
                log(f'提交者: {data.get("pusher", {}).get("name", "")}')
                log(f'提交信息: {data.get("head_commit", {}).get("message", "")}')
                
                # 触发部署
                try:
                    subprocess.run(['bash', DEPLOY_SCRIPT], check=True)
                    log('部署脚本执行成功')
                except subprocess.CalledProcessError as e:
                    log(f'部署脚本执行失败: {e}')
                
                self.send_response(200)
                self.end_headers()
                self.wfile.write(b'{"status": "success", "message": "Deployment triggered"}')
            else:
                log(f'忽略非 main 分支的 push: {ref}')
                self.send_response(200)
                self.end_headers()
        else:
            log(f'忽略事件: {event_type}')
            self.send_response(200)
            self.end_headers()

    def log_message(self, format, *args):
        log(f'HTTP: {args[0]}')

def main():
    os.makedirs('/app/logs', exist_ok=True)
    log(f'Webhook 接收器启动，监听端口 {PORT}')
    server = HTTPServer(('0.0.0.0', PORT), WebhookHandler)
    server.serve_forever()

if __name__ == '__main__':
    main()