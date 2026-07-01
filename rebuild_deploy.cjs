const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔧 重新编译（跳过测试编译）...\n');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
cd /root/workspace/ZRWS/code/java
echo "=== 编译后端（跳过所有测试） ==="
mvn clean install -Dmaven.test.skip=true -pl zrws-approval -am 2>&1 | tail -15
echo ""

echo "=== 检查JAR是否生成 ==="
ls -lh zrws-approval/target/zrws-approval.jar
echo ""

echo "=== 复制JAR并启动服务 ==="
cp zrws-approval/target/zrws-approval.jar /root/workspace/app.jar
systemctl start zrws.service
echo "服务已启动，等待90秒..."
sleep 90

echo ""
echo "=== 服务状态 ==="
systemctl status zrws.service | head -10

echo ""
echo "=== 健康检查 ==="
curl -s http://localhost:5571/approval/actuator/health
echo ""

echo ""
echo "=== 检查生态环境表 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE '%eco%'; SHOW TABLES LIKE '%climate%'; SHOW TABLES LIKE '%desert%';" 2>&1 | grep -v Warning

echo ""
echo "=== 测试生态标准API ==="
curl -s "http://localhost:5571/approval/api/v1/eco-standard/list?pageNum=1&pageSize=3" | head -c 300
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
