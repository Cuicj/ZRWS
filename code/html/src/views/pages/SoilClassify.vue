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
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.ai-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-3); }
.result-grid { display: grid; gap: var(--s-3); }
.result-card { padding: var(--s-4); background: var(--ink-700); border-left: 2px solid var(--sand-500); }
.result-head { display: flex; justify-content: space-between; margin-bottom: var(--s-2); }
.result-name { font-size: 16px; font-weight: 500; }
.result-conf { font-size: 12px; color: var(--sand-500); }
.result-desc { font-size: 13px; color: var(--signal-dim); margin-bottom: var(--s-2); }
.result-stats { display: flex; gap: var(--s-3); font-size: 11px; color: var(--signal-dim); }
</style>