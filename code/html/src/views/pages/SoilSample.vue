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
      <div class="table-wrapper">
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
      </div>
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
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100%;
}
.page-head {
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: 1px solid #E8E2D9;
}
.page-title {
  font-size: 28px;
  font-weight: 200;
  color: #5D4E37;
}
.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-3);
  margin-bottom: var(--s-4);
}
.table-wrapper {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
table {
  width: 100%;
  border-collapse: collapse;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
}
thead {
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E0 100%);
}
th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #E8E2D9;
}
td {
  padding: 14px 16px;
  font-size: 13px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
tbody tr:last-child td {
  border-bottom: none;
}
tbody tr:hover td {
  background: rgba(201, 168, 108, 0.08);
}
.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
  color: #8B7355;
}
</style>
