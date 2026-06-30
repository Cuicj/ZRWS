const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🔍 检查服务器上的代码文件...');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const cmd = `
echo "=== 1. 检查服务器上是否有新增的Java文件 ==="
find /root/workspace/ZRWS/code/java/zrws-approval/src/main/java/com/zrws/approval -name "*Eco*" -o -name "*Climate*" -o -name "*Desert*" 2>/dev/null | head -20

echo ""
echo "=== 2. 检查当前git状态 ==="
cd /root/workspace/ZRWS
git log --oneline -5
echo "---"
git tag --sort=-creatordate | head -5

echo ""
echo "=== 3. 检查entity目录 ==="
ls -la /root/workspace/ZRWS/code/java/zrws-approval/src/main/java/com/zrws/approval/domain/entity/ | grep -iE "eco|climate|desert"
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
