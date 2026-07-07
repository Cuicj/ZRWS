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

  // 停止服务
  log('停止后端服务...');
  await exec('systemctl stop zrws.service 2>/dev/null; sleep 2');

  // 上传 JAR
  const localJar = 'E:/AIdeom/智壤卫士/code/java/zrws-approval/target/zrws-approval.jar';
  const jarSize = (fs.statSync(localJar).size / 1024 / 1024).toFixed(1);
  log(`上传 JAR (${jarSize}MB)...`);

  await new Promise((resolve, reject) => {
    conn.sftp((err, sftp) => {
      if (err) return reject(err);
      const readStream = fs.createReadStream(localJar);
      const writeStream = sftp.createWriteStream('/root/workspace/app.jar.new');
      let uploaded = 0;
      let lastPct = -1;
      const total = jarSize * 1024 * 1024;
      readStream.on('data', (chunk) => {
        uploaded += chunk.length;
        const pct = Math.floor(uploaded / total * 100);
        if (pct !== lastPct && pct % 10 === 0) {
          lastPct = pct;
          log(`JAR 上传: ${pct}% (${(uploaded/1024/1024).toFixed(1)}MB)`);
        }
      });
      writeStream.on('close', () => resolve());
      writeStream.on('error', reject);
      readStream.pipe(writeStream);
    });
  });
  log('JAR 上传完成');

  // 替换 + 启动
  log('替换 JAR 并启动服务...');
  let r = await exec('mv -f /root/workspace/app.jar.new /root/workspace/app.jar && systemctl start zrws.service && echo "已启动"');
  console.log(r.stdout);

  log('等待 90 秒启动...');
  await new Promise(r => setTimeout(r, 90000));

  // 验证
  log('--- 验证 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -10');
  console.log('服务状态:', r.stdout);

  r = await exec('curl -s --connect-timeout 5 http://localhost:5571/approval/actuator/health 2>&1');
  console.log('健康检查:', r.stdout || '(无响应)');

  r = await exec('curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\'');
  console.log('登录 API 状态码:', r.stdout);

  r = await exec('journalctl -u zrws.service -n 25 --no-pager | grep -iE "started|error|exception|fail" | tail -10');
  console.log('日志摘要:', r.stdout);

  log('=== 部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
