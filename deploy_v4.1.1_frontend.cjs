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
    name: 'Step 1/5: Fetch code and checkout v4.1.1',
    command: 'cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.1.1',
    timeout: 120000
  },
  {
    name: 'Step 2/5: Fix postcss config',
    command: "printf 'module.exports = { plugins: { autoprefixer: {}, }, }\\n' > /root/workspace/ZRWS/code/html/postcss.config.js && cat /root/workspace/ZRWS/code/html/postcss.config.js",
    timeout: 10000
  },
  {
    name: 'Step 3/5: Build frontend',
    command: 'cd /root/workspace/ZRWS/code/html && (npm ls autoprefixer || npm install autoprefixer) && npm run build',
    timeout: 300000
  },
  {
    name: 'Step 4/5: Deploy frontend',
    command: 'rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/ && ls /var/www/zrws/',
    timeout: 30000
  },
  {
    name: 'Step 5/5: Deploy announcement',
    command: 'mkdir -p /app/announcement && cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html',
    timeout: 10000
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

async function main() {
  console.log('='.repeat(60));
  console.log('  ZRWS v4.1.1 Frontend-only Deployment');
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

    console.log('\n--- Frontend verify ---');
    try {
      await executeCommand(conn, 'curl -sL -o /dev/null -w "%{http_code}" http://localhost/ && echo ""', 15000);
    } catch (e) {}

    console.log('\n--- APP download page verify ---');
    try {
      await executeCommand(conn, 'curl -sL -o /dev/null -w "%{http_code}" http://localhost/app-download.html && echo ""', 15000);
    } catch (e) {}

    console.log('\n\n' + '='.repeat(60));
    console.log('  Deployment Summary');
    console.log('='.repeat(60));
    console.log('  Version: v4.1.1');
    console.log('  Type: Frontend-only deployment');
    console.log('  Frontend: https://www.zrws.cloud');
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
