<template>
  <app-page-layout title="数据导出" :showBack="true">
    <view class="export-page">
      <view class="card type-card">
        <view class="card-header">
          <text class="card-title">📂 导出类型</text>
        </view>
        <view class="type-grid">
          <view
            v-for="(t, i) in exportTypes"
            :key="i"
            class="type-item"
            :class="{ active: selectedType === t.value }"
            @tap="selectType(t.value)">
            <text class="type-icon">{{ t.icon }}</text>
            <text class="type-name">{{ t.label }}</text>
          </view>
        </view>
      </view>

      <view class="card filter-card">
        <view class="card-header">
          <text class="card-title">🔍 筛选条件</text>
        </view>
        <view class="filter-group">
          <text class="filter-label">时间范围</text>
          <view class="date-range">
            <picker mode="date" :value="startDate" @change="onStartDateChange">
              <view class="date-input">
                <text>{{ startDate || '开始日期' }}</text>
              </view>
            </picker>
            <text class="date-sep">至</text>
            <picker mode="date" :value="endDate" @change="onEndDateChange">
              <view class="date-input">
                <text>{{ endDate || '结束日期' }}</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="filter-group">
          <text class="filter-label">区域筛选</text>
          <view class="filter-select" @tap="selectArea">
            <text :class="{ placeholder: !selectedArea }">{{ selectedArea || '全部区域' }}</text>
            <text class="select-arrow">›</text>
          </view>
        </view>
        <view class="filter-group">
          <text class="filter-label">数据状态</text>
          <view class="status-tags">
            <view
              v-for="(s, i) in statusOptions"
              :key="i"
              class="status-tag"
              :class="{ active: selectedStatus.includes(s.value) }"
              @tap="toggleStatus(s.value)">
              {{ s.label }}
            </view>
          </view>
        </view>
      </view>

      <view class="card fields-card">
        <view class="card-header">
          <text class="card-title">📋 导出字段</text>
          <view class="select-all" @tap="toggleSelectAll">
            <text class="select-all-check">{{ allSelected ? '☑' : '☐' }}</text>
            <text class="select-all-text">全选</text>
          </view>
        </view>
        <view class="field-list">
          <view
            v-for="(f, i) in fieldOptions"
            :key="i"
            class="field-item"
            @tap="toggleField(f.value)">
            <text class="field-check">{{ selectedFields.includes(f.value) ? '☑' : '☐' }}</text>
            <text class="field-name">{{ f.label }}</text>
          </view>
        </view>
      </view>

      <view class="card action-card">
        <button class="export-btn" :disabled="exporting" @tap="startExport">
          <text>{{ exporting ? '导出中...' : '📤 开始导出' }}</text>
        </button>
        <view class="export-format">
          <text class="format-label">导出格式：</text>
          <view class="format-options">
            <view
              class="format-item"
              :class="{ active: exportFormat === 'xlsx' }"
              @tap="exportFormat = 'xlsx'">
              Excel (.xlsx)
            </view>
            <view
              class="format-item"
              :class="{ active: exportFormat === 'csv' }"
              @tap="exportFormat = 'csv'">
              CSV (.csv)
            </view>
          </view>
        </view>
      </view>

      <view class="card task-card">
        <view class="card-header">
          <text class="card-title">📦 导出任务</text>
          <text class="task-count">共 {{ taskList.length }} 个</text>
        </view>
        <view v-if="taskList.length === 0" class="empty-tip">暂无导出任务</view>
        <view v-else class="task-list">
          <view v-for="(t, i) in taskList" :key="i" class="task-item">
            <view class="task-icon">
              {{ t.status === 'success' ? '✅' : t.status === 'processing' ? '⏳' : t.status === 'failed' ? '❌' : '📊' }}
            </view>
            <view class="task-info">
              <text class="task-name">{{ t.fileName }}</text>
              <text class="task-time">{{ t.createTime }}</text>
              <view v-if="t.status === 'processing'" class="task-progress">
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: t.progress + '%' }"></view>
                </view>
                <text class="progress-text">{{ t.progress }}%</text>
              </view>
            </view>
            <view class="task-action">
              <view v-if="t.status === 'success'" class="action-btn download" @tap="downloadFile(t)">
                下载
              </view>
              <view v-else-if="t.status === 'failed'" class="action-btn retry" @tap="retryExport(t)">
                重试
              </view>
              <view v-else class="task-status-text">
                {{ t.status === 'processing' ? '导出中' : '等待中' }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;"></view>
    </view>
  </app-page-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { toast } from '@/utils/index.js'

const selectedType = ref('soil_sample')
const startDate = ref('')
const endDate = ref('')
const selectedArea = ref('')
const selectedStatus = ref(['approved', 'pending'])
const selectedFields = ref(['name', 'location', 'time', 'collector', 'depth'])
const exportFormat = ref('xlsx')
const exporting = ref(false)

const exportTypes = [
  { value: 'soil_sample', label: '土壤采样', icon: '🌱' },
  { value: 'rock_sample', label: '岩石采样', icon: '🪨' },
  { value: 'disaster_record', label: '灾害记录', icon: '⚠️' },
  { value: 'device_data', label: '设备数据', icon: '🔧' },
  { value: 'mission_data', label: '任务数据', icon: '📋' },
  { value: 'report_data', label: '报表数据', icon: '📊' }
]

const statusOptions = [
  { value: 'pending', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已驳回' }
]

const fieldOptions = [
  { value: 'name', label: '名称' },
  { value: 'location', label: '位置坐标' },
  { value: 'time', label: '采集时间' },
  { value: 'collector', label: '采集人' },
  { value: 'depth', label: '采样深度' },
  { value: 'type', label: '类型分类' },
  { value: 'description', label: '描述说明' },
  { value: 'photos', label: '图片附件' }
]

const taskList = ref([
  {
    fileName: '土壤采样数据_2024Q1.xlsx',
    createTime: '2024-03-15 10:30',
    status: 'success',
    size: '2.4 MB'
  },
  {
    fileName: '灾害记录汇总.xlsx',
    createTime: '2024-03-14 16:45',
    status: 'success',
    size: '1.8 MB'
  },
  {
    fileName: '设备监测数据_本周.csv',
    createTime: '2024-03-18 09:00',
    status: 'processing',
    progress: 65
  }
])

const allSelected = computed(() => selectedFields.value.length === fieldOptions.length)

function selectType(type) {
  selectedType.value = type
}

function onStartDateChange(e) {
  startDate.value = e.detail.value
}

function onEndDateChange(e) {
  endDate.value = e.detail.value
}

function selectArea() {
  const areas = ['全部区域', '华北片区', '华东片区', '华南片区', '西南片区', '西北片区']
  uni.showActionSheet({
    itemList: areas,
    success: (res) => {
      selectedArea.value = areas[res.tapIndex] === '全部区域' ? '' : areas[res.tapIndex]
    }
  })
}

function toggleStatus(value) {
  const index = selectedStatus.value.indexOf(value)
  if (index > -1) {
    if (selectedStatus.value.length > 1) {
      selectedStatus.value.splice(index, 1)
    }
  } else {
    selectedStatus.value.push(value)
  }
}

function toggleSelectAll() {
  if (allSelected.value) {
    selectedFields.value = []
  } else {
    selectedFields.value = fieldOptions.map(f => f.value)
  }
}

function toggleField(value) {
  const index = selectedFields.value.indexOf(value)
  if (index > -1) {
    selectedFields.value.splice(index, 1)
  } else {
    selectedFields.value.push(value)
  }
}

async function startExport() {
  if (selectedFields.value.length === 0) {
    toast.error('请至少选择一个导出字段')
    return
  }

  exporting.value = true
  toast.loading('生成导出文件...')

  setTimeout(() => {
    exporting.value = false
    toast.hide()

    const newTask = {
      fileName: getExportFileName(),
      createTime: formatNow(),
      status: 'processing',
      progress: 0
    }
    taskList.value.unshift(newTask)

    simulateProgress(newTask)

    toast.success('导出任务已创建')
  }, 1000)
}

function getExportFileName() {
  const typeMap = {
    'soil_sample': '土壤采样数据',
    'rock_sample': '岩石采样数据',
    'disaster_record': '灾害记录',
    'device_data': '设备数据',
    'mission_data': '任务数据',
    'report_data': '报表数据'
  }
  const typeName = typeMap[selectedType.value] || '数据'
  const ext = exportFormat.value === 'xlsx' ? '.xlsx' : '.csv'
  const now = new Date()
  const dateStr = now.getFullYear() +
    String(now.getMonth() + 1).padStart(2, '0') +
    String(now.getDate()).padStart(2, '0')
  return `${typeName}_${dateStr}${ext}`
}

function formatNow() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`
}

function simulateProgress(task) {
  const interval = setInterval(() => {
    task.progress += Math.random() * 15 + 5
    if (task.progress >= 100) {
      task.progress = 100
      task.status = 'success'
      task.size = (Math.random() * 3 + 1).toFixed(1) + ' MB'
      clearInterval(interval)
    }
  }, 500)
}

function downloadFile(task) {
  toast.success(`开始下载: ${task.fileName}`)
}

function retryExport(task) {
  task.status = 'processing'
  task.progress = 0
  simulateProgress(task)
  toast.info('重新导出中...')
}
</script>

<style lang="scss" scoped>
.export-page {
  padding-bottom: 20rpx;
}

.card {
  background: linear-gradient(145deg, #FAFAF8, #F5F2ED);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(93, 78, 55, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #5D4E37;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.type-item {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16rpx;
  padding: 20rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;

  &.active {
    background: rgba(201, 169, 110, 0.1);
    border-color: #C9A96E;
  }
}

.type-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.type-name {
  font-size: 24rpx;
  color: #5D4E37;
  font-weight: 500;
}

.filter-group {
  margin-bottom: 20rpx;

  &:last-child { margin-bottom: 0; }
}

.filter-label {
  display: block;
  font-size: 26rpx;
  color: #5D4E37;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.date-input {
  flex: 1;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #5D4E37;
  border: 2rpx solid #F0EBE2;
}

.date-sep {
  font-size: 26rpx;
  color: #8B7355;
}

.filter-select {
  height: 72rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 26rpx;
  color: #5D4E37;
  border: 2rpx solid #F0EBE2;

  .placeholder {
    color: #B5A896;
  }
}

.select-arrow {
  font-size: 28rpx;
  color: #B5A896;
  transform: rotate(90deg);
}

.status-tags {
  display: flex;
  gap: 12rpx;
}

.status-tag {
  flex: 1;
  height: 68rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #8B7355;
  border: 2rpx solid #F0EBE2;
  transition: all 0.3s ease;

  &.active {
    background: rgba(201, 169, 110, 0.15);
    border-color: #C9A96E;
    color: #C9A96E;
    font-weight: 500;
  }
}

.select-all {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.select-all-check {
  font-size: 28rpx;
  color: #C9A96E;
}

.select-all-text {
  font-size: 24rpx;
  color: #8B7355;
}

.field-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.field-item {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12rpx;
}

.field-check {
  font-size: 28rpx;
  color: #C9A96E;
  margin-right: 10rpx;
}

.field-name {
  font-size: 26rpx;
  color: #5D4E37;
}

.export-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #D9C49A 0%, #C9A96E 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 44rpx;
  border: none;
  box-shadow: 0 6rpx 20rpx rgba(201, 169, 110, 0.35);

  &:disabled {
    opacity: 0.6;
  }
}

.export-format {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
}

.format-label {
  font-size: 26rpx;
  color: #5D4E37;
  margin-right: 16rpx;
}

.format-options {
  flex: 1;
  display: flex;
  gap: 12rpx;
}

.format-item {
  flex: 1;
  height: 68rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #8B7355;
  border: 2rpx solid #F0EBE2;
  transition: all 0.3s ease;

  &.active {
    background: rgba(201, 169, 110, 0.15);
    border-color: #C9A96E;
    color: #C9A96E;
    font-weight: 500;
  }
}

.task-count {
  font-size: 24rpx;
  color: #8B7355;
}

.empty-tip {
  text-align: center;
  padding: 40rpx 0;
  color: #B5A896;
  font-size: 26rpx;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16rpx;
}

.task-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.task-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.task-name {
  font-size: 26rpx;
  color: #5D4E37;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-time {
  font-size: 22rpx;
  color: #B5A896;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 4rpx;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background: #F0EBE2;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #D9C49A, #C9A96E);
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 20rpx;
  color: #C9A96E;
  flex-shrink: 0;
}

.task-action {
  margin-left: 12rpx;
  flex-shrink: 0;
}

.action-btn {
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;

  &.download {
    background: rgba(201, 169, 110, 0.15);
    color: #C9A96E;
  }

  &.retry {
    background: rgba(93, 156, 167, 0.15);
    color: #5D9CA7;
  }
}

.task-status-text {
  font-size: 24rpx;
  color: #8B7355;
}
</style>
