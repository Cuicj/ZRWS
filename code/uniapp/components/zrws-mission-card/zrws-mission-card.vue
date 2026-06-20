<template>
  <view class="mission-card" @tap="onTap">
    <view class="mc-head">
      <text class="mc-id">{{ mission.id }}</text>
      <view :class="['mc-tag', 'tag-' + statusColor(mission.status)]">{{ statusText(mission.status) }}</view>
    </view>
    <view class="mc-desc">{{ mission.area || mission.name }}</view>
    <view class="mc-meta">
      <text v-if="mission.operator">👤 {{ mission.operator }}</text>
      <text v-if="mission.coverage">📐 {{ mission.coverage }} 亩</text>
      <text v-if="mission.date">📅 {{ mission.date }}</text>
    </view>
    <view v-if="mission.progress !== undefined" class="mc-progress">
      <view class="mc-progress-bar">
        <view class="mc-progress-fill" :style="{ width: mission.progress + '%' }"></view>
      </view>
      <text class="mc-progress-text">{{ mission.progress }}%</text>
    </view>
  </view>
</template>

<script setup>
  const props = defineProps({
    mission: { type: Object, required: true }
  })
  const emit = defineEmits(['tap'])

  function statusText(s) {
    const map = {
      'processing': '执行中',
      'pending': '待执行',
      'completed': '已完成',
      'abnormal': '异常',
      'running': '执行中'
    }
    return map[s] || s
  }
  function statusColor(s) {
    const map = {
      'processing': 'primary',
      'pending': 'warning',
      'completed': 'success',
      'abnormal': 'danger',
      'running': 'primary'
    }
    return map[s] || 'info'
  }

  function onTap() { emit('tap', props.mission) }
</script>

<style lang="scss" scoped>
  .mission-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .mc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14rpx;
  }
  .mc-id {
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
  }
  .mc-tag {
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
  }
  .tag-primary { background: #ecf5ff; color: #2b6cb0; }
  .tag-success { background: #f0f9eb; color: #67c23a; }
  .tag-warning { background: #fdf6ec; color: #e6a23c; }
  .tag-danger  { background: #fef0f0; color: #f56c6c; }
  .tag-info    { background: #f4f4f5; color: #909399; }
  .mc-desc {
    font-size: 26rpx;
    color: #606266;
    margin-bottom: 16rpx;
    line-height: 1.5;
  }
  .mc-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 24rpx;
    font-size: 22rpx;
    color: #909399;
  }
  .mc-progress {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx dashed #f0f0f0;
  }
  .mc-progress-bar {
    flex: 1;
    height: 12rpx;
    background: #f0f2f5;
    border-radius: 6rpx;
    overflow: hidden;
    margin-right: 16rpx;
  }
  .mc-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #4299e1, #67c23a);
    border-radius: 6rpx;
    transition: width .3s;
  }
  .mc-progress-text {
    font-size: 22rpx;
    color: #2b6cb0;
    font-weight: 600;
  }
</style>
