const { Client } = require('ssh2');

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

async function run() {
  log('连接服务器...');
  await new Promise((resolve, reject) => {
    conn.on('ready', () => { log('SSH 连接成功'); resolve(); })
      .on('error', reject).connect(config);
  });

  // 查看完整的启动错误堆栈
  log('--- 完整启动错误 ---');
  let r = await exec('journalctl -u zrws.service -n 100 --no-pager | grep -B5 -A20 "WebMvcAutoConfiguration"');
  console.log(r.stdout);

  // 查看更多上下文
  log('--- 错误堆栈 (前100行) ---');
  r = await exec('journalctl -u zrws.service -n 200 --no-pager | grep -A50 "Application run failed" | head -60');
  console.log(r.stdout);

  conn.end();
}

run().catch(err => {
  console.error('失败:', err.message);
  try { conn.end(); } catch(e) {}
  process.exit(1);
});
