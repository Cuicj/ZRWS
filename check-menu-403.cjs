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

  console.log('--- 最近 5 分钟 403 相关日志 ---');
  let r = await exec('journalctl -u zrws.service --since "5 minutes ago" --no-pager | grep -iE "403|forbidden|denied|jwt|token|unauth" | head -20');
  console.log(r.stdout || '(无)');

  console.log('--- 菜单接口调用情况 ---');
  r = await exec('journalctl -u zrws.service --since "5 minutes ago" --no-pager | grep -i "menu" | head -10');
  console.log(r.stdout || '(无 menu 日志)');

  console.log('--- 测试直接调用菜单接口（带 token） ---');
  const loginResp = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\'');
  console.log('登录响应:', loginResp.stdout.substring(0, 200));

  const tokenMatch = loginResp.stdout.match(/"token":"([^"]+)"/);
  if (tokenMatch) {
    const token = tokenMatch[1];
    console.log('获取到 token，长度:', token.length);
    const menuResp = await exec(`curl -s -w "\\n%{http_code}" -H "Authorization: Bearer ${token}" http://localhost:5571/approval/api/v1/menu/tree`);
    console.log('菜单接口响应:', menuResp.stdout);
  }

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
