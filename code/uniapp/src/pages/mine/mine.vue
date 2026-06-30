<template>
  <app-page-layout title="个人中心" :showBack="true" :showTabbar="true">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="uc-avatar">{{ user.name.charAt(0) }}</view>
      <view class="uc-info">
        <text class="uc-name">{{ user.name }}</text>
        <text class="uc-role">{{ user.role }} · {{ user.department }}</text>
        <text class="uc-phone">{{ user.phone }}</text>
      </view>
      <view class="uc-badge">
        <text class="badge-text">认证</text>
      </view>
    </view>

    <!-- 统计 -->
    <view class="stats-block">
      <view class="sb-item">
        <text class="sb-num">{{ stats.missions }}</text>
        <text class="sb-label">执行任务</text>
      </view>
      <view class="sb-item">
        <text class="sb-num">{{ stats.flightHours }}</text>
        <text class="sb-label">飞行小时</text>
      </view>
      <view class="sb-item">
        <text class="sb-num">{{ stats.samples }}</text>
        <text class="sb-label">采样数量</text>
      </view>
      <view class="sb-item">
        <text class="sb-num">{{ stats.area }}</text>
        <text class="sb-label">测绘面积</text>
      </view>
    </view>

    <!-- 我的任务菜单 -->
    <view class="menu-section">
      <text class="menu-title">📋 我的任务</text>
      <view class="menu-list">
        <view v-for="(m, i) in taskMenus" :key="i" class="menu-item" @tap="goTo(m.url)">
          <text class="mi-icon">{{ m.icon }}</text>
          <text class="mi-name">{{ m.name }}</text>
          <view v-if="m.badge" class="mi-badge">{{ m.badge }}</view>
          <text class="mi-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 移动端工具 -->
    <view class="menu-section">
      <text class="menu-title">📱 移动端工具</text>
      <view class="menu-list">
        <view v-for="(m, i) in mobileMenus" :key="i" class="menu-item" @tap="goTo(m.url)">
          <text class="mi-icon">{{ m.icon }}</text>
          <text class="mi-name">{{ m.name }}</text>
          <view v-if="m.badge" class="mi-badge">{{ m.badge }}</view>
          <text class="mi-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="menu-section">
      <text class="menu-title">⚙️ 设置与工具</text>
      <view class="menu-list">
        <view v-for="(m, i) in settingMenus" :key="i" class="menu-item" @tap="doSetting(m)">
          <text class="mi-icon">{{ m.icon }}</text>
          <text class="mi-name">{{ m.name }}</text>
          <text class="mi-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 关于 -->
    <view class="about-card">
      <text class="about-title">智壤卫士</text>
      <text class="about-version">Version {{ appVersion }}</text>
      <text class="about-desc">无人机地形数据采集与土质分析平台 · 为农业、水利、灾害监测提供精准测绘解决方案</text>
    </view>

    <!-- 退出登录 -->
    <zrws-button variant="danger" size="lg" block @click="doLogout">退出登录</zrws-button>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { store, logout as storeLogout } from '@/store/user.js'
  import { loginApi, dashboardApi } from '@/api/index.js'
  import { toast, nav, confirm } from '@/utils/index.js'

  const user = reactive({
    name: '',
    role: '',
    department: '',
    phone: ''
  })

  const stats = reactive({
    missions: 0,
    flightHours: 0,
    samples: 0,
    area: '0亩'
  })

  const loading = ref(false)

  const taskMenus = [
    { icon: '📝', name: '我的任务', url: '/pages/mission/list', badge: '' },
    { icon: '✈️', name: '飞行记录', url: '/pages/flight/control', badge: '' },
    { icon: '📍', name: 'GPS轨迹库', url: '/pages/gps/track', badge: '' },
    { icon: '🌱', name: '土壤采样库', url: '/pages/soil/sample', badge: '' },
    { icon: '📐', name: '地块面积测量', url: '/pages/area/calc', badge: '' },
    { icon: '✅', name: '审批记录', url: '/pages/approval/list', badge: '' }
  ]

  const mobileMenus = [
    { icon: '🗺️', name: '野外采集', url: '/pages/collection/field', badge: '' },
    { icon: '📡', name: '离线同步', url: '/pages/offline/sync', badge: '' },
    { icon: '📥', name: '数据导入', url: '/pages/data/import', badge: '' },
    { icon: '📤', name: '数据导出', url: '/pages/data/export', badge: '' }
  ]

  const settingMenus = [
    { icon: '🔔', name: '消息通知', url: 'notify' },
    { icon: '🌍', name: '坐标系统设置', url: 'coordinate' },
    { icon: '💾', name: '数据缓存管理', url: 'cache' },
    { icon: '📱', name: '离线地图下载', url: 'offline' },
    { icon: '🔐', name: '修改密码', url: 'password' },
    { icon: '📄', name: '用户协议与隐私', url: 'privacy' },
    { icon: '❓', name: '帮助与反馈', url: 'help' },
    { icon: 'ℹ️', name: '关于智壤卫士', url: 'about' }
  ]

  const appVersion = typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : '1.0.0'

  onMounted(() => {
    loadUserInfo()
    loadStats()
  })

  async function loadUserInfo() {
    loading.value = true
    try {
      const res = await loginApi.getUserInfo()
      if (res) {
        user.name = res.name || res.username || store.user.name
        user.role = res.role || store.user.role
        user.department = res.department || store.user.department
        user.phone = res.phone || store.user.phone
      }
    } catch (e) {
      // 从本地存储读取
      try {
        const saved = uni.getStorageSync('userInfo')
        if (saved && saved.name) {
          user.name = saved.name || user.name
          user.role = saved.role || user.role
          user.department = saved.department || user.department
          user.phone = saved.phone || user.phone
        }
      } catch (e2) {}
    } finally {
      loading.value = false
    }
  }

  async function loadStats() {
    try {
      const res = await dashboardApi.stats()
      if (res) {
        stats.missions = res.missionTotal || res.totalMissions || 0
        stats.flightHours = res.flightHours || 0
        stats.samples = res.soilSamples || res.totalSamples || 0
        stats.area = (res.totalArea || res.area || 0) + '亩'
      }
      // 更新审批badge
      const pendingApproval = stats.pendingApproval || 0
      if (pendingApproval > 0) {
        taskMenus[5].badge = String(pendingApproval)
      }
    } catch (e) {
      // 错误提示已在 request 封装中处理
    }
  }

  function goTo(url) {
    if (!url) return
    uni.navigateTo({
      url,
      fail: () => {
        uni.redirectTo({ url })
      }
    })
  }

  function doSetting(m) {
    const msgMap = {
      notify: '消息通知设置',
      coordinate: 'WGS84 / CGCS2000 / 自定义',
      cache: '当前缓存: 45.2 MB',
      offline: '离线地图下载',
      password: '修改密码',
      privacy: '用户协议与隐私政策',
      help: '联系客服: 400-xxx-xxxx',
      about: '智壤卫士 v' + appVersion
    }
    uni.showModal({
      title: m.name,
      content: msgMap[m.url] || '功能开发中',
      showCancel: false
    })
  }

  async function doLogout() {
    uni.showModal({
      title: '退出登录',
      content: '确定退出当前账号？',
      confirmText: '退出',
      confirmColor: '#f56c6c',
      success: async (res) => {
        if (res.confirm) {
          try {
            await loginApi.logout()
          } catch (e) {
            // 忽略登出API错误，继续本地登出
          }
          storeLogout()
          toast.success('已退出登录')
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 500)
        }
      }
    })
  }
