<template>
  <div class="land-map-page">
    <div class="page-header">
      <h2>土地资源地图</h2>
      <div class="header-actions">
        <el-select v-model="selectedYear" placeholder="选择年份" @change="updateMapData">
          <el-option label="2024年" value="2024" />
          <el-option label="2025年" value="2025" />
          <el-option label="2026年" value="2026" />
        </el-select>
        <el-select v-model="mapType" placeholder="地图类型" @change="updateMapType">
          <el-option label="土地面积" value="area" />
          <el-option label="耕地分布" value="cultivated" />
          <el-option label="土壤类型" value="soil" />
          <el-option label="灾害风险" value="disaster" />
        </el-select>
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </div>

    <div class="map-container">
      <div ref="mapRef" class="map-chart"></div>
      <div class="map-legend">
        <div class="legend-title">{{ legendTitle }}</div>
        <div class="legend-items">
          <div v-for="item in legendItems" :key="item.label" class="legend-item">
            <span class="legend-color" :style="{ background: item.color }"></span>
            <span class="legend-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon">🌍</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalArea.toLocaleString() }}</div>
          <div class="stat-label">总面积(亩)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🌾</div>
        <div class="stat-content">
          <div class="stat-value">{{ cultivatedArea.toLocaleString() }}</div>
          <div class="stat-label">耕地面积(亩)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🌿</div>
        <div class="stat-content">
          <div class="stat-value">{{ forestArea.toLocaleString() }}</div>
          <div class="stat-label">林地面积(亩)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏗️</div>
        <div class="stat-content">
          <div class="stat-value">{{ constructionArea.toLocaleString() }}</div>
          <div class="stat-label">建设用地(亩)</div>
        </div>
      </div>
    </div>

    <div class="detail-panel">
      <div class="panel-header">
        <h3>土地划分详情</h3>
        <el-button type="text" @click="togglePanel">{{ panelExpanded ? '收起' : '展开' }}</el-button>
      </div>
      <div class="panel-content" v-show="panelExpanded">
        <el-table :data="landDetailData" stripe border>
          <el-table-column prop="province" label="省份" width="120" />
          <el-table-column prop="total" label="总面积(亩)" width="150" />
          <el-table-column prop="cultivated" label="耕地(亩)" width="150" />
          <el-table-column prop="forest" label="林地(亩)" width="150" />
          <el-table-column prop="construction" label="建设用地(亩)" width="150" />
          <el-table-column prop="water" label="水域(亩)" width="150" />
          <el-table-column prop="ratio" label="耕地占比" width="120">
            <template #default="{ row }">
              <span :style="{ color: getRatioColor(row.ratio) }">{{ row.ratio }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <h3>土地类型占比</h3>
        <div ref="pieRef" class="chart-content"></div>
      </div>
      <div class="chart-card">
        <h3>各省份土地面积排名</h3>
        <div ref="barRef" class="chart-content"></div>
      </div>
    </div>
  </div>
</template>

<script setup>import { ref, onMounted, computed } from 'vue';
import * as echarts from 'echarts';
const mapRef = ref(null);
const pieRef = ref(null);
const barRef = ref(null);
const selectedYear = ref('2026');
const mapType = ref('area');
const panelExpanded = ref(true);
let mapChart = null;
let pieChart = null;
let barChart = null;
const legendTitle = computed(() => {
 const titles = {
 area: '土地面积分布',
 cultivated: '耕地面积分布',
 soil: '土壤类型分布',
 disaster: '灾害风险等级'
 };
 return titles[mapType.value] || '土地面积分布';
});
const legendItems = computed(() => {
 if (mapType.value === 'disaster') {
 return [
 { label: '低风险', color: '#52c41a' },
 { label: '中风险', color: '#faad14' },
 { label: '高风险', color: '#ff4d4f' },
 { label: '极高风险', color: '#722ed1' }
 ];
 }
 return [
 { label: '0-500万亩', color: '#e6f7ff' },
 { label: '500-1000万亩', color: '#91d5ff' },
 { label: '1000-2000万亩', color: '#40a9ff' },
 { label: '2000-5000万亩', color: '#096dd9' },
 { label: '5000万亩以上', color: '#003a8c' }
 ];
});
const landData = ref([
 { name: '北京', area: 16411000, cultivated: 3400000, forest: 6800000, construction: 2100000, water: 800000, disaster: '低' },
 { name: '天津', area: 11966600, cultivated: 4800000, forest: 2200000, construction: 1500000, water: 1200000, disaster: '低' },
 { name: '河北', area: 188800000, cultivated: 98000000, forest: 45000000, construction: 18000000, water: 8000000, disaster: '中' },
 { name: '山西', area: 156700000, cultivated: 58000000, forest: 62000000, construction: 12000000, water: 4000000, disaster: '低' },
 { name: '内蒙古', area: 1183000000, cultivated: 120000000, forest: 280000000, construction: 15000000, water: 65000000, disaster: '低' },
 { name: '辽宁', area: 148000000, cultivated: 65000000, forest: 52000000, construction: 18000000, water: 10000000, disaster: '中' },
 { name: '吉林', area: 187400000, cultivated: 85000000, forest: 78000000, construction: 8000000, water: 12000000, disaster: '低' },
 { name: '黑龙江', area: 473000000, cultivated: 210000000, forest: 190000000, construction: 12000000, water: 45000000, disaster: '低' },
 { name: '上海', area: 6340500, cultivated: 2300000, forest: 600000, construction: 1800000, water: 800000, disaster: '低' },
 { name: '江苏', area: 107200000, cultivated: 58000000, forest: 12000000, construction: 22000000, water: 10000000, disaster: '中' },
 { name: '浙江', area: 105500000, cultivated: 29000000, forest: 59000000, construction: 10000000, water: 4500000, disaster: '中' },
 { name: '安徽', area: 140100000, cultivated: 73000000, forest: 36000000, construction: 16000000, water: 10000000, disaster: '中' },
 { name: '福建', area: 121400000, cultivated: 20000000, forest: 76000000, construction: 7000000, water: 5500000, disaster: '中' },
 { name: '江西', area: 166900000, cultivated: 36000000, forest: 102000000, construction: 8000000, water: 12000000, disaster: '中' },
 { name: '山东', area: 157100000, cultivated: 83000000, forest: 28000000, construction: 25000000, water: 7000000, disaster: '中' },
 { name: '河南', area: 167000000, cultivated: 81000000, forest: 35000000, construction: 23000000, water: 12000000, disaster: '高' },
 { name: '湖北', area: 185900000, cultivated: 50000000, forest: 75000000, construction: 18000000, water: 30000000, disaster: '中' },
 { name: '湖南', area: 211800000, cultivated: 42000000, forest: 122000000, construction: 13000000, water: 20000000, disaster: '中' },
 { name: '广东', area: 179700000, cultivated: 42000000, forest: 90000000, construction: 25000000, water: 12000000, disaster: '中' },
 { name: '广西', area: 237600000, cultivated: 43000000, forest: 146000000, construction: 10000000, water: 21000000, disaster: '中' },
 { name: '海南', area: 35400000, cultivated: 7000000, forest: 20000000, construction: 3000000, water: 3000000, disaster: '低' },
 { name: '重庆', area: 82400000, cultivated: 25000000, forest: 44000000, construction: 5000000, water: 5000000, disaster: '中' },
 { name: '四川', area: 486000000, cultivated: 90000000, forest: 260000000, construction: 18000000, water: 55000000, disaster: '中' },
 { name: '贵州', area: 176200000, cultivated: 45000000, forest: 86000000, construction: 8000000, water: 10000000, disaster: '高' },
 { name: '云南', area: 394100000, cultivated: 60000000, forest: 210000000, construction: 9000000, water: 28000000, disaster: '中' },
 { name: '西藏', area: 1230000000, cultivated: 3000000, forest: 140000000, construction: 2000000, water: 65000000, disaster: '低' },
 { name: '陕西', area: 205600000, cultivated: 58000000, forest: 80000000, construction: 18000000, water: 16000000, disaster: '中' },
 { name: '甘肃', area: 425800000, cultivated: 50000000, forest: 55000000, construction: 8000000, water: 12000000, disaster: '低' },
 { name: '青海', area: 722300000, cultivated: 8000000, forest: 45000000, construction: 3000000, water: 108000000, disaster: '低' },
 { name: '宁夏', area: 66400000, cultivated: 21000000, forest: 15000000, construction: 4000000, water: 6000000, disaster: '中' },
 { name: '新疆', area: 1664900000, cultivated: 75000000, forest: 68000000, construction: 12000000, water: 95000000, disaster: '低' }
]);
const totalArea = computed(() => landData.value.reduce((sum, item) => sum + item.area, 0));
const cultivatedArea = computed(() => landData.value.reduce((sum, item) => sum + item.cultivated, 0));
const forestArea = computed(() => landData.value.reduce((sum, item) => sum + item.forest, 0));
const constructionArea = computed(() => landData.value.reduce((sum, item) => sum + item.construction, 0));
const landDetailData = computed(() => landData.value.map(item => ({
 province: item.name,
 total: item.area.toLocaleString(),
 cultivated: item.cultivated.toLocaleString(),
 forest: item.forest.toLocaleString(),
 construction: item.construction.toLocaleString(),
 water: item.water.toLocaleString(),
 ratio: ((item.cultivated / item.area) * 100).toFixed(1)
})));
const getRatioColor = (ratio) => {
 const r = parseFloat(ratio);
 if (r > 50)
 return '#52c41a';
 if (r > 30)
 return '#faad14';
 return '#ff4d4f';
};
const initMap = () => {
 if (!mapRef.value)
 return;
 mapChart = echarts.init(mapRef.value);
 const getMapValue = (item) => {
 switch (mapType.value) {
 case 'cultivated': return item.cultivated;
 case 'soil': return item.area;
 case 'disaster':
 const disasterMap = { '低': 1, '中': 2, '高': 3, '极高': 4 };
 return disasterMap[item.disaster] || 1;
 default: return item.area;
 }
 };
 const mapOption = {
 backgroundColor: 'transparent',
 tooltip: {
 trigger: 'item',
 formatter: (params) => {
 const item = landData.value.find(d => d.name === params.name);
 if (!item)
 return '';
 return `
 <div style="font-weight:bold;margin-bottom:8px;">${params.name}</div>
 <div>总面积: ${(item.area / 10000).toFixed(1)}万亩</div>
 <div>耕地: ${(item.cultivated / 10000).toFixed(1)}万亩</div>
 <div>林地: ${(item.forest / 10000).toFixed(1)}万亩</div>
 <div>建设用地: ${(item.construction / 10000).toFixed(1)}万亩</div>
 <div>灾害风险: ${item.disaster}</div>
 `;
 }
 },
 visualMap: {
 min: 0,
 max: mapType.value === 'disaster' ? 4 : 500000000,
 left: 'left',
 top: 'bottom',
 text: ['高', '低'],
 inRange: {
 color: mapType.value === 'disaster'
 ? ['#52c41a', '#faad14', '#ff4d4f', '#722ed1']
 : ['#e6f7ff', '#91d5ff', '#40a9ff', '#096dd9', '#003a8c']
 },
 textStyle: {
 color: '#fff'
 }
 },
 series: [
 {
 name: legendTitle.value,
 type: 'map',
 map: 'china',
 roam: true,
 zoom: 1.2,
 label: {
 show: true,
 color: '#fff',
 fontSize: 10
 },
 emphasis: {
 label: {
 color: '#fff',
 fontSize: 12
 },
 itemStyle: {
 areaColor: '#1890ff'
 }
 },
 data: landData.value.map(item => ({
 name: item.name,
 value: getMapValue(item)
 }))
 }
 ]
 };
 mapChart.setOption(mapOption);
};
const initPie = () => {
 if (!pieRef.value)
 return;
 pieChart = echarts.init(pieRef.value);
 const total = totalArea.value;
 const option = {
 backgroundColor: 'transparent',
 tooltip: {
 trigger: 'item',
 formatter: '{b}: {c}亩 ({d}%)'
 },
 legend: {
 orient: 'vertical',
 right: 10,
 top: 'center',
 textStyle: {
 color: '#fff'
 }
 },
 series: [
 {
 name: '土地类型',
 type: 'pie',
 radius: ['40%', '70%'],
 avoidLabelOverlap: false,
 itemStyle: {
 borderRadius: 8,
 borderColor: '#1a1a2e',
 borderWidth: 2
 },
 label: {
 show: true,
 color: '#fff'
 },
 emphasis: {
 label: {
 show: true,
 fontSize: 14,
 fontWeight: 'bold'
 }
 },
 data: [
 { value: cultivatedArea.value, name: '耕地', itemStyle: { color: '#52c41a' } },
 { value: forestArea.value, name: '林地', itemStyle: { color: '#13c2c2' } },
 { value: constructionArea.value, name: '建设用地', itemStyle: { color: '#faad14' } },
 { value: landData.value.reduce((sum, item) => sum + item.water, 0), name: '水域', itemStyle: { color: '#1890ff' } },
 { value: total - cultivatedArea.value - forestArea.value - constructionArea.value - landData.value.reduce((sum, item) => sum + item.water, 0), name: '其他', itemStyle: { color: '#722ed1' } }
 ]
 }
 ]
 };
 pieChart.setOption(option);
};
const initBar = () => {
 if (!barRef.value)
 return;
 barChart = echarts.init(barRef.value);
 const sortedData = [...landData.value].sort((a, b) => b.area - a.area).slice(0, 10);
 const option = {
 backgroundColor: 'transparent',
 tooltip: {
 trigger: 'axis',
 axisPointer: {
 type: 'shadow'
 }
 },
 grid: {
 left: '3%',
 right: '4%',
 bottom: '3%',
 containLabel: true
 },
 xAxis: {
 type: 'category',
 data: sortedData.map(item => item.name),
 axisLabel: {
 color: '#fff',
 rotate: 30
 }
 },
 yAxis: {
 type: 'value',
 name: '面积(万亩)',
 nameTextStyle: {
 color: '#fff'
 },
 axisLabel: {
 color: '#fff',
 formatter: (value) => (value / 10000).toFixed(0)
 }
 },
 series: [
 {
 name: '土地面积',
 type: 'bar',
 data: sortedData.map(item => item.area),
 itemStyle: {
 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
 { offset: 0, color: '#1890ff' },
 { offset: 1, color: '#096dd9' }
 ]),
 borderRadius: [4, 4, 0, 0]
 }
 }
 ]
 };
 barChart.setOption(option);
};
const updateMapData = () => {
 initMap();
 initPie();
 initBar();
};
const updateMapType = () => {
 initMap();
};
const refreshData = () => {
 initMap();
 initPie();
 initBar();
};
const togglePanel = () => {
 panelExpanded.value = !panelExpanded.value;
};
const handleResize = () => {
 mapChart?.resize();
 pieChart?.resize();
 barChart?.resize();
};
onMounted(() => {
 initMap();
 initPie();
 initBar();
 window.addEventListener('resize', handleResize);
});
</script>

<style scoped>
.land-map-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  color: #fff;
  font-size: 20px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.map-container {
  position: relative;
  background: var(--ink-800);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.map-chart {
  height: 500px;
  width: 100%;
}

.map-legend {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.7);
  padding: 12px 16px;
  border-radius: 8px;
}

.legend-title {
  color: #fff;
  font-size: 12px;
  margin-bottom: 8px;
  font-weight: bold;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 20px;
  height: 12px;
  border-radius: 2px;
}

.legend-label {
  color: #ccc;
  font-size: 11px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--ink-800);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.detail-panel {
  background: var(--ink-800);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-header h3 {
  color: #fff;
  font-size: 16px;
  margin: 0;
}

.panel-content {
  max-height: 400px;
  overflow-y: auto;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  background: var(--ink-800);
  border-radius: 12px;
  padding: 20px;
}

.chart-card h3 {
  color: #fff;
  font-size: 16px;
  margin: 0 0 16px 0;
}

.chart-content {
  height: 300px;
}

.el-table {
  --el-table-bg-color: transparent;
  --el-table-text-color: #fff;
}

.el-table th {
  color: #ccc !important;
  background: rgba(255, 255, 255, 0.05) !important;
}

.el-table td {
  color: #fff !important;
}
</style>
