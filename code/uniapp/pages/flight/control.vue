<template>
  <app-page-layout title="飞行控制" :showBack="true" :showTabbar="true">
    <!-- 无人机状态大卡片 -->
    <view class="drone-card">
      <view class="dc-head">
        <text class="dc-name">🛩️ {{ telemetry.droneId }}</text>
        <view class="dc-status" :class="{connected: telemetry.connected}">
          {{ telemetry.connected ? '● 已连接' : '○ 未连接' }}
        </view>
      </view>

      <view class="dc-main">
        <text class="dc-alt">{{ telemetry.alt }}</text>
        <text class="dc-unit">米高度</text>
      </view>

      <view class="dc-data">
        <view class="dc-item">
          <text class="dc-label">⚡ 电量</text>
          <view class="battery-bar">
            <view class="battery-fill" :style="{ width: telemetry.battery + '%' }"></view>
            <text class="battery-text">{{ telemetry.battery }}%</text>
          </view>
        </view>
        <view class="dc-item">
          <text class="dc-label">📡 卫星</text>
          <text class="dc-value-big">{{ telemetry.sat }}</text>
        </view>
        <view class="dc-item">
          <text class="dc-label">🚀 速度</text>
          <text class="dc-value-big">{{ telemetry.speed }} m/s</text>
        </view>
        <view class="dc-item">
          <text class="dc-label">🧭 航向</text>
          <text class="dc-value-big">{{ telemetry.heading }}°</text>
        </view>
      </view>

      <view class="dc-coord">
        <view class="coord-item">
          <text class="coord-label">经度</text>
          <text class="coord-value">{{ telemetry.lng }}°</text>
        </view>
        <view class="coord-item">
          <text class="coord-label">纬度</text>
          <text class="coord-value">{{ telemetry.lat }}°</text>
        </view>
      </view>
    </view>

    <!-- 简易位置显示 -->
    <view class="position-card">
      <view class="pos-head">
        <text class="pos-title">📍 实时位置</text>
        <text class="pos-time">{{ currentTime }}</text>
      </view>
      <view class="pos-map">
        <view class="pos-drone">✈️</view>
        <view class="pos-path">
          <view v-for="(p, i) in recentPath" :key="i" class="pos-point" :style="{ left: p.x + '%', top: p.y + '%' }"></view>
        </view>
      </view>
      <view class="pos-info">
        <text>当前信号强度: {{ telemetry.signal }}%</text>
        <text>RTK状态: 固定解</text>
      </view>
    </view>

    <!-- 操作按钮区 -->
    <view class="action-row">
      <button class="action-btn success" @tap="startFlight">▶ 开始采集</button>
      <button class="action-btn warning" @tap="pauseFlight">⏸ 暂停</button>
      <button class="action-btn danger" @tap="returnHome">↩ 返航</button>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
  import { mockTelemetry } from '@/utils/mock.js'
  import { toast, nav } from '@/utils/index.js'

  const telemetry = reactive({ ...mockTelemetry })
  const currentTime = ref('')
  let timer = null

  // 模拟最近飞行路径点
  const recentPath = computed(() => {
    const arr = []
    for (let i = 0; i < 10; i++) {
      arr.push({
        x: 20 + i * 6 + Math.random() * 5,
        y: 25 + Math.random() * 50
      })
    }
    return arr
  })

  onMounted(() => {
    updateTime()
    timer = setInterval(() => {
      updateTime()
      // 模拟遥测数据变化
      telemetry.alt = (telemetry.alt + (Math.random() - 0.5) * 1.2).toFixed(1)
      telemetry.speed = (8 + Math.random() * 3).toFixed(1)
      telemetry.heading = Math.floor(90 + (Math.random() - 0.5) * 10)
      telemetry.battery = Math.max(0, telemetry.battery - Math.random() * 0.3).toFixed(0)
      telemetry.sat = 22 + Math.floor(Math.random() * 5)
      telemetry.signal = 90 + Math.floor(Math.random() * 10)
      telemetry.lng = (112.835210 + (Math.random() - 0.5) * 0.0005).toFixed(6)
      telemetry.lat = (28.456720 + (Math.random() - 0.5) * 0.0005).toFixed(6)
    }, 1500)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  function updateTime() {
    currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  }

  function startFlight() {
    toast.success('开始执行采集任务')
    telemetry.connected = true
  }
  function pauseFlight() {
    toast.info('飞行已暂停')
  }
  function returnHome() {
    uni.showModal({
      title: '返航确认',
      content: '确定执行一键返航命令？',
      confirmText: '确定',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          toast.info('返航指令已发送')
        }
      }
    })
  }
