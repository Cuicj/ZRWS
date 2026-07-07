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

  console.log('再等 60 秒...');
  await new Promise(r => setTimeout(r, 60000));

  console.log('--- 端口 ---');
  let r = await exec('ss -tlnp | grep java');
  console.log(r.stdout || '(无)');

  console.log('--- 最后 20 行日志 ---');
  r = await exec('journalctl -u zrws.service -n 20 --no-pager');
  console.log(r.stdout);

  console.log('--- ERROR ---');
  r = await exec('journalctl -u zrws.service --no-pager | grep -i ERROR | tail -5');
  console.log(r.stdout || '(无)');

  console.log('--- Started ---');
  r = await exec('journalctl -u zrws.service --no-pager | grep -i "Started Zrws"');
  console.log(r.stdout || '(无)');

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
