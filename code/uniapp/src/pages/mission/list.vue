<template>
  <app-page-layout title="任务列表" :showBack="false" :showTabbar="true" :refresherEnabled="true" @refresh="loadData">
    <!-- 搜索筛选 -->
    <view class="filter-bar">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="filter.keyword"
          placeholder="搜索任务编号/区域"
          confirm-type="search"
          @confirm="loadData" />
      </view>
      <view class="filter-tags">
        <text
          v-for="t in statusTags"
          :key="t.value"
          :class="['ft-item', {active: filter.status === t.value}]"
          @tap="filter.status = t.value; loadData()">
          {{ t.label }}
        </text>
      </view>
    </view>

    <!-- 新建按钮 -->
    <view class="create-btn-wrap">
      <zrws-button variant="primary" size="lg" block @click="showCreateDialog = true">
        ＋ 新建采集任务
      </zrws-button>
    </view>

    <!-- 任务卡片列表 -->
    <view v-if="list.length === 0" class="empty-state">
      <text class="empty-icon">📭</text>
      <text>暂无任务数据</text>
    </view>

    <zrws-mission-card
      v-for="(m, i) in list"
      :key="i"
      :mission="m"
      @tap="openDetail(m)">
    </zrws-mission-card>

    <view style="height: 40rpx;"></view>
  </app-page-layout>

  <!-- 新建任务弹窗 -->
  <view v-if="showCreateDialog" class="dialog-mask" @tap="showCreateDialog = false">
    <view class="dialog-panel" @tap.stop>
      <view class="dialog-title">新建采集任务</view>

      <view class="form-item">
        <text class="form-label">任务名称</text>
        <input class="form-input" v-model="newMission.name" placeholder="如 望城区乔口镇土地调查" />
      </view>

      <view class="form-item">
        <text class="form-label">采集区域</text>
        <input class="form-input" v-model="newMission.area" placeholder="湖南省长沙市望城区..." />
      </view>

      <view class="form-item">
        <text class="form-label">飞行高度（米）</text>
        <input class="form-input" type="number" v-model="newMission.altitude" placeholder="120" />
      </view>

      <view class="form-item">
        <text class="form-label">GPS定位模式</text>
        <view class="radio-group">
          <text
            :class="['radio-item', {active: newMission.gpsMode === 'RTK'}]"
            @tap="newMission.gpsMode = 'RTK'">
            ● RTK厘米级
          </text>
          <text
            :class="['radio-item', {active: newMission.gpsMode === 'DGPS'}]"
            @tap="newMission.gpsMode = 'DGPS'">
            ○ DGPS分米级
          </text>
        </view>
      </view>

      <view class="dialog-actions">
        <zrws-button variant="outline" size="md" @click="showCreateDialog = false">取消</zrws-button>
        <zrws-button variant="primary" size="md" @click="createMission">创建</zrws-button>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue'
  import { missionApi } from '@/api/index.js'
  import { toast, nav } from '@/utils/index.js'

  const statusTags = [
    { label: '全部', value: '' },
    { label: '执行中', value: 'processing' },
    { label: '待执行', value: 'pending' },
    { label: '已完成', value: 'completed' },
    { label: '异常', value: 'abnormal' }
  ]

  const filter = reactive({ keyword: '', status: '' })
  const list = ref([])
  const loading = ref(false)
  const showCreateDialog = ref(false)
  const newMission = reactive({
    name: '', area: '', altitude: 120, gpsMode: 'RTK'
  })

  const pagination = reactive({
    page: 1,
    pageSize: 20,
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
        keyword: filter.keyword || undefined,
        status: filter.status || undefined
      }
      const res = await missionApi.list(params)
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
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  function loadMore() {
    if (!pagination.hasMore || loading.value) return
    pagination.page++
    loadData(false)
  }

  function openDetail(m) {
    nav.to('/pages/mission/detail?id=' + m.id)
  }

  async function createMission() {
    if (!newMission.name) {
      toast.info('请填写任务名称')
      return
    }

    try {
      const missionData = {
        name: newMission.name,
        area: newMission.area || newMission.name,
        altitude: newMission.altitude || 120,
        gpsMode: newMission.gpsMode || 'RTK'
      }
      await missionApi.create(missionData)
      showCreateDialog.value = false
      toast.success('任务创建成功')
      newMission.name = ''
      newMission.area = ''
      loadData()
    } catch (e) {
      // 错误提示已在 request 封装中处理
    }
  }
</script>

<style lang="scss" scoped>
  .filter-bar {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 20rpx;
    margin-bottom: 20rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .search-box {
    display: flex;
    align-items: center;
    background: $zrws-bg-tertiary;
    border-radius: 40rpx;
    padding: 0 24rpx;
    height: 72rpx;
    margin-bottom: 16rpx;
  }
  .search-icon {
    font-size: 28rpx;
    margin-right: 12rpx;
    opacity: 0.6;
  }
  .search-input {
    flex: 1;
    height: 72rpx;
    font-size: 26rpx;
    color: $zrws-text-primary;
  }
  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  .ft-item {
    padding: 12rpx 28rpx;
    background: $zrws-bg-tertiary;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: $zrws-text-secondary;
  }
  .ft-item.active {
    background: linear-gradient(135deg, $zrws-primary-dark, $zrws-primary);
    color: $zrws-text-inverse;
  }

  .create-btn-wrap {
    margin-bottom: 24rpx;
  }

  .empty-state {
    text-align: center;
    padding: 120rpx 40rpx;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: $zrws-text-tertiary;
    font-size: 26rpx;
  }
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  /* 弹窗样式 */
  .dialog-mask {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(93, 78, 55, 0.5);
    z-index: 999;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60rpx;
  }
  .dialog-panel {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-lg;
    padding: 40rpx 32rpx;
    width: 100%;
    max-height: 85vh;
    overflow-y: auto;
    box-shadow: $zrws-shadow-lg;
  }
  .dialog-title {
    font-size: 34rpx;
    font-weight: 600;
    color: $zrws-text-primary;
    text-align: center;
    margin-bottom: 40rpx;
  }
  .form-item {
    margin-bottom: 28rpx;
  }
  .form-label {
    display: block;
    font-size: 26rpx;
    color: $zrws-text-secondary;
    margin-bottom: 12rpx;
  }
  .form-input {
    width: 100%;
    height: 84rpx;
    background: $zrws-bg-tertiary;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: $zrws-text-primary;
  }
  .radio-group {
    display: flex;
    gap: 20rpx;
  }
  .radio-item {
    flex: 1;
    padding: 20rpx;
    background: $zrws-bg-tertiary;
    border-radius: 12rpx;
    text-align: center;
    font-size: 26rpx;
    color: $zrws-text-secondary;
  }
  .radio-item.active {
    background: $zrws-primary-bg;
    color: $zrws-primary-dark;
    font-weight: 600;
  }
  .dialog-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 40rpx;
  }
  .dialog-actions zrws-button {
    flex: 1;
  }
</style>
