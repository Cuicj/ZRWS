# ZRWS 项目部署文档

> 最后更新：2026-06-29  
> 适用版本：v3.4.0+

---

## 1. 服务器信息

| 项目 | 值 |
|------|-----|
| **服务器地址** | `8.163.137.149` |
| **SSH 用户名** | `root` |
| **SSH 密码** | `Test_admin` |
| **SSH 端口** | `22` |

```bash
ssh root@8.163.137.149
# 密码：Test_admin
```

---

## 2. 项目信息

| 项目 | 值 |
|------|-----|
| **GitHub 仓库** | https://github.com/Cuicj/ZRWS |
| **服务器代码目录** | `/root/workspace/ZRWS` |
| **后端 JAR 路径** | `/root/workspace/app.jar` |
| **前端部署目录** | `/var/www/zrws/` |
| **后端 API 端口** | `5571` |
| **Webhook 端口** | `9095` |
| **HTTPS 域名** | `https://www.zrws.cloud` |

---

## 3. 数据库与缓存

### 数据库（阿里云 RDS）

| 项目 | 值 |
|------|-----|
| **地址** | `rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com:3306` |
| **数据库名** | `zrws_approval` |
| **已加白名单** | ✅ 服务器 `8.163.137.149` |

### Redis（本地）

| 项目 | 值 |
|------|-----|
| **地址** | `127.0.0.1:6379` |
| **安装状态** | ✅ 已安装并运行 |

---

## 4. 服务管理

### systemd 服务

| 服务名 | 说明 | 状态 |
|--------|------|------|
| `zrws.service` | 后端 Spring Boot 应用 | active (running) |
| `webhook-receiver.service` | GitHub Webhook 接收器 | active (running) |

### 常用命令

```bash
# 查看后端状态
systemctl status zrws.service

# 启动后端
systemctl start zrws.service

# 停止后端
systemctl stop zrws.service

# 重启后端
systemctl restart zrws.service

# 查看实时日志
journalctl -u zrws.service -f

# 查看最近 100 行日志
journalctl -u zrws.service -n 100 --no-pager
```

---

## 5. 手动部署流程

### 5.1 完整部署（后端 + 前端）

```bash
# SSH 登录服务器
ssh root@8.163.137.149
# 输入密码：Test_admin

# ========== 1. 停止后端服务 ==========
systemctl stop zrws.service

# ========== 2. 拉取代码并切换 Tag ==========
cd /root/workspace/ZRWS
git fetch --tags
git checkout v3.4.0   # 替换为目标版本号

# ========== 3. 备份当前 JAR ==========
cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)

# ========== 4. Maven 编译后端 ==========
cd /root/workspace/ZRWS/code/java
mvn clean package -DskipTests
# 预计耗时：3-5 分钟

# ========== 5. 部署后端 JAR ==========
cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar

# ========== 6. 构建前端 ==========
cd /root/workspace/ZRWS/code/html

# 检查 postcss.config.js 格式（Node.js v18 需使用 CJS 格式）
# 如果文件内容是 "export default"，需改为 "module.exports = ..."
npm run build
# 预计耗时：1-2 分钟

# ========== 7. 部署前端 ==========
rm -rf /var/www/zrws/*
cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/

# ========== 8. 启动后端服务 ==========
systemctl start zrws.service

# ========== 9. 验证启动 ==========
# 等待 30 秒后检查
sleep 30
curl http://localhost:5571/approval/actuator/health
# 应返回：{"status":"UP"}
```

### 5.2 仅部署后端

```bash
ssh root@8.163.137.149
systemctl stop zrws.service

cd /root/workspace/ZRWS
git fetch --tags
git checkout <tag名>
cd code/java && mvn clean package -DskipTests
cp zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar

systemctl start zrws.service
```

### 5.3 仅部署前端

```bash
ssh root@8.163.137.149
cd /root/workspace/ZRWS
git checkout <tag名>
cd code/html && npm run build
rm -rf /var/www/zrws/* && cp -r dist/* /var/www/zrws/
# 前端无需重启服务，刷新浏览器即可
```

---

## 6. 自动部署（Webhook）

当向 GitHub 推送新的 Git Tag 时，服务器会自动拉取代码并部署。

### Webhook 配置

| 项目 | 值 |
|------|-----|
| **Webhook 地址** | `http://8.163.137.149:9095/webhook` |
| **触发事件** | Push event（仅处理 tag push） |
| **服务状态** | ✅ 运行中 |

