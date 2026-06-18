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
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';

const risks = ref({ landslide: '高', flood: '中', mudslide: '低', erosion: '低' });
const records = ref([
  { area: '乔口镇 A区', type: '滑坡', levelText: '高风险', levelClass: 'status-err', score: 78, advice: '建议设置抗滑桩并加强排水' },
  { area: '乔口镇 B区', type: '洪涝', levelText: '中风险', levelClass: 'status-warn', score: 62, advice: '完善排洪沟渠，定期清理淤泥' },
  { area: '莲花镇', type: '泥石流', levelText: '低风险', levelClass: 'status-ok', score: 28, advice: '定期植被恢复和水土保持' }
]);
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.risk-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
</style>