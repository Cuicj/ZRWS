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
    name: '切换到 v4.2.1',
    cmd: 'cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.2.1 && echo "已切换到 v4.2.1"'
  },
  {
    name: '备份当前JAR',
    cmd: 'cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S) && echo "JAR备份完成"'
  },
  {
    name: 'Maven编译后端',
    cmd: 'cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true -q && echo "后端编译完成"'
  },
  {
    name: '部署后端JAR',
    cmd: 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar && ls -la /root/workspace/app.jar && echo "JAR部署完成"'
  },
  {
    name: '构建前端',
    cmd: 'cd /root/workspace/ZRWS/code/html && npm run build 2>&1 | tail -10 && echo "前端构建完成"'
  },
  {
    name: '部署前端',
    cmd: 'rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/ && echo "前端部署完成"'
  },
  {
    name: '启动后端服务',
    cmd: 'systemctl start zrws.service && echo "后端服务已启动"'
  },
  {
    name: '等待80秒后验证',
    cmd: 'sleep 80 && echo "等待完成"'
  },
  {
    name: '验证 - 服务状态',
    cmd: 'systemctl status zrws.service --no-pager | head -10'
  },
  {
    name: '验证 - 健康检查',
    cmd: 'curl -s http://localhost:5571/approval/actuator/health && echo ""'
  },
  {
    name: '验证 - 生态表是否创建',
    cmd: 'mysql -u test_admin -p\'Test_admin\' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE \'%eco%\'; SHOW TABLES LIKE \'%climate%\'; SHOW TABLES LIKE \'%desert%\';" 2>&1 | grep -v Warning'
  },
  {
    name: '验证 - Schema同步日志',
    cmd: 'journalctl -u zrws.service --since "2 minutes ago" --no-pager | grep -E "\[DBA\]|创建表|新增字段|期望表|扫描实体" | head -20'
  }
];

let currentStep = 0;

function runNextStep() {
  if (currentStep >= steps.length) {
    console.log('\n🎉 所有部署步骤已完成！');
    console.log('🌐 前端地址: https://www.zrws.cloud');
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
      setTimeout(runNextStep, 300);
    });
  });
}

console.log('🚀 重新部署 ZRWS v4.2.1 到阿里云服务器...');
console.log('📡 服务器: 8.163.137.149');
console.log('🐛 修复: EcoDataInitializer 编译错误');
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
