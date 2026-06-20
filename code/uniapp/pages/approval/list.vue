<template>
  <app-page-layout title="审批中心" :showBack="true" :showTabbar="true">
    <!-- 统计卡片 -->
    <view class="stats-row">
      <view class="stat-item red">
        <text class="si-num">{{ stats.pending }}</text>
        <text class="si-label">待审批</text>
      </view>
      <view class="stat-item green">
        <text class="si-num">{{ stats.approved }}</text>
        <text class="si-label">已通过</text>
      </view>
      <view class="stat-item blue">
        <text class="si-num">{{ stats.underway }}</text>
        <text class="si-label">审核中</text>
      </view>
      <view class="stat-item gray">
        <text class="si-num">{{ stats.rejected }}</text>
        <text class="si-label">已驳回</text>
      </view>
    </view>

    <!-- Tab筛选 -->
    <view class="tabs-row">
      <text
        v-for="t in tabs"
        :key="t.value"
        class="tab-item"
        :class="{active: currentTab === t.value}"
        @tap="currentTab = t.value">{{ t.label }}</text>
    </view>

    <!-- 待办列表 -->
    <view v-if="filteredList.length === 0" class="empty-state">
      <text class="empty-icon">✅</text>
      <text class="empty-text">暂无{{ tabs.find(t => t.value === currentTab).label }}事项</text>
    </view>

    <view v-for="(a, i) in filteredList" :key="i" class="approval-card">
      <view class="ac-head">
        <view class="ac-type-tag">{{ a.type }}</view>
        <text class="ac-time">{{ a.createdAt }}</text>
      </view>
      <text class="ac-title">{{ a.title }}</text>

      <view class="ac-flow">
        <view class="flow-step">
          <text class="step-icon">👤</text>
          <text class="step-name">{{ a.initiator }}</text>
        </view>
        <text class="flow-arrow">→</text>
        <view class="flow-step active">
          <text class="step-icon">🔍</text>
          <text class="step-name">{{ a.currentNode }}</text>
        </view>
        <text class="flow-arrow">→</text>
        <view class="flow-step">
          <text class="step-icon">📝</text>
          <text class="step-name">归档</text>
        </view>
      </view>

      <view v-if="currentTab === 'pending'" class="ac-actions">
        <button class="ac-btn ac-btn-approve" @tap="onApprove(a)">✓ 通过</button>
        <button class="ac-btn ac-btn-reject" @tap="onReject(a)">✕ 驳回</button>
      </view>
      <view v-else class="ac-actions">
        <button class="ac-btn ac-btn-view" @tap="onView(a)">📄 查看详情</button>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { mockApprovals } from '@/utils/mock.js'
  import { toast, prompt } from '@/utils/index.js'

  const tabs = [
    { label: '待我审批', value: 'pending' },
    { label: '审核中', value: 'underway' },
    { label: '已通过', value: 'approved' },
    { label: '已驳回', value: 'rejected' }
  ]

  const currentTab = ref('pending')

  const stats = reactive({
    pending: 0,
    approved: 0,
    underway: 0,
    rejected: 0
  })

  // 模拟数据：将同一个列表映射成不同状态
  const allData = ref([])

  const filteredList = computed(() => {
    return allData.value.filter(a => a.status === currentTab.value)
  })

  onMounted(() => {
    loadData()
  })

  function loadData() {
    // 从mock数据生成不同状态列表
    allData.value = [
      ...mockApprovals.slice(0, 2).map(a => ({ ...a, status: 'pending' })),
      ...mockApprovals.slice(0, 1).map(a => ({ ...a, status: 'underway',
        title: '无人机 M350-002 飞行报备',
        currentNode: '部门领导审核' })),
      ...mockApprovals.slice(1, 2).map(a => ({ ...a, status: 'approved',
        title: '测绘物资申领 - GPS RTK基站',
        currentNode: '已通过' })),
      ...mockApprovals.slice(0, 1).map(a => ({ ...a, status: 'rejected',
        title: '野外作业设备采购申请',
        currentNode: '已驳回' }))
    ]
    stats.pending = allData.value.filter(a => a.status === 'pending').length
    stats.underway = allData.value.filter(a => a.status === 'underway').length
    stats.approved = allData.value.filter(a => a.status === 'approved').length
    stats.rejected = allData.value.filter(a => a.status === 'rejected').length
  }

  function onApprove(a) {
    uni.showModal({
      title: '确认通过',
      content: '确定通过「' + a.title + '」审批？',
      confirmText: '通过',
      confirmColor: '#67c23a',
      success: (res) => {
        if (res.confirm) {
          a.status = 'approved'
          a.currentNode = '已通过'
          stats.pending--
          stats.approved++
          toast.success('审批通过')
        }
      }
    })
  }

  function onReject(a) {
    uni.showModal({
      title: '驳回审批',
      content: '请填写驳回原因',
      editable: true,
      placeholderText: '请输入驳回原因...',
      confirmText: '驳回',
      confirmColor: '#f56c6c',
      success: (res) => {
        if (res.confirm) {
          a.status = 'rejected'
          a.currentNode = '已驳回'
          stats.pending--
          stats.rejected++
          toast.success('已驳回')
        }
      }
    })
  }

  function onView(a) {
    uni.showToast({
      title: '查看详情',
      icon: 'none',
      duration: 1500
    })
  }
