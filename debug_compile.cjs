const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查Maven编译输出...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查zrws-approval的target目录 ==="
ls -la /root/workspace/ZRWS/code/java/zrws-approval/target/*.jar 2>&1 | head -5

echo ""
echo "=== 2. 检查target/classes下的entity ==="
ls /root/workspace/ZRWS/code/java/zrws-approval/target/classes/com/zrws/approval/domain/entity/ 2>&1 | grep -iE "eco|climate|desert"

echo ""
echo "=== 3. 重新编译zrws-approval（带输出） ==="
cd /root/workspace/ZRWS/code/java
mvn clean compile -pl zrws-approval -am 2>&1 | tail -30
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
      console.log(`\n--- 完成，退出码: ${code} ---`);
      conn.end();
    });
  });
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
