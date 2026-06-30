<template>
  <app-page-layout title="质量校验" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="stats-row">
      <zrws-stat-card
        label="总任务"
        :value="stats.total"
        icon="📋"
        color="blue" />
      <zrws-stat-card
        label="已完成"
        :value="stats.completed"
        icon="✅"
        color="green" />
      <zrws-stat-card
        label="合格率"
        :value="stats.passRate + '%'"
        icon="📊"
        color="orange" />
    </view>

    <view class="filter-bar">
      <text
        v-for="t in statusTabs"
        :key="t.value"
        :class="['filter-item', {active: activeStatus === t.value}]"
        @tap="activeStatus = t.value; loadData()">
        {{ t.label }}
      </text>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty-wrap">
      <zrws-empty text="暂无校验任务" icon="🔍"></zrws-empty>
    </view>

    <view v-for="(item, index) in list" :key="index" class="check-card" @tap="goDetail(item)">
      <view class="card-header">
        <text class="task-name">{{ item.name }}</text>
        <text :class="['result-tag', item.result]">
          {{ getResultText(item.result) }}
        </text>
      </view>
      <view class="card-body">
        <view class="info-row">
          <text class="info-label">任务类型</text>
          <text class="info-value">{{ item.type }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">任务状态</text>
          <text :class="['status-text', item.status]">{{ getStatusText(item.status) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">完成时间</text>
          <text class="info-value">{{ item.completeTime || item.createTime }}</text>
        </view>
      </view>
      <view class="card-footer">
        <text class="checker">校验人：{{ item.checker }}</text>
        <text class="detail-link">查看详情 ›</text>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { qualityCheckApi } from '@/api/index.js'
  import { nav } from '@/utils/index.js'

  const statusTabs = [
    { label: '全部', value: '' },
    { label: '待校验', value: 'pending' },
    { label: '校验中', value: 'processing' },
    { label: '已完成', value: 'completed' }
  ]

  const activeStatus = ref('')
  const list = ref([])
  const loading = ref(false)

  const stats = reactive({
    total: 0,
    completed: 0,
    passRate: 0
  })

  onMounted(() => { loadData() })

  async function loadData() {
    loading.value = true
    try {
      const [listRes, statsRes] = await Promise.all([
        qualityCheckApi.list({ status: activeStatus.value || undefined }).catch(() => null),
        qualityCheckApi.stats().catch(() => null)
      ])

      if (listRes) {
        list.value = listRes.list || listRes.records || listRes || []
      }

      if (statsRes) {
        stats.total = statsRes.total || 0
        stats.completed = statsRes.completed || 0
        stats.passRate = statsRes.passRate || 0
      }
    } catch (e) {
    } finally {
      loading.value = false
    }
  }

  function getResultText(result) {
    const map = {
      pass: '合格',
      fail: '不合格',
      pending: '待判定'
    }
    return map[result] || result
  }

  function getStatusText(status) {
    const map = {
      pending: '待校验',
      processing: '校验中',
      completed: '已完成'
    }
    return map[status] || status
  }

  function goDetail(item) {
    nav.to('/pages/quality/detail?id=' + item.id)
  }
</script>

<style lang="scss" scoped>
  .stats-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
    margin-bottom: 24rpx;
  }

  .filter-bar {
    display: flex;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-lg;
    padding: 8rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .filter-item {
    flex: 1;
    text-align: center;
    padding: 18rpx 0;
    font-size: 26rpx;
    color: $zrws-text-secondary;
    border-radius: $zrws-radius-md;
    transition: all 0.3s ease;
  }
  .filter-item.active {
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4rpx 12rpx rgba(201, 169, 110, 0.3);
  }

  .check-card {
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .check-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .task-name {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
    flex: 1;
    padding-right: 16rpx;
  }
  .result-tag {
    padding: 8rpx 20rpx;
    border-radius: 16rpx;
    font-size: 24rpx;
    font-weight: 600;
    flex-shrink: 0;
  }
  .result-tag.pass {
    background: rgba(124, 179, 66, 0.15);
    color: $zrws-success;
  }
  .result-tag.fail {
    background: rgba(229, 57, 53, 0.15);
    color: $zrws-error;
  }
  .result-tag.pending {
    background: rgba(245, 124, 0, 0.15);
    color: $zrws-warning;
  }

  .card-body {
    margin-bottom: 20rpx;
  }
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10rpx 0;
  }
  .info-label {
    font-size: 26rpx;
    color: $zrws-text-secondary;
  }
  .info-value {
    font-size: 26rpx;
    color: $zrws-text-primary;
  }
  .status-text {
    font-size: 26rpx;
    font-weight: 500;
  }
  .status-text.pending {
    color: $zrws-warning;
  }
  .status-text.processing {
    color: $zrws-info;
  }
  .status-text.completed {
    color: $zrws-success;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid $zrws-border-light;
  }
  .checker {
    font-size: 24rpx;
    color: $zrws-text-secondary;
  }
  .detail-link {
    font-size: 24rpx;
    color: $zrws-primary;
  }

  .empty-wrap {
    margin-top: 80rpx;
  }
</style>
