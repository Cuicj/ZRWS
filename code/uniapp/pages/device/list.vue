<template>
  <app-page-layout title="设备管理" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="tab-bar">
      <text
        v-for="t in tabs"
        :key="t.value"
        :class="['tab-item', {active: activeTab === t.value}]"
        @tap="activeTab = t.value; loadData()">
        {{ t.label }}
        <text v-if="t.value" class="tab-count">({{ getCount(t.value) }})</text>
      </text>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty-wrap">
      <zrws-empty text="暂无设备数据" icon="📱"></zrws-empty>
    </view>

    <view v-for="(item, index) in list" :key="index" class="device-card" @tap="goDetail(item)">
      <view class="card-left">
        <view class="device-icon">
          <text>{{ getDeviceIcon(item.type) }}</text>
        </view>
      </view>
      <view class="card-right">
        <view class="device-header">
          <text class="device-name">{{ item.name }}</text>
          <text :class="['status-tag', item.status]">
            {{ getStatusText(item.status) }}
          </text>
        </view>
        <view class="device-info">
          <view class="info-item">
            <text class="info-label">型号</text>
            <text class="info-value">{{ item.model }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">电量</text>
            <view class="battery-wrap">
              <view class="battery-bar">
                <view class="battery-level" :class="getBatteryClass(item.battery)" :style="{ width: item.battery + '%' }"></view>
              </view>
              <text class="battery-text">{{ item.battery }}%</text>
            </view>
          </view>
        </view>
        <view class="device-location">
          <text class="location-icon">📍</text>
          <text class="location-text">{{ item.location }}</text>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { deviceApi } from '@/api/index.js'
  import { nav } from '@/utils/index.js'

  const tabs = [
    { label: '全部', value: '' },
    { label: '在线', value: 'online' },
    { label: '离线', value: 'offline' },
    { label: '故障', value: 'fault' }
  ]

  const activeTab = ref('')
  const list = ref([])
  const allList = ref([])
  const loading = ref(false)

  const stats = reactive({
    total: 0,
    online: 0,
    offline: 0,
    fault: 0
  })

  onMounted(() => { loadData() })

  async function loadData() {
    loading.value = true
    try {
      const [listRes, statsRes] = await Promise.all([
        deviceApi.list({ status: activeTab.value || undefined }).catch(() => null),
        deviceApi.stats().catch(() => null)
      ])

      if (listRes) {
        list.value = listRes.list || listRes.records || listRes || []
        allList.value = list.value
      }

      if (statsRes) {
        stats.total = statsRes.total || 0
        stats.online = statsRes.online || 0
        stats.offline = statsRes.offline || 0
        stats.fault = statsRes.fault || 0
      }
    } catch (e) {
    } finally {
      loading.value = false
    }
  }

  function getCount(status) {
    if (status === 'online') return stats.online
    if (status === 'offline') return stats.offline
    if (status === 'fault') return stats.fault
    return stats.total
  }

  function getDeviceIcon(type) {
    const iconMap = {
      drone: '✈️',
      sensor: '📡',
      camera: '📷',
      gps: '📍'
    }
    return iconMap[type] || '📱'
  }

  function getStatusText(status) {
    const map = {
      online: '在线',
      offline: '离线',
      fault: '故障'
    }
    return map[status] || status
  }

  function getBatteryClass(battery) {
    if (battery > 60) return 'high'
    if (battery > 20) return 'medium'
    return 'low'
  }

  function goDetail(item) {
    nav.to('/pages/device/detail?id=' + item.id)
  }
</script>

<style lang="scss" scoped>
  .tab-bar {
    display: flex;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-lg;
    padding: 8rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    font-size: 24rpx;
    color: $zrws-text-secondary;
    border-radius: $zrws-radius-md;
    transition: all 0.3s ease;
  }
  .tab-item.active {
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4rpx 12rpx rgba(201, 169, 110, 0.3);
  }
  .tab-count {
    font-size: 22rpx;
    opacity: 0.8;
  }

  .device-card {
    display: flex;
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .device-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }

  .card-left {
    margin-right: 20rpx;
  }
  .device-icon {
    width: 96rpx;
    height: 96rpx;
    background: $zrws-primary-bg;
    border-radius: $zrws-radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
  }

  .card-right {
    flex: 1;
    min-width: 0;
  }

  .device-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14rpx;
  }
  .device-name {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .status-tag {
    padding: 6rpx 16rpx;
    border-radius: 12rpx;
    font-size: 22rpx;
    font-weight: 500;
  }
  .status-tag.online {
    background: rgba(124, 179, 66, 0.15);
    color: $zrws-success;
  }
  .status-tag.offline {
    background: rgba(181, 168, 150, 0.2);
    color: $zrws-text-tertiary;
  }
  .status-tag.fault {
    background: rgba(229, 57, 53, 0.15);
    color: $zrws-error;
  }

  .device-info {
    display: flex;
    gap: 24rpx;
    margin-bottom: 14rpx;
  }
  .info-item {
    flex: 1;
  }
  .info-label {
    font-size: 22rpx;
    color: $zrws-text-secondary;
    display: block;
    margin-bottom: 6rpx;
  }
  .info-value {
    font-size: 24rpx;
    color: $zrws-text-primary;
  }

  .battery-wrap {
    display: flex;
    align-items: center;
    gap: 10rpx;
  }
  .battery-bar {
    flex: 1;
    height: 16rpx;
    background: $zrws-border-light;
    border-radius: 8rpx;
    overflow: hidden;
  }
  .battery-level {
    height: 100%;
    border-radius: 8rpx;
    transition: width 0.3s ease;
  }
  .battery-level.high {
    background: $zrws-success;
  }
  .battery-level.medium {
    background: $zrws-warning;
  }
  .battery-level.low {
    background: $zrws-error;
  }
  .battery-text {
    font-size: 22rpx;
    color: $zrws-text-secondary;
    min-width: 60rpx;
  }

  .device-location {
    display: flex;
    align-items: center;
    padding-top: 12rpx;
    border-top: 1rpx solid $zrws-border-light;
  }
  .location-icon {
    font-size: 24rpx;
    margin-right: 8rpx;
  }
  .location-text {
    font-size: 24rpx;
    color: $zrws-text-secondary;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .empty-wrap {
    margin-top: 100rpx;
  }
</style>
