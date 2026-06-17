/**
 * 智壤卫士 - 土壤探针检测模块
 * SoilSensorModule.js
 */
class SoilSensorModule {
  constructor() {
    this.calibrated = false;
    this.probeDepth = 30; // cm
    this.lastReading = null;
  }

  // 校准传感器
  calibrate() {
    console.log('[Soil] 传感器校准中...');
    // 模拟校准过程
    setTimeout(() => {
      this.calibrated = true;
      console.log('[Soil] 校准完成，精度 ±0.1');
    }, 1000);
    return this;
  }

  // 执行检测
  async detect() {
    if (!this.calibrated) {
      throw new Error('传感器未校准');
    }

    console.log('[Soil] 探针插入土壤，深度 30cm...');
    await this.delay(2000);

    // 模拟传感器读数
    const reading = {
      ph: (5.5 + Math.random() * 2.5).toFixed(1),
      moisture: (Math.random() * 0.5).toFixed(3),
      ec: Math.floor(150 + Math.random() * 200), // 电导率 μs/cm
      temperature: (18 + Math.random() * 10).toFixed(1),
      timestamp: new Date().toISOString()
    };

    // 判断土质类型
    reading.soilType = this.classifySoil(reading);
    
    this.lastReading = reading;
    console.log(`[Soil] 检测完成: pH=${reading.ph}, 含水量=${(reading.moisture*100).toFixed(1)}%, ${reading.soilType}`);
    return reading;
  }

  // 土质分类
  classifySoil(reading) {
    const ph = parseFloat(reading.ph);
    const moisture = parseFloat(reading.moisture);
    
    if (moisture > 0.35 && ph > 6.0 && ph < 7.5) return '壤土';
    if (moisture > 0.4) return '黏土';
    if (moisture < 0.25) return '砂土';
    return '混合土';
  }

  // 批量检测
  async batchDetect(locations) {
    const results = [];
    for (const loc of locations) {
      console.log(`[Soil] 在 (${loc.lat}, ${loc.lng}) 执行检测...`);
      const reading = await this.detect();
      results.push({ location: loc, ...reading });
    }
    return results;
  }

  // 生成检测报告
  generateReport(readings) {
    const stats = {
      total: readings.length,
      avgPH: (readings.reduce((s, r) => s + parseFloat(r.ph), 0) / readings.length).toFixed(2),
      avgMoisture: (readings.reduce((s, r) => s + parseFloat(r.moisture), 0) / readings.length * 100).toFixed(1),
      soilTypes: {}
    };

    readings.forEach(r => {
      stats.soilTypes[r.soilType] = (stats.soilTypes[r.soilType] || 0) + 1;
    });

    return stats;
  }

  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

module.exports = SoilSensorModule;
