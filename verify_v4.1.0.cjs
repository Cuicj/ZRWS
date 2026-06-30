const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

function executeCommand(conn, command, timeout) {
  return new Promise((resolve, reject) => {
    let output = '';
    let timedOut = false;

    const timer = setTimeout(() => {
      timedOut = true;
      reject(new Error('Command timeout'));
    }, timeout);

    conn.exec(command, (err, stream) => {
      if (err) {
        clearTimeout(timer);
        reject(err);
        return;
      }

      stream.on('close', (code) => {
        clearTimeout(timer);
        if (timedOut) return;
        resolve({ code, output });
      });

      stream.stdout.on('data', (data) => {
        output += data.toString();
      });

      stream.stderr.on('data', (data) => {
        output += data.toString();
      });
    });
  });
}

async function main() {
  console.log('='.repeat(60));
  console.log('  ZRWS v4.1.0 Deployment Verification');
  console.log('  Server: ' + config.host);
  console.log('='.repeat(60));
  console.log('');

  try {
    await new Promise((resolve, reject) => {
      console.log('Connecting to server...');
      conn.on('ready', () => {
        console.log('[OK] SSH connected\n');
        resolve();
      }).on('error', (err) => {
        console.log('[ERROR] SSH connection error: ' + err.message);
        reject(err);
      }).connect(config);
    });

    console.log('--- Backend Health Check ---');
    try {
      const result = await executeCommand(conn, 'curl -s http://localhost:5571/approval/actuator/health', 15000);
      console.log(result.output.trim());
      console.log(result.code === 0 ? '[OK] Health check passed\n' : '[FAIL] Health check failed\n');
    } catch (e) {
      console.log('[FAIL] Health check error: ' + e.message + '\n');
    }

    console.log('--- Service Status ---');
    try {
      const result = await executeCommand(conn, 'systemctl status zrws.service --no-pager | head -10', 10000);
      console.log(result.output.trim());
      console.log('');
    } catch (e) {}

    console.log('--- Frontend Check ---');
    try {
      const result = await executeCommand(conn, 'curl -s -o /dev/null -w "%{http_code}" http://localhost/', 10000);
      console.log('HTTP Status: ' + result.output.trim());
      console.log(result.output.trim() === '200' ? '[OK] Frontend accessible\n' : '[WARN] Frontend may have issues\n');
    } catch (e) {
      console.log('[FAIL] Frontend check error: ' + e.message + '\n');
    }

    console.log('--- APP Download Page Check ---');
    try {
      const result = await executeCommand(conn, 'curl -s -o /dev/null -w "%{http_code}" http://localhost/app-download.html', 10000);
      console.log('HTTP Status: ' + result.output.trim());
      console.log(result.output.trim() === '200' ? '[OK] APP download page accessible\n' : '[FAIL] APP download page not found\n');
    } catch (e) {
      console.log('[FAIL] APP download page check error: ' + e.message + '\n');
    }

    console.log('--- Git Version ---');
    try {
      const result = await executeCommand(conn, 'cd /root/workspace/ZRWS && git describe --tags', 10000);
      console.log('Current tag: ' + result.output.trim());
      console.log('');
    } catch (e) {}

    console.log('--- JAR File Info ---');
    try {
      const result = await executeCommand(conn, 'ls -lh /root/workspace/app.jar', 10000);
      console.log(result.output.trim());
      console.log('');
    } catch (e) {}

    console.log('='.repeat(60));
    console.log('  Verification Complete');
    console.log('='.repeat(60));
    console.log('  Version: v4.1.0');
    console.log('  Frontend: https://www.zrws.cloud');
    console.log('  Backend API: https://www.zrws.cloud/approval/');
    console.log('  APP Download: https://www.zrws.cloud/app-download.html');
    console.log('='.repeat(60));

    conn.end();
  } catch (err) {
    console.error('\n[FATAL] ' + err.message);
    conn.end();
    process.exit(1);
  }
}

main();
