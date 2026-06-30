echo "=== 步骤1: 停止后端服务 ==="
systemctl stop zrws.service
echo "服务已停止"

echo "=== 步骤2: 拉取代码并切换版本 ==="
cd /root/workspace/ZRWS
git fetch --tags -f
git checkout v4.2.0
echo "已切换到 v4.2.0"

echo "=== 步骤3: 备份当前JAR ==="
cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)
echo "JAR备份完成"
ls -la /root/workspace/app.jar.backup.* | tail -3

echo "=== 步骤4: Maven编译后端 ==="
cd /root/workspace/ZRWS/code/java
mvn clean install -Dmaven.test.skip=true -q
echo "后端编译完成"

echo "=== 步骤5: 部署后端JAR ==="
cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar
ls -la /root/workspace/app.jar
echo "JAR部署完成"

echo "=== 步骤6: 修复前端构建配置 ==="
cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'POSTCSS_EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
POSTCSS_EOF
echo "postcss配置已更新"

cd /root/workspace/ZRWS/code/html
npm ls autoprefixer 2>/dev/null || npm install autoprefixer
echo "autoprefixer已就绪"

echo "=== 步骤7: 构建前端 ==="
npm run build
echo "前端构建完成"

echo "=== 步骤8: 部署前端 ==="
rm -rf /var/www/zrws/*
cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/
echo "前端部署完成"
ls -la /var/www/zrws/

echo "=== 步骤9: 启动后端服务 ==="
systemctl start zrws.service
echo "后端服务已启动"

echo "=== 等待30秒后验证 ==="
sleep 30

echo "=== 步骤10: 验证部署 ==="
systemctl status zrws.service --no-pager
curl -s http://localhost:5571/approval/actuator/health
echo ""
echo "=== 部署完成 ==="
