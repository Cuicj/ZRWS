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
    'zrws_rock_stratum_analysis',
    'zrws_disaster_risk',
    'zrws_device',
    'zrws_quality_check',
    'zrws_announcement',
    'zrws_approval_task',
    'zrws_desertification',
    'zrws_geo_standard',
    'zrws_eco_standard',
    'zrws_rock_sample',
    'zrws_organization',
    'zrws_sys_role',
    'zrws_user_org',
  ];

  console.log('\n========== 修复各表 tenant_id ==========\n');

  for (const table of tables) {
    let r = await exec(`mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p'Test_admin' zrws_approval -e "UPDATE ${table} SET tenant_id = 1 WHERE tenant_id IS NULL;" 2>&1 | tail -1`);
    console.log(`${table}: ${r.stdout.trim()}`);
  }

  // 修复 is_deleted 为 NULL 的记录
  console.log('\n========== 修复 is_deleted ==========\n');
  for (const table of tables) {
    let r = await exec(`mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p'Test_admin' zrws_approval -e "UPDATE ${table} SET is_deleted = 0 WHERE is_deleted IS NULL;" 2>&1 | tail -1`);
    console.log(`${table}: ${r.stdout.trim()}`);
  }

  console.log('\n========================================\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
