/**
 * 智壤卫士 Mock 数据（开发调试用）
 * 后端接口对接后可移除
 */

export const mockMissions = [
  { id: 'ZRS-2026-0618-001', area: '望城区乔口镇田心村1号地块', operator: '张工', coverage: 86.4, photos: 1247, altitude: 120, status: 'processing', progress: 65, date: '2026-06-18', lng: 112.835210, lat: 28.456720 },
  { id: 'ZRS-2026-0618-002', area: '望城区乔口镇田心村2号地块', operator: '李工', coverage: 72.3, photos: 892, altitude: 150, status: 'pending', progress: 0, date: '2026-06-18', lng: 112.838000, lat: 28.458000 },
  { id: 'ZRS-2026-0617-005', area: '宁乡市花明楼镇3号地块', operator: '王工', coverage: 95.8, photos: 2103, altitude: 180, status: 'completed', progress: 100, date: '2026-06-17', lng: 112.750000, lat: 28.320000 },
  { id: 'ZRS-2026-0617-004', area: '宁乡市花明楼镇2号地块', operator: '陈工', coverage: 68.2, photos: 1530, altitude: 120, status: 'completed', progress: 100, date: '2026-06-17', lng: 112.760000, lat: 28.330000 },
  { id: 'ZRS-2026-0616-003', area: '岳麓区含浦街道土壤调查', operator: '刘工', coverage: 45.6, photos: 820, altitude: 100, status: 'abnormal', progress: 40, date: '2026-06-16', lng: 112.930000, lat: 28.170000 },
  { id: 'ZRS-2026-0615-002', area: '开福区捞刀河街道边界测量', operator: '周工', coverage: 112.5, photos: 2350, altitude: 200, status: 'completed', progress: 100, date: '2026-06-15', lng: 112.980000, lat: 28.250000 }
]

export const mockTelemetry = {
  droneId: 'DJI-M350-003',
  connected: true,
  alt: 118.4,
  lng: 112.835210,
  lat: 28.456720,
  speed: 8.5,
  heading: 92,
  battery: 76,
  sat: 24,
  signal: 95
}

export const mockGpsPoints = (() => {
  const arr = []
  const t0 = Date.now()
  for (let i = 0; i < 10; i++) {
    const t = new Date(t0 - (9 - i) * 2000)
    arr.push({
      seq: i + 1,
      time: t.toLocaleTimeString('zh-CN', { hour12: false }),
      lng: (112.835210 + (Math.random() - 0.5) * 0.001).toFixed(6),
      lat: (28.456720 + (Math.random() - 0.5) * 0.001).toFixed(6),
      alt: (115 + Math.random() * 8).toFixed(1),
      fix: i % 3 === 0 ? 'FLOAT' : 'FIXED'
    })
  }
  return arr
})()

export const mockSoilSamples = [
  { id: 'SP-2026-0618-012', lng: 112.835210, lat: 28.456720, ph: 6.8, moisture: 32.5, ec: 145, type: '壤土', note: '土质均匀，适合耕种' },
  { id: 'SP-2026-0618-011', lng: 112.836010, lat: 28.457220, ph: 7.1, moisture: 28.0, ec: 128, type: '壤土', note: '' },
  { id: 'SP-2026-0618-010', lng: 112.834800, lat: 28.455900, ph: 8.2, moisture: 18.5, ec: 320, type: '盐碱土', note: '局部盐碱化明显' },
  { id: 'SP-2026-0618-009', lng: 112.833500, lat: 28.458100, ph: 5.9, moisture: 22.1, ec: 98, type: '砂土', note: '' },
  { id: 'SP-2026-0618-008', lng: 112.832800, lat: 28.459500, ph: 6.5, moisture: 35.8, ec: 165, type: '黏土', note: '' }
]

export const mockPlotAreas = [
  { name: '1号地块（东北角）', gpsArea: 86.4, registeredArea: 85.2, diff: 1.2 },
  { name: '2号地块（中部）', gpsArea: 72.3, registeredArea: 72.0, diff: 0.3 },
  { name: '3号地块（西侧）', gpsArea: 95.8, registeredArea: 98.5, diff: -2.7 },
  { name: '4号地块（南侧）', gpsArea: 45.6, registeredArea: 46.0, diff: -0.4 },
  { name: '5号地块（农田区）', gpsArea: 156.8, registeredArea: 155.0, diff: 1.8 }
]

export const mockApprovals = [
  { title: 'ZRS-2026-0618-001 成果审批', type: '成果审批', initiator: '张工', currentNode: '校核中', createdAt: '2026-06-18 14:20' },
  { title: 'ZRS-2026-0617-005 质检报告', type: '质检报告', initiator: '李工', currentNode: '审核中', createdAt: '2026-06-18 10:05' },
  { title: '无人机 M350-003 外出报备', type: '飞行报备', initiator: '王工', currentNode: '待审批', createdAt: '2026-06-18 08:30' },
  { title: '测绘物资申领 - 锂电池10块', type: '物资申领', initiator: '刘工', currentNode: '待审批', createdAt: '2026-06-17 17:42' }
]

export const mockDisasterSummary = [
  { type: '滑坡风险', level: '低风险', levelColor: 'success', area: 12.5, desc: '坡度较缓，植被覆盖良好', action: '建议定期巡查，雨季重点关注' },
  { type: '泥石流风险', level: '低风险', levelColor: 'success', area: 8.2, desc: '沟道条件不具备大规模物源', action: '建立雨量监测站，超阈值预警' },
  { type: '洪涝风险', level: '中风险', levelColor: 'warning', area: 24.8, desc: '低洼区排水能力一般，雨季需注意', action: '完善排水系统，预留应急通道' },
  { type: '地面沉降', level: '监测中', levelColor: 'info', area: 15.0, desc: '年均沉降1-2cm，需持续观测', action: '建立沉降监测网络，定期复测' },
  { type: '土壤盐碱化', level: '中风险', levelColor: 'warning', area: 6.5, desc: '西侧局部区域EC值偏高', action: '建议水利改良，增加排灌设施' }
]

export default {
  mockMissions,
  mockTelemetry,
  mockGpsPoints,
  mockSoilSamples,
  mockPlotAreas,
  mockApprovals,
  mockDisasterSummary
}
