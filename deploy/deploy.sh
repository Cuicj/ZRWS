#!/bin/bash
# deploy.sh - 部署脚本
# 放在仓库根目录，受Git版本控制

set -e

# 配置
APP_NAME="zrws"
APP_DIR="/app"                          # 应用部署目录
REPO_DIR="$APP_DIR/ZRWS"                # Git 仓库目录
BACKEND_DIR="$REPO_DIR/backend"         # 后端目录

JAR_FILE="$BACKEND_DIR/zrws-approval/target/zrws-approval.jar"
DEPLOY_JAR="$APP_DIR/app.jar"

LOG_FILE="$APP_DIR/logs/deploy.log"
BACKUP_DIR="$APP_DIR/backups"

mkdir -p "$APP_DIR/logs"
mkdir -p "$BACKUP_DIR"

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
    cp "$DEPLOY_JAR" "$BACKUP_DIR/app-$(date '+%Y%m%d_%H%M%S').jar"
fi

# 5. 部署新版本
log "部署新版本..."
cp "$JAR_FILE" "$DEPLOY_JAR"

# 6. 启动服务
log "启动服务..."
systemctl start "$APP_NAME"

# 7. 健康检查
sleep 5
if systemctl is-active --quiet "$APP_NAME"; then
    log "部署成功！"
    # 清理旧备份（保留最近 5 个）
    ls -t "$BACKUP_DIR/"*.jar | tail -n +6 | xargs rm -f 2>/dev/null || true
else
    log "部署失败，服务未启动"
    exit 1
fi