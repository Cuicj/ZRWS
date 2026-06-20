<template>
  <view class="stat-card" :class="colorClass" @tap="onTap">
    <view class="stat-info">
      <text class="stat-value">{{ value }}</text>
      <text class="stat-label">{{ label }}</text>
      <text v-if="trend" class="stat-trend">{{ trend }}</text>
    </view>
    <view class="stat-icon-box">
      <text class="stat-icon">{{ icon }}</text>
    </view>
  </view>
</template>

<script setup>
  defineProps({
    label: { type: String, default: '' },
    value: { type: [String, Number], default: '' },
    trend: { type: String, default: '' },
    icon: { type: String, default: '📊' },
    color: { type: String, default: 'blue' }  // blue, green, orange, red, purple
  })

  const emit = defineEmits(['tap'])

  const colorClass = computed(() => 'color-' + props.color)

  import { computed } from 'vue'
  const props = defineProps({
    label: { type: String, default: '' },
    value: { type: [String, Number], default: '' },
    trend: { type: String, default: '' },
    icon: { type: String, default: '📊' },
    color: { type: String, default: 'blue' }
  })

  const colorClass = computed(() => 'color-' + props.color)

  function onTap() { emit('tap') }
</script>

<style lang="scss" scoped>
  .stat-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx 24rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
    margin-bottom: 20rpx;
  }
  .stat-info {
    flex: 1;
  }
  .stat-value {
    font-size: 56rpx;
    font-weight: 700;
    color: #303133;
    display: block;
    line-height: 1.2;
    font-family: 'DIN', monospace;
  }
  .stat-label {
    font-size: 26rpx;
    color: #909399;
    margin-top: 4rpx;
    display: block;
  }
  .stat-trend {
    font-size: 22rpx;
    color: #67c23a;
    margin-top: 6rpx;
    display: block;
  }
  .stat-icon-box {
    width: 80rpx;
    height: 80rpx;
    border-radius: 20rpx;
    background: linear-gradient(135deg, #4299e1, #2b6cb0);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 42rpx;
  }
  .color-blue   .stat-icon-box { background: linear-gradient(135deg, #4299e1, #2b6cb0); }
  .color-green  .stat-icon-box { background: linear-gradient(135deg, #68d391, #67c23a); }
  .color-orange .stat-icon-box { background: linear-gradient(135deg, #f6ad55, #e6a23c); }
  .color-red    .stat-icon-box { background: linear-gradient(135deg, #fc8181, #f56c6c); }
  .color-purple .stat-icon-box { background: linear-gradient(135deg, #b794f4, #9f7aea); }
  .color-blue   .stat-value { color: #2b6cb0; }
  .color-green  .stat-value { color: #67c23a; }
  .color-orange .stat-value { color: #e6a23c; }
  .color-red    .stat-value { color: #f56c6c; }
  .color-purple .stat-value { color: #9f7aea; }
</style>
