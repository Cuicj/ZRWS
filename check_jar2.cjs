const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查新启动的进程...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 查看最近的启动日志（Schema同步部分） ==="
journalctl -u zrws.service --since "2026-07-01 02:02:00" --no-pager | grep -E "SchemaSync|\[DBA\]|扫描实体|期望表|现有表|创建表|同步完成|eco_standard|climate_warming|desertification" | head -40

echo ""
echo "=== 2. 查看JAR中是否有新增的类 ==="
jar tf /root/workspace/app.jar | grep -iE "EcoStandard|ClimateWarming|Desertification" | head -10

echo ""
echo "=== 3. 检查Controller是否在JAR中 ==="
jar tf /root/workspace/app.jar | grep -iE "EcoStandardController|ClimateWarmingController|DesertificationController" | head -10

echo ""
echo "=== 4. 检查服务进程 ==="
ps aux | grep java | grep -v grep
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
      console.log(`\n--- 完成 ---`);
      conn.end();
    });
  });
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
