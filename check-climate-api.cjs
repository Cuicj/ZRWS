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

  let r = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\' 2>&1');
  const tokenMatch = r.stdout.match(/"token":"([^"]+)"/);
  const token = tokenMatch ? tokenMatch[1] : '';
  const authHeader = `-H "Authorization: Bearer ${token}"`;

  console.log('\n========== 气候变暖 stats ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/climate-warming/stats" 2>&1`);
  console.log(r.stdout);

  console.log('\n========== 气候变暖 trend ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/climate-warming/trend" 2>&1`);
  console.log(r.stdout.substring(0, 500));

  console.log('\n========== 气候变暖 risk-distribution ==========\n');
  r = await exec(`curl -s ${authHeader} "http://localhost:5571/approval/api/v1/climate-warming/risk-distribution" 2>&1`);
  console.log(r.stdout);

  console.log('\n========================================\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
