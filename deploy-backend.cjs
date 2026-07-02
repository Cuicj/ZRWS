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

  log('清理失败缓存...');
  await sshExec(conn, `rm -rf /root/.m2/repository/org/springframework/ai/spring-ai-bom/1.0.0-M3 2>/dev/null; echo "cleaned"`);

  log('编译后端 (强制更新依赖)...');
  const { code: buildCode } = await sshExec(
    conn,
    `cd ${remoteProjectDir}/code/java && mvn clean package -DskipTests -U -pl zrws-approval -am 2>&1 | tail -80`
  );
  
  if (buildCode !== 0) {
    log('编译失败！尝试使用备用方案...');
    
    // 尝试用阿里云的spring插件仓库
    log('尝试添加 Spring Milestone 镜像...');
    await sshExec(conn, `mkdir -p /root/.m2`);
    await sshExec(conn, `cat > /root/.m2/settings.xml << 'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*,!spring-milestones</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>spring-milestones</id>
      <repositories>
        <repository>
          <id>spring-milestones</id>
          <name>Spring Milestones</name>
          <url>https://maven.aliyun.com/repository/spring-milestone</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>spring-milestones</id>
          <name>Spring Milestones</name>
          <url>https://maven.aliyun.com/repository/spring-milestone</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>spring-milestones</activeProfile>
  </activeProfiles>
</settings>
EOF` );
    
    log('重新编译...');
    const { code: buildCode2 } = await sshExec(
      conn,
      `cd ${remoteProjectDir}/code/java && mvn clean package -DskipTests -U -pl zrws-approval -am 2>&1 | tail -80`
    );
    
    if (buildCode2 !== 0) {
      log('编译仍然失败！');
      conn.end();
      process.exit(1);
    }
  }
  log('编译成功');

  log('停止服务...');
  await sshExec(conn, `systemctl stop ${serviceName}`);

  log('备份旧 JAR...');
  await sshExec(conn, `cp ${remoteJarPath} ${remoteJarPath}.bak.v4.3 2>/dev/null || echo "backup done"`);

  log('复制新 JAR...');
  await sshExec(conn, `cp ${remoteProjectDir}/code/java/zrws-approval/target/zrws-approval.jar ${remoteJarPath}`);

  log('启动服务...');
  await sshExec(conn, `systemctl start ${serviceName}`);

  log('等待服务启动 (30s)...');
  await new Promise(r => setTimeout(r, 30000));

  log('检查服务状态...');
  await sshExec(conn, `systemctl status ${serviceName} | head -10`);

  log('健康检查...');
  await sshExec(conn, `curl -s http://127.0.0.1:5571/approval/actuator/health`);
  console.log('');

  log('验证统一返回格式...');
  await sshExec(conn, `curl -s -X POST http://127.0.0.1:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; d=json.load(sys.stdin); print('code:', d.get('code')); print('success:', d.get('success')); print('has data:', 'data' in d)" 2>/dev/null || curl -s -X POST http://127.0.0.1:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | head -c 300`);
  console.log('');

  log('验证列表接口...');
  const verifyCmd = 'TOKEN=$(curl -s -X POST http://127.0.0.1:5571/approval/api/v1/auth/login -H \'Content-Type: application/json\' -d \'{"username":"admin","password":"admin123"}\' | python3 -c "import sys,json; print(json.load(sys.stdin)[\'data\'][\'token\'])" 2>/dev/null || echo "")'
    + '; echo "TOKEN_LEN: ${#TOKEN}"'
    + '; curl -s -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:5571/approval/api/v1/climate-warming/list?page=1&size=3" | head -c 300';
  await sshExec(conn, verifyCmd);
  console.log('');

  log('=== 后端部署完成 ===');
  conn.end();
}

main().catch(err => {
  console.error('部署失败:', err.message);
  process.exit(1);
});
