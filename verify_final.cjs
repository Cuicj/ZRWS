const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 最终验证 - 生态环境表和API...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查生态环境相关表 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE '%eco%'; SHOW TABLES LIKE '%climate%'; SHOW TABLES LIKE '%desert%';" 2>&1 | grep -v Warning

echo ""
echo "=== 2. Schema同步结果摘要 ==="
journalctl -u zrws.service --since "5 minutes ago" --no-pager | grep -E "\[DBA\]|扫描实体|期望表|现有表|创建表|新增字段|同步完成" | grep -v DEBUG | head -20

echo ""
echo "=== 3. 健康检查 ==="
curl -s http://localhost:5571/approval/actuator/health
echo ""

echo ""
echo "=== 4. 测试生态标准API ==="
curl -s http://localhost:5571/approval/api/v1/eco-standard/list | head -c 300
echo ""

echo ""
echo "=== 5. 测试气候变暖API ==="
curl -s http://localhost:5571/approval/api/v1/climate-warming/stats | head -c 300
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
      console.log(`\n--- 验证完成 ---`);
      conn.end();
    });
  });
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
