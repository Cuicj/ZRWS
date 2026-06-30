<template>
  <app-page-layout title="报表中心" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="section-title">
      <text class="st-text">报表模板</text>
    </view>

    <view class="category-bar">
      <text
        v-for="c in categories"
        :key="c.value"
        :class="['cat-item', {active: activeCategory === c.value}]"
        @tap="activeCategory = c.value; loadTemplates()">
        {{ c.label }}
      </text>
    </view>

    <view v-if="templateList.length === 0 && !loading" class="empty-wrap">
      <zrws-empty text="暂无报表模板" icon="📄"></zrws-empty>
    </view>

    <view class="template-grid">
      <view
        v-for="(item, index) in templateList"
        :key="index"
        class="template-card"
        @tap="generateReport(item)">
        <view class="template-icon">
          <text>{{ getTemplateIcon(item.type) }}</text>
        </view>
        <text class="template-name">{{ item.name }}</text>
        <text class="template-desc">{{ item.description }}</text>
        <view class="generate-btn">
          <text>生成报表</text>
        </view>
      </view>
    </view>

    <view class="section-title">
      <text class="st-text">历史记录</text>
      <text class="st-more" @tap="viewAllHistory">查看全部 ›</text>
    </view>

    <view v-if="historyList.length === 0 && !loading" class="empty-wrap small">
      <zrws-empty text="暂无历史记录" icon="📋"></zrws-empty>
    </view>

    <view v-for="(item, index) in historyList" :key="index" class="history-card" @tap="viewHistory(item)">
      <view class="hc-icon">📊</view>
      <view class="hc-content">
        <text class="hc-name">{{ item.name }}</text>
        <text class="hc-time">{{ item.createTime }}</text>
      </view>
      <text class="hc-status">{{ item.status }}</text>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import { reportApi } from '@/api/index.js'
  import { nav, toast } from '@/utils/index.js'

  const categories = [
    { label: '全部', value: '' },
    { label: '仪表盘', value: 'dashboard' },
    { label: '土质', value: 'soil' },
    { label: '灾害', value: 'disaster' },
    { label: '岩层', value: 'rock' }
  ]

  const activeCategory = ref('')
  const templateList = ref([])
  const historyList = ref([])
  const loading = ref(false)

  onMounted(() => { loadData() })

  async function loadData() {
    loadTemplates()
    loadHistory()
  }

  async function loadTemplates() {
    loading.value = true
    try {
      const params = {
        category: activeCategory.value || undefined
      }
      const res = await reportApi.templates(params)
      templateList.value = res?.list || res?.records || res || []
    } catch (e) {
    } finally {
      loading.value = false
    }
  }

  async function loadHistory() {
    try {
      const res = await reportApi.history({ pageSize: 5 })
      historyList.value = res?.list || res?.records || res || []
    } catch (e) {
    }
  }

  function getTemplateIcon(type) {
    const iconMap = {
      dashboard: '📊',
      soil: '🌱',
      disaster: '⚠️',
      rock: '🪨'
    }
    return iconMap[type] || '📄'
  }

  async function generateReport(template) {
    try {
      toast.loading('生成中...')
      await reportApi.generate({ templateId: template.id })
      toast.hide()
      toast.success('报表生成成功')
      loadHistory()
    } catch (e) {
      toast.hide()
    }
  }

  function viewAllHistory() {
    toast.info('历史记录页面开发中')
  }

  function viewHistory(item) {
    toast.info('报表详情开发中')
  }
</script>

<style lang="scss" scoped>
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 4rpx 16rpx;
  }
  .st-text {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .st-more {
    font-size: 24rpx;
    color: $zrws-primary;
  }

  .category-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 24rpx;
  }
  .cat-item {
    padding: 14rpx 28rpx;
    background: $zrws-bg-secondary;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: $zrws-text-secondary;
    transition: all 0.3s ease;
    border: 1rpx solid $zrws-border-light;
  }
  .cat-item.active {
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    color: #fff;
    border-color: transparent;
    box-shadow: 0 4rpx 12rpx rgba(201, 169, 110, 0.3);
  }

  .template-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20rpx;
    margin-bottom: 16rpx;
  }
  .template-card {
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 28rpx 20rpx;
    text-align: center;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .template-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }
  .template-icon {
    width: 96rpx;
    height: 96rpx;
    margin: 0 auto 16rpx;
    background: $zrws-primary-bg;
    border-radius: $zrws-radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
  }
  .template-name {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: $zrws-text-primary;
    margin-bottom: 8rpx;
  }
  .template-desc {
    display: block;
    font-size: 22rpx;
    color: $zrws-text-tertiary;
    margin-bottom: 16rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .generate-btn {
    display: inline-block;
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary);
    color: #fff;
    border-radius: 24rpx;
    font-size: 24rpx;
    font-weight: 500;
  }

  .history-card {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, $zrws-bg-secondary 0%, $zrws-bg-tertiary 100%);
    border-radius: $zrws-radius-lg;
    padding: 24rpx;
    margin-bottom: 16rpx;
    box-shadow: $zrws-shadow-sm;
    transition: all 0.3s ease;
  }
  .history-card:active {
    transform: translateY(-2rpx);
    box-shadow: $zrws-shadow-md;
  }
  .hc-icon {
    width: 72rpx;
    height: 72rpx;
    background: $zrws-primary-bg;
    border-radius: $zrws-radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
  }
  .hc-content {
    flex: 1;
    min-width: 0;
  }
  .hc-name {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: $zrws-text-primary;
    margin-bottom: 6rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .hc-time {
    display: block;
    font-size: 22rpx;
    color: $zrws-text-tertiary;
  }
  .hc-status {
    font-size: 24rpx;
    color: $zrws-success;
    padding-left: 16rpx;
    flex-shrink: 0;
  }

  .empty-wrap {
    margin-top: 60rpx;
  }
  .empty-wrap.small {
    margin-top: 40rpx;
  }
</style>
