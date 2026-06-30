<template>
  <app-page-layout title="地质标准" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="search-box">
      <text class="search-icon">🔍</text>
      <input
        class="search-input"
        v-model="keyword"
        placeholder="搜索标准名称/编号"
        confirm-type="search"
        @confirm="loadData" />
    </view>

    <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
      <view class="tab-bar">
        <text
          v-for="t in tabs"
          :key="t.value"
          :class="['tab-item', {active: activeTab === t.value}]"
          @tap="activeTab = t.value; loadData()">
          {{ t.label }}
        </text>
      </view>
    </scroll-view>

    <view v-if="list.length === 0 && !loading" class="empty-wrap">
      <zrws-empty text="暂无标准数据" icon="📖"></zrws-empty>
    </view>

    <view v-for="(item, index) in list" :key="index" class="standard-card" @tap="goDetail(item)">
      <view class="card-header">
        <text class="std-name">{{ item.name }}</text>
        <text class="std-category">{{ item.category }}</text>
      </view>
      <view class="std-code">标准编号：{{ item.code }}</view>
      <view class="std-desc">{{ item.description }}</view>
      <view class="card-footer">
        <text class="std-version">版本：{{ item.version }}</text>
        <text class="detail-link">查看详情 ›</text>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { geoStandardApi } from '@/api/index.js'
  import { nav } from '@/utils/index.js'

  const tabs = [
    { label: '中国土壤', value: 'china-soil' },
    { label: '国际WRB', value: 'wrb' },
    { label: '岩浆岩', value: 'igneous' },
    { label: '沉积岩', value: 'sedimentary' },
    { label: '变质岩', value: 'metamorphic' }
  ]

  const activeTab = ref('china-soil')
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
      const res = await geoStandardApi.list(params)
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

  function goDetail(item) {
    nav.to('/pages/standard/detail?id=' + item.id)
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
    margin-bottom: 20rpx;
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

  .tab-scroll {
    white-space: nowrap;
    margin-bottom: 24rpx;
  }
  .tab-bar {
    display: inline-flex;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-lg;
    padding: 8rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .tab-item {
    display: inline-block;
    padding: 18rpx 28rpx;
    font-size: 26rpx;
    color: $zrws-text-secondary;
    border-radius: $zrws-radius-md;
    transition: all 0.3s ease;
    margin-right: 4rpx;
  }
  .tab-item:last-child {
    margin-right: 0;
  }
  .tab-item.active {
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4rpx 12rpx rgba(201, 169, 110, 0.3);
  }

  .standard-card {
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .standard-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
  }
  .std-name {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
    flex: 1;
    padding-right: 16rpx;
  }
  .std-category {
    padding: 6rpx 16rpx;
    background: $zrws-primary-bg;
    color: $zrws-primary-dark;
    border-radius: 12rpx;
    font-size: 22rpx;
    flex-shrink: 0;
  }

  .std-code {
    font-size: 24rpx;
    color: $zrws-text-secondary;
    margin-bottom: 12rpx;
  }

  .std-desc {
    font-size: 24rpx;
    color: $zrws-text-tertiary;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    margin-bottom: 16rpx;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid $zrws-border-light;
  }
  .std-version {
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
