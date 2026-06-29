<template>
  <app-page-layout title="离线同步" :showBack="true">
    <view class="sync-page">
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-num">{{ stats.pending }}</text>
          <text class="stats-label">待同步</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-num success">{{ stats.success }}</text>
          <text class="stats-label">已成功</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-num failed">{{ stats.failed }}</text>
          <text class="stats-label">失败</text>
        </view>
      </view>

      <view class="tab-bar">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'pending' }"
          @tap="activeTab = 'pending'">
          <text>待同步</text>
          <view v-if="stats.pending > 0" class="tab-badge">{{ stats.pending }}</view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'synced' }"
          @tap="activeTab = 'synced'">
          <text>已同步</text>
        </view>
      </view>

      <view v-if="syncStatus === 'syncing'" class="sync-progress-card">
        <view class="progress-header">
          <text class="progress-title">同步中...</text>
          <text class="progress-percent">{{ syncProgress }}%</text>
        </view>
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: syncProgress + '%' }"></view>
        </view>
        <view class="progress-detail">
          <text>{{ syncResult.success }} 成功 / {{ syncResult.failed }} 失败</text>
        </view>
      </view>

      <view v-if="activeTab === 'pending'" class="list-section">
        <view v-if="pendingItems.length === 0" class="empty-state">
          <text class="empty-icon">📦</text>
          <text class="empty-text">暂无待同步数据</text>
        </view>
        <view v-else class="sync-list">
          <view v-for="(item, i) in pendingItems" :key="i" class="sync-item">
            <view class="item-icon">
              {{ getTypeIcon(item.type) }}
            </view>
            <view class="item-info">
              <text class="item-title">{{ getItemTitle(item) }}</text>
              <text class="item-time">{{ formatTime(item.createdAt) }}</text>
              <text v-if="item.status === 'failed'" class="item-error">{{ item.errorMsg }}</text>
            </view>
            <view class="item-status" :class="item.status">
              {{ getStatusText(item.status) }}
            </view>
            <view class="item-actions">
              <view v-if="item.status === 'failed'" class="action-btn retry" @tap="retryOne(item)">
                重试
              </view>
              <view class="action-btn delete" @tap="deleteItem(item)">
                删除
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="activeTab === 'synced'" class="list-section">
        <view v-if="syncedItems.length === 0" class="empty-state">
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无已同步记录</text>
        </view>
        <view v-else class="sync-list">
          <view v-for="(item, i) in syncedItems" :key="i" class="sync-item">
            <view class="item-icon">
              {{ getTypeIcon(item.type) }}
            </view>
            <view class="item-info">
              <text class="item-title">{{ getItemTitle(item) }}</text>
              <text class="item-time">{{ formatTime(item.syncedAt || item.createdAt) }}</text>
            </view>
            <view class="item-status success">
              已同步
            </view>
          </view>
        </view>
      </view>

      <view style="height: 160rpx;"></view>
    </view>

    <view class="bottom-bar">
      <button
        v-if="activeTab === 'pending'"
        class="btn btn-outline"
        :disabled="stats.failed === 0"
        @tap="clearFailed">
        <text>清除失败</text>
      </button>
      <button
        class="btn btn-primary"
        :disabled="syncStatus === 'syncing' || stats.pending === 0"
        @tap="startSyncAll">
        <text>{{ syncStatus === 'syncing' ? '同步中...' : '一键同步' }}</text>
      </button>
    </view>
  </app-page-layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toast, confirm, formatDate } from '@/utils/index.js'
import { useOfflineStore } from '@/store/offline.js'

const offlineStore = useOfflineStore()
const activeTab = ref('pending')

const stats = computed(() => offlineStore.stats)
const syncStatus = computed(() => offlineStore.syncStatus)
const syncProgress = computed(() => offlineStore.syncProgress)
const syncResult = computed(() => offlineStore.syncResult)

const pendingItems = computed(() =>
  offlineStore.pendingList.filter(item => item.status !== 'success')
)

const syncedItems = computed(() =>
  offlineStore.pendingList.filter(item => item.status === 'success')
)

onMounted(() => {
  offlineStore.loadPending()
})

function getTypeIcon(type) {
  const map = {
    'field_collection': '🌱',
    'soil_sample': '🌱',
    'rock_sample': '🪨',
    'disaster_record': '⚠️',
    'device_inspection': '🔧',
    'soil': '🌱',
    'rock': '🪨',
    'disaster': '⚠️',
    'device': '🔧'
  }
  return map[type] || '📋'
}

function getItemTitle(item) {
  if (item.data && item.data.name) {
    return item.data.name
  }
  const typeMap = {
    'field_collection': '野外采集',
    'soil_sample': '土壤采样',
    'rock_sample': '岩石采样',
    'disaster_record': '灾害记录',
    'device_inspection': '设备巡检'
  }
  return typeMap[item.type] || '数据记录'
}

