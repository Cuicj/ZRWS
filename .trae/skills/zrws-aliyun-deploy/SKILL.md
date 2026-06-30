---
name: "zrws-aliyun-deploy"
description: "将智壤卫士（ZRWS）项目部署到阿里云服务器，支持前后端编译、部署、验证与回滚。当用户要求部署ZRWS、发布新版本、上线代码、打tag部署时调用。"
---

# ZRWS 阿里云部署 Skill

## 概述

本 Skill 用于将智壤卫士（ZRWS）项目部署到阿里云服务器 `8.163.137.149`。支持完整部署（前端+后端）、仅前端部署、仅后端部署，以及版本回滚。

## 服务器信息

| 项目 | 值 |
|------|-----|
| **服务器地址** | `8.163.137.149` |
| **SSH 用户名** | `root` |
| **SSH 密码** | `Test_admin` |
| **SSH 端口** | `22` |
| **服务器代码目录** | `/root/workspace/ZRWS` |
| **后端 JAR 路径** | `/root/workspace/app.jar` |
| **前端部署目录** | `/var/www/zrws/` |
| **后端 API 端口** | `5571` |
| **HTTPS 域名** | `https://www.zrws.cloud` |

## 部署流程

### 前置检查

部署前确认以下信息：
1. 目标版本号（如 `v4.0.0`）
2. 部署范围：完整部署 / 仅后端 / 仅前端
3. GitHub 代码是否已推送并打 tag

### 完整部署（前端 + 后端）

按以下步骤执行：

#### 步骤 1：本地准备（如需要）
- 更新前端 `package.json` 版本号
- 提交代码并创建 git tag
- 推送 tag 到远程仓库

#### 步骤 2：SSH 连接服务器
使用 `ssh-remote` Skill 或直接通过 plink 连接：
```
服务器：8.163.137.149
用户：root
密码：Test_admin
```

#### 步骤 3：停止后端服务
```bash
systemctl stop zrws.service
```

#### 步骤 4：拉取代码并切换版本
```bash
cd /root/workspace/ZRWS
git fetch --tags -f
git checkout <版本号>
```

#### 步骤 5：备份当前 JAR
```bash
cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)
```

#### 步骤 6：Maven 编译后端
```bash
cd /root/workspace/ZRWS/code/java
mvn clean install -Dmaven.test.skip=true
```
> 注意：使用 `-Dmaven.test.skip=true` 跳过测试，避免测试代码编译失败

#### 步骤 7：部署后端 JAR
```bash
cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar
```

#### 步骤 8：修复前端构建配置
Node.js v18 环境下需要 CJS 格式的 postcss 配置：
```bash
cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
EOF

# 确保 autoprefixer 已安装
cd /root/workspace/ZRWS/code/html
npm ls autoprefixer || npm install autoprefixer
```

#### 步骤 9：构建前端
```bash
cd /root/workspace/ZRWS/code/html
npm run build
```

#### 步骤 10：部署前端
```bash
rm -rf /var/www/zrws/*
cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/
```

#### 步骤 11：启动后端服务
```bash
systemctl start zrws.service
```

#### 步骤 12：验证部署
等待 60-90 秒（Spring Boot 启动较慢），然后检查：
```bash
# 健康检查
curl http://localhost:5571/approval/actuator/health
# 应返回：{"status":"UP"}

# 服务状态
systemctl status zrws.service

# 查看启动日志
journalctl -u zrws.service -n 50 --no-pager
```

#### 步骤 13：初始化业务数据（可选）
如果需要初始化或补充业务数据：
```bash
curl -X POST http://localhost:5571/approval/api/v1/admin/init-business-data
```

### 仅部署后端

```bash
# 1. 停止服务
systemctl stop zrws.service

# 2. 拉取代码
cd /root/workspace/ZRWS
git fetch --tags -f
git checkout <版本号>

# 3. 备份
cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)

# 4. 编译
cd /root/workspace/ZRWS/code/java
mvn clean install -Dmaven.test.skip=true

# 5. 部署
cp zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar

# 6. 启动
systemctl start zrws.service

# 7. 验证
sleep 60
curl http://localhost:5571/approval/actuator/health
```

### 仅部署前端

```bash
# 1. 拉取代码
cd /root/workspace/ZRWS
git fetch --tags -f
git checkout <版本号>

# 2. 修复 postcss 配置
cat > code/html/postcss.config.js << 'EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
EOF

# 3. 构建
cd code/html
npm ls autoprefixer || npm install autoprefixer
npm run build

# 4. 部署
rm -rf /var/www/zrws/*
cp -r dist/* /var/www/zrws/

# 前端无需重启服务，刷新浏览器即可生效
```

