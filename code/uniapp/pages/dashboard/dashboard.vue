<template>
  <app-page-layout title="智壤卫士" :showTabbar="true" :refresherEnabled="true" @refresh="loadData">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar">{{ user.name.charAt(0) }}</view>
      <view class="user-info">
        <text class="user-name">{{ user.name }}</text>
        <text class="user-role">{{ user.role }} · {{ todayText }}</text>
      </view>
      <view class="user-bell">🔔<view v-if="stats.pendingApproval > 0" class="badge-dot">{{ stats.pendingApproval }}</view></view>
    </view>

    <!-- 数据统计 -->
    <view class="stats-row">
      <zrws-stat-card
        label="总任务数"
        :value="stats.missionTotal"
        :trend="'↑ 今日新增 ' + stats.missionToday"
        icon="📋" color="blue"
        @tap="gotoMission" />

      <zrws-stat-card
        label="待审批"
        :value="stats.pendingApproval"
        trend="⚠ 请及时处理"
        icon="✅" color="red"
        @tap="gotoApproval" />
    </view>

    <view class="stats-row">
      <zrws-stat-card
        label="采样数据"
        :value="stats.soilSamples"
        :trend="'↑ 今日 ' + stats.soilToday"
        icon="🌱" color="green"
        @tap="gotoSoil" />

      <zrws-stat-card
        label="在线设备"
        :value="stats.droneOnline"
        trend="● 实时监控"
        icon="✈️" color="orange"
        @tap="gotoFlight" />
    </view>

    <!-- 快捷入口 -->
    <zrws-section title="快捷操作">
      <zrws-quick-entry :items="quickItems" @tap="onQuickTap"></zrws-quick-entry>
    </zrws-section>

    <!-- 最近任务 -->
    <view class="section-title">
      <text>最近任务</text>
      <text class="st-more" @tap="gotoMission">查看全部 ›</text>
    </view>
    <view v-if="missions.length === 0" class="empty-state">暂无任务数据</view>
    <zrws-mission-card
      v-for="(m, i) in missions.slice(0, 5)"
      :key="i"
      :mission="m"
      @tap="openMission(m)">
    </zrws-mission-card>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { store, checkLogin } from '@/store/user.js'
  import { mockMissions } from '@/utils/mock.js'
  import { toast, nav } from '@/utils/index.js'

  const user = computed(() => store.user)
  const stats = reactive({
    missionTotal: 128,
    missionToday: 3,
    pendingApproval: 5,
    soilSamples: 86,
    soilToday: 12,
    droneOnline: 2
  })

  const missions = ref([])
  const todayText = computed(() => {
    const d = new Date()
    const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return d.getFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate() + ' ' + weeks[d.getDay()]
  })

  const quickItems = [
    { key: 'new-mission', icon: '📝', label: '新建任务', color: 'blue' },
    { key: 'flight', icon: '✈️', label: '飞行控制', color: 'green' },
    { key: 'soil', icon: '🌱', label: '土壤采样', color: 'orange' },
    { key: 'area', icon: '📐', label: '面积测量', color: 'purple' },
    { key: 'gps', icon: '📍', label: 'GPS航迹', color: 'cyan' },
    { key: 'disaster', icon: '⚠️', label: '灾害评估', color: 'yellow' },
    { key: 'approval', icon: '✅', label: '审批中心', color: 'pink' },
    { key: 'mine', icon: '👤', label: '我的', color: 'gray' }
  ]

  onMounted(() => {
    // 未登录则跳转到登录
    if (!checkLogin() && !store.user.isLogin) {
      store.user.name = '张工程师'
      store.user.role = '外业操作员'
      store.user.department = '测绘事业部'
      store.user.isLogin = true
    }
    loadData()
  })

  function loadData() {
    // 加载 mock 数据（后端对接后替换为 API 调用）
    setTimeout(() => {
      missions.value = [...mockMissions]
    }, 300)
  }

  function onQuickTap(item) {
    const urlMap = {
      'new-mission': '/pages/mission/list',
      'flight': '/pages/flight/control',
      'soil': '/pages/soil/sample',
      'area': '/pages/area/calc',
      'gps': '/pages/gps/track',
      'disaster': '/pages/disaster/risk',
      'approval': '/pages/approval/list',
      'mine': '/pages/mine/mine'
    }
    if (urlMap[item.key]) {
      nav.to(urlMap[item.key])
    } else {
      toast.info('功能开发中')
    }
  }

  function openMission(m) {
    uni.setStorageSync('currentMission', m)
    nav.to('/pages/mission/detail?id=' + m.id)
  }

  function gotoMission() { nav.to('/pages/mission/list') }
  function gotoApproval() { nav.to('/pages/approval/list') }
  function gotoSoil() { nav.to('/pages/soil/sample') }
  function gotoFlight() { nav.to('/pages/flight/control') }
</script>

<style lang="scss" scoped>
  .user-card {
    background: linear-gradient(135deg, #1e3a5f 0%, #2b6cb0 100%);
    border-radius: 20rpx;
    padding: 36rpx 32rpx;
    margin-bottom: 24rpx;
    display: flex;
    align-items: center;
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(30,58,95,0.2);
  }
  .user-avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    font-weight: 600;
    margin-right: 24rpx;
    flex-shrink: 0;
  }
  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .user-name {
    font-size: 34rpx;
    font-weight: 600;
    margin-bottom: 8rpx;
  }
  .user-role {
    font-size: 24rpx;
    opacity: 0.85;
  }
  .user-bell {
    font-size: 40rpx;
    position: relative;
    padding: 0 10rpx;
  }
  .badge-dot {
    position: absolute;
    top: -6rpx;
    right: 0;
    min-width: 36rpx;
    height: 36rpx;
    line-height: 36rpx;
    background: #f56c6c;
    color: #fff;
    font-size: 20rpx;
    border-radius: 18rpx;
    text-align: center;
    padding: 0 8rpx;
  }

  .stats-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20rpx;
    margin-bottom: 10rpx;
  }

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 4rpx 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
  }
  .st-more {
    font-size: 24rpx;
    color: #2b6cb0;
    font-weight: normal;
  }

  .empty-state {
    text-align: center;
    padding: 80rpx 0;
    color: #c0c4cc;
    font-size: 26rpx;
    background: #fff;
    border-radius: 16rpx;
  }
</style>
