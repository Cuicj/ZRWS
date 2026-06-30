<template>
  <app-page-layout title="任务详情" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view v-if="mission" class="detail-card">
      <view class="dc-head">
        <text class="dc-title">{{ mission.id }}</text>
        <view :class="['dc-status', 'status-' + statusColor(mission.status)]">
          {{ statusText(mission.status) }}
        </view>
      </view>

      <view class="dc-area">{{ mission.area }}</view>

      <view class="dc-rows">
        <view class="dc-row">
          <text class="dc-label">中心经纬度</text>
          <text class="dc-value">{{ mission.lng }}°, {{ mission.lat }}°</text>
        </view>
        <view class="dc-row">
          <text class="dc-label">飞行高度</text>
          <text class="dc-value">{{ mission.altitude }} 米</text>
        </view>
        <view class="dc-row">
          <text class="dc-label">覆盖面积</text>
          <text class="dc-value">{{ mission.coverage }} 亩</text>
        </view>
        <view class="dc-row">
          <text class="dc-label">拍摄照片</text>
          <text class="dc-value">{{ mission.photos }} 张</text>
        </view>
        <view class="dc-row">
          <text class="dc-label">操作员</text>
          <text class="dc-value">{{ mission.operator }}</text>
        </view>
        <view class="dc-row">
          <text class="dc-label">执行日期</text>
          <text class="dc-value">{{ mission.date }}</text>
        </view>
      </view>
    </view>

    <!-- 任务进度 -->
    <view v-if="mission" class="progress-card">
      <view class="pc-head">
        <text class="pc-title">任务执行进度</text>
        <text class="pc-percent">{{ mission.progress || 0 }}%</text>
      </view>

      <view class="pc-bar">
        <view class="pc-fill" :style="{ width: (mission.progress || 0) + '%' }"></view>
      </view>

      <view class="pc-steps">
        <view :class="['step', {done: mission.progress >= 20}]">
          <view class="step-dot">{{ mission.progress >= 20 ? '✓' : '1' }}</view>
          <text class="step-label">任务创建</text>
        </view>
        <view :class="['step', {done: mission.progress >= 40}]">
          <view class="step-dot">{{ mission.progress >= 40 ? '✓' : '2' }}</view>
          <text class="step-label">航线规划</text>
        </view>
        <view :class="['step', {done: mission.progress >= 70, active: mission.status === 'processing'}]">
          <view class="step-dot">{{ mission.progress >= 70 ? '✓' : '3' }}</view>
          <text class="step-label">执行采集</text>
        </view>
        <view :class="['step', {done: mission.progress >= 90}]">
          <view class="step-dot">{{ mission.progress >= 90 ? '✓' : '4' }}</view>
          <text class="step-label">数据回传</text>
        </view>
        <view :class="['step', {done: mission.progress >= 100}]">
          <view class="step-dot">{{ mission.progress >= 100 ? '✓' : '5' }}</view>
          <text class="step-label">成果归档</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view v-if="mission" class="action-area">
      <zrws-button variant="primary" size="lg" block @click="goFlight">
        ✈️ 进入飞行控制
      </zrws-button>
      <zrws-button variant="outline" size="lg" block @click="goGps">
        📍 查看GPS航迹
      </zrws-button>
    </view>

    <view v-if="!mission" class="empty-state">未找到任务信息</view>
    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import { missionApi } from '@/api/index.js'
  import { toast, nav } from '@/utils/index.js'

  const mission = ref(null)
  const loading = ref(false)
  let missionId = ''

  onMounted(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage?.options || {}
    missionId = options.id || ''
    loadData()
  })

  async function loadData() {
    if (!missionId) {
      toast.info('任务ID不存在')
      return
    }

    loading.value = true
    try {
      const res = await missionApi.detail(missionId)
      mission.value = res
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  function statusText(s) {
    return { processing: '执行中', pending: '待执行', completed: '已完成', abnormal: '异常' }[s] || s
  }
  function statusColor(s) {
    return { processing: 'primary', pending: 'warning', completed: 'success', abnormal: 'danger' }[s] || 'info'
  }

  function goFlight() {
    nav.to('/pages/flight/control?missionId=' + missionId)
  }
  function goGps() {
    nav.to('/pages/gps/track?missionId=' + missionId)
  }
</script>

<style lang="scss" scoped>
  .detail-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx 32rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .dc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16rpx;
    border-bottom: 1rpx solid $zrws-border-light;
    margin-bottom: 16rpx;
  }
  .dc-title {
    font-size: 32rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .dc-status {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
  }
  .status-primary { background: $zrws-primary-bg; color: $zrws-primary-dark; }
  .status-success { background: rgba($zrws-success, 0.1); color: $zrws-success; }
  .status-warning { background: rgba($zrws-warning, 0.1); color: $zrws-warning; }
  .status-danger  { background: rgba($zrws-error, 0.1); color: $zrws-error; }

  .dc-area {
    font-size: 26rpx;
    color: $zrws-text-secondary;
    padding: 12rpx 0 20rpx;
  }
  .dc-rows {
    padding-top: 16rpx;
    border-top: 1rpx dashed $zrws-border-light;
  }
  .dc-row {
    display: flex;
    justify-content: space-between;
    padding: 14rpx 0;
    font-size: 26rpx;
  }
  .dc-label {
    color: $zrws-text-tertiary;
  }
  .dc-value {
    color: $zrws-text-primary;
    text-align: right;
    max-width: 60%;
  }

  .progress-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx 32rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .pc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .pc-title {
    font-size: 28rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .pc-percent {
    font-size: 28rpx;
    font-weight: 700;
    color: $zrws-primary;
  }
  .pc-bar {
    height: 16rpx;
    background: $zrws-bg-tertiary;
    border-radius: 8rpx;
    overflow: hidden;
    margin-bottom: 30rpx;
  }
  .pc-fill {
    height: 100%;
    background: linear-gradient(90deg, $zrws-primary-light, $zrws-primary);
    border-radius: 8rpx;
    transition: width 0.3s;
  }

  .pc-steps {
    display: flex;
    justify-content: space-between;
    position: relative;
    padding-top: 20rpx;
  }
  .step {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
  }
  .step-dot {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $zrws-bg-tertiary;
    color: $zrws-text-tertiary;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    font-weight: 600;
    margin-bottom: 12rpx;
  }
  .step.done .step-dot {
    background: $zrws-success;
    color: $zrws-text-inverse;
  }
  .step.active .step-dot {
    background: $zrws-primary;
    color: $zrws-text-inverse;
    box-shadow: 0 0 0 6rpx rgba(201, 169, 110, 0.15);
  }
  .step-label {
    font-size: 22rpx;
    color: $zrws-text-tertiary;
    text-align: center;
  }
  .step.done .step-label, .step.active .step-label {
    color: $zrws-text-primary;
    font-weight: 500;
  }

  .action-area {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    margin-top: 24rpx;
  }

  .empty-state {
    text-align: center;
    padding: 120rpx 40rpx;
    color: $zrws-text-tertiary;
    font-size: 26rpx;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
  }
</style>
