<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">报表中心</h1>
      <div class="page-meta mono">REPORT CENTER · 智能报表生成</div>
    </div>

    <div class="layout-wrapper">
      <div class="left-panel">
        <Panel title="报表模板">
          <div class="tab-bar">
            <span
              v-for="tab in categoryTabs"
              :key="tab.value"
              class="tab-item"
              :class="{ active: activeCategory === tab.value }"
              @click="activeCategory = tab.value"
            >
              {{ tab.label }}
            </span>
          </div>

          <div class="template-list">
            <div
              v-for="tpl in filteredTemplates"
              :key="tpl.code"
              class="template-card"
              :class="{ selected: selectedTemplate?.code === tpl.code }"
              @click="selectTemplate(tpl)"
            >
              <div class="template-thumb">
                <span class="thumb-icon">{{ tpl.icon }}</span>
              </div>
              <div class="template-info">
                <div class="template-name">{{ tpl.name }}</div>
                <div class="template-desc">{{ tpl.description }}</div>
              </div>
            </div>
          </div>
        </Panel>
      </div>

      <div class="right-panel">
        <Panel v-if="selectedTemplate" :title="selectedTemplate.name">
          <template #actions>
            <div class="export-btns">
              <span class="export-btn" @click="exportReport('PDF')">PDF</span>
              <span class="export-btn" @click="exportReport('EXCEL')">Excel</span>
              <span class="export-btn" @click="exportReport('WORD')">Word</span>
            </div>
          </template>

          <div class="report-content">
            <div class="params-section">
              <div class="section-title">参数配置</div>
              <div class="params-grid">
                <div class="param-item">
                  <label class="param-label">开始日期</label>
                  <el-date-picker
                    v-model="reportParams.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    class="full-width"
                  />
                </div>
                <div class="param-item">
                  <label class="param-label">结束日期</label>
                  <el-date-picker
                    v-model="reportParams.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    class="full-width"
                  />
                </div>
                <div class="param-item">
                  <label class="param-label">区域</label>
                  <el-select v-model="reportParams.area" placeholder="选择区域" class="full-width">
                    <el-option label="全部区域" value="all" />
                    <el-option label="乔口镇" value="qiaokou" />
                    <el-option label="莲花镇" value="lianhua" />
                    <el-option label="雨敞坪镇" value="yuchangping" />
                  </el-select>
                </div>
                <div class="param-item">
                  <label class="param-label">粒度</label>
                  <el-select v-model="reportParams.granularity" placeholder="选择粒度" class="full-width">
                    <el-option label="按日" value="day" />
                    <el-option label="按周" value="week" />
                    <el-option label="按月" value="month" />
                  </el-select>
                </div>
              </div>
              <el-button
                type="primary"
                class="generate-btn"
                @click="generateReport"
                :loading="generating"
              >
                生成报表
              </el-button>
            </div>

            <div v-if="reportGenerated" class="report-sections">
              <div class="report-section">
                <div class="section-title">{{ selectedTemplate.name }} · 趋势分析</div>
                <div ref="trendChartRef" class="chart-box"></div>
              </div>

              <div class="report-section">
                <div class="section-title">数据统计</div>
                <div class="stat-row">
                  <div class="stat-item">
                    <div class="stat-value">{{ reportStats.total }}</div>
                    <div class="stat-label">总数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value ok">{{ reportStats.passRate }}%</div>
                    <div class="stat-label">合格率</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value warn">{{ reportStats.pending }}</div>
                    <div class="stat-label">待处理</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value accent">{{ reportStats.efficiency }}%</div>
                    <div class="stat-label">完成率</div>
                  </div>
                </div>
              </div>

              <div class="report-section">
                <div class="section-title">分布占比</div>
                <div ref="pieChartRef" class="chart-box pie-chart"></div>
              </div>

              <div class="report-section">
                <div class="section-title">明细数据</div>
                <div class="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>序号</th>
                        <th>名称</th>
                        <th>类型</th>
                        <th>状态</th>
                        <th>数值</th>
                        <th>时间</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(item, index) in reportData" :key="index">
                        <td class="mono">{{ index + 1 }}</td>
                        <td>{{ item.name }}</td>
                        <td>{{ item.type }}</td>
                        <td>
                          <span class="status-badge" :class="item.statusClass">
                            {{ item.status }}
                          </span>
                        </td>
                        <td class="mono">{{ item.value }}</td>
                        <td class="mono">{{ item.time }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <div v-else class="empty-report">
              <div class="empty-icon">📊</div>
              <div class="empty-text">选择参数后点击"生成报表"查看数据</div>
            </div>
          </div>
        </Panel>

        <div v-else class="empty-template">
          <div class="empty-icon">📋</div>
          <div class="empty-title">请选择报表模板</div>
          <div class="empty-desc">从左侧列表中选择一个报表模板开始</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import Panel from '@/components/common/Panel.vue'
import { reportApi } from '@/api/report'

const categoryTabs = [
  { label: '全部', value: 'all' },
  { label: '仪表盘', value: 'dashboard' },
  { label: '土质', value: 'soil' },
  { label: '灾害', value: 'disaster' },
  { label: '岩层', value: 'rock' },
  { label: '设备', value: 'device' },
  { label: '质量', value: 'quality' }
]

const templates = ref([
  { code: 'dashboard-overview', name: '综合仪表盘', category: 'dashboard', icon: '📊', description: '全局数据概览报表' },
  { code: 'soil-analysis', name: '土质分析报表', category: 'soil', icon: '🌱', description: '土壤质量与分类分析' },
  { code: 'soil-sample', name: '采样统计报表', category: 'soil', icon: '🧪', description: '土壤采样数据统计' },
  { code: 'disaster-risk', name: '灾害风险报表', category: 'disaster', icon: '⚠️', description: '地质灾害风险评估' },
  { code: 'rock-stratum', name: '岩层分析报表', category: 'rock', icon: '🪨', description: '地层结构分析报告' },
  { code: 'device-status', name: '设备运行报表', category: 'device', icon: '📡', description: '设备状态与运行统计' },
  { code: 'quality-report', name: '质量校验报表', category: 'quality', icon: '✓', description: '数据质量检查报告' },
  { code: 'mission-report', name: '任务统计报表', category: 'dashboard', icon: '📋', description: '采集任务完成统计' }
])

const activeCategory = ref('all')
const selectedTemplate = ref(null)
const generating = ref(false)
const reportGenerated = ref(false)

const reportParams = ref({
  startDate: '',
  endDate: '',
  area: 'all',
  granularity: 'day'
})

const reportStats = ref({
  total: 0,
  passRate: 0,
  pending: 0,
  efficiency: 0
})

const reportData = ref([])

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const filteredTemplates = computed(() => {
  if (activeCategory.value === 'all') {
    return templates.value
  }
  return templates.value.filter(t => t.category === activeCategory.value)
})

const selectTemplate = (tpl) => {
  selectedTemplate.value = tpl
  reportGenerated.value = false
}

const generateReport = async () => {
  try {
    generating.value = true
    await reportApi.generateReport({
      templateCode: selectedTemplate.value.code,
      params: reportParams.value
    }).catch(() => {})

    reportStats.value = {
      total: 1256,
      passRate: 94.5,
      pending: 68,
      efficiency: 87.2
    }

    reportData.value = [
      { name: '乔口镇采样点A', type: '土壤采样', status: '合格', statusClass: 'status-ok', value: '85.6', time: '2024-01-15 10:30' },
      { name: '莲花镇采样点B', type: '土壤采样', status: '合格', statusClass: 'status-ok', value: '92.3', time: '2024-01-15 11:20' },
      { name: '雨敞坪采样点C', type: '土壤采样', status: '待检', statusClass: 'status-warn', value: '-', time: '2024-01-15 14:00' },
      { name: '乔口镇采样点D', type: '土壤采样', status: '不合格', statusClass: 'status-err', value: '58.2', time: '2024-01-15 15:30' },
      { name: '莲花镇采样点E', type: '土壤采样', status: '合格', statusClass: 'status-ok', value: '88.9', time: '2024-01-15 16:45' }
    ]

    reportGenerated.value = true

    await nextTick()
    initCharts()

    ElMessage.success('报表生成成功')
  } catch (e) {
    ElMessage.error('报表生成失败')
  } finally {
    generating.value = false
  }
}

const initCharts = () => {
  if (trendChartRef.value) {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      backgroundColor: 'transparent',
      grid: { top: 30, right: 30, bottom: 40, left: 60 },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E8E2D9',
        borderWidth: 1,
        textStyle: { color: '#5D4E37', fontSize: 12 },
        borderRadius: 8
      },
      legend: {
        data: ['合格数', '不合格数'],
        right: 10,
        top: 0,
        textStyle: { color: '#8B7355', fontSize: 12 }
      },
      xAxis: {
        type: 'category',
        data: ['01-09', '01-10', '01-11', '01-12', '01-13', '01-14', '01-15'],
        axisLine: { lineStyle: { color: '#E8E2D9' } },
        axisTick: { show: false },
        axisLabel: { color: '#8B7355', fontFamily: 'JetBrains Mono', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#F0EBE3', type: 'dashed' } },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#8B7355', fontFamily: 'JetBrains Mono', fontSize: 11 }
      },
      series: [
        {
          name: '合格数',
          type: 'bar',
          data: [120, 145, 132, 168, 155, 178, 189],
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#A5D6A7' },
                { offset: 1, color: '#66BB6A' }
              ]
            },
            borderRadius: [6, 6, 0, 0]
          },
          barWidth: '30%'
        },
        {
          name: '不合格数',
          type: 'bar',
          data: [12, 8, 15, 6, 10, 5, 7],
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#EF9A9A' },
                { offset: 1, color: '#E57373' }
              ]
            },
            borderRadius: [6, 6, 0, 0]
          },
          barWidth: '30%'
        }
      ]
    })
  }

  if (pieChartRef.value) {
    if (pieChart) pieChart.dispose()
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E8E2D9',
        borderWidth: 1,
        textStyle: { color: '#5D4E37', fontSize: 12 },
        borderRadius: 8
      },
      legend: {
        orient: 'vertical',
        right: 30,
        top: 'center',
        textStyle: { color: '#5D4E37', fontSize: 12 }
      },
      series: [
        {
          name: '类型分布',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 3
          },
          label: { show: false },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#5D4E37'
            }
          },
          labelLine: { show: false },
          data: [
            { value: 480, name: '土壤采样', itemStyle: { color: '#C9A96E' } },
            { value: 320, name: '质量校验', itemStyle: { color: '#7FB3D5' } },
            { value: 210, name: '设备数据', itemStyle: { color: '#81C784' } },
            { value: 156, name: '灾害评估', itemStyle: { color: '#FFB74D' } },
            { value: 90, name: '岩层分析', itemStyle: { color: '#BA68C8' } }
          ]
        }
      ]
    })
  }
}

