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

  console.log('--- admin 用户密码哈希 ---');
  let r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SELECT id, username, password, status, is_deleted FROM zrws_sys_user WHERE username=\'admin\';" 2>&1');
  console.log(r.stdout);

  console.log('--- 最近的登录错误日志 ---');
  r = await exec('journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep -iE "login|auth|password|IllegalArgument" | tail -15');
  console.log(r.stdout || '(无)');

  console.log('--- Redis 连接状态 ---');
  r = await exec('journalctl -u zrws.service --since "30 minutes ago" --no-pager | grep -iE "redis|Unable to connect" | tail -10');
  console.log(r.stdout || '(无 redis 错误)');

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
