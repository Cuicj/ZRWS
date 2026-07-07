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

  const tables = [
    'zrws_soil_sample',
    'zrws_land_plot',
    'zrws_gps_track_point',
    'zrws_climate_warming',
    'zrws_soil_classification',
    'zrws_flight_mission',
  ];

  console.log('\n========== 各表 tenant_id 分布 ==========\n');

  for (const table of tables) {
    let r = await exec(`mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p'Test_admin' zrws_approval -e "SELECT DISTINCT tenant_id FROM ${table} LIMIT 5;" 2>&1`);
    console.log(`${table}:`);
    console.log(r.stdout.replace(/\n/g, '\n   ').trim());
    console.log('');
  }

  // 查看用户的 tenant_id
  r = await exec(`mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p'Test_admin' zrws_approval -e "SELECT id, username, tenant_id FROM zrws_sys_user;" 2>&1`);
  console.log('zrws_sys_user:');
  console.log(r.stdout.trim());

  console.log('\n========================================\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
