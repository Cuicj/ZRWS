# ZRWS 项目结构优化方案

> 当前项目结构对自动化部署不够友好，本文档提供一套更易部署的目录结构方案。

---

## 一、当前项目结构分析

### 现有结构（问题）

```
/root/workspace/ZRWS/          # Git 仓库根目录
├── 01-需求分析/               # ❌ 不应在部署仓库中
├── 02-详细设计/               # ❌ 不应在部署仓库中
├── 03-系统开发/               # ❌ 不应在部署仓库中
├── 04-测试部署/               # ❌ 不应在部署仓库中
├── 05-用户手册/               # ❌ 不应在部署仓库中
├── code/                      # ⚠️ 嵌套层级过深
│   ├── java/                 # Maven 项目根目录（应在 repo 根目录）
│   │   ├── pom.xml          # 父 POM
│   │   ├── zrws-common/
│   │   ├── zrws-approval/
│   │   └── lib/            # ⚠️ 本地 JAR 应上传到 Maven 仓库
│   ├── html/                # 前端（应独立或用 frontend/ 命名）
│   ├── mobile/              # 移动端（uniapp）
│   ├── python/              # Python 脚本
│   ├── sql/                 # SQL 脚本
│   └── uniapp/             # 重复的移动端？
├── index.html               # ❓ 作用不明确
├── 说明.html                 # ❓ 作用不明确
├── README.md
├── .gitignore
├── _shared/                 # ❓ 作用不明确
└── .trae/                  # 开发工具配置
```

### 主要问题

| 问题 | 影响 |
|------|------|
| 中文目录名 | Linux 部署时可能乱码，CI/CD 友好度差 |
| 代码嵌套过深（code/java/） | Maven 构建路径复杂，deploy.sh 容易写错路径 |
| 本地 JAR（lib/） | 无法在 CI/CD 中复现，需手动上传 |
| 部署脚本在 /root/workspace/ | 不在 Git 版本控制中，难以维护 |
| 文档混入代码仓库 | 仓库体积大，部署时无需拉取文档 |

---

## 二、推荐的项目结构

### 方案一：标准 Maven + 前后端分离（推荐）

```
zrws/                              # 仓库根目录（英文名）
├── backend/                        # 后端（原名 code/java）
│   ├── pom.xml                    # 父 POM（原 zrws-parent）
│   ├── zrws-common/              # 公共模块
│   ├── zrws-approval/            # 审批主模块
│   └── README.md                 # 后端说明（含部署命令）
│
├── frontend/                       # 前端（原名 code/html）
│   ├── src/
│   ├── package.json
│   ├── vite.config.js
│   ├── dist/                     # 构建产物（.gitignore）
│   └── README.md
│
├── mobile/                         # 移动端（uniapp）
│   └── ...
│
├── deploy/                         # 🆕 部署相关（版本控制）
│   ├── deploy.sh                 # 部署脚本（原 /root/workspace/deploy.sh）
│   ├── webhook.py                # Webhook 接收器
│   ├── zrws.service              # systemd 服务文件
│   ├── nginx.conf                # Nginx 配置模板
│   └── README.md                # 部署文档
│
├── sql/                           # 数据库脚本
│   ├── init/
│   ├── update/
│   └── README.md
│
├── docs/                          # 🆕 文档（原 01-05 目录）
│   ├── requirements/             # 需求分析
│   ├── design/                  # 详细设计
│   ├── manual/                  # 用户手册
│   └── deployment.md            # 部署文档（指向 deploy/）
│
├── .github/                       # 🆕 CI/CD（GitHub Actions）
│   └── workflows/
│       └── deploy.yml           # 自动部署配置
│
├── .gitignore
├── pom.xml                        # 🆕 根 POM（可选，用于统一构建）
└── README.md                     # 项目总说明
```

---

## 三、调整后的优势

### 3.1 部署流程简化

**调整前（当前）：**
```bash
# deploy.sh 需要进入深层目录
cd /root/workspace/ZRWS/code/java
mvn clean package -DskipTests
cp zrws-approval/target/*.jar /root/workspace/app.jar
```

**调整后（推荐）：**
```bash
# deploy.sh 路径更清晰
cd /app/backend
mvn clean package -DskipTests
cp zrws-approval/target/*.jar /app/app.jar
```

### 3.2 POM 文件简化

**调整后，父 POM 在仓库根目录，`pom.xml` 可以直接引用：**

```bash
# 调整前：需要指定 -f code/java/pom.xml
mvn -f code/java/pom.xml clean package

# 调整后：直接在 backend/ 目录执行
cd backend && mvn clean package
```

### 3.3 部署脚本纳入版本控制

**调整前：** `deploy.sh` 在 `/root/workspace/` 目录，不在 Git 中

**调整后：** `deploy.sh` 在 `deploy/` 目录，受 Git 管理，支持 rollback

---

## 四、具体调整步骤

### 步骤 1：创建新目录结构

```bash
# 在服务器上操作
cd /root/workspace/

# 创建新目录
mkdir -p ZRWS-new/backend
mkdir -p ZRWS-new/frontend
mkdir -p ZRWS-new/deploy
mkdir -p ZRWS-new/sql
mkdir -p ZRWS-new/docs

# 移动后端代码
cp -r ZRWS/code/java ZRWS-new/backend/

# 移动前端代码
cp -r ZRWS/code/html ZRWS-new/frontend/

# 移动部署脚本
cp deploy.sh ZRWS-new/deploy/
cp webhook_receiver.py ZRWS-new/deploy/

# 移动 SQL
cp -r ZRWS/code/sql ZRWS-new/

# 移动文档（可选）
cp -r ZRWS/01-*/ ZRWS-new/docs/ 2>/dev/null || true
```

