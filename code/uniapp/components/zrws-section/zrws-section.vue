<template>
  <view class="section-card">
    <view class="sc-head" v-if="title">
      <text class="sc-title">{{ title }}</text>
      <text v-if="actionText" class="sc-action" @tap="onAction">{{ actionText }} ›</text>
    </view>
    <view class="sc-body">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
  const props = defineProps({
    title: { type: String, default: '' },
    actionText: { type: String, default: '' }
  })
  const emit = defineEmits(['action'])
  function onAction() { emit('action') }
</script>

<style lang="scss" scoped>
  .section-card {
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
    overflow: hidden;
  }
  .sc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 28rpx 28rpx 20rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }
  .sc-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
  }
  .sc-action {
    font-size: 24rpx;
    color: #2b6cb0;
  }
  .sc-body {
    padding: 24rpx 28rpx;
  }
</style>
