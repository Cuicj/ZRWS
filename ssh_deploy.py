#!/usr/bin/env python3
import subprocess
import sys

# Server configuration
HOST = "8.163.137.149"
USER = "root"
PASSWORD = "Test_admin"

# Test connection
print("正在测试SSH连接...")
result = subprocess.run([
    "ssh", "-o", "StrictHostKeyChecking=no",
    f"{USER}@{HOST}", "echo 'SSH连接成功' && uname -a"
], input=PASSWORD + "\n", capture_output=True, text=True, encoding='utf-8')

print("STDOUT:", result.stdout)
print("STDERR:", result.stderr)
print("Return code:", result.returncode)
