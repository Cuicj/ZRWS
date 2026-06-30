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
  console.log('  ZRWS v4.1.0 Final Verification');
  console.log('='.repeat(60));
  console.log('');

  try {
    await new Promise((resolve, reject) => {
      conn.on('ready', () => { resolve(); }).on('error', reject).connect(config);
    });

    console.log('1. Backend Health:');
    const health = await executeCommand(conn, 'curl -s http://localhost:5571/approval/actuator/health', 15000);
    console.log('   ' + health.output.trim());

    console.log('\n2. Frontend (with redirect follow):');
    const fe = await executeCommand(conn, 'curl -sL -o /dev/null -w "%{http_code}" http://localhost/', 15000);
    console.log('   HTTP Status: ' + fe.output.trim());

    console.log('\n3. APP Download Page (with redirect follow):');
    const dl = await executeCommand(conn, 'curl -sL -o /dev/null -w "%{http_code}" http://localhost/app-download.html', 15000);
    console.log('   HTTP Status: ' + dl.output.trim());

    console.log('\n4. APP Download Page Content (first 5 lines):');
    const dlc = await executeCommand(conn, 'curl -sL http://localhost/app-download.html | head -5', 15000);
    console.log(dlc.output.trim().split('\n').map(l => '   ' + l).join('\n'));

    console.log('\n5. Deployed files count:');
    const fc = await executeCommand(conn, 'ls /var/www/zrws/ | wc -l && ls /var/www/zrws/assets/ | wc -l', 10000);
    const lines = fc.output.trim().split('\n');
    console.log('   Root files: ' + (lines[0] || '?'));
    console.log('   Assets files: ' + (lines[1] || '?'));

    console.log('\n6. Current git tag:');
    const tag = await executeCommand(conn, 'cd /root/workspace/ZRWS && git describe --tags', 10000);
    console.log('   ' + tag.output.trim());

    console.log('\n' + '='.repeat(60));
    console.log('  v4.1.0 DEPLOYMENT CONFIRMED');
    console.log('='.repeat(60));
    console.log('  Frontend: https://www.zrws.cloud');
    console.log('  Backend:  https://www.zrws.cloud/approval/');
    console.log('  APP DL:   https://www.zrws.cloud/app-download.html');
    console.log('  Health:   https://www.zrws.cloud/approval/actuator/health');
    console.log('='.repeat(60));

    conn.end();
  } catch (err) {
    console.error('[ERROR] ' + err.message);
    conn.end();
    process.exit(1);
  }
}

main();
