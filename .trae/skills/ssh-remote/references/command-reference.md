# SSH 命令参考

## ssh 命令

### 基本语法
```powershell
ssh [选项] [用户@]主机 [命令]
```

### 常用选项

| 选项 | 说明 | 示例 |
|-----|------|------|
| `-p` | 指定端口 | `ssh -p 2222 user@host` |
| `-i` | 指定密钥文件 | `ssh -i ~/.ssh/mykey user@host` |
| `-l` | 指定登录用户 | `ssh -l user host` |
| `-J` | 跳板机代理 | `ssh -J jumphost targethost` |
| `-L` | 本地端口转发 | `ssh -L 8080:localhost:80 user@host` |
| `-R` | 远程端口转发 | `ssh -R 3306:localhost:3306 user@host` |
| `-D` | 动态端口转发(SOCKS) | `ssh -D 1080 user@host` |
| `-N` | 不执行远程命令（仅转发） | `ssh -N -L 8080:localhost:80 user@host` |
| `-f` | 后台运行 | `ssh -fN -D 1080 user@host` |
| `-T` | 不分配终端 | `ssh -T user@host` |
| `-t` | 强制分配终端 | `ssh -t user@host "vim file"` |
| `-v` | 调试模式（显示连接详情） | `ssh -v user@host` |
| `-vv` | 更详细的调试 | `ssh -vv user@host` |
| `-o` | 指定配置选项 | `ssh -o StrictHostKeyChecking=no user@host` |

### 常用 -o 配置选项

| 选项 | 说明 | 推荐值 |
|-----|------|--------|
| `ConnectTimeout` | 连接超时（秒） | `10` |
| `ServerAliveInterval` | 心跳间隔（秒） | `60` |
| `ServerAliveCountMax` | 最大心跳失败次数 | `3` |
| `StrictHostKeyChecking` | 主机密钥检查 | `ask` / `yes` / `no` |
| `BatchMode` | 批处理模式（不交互） | `yes` |
| `UserKnownHostsFile` | known_hosts 文件路径 | `~/.ssh/known_hosts` |
| `ProxyCommand` | 代理命令 | `nc -X connect -x proxy:port %h %p` |
| `IdentityFile` | 密钥文件 | `~/.ssh/id_ed25519` |
| `ForwardAgent` | 转发 ssh-agent | `yes` |

## scp 命令

### 基本语法
```powershell
scp [选项] 源路径 目标路径
```

### 常用选项

| 选项 | 说明 | 示例 |
|-----|------|------|
| `-P` | 指定端口（注意大写） | `scp -P 2222 file user@host:/path` |
| `-r` | 递归复制目录 | `scp -r dir/ user@host:/path` |
| `-i` | 指定密钥 | `scp -i ~/.ssh/key file user@host:/path` |
| `-C` | 启用压缩 | `scp -C largefile user@host:/path` |
| `-l` | 限制带宽（Kbit/s） | `scp -l 1024 file user@host:/path` |
| `-q` | 静默模式 | `scp -q file user@host:/path` |
| `-p` | 保留文件属性 | `scp -p file user@host:/path` |

## sftp 命令

### 常用操作
```powershell
sftp user@host
# 进入后可用命令：
# ls, cd, pwd        — 远程目录操作
# lls, lcd, lpwd     — 本地目录操作
# get remote local    — 下载文件
# put local remote    — 上传文件
# mget *.log          — 批量下载
# mput *.txt          — 批量上传
# rm remote           — 删除远程文件
# mkdir remote        — 创建远程目录
# bye / exit          — 退出
```

## ssh-keygen 命令

### 生成密钥
```powershell
# Ed25519（推荐）
ssh-keygen -t ed25519 -C "your_email@example.com" -f ~/.ssh/id_ed25519

# RSA 4096
ssh-keygen -t rsa -b 4096 -C "your_email@example.com" -f ~/.ssh/id_rsa

# 带密码保护
ssh-keygen -t ed25519 -P "passphrase"

# 从 PEM 转换为 OpenSSH 格式
ssh-keygen -p -f ~/.ssh/id_rsa -m pem
```

### 密钥管理
```powershell
# 查看公钥指纹
ssh-keygen -lf ~/.ssh/id_ed25519.pub

# 查看所有密钥的指纹
ssh-keygen -lE md5 -f ~/.ssh/known_hosts

# 删除 known_hosts 中的旧条目
ssh-keygen -R hostname

# 生成 known_hosts 的哈希版本
ssh-keygen -H -f ~/.ssh/known_hosts
```

## ssh-copy-id 命令

```powershell
# 复制公钥到远程主机
ssh-copy-id user@host

# 指定端口
ssh-copy-id -p 2222 user@host

# 指定密钥
ssh-copy-id -i ~/.ssh/custom_key.pub user@host
```

## ssh-agent 命令

```powershell
# 启动 ssh-agent
eval $(ssh-agent)

# 添加密钥到 agent
ssh-add ~/.ssh/id_ed25519

# 添加所有默认密钥
ssh-add

# 列出已加载的密钥
ssh-add -l

# 删除所有密钥
ssh-add -D
```

## ssh_config 配置文件

### 文件位置
- 全局：`/etc/ssh/ssh_config`
- 用户：`~/.ssh/config`

### 配置示例
```
# 生产服务器
Host prod
    HostName 192.168.1.100
    User deploy
    Port 22
    IdentityFile ~/.ssh/id_ed25519_prod
    ForwardAgent yes
    ServerAliveInterval 60

# 通过跳板机访问内网
Host internal-*
    ProxyJump jumphost
    User admin

Host jumphost
    HostName jump.example.com
    User jumpuser
    IdentityFile ~/.ssh/id_ed25519_jump

Host internal-db
    HostName 10.0.0.50
    Port 22
```

## rsync 通过 SSH

```powershell
# 基本同步
rsync -avz -e ssh local_dir/ user@host:/remote_dir/

# 指定端口
rsync -avz -e "ssh -p 2222" local_dir/ user@host:/remote_dir/

# 显示进度
rsync -avz --progress local_dir/ user@host:/remote_dir/

# 排除文件
rsync -avz --exclude='*.log' local_dir/ user@host:/remote_dir/

# 删除目标端多余文件（镜像同步）
rsync -avz --delete local_dir/ user@host:/remote_dir/

# 断点续传
rsync -avz --partial --progress large_file user@host:/remote_path/
```
