<template>
  <app-page-layout title="数据导入" :showBack="true">
    <view class="import-page">
      <view class="card type-card">
        <view class="card-header">
          <text class="card-title">📂 选择数据类型</text>
        </view>
        <view class="type-list">
          <view
            v-for="(t, i) in boTypes"
            :key="i"
            class="type-item"
            :class="{ active: selectedType === t.value }"
            @tap="selectedType = t.value">
            <text class="type-icon">{{ t.icon }}</text>
            <view class="type-info">
              <text class="type-name">{{ t.label }}</text>
              <text class="type-desc">{{ t.desc }}</text>
            </view>
            <view class="type-check">
              <text v-if="selectedType === t.value">✓</text>
            </view>
          </view>
        </view>
      </view>

      <view class="card file-card">
        <view class="card-header">
          <text class="card-title">📄 选择文件</text>
        </view>
        <view class="file-upload-area" @tap="chooseFile">
          <view v-if="!selectedFile" class="upload-placeholder">
            <text class="upload-icon">📁</text>
            <text class="upload-text">点击选择 Excel / CSV 文件</text>
            <text class="upload-hint">支持 .xlsx, .xls, .csv 格式</text>
          </view>
          <view v-else class="file-info">
            <text class="file-icon">📊</text>
            <view class="file-detail">
              <text class="file-name">{{ selectedFile.name }}</text>
              <text class="file-size">{{ formatSize(selectedFile.size) }}</text>
            </view>
            <view class="file-remove" @tap.stop="removeFile">×</view>
          </view>
        </view>
      </view>

      <view v-if="selectedFile && importResult === null" class="card action-card">
        <zrws-button variant="primary" size="lg" block :loading="importing" @click="startImport">
          {{ importing ? '导入中...' : '🚀 开始导入' }}
        </zrws-button>
        <view class="import-tips">
          <text class="tips-title">💡 导入说明</text>
          <text class="tips-item">• 请确保文件格式符合模板要求</text>
          <text class="tips-item">• 重复数据将自动跳过</text>
          <text class="tips-item">• 导入过程请勿关闭页面</text>
        </view>
      </view>

      <view v-if="importResult" class="card result-card">
        <view class="card-header">
          <text class="card-title">📊 导入结果</text>
        </view>
        <view class="result-stats">
          <view class="result-item success">
            <text class="result-num">{{ importResult.success }}</text>
            <text class="result-label">成功</text>
          </view>
          <view class="result-item failed">
            <text class="result-num">{{ importResult.failed }}</text>
            <text class="result-label">失败</text>
          </view>
          <view class="result-item total">
            <text class="result-num">{{ importResult.total }}</text>
            <text class="result-label">总计</text>
          </view>
        </view>
        <view v-if="importResult.errors && importResult.errors.length > 0" class="error-detail">
          <view class="error-header" @tap="showErrors = !showErrors">
            <text class="error-title">错误详情 ({{ importResult.errors.length }})</text>
            <text class="error-arrow">{{ showErrors ? '▲' : '▼' }}</text>
          </view>
          <view v-if="showErrors" class="error-list">
            <view v-for="(e, i) in importResult.errors.slice(0, 10)" :key="i" class="error-item">
              <text class="error-row">第{{ e.row }}行</text>
              <text class="error-msg">{{ e.message }}</text>
            </view>
            <view v-if="importResult.errors.length > 10" class="error-more">
              还有 {{ importResult.errors.length - 10 }} 条错误...
            </view>
          </view>
        </view>
        <zrws-button variant="outline" size="md" block @click="resetImport">
          重新导入
        </zrws-button>
      </view>

      <view class="card history-card">
        <view class="card-header">
          <text class="card-title">📋 历史记录</text>
          <text class="history-count">最近 {{ historyList.length }} 次</text>
        </view>
        <view v-if="historyList.length === 0" class="empty-tip">暂无导入记录</view>
        <view v-else class="history-list">
          <view v-for="(h, i) in historyList" :key="i" class="history-item">
            <text class="h-icon">{{ h.success > 0 ? '✅' : '❌' }}</text>
            <view class="h-info">
              <text class="h-name">{{ h.fileName }}</text>
              <text class="h-time">{{ h.time }}</text>
            </view>
            <view class="h-result">
              <text class="h-success">{{ h.success }}成功</text>
              <text v-if="h.failed > 0" class="h-failed">{{ h.failed }}失败</text>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;"></view>
    </view>
  </app-page-layout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { toast, formatDate } from '@/utils/index.js'

const selectedType = ref('soil_sample')
const selectedFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const showErrors = ref(false)

const boTypes = [
  { value: 'soil_sample', label: '土壤采样数据', icon: '🌱', desc: '土壤样本采集记录' },
  { value: 'rock_sample', label: '岩石采样数据', icon: '🪨', desc: '岩石样本采集记录' },
  { value: 'disaster_record', label: '灾害记录数据', icon: '⚠️', desc: '地质灾害记录' },
  { value: 'device_data', label: '设备数据', icon: '🔧', desc: '设备监测数据' },
  { value: 'mission_data', label: '任务数据', icon: '📋', desc: '任务相关数据' }
]

