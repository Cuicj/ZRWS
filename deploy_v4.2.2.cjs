const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin'
};

console.log('🚀 开始部署 v4.2.2 - 修复编译错误，重新创建生态环境表...\n');

conn.on('ready', () => {
  console.log('✅ SSH连接成功\n');

  const steps = [
    { cmd: 'systemctl stop zrws.service', desc: '停止服务' },
    { cmd: 'cd /root/workspace/ZRWS && git pull --tags && git checkout v4.2.2', desc: '拉取v4.2.2代码' },
    { cmd: 'cd /root/workspace/ZRWS/code/java && mvn clean install -DskipTests -pl zrws-approval -am -q 2>&1 | tail -20', desc: '编译后端 (约3分钟)' },
    { cmd: 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval.jar /root/workspace/app.jar', desc: '复制JAR包' },
    { cmd: 'systemctl start zrws.service', desc: '启动服务' },
    { cmd: 'sleep 60 && systemctl status zrws.service | head -10', desc: '等待启动并检查状态' },
    { cmd: `
echo "=== 检查生态环境表 ==="
mysql -u test_admin -p'Test_admin' -h rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com zrws_approval -e "SHOW TABLES LIKE '%eco%'; SHOW TABLES LIKE '%climate%'; SHOW TABLES LIKE '%desert%';" 2>&1 | grep -v Warning

echo ""
echo "=== 健康检查 ==="
curl -s http://localhost:5571/approval/actuator/health
echo ""

echo ""
echo "=== 测试API ==="
curl -s "http://localhost:5571/approval/api/v1/eco-standard/list?pageNum=1&pageSize=3" | head -c 200
echo ""
`, desc: '验证表和API' }
  ];

  let current = 0;

  const runNext = () => {
    if (current >= steps.length) {
      console.log('\n🎉 部署完成！');
      conn.end();
      return;
    }

    const step = steps[current];
    console.log(`\n[${current + 1}/${steps.length}] ${step.desc}...`);

    conn.exec(step.cmd, (err, stream) => {
      if (err) {
        console.error(`❌ 步骤失败: ${err.message}`);
        conn.end();
        return;
      }

      let output = '';
      stream.on('data', (data) => {
        output += data.toString();
        if (step.desc.includes('编译') || step.desc.includes('等待')) {
          process.stdout.write('.');
        }
      });
      stream.stderr.on('data', (data) => {
        output += data.toString();
      });
      stream.on('close', (code) => {
        if (code === 0) {
          console.log(' ✅');
          if (!step.desc.includes('编译') && !step.desc.includes('等待')) {
            console.log(output.trim().split('\n').slice(0, 8).join('\n'));
          } else {
            const lines = output.trim().split('\n');
            if (lines.length > 5) {
              console.log(lines.slice(-5).join('\n'));
            }
          }
        } else {
          console.log(' ❌');
          console.log(output);
        }
        current++;
        setTimeout(runNext, 500);
      });
    });
  };

  runNext();
});

conn.on('error', (err) => {
  console.error('❌ SSH连接失败:', err.message);
  process.exit(1);
});

conn.connect(config);
