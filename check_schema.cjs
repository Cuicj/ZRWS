const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 查看Schema同步的完整日志...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. Schema同步期间的完整日志（约01:36:00-01:37:00） ==="
journalctl -u zrws.service --since "2026-07-01 01:35:30" --until "2026-07-01 01:38:00" --no-pager | grep -E "SchemaSync|EntityClassScanner|\[DBA\]|期望表|现有表|创建|新增|同步|created|added|eco_standard|climate_warming|desertification" | head -100

echo ""
echo "=== 2. 查找SchemaSyncService相关日志 ==="
journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep "SchemaSyncService" | head -50

echo ""
echo "=== 3. 检查启动时扫描的实体类中是否包含新增的 ==="
journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep -i "扫描实体" | grep -iE "eco|climate|desert"
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