### 使用方式

```bash
# 本地打 tag 并推送，触发自动部署
git tag v3.4.1
git push origin v3.4.1

# 服务器 Webhook 接收器会自动：
# 1. 拉取新 tag
# 2. Maven 编译后端
# 3. 部署 JAR 并重启服务
```

---

## 7. 验证部署

### 7.1 后端健康检查

```bash
curl http://localhost:5571/approval/actuator/health
# 正常返回：{"status":"UP"}
```

### 7.2 访问地址

| 功能 | 地址 |
|------|------|
| 前端页面 | https://www.zrws.cloud |
| 后端 API | https://www.zrws.cloud/approval/ |
| Swagger 文档 | https://www.zrws.cloud/approval/swagger-ui/index.html |
| 健康检查 | https://www.zrws.cloud/approval/actuator/health |

### 7.3 端口检查

```bash
# 检查后端端口
netstat -tlnp | grep :5571

# 检查 Webhook 端口
netstat -tlnp | grep :9095

# 检查 HTTPS 端口
netstat -tlnp | grep :443
```

---

## 8. 常见问题处理

### 8.1 后端启动失败（Redis 连接错误）

**错误**：`Unable to connect to 8.163.137.149:6379`

**原因**：`application.yml` 中 Redis 配置默认值为公网 IP

**修复**：
```bash
# 检查 systemd 环境变量
grep REDIS_HOST /etc/systemd/system/zrws.service
# 如果不存在，添加：
sed -i '/\[Service\]/a Environment="REDIS_HOST=127.0.0.1"' /etc/systemd/system/zrws.service
systemctl daemon-reload
systemctl restart zrws.service
```

### 8.2 前端构建失败（postcss.config.js）

**错误**：`SyntaxError: Unexpected token 'export'`

**原因**：Node.js v18 默认用 CommonJS 解析 `.config.js` 文件

**修复**：
```bash
# 将 postcss.config.js 改为 CJS 格式
cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
EOF
```

### 8.3 403 Forbidden（前端）

**原因**：`/var/www/zrws/` 目录为空

**修复**：
```bash
ls /var/www/zrws/
# 如果为空，重新构建并部署前端（见 5.3）
```

### 8.4 Flowable 启动失败

**检查**：查看日志中是否有数据库连接错误
```bash
journalctl -u zrws.service -n 50 --no-pager | grep -i error
```

**常见原因**：
- 阿里云 RDS 白名单未包含服务器 IP
- 数据库 `zrws_approval` 不存在
- Flowable 版本与 Spring Boot 不兼容（需用 Flowable 7.x 配 Spring Boot 3.2.x）

---

## 9. 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Flowable | 7.0.0 |
| Spring AI | 1.0.0-M2 |
| Java | 21 |
| MySQL | 阿里云 RDS |
| Redis | 6.0 |
| Nginx | 已配置 HTTPS |
| Node.js | v18（前端构建）|

---

## 10. 目录结构

```
/root/workspace/
├── ZRWS/                    # Git 仓库
│   ├── code/java/           # 后端 Spring Boot 项目
│   │   ├── pom.xml          # 父 POM
│   │   └── zrws-approval/   # 后端主模块
│   ├── code/html/           # 前端 Vue 项目
│   │   ├── dist/            # 构建产物（部署用）
│   │   └── postcss.config.js
│   └── deploy.sh            # 部署脚本（可选）
├── app.jar                  # 后端运行 JAR（systemd 使用）
└── app.jar.backup.*         # 历史备份

/var/www/zrws/               # 前端部署目录（Nginx 指向此处）
├── index.html
├── assets/
└── cesium/

/etc/systemd/system/
├── zrws.service             # 后端服务配置
└── webhook-receiver.service # Webhook 服务配置

/etc/nginx/sites-available/
└── zrws                     # Nginx 配置（HTTPS + 反向代理）
```

---

## 11. 回滚方案

如果新版本有问题，快速回滚：

```bash
# 停止服务
systemctl stop zrws.service

# 恢复备份的 JAR
ls /root/workspace/app.jar.backup.* | tail -1   # 找到最新备份
cp /root/workspace/app.jar.backup.<时间戳> /root/workspace/app.jar

# 重启服务
systemctl start zrws.service

# 验证
curl http://localhost:5571/approval/actuator/health
```

---

*部署文档结束*
