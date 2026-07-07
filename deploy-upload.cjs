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
          log(`上传中 ${path.basename(localPath)}: ${pct}% (${(uploaded/1024/1024).toFixed(1)}MB / ${(totalSize/1024/1024).toFixed(1)}MB)`);
        }
      });
      
      writeStream.on('close', () => resolve());
      writeStream.on('error', reject);
      readStream.pipe(writeStream);
    });
  });
}

function uploadDir(localDir, remoteDir) {
  return new Promise((resolve, reject) => {
    conn.sftp(async (err, sftp) => {
      if (err) return reject(err);
      try {
        await exec(`mkdir -p ${remoteDir}`);
        const items = fs.readdirSync(localDir, { withFileTypes: true });
        for (const item of items) {
          const localPath = path.join(localDir, item.name);
          const remotePath = `${remoteDir}/${item.name}`;
          if (item.isDirectory()) {
            await uploadDir(localPath, remotePath);
          } else {
            await new Promise((res, rej) => {
              const readStream = fs.createReadStream(localPath);
              const writeStream = sftp.createWriteStream(remotePath);
              writeStream.on('close', () => res());
              writeStream.on('error', rej);
              readStream.pipe(writeStream);
            });
          }
        }
        resolve();
      } catch (e) {
        reject(e);
      }
    });
  });
}

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => { log('SSH 连接成功'); resolve(); })
      .on('error', reject).connect(config);
  });

  // 上传后端 JAR
  const localJar = 'E:/AIdeom/智壤卫士/code/java/zrws-approval/target/zrws-approval.jar';
  log(`上传后端 JAR (${(fs.statSync(localJar).size/1024/1024).toFixed(1)}MB)...`);
  await uploadFile(localJar, '/root/workspace/app.jar.new');
  log('JAR 上传完成');

  // 部署新 JAR
  log('部署新 JAR...');
  let r = await exec('mv /root/workspace/app.jar /root/workspace/app.jar.backup.v4.3.0-pre && mv /root/workspace/app.jar.new /root/workspace/app.jar && ls -lh /root/workspace/app.jar');
  console.log(r.stdout);

  // 上传前端 dist
  const localDist = 'E:/AIdeom/智壤卫士/code/html/dist';
  log('上传前端 dist...');
  await exec('rm -rf /var/www/zrws/*');
  await uploadDir(localDist, '/var/www/zrws');
  r = await exec('ls -la /var/www/zrws/');
  console.log('前端部署:', r.stdout);

  // 重启后端
  log('重启后端服务...');
  r = await exec('systemctl restart zrws.service && echo "已重启，等待 90 秒..."');
  console.log(r.stdout);

  await new Promise(r => setTimeout(r, 90000));

  // 验证
  log('--- 验证部署 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -10');
  console.log('服务状态:', r.stdout);

  r = await exec('curl -s http://localhost:5571/approval/actuator/health');
  console.log('健康检查:', r.stdout);

  r = await exec('journalctl -u zrws.service -n 20 --no-pager | tail -10');
  console.log('最后日志:', r.stdout);

  log('=== v4.3.0 部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
