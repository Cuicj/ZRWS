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

  console.log('--- 修复 admin 用户 is_deleted ---');
  let r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "UPDATE zrws_sys_user SET is_deleted=0 WHERE username=\'admin\' AND is_deleted IS NULL;" 2>&1');
  console.log(r.stdout);
  if (r.stderr) console.log('stderr:', r.stderr);

  console.log('--- 修复后查询 ---');
  r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SELECT id, username, is_deleted, tenant_id, status FROM zrws_sys_user WHERE username=\'admin\';" 2>&1');
  console.log(r.stdout);

  console.log('--- 检查 sys_user 表中 is_deleted 为 NULL 的记录 ---');
  r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SELECT COUNT(*) as null_count FROM zrws_sys_user WHERE is_deleted IS NULL;" 2>&1');
  console.log(r.stdout);

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
