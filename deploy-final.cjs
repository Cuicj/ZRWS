const { Client } = require('ssh2');
const fs = require('fs');
const path = require('path');

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

function uploadFile(sftp, localPath, remotePath) {
  return new Promise((resolve, reject) => {
    const readStream = fs.createReadStream(localPath);
    const writeStream = sftp.createWriteStream(remotePath);
    writeStream.on('close', () => resolve());
    writeStream.on('error', reject);
    readStream.pipe(writeStream);
  });
}

function mkdir(sftp, dir) {
  return new Promise((resolve, reject) => {
    sftp.mkdir(dir, (err) => {
      if (err && err.code !== 4) reject(err);
      else resolve();
    });
  });
}

async function uploadDir(sftp, localDir, remoteDir) {
  await mkdir(sftp, remoteDir);
  const items = fs.readdirSync(localDir, { withFileTypes: true });
  for (const item of items) {
    const localPath = path.join(localDir, item.name);
    const remotePath = `${remoteDir}/${item.name}`;
    if (item.isDirectory()) {
      await uploadDir(sftp, localPath, remotePath);
    } else {
      await uploadFile(sftp, localPath, remotePath);
    }
  }
}

function getSftp() {
  return new Promise((resolve, reject) => {
    conn.sftp((err, sftp) => {
      if (err) reject(err);
      else resolve(sftp);
    });
  });
}

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => { log('SSH 连接成功'); resolve(); })
      .on('error', reject).connect(config);
  });

  // 停止后端服务
  log('停止后端服务...');
  await exec('systemctl stop zrws.service 2>/dev/null; sleep 2');

  // 上传后端 JAR
  const localJar = 'E:/AIdeom/智壤卫士/code/java/zrws-approval/target/zrws-approval.jar';
  const jarSize = (fs.statSync(localJar).size / 1024 / 1024).toFixed(1);
  log(`上传后端 JAR (${jarSize}MB)...`);
  
  const sftp1 = await getSftp();
  await new Promise((resolve, reject) => {
    const readStream = fs.createReadStream(localJar);
    const writeStream = sftp1.createWriteStream('/root/workspace/app.jar.new');
    let uploaded = 0;
    let lastPct = -1;
    readStream.on('data', (chunk) => {
      uploaded += chunk.length;
      const pct = Math.floor(uploaded / (jarSize * 1024 * 1024) * 100);
      if (pct !== lastPct && pct % 10 === 0) {
        lastPct = pct;
        log(`JAR上传进度: ${pct}% (${(uploaded/1024/1024).toFixed(1)}MB)`);
      }
    });
    writeStream.on('close', () => resolve());
    writeStream.on('error', reject);
    readStream.pipe(writeStream);
  });
  log('JAR 上传完成');

  // 部署 JAR
  log('部署 JAR...');
  let r = await exec('mv -f /root/workspace/app.jar.new /root/workspace/app.jar && ls -lh /root/workspace/app.jar');
  console.log(r.stdout);

  // 部署前端：逐个文件上传（避免channel过多）
  const distDir = 'E:/AIdeom/智壤卫士/code/html/dist';
  log('部署前端 (递归上传)...');
  await exec('rm -rf /var/www/zrws/*');
  const sftp2 = await getSftp();
  await uploadDir(sftp2, distDir, '/var/www/zrws');
  log('前端上传完成');

  r = await exec('ls -la /var/www/zrws/ | head -15');
  console.log('前端目录:', r.stdout);

  r = await exec('ls /var/www/zrws/assets/ 2>/dev/null | wc -l');
  console.log('assets 文件数:', r.stdout.trim());

  // 启动后端
  log('启动后端服务...');
  r = await exec('systemctl start zrws.service && echo "已启动，等待 90 秒..."');
  console.log(r.stdout);

  await new Promise(r => setTimeout(r, 90000));

  // 验证
  log('--- 验证部署 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -12');
  console.log('服务状态:', r.stdout);

  r = await exec('curl -s --connect-timeout 5 http://localhost:5571/approval/actuator/health 2>&1');
  console.log('健康检查:', r.stdout || '(无响应)');

  r = await exec('curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"admin123"}\'');
  console.log('登录API状态码:', r.stdout);

  r = await exec('journalctl -u zrws.service -n 20 --no-pager | grep -iE "error|exception|fail|started" | tail -15');
  console.log('日志摘要:', r.stdout);

  log('=== v4.3.0 部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