function getStatusText(status) {
  const map = {
    'pending': '待同步',
    'syncing': '同步中',
    'failed': '失败',
    'success': '已成功'
  }
  return map[status] || status
}

function formatTime(timestamp) {
  if (!timestamp) return '--'
  return formatDate(timestamp, 'MM-DD HH:mm')
}

async function startSyncAll() {
  if (pendingItems.value.length === 0) {
    toast.info('暂无待同步数据')
    return
  }

  try {
    const result = await offlineStore.syncAll()
    if (result.failed > 0) {
      toast.error(`同步完成，${result.failed} 条失败`)
    } else {
      toast.success('全部同步成功')
    }
  } catch (e) {
    toast.error('同步失败')
  }
}

async function retryOne(item) {
  toast.loading('重试中...')
  try {
    const result = await offlineStore.syncOne(item.syncId)
    toast.hide()
    if (result && result.success) {
      toast.success('同步成功')
    } else {
      toast.error('同步失败')
    }
  } catch (e) {
    toast.hide()
    toast.error('同步失败')
  }
}

async function deleteItem(item) {
  try {
    await confirm('提示', '确定删除这条待同步数据？')
    offlineStore.removeItem(item.syncId)
    toast.success('已删除')
  } catch (e) {
    // 用户取消
  }
}

async function clearFailed() {
  try {
    await confirm('提示', '确定清除所有失败的同步记录？')
    offlineStore.clearFailed()
    toast.success('已清除')
  } catch (e) {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.sync-page {
  padding-bottom: 20rpx;
}

.stats-card {
  background: linear-gradient(145deg, #FAFAF8, #F5F2ED);
  border-radius: 24rpx;
  padding: 32rpx 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(93, 78, 55, 0.06);
}

.stats-item {
  flex: 1;
  text-align: center;
}

.stats-num {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #5D4E37;
  margin-bottom: 8rpx;
  font-family: monospace;

  &.success {
    color: #7CB342;
  }

  &.failed {
    color: #E53935;
  }
}

.stats-label {
  font-size: 24rpx;
  color: #8B7355;
}

.stats-divider {
  width: 2rpx;
  height: 60rpx;
  background: #E8E2D9;
}

.tab-bar {
  display: flex;
  background: rgba(250, 250, 248, 0.8);
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #8B7355;
  border-radius: 16rpx;
  position: relative;
  transition: all 0.3s ease;

  &.active {
    background: #fff;
    color: #5D4E37;
    font-weight: 600;
    box-shadow: 0 2rpx 8rpx rgba(93, 78, 55, 0.08);
  }
}

.tab-badge {
  position: absolute;
  top: 6rpx;
  right: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  background: #E53935;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  padding: 0 8rpx;
  text-align: center;
}

.sync-progress-card {
  background: linear-gradient(135deg, #F5EDE0, #EDE4D3);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid #D9C49A;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.progress-title {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 600;
}

.progress-percent {
  font-size: 28rpx;
  color: #C9A96E;
  font-weight: 700;
}

.progress-bar {
  height: 16rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #D9C49A, #C9A96E);
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

.progress-detail {
  font-size: 22rpx;
  color: #8B7355;
  text-align: right;
}

.list-section {
  min-height: 400rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon {
  display: block;
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #B5A896;
}

.sync-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.sync-item {
  background: linear-gradient(145deg, #FAFAF8, #F5F2ED);
  border-radius: 20rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(93, 78, 55, 0.04);
}

.item-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-title {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 22rpx;
  color: #B5A896;
}

.item-error {
  font-size: 22rpx;
  color: #E53935;
  margin-top: 4rpx;
}

.item-status {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  margin-left: 12rpx;
  flex-shrink: 0;

  &.pending {
    background: rgba(201, 169, 110, 0.15);
    color: #C9A96E;
  }

  &.syncing {
    background: rgba(93, 156, 167, 0.15);
    color: #5D9CA7;
  }

  &.failed {
    background: rgba(229, 57, 53, 0.15);
    color: #E53935;
  }

  &.success {
    background: rgba(124, 179, 66, 0.15);
    color: #7CB342;
  }
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-left: 12rpx;
}

.action-btn {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  text-align: center;

  &.retry {
    background: rgba(201, 169, 110, 0.15);
    color: #C9A96E;
  }

  &.delete {
    background: rgba(229, 57, 53, 0.1);
    color: #E53935;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FEFBF6;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -4rpx 16rpx rgba(93, 78, 55, 0.08);
  z-index: 100;
}

.btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  line-height: 1;

  &:disabled {
    opacity: 0.5;
  }
}

.btn-outline {
  flex: 0 0 30%;
  background: transparent;
  color: #C9A96E;
  border: 2rpx solid #C9A96E;
}

.btn-primary {
  flex: 1;
  background: linear-gradient(135deg, #D9C49A 0%, #C9A96E 100%);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(201, 169, 110, 0.35);
}
</style>
