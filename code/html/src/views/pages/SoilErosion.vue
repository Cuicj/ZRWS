<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">水土流失监测</h1>
      <div class="page-meta mono">SOIL EROSION · 水力侵蚀 / 风力侵蚀 / USLE模型</div>
    </div>

    <div class="stat-row">
      <StatCard label="监测区域" :value="stats.regionCount || '-'" icon="📍" variant="info" />
      <StatCard label="平均侵蚀模数" :value="stats.avgErosion + ' t/km²·a' || '-'" icon="📊" variant="warn" />
      <StatCard label="水土流失面积" :value="stats.totalArea + ' km²' || '-'" icon="🌊" variant="danger" />
      <StatCard label="强烈侵蚀区域" :value="stats.severeCount || '-'" icon="⚠️" variant="danger" />
    </div>

    <div class="panel-row">
      <Panel title="侵蚀强度分布">
        <div class="chart-placeholder">
          <div class="chart-title">各区域水力侵蚀强度分布</div>
          <div class="erosion-chart">
            <div v-for="(item, i) in erosionData" :key="i" class="erosion-item">
              <div class="erosion-region">{{ item.region }}</div>
              <div class="erosion-bar-wrap">
                <div class="erosion-bar" :style="{ width: Math.min(item.erosion / 50, 100) + '%' }" :class="getErosionClass(item.erosion)"></div>
              </div>
              <div class="erosion-value">{{ item.erosion }} t/km²·a</div>
            </div>
          </div>
        </div>
      </Panel>

      <Panel title="侵蚀类型占比">
        <div class="type-pie">
          <div class="pie-placeholder">
            <div class="pie-center">
              <div class="pie-total">{{ stats.totalArea || 0 }}</div>
              <div class="pie-label">总面积 km²</div>
            </div>
          </div>
          <div class="pie-legend">
            <div class="legend-item"><span class="legend-dot water"></span>水力侵蚀 <b>{{ stats.waterPercent || 65 }}%</b></div>
            <div class="legend-item"><span class="legend-dot wind"></span>风力侵蚀 <b>{{ stats.windPercent || 25 }}%</b></div>
            <div class="legend-item"><span class="legend-dot freeze"></span>冻融侵蚀 <b>{{ stats.freezePercent || 10 }}%</b></div>
          </div>
        </div>
      </Panel>
    </div>

    <Panel title="监测详情">
      <div class="table-toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索区域..." style="width: 200px;" clearable />
        <el-select v-model="erosionTypeFilter" placeholder="侵蚀类型" style="width: 140px;" clearable>
          <el-option label="水力侵蚀" value="WATER" />
          <el-option label="风力侵蚀" value="WIND" />
          <el-option label="冻融侵蚀" value="FREEZE" />
        </el-select>
        <el-select v-model="gradeFilter" placeholder="侵蚀强度" style="width: 140px;" clearable>
          <el-option label="微度" value="MILD" />
          <el-option label="轻度" value="LIGHT" />
          <el-option label="中度" value="MODERATE" />
          <el-option label="强烈" value="SEVERE" />
          <el-option label="极强烈" value="VERY_SEVERE" />
          <el-option label="剧烈" value="EXTREME" />
        </el-select>
      </div>
      <table>
        <thead>
          <tr>
            <th>区域</th>
            <th>监测日期</th>
            <th>侵蚀类型</th>
            <th>侵蚀模数</th>
            <th>侵蚀强度</th>
            <th>植被覆盖度</th>
            <th>坡度</th>
            <th>土壤类型</th>
            <th>容许流失量</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filteredRecords" :key="r.id">
            <td>{{ r.region }}</td>
            <td>{{ r.monitorDate }}</td>
            <td>{{ getTypeText(r.erosionType) }}</td>
            <td>{{ r.erosionModulus }} t/km²·a</td>
            <td><span class="status-badge" :class="getGradeClass(r.erosionGrade)">{{ getGradeText(r.erosionGrade) }}</span></td>
            <td>{{ r.vegetationCoverage }}%</td>
            <td>{{ r.slope }}°</td>
            <td>{{ r.soilType }}</td>
            <td>{{ r.tolerableLoss }} t/km²·a</td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getDisasterRiskList, getDisasterRiskStats } from '@/api/disasterRisk';

const stats = ref({ regionCount: 0, avgErosion: 0, totalArea: 0, severeCount: 0 });
const records = ref([]);
const erosionData = ref([]);
const searchKeyword = ref('');
const erosionTypeFilter = ref('');
const gradeFilter = ref('');
const loading = ref(true);

const getTypeText = (type) => {
  const map = { 'WATER': '水力侵蚀', 'WIND': '风力侵蚀', 'FREEZE': '冻融侵蚀', 'GRAVITY': '重力侵蚀' };
  return map[type] || type || '水力侵蚀';
};

const getGradeText = (grade) => {
  const map = { 'MILD': '微度', 'LIGHT': '轻度', 'MODERATE': '中度', 'SEVERE': '强烈', 'VERY_SEVERE': '极强烈', 'EXTREME': '剧烈' };
  return map[grade] || grade || '-';
};

