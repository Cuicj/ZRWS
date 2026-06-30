<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">沙漠化监测</h1>
      <div class="page-meta mono">DESERTIFICATION · 植被覆盖 / 风蚀强度 / 土地退化</div>
    </div>

    <div class="stat-row">
      <StatCard label="监测区域" :value="stats.regionCount || '-'" icon="📍" variant="info" />
      <StatCard label="平均植被覆盖度" :value="stats.avgVegetation + '%' || '-'" icon="🌿" variant="ok" />
      <StatCard label="沙漠化面积" :value="stats.totalArea + 'km²' || '-'" icon="🏜️" variant="warn" />
      <StatCard label="高风险区域" :value="stats.highRiskCount || '-'" icon="⚠️" variant="danger" />
    </div>

    <div class="panel-row">
      <Panel title="植被覆盖度趋势">
        <div class="chart-placeholder">
          <div class="chart-title">近12个月植被覆盖度变化</div>
          <div class="chart-bars">
            <div v-for="(item, i) in trendData" :key="i" class="bar-item">
              <div class="bar veg-bar" :style="{ height: item.coverage * 1.8 + 'px' }"></div>
              <div class="bar-label">{{ item.month }}</div>
            </div>
          </div>
        </div>
      </Panel>

      <Panel title="沙漠化程度分布">
        <div class="risk-distribution">
          <div v-for="item in gradeDistribution" :key="item.grade" class="risk-item">
            <div class="risk-bar">
              <div class="risk-fill" :class="'grade-' + item.grade.toLowerCase()" :style="{ width: item.percent + '%' }"></div>
            </div>
            <div class="risk-info">
              <span class="risk-label">{{ item.label }}</span>
              <span class="risk-count">{{ item.count }} 个区域</span>
            </div>
          </div>
        </div>
      </Panel>
    </div>

    <Panel title="监测详情">
      <div class="table-toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索区域..." style="width: 200px;" clearable />
        <el-select v-model="riskLevelFilter" placeholder="风险等级" style="width: 140px;" clearable>
          <el-option label="低风险" value="LOW" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
          <el-option label="极高风险" value="EXTREME" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="沙漠化类型" style="width: 140px;" clearable>
          <el-option label="风蚀沙漠化" value="WIND" />
          <el-option label="水蚀沙漠化" value="WATER" />
          <el-option label="盐渍化沙漠化" value="SALT" />
          <el-option label="冻融沙漠化" value="FREEZE" />
        </el-select>
      </div>
      <table>
        <thead>
          <tr>
            <th>区域</th>
            <th>监测日期</th>
            <th>植被覆盖度</th>
            <th>裸地占比</th>
            <th>干旱指数</th>
            <th>风蚀模数</th>
            <th>沙漠化类型</th>
            <th>沙漠化程度</th>
            <th>风险等级</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filteredRecords" :key="r.recordId">
            <td>{{ r.region }}</td>
            <td>{{ r.monitorDate }}</td>
            <td>
              <div class="coverage-cell">
                <div class="coverage-bar"><div class="coverage-fill" :style="{ width: r.vegetationCoverage + '%' }"></div></div>
                <span>{{ r.vegetationCoverage }}%</span>
              </div>
            </td>
            <td>{{ r.bareLandRatio }}%</td>
            <td>{{ r.aridityIndex }}</td>
            <td>{{ r.windErosionModulus }} t/km²·a</td>
            <td>{{ getTypeText(r.desertificationType) }}</td>
            <td><span class="status-badge" :class="getGradeClass(r.desertificationGrade)">{{ getGradeText(r.desertificationGrade) }}</span></td>
            <td><span class="status-badge" :class="getRiskClass(r.riskLevel)">{{ getRiskText(r.riskLevel) }}</span></td>
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
import { getDesertificationList, getDesertificationStats, getDesertificationTrend, getDesertificationRiskDistribution } from '@/api/desertification';

const stats = ref({ regionCount: 0, avgVegetation: 0, totalArea: 0, highRiskCount: 0 });
const records = ref([]);
const trendData = ref([]);
const gradeDistribution = ref([]);
const searchKeyword = ref('');
const riskLevelFilter = ref('');
const typeFilter = ref('');
const loading = ref(true);

const getRiskText = (level) => {
  const map = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险', 'EXTREME': '极高风险' };
  return map[level] || level || '-';
};

