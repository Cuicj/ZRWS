const { Client } = require('ssh2');
const conn = new Client();
const config = { host: '8.163.137.149', port: 22, username: 'root', password: 'Test_admin' };

conn.on('ready', () => {
  const cmd = `
echo "=== 1. 登录获取token ==="
TOKEN=$(curl -s -X POST http://localhost:5571/approval/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)
echo "Token: $TOKEN"
echo ""

echo "=== 2. 气候变暖趋势API（带token）==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:5571/approval/api/v1/climate-warming/trend" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}); print('月份:', d.get('months',[])); print('温度距平:', d.get('tempAnomalies',[]))" 2>/dev/null || echo "解析失败"
echo ""

echo "=== 3. 气候变暖列表API ==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:5571/approval/api/v1/climate-warming/list?pageNum=1&pageSize=3" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}); print('总数:', d.get('total')); [print(f'  {r[\"region\"]} - {r[\"monitorDate\"]} - 风险:{r[\"riskLevel\"]}') for r in d.get('list',[])]" 2>/dev/null || echo "解析失败"
echo ""

echo "=== 4. 灾害风险列表API（水土流失）==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:5571/approval/api/v1/disaster-risk/list?disasterType=SOIL_EROSION&pageSize=3" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}); print('总数:', d.get('total')); [print(f'  {r[\"region\"]} - {r[\"disasterType\"]} - 风险:{r[\"riskLevel\"]}') for r in d.get('list',[])]" 2>/dev/null || echo "解析失败"
echo ""

echo "=== 5. 灾害风险统计API ==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:5571/approval/api/v1/disaster-risk/stats" | head -c 300
echo ""

echo ""
echo "=== 6. 沙漠化趋势API ==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:5571/approval/api/v1/desertification/trend" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}); print('月份:', d.get('months',[])); print('植被覆盖度:', d.get('vegetationCoverages',[]))" 2>/dev/null || echo "解析失败"
echo ""
`;
  conn.exec(cmd, (err, stream) => {
    if (err) { console.error(err.message); conn.end(); return; }
    stream.on('data', d => process.stdout.write(d.toString()));
    stream.stderr.on('data', d => process.stderr.write(d.toString()));
    stream.on('close', () => conn.end());
  });
});
conn.on('error', err => { console.error(err.message); process.exit(1); });
conn.connect(config);
