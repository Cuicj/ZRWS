/**
 * 智壤卫士 - 无人机飞行控制模块
 * DroneFlightController.js
 */
class DroneFlightController {
  constructor(droneId) {
    this.droneId = droneId;
    this.status = 'idle'; // idle, flying, paused, returning
    this.battery = 100;
    this.altitude = 0;
    this.speed = 0;
    this.position = { lat: 28.456720, lng: 112.835210 };
    this.waypoints = [];
    this.coverage = 0;
  }

  // 设置航线
  setWaypoints(waypoints) {
    this.waypoints = waypoints;
    console.log(`[${this.droneId}] 航线已设置，共 ${waypoints.length} 个航点`);
  }

  // 一键起飞
  async takeoff(targetAltitude = 120) {
    if (this.status !== 'idle') {
      throw new Error('无人机不在待机状态');
    }
    this.status = 'flying';
    console.log(`[${this.droneId}] 起飞中，目标高度 ${targetAltitude}m...`);
    
    // 模拟爬升
    for (let h = 0; h <= targetAltitude; h += 10) {
      this.altitude = h;
      await this.delay(200);
    }
    console.log(`[${this.droneId}] 已到达目标高度 ${targetAltitude}m`);
    this.startMission();
  }

  // 执行航线任务
  async startMission() {
    for (const wp of this.waypoints) {
      if (this.status === 'paused') {
        console.log(`[${this.droneId}] 任务暂停，等待恢复...`);
        await this.waitForResume();
      }
      if (this.status === 'returning') break;
      
      await this.flyTo(wp);
      this.coverage += wp.area || 10;
      this.battery -= 0.5;
      
      // 触发传感器采集
      this.triggerSensors(wp);
    }
    
    if (this.status !== 'returning') {
      this.returnToHome();
    }
  }

  // 飞往指定航点
  async flyTo(waypoint) {
    this.speed = 15; // m/s
    console.log(`[${this.droneId}] 飞往航点 (${waypoint.lat}, ${waypoint.lng})`);
    await this.delay(1000);
    this.position = { lat: waypoint.lat, lng: waypoint.lng };
    console.log(`[${this.droneId}] 到达航点`);
  }

  // 触发传感器采集
  triggerSensors(waypoint) {
    // 多光谱相机
    console.log(`[Sensor] 多光谱采集 @ ${waypoint.lat}, ${waypoint.lng}`);
    // LiDAR扫描
    console.log(`[Sensor] LiDAR点云采集`);
    // 土壤探针（低空悬停时）
    if (waypoint.soilSample) {
      console.log(`[Sensor] 土壤探针检测: pH=${(5.5+Math.random()*2.5).toFixed(1)}`);
    }
    // 拍照留样
    console.log(`[Camera] 2000万像素照片已拍摄，GPS已嵌入`);
  }

  // 暂停任务
  pause() {
    this.status = 'paused';
    this.speed = 0;
    console.log(`[${this.droneId}] 任务已暂停`);
  }

  // 恢复任务
  resume() {
    if (this.status === 'paused') {
      this.status = 'flying';
      console.log(`[${this.droneId}] 任务已恢复`);
    }
  }

  // 一键返航
  async returnToHome() {
    this.status = 'returning';
    console.log(`[${this.droneId}] 执行返航...`);
    await this.delay(2000);
    this.altitude = 0;
    this.speed = 0;
    this.status = 'idle';
    console.log(`[${this.droneId}] 已安全降落，任务完成`);
  }

  // 等待恢复
  waitForResume() {
    return new Promise(resolve => {
      const check = setInterval(() => {
        if (this.status !== 'paused') {
          clearInterval(check);
          resolve();
        }
      }, 500);
    });
  }

  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // 获取飞行状态
  getStatus() {
    return {
      droneId: this.droneId,
      status: this.status,
      battery: this.battery,
      altitude: this.altitude,
      position: this.position,
      coverage: this.coverage
    };
  }
}

// 使用示例
const drone = new DroneFlightController('UAV-DJI-M350-003');
drone.setWaypoints([
  { lat: 28.456720, lng: 112.835210, area: 50, soilSample: true },
  { lat: 28.457000, lng: 112.835500, area: 50 },
  { lat: 28.457200, lng: 112.835800, area: 50, soilSample: true }
]);
// drone.takeoff(120);

module.exports = DroneFlightController;
