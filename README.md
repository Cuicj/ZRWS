# ZRWS 智壤卫士

智慧土壤监测与审批管理系统

## 项目结构

```
ZRWS/
├── code/
│   ├── java/                    # 后端 (Spring Boot 3.2.5 + Java 17)
│   │   ├── pom.xml             # 父 POM
│   │   ├── zrws-common/        # 公共模块
│   │   ├── zrws-approval/      # 审批主模块
│   │   └── lib/                # 本地依赖 (Spring AI 1.0.0-M2)
│   ├── html/                    # 前端 (Vue 3 + Vite)
│   ├── mobile/                  # 移动端 H5
│   ├── uniapp/                  # UniApp 移动端
│   ├── python/                  # Python 脚本
│   └── sql/                     # 数据库脚本
├── deploy/                      # 部署相关
│   ├── deploy.sh               # 部署脚本
│   ├── webhook_receiver.py     # GitHub Webhook 接收器
│   ├── zrws.service            # systemd 服务文件
│   ├── nginx.conf              # Nginx 配置模板
│   └── README.md               # 部署文档
├── doc/                         # 项目文档
├── .github/
│   └── workflows/
│       └── deploy.yml          # GitHub Actions CI/CD
├── .gitignore
└── README.md
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Spring AI 1.0.0-M2
- Flowable 7.0.0 (工作流引擎)
- MySQL 8.0+
- Redis 6.0+

### 前端
- Vue 3
- Vite
- Element Plus

### 移动端
- UniApp

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 本地开发

```bash
# 后端
cd code/java
mvn clean install -DskipTests
cd zrws-approval
mvn spring-boot:run

# 前端
cd code/html
npm install
npm run dev
```

## 部署说明

### 服务器部署

详细部署文档请查看 [deploy/README.md](deploy/README.md)

#### 快速部署

```bash
# 1. 克隆仓库
git clone https://github.com/your-org/ZRWS.git /app/ZRWS
cd /app/ZRWS

# 2. 安装 systemd 服务
sudo cp deploy/zrws.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable zrws

# 3. 配置 Nginx
sudo cp deploy/nginx.conf /etc/nginx/sites-available/zrws
sudo ln -s /etc/nginx/sites-available/zrws /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# 4. 首次部署
chmod +x deploy/deploy.sh
./deploy/deploy.sh
```

### GitHub Actions 自动部署

项目已配置 GitHub Actions CI/CD，推送 tag 时自动触发部署：

```bash
# 创建并推送 tag
git tag v1.0.0
git push origin v1.0.0
```

需要在 GitHub 仓库设置中配置以下 Secrets：
- `SERVER_HOST`: 服务器地址
- `SERVER_USER`: SSH 用户名
- `SSH_PRIVATE_KEY`: SSH 私钥
- `SERVER_PORT`: SSH 端口（可选，默认 22）

### Webhook 自动部署

部署了 `webhook_receiver.py` 后，可以通过 GitHub Webhook 触发自动部署：

```bash
# 启动 Webhook 接收器
python3 deploy/webhook_receiver.py
```

## 版本历史

- **v4.2.2** - 修复EcoDataInitializer编译错误，生态环境监测模块正式上线
- **v4.2.0** - 新增生态环境监测模块（气候变暖、沙漠化、水土流失、生态标准库）
- **v2.0.0** - Spring Boot 3.2.5 + Spring AI 1.0.0-M2
- **v1.0.0** - 初始版本

## 许可证

MIT License