#!/bin/bash
# 智壤卫士 v3.4.0 一键部署脚本
# 用法: bash deploy_v3.4.0.sh

set -e

VERSION="v3.4.0"
REPO_DIR="/root/workspace/ZRWS"
JAR_PATH="/root/workspace/app.jar"
FRONTEND_DIR="/var/www/zrws"
BACKEND_PORT=5571

echo "========================================"
echo "  智壤卫士 $VERSION 部署脚本"
echo "========================================"
echo ""

# ========== 1. 拉取代码并切换 Tag ==========
echo "[1/10] 拉取代码并切换到 $VERSION ..."
cd $REPO_DIR
git fetch --tags
git checkout $VERSION
echo "✅ 已切换到 $VERSION"
git log --oneline -3
echo ""

# ========== 2. 备份当前 JAR ==========
echo "[2/10] 备份当前 JAR ..."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
cp $JAR_PATH $JAR_PATH.backup.$TIMESTAMP
echo "✅ 备份完成: app.jar.backup.$TIMESTAMP"
ls -lh $JAR_PATH.backup.* | tail -3
echo ""

# ========== 3. 停止后端服务 ==========
echo "[3/10] 停止后端服务 ..."
systemctl stop zrws.service
sleep 3
echo "✅ 服务已停止"
systemctl status zrws.service --no-pager | head -5
echo ""

# ========== 4. Maven 编译后端 ==========
echo "[4/10] Maven 编译后端 (预计 3-5 分钟) ..."
cd $REPO_DIR/code/java
mvn clean package -DskipTests
echo "✅ 编译完成"
ls -lh $REPO_DIR/code/java/zrws-approval/target/*.jar
echo ""

# ========== 5. 部署后端 JAR ==========
echo "[5/10] 部署后端 JAR ..."
cp $REPO_DIR/code/java/zrws-approval/target/zrws-approval-*.jar $JAR_PATH
echo "✅ JAR 部署完成"
ls -lh $JAR_PATH
echo ""

# ========== 6. 修复 postcss.config.js ==========
echo "[6/10] 检查前端构建环境 ..."
POSTCSS_FILE="$REPO_DIR/code/html/postcss.config.js"
if grep -q "export default" $POSTCSS_FILE 2>/dev/null; then
    echo "⚠️  检测到 ESM 格式，转换为 CJS..."
    cat > $POSTCSS_FILE << 'EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
EOF
fi
echo "✅ postcss.config.js 格式正确"
cat $POSTCSS_FILE
echo ""

# ========== 7. 构建前端 ==========
echo "[7/10] 构建前端 (预计 1-2 分钟) ..."
cd $REPO_DIR/code/html
npm run build
echo "✅ 前端构建完成"
ls -lh dist/
echo ""

# ========== 8. 部署前端 ==========
echo "[8/10] 部署前端到 Nginx 目录 ..."
rm -rf $FRONTEND_DIR/*
cp -r dist/* $FRONTEND_DIR/
echo "✅ 前端部署完成"
ls -la $FRONTEND_DIR/
echo ""

# ========== 9. 启动后端服务 ==========
echo "[9/10] 启动后端服务 ..."
systemctl start zrws.service
echo "⏳ 等待 30 秒让服务启动..."
sleep 30
echo ""

# ========== 10. 验证部署 ==========
echo "[10/10] 验证部署 ..."

# 后端健康检查
echo "--- 后端健康检查 ---"
HEALTH=$(curl -s http://localhost:$BACKEND_PORT/approval/actuator/health)
echo "$HEALTH"
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ 后端健康检查通过"
else
    echo "❌ 后端健康检查失败"
fi

# 前端检查
echo ""
echo "--- 前端检查 ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/)
echo "HTTP 状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ 前端 HTTP 200 OK"
else
    echo "⚠️  前端 HTTP 状态异常"
fi

echo ""
echo "--- 服务状态 ---"
systemctl status zrws.service --no-pager | head -10

echo ""
echo "========================================"
echo "  🎉 部署完成! $VERSION"
echo "========================================"
echo ""
echo "📌 访问地址:"
echo "   前端: https://www.zrws.cloud"
echo "   后端: https://www.zrws.cloud/approval/"
echo "   Swagger: https://www.zrws.cloud/approval/swagger-ui/index.html"
echo ""
echo "📌 版本: $VERSION"
echo "📌 部署时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""
echo "💡 提示: 浏览器强刷新 (Ctrl+F5) 查看最新版本"
echo ""
