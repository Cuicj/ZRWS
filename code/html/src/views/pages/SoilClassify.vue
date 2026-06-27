<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">AI 土质分类</h1>
      <div class="page-meta mono">SOIL CLASSIFY · 深度学习识别</div>
    </div>

    <div class="ai-row">
      <Panel title="分类结果">
        <div class="result-grid">
          <div v-for="r in results" :key="r.name" class="result-card">
            <div class="result-head">
              <span class="result-name">{{ r.name }}</span>
              <span class="result-conf mono">{{ r.confidence }}%</span>
            </div>
            <div class="result-desc">{{ r.description }}</div>
            <div class="result-stats mono">
              <span>pH {{ r.ph }}</span>
              <span>有机质 {{ r.organic }}%</span>
              <span>含水 {{ r.moisture }}%</span>
            </div>
          </div>
        </div>
      </Panel>

      <Panel title="分析记录">
        <table>
          <thead><tr><th>ID</th><th>任务</th><th>采样数</th><th>结果</th><th>置信度</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="a in analysis" :key="a.id">
              <td class="mono">{{ a.id }}</td>
              <td class="mono">{{ a.taskId }}</td>
              <td>{{ a.sampleCount }}</td>
              <td>{{ a.result }}</td>
              <td>{{ a.confidence }}%</td>
              <td class="mono">{{ a.analysisTime }}</td>
            </tr>
          </tbody>
        </table>
      </Panel>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';

const results = ref([
  { name: '水稻土', confidence: 96.5, description: '土质疏松，富含有机质，适合水稻种植', ph: '6.5', organic: '3.2', moisture: '34.5' },
  { name: '菜园土', confidence: 93.2, description: '经过长期耕作改良，肥力较高', ph: '6.8', organic: '2.8', moisture: '31.2' },
  { name: '黄棕壤', confidence: 91.8, description: '中等肥力，适合旱作和果树种植', ph: '6.4', organic: '2.5', moisture: '28.9' }
]);
const analysis = ref([
  { id: 'AI-001', taskId: 'ZRS-2026-0617-001', sampleCount: 36, result: '水稻土', confidence: 94.3, analysisTime: '2026-06-17 12:00' }
]);
</script>

<style scoped>
.page-container {
  padding: 24px;
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
  font-weight: 500;
  color: #5D4E37;
  letter-spacing: 0.5px;
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 2px;
}

.ai-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.result-grid {
  display: grid;
  gap: 16px;
}

.result-card {
  padding: 20px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border-left: 3px solid #C9A86C;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.18);
}

.result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-name {
  font-size: 16px;
  font-weight: 600;
  color: #5D4E37;
}

.result-conf {
  font-size: 13px;
  color: #C9A86C;
  font-weight: 600;
  padding: 4px 10px;
  background: rgba(201, 168, 108, 0.12);
  border-radius: 20px;
}

.result-desc {
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 14px;
  line-height: 1.6;
}

.result-stats {
  display: flex;
  gap: 16px;
  font-size: 11px;
  color: #8B7355;
  flex-wrap: wrap;
}

.result-stats span {
  padding: 4px 10px;
  background: #FEFBF6;
  border-radius: 6px;
  border: 1px solid #E8E2D9;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

thead {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
}

thead th {
  padding: 14px 16px;
  text-align: left;
  color: #5D4E37;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #E8E2D9;
}

tbody tr {
  transition: background 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

tbody tr:hover {
  background: rgba(201, 168, 108, 0.06);
}

tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
}

tbody .mono {
  color: #8B7355;
  font-size: 12px;
}

@media (max-width: 900px) {
  .ai-row {
    grid-template-columns: 1fr;
  }
}
</style>
