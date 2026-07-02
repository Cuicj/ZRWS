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
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getSoilSampleList, getSoilSampleStats } from '@/api/soilSample.js';

const stats = ref({ count: 0, avgPH: '0', avgMoisture: '0', organic: '0' });
const samples = ref([]);
const loading = ref(false);

// 加载采样数据和统计信息
const loadSoilData = async () => {
  loading.value = true;
  try {
    // 加载采样列表
    const listRes = await getSoilSampleList();
    // 加载统计信息
    const statsRes = await getSoilSampleStats();
    
    if (listRes.list) {
      samples.value = (listRes.list || []).map(item => ({
        id: item.id || item.sampleId,
        lat: item.latitude || item.lat,
        lng: item.longitude || item.lng,
        ph: item.ph || item.phValue,
        moisture: item.moisture || item.moistureContent,
        ec: item.ec || item.electricalConductivity,
        soilType: item.soilType || item.textureType
      }));
    }
    
    if (statsRes.data) {
      const statsData = statsRes.data || {};
      stats.value = {
        count: statsData.totalCount || samples.value.length,
        avgPH: statsData.avgPH || '0',
        avgMoisture: statsData.avgMoisture || '0',
        organic: statsData.avgOrganic || '0'
      };
    }
  } catch (error) {
    console.error('加载采样数据失败:', error);
    ElMessage.error('加载采样数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadSoilData();
});
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
