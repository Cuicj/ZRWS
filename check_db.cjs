const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查数据库表和启动日志...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查生态环境相关表是否存在 ==="
mysql -u root -p'Zrwstest@2024' -h rm-bp12967624r282h17.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE '%eco%'; SHOW TABLES LIKE '%climate%'; SHOW TABLES LIKE '%desert%';" 2>/dev/null

echo ""
echo "=== 2. 检查最近启动日志中的 DBA/Schema 相关信息 ==="
journalctl -u zrws.service -n 200 --no-pager | grep -i -E "dba|schema|sync|eco_standard|climate_warm|desertif|创建表|table|error" | tail -50

echo ""
echo "=== 3. 检查服务状态 ==="
systemctl status zrws.service --no-pager | head -10
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
