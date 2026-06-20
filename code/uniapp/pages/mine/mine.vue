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
      <text class="about-version">Version 1.0.0 (Build 20260618)</text>
      <text class="about-desc">无人机地形数据采集与土质分析平台 · 为农业、水利、灾害监测提供精准测绘解决方案</text>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn" @tap="doLogout">退出登录</button>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { toast, nav, confirm } from '@/utils/index.js'

  const user = reactive({
    name: '张工程师',
    role: '外业操作员',
    department: '测绘事业部',
    phone: '138****8888'
  })

  const stats = reactive({
    missions: 128,
    flightHours: 425.5,
    samples: 864,
    area: '86.4亩'
  })

  const taskMenus = [
    { icon: '📝', name: '我的任务', url: '/pages/mission/list', badge: '' },
    { icon: '✈️', name: '飞行记录', url: '/pages/flight/control', badge: '' },
    { icon: '📍', name: 'GPS轨迹库', url: '/pages/gps/track', badge: '' },
    { icon: '🌱', name: '土壤采样库', url: '/pages/soil/sample', badge: '' },
    { icon: '📐', name: '地块面积测量', url: '/pages/area/calc', badge: '' },
    { icon: '✅', name: '审批记录', url: '/pages/approval/list', badge: '5' }
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

  onMounted(() => {
    // 同步登录用户信息
    try {
      const saved = uni.getStorageSync('userInfo')
      if (saved && saved.name) {
        user.name = saved.name || user.name
        user.role = saved.role || user.role
        user.department = saved.department || user.department
      }
    } catch (e) {}
  })

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
      about: '智壤卫士 v1.0.0'
    }
    uni.showModal({
      title: m.name,
      content: msgMap[m.url] || '功能开发中',
      showCancel: false
    })
  }

  function doLogout() {
    uni.showModal({
      title: '退出登录',
      content: '确定退出当前账号？',
      confirmText: '退出',
      confirmColor: '#f56c6c',
      success: (res) => {
        if (res.confirm) {
          try {
            uni.removeStorageSync('userInfo')
            uni.removeStorageSync('token')
          } catch (e) {}
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
    background: linear-gradient(135deg, #1e3a5f 0%, #2b6cb0 100%);
    border-radius: 20rpx;
    padding: 40rpx 32rpx;
    margin-bottom: 24rpx;
    display: flex;
    align-items: center;
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(30,58,95,0.25);
  }
  .uc-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    font-weight: 600;
    margin-right: 24rpx;
    flex-shrink: 0;
    border: 4rpx solid rgba(255,255,255,0.4);
  }
  .uc-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
  .uc-name { font-size: 36rpx; font-weight: 700; }
  .uc-role { font-size: 24rpx; opacity: 0.9; }
  .uc-phone { font-size: 22rpx; opacity: 0.75; }
  .uc-badge {
    background: rgba(255,255,255,0.25);
    padding: 8rpx 20rpx;
    border-radius: 16rpx;
    margin-left: 10rpx;
  }
  .badge-text { font-size: 22rpx; font-weight: 600; }

  .stats-block {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx 8rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .sb-item { text-align: center; padding: 0 4rpx; }
  .sb-num {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: #2b6cb0;
    font-family: 'DIN', monospace;
    margin-bottom: 6rpx;
  }
  .sb-label { font-size: 22rpx; color: #606266; }

  .menu-section { margin-bottom: 24rpx; }
  .menu-title {
    display: block;
    padding: 0 4rpx 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .menu-list {
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .menu-item {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }
  .menu-item:last-child { border-bottom: none; }
  .mi-icon { font-size: 32rpx; margin-right: 20rpx; width: 56rpx; text-align: center; }
  .mi-name { flex: 1; font-size: 28rpx; color: #303133; }
  .mi-badge {
    background: #f56c6c;
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 14rpx;
    border-radius: 16rpx;
    margin-right: 12rpx;
    font-weight: 600;
  }
  .mi-arrow { font-size: 36rpx; color: #c0c4cc; font-weight: 300; }

  .about-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 32rpx;
    text-align: center;
    margin-bottom: 32rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .about-title {
    display: block;
    font-size: 32rpx;
    font-weight: 700;
    color: #1e3a5f;
    margin-bottom: 10rpx;
  }
  .about-version {
    display: block;
    font-size: 22rpx;
    color: #909399;
    margin-bottom: 20rpx;
  }
  .about-desc {
    display: block;
    font-size: 24rpx;
    color: #606266;
    line-height: 1.7;
  }

  .logout-btn {
    width: 100%;
    height: 90rpx;
    background: #fff;
    color: #f56c6c;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: 16rpx;
    border: 2rpx solid #f56c6c;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
