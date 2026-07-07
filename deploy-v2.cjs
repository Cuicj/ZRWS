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

function uploadFile(localPath, remotePath) {
  return new Promise((resolve, reject) => {
    conn.sftp((err, sftp) => {
      if (err) return reject(err);
      const stats = fs.statSync(localPath);
      const totalSize = stats.size;
      let uploaded = 0;
      const readStream = fs.createReadStream(localPath);
      const writeStream = sftp.createWriteStream(remotePath);
      let lastPct = -1;
      readStream.on('data', (chunk) => {
        uploaded += chunk.length;
        const pct = Math.floor(uploaded / totalSize * 100);
        if (pct !== lastPct && pct % 10 === 0) {
          lastPct = pct;
          log(`上传 ${remotePath}: ${pct}% (${(uploaded/1024/1024).toFixed(1)}MB)`);
        }
      });
      writeStream.on('close', () => resolve());
      writeStream.on('error', reject);
      readStream.pipe(writeStream);
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
  await exec('systemctl stop zrws.service 2>/dev/null; sleep 1');

  // 上传后端 JAR
  const localJar = 'E:/AIdeom/智壤卫士/code/java/zrws-approval/target/zrws-approval.jar';
  const jarSize = (fs.statSync(localJar).size / 1024 / 1024).toFixed(1);
  log(`上传后端 JAR (${jarSize}MB)...`);
  await uploadFile(localJar, '/root/workspace/app.jar.new');
  log('JAR 上传完成');

  // 部署 JAR
  log('部署 JAR...');
  let r = await exec('mv /root/workspace/app.jar /root/workspace/app.jar.backup.v4.2.x && mv /root/workspace/app.jar.new /root/workspace/app.jar && ls -lh /root/workspace/app.jar');
  console.log(r.stdout);

  // 前端：用 jar 命令解压（因为 unzip 不存在）
  log('部署前端 (使用 jar xf 解压)...');
  r = await exec('rm -rf /var/www/zrws/* && cd /var/www/zrws && jar xf /tmp/dist.zip && ls -la /var/www/zrws/ | head -10');
  console.log(r.stdout);
  if (r.stderr) console.log('jar stderr:', r.stderr.substring(0, 300));

  // 验证前端
  r = await exec('ls /var/www/zrws/assets/ | wc -l');
  console.log('前端资源数:', r.stdout.trim());

  // 启动后端
  log('启动后端服务...');
  r = await exec('systemctl start zrws.service && echo "已启动，等待 90 秒..."');
  console.log(r.stdout);

  await new Promise(r => setTimeout(r, 90000));

  // 验证
  log('--- 验证部署 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -10');
  console.log('服务状态:', r.stdout);

  r = await exec('curl -s http://localhost:5571/approval/actuator/health');
  console.log('健康检查:', r.stdout);

  r = await exec('curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"');
  console.log('登录API状态码:', r.stdout);

  r = await exec('journalctl -u zrws.service -n 30 --no-pager | grep -iE "error|exception|fail|started" | tail -20');
  console.log('日志摘要:', r.stdout);

  log('=== v4.3.0 部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
