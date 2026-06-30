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
  import { computed } from 'vue'

  const props = defineProps({
    label: { type: String, default: '' },
    value: { type: [String, Number], default: '' },
    trend: { type: String, default: '' },
    icon: { type: String, default: '📊' },
    color: { type: String, default: 'blue' }
  })

  const emit = defineEmits(['tap'])

  const colorClass = computed(() => 'color-' + props.color)

  function onTap() { emit('tap') }
</script>

<style lang="scss" scoped>
  .stat-card {
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx 24rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: $zrws-shadow-sm;
    margin-bottom: 20rpx;
    transition: all .3s ease;
  }
  .stat-card:active {
    transform: translateY(-4rpx);
    box-shadow: $zrws-shadow-md;
  }
  .stat-info {
    flex: 1;
  }
  .stat-value {
    font-size: 56rpx;
    font-weight: 700;
    color: $zrws-primary;
    display: block;
    line-height: 1.2;
    font-family: 'DIN', monospace;
  }
  .stat-label {
    font-size: 26rpx;
    color: $zrws-text-secondary;
    margin-top: 4rpx;
    display: block;
  }
  .stat-trend {
    font-size: 22rpx;
    color: $zrws-success;
    margin-top: 6rpx;
    display: block;
  }
  .stat-icon-box {
    width: 80rpx;
    height: 80rpx;
    border-radius: 20rpx;
    background: $zrws-primary-bg;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 42rpx;
  }
  .color-blue   .stat-icon-box { background: rgba(201, 169, 110, 0.15); }
  .color-green  .stat-icon-box { background: rgba(124, 179, 66, 0.15); }
  .color-orange .stat-icon-box { background: rgba(245, 124, 0, 0.15); }
  .color-red    .stat-icon-box { background: rgba(229, 57, 53, 0.15); }
  .color-purple .stat-icon-box { background: rgba(201, 169, 110, 0.2); }
  .color-blue   .stat-value { color: $zrws-primary; }
  .color-green  .stat-value { color: $zrws-success; }
  .color-orange .stat-value { color: $zrws-warning; }
  .color-red    .stat-value { color: $zrws-error; }
  .color-purple .stat-value { color: $zrws-primary-dark; }
</style>
