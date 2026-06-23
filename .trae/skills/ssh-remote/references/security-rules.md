# SSH 安全规范

## 认证安全

### 推荐认证方式优先级

1. **SSH 密钥认证（ed25519）** — 最推荐
   - `ssh-keygen -t ed25519 -C "comment"`
   - 抗量子计算，密钥更短更安全

2. **SSH 密钥认证（RSA 4096+）** — 次选
   - `ssh-keygen -t rsa -b 4096 -C "comment"`
   - 兼容性更好

3. **密码认证** — 不推荐用于自动化
   - 仅在密钥不可用时使用
   - 必须使用强密码（12位以上，含大小写、数字、特殊字符）

### 密钥管理规则

- 私钥文件权限必须为 `600`：`chmod 600 ~/.ssh/id_*`
- `authorized_keys` 文件权限必须为 `644`
- `~/.ssh/` 目录权限必须为 `700`
- 定期轮换密钥（建议每 90 天）
- 禁用空密码密钥

## 连接安全

### SSH Config 安全配置

```
# ~/.ssh/config 安全推荐配置
Host *
    ServerAliveInterval 60
    ServerAliveCountMax 3
    StrictHostKeyChecking ask
    UserKnownHostsFile ~/.ssh/known_hosts
    AddKeysToAgent yes
    IdentityFile ~/.ssh/id_ed25519
    ConnectTimeout 10
```

### 服务端安全配置（sshd_config）

```
# 禁用 root 远程登录
PermitRootLogin no

# 禁用密码认证（仅密钥）
PasswordAuthentication no

# 禁用空密码
PermitEmptyPasswords no

# 修改默认端口（可选）
Port 22

# 限制最大认证尝试
MaxAuthTries 3

# 禁用 X11 转发（不需要时）
X11Forwarding no

# 设置登录超时
LoginGraceTime 30

# 限制允许的用户
AllowUsers deploy admin
```

## 操作安全

### 权限分级

| 级别 | 允许操作 | 是否需要确认 |
|-----|---------|------------|
| L0 只读 | `ls`, `cat`, `head`, `tail`, `grep`, `find`, `df`, `free`, `top`, `ps`, `uname`, `uptime`, `whoami`, `pwd`, `echo` | 不需要 |
| L1 查询 | `netstat`, `ss`, `ip`, `ifconfig`, `docker ps`, `kubectl get`, `systemctl status` | 不需要 |
| L2 轻量写入 | `touch`, `mkdir`, `cp`, `mv`, `chmod`, `chown`, `tee`, `echo >` | 需要说明 |
| L3 服务管理 | `systemctl restart/stop/start`, `service`, `docker restart`, `kubectl rollout` | 需要确认 |
| L4 危险操作 | `rm -rf`, `shutdown`, `reboot`, `dd`, `mkfs`, `DROP TABLE` | 必须确认 |
| L5 系统级 | `fdisk`, `parted`, `mount`, `umount`, `iptables`, `firewall-cmd` | 必须确认 |

### 会话安全

- 每次远程操作完成后，确认连接已正确关闭
- 不在远程会话中保存敏感信息到文件
- 使用 `set -o history -o histexpand` 避免敏感命令被记录到 bash history
- 临时禁用 history：`unset HISTFILE`

## 网络安全

### 防火墙规则建议

- SSH 端口仅对必要 IP 段开放
- 使用 fail2ban 防止暴力破解
- 考虑使用 VPN 或跳板机间接访问

### 跳板机 / Bastion Host

对于生产环境，推荐通过跳板机访问：

```powershell
# 通过跳板机访问内网服务器
ssh -J jump_user@jump_host:jump_port target_user@target_host "command"
```

## 审计与日志

### 建议记录的操作日志

每次通过 AI 助手执行的远程操作应记录：
- 操作时间
- 目标主机
- 执行的命令
- 命令输出摘要
- 操作结果（成功/失败）
- 是否需要用户确认及确认结果
