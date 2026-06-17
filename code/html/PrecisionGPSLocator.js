/**
 * 智壤卫士 - 高精度GPS定位模块
 * PrecisionGPSLocator.js
 */
class PrecisionGPSLocator {
  constructor() {
    this.mode = 'RTK'; // RTK, DGPS, SINGLE
    this.coordSystem = 'CGCS2000';
    this.recording = false;
    this.trackPoints = [];
    this.samplePoints = [];
    this.baseStation = { lat: 28.456720, lng: 112.835210 };
  }

  // 初始化RTK定位
  initRTK(baseStationCoords) {
    this.baseStation = baseStationCoords;
    console.log('[GPS] RTK基站已连接');
    console.log('[GPS] 水平精度: ±1.2cm, 垂直精度: ±2.1cm');
    return {
      horizontal: '±1.2cm',
      vertical: '±2.1cm',
      satellites: 18,
      fixType: 'FIXED'
    };
  }

  // 获取当前位置
  getCurrentPosition() {
    // 模拟RTK高精度定位
    const noise = () => (Math.random() - 0.5) * 0.000002;
    return {
      lat: this.baseStation.lat + noise(),
      lng: this.baseStation.lng + noise(),
      altitude: 120 + (Math.random() - 0.5) * 0.5,
      accuracy: {
        horizontal: 0.012, // 1.2cm
        vertical: 0.021    // 2.1cm
      },
      timestamp: new Date().toISOString()
    };
  }

  // 开始记录航迹
  startRecording() {
    this.recording = true;
    this.trackPoints = [];
    console.log('[GPS] 航迹记录开始');
    
    // 模拟每秒记录一个点
    this.recordInterval = setInterval(() => {
      if (!this.recording) return;
      const pos = this.getCurrentPosition();
      this.trackPoints.push(pos);
      
      if (this.trackPoints.length % 10 === 0) {
        console.log(`[GPS] 已记录 ${this.trackPoints.length} 个航迹点`);
      }
    }, 1000);
  }

  // 停止记录
  stopRecording() {
    this.recording = false;
    clearInterval(this.recordInterval);
    console.log(`[GPS] 航迹记录结束，共 ${this.trackPoints.length} 个点`);
    return this.trackPoints;
  }

  // 标记采样点
  markSamplePoint(note = '') {
    const pos = this.getCurrentPosition();
    const sample = {
      id: `SP-${(this.samplePoints.length + 1).toString().padStart(3, '0')}`,
      ...pos,
      note
    };
    this.samplePoints.push(sample);
    console.log(`[GPS] 采样点已标记: ${sample.id} (${pos.lat.toFixed(6)}, ${pos.lng.toFixed(6)})`);
    return sample;
  }

  // 坐标系转换
  transformCoord(lat, lng, fromSystem, toSystem) {
    const transforms = {
      'WGS84_CGCS2000': (lat, lng) => ({ lat: lat + 0.000006, lng: lng - 0.000004 }),
      'CGCS2000_WGS84': (lat, lng) => ({ lat: lat - 0.000006, lng: lng + 0.000004 }),
      'Beijing54_CGCS2000': (lat, lng) => ({ lat: lat - 0.000053, lng: lng + 0.000047 })
    };
    
    const key = `${fromSystem}_${toSystem}`;
    if (transforms[key]) {
      return transforms[key](lat, lng);
    }
    return { lat, lng };
  }

  // 导出航迹为GPX
  exportToGPX() {
    let gpx = `<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1">
  <trk><name>智壤卫士航迹</name><trkseg>`;
    
    this.trackPoints.forEach(p => {
      gpx += `
    <trkpt lat="${p.lat}" lon="${p.lng}">
      <ele>${p.altitude}</ele>
      <time>${p.timestamp}</time>
    </trkpt>`;
    });
    
    gpx += `
  </trkseg></trk>
</gpx>`;
    return gpx;
  }
}

module.exports = PrecisionGPSLocator;
