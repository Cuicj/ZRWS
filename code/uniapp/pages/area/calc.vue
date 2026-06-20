<template>
  <app-page-layout title="地块面积测量" :showBack="true" :showTabbar="true">
    <view class="result-card">
      <view class="result-head">
        <text class="result-label">📐 当前测量结果</text>
        <text class="result-points">{{ areaResult.points }}个边界点</text>
      </view>

      <view class="result-main">
        <text class="result-num">{{ areaResult.mu }}</text>
        <text class="result-unit">亩</text>
      </view>

      <view class="result-sub">
        <text>≈ {{ areaResult.sqm }} 平方米</text>
        <text>≈ {{ areaResult.km }} 平方公里</text>
      </view>

      <view class="result-btns">
        <button class="r-btn r-primary" @tap="addPoint">📍 新增边界点</button>
        <button class="r-btn r-secondary" @tap="undoPoint">↩ 撤销</button>
        <button class="r-btn r-danger" @tap="clearPoints">🗑 清空</button>
      </view>
    </view>

    <view class="map-card">
      <view class="map-head">
        <text class="map-title">🗺️ 地块边界</text>
      </view>
      <view class="map-area" @tap="onMapTap">
        <view class="map-grid">
          <view v-for="i in 6" :key="'h'+i" class="ml-h" :style="{top: (i * 16.6) + '%'}"></view>
          <view v-for="i in 6" :key="'v'+i" class="ml-v" :style="{left: (i * 16.6) + '%'}"></view>
        </view>
        <view v-if="boundaryPoints.length > 1" class="boundary-fill"></view>
        <view
          v-for="(p, i) in boundaryPoints"
          :key="i"
          class="boundary-dot"
          :style="{ left: p.x + '%', top: p.y + '%' }">
          <text class="dot-seq">{{ i + 1 }}</text>
        </view>
        <view class="map-center">✈️ 当前位置</view>
      </view>
    </view>

    <view class="points-card">
      <view class="points-head">
        <text class="points-title">📋 边界点列表</text>
      </view>
      <view v-if="boundaryPoints.length === 0" class="empty-small">暂无边界点，请点击上方"新增边界点"</view>
      <view v-for="(p, i) in boundaryPoints" :key="i" class="point-row">
        <view class="pr-seq">{{ i + 1 }}</view>
        <view class="pr-info">
          <text class="pr-coord">X: {{ p.x.toFixed(1) }}%  Y: {{ p.y.toFixed(1) }}%</text>
          <text class="pr-gps">{{ p.lng }}, {{ p.lat }}</text>
        </view>
        <text class="pr-delete" @tap="removePoint(i)">✕</text>
      </view>
    </view>

    <view class="history-head">
      <text class="hh-title">💼 已测量地块 ({{ plotAreas.length }})</text>
    </view>
    <view v-for="(p, i) in plotAreas" :key="i" class="plot-card">
      <view class="plot-head">
        <text class="plot-name">{{ p.name }}</text>
        <view class="plot-diff" :class="{positive: p.diff >= 0}">
          {{ p.diff >= 0 ? '+' : '' }}{{ p.diff.toFixed(2) }} 亩
        </view>
      </view>
      <view class="plot-data">
        <text class="pd-text">GPS测量: <text class="pd-bold">{{ p.gpsArea }} 亩</text></text>
        <text class="pd-text">登记面积: <text class="pd-bold">{{ p.registeredArea }} 亩</text></text>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { mockPlotAreas } from '@/utils/mock.js'
  import { toast } from '@/utils/index.js'

  const boundaryPoints = ref([])
  const plotAreas = ref([])
  const areaResult = reactive({ mu: 0, sqm: 0, km: 0, points: 0 })

  onMounted(() => {
    plotAreas.value = [...mockPlotAreas]
    presetDemo()
  })

  function presetDemo() {
    boundaryPoints.value = [
      { x: 25, y: 30, lng: '112.834000', lat: '28.458000' },
      { x: 70, y: 25, lng: '112.836000', lat: '28.457500' },
      { x: 75, y: 65, lng: '112.837000', lat: '28.455500' },
      { x: 30, y: 75, lng: '112.834200', lat: '28.454000' }
    ]
    calcArea()
  }

  function calcArea() {
    areaResult.points = boundaryPoints.value.length
    if (boundaryPoints.value.length < 3) {
      areaResult.mu = 0
      areaResult.sqm = 0
      areaResult.km = 0
      return
    }
    let area = 0
    const pts = boundaryPoints.value
    const n = pts.length
    for (let i = 0; i < n; i++) {
      const j = (i + 1) % n
      area += pts[i].x * pts[j].y - pts[j].x * pts[i].y
    }
    const sqm = Math.abs(area / 2) * 100
    areaResult.sqm = Math.round(sqm)
    areaResult.mu = (sqm / 666.6667).toFixed(2)
    areaResult.km = (sqm / 1000000).toFixed(4)
  }

  function addPoint() {
    boundaryPoints.value.push({
      x: 20 + Math.random() * 60,
      y: 20 + Math.random() * 60,
      lng: (112.835 + (Math.random() - 0.5) * 0.01).toFixed(6),
      lat: (28.456 + (Math.random() - 0.5) * 0.01).toFixed(6)
    })
    calcArea()
    toast.info('已添加第 ' + boundaryPoints.value.length + ' 个边界点')
  }

  function undoPoint() {
    if (boundaryPoints.value.length === 0) {
      toast.info('没有可撤销的点')
      return
    }
    boundaryPoints.value.pop()
    calcArea()
    toast.info('已撤销上一点')
  }

  function clearPoints() {
    uni.showModal({
      title: '确认清空',
      content: '确定清空所有边界点？',
      success: (res) => {
        if (res.confirm) {
          boundaryPoints.value = []
          calcArea()
          toast.info('已清空')
        }
      }
    })
  }

  function removePoint(i) {
    boundaryPoints.value.splice(i, 1)
    calcArea()
    toast.info('已删除第 ' + (i+1) + ' 个点')
  }

  function onMapTap() {
    boundaryPoints.value.push({
      x: 20 + Math.random() * 60,
      y: 20 + Math.random() * 60,
      lng: (112.835 + (Math.random() - 0.5) * 0.01).toFixed(6),
      lat: (28.456 + (Math.random() - 0.5) * 0.01).toFixed(6)
    })
    calcArea()
  }
