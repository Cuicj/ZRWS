<template>
  <app-page-layout title="GPS 实时航迹" :showBack="true" :showTabbar="true">
    <!-- 当前GPS坐标 -->
    <view class="gps-card">
      <view class="gps-head">
        <text class="gps-title">📍 当前 GPS 坐标</text>
        <view class="rtk-tag">RTK FIXED</view>
      </view>

      <view class="gps-rows">
        <view class="gps-row">
          <view class="gps-item">
            <text class="gps-label">经度</text>
            <text class="gps-value">{{ gps.lng }}°</text>
          </view>
          <view class="gps-item">
            <text class="gps-label">纬度</text>
            <text class="gps-value">{{ gps.lat }}°</text>
          </view>
          <view class="gps-item">
            <text class="gps-label">海拔</text>
            <text class="gps-value">{{ gps.alt }} m</text>
          </view>
        </view>
        <view class="gps-row">
          <view class="gps-item">
            <text class="gps-label">卫星数</text>
            <text class="gps-value">{{ gps.sat }}</text>
          </view>
          <view class="gps-item">
            <text class="gps-label">更新时间</text>
            <text class="gps-value">{{ gps.time }}</text>
          </view>
          <view class="gps-item">
            <text class="gps-label">精度</text>
            <text class="gps-value">±2.1 cm</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 简易位置可视化 -->
    <view class="map-card">
      <view class="map-head">
        <text class="map-title">🗺️ 航迹图</text>
        <text class="map-desc">共 {{ trackPoints.length }} 个航点</text>
      </view>
      <view class="map-area">
        <view class="map-grid">
          <view v-for="i in 5" :key="'h'+i" class="grid-line-h" :style="{top: (i * 20) + '%'}"></view>
          <view v-for="i in 5" :key="'v'+i" class="grid-line-v" :style="{left: (i * 20) + '%'}"></view>
        </view>
        <!-- 航点 -->
        <view
          v-for="(p, i) in trackPoints"
          :key="i"
          class="track-dot"
          :class="{active: i === trackPoints.length - 1}"
          :style="{ left: getX(p) + '%', top: getY(p) + '%' }">
          <text v-if="i === trackPoints.length - 1" class="dot-num">{{ p.seq }}</text>
        </view>
        <!-- 当前位置高亮 -->
        <view class="track-current">✈️</view>
      </view>
    </view>

    <!-- 航迹点列表 -->
    <view class="list-card">
      <view class="list-head">
        <text class="list-title">📋 航迹点位</text>
        <button class="btn-export" @tap="exportCSV">导出CSV</button>
      </view>

      <view class="track-table">
        <view class="t-row t-head">
          <text class="t-col t-seq">序号</text>
          <text class="t-col">时间</text>
          <text class="t-col">经纬度</text>
          <text class="t-col">海拔</text>
        </view>
        <scroll-view scroll-y style="max-height: 800rpx;">
          <view v-for="(p, i) in trackPoints" :key="i" class="t-row" :class="{newest: i === trackPoints.length - 1}">
            <text class="t-col t-seq">{{ p.seq }}</text>
            <text class="t-col">{{ p.time }}</text>
            <text class="t-col">{{ p.lng }}, {{ p.lat }}</text>
            <text class="t-col">{{ p.alt }}m</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted, onUnmounted } from 'vue'
  import { gpsApi } from '@/api/index.js'
  import { toast } from '@/utils/index.js'

  const gps = reactive({
    lng: '',
    lat: '',
    alt: '',
    sat: 0,
    time: new Date().toLocaleTimeString('zh-CN', { hour12: false })
  })

  const trackPoints = ref([])
  const loading = ref(false)
  let timer = null
  let missionId = ''

  onMounted(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage?.options || {}
    missionId = options.missionId || ''

    loadTrackData()
    loadRealtimeGps()

    timer = setInterval(() => {
      loadRealtimeGps()
    }, 3000)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  async function loadTrackData() {
    if (!missionId) return
    loading.value = true
    try {
      const res = await gpsApi.track(missionId)
      const data = res?.list || res?.points || res || []
      trackPoints.value = data.map((p, i) => ({
        seq: i + 1,
        time: p.time || p.timestamp || '',
        lng: p.lng || p.longitude || '',
        lat: p.lat || p.latitude || '',
        alt: p.alt || p.altitude || '',
        fix: p.fix || p.fixType || 'FIXED'
      }))
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  async function loadRealtimeGps() {
    try {
      const res = await gpsApi.realtime()
      if (res) {
        gps.lng = res.lng || res.longitude || gps.lng
        gps.lat = res.lat || res.latitude || gps.lat
        gps.alt = res.alt || res.altitude || gps.alt
        gps.sat = res.sat || res.satellites || 0
        gps.time = new Date().toLocaleTimeString('zh-CN', { hour12: false })

        // 实时新增航点
        if (gps.lng && gps.lat) {
          const newPoint = {
            seq: trackPoints.value.length + 1,
            time: gps.time,
            lng: gps.lng,
            lat: gps.lat,
            alt: gps.alt,
            fix: res.fix || res.fixType || 'FIXED'
          }
          trackPoints.value.push(newPoint)
          if (trackPoints.value.length > 100) {
            trackPoints.value.shift()
          }
        }
      }
    } catch (e) {
      // 静默失败，避免频繁弹窗
    }
  }

  function getX(p) {
    const lngs = trackPoints.value.map(t => parseFloat(t.lng)).filter(v => !isNaN(v))
    if (lngs.length === 0) return 50
    const minLng = Math.min(...lngs)
    const maxLng = Math.max(...lngs)
    const range = maxLng - minLng || 0.001
    return 10 + ((parseFloat(p.lng) - minLng) / range) * 80
  }

  function getY(p) {
    const lats = trackPoints.value.map(t => parseFloat(t.lat)).filter(v => !isNaN(v))
    if (lats.length === 0) return 50
    const minLat = Math.min(...lats)
    const maxLat = Math.max(...lats)
    const range = maxLat - minLat || 0.001
    return 90 - ((parseFloat(p.lat) - minLat) / range) * 80
  }

  async function exportCSV() {
    try {
      if (missionId) {
        await gpsApi.export(missionId)
      }
      let csv = '序号,时间,经度,纬度,海拔(m)\n'
      trackPoints.value.forEach(p => {
        csv += p.seq + ',' + p.time + ',' + p.lng + ',' + p.lat + ',' + p.alt + '\n'
      })
      uni.setClipboardData({
        data: csv,
        success: () => {
          toast.success('航迹数据已复制到剪贴板')
        }
      })
    } catch (e) {
      let csv = '序号,时间,经度,纬度,海拔(m)\n'
      trackPoints.value.forEach(p => {
        csv += p.seq + ',' + p.time + ',' + p.lng + ',' + p.lat + ',' + p.alt + '\n'
      })
      uni.setClipboardData({
        data: csv,
        success: () => {
          toast.success('航迹数据已复制到剪贴板')
        }
      })
    }
  }
</script>

<style lang="scss" scoped>
  .gps-card {
    background: linear-gradient(135deg, #1e3a5f, #2b6cb0);
    border-radius: 20rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    color: #fff;
    box-shadow: 0 4rpx 16rpx rgba(30,58,95,0.2);
  }
  .gps-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
  }
  .gps-title {
    font-size: 30rpx;
    font-weight: 600;
  }
  .rtk-tag {
    background: rgba(103,194,58,0.25);
    color: #b6ebb0;
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    font-weight: 600;
  }
  .gps-rows {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
  .gps-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 16rpx;
  }
  .gps-item {
    background: rgba(255,255,255,0.1);
    padding: 20rpx 16rpx;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    align-items: center;
  }
  .gps-label {
    font-size: 22rpx;
    opacity: 0.75;
  }
  .gps-value {
    font-size: 26rpx;
    font-weight: 600;
    font-family: 'DIN', monospace;
    color: #fff;
  }

  .map-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .map-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .map-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .map-desc {
    font-size: 22rpx;
    color: #909399;
  }
  .map-area {
    height: 380rpx;
    background: #f8fafc;
    border-radius: 12rpx;
    position: relative;
    overflow: hidden;
  }
  .map-grid {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
  }
  .grid-line-h, .grid-line-v {
    position: absolute;
    background: #e4e7eb;
  }
  .grid-line-h {
    left: 0; right: 0; height: 1rpx;
  }
  .grid-line-v {
    top: 0; bottom: 0; width: 1rpx;
  }
  .track-dot {
    position: absolute;
    width: 16rpx;
    height: 16rpx;
    background: #2b6cb0;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    opacity: 0.6;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .track-dot.active {
    background: #67c23a;
    width: 28rpx;
    height: 28rpx;
    opacity: 1;
    box-shadow: 0 0 0 8rpx rgba(103,194,58,0.2);
  }
  .dot-num {
    font-size: 14rpx;
    color: #fff;
  }
  .track-current {
    position: absolute;
    top: 50%; left: 50%;
    transform: translate(-50%, -50%);
    font-size: 48rpx;
    animation: pulse 2s infinite;
  }
  @keyframes pulse {
    0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
    50% { transform: translate(-50%, -50%) scale(1.15); opacity: 0.7; }
  }

  .list-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .list-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .list-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .btn-export {
    background: #ecf5ff;
    color: #2b6cb0;
    font-size: 22rpx;
    padding: 10rpx 24rpx;
    border-radius: 20rpx;
    border: none;
    height: auto;
    line-height: 1;
  }

  .track-table {
    border-radius: 12rpx;
    overflow: hidden;
    border: 1rpx solid #ebeef5;
  }
  .t-row {
    display: grid;
    grid-template-columns: 60rpx 140rpx 1fr 80rpx;
    padding: 16rpx 12rpx;
    font-size: 22rpx;
    border-bottom: 1rpx solid #f0f0f0;
    align-items: center;
  }
  .t-row:last-child {
    border-bottom: none;
  }
  .t-row.t-head {
    background: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
  .t-row.newest {
    background: #f0f9eb;
  }
  .t-col {
    text-align: center;
    color: #606266;
    font-family: 'DIN', monospace;
  }
  .t-seq {
    color: #303133;
    font-weight: 600;
  }
</style>
