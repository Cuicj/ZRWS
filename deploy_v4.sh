#!/bin/bash
export SSH_ASKPASS="e:/AIdeom/智壤卫士/askpass.bat"
export SSH_ASKPASS_REQUIRE="force"
export DISPLAY=:0

SERVER="8.163.137.149"
USER="root"

echo "========================================"
echo "开始部署 v4.0.0 版本"
echo "========================================"

# 步骤1: 拉取最新代码
echo ""
echo "[1/8] 拉取最新代码..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "cd /root/workspace/ZRWS; git pull origin main"

# 步骤2: Maven编译后端
echo ""
echo "[2/8] Maven编译后端 (预计3-5分钟)..."
START_TIME=$(date +%s)
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "cd /root/workspace/ZRWS/code/java; mvn clean package -DskipTests"
END_TIME=$(date +%s)
DURATION=$(( (END_TIME - START_TIME) / 60 ))
echo "Maven编译耗时: $DURATION 分钟"

# 步骤3: 部署JAR
echo ""
echo "[3/8] 部署JAR..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar"

# 步骤4: 构建前端
echo ""
echo "[4/8] 构建前端..."
START_TIME=$(date +%s)
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "cd /root/workspace/ZRWS/code/html; npm run build"
END_TIME=$(date +%s)
DURATION=$(( (END_TIME - START_TIME) / 60 ))
echo "前端构建耗时: $DURATION 分钟"

# 步骤5: 部署前端
echo ""
echo "[5/8] 部署前端..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "rm -rf /var/www/zrws/*; cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/"

# 步骤6: 部署公告栏
echo ""
echo "[6/8] 部署公告栏..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "mkdir -p /app/announcement; cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html"

# 步骤7: 重启服务
echo ""
echo "[7/8] 重启服务 (等待30秒)..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "systemctl restart zrws.service; sleep 30"

# 步骤8: 验证
echo ""
echo "[8/8] 验证部署..."
echo "检查后端健康状态..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "curl -s http://localhost:5571/approval/actuator/health"

echo "检查前端访问..."
ssh -o StrictHostKeyChecking=no ${USER}@${SERVER} "curl -I -s http://localhost/TZ"

echo ""
echo "========================================"
echo "部署完成"
echo "========================================"
