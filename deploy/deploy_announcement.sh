#!/bin/bash
# deploy_announcement.sh - 部署公告栏到 /TZ 路径
# 用法: ./deploy_announcement.sh [服务器IP]
#
# 本地执行会将公告栏推送到服务器 /app/announcement 目录

set -e

# 配置
APP_NAME="zrws-announcement"
REMOTE_DIR="/app/announcement"
LOCAL_HTML="公告栏.html"
REMOTE_HOST="${1:-}"
SSH_USER="${SSH_USER:-root}"
SSH_KEY="${SSH_KEY:-~/.ssh/id_rsa}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查本地文件
if [ ! -f "$LOCAL_HTML" ]; then
    log_error "找不到 $LOCAL_HTML 文件"
    exit 1
fi

log_info "开始部署公告栏..."

# 复制到部署目录
if [ -n "$REMOTE_HOST" ]; then
    log_info "使用 SCP 推送到服务器: $REMOTE_HOST"
    ssh -i "$SSH_KEY" "$SSH_USER@$REMOTE_HOST" "mkdir -p $REMOTE_DIR"
    scp -i "$SSH_KEY" "$LOCAL_HTML" "$SSH_USER@$REMOTE_HOST:$REMOTE_DIR/公告栏.html"
    log_info "部署完成！"
    log_info "访问地址: https://www.zrws.cloud/TZ"
else
    # 本地部署
    log_info "执行本地部署..."
    mkdir -p "$REMOTE_DIR"
    cp "$LOCAL_HTML" "$REMOTE_DIR/公告栏.html"
    log_info "文件已复制到: $REMOTE_DIR/公告栏.html"
    log_info "请确保 nginx 配置已更新并重新加载"
fi

# 重新加载 nginx（需要远程执行）
if [ -n "$REMOTE_HOST" ]; then
    log_info "重新加载 Nginx 配置..."
    ssh -i "$SSH_KEY" "$SSH_USER@$REMOTE_HOST" "nginx -t && nginx -s reload"
fi

log_info "部署成功！"
log_info "========================================"
log_info "公告栏访问地址: https://www.zrws.cloud/TZ"
log_info "========================================"
