const { Client } = require('ssh2');

const conn = new Client();
const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

function exec(cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let stdout = '';
      let stderr = '';
      stream.on('close', (code) => resolve({ code, stdout, stderr }));
      stream.on('data', (d) => { stdout += d.toString(); });
      stream.stderr.on('data', (d) => { stderr += d.toString(); });
    });
  });
}

async function run() {
  await new Promise((resolve, reject) => {
    conn.on('ready', resolve).on('error', reject).connect(config);
  });

  console.log('--- 端口 ---');
  let r = await exec('ss -tlnp | grep java');
  console.log(r.stdout || '(无 java 端口)');

  console.log('--- 服务状态 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -6');
  console.log(r.stdout);

  console.log('--- 最新 Started ---');
  r = await exec('journalctl -u zrws.service --no-pager --since "2 minutes ago" | grep -i "Started ZrwsApproval"');
  console.log(r.stdout || '(未启动成功)');

  console.log('--- 最新 ERROR ---');
  r = await exec('journalctl -u zrws.service --no-pager --since "3 minutes ago" | grep -i ERROR');
  console.log(r.stdout || '(无 ERROR)');

  console.log('--- 最后 3 行 ---');
  r = await exec('journalctl -u zrws.service -n 3 --no-pager');
  console.log(r.stdout);

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
