/* =======================================================
   智壤卫士 · 移动端 H5 · Vue 3 主逻辑
   单文件SPA，Hash路由，轻量状态
   ======================================================= */

const { createApp, ref, computed, reactive, onMounted, nextTick, watch } = Vue;

const app = createApp({
  setup() {

    /* ============ 登录态 ============ */
    const page = ref('login');
    const user = reactive({
      name: '张工程师',
      role: '外业操作员',
      department: '测绘事业部',
      phone: '138****8888'
    });

    const loginForm = reactive({
      username: '',
      password: '',
      remember: true
    });

    // 检查是否有缓存登录
    const savedLogin = localStorage.getItem('zrws_login');
    if (savedLogin) {
      try {
        const info = JSON.parse(savedLogin);
        Object.assign(user, info.user);
        page.value = 'dashboard';
      } catch (e) {}
    }

    function doLogin() {
      if (!loginForm.username || !loginForm.password) {
        ElementPlus.ElMessage.warning('请输入账号密码');
        return;
      }
      // 简化版：直接登录成功
      ElementPlus.ElMessage.success('登录成功');
      user.name = loginForm.username + '工程师';
      if (loginForm.remember) {
        localStorage.setItem('zrws_login', JSON.stringify({ user: {...user}, time: Date.now() }));
      }
      page.value = 'dashboard';
    }

    function doLogout() {
      ElementPlus.ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('zrws_login');
        page.value = 'login';
        ElementPlus.ElMessage.success('已退出');
      }).catch(() => {});
    }

    /* ============ 页面导航 ============ */
    const menuOpen = ref(false);
    const pageTitleMap = {
      'dashboard': '运行仪表盘',
      'missions': '任务列表',
      'mission-detail': '任务详情',
      'flight-control': '飞行控制',
      'gps-track': 'GPS实时航迹',
      'soil-sample': '土壤采样',
      'area-calc': '面积测量',
      'disaster-risk': '灾害风险评估',
      'approval': '审批中心',
      'mine': '我的'
    };
    const pageTitle = computed(() => pageTitleMap[page.value] || '智壤卫士');

    function goTo(p) {
      page.value = p;
      menuOpen.value = false;
      window.scrollTo(0, 0);
      nextTick(() => initPageCharts(p));
    }
    function toggleMenu() { menuOpen.value = !menuOpen.value; }

    function elMessage(msg, type='info') {
      ElementPlus.ElMessage({ message: msg, type: type, duration: 2000 });
    }

    /* ============ 仪表盘数据 ============ */
    const todayText = new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', weekday:'long' });

    const stats = reactive({
      missionTotal: 128,
      missionToday: 3,
      droneOnline: 2,
      soilSamples: 86,
      soilToday: 12,
      pendingApproval: 5
    });
    const unreadCount = computed(() => stats.pendingApproval);

    const quickItems = [
      { name: '新建任务', icon: '📝', target: 'missions-new' },
      { name: '飞行控制', icon: '✈️', target: 'flight-control' },
      { name: '土壤采样', icon: '🌱', target: 'soil-sample' },
      { name: '面积测量', icon: '📐', target: 'area-calc' },
      { name: 'GPS航迹', icon: '📍', target: 'gps-track' },
      { name: '灾害评估', icon: '⚠️', target: 'disaster-risk' },
      { name: '审批中心', icon: '✅', target: 'approval' },
      { name: 'CAD查看', icon: '🖥️', target: 'pc-only' }
    ];

    /* ============ 任务列表数据 ============ */
    const missionList = [
      { id: 'ZRS-2026-0618-001', area: '望城区乔口镇田心村1号地块', operator: '张工', coverage: 86.4, photos: 1247, altitude: 120, status: 'processing', progress: 65, date: '2026-06-18', lng: '112.835210', lat: '28.456720' },
      { id: 'ZRS-2026-0618-002', area: '望城区乔口镇田心村2号地块', operator: '李工', coverage: 72.3, photos: 892, altitude: 150, status: 'pending', progress: 0, date: '2026-06-18', lng: '112.838000', lat: '28.458000' },
      { id: 'ZRS-2026-0617-005', area: '宁乡市花明楼镇3号地块', operator: '王工', coverage: 95.8, photos: 2103, altitude: 180, status: 'completed', progress: 100, date: '2026-06-17', lng: '112.750000', lat: '28.320000' },
      { id: 'ZRS-2026-0617-004', area: '宁乡市花明楼镇2号地块', operator: '陈工', coverage: 68.2, photos: 1530, altitude: 120, status: 'completed', progress: 100, date: '2026-06-17', lng: '112.760000', lat: '28.330000' },
      { id: 'ZRS-2026-0616-003', area: '岳麓区含浦街道土壤调查', operator: '刘工', coverage: 45.6, photos: 820, altitude: 100, status: 'abnormal', progress: 40, date: '2026-06-16', lng: '112.930000', lat: '28.170000' },
      { id: 'ZRS-2026-0615-002', area: '开福区捞刀河街道边界测量', operator: '周工', coverage: 112.5, photos: 2350, altitude: 200, status: 'completed', progress: 100, date: '2026-06-15', lng: '112.980000', lat: '28.250000' },
      { id: 'ZRS-2026-0614-001', area: '长沙县黄花镇农田调查', operator: '吴工', coverage: 156.8, photos: 3210, altitude: 150, status: 'completed', progress: 100, date: '2026-06-14', lng: '113.100000', lat: '28.230000' }
    ];

    const recentMissions = computed(() => missionList.slice(0, 4));

    const missionFilter = reactive({ keyword: '', status: '' });
    const filteredMissions = computed(() => {
      return missionList.filter(m => {
        const matchKw = !missionFilter.keyword ||
          m.id.includes(missionFilter.keyword) ||
          m.area.includes(missionFilter.keyword);
        const matchStatus = !missionFilter.status || m.status === missionFilter.status;
        return matchKw && matchStatus;
      });
    });

    function setMissionFilter(s) { missionFilter.status = s; }

    function statusText(s) {
      return { processing: '执行中', pending: '待执行', completed: '已完成', abnormal: '异常' }[s] || s;
    }
    function statusColor(s) {
      return { processing: 'primary', pending: 'warning', completed: 'success', abnormal: 'danger' }[s] || 'info';
    }

    const currentMission = ref(null);

    function openMission(m) {
      currentMission.value = m;
      page.value = 'mission-detail';
    }

    /* ============ 新建任务 ============ */
    const newMissionDialog = ref(false);
    const newMission = reactive({
      name: '',
      area: '',
      lng: '',
      lat: '',
      altitude: 120,
      gpsMode: 'RTK',
      note: ''
    });

    function showNewMission() {
      newMissionDialog.value = true;
      // 模拟获取当前位置
      if (!newMission.lng) { newMission.lng = '112.835210'; newMission.lat = '28.456720'; }
    }
    function createMission() {
      if (!newMission.name) {
        ElementPlus.ElMessage.warning('请填写任务名称');
        return;
      }
      const m = {
        id: 'ZRS-' + new Date().toISOString().slice(0,10).replace(/-/g,'') + '-NEW',
        area: newMission.area || newMission.name,
        operator: user.name.split('工')[0] + '工',
        coverage: 0,
        photos: 0,
        altitude: newMission.altitude,
        status: 'pending',
        progress: 0,
        date: new Date().toISOString().slice(0,10),
        lng: newMission.lng,
        lat: newMission.lat
      };
      missionList.unshift(m);
      newMissionDialog.value = false;
      ElementPlus.ElMessage.success('任务创建成功');
      Object.assign(newMission, { name:'', area:'', lng:'', lat:'', altitude:120, gpsMode:'RTK', note:'' });
    }

    /* ============ 飞行控制 ============ */
    const telemetry = reactive({
      droneId: 'DJI M350-003',
      connected: true,
      alt: 118.4,
      lng: '112.835210',
      lat: '28.456720',
      speed: 8.5,
      heading: 92,
      battery: 76,
      sat: 24
    });

    let flightTimer = null;
    function startFlight() {
      ElementPlus.ElMessage.success('开始采集任务');
      if (flightTimer) clearInterval(flightTimer);
      flightTimer = setInterval(() => {
        telemetry.alt = (telemetry.alt + (Math.random() - 0.5) * 1.5).toFixed(1);
        telemetry.speed = (8 + Math.random() * 3).toFixed(1);
        telemetry.heading = Math.floor(90 + (Math.random() - 0.5) * 10);
        telemetry.battery = Math.max(0, telemetry.battery - 0.1).toFixed(0);
        // 经纬度微变动
        const lngFloat = parseFloat(telemetry.lng);
        const latFloat = parseFloat(telemetry.lat);
        telemetry.lng = (lngFloat + (Math.random() - 0.5) * 0.0001).toFixed(6);
        telemetry.lat = (latFloat + (Math.random() - 0.5) * 0.0001).toFixed(6);
      }, 1500);
    }
    function pauseFlight() {
      ElementPlus.ElMessage.info('飞行已暂停');
      if (flightTimer) { clearInterval(flightTimer); flightTimer = null; }
    }
    function returnToHome() {
      ElementPlus.ElMessage.success('返航指令已发送，预计4分钟返航');
      if (flightTimer) { clearInterval(flightTimer); flightTimer = null; }
    }

    /* ============ GPS 实时坐标 ============ */
    const gpsLive = reactive({
      lng: '112.835210',
      lat: '28.456720',
      alt: 118.4,
      sat: 24,
      time: new Date().toLocaleTimeString('zh-CN')
    });

    const trackPoints = computed(() => {
      // 模拟航迹点
      const pts = [];
      const t0 = new Date();
      for (let i = 0; i < 12; i++) {
        const t = new Date(t0.getTime() - (11 - i) * 2000);
        pts.push({
          seq: i + 1,
          time: t.toLocaleTimeString('zh-CN'),
          lng: (112.835210 + (Math.random() - 0.5) * 0.0008).toFixed(6),
          lat: (28.456720 + (Math.random() - 0.5) * 0.0008).toFixed(6),
          alt: (110 + Math.random() * 20).toFixed(1),
          fix: i % 3 === 0 ? 'FLOAT' : 'FIXED'
        });
      }
      return pts;
    });

    let gpsTimer = null;
    function startGps() {
      if (gpsTimer) return;
      gpsTimer = setInterval(() => {
        gpsLive.lng = (112.835210 + (Math.random() - 0.5) * 0.0003).toFixed(6);
        gpsLive.lat = (28.456720 + (Math.random() - 0.5) * 0.0003).toFixed(6);
        gpsLive.alt = (118 + (Math.random() - 0.5) * 2).toFixed(1);
        gpsLive.sat = 22 + Math.floor(Math.random() * 6);
        gpsLive.time = new Date().toLocaleTimeString('zh-CN');
      }, 1500);
    }

    function exportTrackCSV() {
      let csv = '序号,时间,经度,纬度,海拔(m),解算类型\n';
      trackPoints.value.forEach(p => {
        csv += `${p.seq},${p.time},${p.lng},${p.lat},${p.alt},${p.fix}\n`;
      });
      const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'GPSTrack_' + new Date().toISOString().slice(0,10) + '.csv';
      link.click();
      ElementPlus.ElMessage.success('航迹已导出');
    }

    /* ============ 土壤采样 ============ */
    const soilStats = reactive({
      total: 86,
      today: 12,
      avgPH: 6.8
    });
    const soilForm = reactive({
      id: '',
      lng: '112.835210',
      lat: '28.456720',
      ph: '',
      moisture: '',
      ec: '',
      type: '壤土',
      note: ''
    });
    const soilSamples = [
      { id: 'SP-2026-0618-012', lng: '112.835210', lat: '28.456720', ph: 6.8, moisture: 32.5, ec: 145, type: '壤土' },
      { id: 'SP-2026-0618-011', lng: '112.836010', lat: '28.457220', ph: 7.1, moisture: 28.0, ec: 128, type: '壤土' },
      { id: 'SP-2026-0618-010', lng: '112.834800', lat: '28.455900', ph: 8.2, moisture: 18.5, ec: 320, type: '盐碱土' },
      { id: 'SP-2026-0618-009', lng: '112.833500', lat: '28.458100', ph: 5.9, moisture: 22.1, ec: 98, type: '砂土' },
      { id: 'SP-2026-0618-008', lng: '112.832800', lat: '28.459500', ph: 6.5, moisture: 35.8, ec: 165, type: '黏土' }
    ];

    function soilTagColor(type) {
      return { '壤土': 'success', '黏土': 'warning', '砂土': 'info', '粉砂土': '', '泥炭土': 'warning', '盐碱土': 'danger' }[type] || 'info';
    }

    function saveSoilSample() {
      if (!soilForm.id) {
        ElementPlus.ElMessage.warning('请填写采样点编号');
        return;
      }
      if (!soilForm.ph) {
        ElementPlus.ElMessage.warning('请填写pH值');
        return;
      }
      soilSamples.unshift({
        id: soilForm.id,
        lng: soilForm.lng,
        lat: soilForm.lat,
        ph: parseFloat(soilForm.ph),
        moisture: parseFloat(soilForm.moisture) || 0,
        ec: parseFloat(soilForm.ec) || 0,
        type: soilForm.type
      });
      soilStats.total++;
      soilStats.today++;
      soilStats.avgPH = ((soilStats.avgPH * (soilStats.total-1) + parseFloat(soilForm.ph)) / soilStats.total).toFixed(1);
      ElementPlus.ElMessage.success('采样数据已保存');
      // 清空表单
      Object.assign(soilForm, { id:'', ph:'', moisture:'', ec:'', type:'壤土', note:'' });
    }

    /* ============ 面积测量 ============ */
    const areaResult = reactive({
      sqm: 57600,
      mu: 86.4,
      points: 8
    });

    function addAreaPoint() {
      areaResult.points++;
      // 模拟面积增长
      areaResult.sqm += Math.floor(Math.random() * 800 + 300);
      areaResult.mu = (areaResult.sqm / 666.6667).toFixed(2);
      ElementPlus.ElMessage.success('已添加第 ' + areaResult.points + ' 个点位');
    }
    function undoAreaPoint() {
      if (areaResult.points <= 0) return;
      areaResult.points--;
      ElementPlus.ElMessage.info('已撤销上一点');
    }
    function clearAreaPoints() {
      areaResult.points = 0;
      areaResult.sqm = 0;
      areaResult.mu = 0;
      ElementPlus.ElMessage.info('已清空，可重新测量');
    }

    const plotAreas = [
      { name: '1号地块（东北角）', gpsArea: 86.4, registeredArea: 85.2, diff: 1.2 },
      { name: '2号地块（中部）', gpsArea: 72.3, registeredArea: 72.0, diff: 0.3 },
      { name: '3号地块（西侧）', gpsArea: 95.8, registeredArea: 98.5, diff: -2.7 },
      { name: '4号地块（南侧）', gpsArea: 45.6, registeredArea: 46.0, diff: -0.4 },
      { name: '5号地块（农田区）', gpsArea: 156.8, registeredArea: 155.0, diff: 1.8 }
    ];

    /* ============ 灾害评估 ============ */
    const disasterRisks = [
      { type: '滑坡风险', level: '低风险', levelColor: 'success', area: 12.5, desc: '坡度较缓，植被覆盖良好', action: '建议定期巡查，雨季重点关注' },
      { type: '泥石流风险', level: '低风险', levelColor: 'success', area: 8.2, desc: '沟道条件不具备大规模物源', action: '建立雨量监测站，超阈值预警' },
      { type: '洪涝风险', level: '中风险', levelColor: 'warning', area: 24.8, desc: '低洼区排水能力一般，雨季需注意', action: '完善排水系统，预留应急通道' },
      { type: '地面沉降', level: '监测中', levelColor: 'info', area: 15.0, desc: '年均沉降1-2cm，需持续观测', action: '建立沉降监测网络，定期复测' },
      { type: '土壤盐碱化', level: '中风险', levelColor: 'warning', area: 6.5, desc: '西侧局部区域EC值偏高', action: '建议水利改良，增加排灌设施' }
    ];

    /* ============ 审批中心 ============ */
    const approvalStats = reactive({
      pending: 5,
      done: 42,
      initiated: 18
    });

    const approvalFilter = ref('pending');
    const pendingApprovals = [
      { title: 'ZRS-2026-0618-001 成果审批', type: '成果审批', initiator: '张工', currentNode: '校核中', createdAt: '2026-06-18 14:20' },
      { title: 'ZRS-2026-0617-005 质检报告', type: '质检报告', initiator: '李工', currentNode: '审核中', createdAt: '2026-06-18 10:05' },
      { title: '无人机M350-003 外出报备', type: '飞行报备', initiator: '王工', currentNode: '待审批', createdAt: '2026-06-18 08:30' },
      { title: '测绘物资申领-锂电池10块', type: '物资申领', initiator: '刘工', currentNode: '待审批', createdAt: '2026-06-17 17:42' },
      { title: 'ZRS-2026-0616-003 异常情况报告', type: '异常报告', initiator: '周工', currentNode: '待审批', createdAt: '2026-06-17 15:20' }
    ];
    const doneApprovals = [
      { title: 'ZRS-2026-0615-002 成果归档', type: '成果归档', initiator: '吴工', currentNode: '已通过', createdAt: '2026-06-16 09:00', result: '已通过' },
      { title: 'RTK基准站采购申请', type: '物资申领', initiator: '陈工', currentNode: '已通过', createdAt: '2026-06-15 11:30', result: '已通过' },
      { title: 'ZRS-2026-0614-001 成果审批', type: '成果审批', initiator: '孙工', currentNode: '已驳回', createdAt: '2026-06-14 16:45', result: '已驳回' }
    ];

    const filteredApprovals = computed(() => {
      return approvalFilter.value === 'pending' ? pendingApprovals : doneApprovals;
    });

    function approveApproval(a) {
      ElementPlus.ElMessageBox.confirm('确定通过此项审批吗？', '审批', {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        const idx = pendingApprovals.indexOf(a);
        if (idx >= 0) {
          pendingApprovals.splice(idx, 1);
          approvalStats.pending--;
          approvalStats.done++;
          ElementPlus.ElMessage.success('审批通过');
        }
      }).catch(() => {});
    }
    function rejectApproval(a) {
      ElementPlus.ElMessageBox.prompt('请输入驳回原因', '驳回审批', {
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '请填写驳回原因...',
        type: 'warning'
      }).then(() => {
        const idx = pendingApprovals.indexOf(a);
        if (idx >= 0) {
          pendingApprovals.splice(idx, 1);
          approvalStats.pending--;
          ElementPlus.ElMessage.success('已驳回');
        }
      }).catch(() => {});
    }

    /* ============ 图表初始化 ============ */
    const dashboardTrendChartRef = ref(null);
    const altitudeChartRef = ref(null);
    const riskHeatmapRef = ref(null);

    let echartsInstances = [];

    function destroyEcharts() {
      echartsInstances.forEach(ec => { try { ec.dispose(); } catch(e){} });
      echartsInstances = [];
    }

    function initDashboardTrend() {
      if (!dashboardTrendChartRef.value) return;
      const ec = echarts.init(dashboardTrendChartRef.value);
      ec.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 35, right: 15, top: 20, bottom: 25 },
        xAxis: { type: 'category', data: ['6/12','6/13','6/14','6/15','6/16','6/17','6/18'], axisLabel:{fontSize:10} },
        yAxis: { type: 'value', axisLabel:{fontSize:10} },
        series: [{
          name: '任务数', type: 'line', smooth: true,
          data: [2, 3, 5, 4, 3, 6, 3],
          itemStyle: { color: '#2b6cb0' },
          areaStyle: { color: 'rgba(43,108,176,0.15)' }
        },{
          name: '采样点', type: 'line', smooth: true,
          data: [12, 18, 25, 20, 15, 28, 12],
          itemStyle: { color: '#67c23a' }
        }]
      });
      echartsInstances.push(ec);
    }

    function initAltitudeChart() {
      if (!altitudeChartRef.value) return;
      const ec = echarts.init(altitudeChartRef.value);
      ec.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 35, right: 15, top: 15, bottom: 25 },
        xAxis: { type: 'category', data: Array.from({length: 20}, (_,i)=>i+'s'), axisLabel:{fontSize:9} },
        yAxis: { type: 'value', name: 'm', axisLabel:{fontSize:10} },
        series: [{
          type: 'line', smooth: true,
          data: Array.from({length: 20}, () => 110 + Math.random() * 15),
          itemStyle: { color: '#4299e1' },
          areaStyle: { color: 'rgba(66,153,225,0.2)' }
        }]
      });
      echartsInstances.push(ec);
    }

    function initRiskHeatmap() {
      if (!riskHeatmapRef.value) return;
      const ec = echarts.init(riskHeatmapRef.value);
      // 简化：用散点图 + 颜色深浅
      const data = [];
      for (let x = 0; x < 10; x++) {
        for (let y = 0; y < 10; y++) {
          data.push([x, y, Math.floor(Math.random() * 100)]);
        }
      }
      ec.setOption({
        tooltip: { position: 'top', formatter: p => '风险值: ' + p.data[2] },
        grid: { left: 20, right: 20, top: 10, bottom: 20 },
        xAxis: { type: 'category', data: Array.from({length:10},(_,i)=>i), show: false },
        yAxis: { type: 'category', data: Array.from({length:10},(_,i)=>i), show: false },
        visualMap: { min: 0, max: 100, calculable: false, orient: 'horizontal', left: 'center', bottom: 0,
          inRange: { color: ['#67c23a', '#e6a23c', '#f56c6c'] }, textStyle:{fontSize:10} },
        series: [{ name: '风险热力', type: 'heatmap', data: data, label: { show: false } }]
      });
      echartsInstances.push(ec);
    }

    /* ============ 地图初始化 ============ */
    const flightMapRef = ref(null);
    const gpsMapRef = ref(null);
    const areaMapRef = ref(null);
    let mapInstances = [];

    function initFlightMap() {
      if (!flightMapRef.value) return;
      try {
        const m = L.map(flightMapRef.value, { zoomControl: true, attributionControl: false }).setView([28.4567, 112.8352], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(m);
        // 标记无人机位置
        L.circleMarker([28.4567, 112.8352], { radius: 10, color: '#f56c6c', fillColor: '#f56c6c', fillOpacity: 0.8 }).addTo(m).bindPopup('无人机位置');
        // 飞行航迹
        const path = [];
        for (let i = 0; i < 12; i++) {
          path.push([28.4567 + (Math.random()-0.5) * 0.002, 112.8352 + (Math.random()-0.5) * 0.002]);
        }
        L.polyline(path, { color: '#2b6cb0', weight: 3, opacity: 0.8 }).addTo(m);
        mapInstances.push(m);
      } catch (e) { console.warn('Leaflet map init failed', e); }
    }

    function initGPSMap() {
      if (!gpsMapRef.value) return;
      try {
        const m = L.map(gpsMapRef.value, { zoomControl: true, attributionControl: false }).setView([28.4567, 112.8352], 16);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(m);
        trackPoints.value.forEach((p, i) => {
          const color = p.fix === 'FIXED' ? '#67c23a' : '#e6a23c';
          L.circleMarker([parseFloat(p.lat), parseFloat(p.lng)], { radius: 5, color, fillColor: color, fillOpacity: 0.7 })
            .addTo(m).bindTooltip(p.seq + '# ' + p.time);
        });
        // 连线
        L.polyline(trackPoints.value.map(p => [parseFloat(p.lat), parseFloat(p.lng)]), { color: '#409eff', weight: 2 }).addTo(m);
        mapInstances.push(m);
      } catch (e) { console.warn('GPS map init failed', e); }
    }

    function initAreaMap() {
      if (!areaMapRef.value) return;
      try {
        const m = L.map(areaMapRef.value, { zoomControl: true, attributionControl: false }).setView([28.4567, 112.8352], 16);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(m);
        // 绘制一个多边形地块
        const polygon = L.polygon([
          [28.4555, 112.8335],
          [28.4575, 112.8340],
          [28.4580, 112.8365],
          [28.4560, 112.8375],
          [28.4548, 112.8355]
        ], { color: '#67c23a', fillColor: '#67c23a', fillOpacity: 0.3, weight: 2 }).addTo(m);
        polygon.bindPopup('当前测量地块<br/>面积: 86.4 亩');
        mapInstances.push(m);
      } catch (e) { console.warn('Area map init failed', e); }
    }

    /* ============ 页面切换时的初始化 ============ */
    function initPageCharts(p) {
      destroyEcharts();
      mapInstances.forEach(m => { try { m.remove(); } catch(e){} });
      mapInstances = [];
      setTimeout(() => {
        if (p === 'dashboard') initDashboardTrend();
        if (p === 'flight-control') { initFlightMap(); initAltitudeChart(); }
        if (p === 'gps-track') initGPSMap();
        if (p === 'area-calc') initAreaMap();
        if (p === 'disaster-risk') initRiskHeatmap();
      }, 100);
    }

    function showPCOnly() {
      ElementPlus.ElMessageBox.alert('CAD图纸查看功能需在PC端操作，请登录网页版完成', 'PC端功能', {
        confirmButtonText: '知道了', type: 'info'
      });
    }

    /* ============ 页面挂载 ============ */
    onMounted(() => {
      if (page.value !== 'login') {
        initPageCharts(page.value);
      }
      startGps();

      // 监听resize
      window.addEventListener('resize', () => {
        echartsInstances.forEach(ec => { try { ec.resize(); } catch(e){} });
      });
    });

    /* ============ 暴露给template ============ */
    return {
      // 基础
      page, pageTitle, user, menuOpen, toggleMenu, goTo, elMessage,
      // 登录
      loginForm, doLogin, doLogout,
      // 仪表盘
      todayText, stats, unreadCount, quickItems, recentMissions,
      // 任务
      missionList, missionFilter, filteredMissions, setMissionFilter,
      statusText, statusColor, currentMission, openMission,
      newMissionDialog, newMission, showNewMission, createMission,
      // 飞行
      telemetry, startFlight, pauseFlight, returnToHome,
      // GPS
      gpsLive, trackPoints, exportTrackCSV,
      // 土壤
      soilStats, soilForm, soilSamples, soilTagColor, saveSoilSample,
      // 面积
      areaResult, addAreaPoint, undoAreaPoint, clearAreaPoints, plotAreas,
      // 灾害
      disasterRisks,
      // 审批
      approvalStats, approvalFilter, filteredApprovals, approveApproval, rejectApproval,
      // refs
      dashboardTrendChartRef, altitudeChartRef, riskHeatmapRef, flightMapRef, gpsMapRef, areaMapRef,
      // pc only
      showPCOnly
    };
  }
});

app.use(ElementPlus);
app.mount('#app');