</script>

<style lang="scss" scoped>
  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
    margin-bottom: 24rpx;
  }
  .stat-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 12rpx;
    text-align: center;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .si-num {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    font-family: 'DIN', monospace;
    margin-bottom: 6rpx;
    line-height: 1.2;
  }
  .stat-item.red .si-num { color: #f56c6c; }
  .stat-item.green .si-num { color: #67c23a; }
  .stat-item.blue .si-num { color: #2b6cb0; }
  .stat-item.gray .si-num { color: #909399; }
  .si-label { font-size: 22rpx; color: #606266; }

  .tabs-row {
    display: flex;
    background: #fff;
    border-radius: 12rpx;
    padding: 8rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 18rpx 0;
    font-size: 24rpx;
    color: #606266;
    border-radius: 8rpx;
    transition: all 0.2s;
  }
  .tab-item.active {
    background: linear-gradient(135deg, #1e3a5f, #2b6cb0);
    color: #fff;
    font-weight: 600;
  }

  .empty-state {
    text-align: center;
    padding: 120rpx 40rpx;
    background: #fff;
    border-radius: 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .empty-icon { font-size: 80rpx; }
  .empty-text { font-size: 26rpx; color: #c0c4cc; }

  .approval-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .ac-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
  }
  .ac-type-tag {
    padding: 6rpx 18rpx;
    background: #ecf5ff;
    color: #2b6cb0;
    font-size: 22rpx;
    font-weight: 600;
    border-radius: 16rpx;
  }
  .ac-time { font-size: 22rpx; color: #909399; }
  .ac-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
    line-height: 1.5;
    margin-bottom: 20rpx;
    display: block;
  }
  .ac-flow {
    display: flex;
    align-items: center;
    background: #f5f7fa;
    padding: 20rpx 16rpx;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
  }
  .flow-step {
    flex: 1;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
  }
  .flow-step .step-icon { font-size: 28rpx; opacity: 0.6; }
  .flow-step.active .step-icon { opacity: 1; font-size: 32rpx; }
  .flow-step .step-name { font-size: 20rpx; color: #909399; }
  .flow-step.active .step-name { color: #2b6cb0; font-weight: 600; }
  .flow-arrow { font-size: 24rpx; color: #c0c4cc; margin: 0 6rpx; }

  .ac-actions { display: flex; gap: 16rpx; }
  .ac-btn {
    flex: 1;
    height: 76rpx;
    font-size: 26rpx;
    font-weight: 600;
    border-radius: 12rpx;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .ac-btn-approve {
    background: linear-gradient(135deg, #67c23a, #4299e1);
    color: #fff;
  }
  .ac-btn-reject {
    background: #fff;
    color: #f56c6c;
    border: 2rpx solid #f56c6c;
  }
  .ac-btn-view {
    background: #f5f7fa;
    color: #606266;
  }
</style>
