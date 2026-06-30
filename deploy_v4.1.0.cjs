const { Client } = require('ssh2');

const conn = new Client();

const config = {
  host: '8.163.137.149',
  port: 22,
  username: 'root',
  password: 'Test_admin',
  readyTimeout: 30000
};

const steps = [
  {
    name: 'Step 1/11: Stop backend service',
    command: 'systemctl stop zrws.service',
    timeout: 30000
  },
  {
    name: 'Step 2/11: Backup current JAR',
    command: 'cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)',
    timeout: 30000
  },
  {
    name: 'Step 3/11: Fetch code and checkout v4.1.0',
    command: 'cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.1.0',
    timeout: 120000
  },
  {
    name: 'Step 4/11: Maven build backend (skip tests)',
    command: 'cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true',
    timeout: 600000
  },
  {
    name: 'Step 5/11: Deploy backend JAR',
    command: 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar',
    timeout: 30000
  },
  {
    name: 'Step 6/11: Fix postcss config',
    command: "printf 'module.exports = { plugins: { autoprefixer: {}, }, }\\n' > /root/workspace/ZRWS/code/html/postcss.config.js && cat /root/workspace/ZRWS/code/html/postcss.config.js",
    timeout: 10000
  },
  {
    name: 'Step 7/11: Build frontend',
    command: 'cd /root/workspace/ZRWS/code/html && (npm ls autoprefixer || npm install autoprefixer) && npm run build',
    timeout: 300000
  },
  {
    name: 'Step 8/11: Deploy frontend',
    command: 'rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/',
    timeout: 30000
  },
  {
    name: 'Step 9/11: Deploy announcement',
    command: 'mkdir -p /app/announcement && cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html',
    timeout: 10000
  },
  {
    name: 'Step 10/11: Start backend service',
    command: 'systemctl start zrws.service',
    timeout: 30000
  }
];

function executeCommand(conn, command, timeout) {
  return new Promise((resolve, reject) => {
    console.log('\n' + '='.repeat(60));
    console.log('Executing: ' + command);
    console.log('='.repeat(60));

    let output = '';
    let timedOut = false;

    const timer = setTimeout(() => {
      timedOut = true;
      console.log('\n[ERROR] Command timeout (' + (timeout / 1000) + 's)');
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

        console.log('');
        if (code === 0) {
          console.log('[OK] Command succeeded');
          resolve({ code, output });
        } else {
          console.log('[ERROR] Command failed with exit code: ' + code);
          reject(new Error('Command failed with exit code ' + code));
        }
      });

      stream.stdout.on('data', (data) => {
        const text = data.toString();
        process.stdout.write(text);
        output += text;
      });

      stream.stderr.on('data', (data) => {
        const text = data.toString();
        process.stderr.write(text);
        output += text;
      });
    });
  });
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function main() {
  console.log('='.repeat(60));
  console.log('  ZRWS v4.1.0 Full Deployment');
  console.log('  Server: ' + config.host + ':' + config.port);
  console.log('='.repeat(60));
  console.log('');

  try {
    await new Promise((resolve, reject) => {
      console.log('Connecting to server...');
      conn.on('ready', () => {
        console.log('[OK] SSH connected');
        resolve();
      }).on('error', (err) => {
        console.log('[ERROR] SSH connection error: ' + err.message);
        reject(err);
      }).connect(config);
    });

    for (let i = 0; i < steps.length; i++) {
      const step = steps[i];
      console.log('\n\n' + '#'.repeat(60));
      console.log('# ' + step.name);
      console.log('#'.repeat(60));

      try {
        await executeCommand(conn, step.command, step.timeout);
      } catch (err) {
        console.log('\n' + '!'.repeat(60));
        console.log('[FATAL] ' + step.name + ' FAILED - deployment aborted!');
        console.log('!'.repeat(60));
        conn.end();
        process.exit(1);
      }
    }

    console.log('\n\n' + '#'.repeat(60));
    console.log('# Step 11/11: Wait 60s then verify deployment');
    console.log('#'.repeat(60));
    console.log('\nWaiting 60 seconds for service to start...');

    for (let i = 60; i > 0; i -= 10) {
      console.log('  ' + i + ' seconds remaining...');
      await sleep(10000);
    }

    console.log('\n--- Backend health check ---');
    try {
      await executeCommand(conn, 'curl -s http://localhost:5571/approval/actuator/health', 30000);
    } catch (e) {
      console.log('Health check failed (service may still be starting)');
    }

    console.log('\n--- Frontend check ---');
    try {
      await executeCommand(conn, 'curl -I http://localhost/', 30000);
    } catch (e) {
      console.log('Frontend check failed');
    }

    console.log('\n--- Service status ---');
    try {
      await executeCommand(conn, 'systemctl status zrws.service', 10000);
    } catch (e) {}

    console.log('\n\n' + '='.repeat(60));
    console.log('  Deployment Summary');
    console.log('='.repeat(60));
    console.log('  Version: v4.1.0');
    console.log('  Type: Full deployment (frontend + backend)');
    console.log('  Backend: Started');
    console.log('  Frontend: https://www.zrws.cloud');
    console.log('  Backend API: https://www.zrws.cloud/approval/');
    console.log('  Health check: https://www.zrws.cloud/approval/actuator/health');
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
