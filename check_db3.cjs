const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查Schema同步完整日志...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. Schema同步完整日志 ==="
journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep -E "\[DBA\]|createdTable|addedColumn|期望表|现有表|创建表|新增字段|同步完成|扫描实体" | head -80

echo ""
echo "=== 2. 检查错误 ==="
journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep -i -E "error|exception|fail" | grep -v Flowable | grep -v liquibase | head -30

echo ""
echo "=== 3. 检查当前数据库中的生态表 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE '%eco%'; SHOW TABLES LIKE '%climate%'; SHOW TABLES LIKE '%desert%'; SHOW TABLES LIKE '%soil_erosion%';" 2>&1 | grep -v "Warning"
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
      console.log(`\n--- 执行完成 ---`);
      conn.end();
    });
  });
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
