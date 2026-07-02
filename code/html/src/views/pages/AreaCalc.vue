<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">面积计算</h1>
      <div class="page-meta mono">AREA CALC · GPS vs 登记面积</div>
    </div>

    <div class="stat-row">
      <StatCard label="总地块" :value="stats.totalPlots" icon="◭" variant="accent" />
      <StatCard label="总面积" :value="stats.totalArea" unit="亩" icon="□" />
      <StatCard label="差异地块" :value="stats.diffPlots" icon="!" variant="warn" />
      <StatCard label="平均精度" :value="stats.avgAccuracy" unit="cm" icon="◎" />
    </div>

    <Panel title="地块测量记录">
      <table>
        <thead><tr><th>ID</th><th>名称</th><th>GPS面积</th><th>登记面积</th><th>差异</th><th>精度</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="p in plots" :key="p.id">
            <td class="mono">{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>{{ p.gpsArea }} 亩</td>
            <td>{{ p.regArea }} 亩</td>
            <td :class="Math.abs(p.diff) > 5 ? 'diff-high' : 'diff-normal'">{{ p.diff > 0 ? '+' : '' }}{{ p.diff }}%</td>
            <td>{{ stats.avgAccuracy }} cm</td>
            <td class="mono">{{ p.measureTime }}</td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getLandPlotList, getAreaStats } from '@/api/landPlot.js';

const stats = ref({ totalPlots: 0, totalArea: 0, diffPlots: 0, avgAccuracy: 0 });
const plots = ref([]);
const loading = ref(false);

// 加载地块数据和统计信息
const loadLandData = async () => {
  loading.value = true;
  try {
    // 加载地块列表
    const listRes = await getLandPlotList();
    // 加载统计信息
    const statsRes = await getAreaStats();
    
    if (listRes.data?.list) {
      plots.value = (listRes.data.list || []).map(item => ({
        id: item.id || item.plotId,
        name: item.name || item.plotName,
        gpsArea: item.gpsArea || item.measuredArea,
        regArea: item.regArea || item.registeredArea,
        diff: item.diff || calculateDiff(item.gpsArea, item.regArea),
        measureTime: item.measureTime || item.createTime
      }));
    }

    if (statsRes.data) {
      const statsData = statsRes.data || {};
      stats.value = {
        totalPlots: statsData.totalPlots || plots.value.length,
        totalArea: statsData.totalArea || 0,
        diffPlots: statsData.diffPlots || 0,
        avgAccuracy: statsData.avgAccuracy || 0
      };
    }
  } catch (error) {
    console.error('加载地块数据失败:', error);
    ElMessage.error('加载地块数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 计算差异百分比
const calculateDiff = (gpsArea, regArea) => {
  if (!gpsArea || !regArea) return 0;
  return ((gpsArea - regArea) / regArea * 100).toFixed(2);
};

onMounted(() => {
  loadLandData();
});
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

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
}

thead th {
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  color: #5D4E37;
  font-weight: 600;
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 13px;
  letter-spacing: 0.3px;
}

thead th:first-child {
  border-top-left-radius: 12px;
}

thead th:last-child {
  border-top-right-radius: 12px;
}

tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tbody tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #8B7355;
}

.diff-normal {
  color: #8B7355;
  font-weight: 500;
}

.diff-high {
  color: #E53935;
  font-weight: 600;
}
</style>
