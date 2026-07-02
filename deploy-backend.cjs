const { Client } = require('ssh2');

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
};

const remoteProjectDir = '/root/workspace/ZRWS';
const remoteJarPath = '/root/workspace/app.jar';
const serviceName = 'zrws';

function log(msg) {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false });
  console.log(`[${time}] ${msg}`);
}

function sshExec(conn, command) {
  return new Promise((resolve, reject) => {
    conn.exec(command, (err, stream) => {
      if (err) return reject(err);
      let output = '';
      stream.on('data', (data) => { output += data.toString(); process.stdout.write(data.toString()); });
      stream.stderr.on('data', (data) => { output += data.toString(); process.stderr.write(data.toString()); });
      stream.on('close', (code) => { resolve({ code, output }); });
    });
  });
}

async function main() {
  const conn = new Client();
  await new Promise((resolve, reject) => {
    conn.on('ready', resolve).on('error', reject).connect(config);
  });
  log('SSH 连接成功');

  log('拉取最新代码...');
  await sshExec(conn, `cd ${remoteProjectDir} && git pull origin main`);

  log('编译后端 (约3-5分钟)...');
  const { code: buildCode } = await sshExec(
    conn,
    `cd ${remoteProjectDir}/code/java && mvn clean package -DskipTests -pl zrws-approval -am 2>&1 | tail -60`
  );
  
  if (buildCode !== 0) {
    log('编译失败！');
    conn.end();
    process.exit(1);
  }
  log('编译成功');

  log('停止服务...');
  await sshExec(conn, `systemctl stop ${serviceName}`);

  log('备份旧 JAR...');
  await sshExec(conn, `cp ${remoteJarPath} ${remoteJarPath}.bak 2>/dev/null || echo "no backup needed"`);

  log('复制新 JAR...');
  await sshExec(conn, `cp ${remoteProjectDir}/code/java/zrws-approval/target/zrws-approval.jar ${remoteJarPath}`);

  log('启动服务...');
  await sshExec(conn, `systemctl start ${serviceName}`);

  log('等待服务启动 (25s)...');
  await new Promise(r => setTimeout(r, 25000));

  log('检查服务状态...');
  await sshExec(conn, `systemctl status ${serviceName} | head -10`);

  log('健康检查...');
  await sshExec(conn, `curl -s http://127.0.0.1:5571/approval/actuator/health`);
  console.log('');

  log('验证管理接口...');
  await sshExec(conn, `curl -s -X POST http://127.0.0.1:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | head -c 200`);
  console.log('');

  log('=== 后端部署完成 ===');
  conn.end();
}

main().catch(err => {
  console.error('部署失败:', err.message);
  process.exit(1);
});
