/**
 * 智壤卫士 - Mock数据
 * 飞行任务相关Mock数据
 */
const FlightMissionMock = {
  // 任务基础信息
  taskInfo: {
    taskId: 'ZRS-2026-0617-001',
    area: '湖南省长沙市望城区乔口镇',
    droneId: 'UAV-DJI-M350-003',
    operator: '王工',
    flightTime: '2026-06-17T08:30:00+08:00',
    duration: 42,
    coverage: 860,
    altitude: 120,
    overlap: { forward: 0.8, side: 0.65 },
    photos: 1247,
    lidarPoints: 286000000,
    soilSamples: 36,
    gpsAccuracy: { h: '±1.2cm', v: '±2.1cm' },
    status: 'completed'
  },

  // 任务列表
  taskList: [
    { id: 'ZRS-2026-0617-001', area: '望城区乔口镇', status: 'completed', date: '06-17', operator: '王工', coverage: 860 },
    { id: 'ZRS-2026-0616-003', area: '岳麓区莲花镇', status: 'completed', date: '06-16', operator: '李工', coverage: 1250 },
    { id: 'ZRS-2026-0616-002', area: '雨花区跳马镇', status: 'processing', date: '06-16', operator: '王工', coverage: 680 },
    { id: 'ZRS-2026-0615-001', area: '开福区青竹湖', status: 'completed', date: '06-15', operator: '张工', coverage: 520 },
    { id: 'ZRS-2026-0614-002', area: '天心区暮云镇', status: 'abnormal', date: '06-14', operator: '李工', coverage: 320 }
  ],

  // 设备状态
  droneStatus: {
    battery: 78,
    signal: 95,
    storage: 45,
    mode: 'standby'
  },

  // 天气信息
  weather: {
    temp: 28,
    wind: 3.2,
    humidity: 65,
    visibility: '良好'
  }
};

// GPS航迹数据
const GPSMock = {
  // 航迹点数据
  trackPoints: [
    { seq: 1, lat: 28.456720, lng: 112.835210, alt: 120.0, time: '08:30:01' },
    { seq: 2, lat: 28.456735, lng: 112.835325, alt: 120.2, time: '08:30:02' },
    { seq: 3, lat: 28.456750, lng: 112.835440, alt: 119.8, time: '08:30:03' },
    { seq: 4, lat: 28.456768, lng: 112.835560, alt: 120.1, time: '08:30:04' },
    { seq: 5, lat: 28.456785, lng: 112.835680, alt: 120.0, time: '08:30:05' }
  ],

  // 航点数据
  waypoints: [
    { id: 'W1', lat: 28.456720, lng: 112.835210, alt: 120, type: 'takeoff' },
    { id: 'W2', lat: 28.457500, lng: 112.836800, alt: 120, type: 'scan' },
    { id: 'W3', lat: 28.458200, lng: 112.837500, alt: 120, type: 'scan' },
    { id: 'W4', lat: 28.458800, lng: 112.836200, alt: 120, type: 'scan' },
    { id: 'W5', lat: 28.458500, lng: 112.835000, alt: 120, type: 'sample' },
    { id: 'W6', lat: 28.457800, lng: 112.834500, alt: 120, type: 'scan' },
    { id: 'W7', lat: 28.457200, lng: 112.835000, alt: 120, type: 'landing' }
  ],

  // 实时GPS
  currentGPS: {
    lat: 28.456720,
    lng: 112.835210,
    alt: 120.5,
    accuracy: { h: 0.012, v: 0.021 },
    satellites: 18,
    fixType: 'FIXED'
  }
};

// 土壤检测数据
const SoilMock = {
  // 采样点数据
  samples: [
    { id: 'SP-001', lat: 28.45672, lng: 112.83521, pH: 6.8, moisture: 0.32, ec: 245, type: '壤土', date: '06-17' },
    { id: 'SP-002', lat: 28.45718, lng: 112.83605, pH: 7.2, moisture: 0.45, ec: 312, type: '黏土', date: '06-17' },
    { id: 'SP-003', lat: 28.45801, lng: 112.83489, pH: 5.9, moisture: 0.18, ec: 178, type: '砂土', date: '06-17' },
    { id: 'SP-004', lat: 28.45765, lng: 112.83542, pH: 6.5, moisture: 0.28, ec: 220, type: '壤土', date: '06-17' },
    { id: 'SP-005', lat: 28.45822, lng: 112.83618, pH: 7.0, moisture: 0.38, ec: 280, type: '黏土', date: '06-17' },
    { id: 'SP-006', lat: 28.45688, lng: 112.83475, pH: 6.2, moisture: 0.22, ec: 195, type: '砂土', date: '06-17' }
  ],

  // 土质分类统计
  classification: {
    loam: 45.2,
    clay: 28.6,
    sand: 18.3,
    other: 7.9
  },

  // 检测报告统计
  reportStats: {
    totalSamples: 36,
    avgPH: 6.7,
    avgMoisture: 32.5,
    avgEC: 245,
    accuracy: 92
  }
};

