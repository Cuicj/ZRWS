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

  // 登录
  let r = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\' 2>&1');
  const tokenMatch = r.stdout.match(/"token":"([^"]+)"/);
  const token = tokenMatch ? tokenMatch[1] : '';
  const authHeader = `-H "Authorization: Bearer ${token}"`;

  console.log('\n========== 气候变暖 API 返回 ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/climate-warming/region-stats?region=全国" 2>&1`);
  console.log('region-stats:', r.stdout.substring(0, 500));

  console.log('\n========== 气候变暖 list ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/climate-warming/list?page=1&size=3" 2>&1`);
  console.log('list:', r.stdout.substring(0, 500));

  console.log('\n========== 土壤采样 API 返回 ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/soil-sample/list?page=1&size=3" 2>&1`);
  console.log(r.stdout.substring(0, 500));

  console.log('\n========== 地块 API 返回 ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/land-plot/area-stats" 2>&1`);
  console.log('area-stats:', r.stdout.substring(0, 500));

  console.log('\n========================================\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