const historyList = ref([
  { fileName: '土壤采样2024Q1.xlsx', time: '2024-03-15 14:30', success: 128, failed: 3 },
  { fileName: '岩石样本汇总.csv', time: '2024-03-10 09:15', success: 56, failed: 0 },
  { fileName: '灾害记录批量导入.xlsx', time: '2024-03-05 16:45', success: 34, failed: 8 }
])

function chooseFile() {
  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.xlsx', '.xls', '.csv'],
    success: (res) => {
      const file = res.tempFiles[0]
      selectedFile.value = {
        name: file.name,
        size: file.size,
        path: file.path
      }
      importResult.value = null
    },
    fail: () => {
      // 用户取消或不支持
      toast.info('请选择有效的文件')
    }
  })
}

function removeFile() {
  selectedFile.value = null
  importResult.value = null
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

async function startImport() {
  if (!selectedType.value) {
    toast.error('请选择数据类型')
    return
  }
  if (!selectedFile.value) {
    toast.error('请选择文件')
    return
  }

  importing.value = true
  toast.loading('导入中...')

  setTimeout(() => {
    importing.value = false
    toast.hide()

    importResult.value = {
      total: 87,
      success: 82,
      failed: 5,
      errors: [
        { row: 12, message: '经度格式不正确' },
        { row: 25, message: '采样深度不能为空' },
        { row: 47, message: '日期格式错误' },
        { row: 63, message: '土壤类型不存在' },
        { row: 78, message: '缺少必填字段：采集人' }
      ]
    }

    historyList.value.unshift({
      fileName: selectedFile.value.name,
      time: formatDate(new Date(), 'MM-DD HH:mm'),
      success: 82,
      failed: 5
    })

    if (importResult.value.failed > 0) {
      toast.error(`导入完成，${importResult.value.failed} 条失败`)
    } else {
      toast.success('导入成功')
    }
  }, 2000)
}

function resetImport() {
  selectedFile.value = null
  importResult.value = null
  showErrors.value = false
}
</script>

<style lang="scss" scoped>
.import-page {
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

.type-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.type-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;

  &.active {
    background: rgba(201, 169, 110, 0.1);
    border-color: #C9A96E;
  }
}

.type-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.type-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.type-name {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 500;
}

.type-desc {
  font-size: 22rpx;
  color: #8B7355;
}

.type-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #C9A96E;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
}

.file-upload-area {
  min-height: 200rpx;
  border: 2rpx dashed #D8CFBE;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-placeholder {
  text-align: center;
  padding: 40rpx 0;
}

.upload-icon {
  display: block;
  font-size: 56rpx;
  margin-bottom: 12rpx;
}

.upload-text {
  display: block;
  font-size: 28rpx;
  color: #5D4E37;
  margin-bottom: 8rpx;
}

.upload-hint {
  font-size: 22rpx;
  color: #B5A896;
}

.file-info {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 20rpx;
}

.file-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.file-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.file-name {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 22rpx;
  color: #8B7355;
}

.file-remove {
  width: 48rpx;
  height: 48rpx;
  background: rgba(229, 57, 53, 0.1);
  color: #E53935;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  line-height: 1;
}



.import-tips {
  margin-top: 24rpx;
  padding: 20rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16rpx;
}

.tips-title {
  display: block;
  font-size: 26rpx;
  color: #5D4E37;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.tips-item {
  display: block;
  font-size: 24rpx;
  color: #8B7355;
  line-height: 1.8;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
  border-bottom: 1rpx solid #F0EBE2;
}

.result-item {
  text-align: center;

  &.success .result-num { color: #7CB342; }
  &.failed .result-num { color: #E53935; }
  &.total .result-num { color: #5D4E37; }
}

.result-num {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 6rpx;
  font-family: monospace;
}

.result-label {
  font-size: 24rpx;
  color: #8B7355;
}

.error-detail {
  margin-bottom: 20rpx;
}

.error-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.error-title {
  font-size: 26rpx;
  color: #E53935;
  font-weight: 500;
}

.error-arrow {
  font-size: 22rpx;
  color: #8B7355;
}

.error-list {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12rpx;
  padding: 12rpx;
  max-height: 300rpx;
  overflow-y: auto;
}

.error-item {
  display: flex;
  padding: 8rpx 0;
  border-bottom: 1rpx solid #F0EBE2;

  &:last-child { border-bottom: none; }
}

.error-row {
  font-size: 24rpx;
  color: #8B7355;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.error-msg {
  font-size: 24rpx;
  color: #E53935;
  flex: 1;
}

.error-more {
  text-align: center;
  font-size: 22rpx;
  color: #B5A896;
  padding: 12rpx 0 4rpx;
}



.history-count {
  font-size: 24rpx;
  color: #8B7355;
}

.empty-tip {
  text-align: center;
  padding: 40rpx 0;
  color: #B5A896;
  font-size: 26rpx;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12rpx;
}

.h-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.h-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.h-name {
  font-size: 26rpx;
  color: #5D4E37;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.h-time {
  font-size: 22rpx;
  color: #B5A896;
}

.h-result {
  text-align: right;
}

.h-success {
  display: block;
  font-size: 24rpx;
  color: #7CB342;
  font-weight: 500;
}

.h-failed {
  font-size: 20rpx;
  color: #E53935;
}
</style>
