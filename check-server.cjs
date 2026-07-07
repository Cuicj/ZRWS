const { Client } = require('ssh2');

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
};

function log(msg) {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false });
  console.log(`[${time}] ${msg}`);
}

function sshExec(conn, command) {
  return new Promise((resolve, reject) => {
    conn.exec(command, (err, stream) => {
      if (err) return reject(err);
      let output = '';
      stream.on('data', (data) => {
        output += data.toString();
        process.stdout.write(data.toString());
      });
      stream.stderr.on('data', (data) => {
        output += data.toString();
        process.stderr.write(data.toString());
      });
      stream.on('close', (code) => {
        resolve({ code, output });
      });
    });
  });
}

async function main() {
  const conn = new Client();
  
  await new Promise((resolve, reject) => {
    conn.on('ready', resolve).on('error', reject).connect(config);
  });
  log('SSH 连接成功');

  log('检查环境...');
  await sshExec(conn, 'which java && java -version 2>&1 | head -1');
  await sshExec(conn, 'which mvn && mvn -version 2>&1 | head -1 || echo "mvn not found"');
  await sshExec(conn, 'which git && git --version || echo "git not found"');
  await sshExec(conn, 'ls /root/zrws-java 2>/dev/null || echo "no zrws-java dir"');
  await sshExec(conn, 'ls /opt/zrws/ 2>/dev/null || echo "no /opt/zrws dir"');

  conn.end();
}

main().catch(err => {
  console.error('失败:', err.message);
  process.exit(1);
});
