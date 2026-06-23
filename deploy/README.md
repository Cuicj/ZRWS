# ZRWS 部署文档

## 部署架构

```
/app/
├── ZRWS/              # Git 仓库
│   ├── backend/       # 后端代码
│   ├── frontend/      # 前端代码
│   └── deploy/        # 部署脚本
├── app.jar            # 运行的应用
├── logs/              # 日志目录
│   ├── zrws.log
│   ├── deploy.log
│   └── webhook.log
└── backups/           # 备份目录
```

## 部署步骤

### 1. 环境准备

```bash
# 安装 Java 17
yum install java-17-openjdk java-17-openjdk-devel

# 安装 Maven
yum install maven

# 安装 Redis
yum install redis
systemctl start redis

# 安装 Nginx
yum install nginx
```

### 2. 克隆代码

```bash
cd /app
git clone https://github.com/Cuicj/ZRWS.git
```

### 3. 配置 systemd 服务

```bash
# 复制服务文件
cp /app/ZRWS/deploy/zrws.service /etc/systemd/system/

# 启用服务
systemctl daemon-reload
systemctl enable zrws
```

### 4. 配置 Nginx

```bash
# 复制 Nginx 配置
cp /app/ZRWS/deploy/nginx.conf /etc/nginx/conf.d/zrws.conf

# 重启 Nginx
systemctl restart nginx
```

### 5. 配置 Webhook（可选）

```bash
# 安装 Python 依赖
pip3 install flask

# 启动 Webhook 接收器
python3 /app/ZRWS/deploy/webhook_receiver.py &

# 或使用 systemd 管理
```

### 6. 首次部署

```bash
# 手动执行部署脚本
bash /app/ZRWS/deploy/deploy.sh
```

## 自动部署

### GitHub Webhook 配置

1. 在 GitHub 仓库设置中添加 Webhook
2. URL: `http://your-server:9000/`
3. Secret: 设置环境变量 `GITHUB_WEBHOOK_SECRET`
4. 触发事件: `push`

### GitHub Actions 配置

参考 `.github/workflows/deploy.yml`

## 常用命令

```bash
# 查看服务状态
systemctl status zrws

# 启动服务
systemctl start zrws

# 停止服务
systemctl stop zrws

# 重启服务
systemctl restart zrws

# 查看日志
tail -f /app/logs/zrws.log

# 查看部署日志
tail -f /app/logs/deploy.log
```

## 回滚

```bash
# 查看备份
ls -la /app/backups/

# 回滚到指定版本
cp /app/backups/app-20260623_120000.jar /app/app.jar
systemctl restart zrws
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `JAVA_HOME` | Java 安装路径 | `/usr/lib/jvm/java-17-openjdk` |
| `SPRING_PROFILES_ACTIVE` | Spring 配置 | `prod` |
| `OPENAI_API_KEY` | OpenAI API Key | - |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |