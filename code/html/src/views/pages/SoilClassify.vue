<template>
  <div class="classify-page">
    <section class="page-hero">
      <div class="hero-bg"></div>
      <div class="hero-inner">
        <div class="hero-meta mono">AI SOIL CLASSIFICATION · 智能土壤识别</div>
        <h1 class="hero-title">AI 土壤比对分析</h1>
        <p class="hero-desc">
          基于深度学习模型与地学标准数据库，通过多维度特征比对，
          实现土壤类型智能识别与分类建议，支持图像、参数、光谱等多种输入方式。
        </p>
      </div>
    </section>

    <section class="workflow-section">
      <div class="section-header">
        <h2 class="section-title">分析流程</h2>
        <p class="section-sub">四步完成土壤智能比对</p>
      </div>
      <div class="step-timeline">
        <div
          v-for="(step, index) in steps"
          :key="step.id"
          :class="['step-item', { active: currentStep >= index, done: currentStep > index }]"
        >
          <div class="step-circle">
            <span v-if="currentStep > index" class="step-check">✓</span>
            <span v-else class="step-num">{{ index + 1 }}</span>
          </div>
          <div class="step-line" v-if="index < steps.length - 1"></div>
          <div class="step-content">
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-desc">{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="main-content">
      <div class="input-panel">
        <div class="panel-head">
          <h3 class="panel-title">数据输入</h3>
          <div class="input-tabs">
            <button
              v-for="tab in inputTabs"
              :key="tab.key"
              :class="['tab-btn', { active: activeTab === tab.key }]"
              @click="activeTab = tab.key"
            >
              {{ tab.icon }} {{ tab.name }}
            </button>
          </div>
        </div>

        <div class="panel-body">
          <div v-if="activeTab === 'params'" class="params-form">
            <div class="form-grid">
              <div class="form-item">
                <label class="form-label">pH 值</label>
                <input v-model.number="formData.ph" type="number" step="0.1" placeholder="6.5 - 7.5" class="form-input" />
              </div>
              <div class="form-item">
                <label class="form-label">有机质含量 (%)</label>
                <input v-model.number="formData.organicMatter" type="number" step="0.1" placeholder="2.5" class="form-input" />
              </div>
              <div class="form-item">
                <label class="form-label">含水量 (%)</label>
                <input v-model.number="formData.moisture" type="number" step="0.1" placeholder="30" class="form-input" />
              </div>
              <div class="form-item">
                <label class="form-label">电导率 (ms/cm)</label>
                <input v-model.number="formData.ec" type="number" step="0.01" placeholder="0.35" class="form-input" />
              </div>
              <div class="form-item">
                <label class="form-label">砂粒含量 (%)</label>
                <input v-model.number="formData.sand" type="number" step="1" placeholder="40" class="form-input" />
              </div>
              <div class="form-item">
                <label class="form-label">黏粒含量 (%)</label>
                <input v-model.number="formData.clay" type="number" step="1" placeholder="25" class="form-input" />
              </div>
              <div class="form-item full">
                <label class="form-label">颜色描述</label>
                <select v-model="formData.color" class="form-select">
                  <option value="">请选择</option>
                  <option value="红色">红色</option>
                  <option value="黄色">黄色</option>
                  <option value="棕褐色">棕褐色</option>
                  <option value="黑色">黑色</option>
                  <option value="灰色">灰色</option>
                  <option value="白色">白色</option>
                </select>
              </div>
              <div class="form-item full">
                <label class="form-label">质地类型</label>
                <select v-model="formData.texture" class="form-select">
                  <option value="">请选择</option>
                  <option value="砂土">砂土</option>
                  <option value="砂壤土">砂壤土</option>
                  <option value="壤土">壤土</option>
                  <option value="黏壤土">黏壤土</option>
                  <option value="黏土">黏土</option>
                </select>
              </div>
            </div>
            <div class="form-actions">
              <button class="btn-secondary" @click="resetForm">重置</button>
              <button class="btn-primary" @click="startAnalysis" :disabled="analyzing">
                <span v-if="analyzing" class="btn-spinner"></span>
                {{ analyzing ? '分析中...' : '开始比对分析' }}
              </button>
            </div>
          </div>

          <div v-if="activeTab === 'image'" class="image-upload">
            <div class="upload-area" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
              <div class="upload-icon">◉</div>
              <p class="upload-text">点击或拖拽土壤样本图片到此处</p>
              <p class="upload-hint">支持 JPG、PNG 格式，建议清晰度 1080P 以上</p>
              <input ref="fileInput" type="file" accept="image/*" @change="handleFileChange" hidden />
            </div>
            <div v-if="uploadedImage" class="image-preview">
              <img :src="uploadedImage" alt="样本图片" />
              <button class="btn-primary full-btn" @click="analyzeImage" :disabled="analyzing">
                {{ analyzing ? 'AI识别中...' : 'AI图像识别' }}
              </button>
            </div>
          </div>

          <div v-if="activeTab === 'spectrum'" class="spectrum-input">
            <div class="spectrum-placeholder">
              <div class="spectrum-icon">≋</div>
              <p class="spectrum-text">上传土壤光谱数据文件</p>
              <p class="spectrum-hint">支持 CSV、TXT 格式的近红外/高光谱数据</p>
              <button class="btn-outline">选择文件</button>
            </div>
          </div>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-head">
          <h3 class="panel-title">比对结果</h3>
          <span v-if="results.length > 0" class="result-count mono">{{ results.length }} 个匹配项</span>
        </div>

        <div v-if="results.length === 0" class="empty-result">
          <div class="empty-icon">⚗</div>
          <p class="empty-title">暂无分析结果</p>
          <p class="empty-desc">输入土壤参数或上传图片，开始AI智能比对</p>
        </div>

        <div v-else class="result-list">
          <div v-for="(r, idx) in results" :key="r.name" :class="['result-item', { top: idx === 0 }]">
            <div class="result-rank" v-if="idx === 0">最佳匹配</div>
            <div class="result-head">
              <div class="result-name-row">
                <span class="result-rank-num mono">#{{ idx + 1 }}</span>
                <h4 class="result-name">{{ r.name }}</h4>
              </div>
              <div class="confidence-bar">
                <div class="confidence-fill" :style="{ width: r.confidence + '%' }"></div>
                <span class="confidence-text mono">{{ r.confidence }}%</span>
              </div>
            </div>
            <p class="result-desc">{{ r.description }}</p>
            <div class="result-meta">
              <span class="meta-tag">{{ r.category }}</span>
              <span class="meta-tag">pH {{ r.ph }}</span>
              <span class="meta-tag">有机质 {{ r.organic }}%</span>
            </div>
            <div class="match-details">
              <div v-for="m in r.matches" :key="m.field" class="match-item">
                <span class="match-field">{{ m.field }}</span>
                <div class="match-bar-wrapper">
                  <div class="match-bar" :style="{ width: m.score + '%' }"></div>
                </div>
                <span class="match-score mono">{{ m.score }}%</span>
              </div>
            </div>
            <div class="result-actions">
              <button class="btn-text" @click="viewStandard(r)">查看标准</button>
              <button class="btn-text" @click="viewReport(r)">详细报告</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="history-section">
      <div class="section-header">
        <h2 class="section-title">分析记录</h2>
        <p class="section-sub">历史分析任务列表</p>
      </div>
      <div class="history-table">
        <div class="table-head">
          <span class="th-id mono">ID</span>
          <span class="th-type">输入类型</span>
          <span class="th-sample">样本数</span>
          <span class="th-result">识别结果</span>
          <span class="th-conf">置信度</span>
          <span class="th-time">分析时间</span>
          <span class="th-action">操作</span>
        </div>
        <div class="table-body">
          <div v-for="a in analysisHistory" :key="a.id" class="table-row">
            <span class="td-id mono">{{ a.id }}</span>
            <span class="td-type">
              <span :class="['type-badge', a.inputType]">{{ a.typeLabel }}</span>
            </span>
            <span class="td-sample">{{ a.sampleCount }}</span>
            <span class="td-result">{{ a.result }}</span>
            <span class="td-conf">
              <span class="confidence-mini">
                <span class="conf-fill" :style="{ width: a.confidence + '%' }"></span>
                <span class="conf-text">{{ a.confidence }}%</span>
              </span>
            </span>
            <span class="td-time mono">{{ a.analysisTime }}</span>
            <span class="td-action">
              <button class="btn-link" @click="viewHistory(a)">查看</button>
            </span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSoilClassifyList, getClassifyHistory } from '@/api/soilClassify.js'

