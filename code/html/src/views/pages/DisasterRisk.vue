<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">灾害风险评估</h1>
      <div class="page-meta mono">DISASTER RISK · 滑坡/洪涝/泥石流</div>
    </div>

    <div class="risk-row">
      <StatCard label="滑坡风险" :value="risks.landslide" icon="◬" variant="danger" />
      <StatCard label="洪涝风险" :value="risks.flood" icon="≈" variant="warn" />
      <StatCard label="泥石流" :value="risks.mudslide" icon="◈" variant="ok" />
      <StatCard label="土壤侵蚀" :value="risks.erosion" icon="◊" variant="ok" />
    </div>

    <Panel title="风险详情">
      <table>
        <thead><tr><th>区域</th><th>灾害类型</th><th>风险等级</th><th>评分</th><th>建议</th></tr></thead>
        <tbody>
          <tr v-for="r in records" :key="r.area">
            <td>{{ r.area }}</td>
            <td>{{ r.type }}</td>
            <td><span class="status-badge" :class="r.levelClass">{{ r.levelText }}</span></td>
            <td>{{ r.score }}</td>
            <td>{{ r.advice }}</td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getDisasterRiskList, getDisasterRiskStats } from '@/api/disasterRisk';

const risks = ref({ landslide: '-', flood: '-', mudslide: '-', erosion: '-' });
const records = ref([]);
const loading = ref(true);

const getLevelText = (level) => {
  const map = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险', 'VERY_HIGH': '极高风险' };
  return map[level] || level || '-';
};

const getLevelClass = (level) => {
  const map = { 'LOW': 'status-ok', 'MEDIUM': 'status-warn', 'HIGH': 'status-err', 'VERY_HIGH': 'status-err' };
  return map[level] || 'status-warn';
};

const getRiskIcon = (type) => {
  const map = { '滑坡': '◬', '洪涝': '≈', '泥石流': '◈', '土壤侵蚀': '◊', '地面沉降': '▽' };
  return map[type] || '◉';
};

const loadData = async () => {
  try {
    loading.value = true;
    const [listRes, statsRes] = await Promise.all([
      getDisasterRiskList().catch(() => ({ data: [] })),
      getDisasterRiskStats().catch(() => ({ data: {} }))
    ]);
    
    if (listRes.data) {
      records.value = listRes.data.map(r => ({
        area: r.location || r.area || '-',
        type: r.disasterType || '-',
        levelText: getLevelText(r.riskLevel),
        levelClass: getLevelClass(r.riskLevel),
        score: r.riskScore || 0,
        advice: r.suggestion || r.monitoringStatus || '-'
      }));
    }
    
    if (statsRes.data) {
      risks.value = {
        landslide: statsRes.data.landslide || '-',
        flood: statsRes.data.flood || '-',
        mudslide: statsRes.data.mudslide || '-',
        erosion: statsRes.data.erosion || '-'
      };
    }
  } catch (e) {
    console.warn('灾害风险数据加载失败:', e.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.risk-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
</style>