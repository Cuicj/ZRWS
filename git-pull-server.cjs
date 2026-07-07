const { Client } = require('ssh2');

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
};

const remoteProjectDir = '/root/workspace/ZRWS';

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

  log('查看本地修改...');
  await sshExec(conn, `cd ${remoteProjectDir} && git status --short | head -20`);

  log('stash 本地修改...');
  await sshExec(conn, `cd ${remoteProjectDir} && git stash`);

  log('拉取最新代码...');
  await sshExec(conn, `cd ${remoteProjectDir} && git pull origin main`);

  log('=== 代码更新完成 ===');
  conn.end();
}

main().catch(err => {
  console.error('失败:', err.message);
  process.exit(1);
});
