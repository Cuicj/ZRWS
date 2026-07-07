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

  console.log('--- Nginx zrws 配置 ---');
  let r = await exec('grep -A 20 "zrws\\|/approval" /etc/nginx/sites-enabled/* /etc/nginx/conf.d/* 2>/dev/null | head -60');
  console.log(r.stdout || '(未找到)');

  console.log('--- Nginx 主配置中 approval ---');
  r = await exec('grep -rn "approval" /etc/nginx/ 2>/dev/null | head -20');
  console.log(r.stdout || '(无)');

  console.log('--- 数据库 sys_user 表 ---');
  r = await exec('mysql -uzrws -p\'Zrws@2024\' zrws -e "SELECT id, username, real_name, status FROM sys_user LIMIT 5;" 2>&1');
  console.log(r.stdout);

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