</script>

<style lang="scss" scoped>
  .result-card {
    background: linear-gradient(135deg, #67c23a 0%, #4299e1 100%);
    border-radius: 20rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(66,153,225,0.25);
  }
  .result-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
  }
  .result-label { font-size: 28rpx; font-weight: 600; }
  .result-points {
    font-size: 22rpx;
    background: rgba(255,255,255,0.2);
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
  }
  .result-main {
    text-align: center;
    padding: 24rpx 0;
  }
  .result-num {
    font-size: 120rpx;
    font-weight: 700;
    font-family: 'DIN', monospace;
    text-shadow: 0 4rpx 10rpx rgba(0,0,0,0.15);
  }
  .result-unit { font-size: 36rpx; font-weight: 600; margin-left: 10rpx; }
  .result-sub {
    display: flex;
    justify-content: center;
    gap: 30rpx;
    font-size: 24rpx;
    opacity: 0.9;
  }
  .result-btns {
    display: flex;
    gap: 12rpx;
    margin-top: 24rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid rgba(255,255,255,0.2);
  }
  .r-btn {
    flex: 1;
    height: 80rpx;
    font-size: 22rpx;
    font-weight: 600;
    border-radius: 12rpx;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
  }
  .r-primary { background: rgba(255,255,255,0.28); color: #fff; }
  .r-secondary { background: rgba(255,255,255,0.15); color: #fff; }
  .r-danger { background: rgba(244,106,108,0.45); color: #fff; }

  .map-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .map-head { margin-bottom: 20rpx; }
  .map-title { font-size: 28rpx; font-weight: 600; color: #303133; }
  .map-area {
    height: 400rpx;
    background: #f0f9eb;
    border-radius: 12rpx;
    position: relative;
    overflow: hidden;
    border: 2rpx dashed #c7e6c0;
  }
  .map-grid { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
  .ml-h, .ml-v { position: absolute; background: #e8f4e0; }
  .ml-h { left: 0; right: 0; height: 1rpx; }
  .ml-v { top: 0; bottom: 0; width: 1rpx; }

  .boundary-fill {
    position: absolute;
    top: 25%; left: 25%; right: 25%; bottom: 15%;
    background: rgba(103,194,58,0.2);
    border-radius: 20rpx;
  }

  .boundary-dot {
    position: absolute;
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: #fff;
    border: 4rpx solid #2b6cb0;
    transform: translate(-50%, -50%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(43,108,176,0.3);
    z-index: 2;
  }
  .dot-seq { font-size: 22rpx; font-weight: 700; color: #2b6cb0; }
  .map-center {
    position: absolute;
    bottom: 16rpx; left: 16rpx;
    font-size: 22rpx;
    color: #606266;
    background: rgba(255,255,255,0.9);
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
  }

  .points-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .points-head { margin-bottom: 16rpx; }
  .points-title { font-size: 28rpx; font-weight: 600; color: #303133; }
  .empty-small { text-align: center; padding: 40rpx; color: #c0c4cc; font-size: 24rpx; }
  .point-row {
    display: flex;
    align-items: center;
    padding: 16rpx 10rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }
  .point-row:last-child { border-bottom: none; }
  .pr-seq {
    width: 48rpx; height: 48rpx; border-radius: 50%;
    background: #ecf5ff; color: #2b6cb0;
    font-size: 24rpx; font-weight: 600;
    display: flex; align-items: center; justify-content: center;
    margin-right: 16rpx; flex-shrink: 0;
  }
  .pr-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
  .pr-coord { font-size: 24rpx; color: #303133; font-family: 'DIN', monospace; }
  .pr-gps { font-size: 20rpx; color: #909399; font-family: 'DIN', monospace; }
  .pr-delete { font-size: 32rpx; color: #f56c6c; padding: 8rpx 16rpx; }

  .history-head { padding: 10rpx 4rpx 20rpx; }
  .hh-title { font-size: 28rpx; font-weight: 600; color: #303133; }

  .plot-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .plot-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14rpx;
  }
  .plot-name { font-size: 28rpx; font-weight: 600; color: #303133; }
  .plot-diff {
    font-size: 24rpx;
    font-weight: 700;
    color: #f56c6c;
    padding: 6rpx 20rpx;
    background: #fef0f0;
    border-radius: 20rpx;
  }
  .plot-diff.positive { color: #67c23a; background: #f0f9eb; }
  .plot-data { display: flex; gap: 30rpx; font-size: 24rpx; color: #606266; }
  .pd-bold { font-weight: 600; color: #303133; font-family: 'DIN', monospace; }
</style>
