const { Client } = require('ssh2');

const conn = new Client();
const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

let step = 0;
const steps = [];

function log(msg) {
  const ts = new Date().toLocaleTimeString('zh-CN');
  console.log(`[${ts}] ${msg}`);
}

function exec(cmd, opts = {}) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, { pty: opts.pty || false }, (err, stream) => {
      if (err) return reject(err);
      let stdout = '';
      let stderr = '';
      stream.on('close', (code) => {
        resolve({ code, stdout, stderr });
      }).on('data', (data) => {
        stdout += data.toString();
        if (opts.echo) process.stdout.write(data.toString());
      }).stderr.on('data', (data) => {
        stderr += data.toString();
        if (opts.echo) process.stderr.write(data.toString());
      });
    });
  });
}

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => {
      log('SSH 连接成功');
      resolve();
    }).on('error', reject).connect(config);
  });

  // 步骤1: 检查当前版本
  log('--- 步骤1: 检查当前状态 ---');
  let r = await exec('uname -a && uptime && systemctl status zrws.service --no-pager | head -5');
  console.log(r.stdout);

  // 步骤2: 拉取代码并切换版本
  log('--- 步骤2: 拉取 v4.3.0 代码 ---');
  r = await exec('cd /root/workspace/ZRWS && git fetch --tags -f 2>&1 && git checkout v4.3.0 2>&1 && git log --oneline -3');
  console.log(r.stdout);
  if (r.stderr) console.log('stderr:', r.stderr);

  // 步骤3: 停止后端服务
  log('--- 步骤3: 停止后端服务 ---');
  r = await exec('systemctl stop zrws.service && sleep 2 && systemctl status zrws.service --no-pager | head -3');
  console.log(r.stdout);

  // 步骤4: 备份当前 JAR
  log('--- 步骤4: 备份当前 JAR ---');
  r = await exec('cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S) && ls -lh /root/workspace/app.jar*');
  console.log(r.stdout);

  // 步骤5: Maven 编译后端 (使用 -Dmaven.test.skip=true)
  log('--- 步骤5: 编译后端 (Spring Boot 4.0 + Spring AI 2.0) ---');
  log('编译中，请耐心等待（约 3-5 分钟）...');
  r = await exec('cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true -q 2>&1 | tail -20', { echo: false });
  console.log(r.stdout);
  if (r.stderr) console.log('stderr:', r.stderr.substring(r.stderr.length - 1000));
  
  // 检查编译结果
  r = await exec('ls -lh /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar 2>&1');
  console.log('编译产物:', r.stdout);

  // 步骤6: 部署后端 JAR
  log('--- 步骤6: 部署后端 JAR ---');
  r = await exec('cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar && ls -lh /root/workspace/app.jar');
  console.log(r.stdout);

  // 步骤7: 修复前端 postcss 配置
  log('--- 步骤7: 修复前端 postcss 配置 ---');
  r = await exec(`cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'POSTCSSEOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
POSTCSSEOF
cat /root/workspace/ZRWS/code/html/postcss.config.js`);
  console.log(r.stdout);

  // 步骤8: 安装依赖并构建前端
  log('--- 步骤8: 构建前端 ---');
  log('前端构建中...');
  r = await exec('cd /root/workspace/ZRWS/code/html && npm install 2>&1 | tail -5 && npm run build 2>&1 | tail -20', { echo: false });
  console.log(r.stdout);
  if (r.stderr) {
    const errLines = r.stderr.split('\n').filter(l => l.includes('ERROR') || l.includes('error') || l.includes('Failed')).slice(-10);
    if (errLines.length) console.log('错误:', errLines.join('\n'));
  }

  // 检查构建产物
  r = await exec('ls -la /root/workspace/ZRWS/code/html/dist/ 2>&1 | head -10');
  console.log('前端产物:', r.stdout);

  // 步骤9: 部署前端
  log('--- 步骤9: 部署前端 ---');
  r = await exec('rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/ && ls -la /var/www/zrws/ | head -10');
  console.log(r.stdout);

  // 步骤10: 启动后端服务
  log('--- 步骤10: 启动后端服务 ---');
  r = await exec('systemctl start zrws.service && echo "服务已启动，等待 90 秒..."');
  console.log(r.stdout);

  // 等待启动
  log('等待 Spring Boot 启动 (90秒)...');
  await new Promise(r => setTimeout(r, 90000));

  // 步骤11: 验证部署
  log('--- 步骤11: 验证部署 ---');
  r = await exec('systemctl status zrws.service --no-pager | head -10');
  console.log('服务状态:', r.stdout);

  r = await exec('curl -s http://localhost:5571/approval/actuator/health 2>&1');
  console.log('健康检查:', r.stdout);

  r = await exec('journalctl -u zrws.service -n 30 --no-pager 2>&1 | grep -iE "error|exception|started|fail" | tail -20');
  console.log('启动日志摘要:', r.stdout);

  log('=== 部署完成 ===');
  conn.end();
}

run().catch(err => {
  console.error('部署失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
