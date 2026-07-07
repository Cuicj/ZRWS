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
      stream.on('data', (data) => { output += data.toString(); process.stdout.write(data.toString()); });
      stream.stderr.on('data', (data) => { output += data.toString(); process.stderr.write(data.toString()); });
      stream.on('close', (code) => { resolve({ code, output }); });
    });
  });
}

async function main() {
  const conn = new Client();
  await new Promise((resolve, reject) => {
    conn.on('ready', resolve).on('error', reject).connect(config);
  });
  log('SSH 连接成功');

  log('查找 JAR 和服务...');
  await sshExec(conn, 'find / -name "zrws-approval.jar" -type f 2>/dev/null');
  await sshExec(conn, 'systemctl list-units --type=service | grep -i zrws || echo "no zrws service"');
  await sshExec(conn, 'ps aux | grep java | grep -v grep');
  await sshExec(conn, 'cat /etc/systemd/system/zrws*.service 2>/dev/null || echo "no service file"');

  conn.end();
}

main().catch(err => {
  console.error('失败:', err.message);
  process.exit(1);
});
