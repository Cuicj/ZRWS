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

  console.log('\n========== 验证新增的 API 接口 ==========\n');

  // 登录获取 token
  let r = await exec('curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\' 2>&1');
  const tokenMatch = r.stdout.match(/"token":"([^"]+)"/);
  const token = tokenMatch ? tokenMatch[1] : '';
  console.log('登录状态:', token ? '成功' : '失败');

  const authHeader = `-H "Authorization: Bearer ${token}"`;

  // 测试新增的接口
  const apis = [
    { name: '土壤采样列表', path: '/api/v1/soil-sample/list' },
    { name: '土质分类列表', path: '/api/v1/soil-classification/list' },
    { name: '土质分类历史', path: '/api/v1/soil-classification/history' },
    { name: 'GPS航迹列表', path: '/api/v1/gps-track/list' },
    { name: '地块列表', path: '/api/v1/land-plot/list' },
    { name: '地块面积统计', path: '/api/v1/land-plot/area-stats' },
    { name: '审批待办', path: '/api/v1/todo?assignee=admin' },
    { name: '审批已办', path: '/api/v1/my-applied?applicantId=1' },
    { name: '系统配置', path: '/api/v1/sysconfig' },
    { name: '公告列表', path: '/api/v1/announcement/list' },
  ];

  for (const api of apis) {
    r = await exec(`curl -s -w "\\n%{http_code}" ${authHeader} http://localhost:5571/approval${api.path} 2>&1`);
    const lines = r.stdout.split('\n');
    const httpCode = lines.pop();
    const body = lines.join('\n').substring(0, 200);
    const success = httpCode === '200' || httpCode === '201';
    console.log(`${success ? '✅' : '❌'} ${api.name} (${httpCode})`);
    if (!success || body.includes('error')) {
      console.log('   响应:', body.substring(0, 150));
    }
  }

  console.log('\n========== 验证完成 ==========\n');
  conn.end();
}

run().catch(err => {
  console.error(err);
  try { conn.end(); } catch(e) {}
});