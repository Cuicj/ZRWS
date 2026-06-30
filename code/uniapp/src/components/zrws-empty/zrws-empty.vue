<template>
  <view class="empty-container">
    <view class="empty-icon">
      <text v-if="icon" class="icon-text">{{ icon }}</text>
      <text v-else class="icon-default">📭</text>
    </view>
    <text class="empty-text">{{ text }}</text>
    <view v-if="showButton" class="empty-button" @tap="onButtonTap">
      <text class="button-text">{{ buttonText }}</text>
    </view>
  </view>
</template>

<script setup>
  const props = defineProps({
    text: { type: String, default: '暂无数据' },
    icon: { type: String, default: '' },
    showButton: { type: Boolean, default: false },
    buttonText: { type: String, default: '去添加' }
  })

  const emit = defineEmits(['button-tap'])

  function onButtonTap() {
    emit('button-tap')
  }
</script>

<style lang="scss" scoped>
  .empty-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80rpx 40rpx;
  }
  .empty-icon {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: $zrws-primary-bg;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;
  }
  .icon-text, .icon-default {
    font-size: 80rpx;
  }
  .empty-text {
    font-size: 28rpx;
    color: $zrws-text-secondary;
    margin-bottom: 40rpx;
    text-align: center;
  }
  .empty-button {
    min-width: 200rpx;
    height: 72rpx;
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    border-radius: 36rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 40rpx;
    box-shadow: 0 4rpx 12rpx rgba(201, 169, 110, 0.3);
    transition: all .3s ease;
  }
  .empty-button:active {
    transform: scale(0.96);
    opacity: 0.9;
  }
  .button-text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 500;
  }
</style>
