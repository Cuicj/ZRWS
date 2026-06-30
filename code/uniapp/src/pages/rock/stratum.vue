<template>
  <app-page-layout title="岩层分析" :showBack="true" :refresherEnabled="true" @refresh="loadData">
    <view class="tab-bar">
      <text
        v-for="t in tabs"
        :key="t.value"
        :class="['tab-item', {active: activeTab === t.value}]"
        @tap="activeTab = t.value">
        {{ t.label }}
      </text>
    </view>

    <view v-if="activeTab === 'comprehensive'">
      <view class="section-title">
        <text class="st-text">钻孔数据</text>
      </view>
      <view v-if="drillingData.length === 0" class="empty-wrap">
        <zrws-empty text="暂无钻孔数据" icon="⛏️"></zrws-empty>
      </view>
      <view v-for="(item, index) in drillingData" :key="index" class="drilling-card">
        <view class="dc-header">
          <text class="dc-name">{{ item.name }}</text>
          <text class="dc-depth">深度 {{ item.depth }}m</text>
        </view>
        <view class="dc-body">
          <view class="dc-info">
            <text class="dc-label">钻孔编号</text>
            <text class="dc-value">{{ item.code }}</text>
          </view>
          <view class="dc-info">
            <text class="dc-label">地理位置</text>
            <text class="dc-value">{{ item.location }}</text>
          </view>
        </view>
      </view>

      <view class="section-title">
        <text class="st-text">岩层分布概览</text>
      </view>
      <view v-if="stratumOverview.length === 0" class="empty-wrap">
        <zrws-empty text="暂无岩层分布数据" icon="🪨"></zrws-empty>
      </view>
      <view class="stratum-chart">
        <view
          v-for="(layer, index) in stratumOverview"
          :key="index"
          class="stratum-layer"
          :style="{ height: layer.height + '%', background: layer.color }">
          <text class="layer-name">{{ layer.name }}</text>
          <text class="layer-depth">{{ layer.depth }}</text>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'sample'">
      <view class="section-title">
        <text class="st-text">样品列表</text>
      </view>
      <view v-if="sampleList.length === 0" class="empty-wrap">
        <zrws-empty text="暂无样品数据" icon="🧪"></zrws-empty>
      </view>
      <view v-for="(item, index) in sampleList" :key="index" class="sample-card">
        <view class="sc-header">
          <text class="sc-name">{{ item.name }}</text>
          <text class="sc-type">{{ item.type }}</text>
        </view>
        <view class="sc-body">
          <view class="sc-info">
            <text class="sc-label">采样深度</text>
            <text class="sc-value">{{ item.depth }}</text>
          </view>
          <view class="sc-info">
            <text class="sc-label">采样日期</text>
            <text class="sc-value">{{ item.date }}</text>
          </view>
        </view>
        <view class="sc-components">
          <text class="sc-comp-title">主要成分：</text>
          <text class="sc-comp-text">{{ item.components }}</text>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'standard'">
      <view class="section-title">
        <text class="st-text">岩石标准库</text>
      </view>
      <view v-if="standardList.length === 0" class="empty-wrap">
        <zrws-empty text="暂无标准数据" icon="📚"></zrws-empty>
      </view>
      <view v-for="(item, index) in standardList" :key="index" class="standard-card" @tap="goStandardDetail(item)">
        <view class="std-name">{{ item.name }}</view>
        <view class="std-code">编号：{{ item.code }}</view>
        <view class="std-desc">{{ item.description }}</view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import { rockStratumApi } from '@/api/index.js'
  import { nav } from '@/utils/index.js'

  const tabs = [
    { label: '综合分析', value: 'comprehensive' },
    { label: '取样法分析', value: 'sample' },
    { label: '标准库', value: 'standard' }
  ]

  const activeTab = ref('comprehensive')
  const drillingData = ref([])
  const stratumOverview = ref([])
  const sampleList = ref([])
  const standardList = ref([])

  onMounted(() => { loadData() })

  async function loadData() {
    try {
      const [analysisRes, sampleRes, standardRes] = await Promise.all([
        rockStratumApi.analysis().catch(() => null),
        rockStratumApi.sampleList().catch(() => null),
        rockStratumApi.standards().catch(() => null)
      ])

      if (analysisRes) {
        drillingData.value = analysisRes.drillingData || analysisRes.drillings || []
        stratumOverview.value = analysisRes.stratumOverview || analysisRes.layers || []
      }

      if (sampleRes) {
        sampleList.value = sampleRes.list || sampleRes.records || sampleRes || []
      }

      if (standardRes) {
        standardList.value = standardRes.list || standardRes.records || standardRes || []
      }
    } catch (e) {
    }
  }

  function goStandardDetail(item) {
    nav.to('/pages/standard/detail?id=' + item.id)
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

  .section-title {
    padding: 20rpx 4rpx 16rpx;
  }
  .st-text {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }

  .drilling-card, .sample-card, .standard-card {
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

  .dc-header, .sc-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
  }
  .dc-name, .sc-name, .std-name {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .dc-depth {
    font-size: 24rpx;
    color: $zrws-primary;
    font-weight: 500;
  }
  .sc-type {
    padding: 6rpx 16rpx;
    background: $zrws-primary-bg;
    color: $zrws-primary-dark;
    border-radius: 12rpx;
    font-size: 22rpx;
  }

  .dc-body, .sc-body {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  .dc-info, .sc-info {
    flex: 1;
    min-width: 45%;
  }
  .dc-label, .sc-label {
    font-size: 24rpx;
    color: $zrws-text-secondary;
    display: block;
    margin-bottom: 4rpx;
  }
  .dc-value, .sc-value {
    font-size: 26rpx;
    color: $zrws-text-primary;
  }

  .sc-components {
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid $zrws-border-light;
  }
  .sc-comp-title {
    font-size: 24rpx;
    color: $zrws-text-secondary;
  }
  .sc-comp-text {
    font-size: 26rpx;
    color: $zrws-text-primary;
    margin-left: 8rpx;
  }

  .stratum-chart {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-lg;
    padding: 24rpx;
    height: 500rpx;
    display: flex;
    flex-direction: column;
    box-shadow: $zrws-shadow-sm;
  }
  .stratum-layer {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border-radius: $zrws-radius-sm;
    margin-bottom: 4rpx;
    transition: all 0.3s ease;
  }
  .stratum-layer:last-child {
    margin-bottom: 0;
  }
  .layer-name {
    font-size: 26rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 4rpx;
  }
  .layer-depth {
    font-size: 22rpx;
    color: rgba(255,255,255,0.8);
  }

  .std-code {
    font-size: 24rpx;
    color: $zrws-text-secondary;
    margin-top: 8rpx;
  }
  .std-desc {
    font-size: 24rpx;
    color: $zrws-text-tertiary;
    margin-top: 12rpx;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .empty-wrap {
    margin-top: 60rpx;
  }
</style>
