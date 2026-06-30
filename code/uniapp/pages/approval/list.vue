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
        <zrws-button variant="success" size="md" @click="onApprove(a)">✓ 通过</zrws-button>
        <zrws-button variant="danger" size="md" @click="onReject(a)">✕ 驳回</zrws-button>
      </view>
      <view v-else class="ac-actions">
        <zrws-button variant="ghost" size="md" @click="onView(a)">📄 查看详情</zrws-button>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, watch } from 'vue'
  import { approvalApi } from '@/api/index.js'
  import { toast, prompt } from '@/utils/index.js'

  const tabs = [
    { label: '待我审批', value: 'pending' },
    { label: '审核中', value: 'underway' },
    { label: '已通过', value: 'approved' },
    { label: '已驳回', value: 'rejected' }
  ]

  const currentTab = ref('pending')
  const loading = ref(false)

  const stats = reactive({
    pending: 0,
    approved: 0,
    underway: 0,
    rejected: 0
  })

  const allData = ref([])

  const filteredList = computed(() => {
    return allData.value.filter(a => a.status === currentTab.value)
  })

  onMounted(() => {
    loadData()
  })

  watch(currentTab, () => {
    loadData()
  })

  async function loadData() {
    loading.value = true
    try {
      const [pendingRes, doneRes] = await Promise.all([
      approvalApi.pending().catch(() => []),
      approvalApi.done().catch(() => [])
    ])

    const pendingList = Array.isArray(pendingRes) ? pendingRes : (pendingRes?.list || pendingRes?.records || [])
    const doneList = Array.isArray(doneRes) ? doneRes : (doneRes?.list || doneRes?.records || [])

    const mappedPending = pendingList.map(a => ({
      ...a,
      id: a.id || a.approvalId || '',
      title: a.title || a.name || a.subject || '',
      type: a.type || a.approvalType || '',
      initiator: a.initiator || a.applicant || a.createBy || '',
      currentNode: a.currentNode || a.statusText || a.step || '',
      createdAt: a.createdAt || a.createTime || a.submitTime || '',
      status: 'pending'
    }))

    const mappedDone = doneList.map(a => ({
      ...a,
      id: a.id || a.approvalId || '',
      title: a.title || a.name || a.subject || '',
      type: a.type || a.approvalType || '',
      initiator: a.initiator || a.applicant || a.createBy || '',
      currentNode: a.status === 'approved' ? '已通过' : (a.status === 'rejected' ? '已驳回' : (a.currentNode || a.statusText || '')),
      createdAt: a.createdAt || a.createTime || a.approvalTime || '',
      status: a.status || (a.result === 'approved' ? 'approved' : (a.result === 'rejected' ? 'rejected' : 'underway'))
    }))

    allData.value = [...mappedPending, ...mappedDone]

    stats.pending = allData.value.filter(a => a.status === 'pending').length
    stats.underway = allData.value.filter(a => a.status === 'underway').length
    stats.approved = allData.value.filter(a => a.status === 'approved').length
    stats.rejected = allData.value.filter(a => a.status === 'rejected').length
  } catch (e) {
    // 错误提示已在 request 封装中处理
  } finally {
    loading.value = false
  }
}

  async function onApprove(a) {
    uni.showModal({
      title: '确认通过',
      content: '确定通过「' + a.title + '」审批？',
      confirmText: '通过',
      confirmColor: '#67c23a',
      success: async (res) => {
        if (res.confirm) {
          try {
            await approvalApi.approve(a.id)
            a.status = 'approved'
            a.currentNode = '已通过'
            stats.pending--
            stats.approved++
            toast.success('审批通过')
          } catch (e) {
            // 错误提示已在 request 封装中处理
          }
        }
      }
    })
  }

  async function onReject(a) {
    uni.showModal({
      title: '驳回审批',
      content: '请填写驳回原因',
      editable: true,
      placeholderText: '请输入驳回原因...',
      confirmText: '驳回',
      confirmColor: '#f56c6c',
      success: async (res) => {
        if (res.confirm) {
          try {
            const reason = res.content || '不同意'
            await approvalApi.reject(a.id, reason)
            a.status = 'rejected'
            a.currentNode = '已驳回'
            stats.pending--
            stats.rejected++
            toast.success('已驳回')
          } catch (e) {
            // 错误提示已在 request 封装中处理
          }
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
  .ac-actions zrws-button {
    flex: 1;
  }
</style>
