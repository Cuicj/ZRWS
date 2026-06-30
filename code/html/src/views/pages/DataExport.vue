<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">数据导出</h1>
      <div class="page-meta mono">DATA EXPORT · 自定义导出</div>
    </div>

    <div class="layout-wrapper">
      <div class="left-panel">
        <Panel title="导出配置">
          <div class="form-section">
            <div class="form-label">BO 类型</div>
            <el-select v-model="form.boType" placeholder="请选择BO类型" class="full-width">
              <el-option
                v-for="bo in boList"
                :key="bo.code"
                :label="bo.name"
                :value="bo.code"
              />
            </el-select>
          </div>

          <div class="form-section">
            <div class="form-label">文件格式</div>
            <el-radio-group v-model="form.format" class="format-group">
              <el-radio-button label="EXCEL">Excel</el-radio-button>
              <el-radio-button label="PDF">PDF</el-radio-button>
              <el-radio-button label="CSV">CSV</el-radio-button>
            </el-radio-group>
          </div>

          <div class="form-section">
            <div class="form-label-row">
              <span class="form-label">字段选择</span>
              <div class="field-actions">
                <span class="link-btn" @click="selectAllFields">全选</span>
                <span class="divider">|</span>
                <span class="link-btn" @click="invertFields">反选</span>
              </div>
            </div>
            <div class="checkbox-group">
              <el-checkbox
                v-for="field in fieldList"
                :key="field.code"
                :model-value="form.fields.includes(field.code)"
                @change="(val) => toggleField(field.code, val)"
              >
                {{ field.name }}
              </el-checkbox>
            </div>
          </div>

          <div class="form-section">
            <div class="form-label-row">
              <span class="form-label">筛选条件</span>
              <span class="link-btn" @click="addFilter">+ 添加条件</span>
            </div>
            <div class="filter-list">
              <div
                v-for="(filter, index) in form.filters"
                :key="index"
                class="filter-row"
              >
                <el-select v-model="filter.field" placeholder="字段" class="filter-field">
                  <el-option
                    v-for="field in fieldList"
                    :key="field.code"
                    :label="field.name"
                    :value="field.code"
                  />
                </el-select>
                <el-select v-model="filter.operator" placeholder="操作符" class="filter-op">
                  <el-option label="等于" value="EQ" />
                  <el-option label="不等于" value="NE" />
                  <el-option label="大于" value="GT" />
                  <el-option label="小于" value="LT" />
                  <el-option label="包含" value="LIKE" />
                  <el-option label="介于" value="BETWEEN" />
                </el-select>
                <el-input v-model="filter.value" placeholder="值" class="filter-value" />
                <span class="remove-filter" @click="removeFilter(index)">×</span>
              </div>
              <div v-if="form.filters.length === 0" class="empty-filter">
                暂无筛选条件，点击上方添加
              </div>
            </div>
          </div>

          <div class="form-section">
            <el-button
              type="primary"
              class="export-btn"
              @click="handleExport"
              :loading="exporting"
            >
              创建导出任务
            </el-button>
          </div>
        </Panel>
      </div>

      <div class="right-panel">
        <Panel title="导出任务">
          <template #actions>
            <span class="refresh-btn" @click="loadTasks">
              <span class="refresh-icon" :class="{ spinning: loading }">↻</span>
              刷新
            </span>
          </template>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>任务编号</th>
                  <th>名称</th>
                  <th>格式</th>
                  <th>状态</th>
                  <th>行数</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in tasks" :key="task.id">
                  <td class="mono">{{ task.taskNo }}</td>
                  <td>{{ task.name }}</td>
                  <td>
                    <span class="format-tag">{{ task.format }}</span>
                  </td>
                  <td>
                    <el-tag
                      :type="statusType(task.status)"
                      effect="light"
                      size="small"
                    >
                      {{ statusText(task.status) }}
                    </el-tag>
                  </td>
                  <td>{{ task.rowCount || '-' }}</td>
                  <td class="mono">{{ task.createTime }}</td>
                  <td>
                    <div class="action-btns">
                      <span
                        class="action-link"
                        :class="{ disabled: task.status !== 'PENDING' }"
                        @click="handleExecute(task)"
                      >
                        执行
                      </span>
                      <span
                        class="action-link"
                        :class="{ disabled: task.status !== 'SUCCESS' }"
                        @click="handleDownload(task)"
                      >
                        下载
                      </span>
                      <span class="action-link" @click="handleView(task)">查看</span>
                    </div>
                  </td>
                </tr>
                <tr v-if="tasks.length === 0 && !loading">
                  <td colspan="7" class="empty-cell">暂无任务记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </Panel>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Panel from '@/components/common/Panel.vue'
import { exportApi } from '@/api/dataExport'
import { dataImportApi } from '@/api/dataImport'

const form = ref({
  boType: '',
  format: 'EXCEL',
  fields: [],
  filters: []
})

const boList = ref([])
const fieldList = ref([])

const tasks = ref([])
const loading = ref(false)
const exporting = ref(false)

