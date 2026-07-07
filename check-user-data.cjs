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

  console.log('--- zrws_sys_user 表数据 ---');
  let r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SELECT id, username, real_name, status, current_org_id, tenant_id FROM zrws_sys_user LIMIT 10;" 2>&1');
  console.log(r.stdout);

  console.log('--- zrws_sys_user 记录数 ---');
  r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SELECT COUNT(*) as total FROM zrws_sys_user WHERE is_deleted=0;" 2>&1');
  console.log(r.stdout);

  console.log('--- 测试登录 API (admin/admin123) ---');
  r = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\' 2>&1');
  console.log(r.stdout.substring(0, 500));

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
