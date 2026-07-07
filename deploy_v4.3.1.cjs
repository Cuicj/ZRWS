const { Client } = require('ssh2');
const conn = new Client();
const config = { host: '8.163.137.149', port: 22, username: 'root', password: 'Test_admin' };

console.log('🚀 重新部署...\n');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');
  const cmd = `
cd /root/workspace/ZRWS && git pull origin main
echo "=== 确认文件大小 ==="
wc -l code/java/zrws-approval/src/main/java/com/zrws/approval/controller/DisasterRiskController.java
echo ""
systemctl stop zrws.service
echo "=== 编译 ==="
cd /root/workspace/ZRWS/code/java
mvn clean install -Dmaven.test.skip=true -pl zrws-approval -am 2>&1 | tail -5
echo ""
echo "=== 部署启动 ==="
cp zrws-approval/target/zrws-approval.jar /root/workspace/app.jar
systemctl start zrws.service
echo "等待120秒让历史数据回填..."
sleep 120
echo ""
echo "=== 健康检查 ==="
curl -s http://localhost:5571/approval/actuator/health
echo ""
echo ""
echo "=== 气候变暖数据 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total, COUNT(DISTINCT DATE_FORMAT(monitor_date,'%Y-%m')) as months FROM zrws_climate_warming WHERE is_deleted=0;" 2>&1 | grep -v Warning
echo ""
echo "=== 沙漠化数据 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total, COUNT(DISTINCT DATE_FORMAT(monitor_date,'%Y-%m')) as months FROM zrws_desertification WHERE is_deleted=0;" 2>&1 | grep -v Warning
echo ""
echo "=== 灾害风险数据 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SELECT COUNT(*) as total, SUM(CASE WHEN disaster_type='SOIL_EROSION' THEN 1 ELSE 0 END) as soil_erosion FROM zrws_disaster_risk WHERE is_deleted=0;" 2>&1 | grep -v Warning
echo ""
echo "=== 气候变暖趋势API ==="
curl -s "http://localhost:5571/approval/api/v1/climate-warming/trend" | head -c 300
echo ""
echo ""
echo "=== 灾害风险列表API ==="
curl -s "http://localhost:5571/approval/api/v1/disaster-risk/list?disasterType=SOIL_EROSION&pageSize=3" | head -c 300
echo ""
echo ""
echo "=== 灾害风险统计API ==="
curl -s "http://localhost:5571/approval/api/v1/disaster-risk/stats" | head -c 300
echo ""
`;
  conn.exec(cmd, (err, stream) => {
    if (err) { console.error('❌', err.message); conn.end(); return; }
    stream.on('data', d => process.stdout.write(d.toString()));
    stream.stderr.on('data', d => process.stderr.write(d.toString()));
    stream.on('close', code => { console.log(`\n--- 退出码: ${code} ---`); conn.end(); });
  });
});
conn.on('error', err => { console.error('❌', err.message); process.exit(1); });
conn.connect(config);