const steps = [
  { id: 1, title: '数据采集', desc: '输入参数/图像/光谱数据' },
  { id: 2, title: '特征提取', desc: 'AI多维度特征解析' },
  { id: 3, title: '标准比对', desc: '与标准数据库匹配' },
  { id: 4, title: '结果输出', desc: '分类建议与置信度' }
]

const inputTabs = [
  { key: 'params', name: '参数输入', icon: '◊' },
  { key: 'image', name: '图像识别', icon: '◉' },
  { key: 'spectrum', name: '光谱分析', icon: '≋' }
]

const currentStep = ref(0)
const activeTab = ref('params')
const analyzing = ref(false)
const uploadedImage = ref('')
const fileInput = ref(null)
const loading = ref(false)

const formData = reactive({
  ph: 6.8,
  organicMatter: 2.6,
  moisture: 32,
  ec: 0.32,
  sand: 35,
  clay: 22,
  color: '棕褐色',
  texture: '壤土'
})

const results = ref([])
const analysisHistory = ref([])

// 加载分类结果和历史记录
const loadClassifyData = async () => {
  loading.value = true
  try {
    // 加载分类列表
    const listRes = await getSoilClassifyList()
    // 加载历史记录
    const historyRes = await getClassifyHistory()
    
    if (listRes.list) {
      results.value = (listRes.list || []).map(item => ({
        name: item.soilTypeName || item.name,
        confidence: item.confidence || 0,
        description: item.description || '',
        category: item.category || item.soilCategory || '',
        ph: item.phRange || '6.5-7.5',
        organic: item.organicRange || '2-4',
        matches: item.matchDetails || [
          { field: 'pH 值', score: item.phMatch || 0 },
          { field: '有机质', score: item.organicMatch || 0 },
          { field: '质地类型', score: item.textureMatch || 0 },
          { field: '颜色特征', score: item.colorMatch || 0 }
        ]
      }))
    }
    
    if (historyRes.list) {
      analysisHistory.value = (historyRes.list || []).map(item => ({
        id: item.id || item.analysisId,
        inputType: item.inputType || 'params',
        typeLabel: getInputTypeLabel(item.inputType),
        sampleCount: item.sampleCount || 1,
        result: item.resultSoilType || item.result,
        confidence: item.confidence || 0,
        analysisTime: item.analysisTime || item.createTime
      }))
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
    ElMessage.error('加载分类数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 辅助函数：获取输入类型标签
const getInputTypeLabel = (type) => {
  const map = {
    params: '参数输入',
    image: '图像识别',
    spectrum: '光谱分析'
  }
  return map[type] || type
}

const resetForm = () => {
  formData.ph = null
  formData.organicMatter = null
  formData.moisture = null
  formData.ec = null
  formData.sand = null
  formData.clay = null
  formData.color = ''
  formData.texture = ''
  currentStep.value = 0
}

const startAnalysis = async () => {
  analyzing.value = true
  currentStep.value = 1

  try {
    for (let i = 2; i <= 4; i++) {
      await new Promise(r => setTimeout(r, 600))
      currentStep.value = i
    }
  } finally {
    analyzing.value = false
  }
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      uploadedImage.value = ev.target?.result
    }
    reader.readAsDataURL(file)
  }
}

const handleDrop = (e) => {
  const file = e.dataTransfer.files?.[0]
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      uploadedImage.value = ev.target?.result
    }
    reader.readAsDataURL(file)
  }
}

