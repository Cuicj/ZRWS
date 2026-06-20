<template>
  <view class="page-layout">
    <!-- 全局 Header（通过 easycom 自动引用 app-header 组件）-->
    <app-header
      v-if="showHeader"
      :title="title"
      :showBack="showBack"
      :showRight="showRight"
      :rightText="rightText"
      :backUrl="backUrl">
      <template #left v-if="$slots.left"><slot name="left"></slot></template>
      <template #right v-if="$slots.right"><slot name="right"></slot></template>
    </app-header>

    <!-- 主体滚动区 -->
    <scroll-view
      class="page-scroll"
      scroll-y
      :style="{ height: scrollHeight }"
      :refresher-enabled="refresherEnabled"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh">
      <view class="page-content" :class="{ 'has-tabbar': showTabbar, 'no-padding': noPadding }">
        <slot></slot>
      </view>
      <!-- 底部安全区 -->
      <view class="safe-bottom" v-if="showSafeBottom"></view>
    </scroll-view>

    <!-- 底部导航 Tabbar（自定义） -->
    <view v-if="showTabbar" class="custom-tabbar" :style="{ paddingBottom: safeBottom + 'px' }">
      <view
        v-for="item in tabItems"
        :key="item.page"
        class="tab-item"
        :class="{ active: currentTab === item.page }"
        @tap="switchTab(item.page)">
        <text class="tab-icon">{{ item.icon }}</text>
        <text class="tab-label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue'

  const props = defineProps({
    title: { type: String, default: '智壤卫士' },
    showHeader: { type: Boolean, default: true },
    showBack: { type: Boolean, default: false },
    showRight: { type: Boolean, default: false },
    rightText: { type: String, default: '' },
    backUrl: { type: String, default: '' },
    showTabbar: { type: Boolean, default: false },
    showSafeBottom: { type: Boolean, default: true },
    noPadding: { type: Boolean, default: false },
    refresherEnabled: { type: Boolean, default: false }
  })

  const emit = defineEmits(['refresh'])

  const safeBottom = ref(0)
  const windowHeight = ref(667)
  const isRefreshing = ref(false)

  // Tabbar 项
  const tabItems = [
    { page: 'dashboard', icon: '📊', label: '仪表盘' },
    { page: 'mission',   icon: '📋', label: '任务' },
    { page: 'flight',    icon: '✈️', label: '飞行' },
    { page: 'approval',  icon: '✅', label: '审批' },
    { page: 'mine',      icon: '👤', label: '我的' }
  ]

  const currentTab = computed(() => {
    const pages = getCurrentPages()
    if (pages.length === 0) return 'dashboard'
    const route = pages[pages.length - 1].route || ''
    if (route.includes('dashboard')) return 'dashboard'
    if (route.includes('mission')) return 'mission'
    if (route.includes('flight')) return 'flight'
    if (route.includes('approval')) return 'approval'
    if (route.includes('mine')) return 'mine'
    return 'dashboard'
  })

  const scrollHeight = computed(() => {
    let h = windowHeight.value
    if (props.showHeader) h -= 90 + 44  // header + statusbar 近似值
    if (props.showTabbar) h -= 110  // tabbar
    return h + 'px'
  })

  onMounted(() => {
    try {
      const sysInfo = uni.getSystemInfoSync()
      safeBottom.value = sysInfo.safeAreaInsets?.bottom || 0
      windowHeight.value = sysInfo.windowHeight || 667
    } catch (e) {
      safeBottom.value = 0
      windowHeight.value = 667
    }
  })

  function switchTab(page) {
    const urlMap = {
      'dashboard': '/pages/dashboard/dashboard',
      'mission': '/pages/mission/list',
      'flight': '/pages/flight/control',
      'approval': '/pages/approval/list',
      'mine': '/pages/mine/mine'
    }
    const url = urlMap[page]
    if (!url) return
    if (page === currentTab.value) return
    uni.redirectTo({
      url,
      fail: () => {
        uni.navigateTo({ url })
      }
    })
  }

  function onRefresh() {
    isRefreshing.value = true
    emit('refresh')
    setTimeout(() => {
      isRefreshing.value = false
    }, 800)
  }
</script>

<style lang="scss" scoped>
  .page-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f0f2f5;
  }
  .page-scroll {
    flex: 1;
    width: 100%;
  }
  .page-content {
    padding: 24rpx;
    min-height: 600rpx;
  }
  .page-content.has-tabbar {
    padding-bottom: 40rpx;
  }
  .page-content.no-padding {
    padding: 0;
  }
  .safe-bottom {
    height: 40rpx;
  }

  /* 自定义 Tabbar */
  .custom-tabbar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    height: 110rpx;
    display: flex;
    border-top: 1rpx solid #ebeef5;
    z-index: 99;
    box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.04);
  }
  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 10rpx;
    color: #909399;
    transition: all .2s;
  }
  .tab-item.active {
    color: #1e3a5f;
  }
  .tab-icon {
    font-size: 40rpx;
    margin-bottom: 4rpx;
  }
  .tab-label {
    font-size: 22rpx;
  }
</style>
