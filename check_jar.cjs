const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查JAR包中是否包含新增实体类...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查JAR中是否有生态环境相关的类 ==="
jar tf /root/workspace/app.jar | grep -i "EcoStandard\|ClimateWarming\|Desertification\|eco" | head -20

echo ""
echo "=== 2. 检查实体类总数 ==="
jar tf /root/workspace/app.jar | grep "domain/entity" | grep "\.class" | wc -l

echo ""
echo "=== 3. 列出所有entity类 ==="
jar tf /root/workspace/app.jar | grep "domain/entity.*\.class$" | head -40
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
