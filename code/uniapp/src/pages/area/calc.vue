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
        <zrws-button variant="primary" size="sm" @click="addPoint">📍 新增边界点</zrws-button>
        <zrws-button variant="outline" size="sm" @click="undoPoint">↩ 撤销</zrws-button>
        <zrws-button variant="danger" size="sm" @click="clearPoints">🗑 清空</zrws-button>
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
  import { areaApi } from '@/api/index.js'
  import { toast } from '@/utils/index.js'

  const boundaryPoints = ref([])
  const plotAreas = ref([])
  const loading = ref(false)
  const areaResult = reactive({ mu: 0, sqm: 0, km: 0, points: 0 })

  onMounted(() => {
    loadAreaRecords()
    presetDemo()
  })

  async function loadAreaRecords() {
    loading.value = true
    try {
      const res = await areaApi.list()
      plotAreas.value = res?.list || res?.records || res || []
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

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

  async function saveAreaRecord() {
    if (boundaryPoints.value.length < 3) {
      toast.info('至少需要3个边界点')
      return
    }
    try {
      const points = boundaryPoints.value.map(p => ({ lng: p.lng, lat: p.lat }))
      await areaApi.create({
        points,
        area: areaResult.sqm,
        areaMu: areaResult.mu
      })
      toast.success('测量记录已保存')
      loadAreaRecords()
    } catch (e) {
      // 错误提示已在 request 封装中处理
    }
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
    background: linear-gradient(135deg, $zrws-primary-dark 0%, $zrws-primary 100%);
    border-radius: $zrws-radius-lg;
    padding: 32rpx;
    margin-bottom: 24rpx;
    color: $zrws-text-inverse;
    box-shadow: 0 6rpx 20rpx rgba(168, 139, 79, 0.25);
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
    text-shadow: 0 4rpx 10rpx rgba(93, 78, 55, 0.15);
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
  .result-btns zrws-button {
    flex: 1;
  }

  .map-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .map-head { margin-bottom: 20rpx; }
  .map-title { font-size: 28rpx; font-weight: 600; color: $zrws-text-primary; }
  .map-area {
    height: 400rpx;
    background: $zrws-bg-tertiary;
    border-radius: 12rpx;
    position: relative;
    overflow: hidden;
    border: 2rpx dashed $zrws-border-medium;
  }
  .map-grid { position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
  .ml-h, .ml-v { position: absolute; background: $zrws-border-light; }
  .ml-h { left: 0; right: 0; height: 1rpx; }
  .ml-v { top: 0; bottom: 0; width: 1rpx; }

  .boundary-fill {
    position: absolute;
    top: 25%; left: 25%; right: 25%; bottom: 15%;
    background: rgba(201, 169, 110, 0.2);
    border-radius: 20rpx;
  }

  .boundary-dot {
    position: absolute;
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: $zrws-text-inverse;
    border: 4rpx solid $zrws-primary;
    transform: translate(-50%, -50%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(201, 169, 110, 0.3);
    z-index: 2;
  }
  .dot-seq { font-size: 22rpx; font-weight: 700; color: $zrws-primary-dark; }
  .map-center {
    position: absolute;
    bottom: 16rpx; left: 16rpx;
    font-size: 22rpx;
    color: $zrws-text-secondary;
    background: rgba(255,255,255,0.9);
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
  }

  .points-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 24rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .points-head { margin-bottom: 16rpx; }
  .points-title { font-size: 28rpx; font-weight: 600; color: $zrws-text-primary; }
  .empty-small { text-align: center; padding: 40rpx; color: $zrws-text-tertiary; font-size: 24rpx; }
  .point-row {
    display: flex;
    align-items: center;
    padding: 16rpx 10rpx;
    border-bottom: 1rpx solid $zrws-border-light;
  }
  .point-row:last-child { border-bottom: none; }
  .pr-seq {
    width: 48rpx; height: 48rpx; border-radius: 50%;
    background: $zrws-primary-bg; color: $zrws-primary-dark;
    font-size: 24rpx; font-weight: 600;
    display: flex; align-items: center; justify-content: center;
    margin-right: 16rpx; flex-shrink: 0;
  }
  .pr-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
  .pr-coord { font-size: 24rpx; color: $zrws-text-primary; font-family: 'DIN', monospace; }
  .pr-gps { font-size: 20rpx; color: $zrws-text-tertiary; font-family: 'DIN', monospace; }
  .pr-delete { font-size: 32rpx; color: $zrws-error; padding: 8rpx 16rpx; }

  .history-head { padding: 10rpx 4rpx 20rpx; }
  .hh-title { font-size: 28rpx; font-weight: 600; color: $zrws-text-primary; }

  .plot-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 24rpx;
    margin-bottom: 16rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .plot-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14rpx;
  }
  .plot-name { font-size: 28rpx; font-weight: 600; color: $zrws-text-primary; }
  .plot-diff {
    font-size: 24rpx;
    font-weight: 700;
    color: $zrws-error;
    padding: 6rpx 20rpx;
    background: rgba($zrws-error, 0.1);
    border-radius: 20rpx;
  }
  .plot-diff.positive { color: $zrws-success; background: rgba($zrws-success, 0.1); }
  .plot-data { display: flex; gap: 30rpx; font-size: 24rpx; color: $zrws-text-secondary; }
  .pd-bold { font-weight: 600; color: $zrws-text-primary; font-family: 'DIN', monospace; }
</style>