### 步骤 2：修改 POM 文件路径

**如果保持原有 `code/java/` 结构不变**，只需修改 `deploy.sh` 中的路径：

```bash
# deploy.sh 关键修改
REPO_DIR="/root/workspace/ZRWS"
BACKEND_DIR="$REPO_DIR/code/java"    # 保持不变

# 或者，如果调整为新结构：
BACKEND_DIR="/app/backend"            # 新的后端目录
```

### 步骤 3：推荐的最小化调整（不改变目录结构）

> 如果你不想大改目录结构，只做最小调整：

```
ZRWS/                          # 保持不变
├── code/
│   └── java/                  # 保持不变
│       ├── pom.xml
│       ├── zrws-common/
│       └── zrws-approval/
├── deploy.sh                   # 🆕 移到仓库内（版本控制）
├── webhook.py                  # 🆕 移到仓库内
└── README.md                  # 更新部署说明
```

**最小调整清单：**

1. **把 `deploy.sh` 和 `webhook_receiver.py` 纳入 Git 管理**
2. **删除 `lib/` 目录，改用 Maven 依赖**
3. **修复 POM 文件**（已完成）
4. **添加 `.github/workflows/deploy.yml`**（可选，实现自动部署）

---

## 五、推荐的 deploy.sh（调整后）

```bash
#!/bin/bash
# deploy.sh - 部署脚本（放在仓库根目录）

set -e

# 配置
APP_NAME="zrws"
APP_DIR="/app"                          # 应用部署目录
REPO_DIR="$APP_DIR/ZRWS"              # Git 仓库目录
BACKEND_DIR="$REPO_DIR/backend"        # 后端目录（调整后）
# BACKEND_DIR="$REPO_DIR/code/java"    # 后端目录（调整前）

JAR_FILE="$BACKEND_DIR/zrws-approval/target/zrws-approval-*.jar"
DEPLOY_JAR="$APP_DIR/app.jar"

LOG_FILE="$APP_DIR/logs/deploy.log"
WEBHOOK_LOG="$APP_DIR/logs/webhook.log"

mkdir -p "$APP_DIR/logs"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 1. 拉取最新代码
log "拉取最新代码..."
cd "$REPO_DIR"
git fetch --all
git pull origin main --tags

# 2. 编译后端
log "开始 Maven 编译..."
cd "$BACKEND_DIR"
mvn clean package -DskipTests -q

# 3. 停止服务
log "停止服务..."
systemctl stop "$APP_NAME" || true

# 4. 备份旧版本
if [ -f "$DEPLOY_JAR" ]; then
    cp "$DEPLOY_JAR" "$APP_DIR/backups/app-$(date '+%Y%m%d_%H%M%S').jar"
fi

# 5. 部署新版本
log "部署新版本..."
cp $JAR_FILE "$DEPLOY_JAR"

# 6. 启动服务
log "启动服务..."
systemctl start "$APP_NAME"

# 7. 健康检查
sleep 5
if systemctl is-active --quiet "$APP_NAME"; then
    log "部署成功！"
    # 清理旧备份（保留最近 5 个）
    ls -t "$APP_DIR/backups/"*.jar | tail -n +6 | xargs rm -f 2>/dev/null || true
else
    log "部署失败，服务未启动"
    exit 1
fi
```

---

## 六、实施建议

### 方案 A：保守调整（推荐，风险低）

1. 只把 `deploy.sh` 和 `webhook_receiver.py` 移到 Git 仓库内
2. 删除 `lib/` 目录，把 Spring AI JAR 安装到 Maven 本地仓库
3. 修复 POM 文件（已完成）
4. 更新 `README.md` 部署说明

**预计时间：** 1 小时

### 方案 B：全面调整（推荐，长期收益高）

1. 按"推荐结构"重新组织目录
2. 修改 `pom.xml` 中的模块路径
3. 更新 `deploy.sh` 中的路径
4. 配置 GitHub Actions 自动部署

**预计时间：** 半天

---

## 七、文件清单（调整后）

### 需要调整的文件

| 文件 | 调整内容 |
|------|----------|
| `deploy.sh` | 移到仓库内，修正路径 |
| `webhook_receiver.py` | 移到仓库内 |
| `code/java/pom.xml` | 删除 `system` scope（已完成） |
| `code/java/pom.xml` | 添加依赖版本（已完成） |
| `.gitignore` | 添加 `target/`, `dist/`, `*.jar` |
| `README.md` | 更新部署说明 |

### 可以删除的文件/目录

| 文件/目录 | 原因 |
|-----------|------|
| `code/java/lib/` | 应改用 Maven 依赖 |
| `01-需求分析/` 等 | 移到 `docs/` 或不纳入部署仓库 |
| `index.html` | 作用不明确，可能是旧版本 |
| `说明.html` | 作用不明确 |

---

## 八、总结

**当前最紧急的调整：**

1. ✅ **修复 POM 文件**（已完成，待验证编译）
2. 🔄 **把部署脚本纳入 Git**（避免服务器上修改丢失）
3. 🔄 **清理 `lib/` 目录**（安装到 Maven 本地仓库）
4. 🔄 **简化部署路径**（修改 `deploy.sh`）

**建议操作顺序：**

```
1. 先完成 Maven 编译（解决当前阻碍）
2. 把 deploy.sh 提交到 Git
3. 调整目录结构（保守方案）
4. 配置 GitHub Actions（实现完全自动化）
```

---

*生成时间：2026-06-23*
*适用版本：ZRWS v2.0+*
