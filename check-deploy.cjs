const { Client } = require('ssh2');
const fs = require('fs');

const conn = new Client();
const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

function log(msg) {
  const ts = new Date().toLocaleTimeString('zh-CN');
  console.log(`[${ts}] ${msg}`);
}

function exec(cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let stdout = '';
      let stderr = '';
      stream.on('close', (code) => {
        resolve({ code, stdout, stderr });
      }).on('data', (d) => { stdout += d.toString(); })
        .stderr.on('data', (d) => { stderr += d.toString(); });
    });
  });
}

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => { log('SSH 连接成功'); resolve(); })
      .on('error', reject).connect(config);
  });

  // 等待后端启动
  log('等待后端启动完成 (60秒)...');
  await new Promise(r => setTimeout(r, 60000));

  // 检查后端状态
  log('--- 后端状态检查 ---');
  let r = await exec('systemctl status zrws.service --no-pager | head -15');
  console.log(r.stdout);

  r = await exec('curl -s --connect-timeout 5 http://localhost:5571/approval/actuator/health 2>&1');
  console.log('健康检查:', r.stdout || '(无响应)');

  r = await exec('journalctl -u zrws.service -n 30 --no-pager | grep -iE "error|exception|fail|started|running" | tail -20');
  console.log('日志:', r.stdout);

  // 修复前端：用 python3 解压 zip（正确处理路径分隔符）
  log('--- 修复前端部署 ---');
  r = await exec('cd /var/www/zrws && rm -rf * && python3 -c "import zipfile; zipfile.ZipFile(\'/tmp/dist.zip\').extractall(\'.\')" && ls -la /var/www/zrws/ | head -15');
  console.log('前端解压结果:', r.stdout);
  if (r.stderr) console.log('python stderr:', r.stderr.substring(0, 500));

  r = await exec('ls /var/www/zrws/assets/ 2>/dev/null | wc -l');
  console.log('assets文件数:', r.stdout.trim());

  // 验证前端主页
  r = await exec('head -5 /var/www/zrws/index.html 2>/dev/null');
  console.log('index.html:', r.stdout.substring(0, 200));

  log('=== 检查完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
