<template>
  <app-page-layout title="灾害评估" :showBack="true" :showTabbar="true">
    <!-- 风险等级总览 -->
    <view class="risk-card">
      <view class="rc-head">
        <text class="rc-title">⚠️ 当前区域风险评估</text>
        <text class="rc-time">{{ now }}</text>
      </view>
      <view class="rc-main">
        <text class="rc-level" :class="'level-' + overallLevel">{{ overallText }}</text>
        <text class="rc-desc">{{ overallDesc }}</text>
      </view>
      <view class="rc-tags">
        <view
          v-for="(tag, i) in tagList"
          :key="i"
          class="rc-tag"
          :class="'tag-' + tag.color">{{ tag.label }}</view>
      </view>
    </view>

    <!-- 各类风险详情 -->
    <view class="section-title">📋 分类风险详情</view>
    <view v-for="(d, i) in disasters" :key="i" class="disaster-card">
      <view class="dc-head">
        <text class="dc-type">{{ d.type }}</text>
        <view class="dc-level" :class="'level-' + levelClass(d.level)">
          {{ d.level }}
        </view>
      </view>
      <view class="dc-body">
        <view class="dc-item">
          <text class="dc-label">影响面积</text>
          <text class="dc-value">{{ d.area }} 亩</text>
        </view>
        <view class="dc-item">
          <text class="dc-label">风险描述</text>
          <text class="dc-text">{{ d.desc }}</text>
        </view>
        <view class="dc-item">
          <text class="dc-label">处置建议</text>
          <text class="dc-text">💡 {{ d.action }}</text>
        </view>
      </view>
      <!-- 热力图 -->
      <view class="heat-map">
        <view class="hm-row" v-for="r in 4" :key="'r'+i+'-'+r">
          <view
            v-for="c in 8"
            :key="'c'+c"
            class="hm-cell"
            :style="{ background: getHeatColor(d.seed, r, c) }"></view>
        </view>
        <text class="hm-legend">
          <text class="lg-low">低</text>
          <text class="lg-bar"></text>
          <text class="lg-high">高</text>
        </text>
      </view>
    </view>

    <!-- 应急预案 -->
    <view class="section-title">📚 应急预案</view>
    <view class="plan-card">
      <view v-for="(p, i) in plans" :key="i" class="plan-item" @tap="togglePlan(i)">
        <view class="pi-head">
          <text class="pi-icon">{{ p.icon }}</text>
          <text class="pi-title">{{ p.title }}</text>
          <text class="pi-arrow" :class="{open: p.open}">›</text>
        </view>
        <view v-if="p.open" class="pi-body">
          <view v-for="(s, j) in p.steps" :key="j" class="pi-step">
            <text class="step-num">{{ j + 1 }}</text>
            <text class="step-text">{{ s }}</text>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { disasterApi } from '@/api/index.js'
  import { toast } from '@/utils/index.js'

  const disasters = ref([])
  const loading = ref(false)

  const now = computed(() => {
    return new Date().toLocaleString('zh-CN', { hour12: false })
  })

  const overallLevel = ref('warning')
  const overallText = computed(() => ({
    low: '低风险', warning: '中等风险', high: '高风险'
  }[overallLevel.value]) || '中等风险')

  const overallDesc = computed(() => {
    return '综合滑坡、泥石流、洪涝、地面沉降、土壤盐碱化等5类灾害指标评估'
  })

  const tagList = ref([
    { label: '中等洪涝风险', color: 'warning' },
    { label: '局部盐碱化', color: 'danger' },
    { label: '沉降监测中', color: 'info' }
  ])

  const plans = ref([
    { icon: '🟢', title: '日常巡查流程', open: false, steps: ['按照既定路线进行野外巡查', '记录地质地貌异常变化', '采集土壤和水样进行化验分析', '上传数据并完成日报告'] },
    { icon: '🟡', title: '预警触发响应', open: false, steps: ['收到预警信号后30分钟内响应', '现场核查确认', '通知周边村民/相关部门', '持续跟踪预警状态变化'] },
    { icon: '🔴', title: '应急处置方案', open: false, steps: ['立即撤离危险区域人员', '封锁现场，设置警戒线', '联合应急、地质、水利等部门', '启动应急监测站持续跟踪', '事件结束后编制调查报告'] }
  ])

  onMounted(() => {
    loadData()
  })

  async function loadData() {
    loading.value = true
    try {
      const [summaryRes, listRes] = await Promise.all([
        disasterApi.summary().catch(() => null),
        disasterApi.list().catch(() => null)
      ])

      let listData = []
      if (listRes) {
        listData = listRes.list || listRes.records || listRes || []
      } else if (summaryRes) {
        listData = summaryRes.list || summaryRes.items || summaryRes || []
      }

      disasters.value = listData.map((d, i) => ({
        ...d,
        type: d.type || d.disasterType || d.name || '',
        level: d.level || d.riskLevel || '低风险',
        area: d.area || d.affectedArea || 0,
        desc: d.desc || d.description || d.remark || '',
        action: d.action || d.suggestion || d.measure || '',
        seed: i + 1
      }))

      // 根据数据更新总体风险等级
      if (summaryRes?.overallLevel || summaryRes?.level) {
        const level = summaryRes.overallLevel || summaryRes.level
        if (level.indexOf('高') >= 0) {
          overallLevel.value = 'high'
        } else if (level.indexOf('中') >= 0) {
          overallLevel.value = 'warning'
        } else {
          overallLevel.value = 'low'
        }
      }

      // 更新标签
      if (summaryRes?.tags) {
        tagList.value = summaryRes.tags
      }
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  function levelClass(level) {
    if (level.indexOf('低') >= 0) return 'low'
    if (level.indexOf('中') >= 0) return 'warning'
    if (level.indexOf('高') >= 0 || level.indexOf('重') >= 0) return 'high'
    if (level.indexOf('监测') >= 0) return 'info'
    return 'info'
  }

  function getHeatColor(seed, r, c) {
    const v = ((seed * 17 + r * 13 + c * 7 + r * c * 3) % 100) / 100
    if (v < 0.35) return 'rgba(103,194,58,0.4)'
    if (v < 0.65) return 'rgba(230,162,60,0.6)'
    if (v < 0.85) return 'rgba(245,108,108,0.7)'
    return 'rgba(192,40,40,0.85)'
  }

  function togglePlan(i) {
    plans.value[i].open = !plans.value[i].open
  }
</script>

<style lang="scss" scoped>
  .risk-card {
    background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
    border-radius: 20rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(245,108,108,0.25);
  }
  .rc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  .rc-title { font-size: 28rpx; font-weight: 600; }
  .rc-time { font-size: 22rpx; opacity: 0.85; }
  .rc-main { text-align: center; padding: 20rpx 0; }
  .rc-level {
    display: block;
    font-size: 56rpx;
    font-weight: 700;
    margin-bottom: 12rpx;
    text-shadow: 0 4rpx 12rpx rgba(0,0,0,0.15);
  }
  .rc-desc { font-size: 24rpx; opacity: 0.9; }
  .rc-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-top: 24rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid rgba(255,255,255,0.2);
    justify-content: center;
  }
  .rc-tag {
    padding: 8rpx 22rpx;
    border-radius: 24rpx;
    font-size: 22rpx;
    background: rgba(255,255,255,0.2);
  }
  .rc-tag.tag-low { background: rgba(103,194,58,0.35); }
  .rc-tag.tag-warning { background: rgba(255,255,255,0.35); }
  .rc-tag.tag-high { background: rgba(255,99,99,0.5); }
  .rc-tag.tag-info { background: rgba(255,255,255,0.25); }

  .section-title {
    padding: 16rpx 4rpx 20rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }

  .disaster-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .dc-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    padding-bottom: 16rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }
  .dc-type { font-size: 30rpx; font-weight: 600; color: #303133; }
  .dc-level {
    padding: 8rpx 22rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    font-weight: 600;
    background: #f0f2f5;
    color: #606266;
  }
  .dc-level.level-low { background: #f0f9eb; color: #67c23a; }
  .dc-level.level-warning { background: #fdf6ec; color: #e6a23c; }
  .dc-level.level-high { background: #fef0f0; color: #f56c6c; }
  .dc-level.level-info { background: #ecf5ff; color: #2b6cb0; }

  .dc-body { padding: 4rpx 0 16rpx; }
  .dc-item { margin-bottom: 16rpx; }
  .dc-label {
    font-size: 22rpx;
    color: #909399;
    display: block;
    margin-bottom: 6rpx;
  }
  .dc-value {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
    font-family: 'DIN', monospace;
  }
  .dc-text {
    font-size: 26rpx;
    color: #606266;
    line-height: 1.6;
  }

  .heat-map {
    margin-top: 16rpx;
    padding: 20rpx 16rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
  }
  .hm-row {
    display: flex;
    gap: 4rpx;
    margin-bottom: 4rpx;
  }
  .hm-row:last-child { margin-bottom: 0; }
  .hm-cell {
    flex: 1;
    height: 30rpx;
    border-radius: 4rpx;
  }
  .hm-legend {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12rpx;
    margin-top: 16rpx;
    font-size: 20rpx;
    color: #909399;
  }
  .lg-bar {
    width: 120rpx;
    height: 10rpx;
    border-radius: 5rpx;
    background: linear-gradient(90deg, #67c23a, #e6a23c, #f56c6c);
  }

  .plan-card {
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .plan-item {
    border-bottom: 1rpx solid #f0f0f0;
  }
  .plan-item:last-child { border-bottom: none; }
  .pi-head {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
  }
  .pi-icon { font-size: 32rpx; margin-right: 16rpx; }
  .pi-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .pi-arrow {
    font-size: 40rpx;
    color: #c0c4cc;
    transform: rotate(0deg);
    transition: transform 0.3s;
  }
  .pi-arrow.open { transform: rotate(90deg); color: #2b6cb0; }
  .pi-body {
    padding: 0 24rpx 24rpx 60rpx;
  }
  .pi-step {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16rpx;
  }
  .pi-step:last-child { margin-bottom: 0; }
  .step-num {
    width: 36rpx;
    height: 36rpx;
    line-height: 36rpx;
    border-radius: 50%;
    background: #2b6cb0;
    color: #fff;
    font-size: 20rpx;
    text-align: center;
    margin-right: 16rpx;
    flex-shrink: 0;
  }
  .step-text {
    font-size: 26rpx;
    color: #606266;
    line-height: 1.6;
    flex: 1;
  }
</style>