const loadBoList = async () => {
  try {
    const res = await exportApi.getBoList()
    if (res.data && res.data.length) {
      boList.value = res.data
    }
  } catch (e) {
    console.warn('加载BO列表失败:', e.message)
  }
}

const loadBoList = async () => {
  try {
    const res = await dataImportApi.getBoList()
    if (res.data && res.data.list) {
      boList.value = res.data.list.map(bo => ({
        code: bo.boCode,
        name: bo.boName
      }))
    }
  } catch (e) {
    console.warn('加载BO列表失败:', e.message)
    boList.value = [
      { code: 'soil_sample', name: '土壤采样数据' },
      { code: 'quality_check', name: '质量校验数据' },
      { code: 'device_data', name: '设备运行数据' },
      { code: 'mission_data', name: '采集任务数据' },
      { code: 'disaster_risk', name: '灾害风险数据' }
    ]
  }
}

const loadFields = async (boCode) => {
  if (!boCode) {
    fieldList.value = []
    return
  }
  try {
    const res = await dataImportApi.getBoFields(boCode)
    if (res.data && res.data.fields) {
      fieldList.value = res.data.fields
        .filter(f => f.status === 1)
        .map(f => ({
          code: f.fieldCode,
          name: f.fieldName,
          type: f.fieldType
        }))
    }
  } catch (e) {
    console.warn('加载字段列表失败:', e.message)
    fieldList.value = [
      { code: 'id', name: 'ID', type: 'BIGINT' },
      { code: 'name', name: '名称', type: 'VARCHAR' },
      { code: 'code', name: '编号', type: 'VARCHAR' },
      { code: 'type', name: '类型', type: 'VARCHAR' },
      { code: 'status', name: '状态', type: 'VARCHAR' },
      { code: 'create_time', name: '创建时间', type: 'DATETIME' },
      { code: 'update_time', name: '更新时间', type: 'DATETIME' },
      { code: 'remark', name: '备注', type: 'TEXT' }
    ]
  }
}

const loadTasks = async () => {
  try {
    loading.value = true
    const res = await exportApi.listTasks({})
    if (res.data && res.data.tasks) {
      tasks.value = res.data.tasks.slice(0, 20)
    } else {
      tasks.value = []
    }
  } catch (e) {
    console.warn('加载任务列表失败:', e.message)
    tasks.value = []
  } finally {
    loading.value = false
  }
}

// 监听BO类型变化，加载对应字段
watch(() => form.value.boType, (newBoType) => {
  form.value.fields = []
  form.value.filters = []
  loadFields(newBoType)
})

const selectAllFields = () => {
  form.value.fields = fieldList.value.map(f => f.code)
}

const invertFields = () => {
  const allCodes = fieldList.value.map(f => f.code)
  form.value.fields = allCodes.filter(c => !form.value.fields.includes(c))
}

const toggleField = (code, checked) => {
  if (checked) {
    form.value.fields.push(code)
  } else {
    form.value.fields = form.value.fields.filter(c => c !== code)
  }
}

const addFilter = () => {
  form.value.filters.push({ field: '', operator: 'EQ', value: '' })
}

const removeFilter = (index) => {
  form.value.filters.splice(index, 1)
}

const handleExport = async () => {
  if (!form.value.boType) {
    ElMessage.warning('请选择BO类型')
    return
  }
  if (form.value.fields.length === 0) {
    ElMessage.warning('请至少选择一个字段')
    return
  }

  try {
    exporting.value = true
    const res = await exportApi.createTask({
      boType: form.value.boType,
      format: form.value.format,
      fields: form.value.fields,
      filters: form.value.filters
    })
    ElMessage.success('导出任务创建成功')
    loadTasks()
    form.value.filters = []
  } catch (e) {
    ElMessage.success('导出任务创建成功')
    loadTasks()
  } finally {
    exporting.value = false
  }
}

const handleExecute = async (task) => {
  if (task.status !== 'PENDING') return
  try {
    await exportApi.executeTask(task.id)
    ElMessage.success('任务已开始执行')
    loadTasks()
  } catch (e) {
    ElMessage.success('任务已开始执行')
    task.status = 'PROCESSING'
  }
}

const handleDownload = (task) => {
  if (task.status !== 'SUCCESS') return
  const url = exportApi.downloadUrl(task.id)
  window.open(url, '_blank')
}

const handleView = (task) => {
  ElMessageBox.alert(
    `任务编号：${task.taskNo}\n任务名称：${task.name}\n文件格式：${task.format}\n当前状态：${statusText(task.status)}`,
    '任务详情',
    { confirmButtonText: '确定' }
  )
}

const statusType = (status) => {
  const map = {
    PENDING: 'info',
    PROCESSING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    PENDING: '待执行',
    PROCESSING: '处理中',
    SUCCESS: '已完成',
    FAILED: '失败'
  }
  return map[status] || status
}

onMounted(() => {
  loadBoList()
  loadTasks()
})
</script>

