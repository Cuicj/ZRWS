const { Client } = require('ssh2');

const conn = new Client();
const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

function log(msg) {
  const ts = new Date().toLocaleTimeString('zh-CN');
  console.log(`[${ts}] ${msg}`);
}

function exec(cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let stdout = '';
      let stderr = '';
      stream.on('close', (code) => {
        resolve({ code, stdout, stderr });
      }).on('data', (d) => { stdout += d.toString(); })
        .stderr.on('data', (d) => { stderr += d.toString(); });
    });
  });
}

async function run() {
  await new Promise((resolve, reject) => {
    conn.on('ready', resolve).on('error', reject).connect(config);
  });

  log('--- 监听端口 ---');
  let r = await exec('ss -tlnp | grep java');
  console.log(r.stdout || '(无 java 端口)');

  log('--- 最近 30 行日志 ---');
  r = await exec('journalctl -u zrws.service -n 30 --no-pager');
  console.log(r.stdout);

  log('--- ERROR ---');
  r = await exec('journalctl -u zrws.service --no-pager | grep -i ERROR | tail -10');
  console.log(r.stdout || '(无 ERROR)');

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
