const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔧 手动更新代码并重新编译...\n');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
cd /root/workspace/ZRWS
echo "=== 1. fetch最新代码 ==="
git fetch --tags origin main
echo ""

echo "=== 2. 切换到v4.2.2 ==="
git checkout v4.2.2
echo ""

echo "=== 3. 确认EcoDataInitializer已更新 ==="
grep -c "系统自定义" code/java/zrws-approval/src/main/java/com/zrws/approval/config/EcoDataInitializer.java
echo "个匹配项（应该有24个）"
echo ""

echo "=== 4. 停止服务 ==="
systemctl stop zrws.service
echo ""

echo "=== 5. 编译后端 ==="
cd /root/workspace/ZRWS/code/java
mvn clean install -DskipTests -pl zrws-approval -am 2>&1 | tail -30
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
