<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">数据导入</h1>
      <div class="page-meta mono">DATA IMPORT · Excel文件导入</div>
    </div>

    <!-- BO类型选择 -->
    <Panel title="导入配置">
      <div class="config-section">
        <div class="config-row">
          <div class="config-label">BO类型</div>
          <el-select v-model="config.boType" placeholder="请选择数据类型" class="config-select" @change="onBoTypeChange">
            <el-option
              v-for="bo in boList"
              :key="bo.boCode"
              :label="bo.boName"
              :value="bo.boCode"
            />
          </el-select>
        </div>

        <div class="config-row">
          <div class="config-label">导入模式</div>
          <el-radio-group v-model="config.importMode" class="import-mode-group">
            <el-radio-button label="INSERT">仅新增</el-radio-button>
            <el-radio-button label="UPDATE">仅更新</el-radio-button>
            <el-radio-button label="INSERT_UPDATE">新增或更新</el-radio-button>
          </el-radio-group>
        </div>

        <div class="config-row">
          <div class="config-label">AI辅助</div>
          <el-switch v-model="config.useAiMapping" active-text="启用智能字段映射" />
        </div>
      </div>
    </Panel>

    <!-- 上传区域 -->
    <Panel title="文件上传">
      <div class="upload-zone" @dragover.prevent @drop.prevent="handleDrop">
        <div class="upload-icon">↧</div>
        <div class="upload-text">
          <span v-if="!selectedFile">拖拽Excel文件到此处，或点击选择</span>
          <span v-else>已选择: {{ selectedFile.name }}</span>
        </div>
        <div class="upload-hint">支持 .xlsx, .xls 格式，建议文件大小不超过50MB</div>
        <input type="file" id="fileInput" ref="fileInputRef" accept=".xlsx,.xls" @change="handleSelect" hidden />
        <button class="btn-ghost" @click="triggerUpload" :disabled="!config.boType">
          {{ selectedFile ? '重新选择' : '选择文件' }}
        </button>
        <button
          v-if="selectedFile && config.boType"
          class="btn-primary"
          @click="handleUpload"
          :loading="uploading"
          :disabled="uploading"
        >
          {{ uploading ? '上传分析中...' : '上传并分析' }}
        </button>
      </div>
    </Panel>

    <!-- 分析结果 -->
    <Panel v-if="analysisResult" title="数据分析结果">
      <template #actions>
        <span class="result-badge" :class="getStatusClass(analysisResult)">
          {{ getStatusText(analysisResult) }}
        </span>
      </template>

      <!-- 统计信息 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ analysisResult.totalRows || 0 }}</div>
          <div class="stat-label">总行数</div>
        </div>
        <div class="stat-card success">
          <div class="stat-value">{{ analysisResult.validationSummary?.passedValidations || 0 }}</div>
          <div class="stat-label">通过</div>
        </div>
        <div class="stat-card danger">
          <div class="stat-value">{{ analysisResult.validationSummary?.failedValidations || 0 }}</div>
          <div class="stat-label">失败</div>
        </div>
        <div class="stat-card warning">
          <div class="stat-value">{{ analysisResult.validationSummary?.warningValidations || 0 }}</div>
          <div class="stat-label">警告</div>
        </div>
      </div>

      <!-- 数据预览 -->
      <div v-if="analysisResult.dataPreview && analysisResult.dataPreview.length > 0" class="preview-section">
        <div class="preview-title">数据预览（前10行）</div>
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th v-for="header in getPreviewHeaders()" :key="header">{{ header }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in analysisResult.dataPreview" :key="index">
                <td class="row-num">{{ index + 1 }}</td>
                <td v-for="header in getPreviewHeaders()" :key="header">{{ row[header] }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- AI字段映射 -->
      <div v-if="analysisResult.fieldMappings && analysisResult.fieldMappings.length > 0" class="mapping-section">
        <div class="section-title">AI智能字段映射</div>
        <div class="mapping-list">
          <div v-for="mapping in analysisResult.fieldMappings" :key="mapping.sourceField" class="mapping-item">
            <span class="source-field">{{ mapping.sourceField }}</span>
            <span class="mapping-arrow">→</span>
            <span class="target-field" :class="{ 'no-mapping': !mapping.targetField }">
              {{ mapping.targetField || '(未映射)' }}
            </span>
            <span class="confidence" v-if="mapping.confidence">
              置信度: {{ (mapping.confidence * 100).toFixed(0) }}%
            </span>
          </div>
        </div>
      </div>

      <!-- 导入按钮 -->
      <div class="action-section">
        <el-button type="primary" size="large" @click="handleImport" :loading="importing" :disabled="importing">
          {{ importing ? '导入中...' : '确认导入数据' }}
        </el-button>
      </div>
    </Panel>

    <!-- 处理队列 -->
    <Panel title="导入记录">
      <template #actions>
        <span class="refresh-btn" @click="loadBatches">
          <span class="refresh-icon" :class="{ spinning: loading }">↻</span>
          刷新
        </span>
      </template>

      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>批次号</th>
              <th>数据类型</th>
              <th>文件名</th>
              <th>总行数</th>
              <th>成功</th>
              <th>失败</th>
              <th>状态</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="batch in batches" :key="batch.batchId">
              <td class="mono">{{ batch.batchNo }}</td>
              <td>{{ batch.boCode }}</td>
              <td class="filename-cell" :title="batch.fileName">{{ batch.fileName }}</td>
              <td>{{ batch.totalRows }}</td>
              <td class="text-success">{{ batch.successRows || 0 }}</td>
              <td class="text-danger">{{ batch.failedRows || 0 }}</td>
              <td><span class="status-badge" :class="getBatchStatusClass(batch.status)">{{ getBatchStatusText(batch.status) }}</span></td>
              <td class="mono">{{ formatTime(batch.createdTime) }}</td>
              <td>
                <span class="action-link" @click="viewBatchDetail(batch)">详情</span>
              </td>
            </tr>
            <tr v-if="batches.length === 0 && !loading">
              <td colspan="9" class="empty-cell">暂无导入记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </Panel>

    <!-- 批次详情弹窗 -->
    <el-dialog v-model="showBatchDetail" title="导入详情" width="700px">
      <div v-if="currentBatch" class="batch-detail">
        <div class="detail-row">
          <span class="detail-label">批次号:</span>
          <span class="detail-value mono">{{ currentBatch.batchNo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">数据类型:</span>
          <span class="detail-value">{{ currentBatch.boCode }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">总行数:</span>
          <span class="detail-value">{{ currentBatch.totalRows }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">成功:</span>
          <span class="detail-value text-success">{{ currentBatch.successRows || 0 }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">失败:</span>
          <span class="detail-value text-danger">{{ currentBatch.failedRows || 0 }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">跳过:</span>
          <span class="detail-value">{{ currentBatch.skippedRows || 0 }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态:</span>
          <span class="status-badge" :class="getBatchStatusClass(currentBatch.status)">
            {{ getBatchStatusText(currentBatch.status) }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">耗时:</span>
          <span class="detail-value">{{ currentBatch.duration ? currentBatch.duration.toFixed(2) + '秒' : '-' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Panel from '@/components/common/Panel.vue'
import { dataImportApi } from '@/api/dataImport'

const config = ref({
  boType: '',
  importMode: 'INSERT_UPDATE',
  useAiMapping: true
})

const boList = ref([])
const selectedFile = ref(null)
const fileInputRef = ref(null)
const uploading = ref(false)
const importing = ref(false)
const loading = ref(false)
const analysisResult = ref(null)
const currentBatchId = ref(null)
const batches = ref([])
const showBatchDetail = ref(false)
const currentBatch = ref(null)

const loadBoList = async () => {
  try {
    const res = await dataImportApi.getBoList()
    if (res.data && res.data.list) {
      boList.value = res.data.list
    }
  } catch (e) {
    console.warn('加载BO列表失败，使用默认列表:', e.message)
    boList.value = [
      { boCode: 'SOIL_SAMPLE', boName: '土壤采样数据' },
      { boCode: 'QUALITY_CHECK', boName: '质量校验数据' },
      { boCode: 'DEVICE_DATA', boName: '设备运行数据' },
      { boCode: 'MISSION_DATA', boName: '采集任务数据' },
      { boCode: 'DISASTER_RISK', boName: '灾害风险数据' }
    ]
  }
}

const loadBatches = async () => {
  try {
    loading.value = true
    const res = await dataImportApi.getBatches({})
    if (res.data && res.data.batches) {
      batches.value = res.data.batches.slice(0, 20)
    }
  } catch (e) {
    console.warn('加载批次列表失败:', e.message)
    batches.value = []
  } finally {
    loading.value = false
  }
}

const onBoTypeChange = () => {
  analysisResult.value = null
  selectedFile.value = null
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleSelect = (e) => {
  const files = e.target.files
  if (files.length > 0) {
    selectedFile.value = files[0]
    analysisResult.value = null
  }
}

const handleDrop = (e) => {
  const files = e.dataTransfer.files
  if (files.length > 0) {
    const file = files[0]
    if (file.name.match(/\.(xlsx|xls)$/i)) {
      selectedFile.value = file
      analysisResult.value = null
    } else {
      ElMessage.warning('请上传Excel文件 (.xlsx, .xls)')
    }
  }
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  if (!config.value.boType) {
    ElMessage.warning('请先选择数据类型')
    return
  }

  try {
    uploading.value = true
    const res = await dataImportApi.uploadAndAnalyze(
      selectedFile.value,
      config.value.boType,
      config.value.useAiMapping
    )
    if (res.data) {
      analysisResult.value = res.data
      currentBatchId.value = res.data.batchId
      ElMessage.success('文件分析完成')
    }
  } catch (e) {
    ElMessage.error('文件上传分析失败: ' + e.message)
  } finally {
    uploading.value = false
  }
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先上传并分析文件')
    return
  }

  try {
    importing.value = true
    const res = await dataImportApi.uploadAndImport(
      selectedFile.value,
      config.value.boType,
      config.value.importMode,
      config.value.useAiMapping
    )
    if (res.data) {
      ElMessage.success(`导入完成！成功: ${res.data.successRows}, 失败: ${res.data.failedRows}`)
      analysisResult.value = null
      selectedFile.value = null
      loadBatches()
    }
  } catch (e) {
    ElMessage.error('导入失败: ' + e.message)
  } finally {
    importing.value = false
  }
}

const viewBatchDetail = async (batch) => {
  currentBatch.value = batch
  showBatchDetail.value = true
}

const getPreviewHeaders = () => {
  if (!analysisResult.value?.dataPreview?.length) return []
  return Object.keys(analysisResult.value.dataPreview[0])
}

const getStatusClass = (result) => {
  if (!result?.validationSummary) return ''
  const failed = result.validationSummary.failedValidations || 0
  if (failed === 0) return 'status-success'
  if (failed < result.validationSummary.totalValidations * 0.1) return 'status-warning'
  return 'status-danger'
}

const getStatusText = (result) => {
  if (!result?.validationSummary) return '待分析'
  const failed = result.validationSummary.failedValidations || 0
  if (failed === 0) return '分析通过'
  return `发现 ${failed} 个问题`
}

const getBatchStatusClass = (status) => {
  const map = {
    PENDING: 'status-pending',
    PROCESSING: 'status-processing',
    SUCCESS: 'status-success',
    PARTIAL: 'status-warning',
    FAILED: 'status-danger'
  }
  return map[status] || 'status-pending'
}

const getBatchStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '已完成',
    PARTIAL: '部分成功',
    FAILED: '失败'
  }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadBoList()
  loadBatches()
})
</script>

<style scoped>
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-head {
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
  font-family: var(--font-display);
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
  letter-spacing: 0.1em;
  font-weight: 500;
}

.config-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.config-label {
  min-width: 80px;
  font-size: 13px;
  font-weight: 600;
  color: #5D4E37;
}

.config-select {
  width: 300px;
}

.import-mode-group {
  display: flex;
  gap: 8px;
}

.upload-zone {
  padding: var(--s-8);
  border: 2px dashed #D4C4B0;
  text-align: center;
  cursor: pointer;
  border-radius: 12px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-zone:hover {
  border-color: #C9A86C;
  background: linear-gradient(135deg, #FFFBF5 0%, #FAF6EF 100%);
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.15);
}

.upload-icon {
  font-size: 48px;
  color: #B8A898;
  margin-bottom: var(--s-4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-zone:hover .upload-icon {
  color: #C9A86C;
  transform: translateY(-4px);
}

.upload-text {
  font-size: 14px;
  color: #8B7355;
  margin-bottom: 8px;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #B8A898;
  margin-bottom: var(--s-4);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  padding: 10px 24px;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 8px;
  background: transparent;
  border: 1px solid #E8E2D9;
  color: #5D4E37;
  margin-right: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-ghost:hover:not(:disabled) {
  border-color: #C9A86C;
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.08);
}

.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  padding: 10px 24px;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 8px;
  background: linear-gradient(135deg, #C9A86C 0%, #D4B87A 100%);
  border: none;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #B89855 0%, #C9A86C 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(201, 168, 108, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.result-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
}

.result-badge.status-success {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #A5D6A7;
}

.result-badge.status-warning {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #F57C00;
  border: 1px solid #FFCC80;
}

.result-badge.status-danger {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}

.stat-card.success {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-color: #A5D6A7;
}

.stat-card.danger {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  border-color: #EF9A9A;
}

.stat-card.warning {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  border-color: #FFCC80;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #5D4E37;
}

.stat-card.success .stat-value { color: #2E7D32; }
.stat-card.danger .stat-value { color: #C62828; }
.stat-card.warning .stat-value { color: #F57C00; }

.stat-label {
  font-size: 12px;
  color: #8B7355;
  margin-top: 4px;
}

.preview-section, .mapping-section {
  margin-top: 24px;
}

.preview-title, .section-title {
  font-size: 14px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 12px;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #E8E2D9;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

thead th {
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  color: #5D4E37;
  font-weight: 600;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #E8E2D9;
  white-space: nowrap;
}

tbody td {
  padding: 10px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

.row-num {
  color: #B8A898;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}

.filename-cell {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mapping-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mapping-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 8px;
  border: 1px solid #E8E2D9;
}

.source-field {
  font-weight: 500;
  color: #5D4E37;
}

.mapping-arrow {
  color: #C9A86C;
  font-weight: 600;
}

.target-field {
  font-weight: 500;
  color: #2E7D32;
}

.target-field.no-mapping {
  color: #B8A898;
  font-style: italic;
}

.confidence {
  margin-left: auto;
  font-size: 11px;
  color: #8B7355;
}

.action-section {
  margin-top: 24px;
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 12px;
}

.status-badge.status-pending {
  background: linear-gradient(135deg, #F5F5F5 0%, #E0E0E0 100%);
  color: #616161;
}

.status-badge.status-processing {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1565C0;
}

.status-badge.status-success {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
}

.status-badge.status-warning {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #F57C00;
}

.status-badge.status-danger {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8B7355;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.refresh-btn:hover {
  color: #C9A86C;
}

.refresh-icon {
  font-size: 16px;
  display: inline-block;
  transition: transform 0.3s ease;
}

.refresh-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.action-link {
  font-size: 13px;
  color: #C9A96E;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.action-link:hover {
  color: #B89855;
}

.empty-cell {
  text-align: center;
  padding: 48px !important;
  color: #B8A898;
  font-size: 13px;
}

.text-success { color: #2E7D32; }
.text-danger { color: #C62828; }
.mono { font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace; font-size: 12px; }

.batch-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-label {
  min-width: 80px;
  color: #8B7355;
  font-size: 13px;
}

.detail-value {
  color: #5D4E37;
  font-size: 13px;
}

:deep(.el-select) {
  --el-select-border-color-hover: #C9A86C;
  --el-select-input-focus-border-color: #C9A86C;
}

:deep(.el-radio-button__inner) {
  border-color: #E8E2D9;
  color: #5D4E37;
  background: #FAFAF8;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border-color: #C9A86C;
  color: #fff;
  box-shadow: none;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #C9A86C;
  border-color: #C9A86C;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #C9A86C;
  --el-button-border-color: #C9A86C;
  --el-button-hover-bg-color: #B89855;
  --el-button-hover-border-color: #B89855;
}
</style>
