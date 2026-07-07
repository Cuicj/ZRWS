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
    'zrws_flight_mission',
    'zrws_soil_sample',
    'zrws_gps_track_point',
    'zrws_land_plot',
    'zrws_soil_classification',
    'zrws_rock_stratum_analysis',
    'zrws_disaster_risk',
    'zrws_device',
    'zrws_quality_check',
    'zrws_announcement',
    'zrws_approval_task',
    'zrws_climate_warming',
    'zrws_desertification',
    'zrws_geo_standard',
    'zrws_eco_standard',
    'zrws_rock_sample',
    'zrws_organization',
    'zrws_sys_role',
    'zrws_sys_user',
    'zrws_user_org',
  ];

  console.log('\n========== 数据库表数据量统计 ==========\n');

  for (const table of tables) {
    let r = await exec(`mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p'Test_admin' zrws_approval -e "SELECT COUNT(*) as cnt FROM ${table} WHERE is_deleted=0;" 2>&1 | tail -1`);
    const cnt = r.stdout.trim();
    const isEmpty = cnt === '0' || cnt === 'cnt' || !cnt;
    console.log(`${isEmpty ? '❌' : '✅'} ${table.padEnd(35)} ${cnt} 条`);
  }

  console.log('\n========================================\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