const exportReport = (format) => {
  if (!reportGenerated.value) {
    ElMessage.warning('请先生成报表')
    return
  }
  ElMessage.success(`正在导出 ${format} 格式文件...`)
}

const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (pieChart) pieChart.resize()
}

const loadTemplates = async () => {
  try {
    const res = await reportApi.listTemplates({})
    if (res.templates && res.templates.length) {
      templates.value = res.templates
    }
  } catch (e) {
    console.warn('加载模板列表失败:', e.message)
  }
}

watch(selectedTemplate, () => {
  reportGenerated.value = false
})

onMounted(() => {
  loadTemplates()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) trendChart.dispose()
  if (pieChart) pieChart.dispose()
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
  grid-template-columns: 320px 1fr;
  gap: 20px;
}

.left-panel {
  min-width: 0;
}

.right-panel {
  min-width: 0;
}

.tab-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E8E2D9;
}

.tab-item {
  padding: 6px 12px;
  font-size: 12px;
  color: #8B7355;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.tab-item:hover {
  background: rgba(201, 168, 108, 0.1);
  color: #C9A96E;
}

.tab-item.active {
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  color: #fff;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  padding-right: 4px;
}

.template-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.template-card:hover {
  border-color: #C9A96E;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.15);
}

.template-card.selected {
  border-color: #C9A96E;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.08) 0%, rgba(212, 184, 122, 0.05) 100%);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.2);
}