</script>

<style lang="scss" scoped>
  .user-card {
    background: linear-gradient(135deg, #C9A96E 0%, #A88B4F 100%);
    border-radius: $zrws-radius-lg;
    padding: 40rpx 32rpx;
    margin-bottom: $zrws-space-md;
    display: flex;
    align-items: center;
    color: #fff;
    box-shadow: $zrws-shadow-md;
  }
  .uc-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    font-weight: 600;
    margin-right: $zrws-space-md;
    flex-shrink: 0;
    border: 4rpx solid rgba(255,255,255,0.4);
  }
  .uc-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
  .uc-name { font-size: 36rpx; font-weight: 700; }
  .uc-role { font-size: 24rpx; opacity: 0.9; }
  .uc-phone { font-size: 22rpx; opacity: 0.8; }
  .uc-badge {
    background: rgba(255,255,255,0.3);
    padding: 8rpx 20rpx;
    border-radius: $zrws-radius-md;
    margin-left: 10rpx;
  }
  .badge-text { font-size: 22rpx; font-weight: 600; }

  .stats-block {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx 8rpx;
    margin-bottom: $zrws-space-md;
    box-shadow: $zrws-shadow-sm;
  }
  .sb-item { text-align: center; padding: 0 4rpx; }
  .sb-num {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: $zrws-primary;
    font-family: 'DIN', monospace;
    margin-bottom: 6rpx;
  }
  .sb-label { font-size: 22rpx; color: $zrws-text-tertiary; }

  .menu-section { margin-bottom: $zrws-space-md; }
  .menu-title {
    display: block;
    padding: 0 4rpx 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .menu-list {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    overflow: hidden;
    box-shadow: $zrws-shadow-sm;
  }
  .menu-item {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
    border-bottom: 1rpx solid $zrws-border-light;
  }
  .menu-item:last-child { border-bottom: none; }
  .mi-icon { font-size: 32rpx; margin-right: 20rpx; width: 56rpx; text-align: center; }
  .mi-name { flex: 1; font-size: 28rpx; color: $zrws-text-primary; }
  .mi-badge {
    background: $zrws-error;
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 14rpx;
    border-radius: 16rpx;
    margin-right: 12rpx;
    font-weight: 600;
  }
  .mi-arrow { font-size: 36rpx; color: $zrws-text-tertiary; font-weight: 300; }

  .about-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 32rpx;
    text-align: center;
    margin-bottom: 32rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .about-title {
    display: block;
    font-size: 32rpx;
    font-weight: 700;
    color: $zrws-text-primary;
    margin-bottom: 10rpx;
  }
  .about-version {
    display: block;
    font-size: 22rpx;
    color: $zrws-text-tertiary;
    margin-bottom: 20rpx;
  }
  .about-desc {
    display: block;
    font-size: 24rpx;
    color: $zrws-text-secondary;
    line-height: 1.7;
  }
</style>
