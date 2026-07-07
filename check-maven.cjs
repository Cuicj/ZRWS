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
      stream.on('data', (data) => { output += data.toString(); });
      stream.stderr.on('data', (data) => { output += data.toString(); });
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

  log('检查 Maven settings...');
  await sshExec(conn, 'cat /root/.m2/settings.xml 2>/dev/null | head -50 || echo "no settings.xml"');
  await sshExec(conn, 'cat /etc/maven/settings.xml 2>/dev/null | head -80 || echo "no global settings"');

  log('检查本地仓库是否有 spring-ai...');
  await sshExec(conn, 'find /root/.m2/repository/org/springframework/ai -name "*.pom" 2>/dev/null | head -10 || echo "no spring-ai"');

  conn.end();
}

main().catch(err => {
  console.error('失败:', err.message);
  process.exit(1);
});
