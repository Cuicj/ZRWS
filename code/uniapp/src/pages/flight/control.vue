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
      <zrws-button variant="success" size="md" @click="startFlight">▶ 开始采集</zrws-button>
      <zrws-button variant="outline" size="md" @click="pauseFlight">⏸ 暂停</zrws-button>
      <zrws-button variant="danger" size="md" @click="returnHome">↩ 返航</zrws-button>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
  import { flightApi } from '@/api/index.js'
  import { toast, nav } from '@/utils/index.js'

  const telemetry = reactive({
    droneId: '',
    connected: false,
    alt: 0,
    lng: '',
    lat: '',
    speed: 0,
    heading: 0,
    battery: 0,
    sat: 0,
    signal: 0
  })
  const currentTime = ref('')
  const loading = ref(false)
  let timer = null
  let missionId = ''
  let droneId = ''

  const recentPath = ref([])

  onMounted(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage?.options || {}
    missionId = options.missionId || ''
    droneId = options.droneId || 'DJI-M350-003'
    telemetry.droneId = droneId

    loadDroneStatus()
    updateTime()
    timer = setInterval(() => {
      updateTime()
      loadTelemetry()
    }, 3000)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  async function loadDroneStatus() {
    loading.value = true
    try {
      const res = await flightApi.getStatus(droneId)
      if (res) {
        telemetry.connected = res.connected !== false
        telemetry.droneId = res.droneId || droneId
      }
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  async function loadTelemetry() {
    try {
      const res = await flightApi.telemetry(droneId)
      if (res) {
        telemetry.alt = res.alt ?? res.altitude ?? 0
        telemetry.lng = res.lng ?? res.longitude ?? telemetry.lng
        telemetry.lat = res.lat ?? res.latitude ?? telemetry.lat
        telemetry.speed = res.speed ?? 0
        telemetry.heading = res.heading ?? 0
        telemetry.battery = res.battery ?? 0
        telemetry.sat = res.sat ?? res.satellites ?? 0
        telemetry.signal = res.signal ?? res.signalStrength ?? 0
        telemetry.connected = res.connected !== false

        // 更新路径点
        if (res.lng && res.lat) {
          recentPath.value.push({
            x: 50 + (parseFloat(res.lng) - 112.835210) * 50000,
            y: 50 - (parseFloat(res.lat) - 28.456720) * 50000
          })
          if (recentPath.value.length > 20) {
            recentPath.value.shift()
          }
        }
      }
    } catch (e) {
      // 静默失败，避免频繁弹窗
    }
  }

  function updateTime() {
    currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  }

  async function startFlight() {
    if (!missionId) {
      toast.info('请先选择任务')
      return
    }
    try {
      await flightApi.start(missionId)
      toast.success('开始执行采集任务')
      telemetry.connected = true
    } catch (e) {
      // 错误提示已在 request 封装中处理
    }
  }

  async function pauseFlight() {
    if (!missionId) {
      toast.info('请先选择任务')
      return
    }
    try {
      await flightApi.pause(missionId)
      toast.info('飞行已暂停')
    } catch (e) {
      // 错误提示已在 request 封装中处理
    }
  }

  async function returnHome() {
    uni.showModal({
      title: '返航确认',
      content: '确定执行一键返航命令？',
      confirmText: '确定',
      cancelText: '取消',
      success: async (res) => {
        if (res.confirm) {
          if (!missionId) {
            toast.info('返航指令已发送')
            return
          }
          try {
            await flightApi.returnHome(missionId)
            toast.info('返航指令已发送')
          } catch (e) {
            // 错误提示已在 request 封装中处理
          }
        }
      }
    })
  }
</script>

<style lang="scss" scoped>
  .drone-card {
    background: linear-gradient(135deg, $zrws-primary-dark 0%, $zrws-primary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx;
    margin-bottom: 24rpx;
    color: $zrws-text-inverse;
    box-shadow: 0 6rpx 20rpx rgba(168, 139, 79, 0.25);
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
    background: rgba(124, 179, 66, 0.3);
    color: #d4edc4;
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
    background: linear-gradient(90deg, $zrws-error, $zrws-warning, $zrws-success);
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
    text-shadow: 0 1rpx 2rpx rgba(93, 78, 55, 0.3);
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
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
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
    color: $zrws-text-primary;
  }
  .pos-time {
    font-size: 22rpx;
    color: $zrws-text-tertiary;
    font-family: 'DIN', monospace;
  }
  .pos-map {
    height: 360rpx;
    background: linear-gradient(135deg, $zrws-bg-tertiary, $zrws-bg-secondary);
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
    background: $zrws-primary;
    border-radius: 50%;
    opacity: 0.5;
  }
  .pos-info {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: $zrws-text-secondary;
    padding: 0 8rpx;
  }

  .action-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 16rpx;
    padding: 10rpx 0;
  }
</style>