// 数据处理进度
const ProcessingMock = {
  // 处理阶段
  stages: [
    { name: '数据导入', progress: 100, status: 'done', message: '导入1247张照片 + 286M LiDAR点云' },
    { name: '质量校验', progress: 100, status: 'done', message: '照片质量检测：1198张合格' },
    { name: '空三解算', progress: 100, status: 'done', message: '285000个稀疏匹配点' },
    { name: '3D重建', progress: 86, status: 'processing', message: '稠密点云加密中：2.45亿点' },
    { name: '坐标转换', progress: 0, status: 'pending', message: '等待3D重建完成' }
  ],

  // 质量校验结果
  qualityResults: [
    { item: '照片质量', total: 1247, pass: 1198, fail: 49, rate: 96.1 },
    { item: 'GPS完整性', total: 1247, pass: 1247, fail: 0, rate: 100 },
    { item: 'LiDAR密度', total: 286, pass: 278, fail: 8, rate: 97.2 },
    { item: '重叠率', total: 0, pass: 0, fail: 0, rate: 78.3 }
  ],

  // 成果统计
  outputStats: {
    totalPhotos: 1247,
    validPhotos: 1198,
    pointCloud: 28600,
    coordSystem: 'CGCS2000',
    resolution: '2cm/pixel'
  }
};

// 面积计算数据
const AreaMock = {
  // 地块列表
  plots: [
    { id: 'P-001', name: '张三家承包地', gpsArea: 8.62, registeredArea: 8.5, diff: 0.12, status: 'normal' },
    { id: 'P-002', name: '村集体水田', gpsArea: 156.38, registeredArea: 156.0, diff: 0.38, status: 'normal' },
    { id: 'P-003', name: '李四旱地', gpsArea: 12.15, registeredArea: 15.0, diff: -2.85, status: 'abnormal' },
    { id: 'P-004', name: '王五菜地', gpsArea: 5.82, registeredArea: 5.8, diff: 0.02, status: 'normal' },
    { id: 'P-005', name: '村小学用地', gpsArea: 8.15, registeredArea: 8.0, diff: 0.15, status: 'review' }
  ],

  // 统计
  stats: {
    totalPlots: 127,
    normalPlots: 118,
    abnormalPlots: 9,
    accuracy: '±0.1%'
  }
};

// OA审批数据
const ApprovalMock = {
  // 审批流程
  workflow: [
    { node: '设计提交', user: '王工', status: 'approved', time: '06-15 14:30', comment: '' },
    { node: '校核', user: '李工', status: 'approved', time: '06-15 16:45', comment: '数据准确' },
    { node: '审核', user: '张总工', status: 'processing', time: '06-17 09:00', comment: '' },
    { node: '批准', user: '陈局长', status: 'pending', time: '--', comment: '' }
  ],

  // 差异比对结果
  diffResult: {
    added: 12,
    deleted: 3,
    modified: 8,
    unchanged: 156
  }
};

// 灾害评估数据
const DisasterMock = {
  risks: [
    { type: '滑坡', level: 'low', description: '地形平缓，风险较低' },
    { type: '泥石流', level: 'medium', description: '沟道存在，需关注雨季' },
    { type: '地面沉降', level: 'low', description: '土层稳定' },
    { type: '洪涝', level: 'medium', description: '排水系统一般' }
  ]
};

// 用户数据
const UserMock = {
  currentUser: {
    id: 'U001',
    name: '王工',
    role: 'operator',
    avatar: 'W',
    dept: '外业采集部'
  },

  users: [
    { id: 'U001', name: '王工', role: 'operator', dept: '外业采集部' },
    { id: 'U002', name: '李工', role: 'analyst', dept: '数据分析部' },
    { id: 'U003', name: '张总工', role: 'reviewer', dept: '技术部' },
    { id: 'U004', name: '陈局长', role: 'admin', dept: '管理层' }
  ]
};

// 导出（浏览器 + Node 兼容）
if (typeof window !== 'undefined') {
  window.ZRSMock = {
    FlightMissionMock,
    GPSMock,
    SoilMock,
    ProcessingMock,
    AreaMock,
    ApprovalMock,
    DisasterMock,
    UserMock
  };
}
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    FlightMissionMock,
    GPSMock,
    SoilMock,
    ProcessingMock,
    AreaMock,
    ApprovalMock,
    DisasterMock,
    UserMock
  };
}