const analyzeImage = async () => {
  analyzing.value = true
  currentStep.value = 1
  for (let i = 2; i <= 4; i++) {
    await new Promise(r => setTimeout(r, 800))
    currentStep.value = i
  }
  analyzing.value = false
}

const viewStandard = (r) => {
  console.log('查看标准:', r.name)
}

const viewReport = (r) => {
  console.log('查看报告:', r.name)
}

const viewHistory = (a) => {
  console.log('查看历史:', a.id)
}

onMounted(() => {
  loadClassifyData()
})
</script>

<style scoped>
.classify-page {
  background: linear-gradient(180deg, #F7F3ED 0%, #FEFBF6 100%);
  min-height: 100vh;
  padding-bottom: 80px;
}

/* Hero */
.page-hero {
  position: relative;
  padding: 60px 40px 80px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(201, 168, 108, 0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 30%, rgba(139, 105, 20, 0.08) 0%, transparent 50%);
}

.hero-inner {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
  animation: fadeUp 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-meta {
  font-size: 11px;
  color: #A68B6A;
  letter-spacing: 3px;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 40px;
  font-weight: 200;
  color: #3D2B1F;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.hero-desc {
  font-size: 14px;
  color: #6B5344;
  line-height: 1.8;
  max-width: 560px;
}

/* Workflow */
.workflow-section {
  padding: 0 40px 48px;
  max-width: 1100px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-size: 22px;
  font-weight: 400;
  color: #3D2B1F;
  margin-bottom: 6px;
}

.section-sub {
  font-size: 13px;
  color: #8B7355;
}

.step-timeline {
  display: flex;
  gap: 0;
  position: relative;
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #E8E2D9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 500;
  color: #8B7355;
  z-index: 2;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.step-item.active .step-circle {
  background: linear-gradient(135deg, #C9A86C 0%, #A67C52 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.4);
}

.step-item.done .step-circle {
  background: #5D7A3E;
  color: white;
}

.step-check {
  font-size: 18px;
}

.step-line {
  position: absolute;
  top: 24px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #E8E2D9;
  z-index: 1;
}

.step-item.done .step-line {
  background: #5D7A3E;
}

.step-content {
  text-align: center;
  margin-top: 14px;
  padding: 0 10px;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  color: #3D2B1F;
  margin-bottom: 4px;
}

.step-desc {
  font-size: 12px;
  color: #8B7355;
  line-height: 1.5;
}

/* Main Content */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 24px;
  padding: 0 40px;
  max-width: 1200px;
  margin: 0 auto 48px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-title {
  font-size: 16px;
  font-weight: 500;
  color: #3D2B1F;
}

.input-panel, .result-panel {
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 14px;
  padding: 24px;
}

.input-tabs {
  display: flex;
  gap: 4px;
  background: #F5F2ED;
  padding: 4px;
  border-radius: 10px;
}

.tab-btn {
  padding: 8px 14px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 12px;
  color: #8B7355;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.tab-btn.active {
  background: #FEFBF6;
  color: #5D4E37;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.1);
}

/* Form */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item.full {
  grid-column: span 2;
}

.form-label {
  font-size: 12px;
  color: #6B5344;
  font-weight: 500;
}

.form-input, .form-select {
  padding: 10px 14px;
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  background: #FAF7F2;
  font-size: 14px;
  color: #3D2B1F;
  outline: none;
  transition: all 0.2s ease;
}

.form-input:focus, .form-select:focus {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
}

.btn-secondary {
  flex: 1;
  padding: 12px 20px;
  background: #F5F2ED;
  border: 1px solid #E8E2D9;
  border-radius: 10px;
  font-size: 14px;
  color: #5D4E37;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: #EDE9E0;
}

.btn-primary {
  flex: 2;
  padding: 12px 20px;
  background: linear-gradient(135deg, #C9A86C 0%, #A67C52 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(201, 168, 108, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Image Upload */
.upload-area {
  border: 2px dashed #D4C4A8;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  background: #FAF7F2;
}

.upload-area:hover {
  border-color: #C9A86C;
  background: rgba(201, 168, 108, 0.05);
}

.upload-icon {
  font-size: 48px;
  color: #C9A86C;
  margin-bottom: 12px;
  opacity: 0.6;
}

.upload-text {
  font-size: 14px;
  color: #5D4E37;
  margin-bottom: 6px;
}

.upload-hint {
  font-size: 12px;
  color: #A68B6A;
}

.image-preview {
  margin-top: 20px;
}

.image-preview img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 16px;
}

.full-btn {
  width: 100%;
}

/* Spectrum */
.spectrum-placeholder {
  text-align: center;
  padding: 48px 24px;
  border: 2px dashed #D4C4A8;
  border-radius: 12px;
  background: #FAF7F2;
}

.spectrum-icon {
  font-size: 48px;
  color: #C9A86C;
  margin-bottom: 12px;
}

.spectrum-text {
  font-size: 14px;
  color: #5D4E37;
  margin-bottom: 6px;
}

.spectrum-hint {
  font-size: 12px;
  color: #A68B6A;
  margin-bottom: 16px;
}

.btn-outline {
  padding: 10px 24px;
  background: transparent;
  border: 1px solid #C9A86C;
  border-radius: 8px;
  color: #8B6914;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-outline:hover {
  background: rgba(201, 168, 108, 0.1);
}

/* Result Panel */
.result-count {
  font-size: 12px;
  color: #8B7355;
  padding: 4px 10px;
  background: #F5F2ED;
  border-radius: 20px;
}

.empty-result {
  text-align: center;
  padding: 60px 24px;
}

.empty-icon {
  font-size: 56px;
  color: #D4C4A8;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  color: #5D4E37;
  margin-bottom: 6px;
  font-weight: 500;
}

.empty-desc {
  font-size: 13px;
  color: #A68B6A;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 8px;
}

.result-item {
  padding: 20px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
}

.result-item.top {
  border-color: #C9A86C;
  box-shadow: 0 4px 20px rgba(201, 168, 108, 0.15);
}

.result-rank {
  position: absolute;
  top: -10px;
  right: 16px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #C9A86C 0%, #A67C52 100%);
  color: white;
  font-size: 11px;
  font-weight: 500;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.result-head {
  margin-bottom: 12px;
}

.result-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.result-rank-num {
  font-size: 12px;
  color: #C9A86C;
  font-weight: 600;
}

.result-name {
  font-size: 18px;
  font-weight: 500;
  color: #3D2B1F;
  margin: 0;
}

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.confidence-fill {
  flex: 1;
  height: 6px;
  background: linear-gradient(90deg, #C9A86C, #A67C52);
  border-radius: 3px;
  transition: width 1s cubic-bezier(0.22, 1, 0.36, 1);
}

.confidence-bar::before {
  content: '';
  flex: 1;
  height: 6px;
  background: #E8E2D9;
  border-radius: 3px;
  position: absolute;
  left: 0;
  z-index: 0;
}

.confidence-bar {
  position: relative;
  height: 6px;
  background: #E8E2D9;
  border-radius: 3px;
  flex: 1;
  margin-right: 60px;
}

.confidence-fill {
  position: relative;
  z-index: 1;
  background: linear-gradient(90deg, #C9A86C, #A67C52);
  border-radius: 3px;
  height: 6px;
}

.confidence-text {
  position: absolute;
  right: -60px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  font-weight: 600;
  color: #8B6914;
}

.result-desc {
  font-size: 13px;
  color: #6B5344;
  line-height: 1.6;
  margin-bottom: 12px;
}

.result-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.meta-tag {
  padding: 4px 10px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 6px;
  font-size: 11px;
  color: #5D4E37;
}

.match-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: #FEFBF6;
  border-radius: 8px;
  margin-bottom: 14px;
}

.match-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.match-field {
  width: 72px;
  font-size: 12px;
  color: #8B7355;
  flex-shrink: 0;
}

.match-bar-wrapper {
  flex: 1;
  height: 4px;
  background: #E8E2D9;
  border-radius: 2px;
  overflow: hidden;
}

.match-bar {
  height: 100%;
  background: linear-gradient(90deg, #8BA86C, #5D7A3E);
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

.match-score {
  width: 40px;
  text-align: right;
  font-size: 11px;
  color: #5D7A3E;
  font-weight: 500;
}

.result-actions {
  display: flex;
  gap: 16px;
}

.btn-text {
  padding: 0;
  background: none;
  border: none;
  color: #8B6914;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.btn-text:hover {
  color: #5D4E37;
}

/* History */
.history-section {
  padding: 0 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.history-table {
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 14px;
  overflow: hidden;
}

.table-head {
  display: grid;
  grid-template-columns: 140px 100px 80px 1fr 120px 160px 80px;
  gap: 12px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E0 100%);
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #E8E2D9;
}

.table-row {
  display: grid;
  grid-template-columns: 140px 100px 80px 1fr 120px 160px 80px;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #EDE9E0;
  align-items: center;
  transition: background 0.2s ease;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: rgba(201, 168, 108, 0.04);
}

.td-id { color: #8B7355; font-size: 12px; }

.type-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

.type-badge.params {
  background: rgba(201, 168, 108, 0.15);
  color: #8B6914;
}

.type-badge.image {
  background: rgba(93, 122, 62, 0.15);
  color: #5D7A3E;
}

.type-badge.spectrum {
  background: rgba(94, 74, 107, 0.15);
  color: #5E4A6B;
}

.td-result {
  font-size: 14px;
  color: #3D2B1F;
  font-weight: 500;
}

.confidence-mini {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conf-fill {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #C9A86C, #A67C52);
  border-radius: 2px;
}

.conf-text {
  font-size: 12px;
  color: #8B6914;
  font-weight: 500;
}

.td-time {
  color: #8B7355;
  font-size: 12px;
}

.btn-link {
  padding: 0;
  background: none;
  border: none;
  color: #8B6914;
  font-size: 13px;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
}

@media (max-width: 900px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  .step-timeline {
    flex-wrap: wrap;
  }
  .step-item {
    flex: 0 0 50%;
    margin-bottom: 20px;
  }
  .step-line { display: none; }
}

@media (max-width: 600px) {
  .form-grid { grid-template-columns: 1fr; }
  .form-item.full { grid-column: span 1; }
  .page-hero { padding: 40px 20px 60px; }
  .hero-title { font-size: 28px; }
  .main-content, .workflow-section, .history-section { padding: 0 20px; }
}
</style>
