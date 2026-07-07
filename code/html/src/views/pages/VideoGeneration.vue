<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">AI 视频生成</h1>
      <div class="page-meta mono">VIDEO GENERATION · 智能创作</div>
    </div>

    <div class="layout-wrapper">
      <div class="left-panel">
        <Panel title="生成配置">
          <div class="form-section">
            <div class="form-label">生成模式</div>
            <el-radio-group v-model="form.mode" class="mode-group">
              <el-radio-button value="TEXT_TO_VIDEO">文生视频</el-radio-button>
              <el-radio-button value="IMAGE_TO_VIDEO">图生视频</el-radio-button>
            </el-radio-group>
          </div>

          <div class="form-section">
            <div class="form-label">AI 引擎</div>
            <el-select v-model="form.provider" placeholder="请选择AI引擎" class="full-width">
              <el-option
                v-for="p in providerList"
                :key="p.value"
                :label="p.label"
                :value="p.value"
              />
            </el-select>
          </div>

          <div class="form-section">
            <div class="form-label-row">
              <span class="form-label">创意描述</span>
              <span class="word-count">{{ form.prompt.length }}/500</span>
            </div>
            <el-input
              v-model="form.prompt"
              type="textarea"
              :rows="4"
              placeholder="描述你想要生成的视频内容，例如：绿水青山环绕的美丽乡村，夕阳西下，炊烟袅袅..."
              maxlength="500"
              class="prompt-textarea"
            />
            <div class="prompt-suggestions">
              <span class="suggestion-tag" v-for="tag in promptTags" :key="tag" @click="addPromptTag(tag)">
                {{ tag }}
              </span>
            </div>
          </div>

          <div v-if="form.mode === 'IMAGE_TO_VIDEO'" class="form-section">
            <div class="form-label">参考图片</div>
            <div class="image-upload-area" @click="triggerImageUpload">
              <div v-if="!form.referenceImage" class="upload-placeholder">
                <div class="upload-icon">+</div>
                <div class="upload-text">点击上传参考图片</div>
              </div>
              <img v-else :src="form.referenceImage" class="preview-image" />
            </div>
            <input
              ref="imageInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />
          </div>

          <div class="form-section">
            <div class="form-label">视频参数</div>
            <div class="param-grid">
              <div class="param-item">
                <div class="param-label">时长</div>
                <el-select v-model="form.duration" class="param-select">
                  <el-option :value="5" label="5 秒" />
                  <el-option :value="10" label="10 秒" />
                  <el-option :value="15" label="15 秒" />
                </el-select>
              </div>
              <div class="param-item">
                <div class="param-label">比例</div>
                <el-select v-model="form.ratio" class="param-select" @change="handleRatioChange">
                  <el-option value="9:16" label="9:16 竖屏" />
                  <el-option value="16:9" label="16:9 横屏" />
                  <el-option value="1:1" label="1:1 方形" />
                </el-select>
              </div>
            </div>
          </div>

          <div class="form-section">
            <div class="form-label">风格预设</div>
            <div class="style-grid">
              <div
                v-for="style in stylePresets"
                :key="style.value"
                class="style-card"
                :class="{ active: form.stylePreset === style.value }"
                @click="form.stylePreset = style.value"
              >
                <div class="style-icon">{{ style.icon }}</div>
                <div class="style-name">{{ style.name }}</div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <el-button
              type="primary"
              class="generate-btn"
              @click="handleGenerate"
              :loading="generating"
              :disabled="!canGenerate"
            >
              {{ generating ? '生成中...' : '开始生成' }}
            </el-button>
          </div>
        </Panel>
      </div>

      <div class="right-panel">
        <Panel title="生成记录">
          <template #actions>
            <span class="refresh-btn" @click="loadTasks">
              <span class="refresh-icon" :class="{ spinning: loading }">↻</span>
              刷新
            </span>
          </template>

          <div class="task-list">
            <div
              v-for="task in tasks"
              :key="task.taskId"
              class="task-card"
              @click="handleViewTask(task)"
            >
              <div class="task-cover">
                <video
                  v-if="task.status === 'SUCCESS' && task.videoUrl"
                  :src="task.videoUrl"
                  :poster="task.coverUrl"
                  muted
                  class="task-video"
                />
                <div v-else class="task-cover-placeholder">
                  <div class="status-icon" :class="statusClass(task.status)">
                    {{ statusIcon(task.status) }}
                  </div>
                </div>
                <div v-if="task.status === 'PROCESSING'" class="progress-overlay">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: (task.progress || 0) + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ task.progress || 0 }}%</span>
                </div>
              </div>
              <div class="task-info">
                <div class="task-name">{{ task.taskName || '未命名视频' }}</div>
                <div class="task-meta">
                  <el-tag :type="statusType(task.status)" effect="light" size="small">
                    {{ statusText(task.status) }}
                  </el-tag>
                  <span class="task-time mono">{{ formatTime(task.createdTime) }}</span>
                </div>
              </div>
            </div>

            <div v-if="tasks.length === 0 && !loading" class="empty-tasks">
              <div class="empty-icon">◎</div>
              <div class="empty-text">暂无生成记录</div>
              <div class="empty-hint">在左侧输入创意描述，开始你的AI创作</div>
            </div>
          </div>
        </Panel>
      </div>
    </div>

    <el-dialog
      v-model="showDetail"
      :title="selectedTask?.taskName || '视频详情'"
      width="640px"
      class="video-detail-dialog"
    >
      <div v-if="selectedTask" class="detail-content">
        <div class="detail-video">
          <video
            v-if="selectedTask.videoUrl"
            :src="selectedTask.videoUrl"
            controls
            class="detail-player"
          />
          <div v-else class="detail-placeholder">
            <el-tag :type="statusType(selectedTask.status)" effect="light">
              {{ statusText(selectedTask.status) }}
            </el-tag>
            <div v-if="selectedTask.errorMessage" class="error-msg">
              {{ selectedTask.errorMessage }}
            </div>
          </div>
        </div>
        <div class="detail-info">
          <div class="info-row">
            <span class="info-label">任务编号</span>
            <span class="info-value mono">{{ selectedTask.taskNo }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">AI引擎</span>
            <span class="info-value">{{ providerLabel(selectedTask.provider) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">分辨率</span>
            <span class="info-value">{{ selectedTask.width }} × {{ selectedTask.height }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时长</span>
            <span class="info-value">{{ selectedTask.duration }} 秒</span>
          </div>
          <div class="info-row">
            <span class="info-label">创意描述</span>
            <span class="info-value prompt-text">{{ selectedTask.prompt }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button
          v-if="selectedTask?.status === 'SUCCESS'"
          type="primary"
          @click="handleDownload(selectedTask)"
        >
          下载视频
        </el-button>
        <el-button
          v-if="selectedTask?.status === 'PROCESSING' || selectedTask?.status === 'PENDING'"
          type="danger"
          @click="handleCancel(selectedTask)"
        >
          取消生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Panel from '@/components/common/Panel.vue'
import { videoGenApi } from '@/api/videoGen'

const form = ref({
  mode: 'TEXT_TO_VIDEO',
  provider: 'KUAISHOU_KELING',
  prompt: '',
  referenceImage: '',
  duration: 5,
  ratio: '9:16',
  width: 720,
  height: 1280,
  stylePreset: 'natural'
})

const tasks = ref([])
const loading = ref(false)
const generating = ref(false)
const showDetail = ref(false)
const selectedTask = ref(null)
const imageInput = ref(null)

let pollTimer = null

const providerList = ref([
  { value: 'MOCK', label: '模拟引擎（测试用）' },
  { value: 'KUAISHOU_KELING', label: '快手可灵 AI' },
  { value: 'BYTE_JIMENG', label: '字节即梦 AI' },
  { value: 'TENCENT_HUNYUAN', label: '腾讯混元' }
])

const promptTags = [
  '绿水青山',
  '美丽乡村',
  '城市夜景',
  '田园风光',
  '森林秘境',
  '日落黄昏',
  '云海翻涌',
  '星空银河'
]

const stylePresets = [
  { value: 'natural', name: '自然写实', icon: '🌿' },
  { value: 'cinematic', name: '电影质感', icon: '🎬' },
  { value: 'anime', name: '动漫风格', icon: '✨' },
  { value: '3d', name: '3D 渲染', icon: '🎨' },
  { value: 'watercolor', name: '水彩画', icon: '🎐' },
  { value: 'oil', name: '油画风', icon: '🖼️' }
]

const canGenerate = computed(() => {
  return form.value.prompt.trim().length > 0
})

const addPromptTag = (tag) => {
  if (form.value.prompt.length + tag.length + 1 <= 500) {
    form.value.prompt += (form.value.prompt ? '，' : '') + tag
  }
}

const handleRatioChange = (ratio) => {
  switch (ratio) {
    case '9:16':
      form.value.width = 720
      form.value.height = 1280
      break
    case '16:9':
      form.value.width = 1280
      form.value.height = 720
      break
    case '1:1':
      form.value.width = 1024
      form.value.height = 1024
      break
  }
}

const triggerImageUpload = () => {
  imageInput.value?.click()
}

const handleImageUpload = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => {
      form.value.referenceImage = event.target.result
    }
    reader.readAsDataURL(file)
  }
}

const handleGenerate = async () => {
  if (!canGenerate.value) {
    ElMessage.warning('请输入创意描述')
    return
  }

  try {
    generating.value = true
    const res = await videoGenApi.generateVideo({
      taskName: form.value.prompt.substring(0, 20) + '...',
      provider: form.value.provider,
      generationMode: form.value.mode,
      prompt: form.value.prompt,
      referenceImageUrl: form.value.referenceImage,
      duration: form.value.duration,
      width: form.value.width,
      height: form.value.height,
      stylePreset: form.value.stylePreset
    })

    if (res.code === 200 || res.code === 0) {
      ElMessage.success('视频生成任务已提交')
      loadTasks()
      startPolling()
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  } catch (e) {
    console.error('生成失败:', e)
    ElMessage.error('提交失败，请重试')
  } finally {
    generating.value = false
  }
}

const loadTasks = async () => {
  try {
    loading.value = true
    const res = await videoGenApi.listTasks({ limit: 20 })
    if (res.data && Array.isArray(res.data)) {
      tasks.value = res.data
    } else {
      tasks.value = []
    }
  } catch (e) {
    console.warn('加载任务列表失败:', e)
    tasks.value = []
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(() => {
    const hasProcessing = tasks.value.some(
      t => t.status === 'PENDING' || t.status === 'PROCESSING'
    )
    if (hasProcessing) {
      loadTasks()
    } else {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }, 5000)
}

const handleViewTask = (task) => {
  selectedTask.value = task
  showDetail.value = true
}

const handleDownload = (task) => {
  window.open(videoGenApi.downloadUrl(task.taskId), '_blank')
}

const handleCancel = async (task) => {
  try {
    await ElMessageBox.confirm('确定要取消这个生成任务吗？', '确认取消', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '继续等待'
    })
    const res = await videoGenApi.cancelTask(task.taskId)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('任务已取消')
      loadTasks()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消失败:', e)
    }
  }
}

const statusType = (status) => {
  const map = {
    PENDING: 'info',
    PROCESSING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    PENDING: '等待中',
    PROCESSING: '生成中',
    SUCCESS: '已完成',
    FAILED: '生成失败',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const statusIcon = (status) => {
  const map = {
    PENDING: '⏳',
    PROCESSING: '🎬',
    SUCCESS: '✓',
    FAILED: '✕',
    CANCELLED: '⊘'
  }
  return map[status] || '?'
}

const statusClass = (status) => {
  return `status-${status?.toLowerCase() || 'pending'}`
}

const providerLabel = (provider) => {
  const p = providerList.value.find(p => p.value === provider)
  return p ? p.label : provider
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${min}`
}

onMounted(() => {
  loadTasks()
  startPolling()
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
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
  grid-template-columns: 38% 62%;
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

.word-count {
  font-size: 12px;
  color: #B8A898;
}

.full-width {
  width: 100%;
}

.mode-group {
  width: 100%;
}

.prompt-textarea {
  width: 100%;
}

.prompt-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.suggestion-tag {
  padding: 4px 12px;
  font-size: 12px;
  color: #C9A96E;
  background: rgba(201, 168, 108, 0.08);
  border: 1px solid rgba(201, 168, 108, 0.2);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.suggestion-tag:hover {
  background: rgba(201, 168, 108, 0.15);
  transform: translateY(-1px);
}

.image-upload-area {
  width: 100%;
  height: 160px;
  border: 2px dashed #E8E2D9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  transition: all 0.3s ease;
  overflow: hidden;
}

.image-upload-area:hover {
  border-color: #C9A96E;
}

.upload-placeholder {
  text-align: center;
  color: #B8A898;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 13px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.param-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.param-item {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #E8E2D9;
}

.param-label {
  font-size: 12px;
  color: #8B7355;
  margin-bottom: 6px;
}

.param-select {
  width: 100%;
}

.style-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.style-card {
  padding: 14px 8px;
  text-align: center;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1.5px solid #E8E2D9;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.style-card:hover {
  border-color: #C9A96E;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.15);
}

.style-card.active {
  border-color: #C9A96E;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.1) 0%, rgba(212, 184, 122, 0.05) 100%);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.2);
}

.style-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.style-name {
  font-size: 12px;
  color: #5D4E37;
  font-weight: 500;
}

.generate-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border: none;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.35);
}

.generate-btn:hover {
  background: linear-gradient(135deg, #B89855 0%, #C9A96E 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(201, 168, 108, 0.45);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
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

.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.task-card {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.task-card:hover {
  border-color: #C9A96E;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(201, 168, 108, 0.15);
}

.task-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #2C2416;
  overflow: hidden;
}

.task-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.task-cover-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3D3024 0%, #2C2416 100%);
}

.status-icon {
  font-size: 32px;
  color: #fff;
}

.progress-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A96E, #D4B87A);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: #fff;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.task-info {
  padding: 12px;
}

.task-name {
  font-size: 13px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-time {
  font-size: 11px;
  color: #B8A898;
}

.empty-tasks {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #B8A898;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: #8B7355;
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 13px;
}

.detail-content {
  padding: 10px 0;
}

.detail-video {
  width: 100%;
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
}

.detail-player {
  width: 100%;
  display: block;
}

.detail-placeholder {
  padding: 60px 20px;
  text-align: center;
}

.error-msg {
  margin-top: 12px;
  font-size: 13px;
  color: #E53935;
}

.detail-info {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  padding: 16px;
}

.info-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #E8E2D9;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #8B7355;
  font-weight: 500;
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: #5D4E37;
  word-break: break-all;
}

.prompt-text {
  line-height: 1.6;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
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

:deep(.el-input__wrapper) {
  border-radius: 10px;
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

:deep(.el-dialog) {
  --el-dialog-radius: 16px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
}

:deep(.el-dialog__title) {
  color: #5D4E37;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #C9A96E;
  --el-button-border-color: #C9A96E;
  --el-button-hover-bg-color: #B89855;
  --el-button-hover-border-color: #B89855;
  --el-button-active-bg-color: #A88844;
  --el-button-active-border-color: #A88844;
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

@media (max-width: 1200px) {
  .layout-wrapper {
    grid-template-columns: 1fr;
  }

  .style-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .page-container {
    padding: 20px 16px;
  }

  .style-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .task-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
