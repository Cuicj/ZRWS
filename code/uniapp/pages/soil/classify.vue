<template>
  <app-page-layout title="土质分类" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="search-box">
      <text class="search-icon">🔍</text>
      <input
        class="search-input"
        v-model="keyword"
        placeholder="搜索土壤名称/编号"
        confirm-type="search"
        @confirm="loadData" />
    </view>

    <view class="tab-bar">
      <text
        v-for="t in tabs"
        :key="t.value"
        :class="['tab-item', {active: activeTab === t.value}]"
        @tap="activeTab = t.value; loadData()">
        {{ t.label }}
      </text>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty-wrap">
      <zrws-empty text="暂无分类数据" icon="🏜️"></zrws-empty>
    </view>

    <view v-for="(item, index) in list" :key="index" class="classify-card" @tap="goDetail(item)">
      <view class="card-header">
        <view class="soil-name">{{ item.name }}</view>
        <view class="confidence" :class="getConfidenceClass(item.confidence)">
          {{ item.confidence }}%
        </view>
      </view>
      <view class="card-body">
        <view class="info-row">
          <text class="info-label">分类编号</text>
          <text class="info-value">{{ item.code }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">主要成分</text>
          <text class="info-value">{{ item.components }}</text>
        </view>
      </view>
      <view class="card-footer">
        <text class="category-tag">{{ item.category }}</text>
        <text class="detail-link">查看详情 ›</text>
      </view>
    </view>

    <view v-if="list.length > 0" class="load-more" @tap="loadMore">
      <text v-if="loading">加载中...</text>
      <text v-else-if="!hasMore">没有更多了</text>
      <text v-else>上拉加载更多</text>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { soilClassifyApi } from '@/api/index.js'
  import { nav } from '@/utils/index.js'

  const tabs = [
    { label: '全部', value: '' },
    { label: '自然土', value: 'natural' },
    { label: '人工填土', value: 'artificial' },
    { label: '岩石', value: 'rock' }
  ]

  const activeTab = ref('')
  const keyword = ref('')
  const list = ref([])
  const loading = ref(false)

  const pagination = reactive({
    page: 1,
    pageSize: 10,
    total: 0,
    hasMore: true
  })

  onMounted(() => { loadData() })

  async function loadData(isRefresh = true) {
    if (loading.value) return
    loading.value = true

    if (isRefresh) {
      pagination.page = 1
      pagination.hasMore = true
    }

    try {
      const params = {
        page: pagination.page,
        pageSize: pagination.pageSize,
        category: activeTab.value || undefined,
        keyword: keyword.value || undefined
      }
      const res = await soilClassifyApi.list(params)
      const data = res?.list || res?.records || res || []
      const total = res?.total || res?.count || data.length

      if (isRefresh) {
        list.value = data
      } else {
        list.value = [...list.value, ...data]
      }
      pagination.total = total
      pagination.hasMore = list.value.length < total
    } catch (e) {
    } finally {
      loading.value = false
    }
  }

  function loadMore() {
    if (!pagination.hasMore || loading.value) return
    pagination.page++
    loadData(false)
  }

  function getConfidenceClass(confidence) {
    if (confidence >= 90) return 'high'
    if (confidence >= 70) return 'medium'
    return 'low'
  }

  function goDetail(item) {
    nav.to('/pages/soil/classify-detail?id=' + item.id)
  }
</script>

<style lang="scss" scoped>
  .search-box {
    display: flex;
    align-items: center;
    background: $zrws-bg-secondary;
    border-radius: 40rpx;
    padding: 0 28rpx;
    height: 80rpx;
    margin-bottom: 24rpx;
    border: 1rpx solid $zrws-border-light;
  }
  .search-icon {
    font-size: 30rpx;
    margin-right: 14rpx;
    opacity: 0.6;
  }
  .search-input {
    flex: 1;
    height: 80rpx;
    font-size: 28rpx;
    color: $zrws-text-primary;
  }

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
    padding: 18rpx 0;
    font-size: 26rpx;
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

  .classify-card {
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .classify-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .soil-name {
    font-size: 32rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .confidence {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    font-weight: 600;
  }
  .confidence.high {
    background: rgba(124, 179, 66, 0.15);
    color: $zrws-success;
  }
  .confidence.medium {
    background: rgba(245, 124, 0, 0.15);
    color: $zrws-warning;
  }
  .confidence.low {
    background: rgba(229, 57, 53, 0.15);
    color: $zrws-error;
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
    max-width: 60%;
    text-align: right;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid $zrws-border-light;
  }
  .category-tag {
    padding: 8rpx 20rpx;
    background: $zrws-primary-bg;
    color: $zrws-primary-dark;
    border-radius: 16rpx;
    font-size: 22rpx;
  }
  .detail-link {
    font-size: 24rpx;
    color: $zrws-primary;
  }

  .load-more {
    text-align: center;
    padding: 30rpx 0;
    font-size: 24rpx;
    color: $zrws-text-tertiary;
  }

  .empty-wrap {
    margin-top: 80rpx;
  }
</style>
