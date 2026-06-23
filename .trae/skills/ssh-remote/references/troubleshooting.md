# SSH 故障排查指南

## 连接问题

### 1. Connection timed out（连接超时）

**可能原因**：
- 目标主机不可达（网络问题）
- 防火墙阻止了 SSH 端口
- 目标主机 sshd 未运行

**排查步骤**：
```powershell
# 1. 测试网络连通性
Test-Connection -ComputerName host -Count 3

# 2. 测试端口是否开放
Test-NetConnection -ComputerName host -Port 22

# 3. 使用详细模式查看连接过程
ssh -vvv user@host
```

**解决方案**：
- 检查目标主机防火墙规则：`sudo ufw allow 22` 或 `sudo firewall-cmd --add-service=ssh`
- 检查 sshd 是否运行：`sudo systemctl status sshd`
- 检查云服务器安全组是否放行 22 端口

### 2. Connection refused（连接被拒绝）

**可能原因**：
- SSH 服务未启动
- SSH 端口不是默认的 22
- sshd 配置错误

**排查步骤**：
```powershell
# 检查远程 sshd 状态（需要其他访问方式）
ssh user@host "sudo systemctl status sshd"

# 尝试不同端口
ssh -p 2222 user@host
```

### 3. Permission denied (publickey)

**可能原因**：
- 密钥不匹配
- 密钥文件权限不正确
- authorized_keys 配置有误

**排查步骤**：
```powershell
# 1. 检查本地密钥权限
icacls ~/.ssh/id_ed25519  # Windows
ls -la ~/.ssh/             # Linux/Mac

# 2. 使用详细模式查看认证过程
ssh -vvv user@host

# 3. 检查远程 authorized_keys
ssh user@host "ls -la ~/.ssh/ && cat ~/.ssh/authorized_keys"
```

**解决方案**：
```powershell
# 修复权限（Linux/Mac）
chmod 700 ~/.ssh
chmod 600 ~/.ssh/id_*
chmod 644 ~/.ssh/id_*.pub
chmod 644 ~/.ssh/authorized_keys

# 重新复制公钥
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@host
```

### 4. Host key verification failed

**可能原因**：
- 远程主机重装系统后密钥变更
- 中间人攻击

**排查步骤**：
```powershell
# 查看冲突的密钥
ssh-keygen -l -f ~/.ssh/known_hosts

# 查看远程主机当前密钥
ssh-keyscan host
```

**解决方案**：
```powershell
# 删除旧的主机密钥
ssh-keygen -R host

# 重新添加（手动确认）
ssh user@host
```

### 5. Too many authentication failures

**可能原因**：
- ssh-agent 中加载了过多无效密钥
- sshd 配置 MaxAuthTries 过低

**解决方案**：
```powershell
# 指定具体密钥
ssh -i ~/.ssh/correct_key user@host

# 清除 ssh-agent 中的密钥后重新添加
ssh-add -D
ssh-add ~/.ssh/correct_key
```

## 性能问题

### 1. SSH 连接很慢

**排查步骤**：
```powershell
# 使用详细模式查看卡在哪一步
ssh -vvv user@host 2>&1 | Select-String "debug"
```

**常见原因和解决方案**：
```powershell
# DNS 反向解析慢 → 禁用 DNS 查找
ssh -o UseDNS=no user@host

# GSSAPI 认证慢 → 禁用
ssh -o GSSAPIAuthentication=no user@host

# 在 sshd_config 中永久配置
# UseDNS no
# GSSAPIAuthentication no
```

### 2. 连接频繁断开

**解决方案**：
```powershell
# 增加心跳保活
ssh -o ServerAliveInterval=30 -o ServerAliveCountMax=5 user@host

# 或在 ~/.ssh/config 中配置
Host *
    ServerAliveInterval 30
    ServerAliveCountMax 5
```

## 文件传输问题

### 1. SCP 传输中断

**解决方案**：
```powershell
# 使用 rsync 替代（支持断点续传）
rsync -avz --partial --progress file user@host:/path/

# 限制带宽避免拥塞
scp -l 8192 file user@host:/path/
```

### 2. 传输速度慢

**解决方案**：
```powershell
# 启用压缩
scp -C file user@host:/path/

# 使用 rsync 压缩传输
rsync -avz --progress file user@host:/path/

# 更换加密算法（更快但安全性略低）
scp -c aes128-gcm@openssh.com file user@host:/path/
```

## 端口转发问题

### 1. 端口转发不生效

**排查步骤**：
```powershell
# 检查本地端口是否被占用
netstat -ano | findstr "8080"

# 检查远程端口是否可达
ssh user@host "nc -zv localhost 3306"

# 检查远程服务是否监听正确地址
ssh user@host "ss -tlnp | grep 3306"
```

**注意**：远程服务需要监听 `127.0.0.1` 或 `0.0.0.0`，如果只监听特定网卡地址，端口转发可能无法连接。

### 2. Address already in use

**解决方案**：
```powershell
# 更换本地端口
ssh -L 8081:localhost:80 user@host

# 或杀掉占用端口的进程
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process
```

## Windows 特有问题

### 1. PowerShell 中 SSH 命令不可用

**解决方案**：
```powershell
# 检查 OpenSSH 是否安装
Get-WindowsCapability -Online | Where-Object Name -like 'OpenSSH*'

# 安装 OpenSSH 客户端
Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0

# 安装 OpenSSH 服务器
Add-WindowsCapability -Online -Name OpenSSH.Server~~~~0.0.1.0
```

### 2. 密钥权限问题（Windows）

**解决方案**：
```powershell
# 修复私钥权限（仅当前用户可访问）
icacls ~/.ssh\id_ed25519 /inheritance:r
icacls ~/.ssh\id_ed25519 /grant:r "$($env:USERNAME):(R)"
```

### 3. ssh-agent 在 Windows 上不工作

**解决方案**：
```powershell
# 以管理员身份启动服务
Get-Service ssh-agent | Set-Service -StartupType Automatic
Start-Service ssh-agent

# 添加密钥
ssh-add ~/.ssh\id_ed25519
```
