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
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-md;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
    border: 1rpx solid $zrws-border-light;
    transition: all .3s ease;
  }
  .mission-card:active {
    transform: translateY(-4rpx);
    box-shadow: $zrws-shadow-md;
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
    color: $zrws-text-primary;
  }
  .mc-tag {
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
  }
  .tag-primary { background: $zrws-primary; color: #fff; }
  .tag-success { background: rgba(124, 179, 66, 0.15); color: $zrws-success; }
  .tag-warning { background: rgba(245, 124, 0, 0.15); color: $zrws-warning; }
  .tag-danger  { background: rgba(229, 57, 53, 0.15); color: $zrws-error; }
  .tag-info    { background: $zrws-bg-tertiary; color: $zrws-text-secondary; }
  .mc-desc {
    font-size: 26rpx;
    color: $zrws-text-secondary;
    margin-bottom: 16rpx;
    line-height: 1.5;
  }
  .mc-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 24rpx;
    font-size: 22rpx;
    color: $zrws-text-tertiary;
  }
  .mc-progress {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx dashed $zrws-border-medium;
  }
  .mc-progress-bar {
    flex: 1;
    height: 12rpx;
    background: $zrws-bg-tertiary;
    border-radius: 6rpx;
    overflow: hidden;
    margin-right: 16rpx;
  }
  .mc-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, $zrws-primary-light, $zrws-primary);
    border-radius: 6rpx;
    transition: width .3s;
  }
  .mc-progress-text {
    font-size: 22rpx;
    color: $zrws-primary;
    font-weight: 600;
  }
</style>
