const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

const steps = [
  {
    name: '停止后端服务',
    cmd: 'systemctl stop zrws.service && echo "服务已停止"'
  },
  {
    name: '拉取代码并切换版本 v4.2.0',
    cmd: 'cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.2.0 && echo "已切换到 v4.2.0"'
  },
  {
    name: '备份当前JAR',
    cmd: 'cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S) && echo "JAR备份完成" && ls -la /root/workspace/app.jar.backup.* | tail -3'
  },
  {
    name: 'Maven编译后端 (约3-5分钟)',
    cmd: 'cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true -q && echo "后端编译完成"'
  },
  {
    name: '部署后端JAR',
    cmd: 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar && ls -la /root/workspace/app.jar && echo "JAR部署完成"'
  },
  {
    name: '修复前端postcss配置',
    cmd: `cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'POSTCSS_EOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
POSTCSS_EOF
cd /root/workspace/ZRWS/code/html && (npm ls autoprefixer 2>/dev/null || npm install autoprefixer) && echo "autoprefixer就绪"`
  },
  {
    name: '构建前端 (约2-3分钟)',
    cmd: 'cd /root/workspace/ZRWS/code/html && npm run build 2>&1 | tail -20 && echo "前端构建完成"'
  },
  {
    name: '部署前端',
    cmd: 'rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/ && echo "前端部署完成" && ls -la /var/www/zrws/'
  },
  {
    name: '启动后端服务',
    cmd: 'systemctl start zrws.service && echo "后端服务已启动"'
  },
  {
    name: '等待60秒后验证',
    cmd: 'sleep 60 && echo "等待完成"'
  },
  {
    name: '验证部署 - 服务状态',
    cmd: 'systemctl status zrws.service --no-pager'
  },
  {
    name: '验证部署 - 健康检查',
    cmd: 'curl -s http://localhost:5571/approval/actuator/health && echo ""'
  }
];

let currentStep = 0;

function runNextStep() {
  if (currentStep >= steps.length) {
    console.log('\n🎉 所有部署步骤已完成！');
    console.log('🌐 前端地址: https://www.zrws.cloud');
    console.log('📡 API地址: https://www.zrws.cloud/approval/');
    conn.end();
    return;
  }

  const step = steps[currentStep];
  console.log(`\n📌 [${currentStep + 1}/${steps.length}] ${step.name}`);
  console.log('─'.repeat(60));

  conn.exec(step.cmd, (err, stream) => {
    if (err) {
      console.error('❌ 执行失败:', err.message);
      conn.end();
      return;
    }

    let output = '';
    stream.on('data', (data) => {
      output += data.toString();
      process.stdout.write(data.toString());
    });
    stream.stderr.on('data', (data) => {
      output += data.toString();
      process.stderr.write(data.toString());
    });
    stream.on('close', (code) => {
      if (code === 0) {
        console.log(`\n✅ ${step.name} - 完成`);
      } else {
        console.log(`\n⚠️  ${step.name} - 退出码: ${code}`);
      }
      currentStep++;
      setTimeout(runNextStep, 500);
    });
  });
}

console.log('🚀 开始部署 ZRWS v4.2.0 到阿里云服务器...');
console.log('📡 服务器: 8.163.137.149');
console.log('⏰ 预计耗时: 8-15分钟');
console.log('');

conn.on('ready', () => {
  console.log('✅ SSH连接成功');
  runNextStep();
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
