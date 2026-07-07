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

  console.log('--- 1. 登录测试 (admin/admin123) ---');
  let r = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\' 2>&1');
  console.log(r.stdout.substring(0, 800));

  const tokenMatch = r.stdout.match(/"token":"([^"]+)"/);
  if (!tokenMatch) {
    console.log('\n登录失败，无法继续测试');
    conn.end();
    return;
  }

  const token = tokenMatch[1];
  console.log('\n获取到 token，长度:', token.length);

  console.log('\n--- 2. 菜单树接口测试 ---');
  r = await exec(`curl -s -w "\\nHTTP_CODE:%{http_code}" -H "Authorization: Bearer ${token}" http://localhost:5571/approval/api/v1/menu/tree 2>&1`);
  console.log(r.stdout.substring(0, 1000));

  console.log('\n--- 3. 用户信息接口测试 ---');
  r = await exec(`curl -s -w "\\nHTTP_CODE:%{http_code}" -H "Authorization: Bearer ${token}" http://localhost:5571/approval/api/v1/auth/me 2>&1`);
  console.log(r.stdout.substring(0, 600));

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
