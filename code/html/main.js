/**
 * 智壤卫士 - 主程序入口
 * main.js
 */
const DroneFlightController = require('./DroneFlightController');
const PrecisionGPSLocator = require('./PrecisionGPSLocator');
const SoilSensorModule = require('./SoilSensorModule');

class SmartSoilGuardian {
  constructor() {
    this.drone = null;
    this.gps = null;
    this.soilSensor = null;
    this.missionData = {
      photos: [],
      soilReadings: [],
      trackPoints: [],
      startTime: null,
      endTime: null
    };
  }

  // 初始化系统
  async init(droneId) {
    console.log('=== 智壤卫士 系统初始化 ===');
    
    // 初始化无人机
    this.drone = new DroneFlightController(droneId);
    
    // 初始化GPS
    this.gps = new PrecisionGPSLocator();
    this.gps.initRTK({ lat: 28.456720, lng: 112.835210 });
    
    // 初始化土壤传感器
    this.soilSensor = new SoilSensorModule();
    this.soilSensor.calibrate();
    
    console.log('系统初始化完成，准备执行任务');
    return this;
  }

  // 执行完整任务
  async executeMission(areaConfig) {
    this.missionData.startTime = new Date();
    
    // 1. 设置航线
    const waypoints = this.generateWaypoints(areaConfig);
    this.drone.setWaypoints(waypoints);
    
    // 2. 开始GPS记录
    this.gps.startRecording();
    
    // 3. 起飞并执行任务
    await this.drone.takeoff(areaConfig.altitude || 120);
    
    // 4. 任务完成后收集数据
    this.missionData.endTime = new Date();
    this.missionData.trackPoints = this.gps.stopRecording();
    
    // 5. 生成报告
    return this.generateMissionReport();
  }

  // 生成航线航点
  generateWaypoints(config) {
    const waypoints = [];
    const { center, width, height, spacing } = config;
    
    for (let x = 0; x < width; x += spacing) {
      for (let y = 0; y < height; y += spacing) {
        waypoints.push({
          lat: center.lat + y * 0.0001,
          lng: center.lng + x * 0.0001,
          area: spacing * spacing,
          soilSample: (x + y) % (spacing * 2) === 0
        });
      }
    }
    return waypoints;
  }

  // 生成任务报告
  generateMissionReport() {
    const duration = (this.missionData.endTime - this.missionData.startTime) / 1000;
    
    return {
      missionId: `ZRS-${Date.now()}`,
      duration: `${Math.floor(duration / 60)}分${duration % 60}秒`,
      coverage: this.drone.getStatus().coverage,
      photos: this.missionData.photos.length,
      soilSamples: this.missionData.soilReadings.length,
      trackPoints: this.missionData.trackPoints.length,
      batteryUsed: 100 - this.drone.getStatus().battery,
      coordSystem: this.gps.coordSystem
    };
  }
}

// 使用示例
async function demo() {
  const app = new SmartSoilGuardian();
  await app.init('UAV-DJI-M350-003');
  
  // 执行任务
  const report = await app.executeMission({
    center: { lat: 28.456720, lng: 112.835210 },
    width: 1000,   // 米
    height: 800,   // 米
    spacing: 100,  // 米
    altitude: 120  // 米
  });
  
  console.log('任务报告:', report);
}

// demo();

module.exports = SmartSoilGuardian;