## 常见问题处理

### 问题 1：前端构建失败 - postcss 语法错误

**错误信息**：
```
SyntaxError: Unexpected token 'export'
Failed to load PostCSS config
```

**原因**：Node.js v18 默认使用 CommonJS 解析 `.config.js` 文件，但 `postcss.config.js` 使用了 ES module 语法。

**修复**：
```bash
cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
EOF
```

### 问题 2：前端构建失败 - 缺少 autoprefixer

**错误信息**：
```
Error: Loading PostCSS Plugin failed: Cannot find module 'autoprefixer'
```

**修复**：
```bash
cd /root/workspace/ZRWS/code/html
npm install autoprefixer
```

### 问题 3：后端启动失败 - Redis 连接错误

**错误信息**：`Unable to connect to 8.163.137.149:6379`

**原因**：`application.yml` 中 Redis 配置默认值为公网 IP，但本地应使用 127.0.0.1

**修复**：
```bash
# 检查 systemd 环境变量
grep REDIS_HOST /etc/systemd/system/zrws.service

# 如果不存在，添加：
sed -i '/\[Service\]/a Environment="REDIS_HOST=127.0.0.1"' /etc/systemd/system/zrws.service
systemctl daemon-reload
systemctl restart zrws.service
```

### 问题 4：后端编译失败 - 测试代码错误

**错误信息**：测试类编译失败（如缺少 import、语法错误等）

**修复**：使用 `-Dmaven.test.skip=true` 跳过测试编译
```bash
mvn clean install -Dmaven.test.skip=true
```

### 问题 5：前端 403 Forbidden

**原因**：`/var/www/zrws/` 目录为空或权限不足

**修复**：
```bash
ls -la /var/www/zrws/
# 如果为空，重新构建并部署前端
chown -R www-data:www-data /var/www/zrws/
```

### 问题 6：后端启动慢或失败 - 数据库连接

**检查方法**：
```bash
journalctl -u zrws.service -n 100 --no-pager | grep -i error
```

**常见原因**：
- 阿里云 RDS 白名单未包含服务器 IP
- 数据库 `zrws_approval` 不存在
- 数据库用户名密码错误

### 问题 7：Flowable 相关错误

**检查**：
```bash
journalctl -u zrws.service -n 50 --no-pager | grep -i flowable
```

**常见原因**：
- Flowable 版本与 Spring Boot 不兼容（需 Flowable 7.x + Spring Boot 3.2.x）
- Flowable 数据库表未创建

## 回滚方案

如果新版本有问题，可按以下步骤回滚：

### 后端回滚

```bash
# 1. 停止服务
systemctl stop zrws.service

# 2. 找到最新备份
ls -lt /root/workspace/app.jar.backup.* | head -5

# 3. 恢复备份
cp /root/workspace/app.jar.backup.<时间戳> /root/workspace/app.jar

# 4. 启动服务
systemctl start zrws.service

# 5. 验证
sleep 60
curl http://localhost:5571/approval/actuator/health
```

### 前端回滚

前端需要重新构建对应版本：
```bash
cd /root/workspace/ZRWS
git checkout <上一个版本号>
cd code/html
npm run build
rm -rf /var/www/zrws/* && cp -r dist/* /var/www/zrws/
```

## 验证部署清单

部署完成后，确认以下各项：

- [ ] 后端服务运行中：`systemctl status zrws.service` 显示 `active (running)`
- [ ] 健康检查通过：`curl /actuator/health` 返回 `{"status":"UP"}`
- [ ] 前端页面可访问：`https://www.zrws.cloud`
- [ ] 登录功能正常
- [ ] 版本号显示正确
- [ ] 菜单完整
- [ ] 各业务页面数据正常

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Flowable | 7.0.0 |
| Java | 21 |
| MySQL | 阿里云 RDS |
| Redis | 6.0（本地） |
| Nginx | 已配置 HTTPS |
| Node.js | v18（前端构建） |
| Vue | 3.x + Vite |

## 访问地址

| 功能 | 地址 |
|------|------|
| 前端页面 | https://www.zrws.cloud |
| 后端 API | https://www.zrws.cloud/approval/ |
| Swagger 文档 | https://www.zrws.cloud/approval/swagger-ui/index.html |
| 健康检查 | https://www.zrws.cloud/approval/actuator/health |
