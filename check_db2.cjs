const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查数据库配置和Schema同步...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查数据库中的表总数 ==="
mysql -u root -p'Zrwstest@2024' -h rm-bp12967624r282h17.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES;" 2>/dev/null | wc -l

echo ""
echo "=== 2. 检查启动日志前500行中的DBA/Schema信息 ==="
journalctl -u zrws.service --since "20 minutes ago" --no-pager | grep -i -E "dba|schema|table|entity|sync" | head -30

echo ""
echo "=== 3. 检查启动日志前100行 ==="
journalctl -u zrws.service --since "20 minutes ago" --no-pager | head -100

echo ""
echo "=== 4. 检查application.yml中的dba配置 ==="
cd /root/workspace/ZRWS/code/java/zrws-approval/src/main/resources
grep -A 10 "dba" application.yml 2>/dev/null || echo "未找到dba配置"
grep -A 5 "zrws" application.yml 2>/dev/null || echo "未找到zrws配置"
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
