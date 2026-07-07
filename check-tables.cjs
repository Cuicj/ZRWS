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

  console.log('--- 数据库中所有表 ---');
  let r = await exec('mysql -hrm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com -utest_admin -p\'Test_admin\' zrws_approval -e "SHOW TABLES;" 2>&1');
  console.log(r.stdout);

  console.log('--- 最近启动时的 DBA / 建表日志 ---');
  r = await exec('journalctl -u zrws.service --since "1 hour ago" --no-pager | grep -iE "dba|table|create|schema|sync" | head -30');
  console.log(r.stdout || '(无)');

  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});