.template-thumb {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.15) 0%, rgba(212, 184, 122, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.thumb-icon {
  font-size: 24px;
}

.template-info {
  flex: 1;
  min-width: 0;
}

.template-name {
  font-size: 14px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 4px;
}

.template-desc {
  font-size: 12px;
  color: #8B7355;
  line-height: 1.5;
}

.export-btns {
  display: flex;
  gap: 8px;
}

.export-btn {
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.export-btn:hover {
  border-color: #C9A96E;
  color: #C9A96E;
  background: rgba(201, 168, 108, 0.08);
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #E8E2D9;
}

.params-section {
  padding: 20px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.param-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-label {
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
}

.full-width {
  width: 100%;
}

.generate-btn {
  height: 42px;
  padding: 0 32px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border: none;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.3);
}

.generate-btn:hover {
  background: linear-gradient(135deg, #B89855 0%, #C9A96E 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(201, 168, 108, 0.4);
}

.report-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.report-section {
  padding: 20px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}

.chart-box {
  height: 320px;
}

.pie-chart {
  height: 280px;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  padding: 20px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  border-radius: 10px;
  border: 1px solid #E8E2D9;
  text-align: center;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #5D4E37;
  font-family: 'Fraunces', serif;
  margin-bottom: 6px;
}

.stat-value.ok {
  color: #43A047;
}

.stat-value.warn {
  color: #F57C00;
}

.stat-value.accent {
  color: #C9A96E;
}

.stat-label {
  font-size: 13px;
  color: #8B7355;
  font-weight: 500;
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
  padding: 12px 14px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 12px;
  letter-spacing: 0.3px;
}

thead th:first-child {
  border-top-left-radius: 10px;
}

thead th:last-child {
  border-top-right-radius: 10px;
}

tbody td {
  padding: 12px 14px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: background-color 0.2s ease;
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.04);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:last-child td:first-child {
  border-bottom-left-radius: 10px;
}

tbody tr:last-child td:last-child {
  border-bottom-right-radius: 10px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #8B7355;
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.status-ok {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #A5D6A7;
}

.status-err {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.status-warn {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #F57C00;
  border: 1px solid #FFCC80;
}

.empty-report,
.empty-template {
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-text {
  font-size: 14px;
  color: #8B7355;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 13px;
  color: #8B7355;
}

.empty-template {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 16px;
  border: 1px solid #E8E2D9;
}

:deep(.el-select) {
  --el-select-border-color-hover: #C9A96E;
  --el-select-input-focus-border-color: #C9A96E;
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

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #C9A96E;
  --el-button-border-color: #C9A96E;
  --el-button-hover-bg-color: #B89855;
  --el-button-hover-border-color: #B89855;
  --el-button-active-bg-color: #A88844;
  --el-button-active-border-color: #A88844;
}

@media (max-width: 1200px) {
  .layout-wrapper {
    grid-template-columns: 1fr;
  }

  .params-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
