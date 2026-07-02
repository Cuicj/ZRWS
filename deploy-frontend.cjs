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

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => { log('SSH 连接成功'); resolve(); })
      .on('error', reject).connect(config);
  });

  const distDir = 'E:/AIdeom/智壤卫士/code/html/dist';
  log('清空旧前端文件...');
  await exec('rm -rf /var/www/zrws/*');

  log('上传前端文件 (递归)...');
  const sftp = await new Promise((resolve, reject) => {
    conn.sftp((err, sftp) => {
      if (err) reject(err);
      else resolve(sftp);
    });
  });
  await uploadDir(sftp, distDir, '/var/www/zrws');
  log('前端上传完成');

  let r = await exec('ls -la /var/www/zrws/ | head -10');
  console.log(r.stdout);

  r = await exec('ls /var/www/zrws/assets/ | wc -l');
  console.log('assets 文件数:', r.stdout.trim());

  r = await exec('grep -l "4\\.3\\.0" /var/www/zrws/assets/*.js | head -3');
  console.log('包含 4.3.0 的文件:', r.stdout || '(无)');

  log('=== 前端重新部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