const getRiskClass = (level) => {
  const map = { 'LOW': 'status-ok', 'MEDIUM': 'status-warn', 'HIGH': 'status-err', 'EXTREME': 'status-err' };
  return map[level] || 'status-warn';
};

const getGradeText = (grade) => {
  const map = { 'MILD': '轻度', 'MODERATE': '中度', 'SEVERE': '重度', 'EXTREME': '极重度' };
  return map[grade] || grade || '-';
};

const getGradeClass = (grade) => {
  const map = { 'MILD': 'status-ok', 'MODERATE': 'status-warn', 'SEVERE': 'status-err', 'EXTREME': 'status-err' };
  return map[grade] || 'status-warn';
};

const getTypeText = (type) => {
  const map = { 'WIND': '风蚀沙漠化', 'WATER': '水蚀沙漠化', 'SALT': '盐渍化沙漠化', 'FREEZE': '冻融沙漠化' };
  return map[type] || type || '-';
};

const filteredRecords = computed(() => {
  let result = records.value;
  if (searchKeyword.value) {
    result = result.filter(r => r.region && r.region.includes(searchKeyword.value));
  }
  if (riskLevelFilter.value) {
    result = result.filter(r => r.riskLevel === riskLevelFilter.value);
  }
  if (typeFilter.value) {
    result = result.filter(r => r.desertificationType === typeFilter.value);
  }
  return result;
});

const loadData = async () => {
  try {
    loading.value = true;
    const [listRes, statsRes, trendRes, riskRes] = await Promise.all([
      getDesertificationList().catch(() => ({ data: [] })),
      getDesertificationStats().catch(() => ({ data: {} })),
      getDesertificationTrend().catch(() => ({ data: [] })),
      getDesertificationRiskDistribution().catch(() => ({ data: [] }))
    ]);
    
    if (listRes.data) {
      records.value = listRes.data;
    }
    
    if (statsRes.data) {
      stats.value = statsRes.data;
    }
    
    if (trendRes.data && trendRes.data.length > 0) {
      trendData.value = trendRes.data;
    } else {
      trendData.value = generateMockTrend();
    }
    
    if (riskRes.data && riskRes.data.length > 0) {
      gradeDistribution.value = riskRes.data;
    } else {
      gradeDistribution.value = [
        { grade: 'MILD', label: '轻度沙漠化', count: 6, percent: 43 },
        { grade: 'MODERATE', label: '中度沙漠化', count: 4, percent: 29 },
        { grade: 'SEVERE', label: '重度沙漠化', count: 3, percent: 21 },
        { grade: 'EXTREME', label: '极重度沙漠化', count: 1, percent: 7 }
      ];
    }
  } catch (e) {
    console.warn('沙漠化数据加载失败:', e.message);
  } finally {
    loading.value = false;
  }
};

const generateMockTrend = () => {
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
  return months.map(m => ({
    month: m,
    coverage: +(40 + Math.random() * 35).toFixed(1)
  }));
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
.chart-bars { display: flex; align-items: flex-end; justify-content: space-around; height: 150px; border-bottom: 1px solid var(--border-color); }
.bar-item { display: flex; flex-direction: column; align-items: center; gap: 8px; flex: 1; }
.bar { width: 20px; border-radius: 4px 4px 0 0; min-height: 4px; transition: all 0.3s; }
.veg-bar { background: linear-gradient(180deg, #52C41A 0%, #389E0D 100%); }
.bar-label { font-size: 11px; color: var(--text-tertiary); }

.risk-distribution { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.risk-item { display: flex; flex-direction: column; gap: 6px; }
.risk-bar { height: 8px; background: var(--bg-secondary); border-radius: 4px; overflow: hidden; }
.risk-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.grade-mild { background: #52C41A; }
.grade-moderate { background: #FAAD14; }
.grade-severe { background: #FA8C16; }
.grade-extreme { background: #F5222D; }
.risk-info { display: flex; justify-content: space-between; font-size: 12px; }
.risk-label { color: var(--text-primary); font-weight: 500; }
.risk-count { color: var(--text-tertiary); }

.coverage-cell { display: flex; align-items: center; gap: 8px; min-width: 120px; }
.coverage-bar { flex: 1; height: 6px; background: var(--bg-secondary); border-radius: 3px; overflow: hidden; }
.coverage-fill { height: 100%; background: linear-gradient(90deg, #52C41A, #73D13D); border-radius: 3px; }
</style>