</script>

<style lang="scss" scoped>
  .drone-card {
    background: linear-gradient(135deg, #1e3a5f 0%, #2b6cb0 100%);
    border-radius: 20rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(30,58,95,0.25);
  }
  .dc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .dc-name {
    font-size: 30rpx;
    font-weight: 600;
  }
  .dc-status {
    font-size: 22rpx;
    padding: 6rpx 16rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 20rpx;
    color: rgba(255,255,255,0.85);
  }
  .dc-status.connected {
    background: rgba(103,194,58,0.3);
    color: #b6ebb0;
  }

  .dc-main {
    text-align: center;
    padding: 30rpx 0 40rpx;
  }
  .dc-alt {
    font-size: 120rpx;
    font-weight: 700;
    font-family: 'DIN', monospace;
    letter-spacing: 2rpx;
  }
  .dc-unit {
    font-size: 26rpx;
    opacity: 0.8;
    margin-left: 8rpx;
  }

  .dc-data {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20rpx;
    padding: 20rpx 0;
    border-top: 1rpx solid rgba(255,255,255,0.15);
    border-bottom: 1rpx solid rgba(255,255,255,0.15);
  }
  .dc-item {
    background: rgba(255,255,255,0.08);
    padding: 16rpx 20rpx;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }
  .dc-label {
    font-size: 22rpx;
    opacity: 0.8;
  }
  .dc-value-big {
    font-size: 28rpx;
    font-weight: 600;
    font-family: 'DIN', monospace;
  }

  .battery-bar {
    position: relative;
    height: 32rpx;
    background: rgba(255,255,255,0.15);
    border-radius: 6rpx;
    overflow: hidden;
  }
  .battery-fill {
    position: absolute;
    top: 0; left: 0; bottom: 0;
    background: linear-gradient(90deg, #f56c6c, #e6a23c, #67c23a);
    border-radius: 6rpx;
    transition: width 0.3s;
  }
  .battery-text {
    position: relative;
    display: block;
    text-align: center;
    font-size: 20rpx;
    line-height: 32rpx;
    font-weight: 600;
    text-shadow: 0 1rpx 2rpx rgba(0,0,0,0.3);
  }

  .dc-coord {
    display: flex;
    justify-content: space-around;
    padding-top: 20rpx;
  }
  .coord-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
  }
  .coord-label {
    font-size: 22rpx;
    opacity: 0.7;
  }
  .coord-value {
    font-size: 26rpx;
    font-weight: 600;
    font-family: 'DIN', monospace;
  }

  .position-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .pos-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .pos-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .pos-time {
    font-size: 22rpx;
    color: #909399;
    font-family: 'DIN', monospace;
  }
  .pos-map {
    height: 360rpx;
    background: linear-gradient(135deg, #e8f0fa, #f5f7fa);
    border-radius: 12rpx;
    position: relative;
    overflow: hidden;
    margin-bottom: 16rpx;
  }
  .pos-drone {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 56rpx;
    animation: pulse 2s infinite;
  }
  @keyframes pulse {
    0%, 100% { transform: translate(-50%, -50%) scale(1); }
    50% { transform: translate(-50%, -50%) scale(1.1); }
  }
  .pos-path {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
  }
  .pos-point {
    position: absolute;
    width: 14rpx;
    height: 14rpx;
    background: #2b6cb0;
    border-radius: 50%;
    opacity: 0.5;
  }
  .pos-info {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: #606266;
    padding: 0 8rpx;
  }

  .action-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 16rpx;
    padding: 10rpx 0;
  }
  .action-btn {
    height: 100rpx;
    font-size: 26rpx;
    font-weight: 600;
    border-radius: 12rpx;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .action-btn.success {
    background: linear-gradient(135deg, #67c23a, #4299e1);
    color: #fff;
  }
  .action-btn.warning {
    background: #fdf6ec;
    color: #e6a23c;
    border: 2rpx solid #e6a23c;
  }
  .action-btn.danger {
    background: #fef0f0;
    color: #f56c6c;
    border: 2rpx solid #f56c6c;
  }
</style>