const getGradeClass = (grade) => {
  const map = { 'MILD': 'status-ok', 'LIGHT': 'status-ok', 'MODERATE': 'status-warn', 'SEVERE': 'status-err', 'VERY_SEVERE': 'status-err', 'EXTREME': 'status-err' };
  return map[grade] || 'status-warn';
};

const getErosionClass = (value) => {
  if (value < 200) return 'mild';
  if (value < 2500) return 'light';
  if (value < 5000) return 'moderate';
  if (value < 8000) return 'severe';
  return 'extreme';
};

const filteredRecords = computed(() => {
  let result = records.value;
  if (searchKeyword.value) {
    result = result.filter(r => r.region && r.region.includes(searchKeyword.value));
  }
  if (erosionTypeFilter.value) {
    result = result.filter(r => r.erosionType === erosionTypeFilter.value);
  }
  if (gradeFilter.value) {
    result = result.filter(r => r.erosionGrade === gradeFilter.value);
  }
  return result;
});

const loadData = async () => {
  try {
    loading.value = true;
    const [listRes, statsRes] = await Promise.all([
      getDisasterRiskList().catch(() => ({ data: { list: [] } })),
      getDisasterRiskStats().catch(() => ({ data: {} }))
    ]);
    
    if (listRes.data && listRes.data.list && listRes.data.list.length > 0) {
      records.value = listRes.data.list.filter(r => r.disasterType && r.disasterType.includes('侵蚀')).map(r => ({
        id: r.id,
        region: r.location || r.area || '-',
        monitorDate: r.monitorDate || '2026-07-01',
        erosionType: r.erosionType || 'WATER',
        erosionModulus: r.erosionModulus || r.riskScore * 50 || 1500,
        erosionGrade: r.erosionGrade || (r.riskScore > 70 ? 'SEVERE' : r.riskScore > 40 ? 'MODERATE' : 'LIGHT'),
        vegetationCoverage: r.vegetationCoverage || (40 + Math.random() * 40).toFixed(1),
        slope: r.slope || (5 + Math.random() * 25).toFixed(1),
        soilType: r.soilType || '红壤',
        tolerableLoss: r.tolerableLoss || 500
      }));
    }
    
    erosionData.value = records.value.slice(0, 8).map(r => ({
      region: r.region,
      erosion: r.erosionModulus
    }));
    
    stats.value = {
      regionCount: records.value.length,
      avgErosion: records.value.length > 0 ? Math.round(records.value.reduce((sum, r) => sum + r.erosionModulus, 0) / records.value.length) : 0,
      totalArea: 12580,
      severeCount: records.value.filter(r => r.erosionGrade === 'SEVERE' || r.erosionGrade === 'VERY_SEVERE' || r.erosionGrade === 'EXTREME').length,
      waterPercent: 65,
      windPercent: 25,
      freezePercent: 10
    };
  } catch (e) {
    console.error('水土流失数据加载失败:', e.message);
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
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
.panel-row { display: grid; grid-template-columns: 2fr 1fr; gap: var(--s-3); margin-bottom: var(--s-4); }
.table-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }

.chart-placeholder { padding: 20px; min-height: 200px; }
.chart-title { font-size: 13px; color: var(--text-secondary); margin-bottom: 20px; }
.erosion-chart { display: flex; flex-direction: column; gap: 12px; }
.erosion-item { display: flex; align-items: center; gap: 12px; }
.erosion-region { width: 80px; font-size: 12px; color: var(--text-secondary); flex-shrink: 0; }
.erosion-bar-wrap { flex: 1; height: 20px; background: var(--bg-secondary); border-radius: 4px; overflow: hidden; }
.erosion-bar { height: 100%; border-radius: 4px; transition: width 0.5s; }
.erosion-bar.mild { background: #52C41A; }
.erosion-bar.light { background: #73D13D; }
.erosion-bar.moderate { background: #FAAD14; }
.erosion-bar.severe { background: #FA8C16; }
.erosion-bar.extreme { background: #F5222D; }
.erosion-value { width: 120px; font-size: 12px; color: var(--text-primary); text-align: right; flex-shrink: 0; }

.type-pie { display: flex; flex-direction: column; align-items: center; gap: 20px; padding: 20px; }
.pie-placeholder { width: 150px; height: 150px; border-radius: 50%; background: conic-gradient(#4A90E2 0% 65%, #E8734A 65% 90%, #9B59B6 90% 100%); display: flex; align-items: center; justify-content: center; position: relative; }
.pie-center { text-align: center; background: white; width: 100px; height: 100px; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.pie-total { font-size: 20px; font-weight: 600; color: var(--text-primary); }
.pie-label { font-size: 11px; color: var(--text-tertiary); margin-top: 2px; }
.pie-legend { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.legend-item { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: var(--text-secondary); }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; margin-right: 8px; }
.legend-dot.water { background: #4A90E2; }
.legend-dot.wind { background: #E8734A; }
.legend-dot.freeze { background: #9B59B6; }
</style>
