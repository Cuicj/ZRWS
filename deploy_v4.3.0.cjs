const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🚀 部署 v4.3.0 - 历史数据回填+灾害风险API+趋势优化...\n');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
cd /root/workspace/ZRWS
echo "=== 1. 拉取最新代码 ==="
git pull origin main
echo ""

echo "=== 2. 确认关键文件已更新 ==="
grep -c "backfillEcoHistoricalData" code/java/zrws-approval/src/main/java/com/zrws/approval/scheduler/DailyDataScheduler.java
echo "(应大于0)"
ls -la code/java/zrws-approval/src/main/java/com/zrws/approval/controller/DisasterRiskController.java
echo ""

echo "=== 3. 停止服务 ==="
systemctl stop zrws.service
echo ""

echo "=== 4. 编译后端（跳过测试编译） ==="
cd /root/workspace/ZRWS/code/java
mvn clean install -Dmaven.test.skip=true -pl zrws-approval -am 2>&1 | tail -10
echo ""

echo "=== 5. 复制JAR并启动服务 ==="
cp zrws-approval/target/zrws-approval.jar /root/workspace/app.jar
systemctl start zrws.service
echo "服务已启动，等待90秒让数据回填完成..."
sleep 90

echo ""
echo "=== 6. 服务状态 ==="
systemctl status zrws.service | head -5

echo ""
echo "=== 7. 健康检查 ==="
curl -s http://localhost:5571/approval/actuator/health
echo ""

echo ""
echo "=== 8. 检查气候变暖数据量 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total FROM zrws_climate_warming WHERE is_deleted=0; SELECT COUNT(DISTINCT DATE_FORMAT(monitor_date,'%Y-%m')) as months FROM zrws_climate_warming WHERE is_deleted=0;" 2>&1 | grep -v Warning

echo ""
echo "=== 9. 检查沙漠化数据量 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total FROM zrws_desertification WHERE is_deleted=0; SELECT COUNT(DISTINCT DATE_FORMAT(monitor_date,'%Y-%m')) as months FROM zrws_desertification WHERE is_deleted=0;" 2>&1 | grep -v Warning

echo ""
echo "=== 10. 检查灾害风险(水土流失)数据量 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total FROM zrws_disaster_risk WHERE is_deleted=0; SELECT COUNT(*) as soil_erosion FROM zrws_disaster_risk WHERE is_deleted=0 AND disaster_type='SOIL_EROSION';" 2>&1 | grep -v Warning

echo ""
echo "=== 11. 测试气候变暖趋势API ==="
curl -s "http://localhost:5571/approval/api/v1/climate-warming/trend" | python3 -c "import sys,json; d=json.load(sys.stdin); print('月份:', d.get('data',{}).get('months',[])); print('温度距平:', d.get('data',{}).get('tempAnomalies',[]))" 2>/dev/null || curl -s "http://localhost:5571/approval/api/v1/climate-warming/trend" | head -c 300

echo ""
echo ""
echo "=== 12. 测试灾害风险列表API ==="
curl -s "http://localhost:5571/approval/api/v1/disaster-risk/list?disasterType=SOIL_EROSION&pageSize=3" | head -c 300
echo ""

echo ""
echo "=== 13. 测试灾害风险统计API ==="
curl -s "http://localhost:5571/approval/api/v1/disaster-risk/stats" | head -c 300
echo ""
`;

  conn.exec(cmd, (err, stream) => {
    if (err) {
      console.error('❌ 执行失败:', err.message);
      conn.end();
      return;
    }

    stream.on('data', (data) => {
      process.stdout.write(data.toString());
    });
    stream.stderr.on('data', (data) => {
      process.stderr.write(data.toString());
    });
    stream.on('close', (code) => {
      console.log(`\n--- 执行完成，退出码: ${code} ---`);
      conn.end();
    });
  });
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
