# 公告栏独立部署指南

## 部署架构

```
                    ┌─────────────────────────────────────┐
                    │           Nginx (www.zrws.cloud)     │
                    ├─────────────────────────────────────┤
                    │                                     │
                    │  /TZ        → /app/announcement/    │
                    │   公告栏全屏页面   (独立部署)         │
                    │                                     │
                    │  /TZ/api/* → 反向代理到后端         │
                    │                                     │
                    │  /          → /app/frontend/         │
                    │   主系统       (Vue应用)             │
                    │                                     │
                    │  /api/*     → 反向代理到后端         │
                    │                                     │
                    └───────────────┬─────────────────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────────────┐
                    │      Backend (127.0.0.1:8080)       │
                    │                                     │
                    │  /api/v1/announcement/*            │
                    │   公告栏数据接口                      │
                    └─────────────────────────────────────┘
```

## 快速部署

### 方式一：使用部署脚本

```bash
# 本地执行（将文件复制到服务器）
./deploy/deploy_announcement.sh 服务器IP

# 或手动上传
scp 公告栏.html root@服务器IP:/app/announcement/公告栏.html
```

### 方式二：手动部署

```bash
# 1. 创建目录
mkdir -p /app/announcement

# 2. 上传公告栏文件
scp 公告栏.html root@服务器IP:/app/announcement/公告栏.html

# 3. 更新 nginx 配置
# 将 deploy/nginx.conf 的内容复制到服务器
scp deploy/nginx.conf root@服务器IP:/etc/nginx/sites-available/zrws

# 4. 测试并重新加载 nginx
ssh root@服务器IP
nginx -t
nginx -s reload
```

## Nginx 配置说明

关键配置段：

```nginx
# 公告栏路径
location /TZ {
    alias /app/announcement;
    index 公告栏.html;
}

# 公告栏 API 代理
location /TZ/api/ {
    rewrite ^/TZ/api/(.*) /$1 break;
    proxy_pass http://127.0.0.1:8080/;
}
```

## 验证部署

部署完成后访问以下地址：

| 地址 | 说明 |
|------|------|
| https://www.zrws.cloud/TZ | 公告栏首页 |
| https://www.zrws.cloud/TZ/公告栏.html | 直接访问HTML文件 |
| https://www.zrws.cloud/TZ/api/v1/announcement/list | API接口测试 |

## 故障排查

### 1. 页面404
```bash
# 检查文件是否存在
ls -la /app/announcement/

# 检查nginx配置
nginx -t

# 查看nginx错误日志
tail -f /var/log/nginx/zrws-error.log
```

### 2. API请求失败
```bash
# 检查后端服务是否运行
curl http://127.0.0.1:8080/api/v1/announcement/list

# 检查nginx代理配置
# 确保 /TZ/api/ 代理到正确的后端地址
```

### 3. SSL证书问题
```bash
# 检查证书文件
ls -la /etc/nginx/ssl/

# 如果证书不存在，需要申请或配置
```

## 更新公告栏

更新公告栏页面后：

```bash
# 重新上传文件
scp 公告栏.html root@服务器IP:/app/announcement/公告栏.html

# 重新加载nginx（可选，HTML文件会立即生效）
ssh root@服务器IP "nginx -s reload"
```

## 独立部署模式

如果需要完全独立部署（不依赖主系统API），可以修改公告栏页面底部的 API 配置：

```javascript
// 使用公告栏专属后端服务
const API_BASE = '/TZ/api/v1';
```

需要同时部署公告栏专属后端服务，监听不同端口。

## 文件清单

| 文件 | 说明 | 部署位置 |
|------|------|----------|
| `公告栏.html` | 全屏公告栏页面 | /app/announcement/ |
| `deploy/nginx.conf` | Nginx配置 | /etc/nginx/sites-available/ |
| `deploy/deploy_announcement.sh` | 部署脚本 | 任意位置 |
