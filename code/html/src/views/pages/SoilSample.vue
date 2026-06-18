<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">土壤采样</h1>
      <div class="page-meta mono">SOIL SAMPLE · {{ samples.length }} 个采样点</div>
    </div>

    <div class="stat-row">
      <StatCard label="采样点数" :value="stats.count" icon="◉" variant="accent" />
      <StatCard label="平均 pH" :value="stats.avgPH" icon="◊" />
      <StatCard label="平均含水" :value="stats.avgMoisture" unit="%" icon="≈" />
      <StatCard label="有机质" :value="stats.organic" unit="%" icon="◆" />
    </div>

    <Panel title="采样点列表">
      <table>
        <thead><tr><th>ID</th><th>纬度</th><th>经度</th><th>pH</th><th>含水</th><th>电导率</th><th>土质</th></tr></thead>
        <tbody>
          <tr v-for="s in samples" :key="s.id">
            <td class="mono">{{ s.id }}</td>
            <td class="mono">{{ s.lat }}</td>
            <td class="mono">{{ s.lng }}</td>
            <td>{{ s.ph }}</td>
            <td>{{ s.moisture }}%</td>
            <td>{{ s.ec }}</td>
            <td>{{ s.soilType }}</td>
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

const stats = ref({ count: 36, avgPH: '6.7', avgMoisture: '32.5', organic: '2.84' });
const samples = ref([
  { id: 'S-001', lat: 28.4568, lng: 112.8352, ph: 6.5, moisture: 34.2, ec: 0.32, soilType: '壤土' },
  { id: 'S-002', lat: 28.4570, lng: 112.8358, ph: 6.8, moisture: 31.5, ec: 0.28, soilType: '黏壤土' },
  { id: 'S-003', lat: 28.4572, lng: 112.8362, ph: 7.1, moisture: 28.9, ec: 0.25, soilType: '砂壤土' },
  { id: 'S-004', lat: 28.4575, lng: 112.8365, ph: 6.3, moisture: 36.8, ec: 0.38, soilType: '重黏土' }
]);
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
</style>