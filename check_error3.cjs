const { Client } = require('ssh2');
const conn = new Client();
const config = { host: '8.163.137.149', port: 22, username: 'root', password: 'Test_admin' };

conn.on('ready', () => {
  const cmd = `cd /root/workspace/ZRWS/code/java && mvn clean compile -Dmaven.test.skip=true -pl zrws-approval -am 2>&1 | grep -E "ERROR|error:" | head -30`;
  conn.exec(cmd, (err, stream) => {
    if (err) { console.error(err.message); conn.end(); return; }
    stream.on('data', d => process.stdout.write(d.toString()));
    stream.stderr.on('data', d => process.stderr.write(d.toString()));
    stream.on('close', () => conn.end());
  });
});
conn.on('error', err => { console.error(err.message); process.exit(1); });
conn.connect(config);