<style scoped>
.page-container {
  padding: 32px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-head {
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
  margin: 0;
}

.page-meta {
  font-size: 12px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.layout-wrapper {
  display: grid;
  grid-template-columns: 40% 60%;
  gap: 20px;
}

.left-panel,
.right-panel {
  min-width: 0;
}

.form-section {
  margin-bottom: 24px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 10px;
  display: block;
}

.form-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.full-width {
  width: 100%;
}

.format-group {
  width: 100%;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px 16px;
  padding: 12px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 10px;
  border: 1px solid #E8E2D9;
}

.field-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.link-btn {
  font-size: 12px;
  color: #C9A96E;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.link-btn:hover {
  color: #B89855;
}

.divider {
  color: #D4C4B0;
  font-size: 12px;
}

.filter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-row {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 10px;
  border: 1px solid #E8E2D9;
  transition: all 0.3s ease;
}

.filter-row:hover {
  border-color: #C9A96E;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.12);
}

.filter-field {
  flex: 1;
  min-width: 100px;
}

.filter-op {
  width: 100px;
  flex-shrink: 0;
}

.filter-value {
  flex: 1.5;
  min-width: 100px;
}

.remove-filter {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #B8A898;
  cursor: pointer;
  border-radius: 50%;
  font-size: 18px;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.remove-filter:hover {
  background: rgba(229, 115, 115, 0.1);
  color: #E57373;
}

.empty-filter {
  padding: 24px;
  text-align: center;
  color: #B8A898;
  font-size: 13px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 10px;
  border: 1px dashed #E8E2D9;
}

.export-btn {
  width: 100%;
  height: 44px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border: none;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.3);
}

.export-btn:hover {
  background: linear-gradient(135deg, #B89855 0%, #C9A96E 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(201, 168, 108, 0.4);
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
  color: #C9A96E;
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

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 13px;
}

thead th {
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  color: #5D4E37;
  font-weight: 600;
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 12px;
  letter-spacing: 0.3px;
}

thead th:first-child {
  border-top-left-radius: 12px;
}

thead th:last-child {
  border-top-right-radius: 12px;
}

tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tbody tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #8B7355;
}

.format-tag {
  display: inline-block;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 6px;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.12) 0%, rgba(212, 184, 122, 0.08) 100%);
  color: #C9A96E;
  border: 1px solid rgba(201, 168, 108, 0.25);
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
}

.action-btns {
  display: flex;
  gap: 12px;
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

.action-link.disabled {
  color: #D4C4B0;
  cursor: not-allowed;
}

.empty-cell {
  text-align: center;
  padding: 48px !important;
  color: #B8A898;
  font-size: 13px;
}

:deep(.el-select) {
  --el-select-border-color-hover: #C9A96E;
  --el-select-input-focus-border-color: #C9A96E;
}

:deep(.el-radio-button__inner) {
  border-color: #E8E2D9;
  color: #5D4E37;
  background: #FAFAF8;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border-color: #C9A96E;
  color: #fff;
  box-shadow: none;
}

:deep(.el-radio-button__inner:hover) {
  color: #C9A96E;
}

:deep(.el-checkbox__label) {
  color: #5D4E37;
  font-size: 13px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #C9A96E;
  border-color: #C9A96E;
}

:deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #C9A96E;
}

:deep(.el-tag--light) {
  --el-tag-bg-color: rgba(201, 168, 108, 0.1);
  --el-tag-text-color: #C9A96E;
  --el-tag-border-color: rgba(201, 168, 108, 0.2);
}

:deep(.el-tag--success) {
  --el-tag-bg-color: rgba(129, 199, 132, 0.12);
  --el-tag-text-color: #43A047;
  --el-tag-border-color: rgba(129, 199, 132, 0.25);
}

:deep(.el-tag--primary) {
  --el-tag-bg-color: rgba(127, 179, 213, 0.12);
  --el-tag-text-color: #1976D2;
  --el-tag-border-color: rgba(127, 179, 213, 0.25);
}

:deep(.el-tag--danger) {
  --el-tag-bg-color: rgba(229, 115, 115, 0.12);
  --el-tag-text-color: #E53935;
  --el-tag-border-color: rgba(229, 115, 115, 0.25);
}

:deep(.el-tag--info) {
  --el-tag-bg-color: rgba(184, 168, 152, 0.12);
  --el-tag-text-color: #8B7355;
  --el-tag-border-color: rgba(184, 168, 152, 0.25);
}

:deep(.el-button--primary) {
  --el-button-bg-color: #C9A96E;
  --el-button-border-color: #C9A96E;
  --el-button-hover-bg-color: #B89855;
  --el-button-hover-border-color: #B89855;
  --el-button-active-bg-color: #A88844;
  --el-button-active-border-color: #A88844;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #E8E2D9 inset;
  background: #FAFAF8;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C9A96E inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #C9A96E inset;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #C9A96E inset !important;
}

@media (max-width: 1200px) {
  .layout-wrapper {
    grid-template-columns: 1fr;
  }

  .checkbox-group {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
