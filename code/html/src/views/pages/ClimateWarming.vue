<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">气候变暖监测</h1>
      <div class="page-meta mono">CLIMATE WARMING · 温度异常 / 极端天气 / 变暖趋势</div>
    </div>

    <div class="stat-row">
      <StatCard label="监测区域" :value="stats.regionCount || '-'" icon="📍" variant="info" />
      <StatCard label="平均气温距平" :value="stats.avgAnomaly + '°C' || '-'" icon="🌡️" variant="warn" />
      <StatCard label="极端高温天数" :value="stats.highTempDays || '-'" icon="🔥" variant="danger" />
      <StatCard label="高风险区域" :value="stats.highRiskCount || '-'" icon="⚠️" variant="danger" />
    </div>

    <div class="panel-row">
      <Panel title="温度趋势">
        <div class="chart-placeholder">
          <div class="chart-title">近12个月平均气温距平变化</div>
          <div class="chart-bars">
            <div v-for="(item, i) in trendData" :key="i" class="bar-item">
              <div class="bar" :style="{ height: Math.abs(item.anomaly) * 30 + 'px', background: item.anomaly >= 0 ? '#E8734A' : '#4A90E2' }"></div>
              <div class="bar-label">{{ item.month }}</div>
            </div>
          </div>
        </div>
      </Panel>

      <Panel title="风险等级分布">
        <div class="risk-distribution">
          <div v-for="item in riskDistribution" :key="item.level" class="risk-item">
            <div class="risk-bar">
              <div class="risk-fill" :class="'risk-' + item.level.toLowerCase()" :style="{ width: item.percent + '%' }"></div>
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
      </div>
      <table>
        <thead>
          <tr>
            <th>区域</th>
            <th>监测日期</th>
            <th>平均气温</th>
            <th>气温距平</th>
            <th>高温天数</th>
            <th>干旱天数</th>
            <th>10年变暖率</th>
            <th>风险等级</th>
            <th>风险评分</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filteredRecords" :key="r.recordId">
            <td>{{ r.region }}</td>
            <td>{{ r.monitorDate }}</td>
            <td>{{ r.avgTemperature }}°C</td>
            <td :class="r.temperatureAnomaly >= 0 ? 'text-warm' : 'text-cool'">{{ r.temperatureAnomaly >= 0 ? '+' : '' }}{{ r.temperatureAnomaly }}°C</td>
            <td>{{ r.extremeHighTempDays }}天</td>
            <td>{{ r.droughtDays }}天</td>
            <td>{{ r.warmingRate10y }}°C/10a</td>
            <td><span class="status-badge" :class="getRiskClass(r.riskLevel)">{{ getRiskText(r.riskLevel) }}</span></td>
            <td>{{ r.riskScore }}</td>
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
import { getClimateWarmingList, getClimateWarmingStats, getClimateWarmingTrend, getClimateWarmingRiskDistribution } from '@/api/climateWarming';

const stats = ref({ regionCount: 0, avgAnomaly: 0, highTempDays: 0, highRiskCount: 0 });
const records = ref([]);
const trendData = ref([]);
const riskDistribution = ref([]);
const searchKeyword = ref('');
const riskLevelFilter = ref('');
const loading = ref(true);

const getRiskText = (level) => {
  const map = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险', 'EXTREME': '极高风险' };
  return map[level] || level || '-';
};

const getRiskClass = (level) => {
  const map = { 'LOW': 'status-ok', 'MEDIUM': 'status-warn', 'HIGH': 'status-err', 'EXTREME': 'status-err' };
  return map[level] || 'status-warn';
};

const filteredRecords = computed(() => {
  let result = records.value;
  if (searchKeyword.value) {
    result = result.filter(r => r.region && r.region.includes(searchKeyword.value));
  }
  if (riskLevelFilter.value) {
    result = result.filter(r => r.riskLevel === riskLevelFilter.value);
  }
  return result;
});

const loadData = async () => {
  try {
    loading.value = true;
    const [listRes, statsRes, trendRes, riskRes] = await Promise.all([
      getClimateWarmingList().catch(() => ({ list: [] })),
      getClimateWarmingStats().catch(() => ({ data: {} })),
      getClimateWarmingTrend().catch(() => ({ data: null })),
      getClimateWarmingRiskDistribution().catch(() => ({ list: [] }))
    ]);
    
    if (listRes.list) {
      records.value = listRes.list;
    }
    
    if (statsRes.data) {
      stats.value = {
        regionCount: statsRes.data.totalRecords || 0,
        avgAnomaly: statsRes.data.avgTempAnomaly || 0,
        highTempDays: statsRes.data.highTempDays || 0,
        highRiskCount: statsRes.data.highRiskCount || 0,
        warmingRate: statsRes.data.warmingRate || 0
      };
    }
    
    if (trendRes.data && trendRes.data.months) {
      const months = trendRes.data.months || [];
      const anomalies = trendRes.data.tempAnomalies || [];
      trendData.value = months.map((month, i) => ({
        month,
        anomaly: anomalies[i] || 0
      }));
    }
    
    if (riskRes.list && riskRes.list.length > 0) {
      riskDistribution.value = riskRes.list;
    } else {
      riskDistribution.value = [
        { level: 'LOW', label: '低风险', count: 5, percent: 35 },
        { level: 'MEDIUM', label: '中风险', count: 6, percent: 43 },
        { level: 'HIGH', label: '高风险', count: 2, percent: 14 },
        { level: 'EXTREME', label: '极高风险', count: 1, percent: 8 }
      ];
    }
  } catch (e) {
    console.warn('气候变暖数据加载失败:', e.message);
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
.text-warm { color: #E8734A; font-weight: 600; }
.text-cool { color: #4A90E2; font-weight: 600; }

.chart-placeholder { padding: 20px; min-height: 200px; }
.chart-title { font-size: 13px; color: var(--text-secondary); margin-bottom: 20px; }
.chart-bars { display: flex; align-items: flex-end; justify-content: space-around; height: 150px; border-bottom: 1px solid var(--border-color); }
.bar-item { display: flex; flex-direction: column; align-items: center; gap: 8px; flex: 1; }
.bar { width: 20px; border-radius: 4px 4px 0 0; min-height: 4px; transition: all 0.3s; }
.bar-label { font-size: 11px; color: var(--text-tertiary); }

.risk-distribution { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.risk-item { display: flex; flex-direction: column; gap: 6px; }
.risk-bar { height: 8px; background: var(--bg-secondary); border-radius: 4px; overflow: hidden; }
.risk-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.risk-low { background: #52C41A; }
.risk-medium { background: #FAAD14; }
.risk-high { background: #FA8C16; }
.risk-extreme { background: #F5222D; }
.risk-info { display: flex; justify-content: space-between; font-size: 12px; }
.risk-label { color: var(--text-primary); font-weight: 500; }
.risk-count { color: var(--text-tertiary); }
</style>
