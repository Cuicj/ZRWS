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

  console.log('--- Nginx 配置中的 zrws 站点 ---');
  let r = await exec('grep -r "zrws.cloud\\|server_name.*zrws\\|root.*zrws" /etc/nginx/ 2>/dev/null | head -20');
  console.log(r.stdout || '(未找到)');

  console.log('--- /var/www/zrws/index.html 中的版本 ---');
  r = await exec('grep -o "v[0-9.]*" /var/www/zrws/index.html | head -5');
  console.log(r.stdout || '(未找到版本号)');

  console.log('--- /var/www/zrws/ 下的 index.html 修改时间 ---');
  r = await exec('ls -la /var/www/zrws/index.html');
  console.log(r.stdout);

  console.log('--- 前端 assets 中是否有 4.3.0 版本 ---');
  r = await exec('grep -r "4\\.3\\.0" /var/www/zrws/assets/ 2>/dev/null | head -3');
  console.log(r.stdout || '(未找到 4.3.0)');

  console.log('--- 前端 assets 中是否有 4.0.0 版本 ---');
  r = await exec('grep -r "4\\.0\\.0" /var/www/zrws/assets/ 2>/dev/null | head -3');
  console.log(r.stdout || '(未找到 4.0.0)');

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
