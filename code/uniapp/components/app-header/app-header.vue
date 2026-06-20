<template>
  <view class="app-header" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="header-inner">
      <view class="header-left" @tap="onLeftTap" v-if="showBack || $slots.left">
        <slot name="left">
          <text v-if="showBack" class="back-icon">‹</text>
        </slot>
      </view>
      <view class="header-left" v-else></view>

      <view class="header-title">
        <slot>
          <text class="title-text">{{ title }}</text>
        </slot>
      </view>

      <view class="header-right" @tap="onRightTap" v-if="showRight || $slots.right">
        <slot name="right">
          <text v-if="showRight" class="right-text">{{ rightText }}</text>
        </slot>
      </view>
      <view class="header-right" v-else></view>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue'

  const props = defineProps({
    title: { type: String, default: '智壤卫士' },
    showBack: { type: Boolean, default: false },
    showRight: { type: Boolean, default: false },
    rightText: { type: String, default: '操作' },
    backUrl: { type: String, default: '' }
  })

  const statusBarHeight = ref(44)

  // 获取状态栏高度
  try {
    const sysInfo = uni.getSystemInfoSync()
    statusBarHeight.value = sysInfo.statusBarHeight || 44
  } catch (e) {
    statusBarHeight.value = 44
  }

  function onLeftTap() {
    if (props.backUrl) {
      uni.redirectTo({ url: props.backUrl })
    } else {
      uni.navigateBack({
        fail: () => {
          uni.switchTab({ url: '/pages/dashboard/dashboard' })
        }
      })
    }
  }

  function onRightTap() {
    uni.$emit('header-right-tap')
  }
</script>

<style lang="scss" scoped>
  .app-header {
    background: linear-gradient(135deg, #1e3a5f 0%, #2b6cb0 100%);
    color: #fff;
    position: sticky;
    top: 0;
    z-index: 999;
    box-shadow: 0 4rpx 12rpx rgba(30, 58, 95, 0.15);
  }
  .header-inner {
    display: flex;
    align-items: center;
    height: 90rpx;
    padding: 0 24rpx;
  }
  .header-left, .header-right {
    width: 120rpx;
    height: 90rpx;
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }
  .header-right {
    justify-content: flex-end;
  }
  .back-icon {
    font-size: 56rpx;
    font-weight: 300;
    line-height: 1;
    color: #fff;
  }
  .right-text {
    font-size: 28rpx;
    color: #fff;
  }
  .header-title {
    flex: 1;
    text-align: center;
    font-size: 34rpx;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .title-text {
    color: #fff;
  }
</style>
