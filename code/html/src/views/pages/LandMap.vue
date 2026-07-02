<template>
  <div class="land-map-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title display">土地资源地图</h2>
        <div class="page-sub mono">LAND RESOURCE MAP · 全国土地资源分布总览</div>
      </div>
      <div class="header-actions">
        <el-select v-model="selectedYear" placeholder="选择年份" @change="updateMapData" class="header-select">
          <el-option label="2024年" value="2024" />
          <el-option label="2025年" value="2025" />
          <el-option label="2026年" value="2026" />
        </el-select>
        <el-select v-model="mapType" placeholder="地图类型" @change="updateMapType" class="header-select">
          <el-option label="土地面积" value="area" />
          <el-option label="耕地分布" value="cultivated" />
          <el-option label="土壤类型" value="soil" />
          <el-option label="灾害风险" value="disaster" />
        </el-select>
        <el-button type="primary" @click="refreshData" class="btn-refresh">
          <span>↻</span> 刷新数据
        </el-button>
      </div>
    </div>

    <div class="map-wrapper">
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
        <div class="map-tip mono">鼠标拖动可平移 · 滚轮可缩放 · 点击省份查看详情</div>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-card-1">
        <div class="stat-card-bg"></div>
        <div class="stat-icon">🌍</div>
        <div class="stat-content">
          <div class="stat-value display">{{ formatNum(totalArea) }}</div>
          <div class="stat-label">总面积(亩)</div>
        </div>
        <div class="stat-trend up">+2.3%</div>
      </div>
      <div class="stat-card stat-card-2">
        <div class="stat-card-bg"></div>
        <div class="stat-icon">🌾</div>
        <div class="stat-content">
          <div class="stat-value display">{{ formatNum(cultivatedArea) }}</div>
          <div class="stat-label">耕地面积(亩)</div>
        </div>
        <div class="stat-trend up">+1.8%</div>
      </div>
      <div class="stat-card stat-card-3">
        <div class="stat-card-bg"></div>
        <div class="stat-icon">🌿</div>
        <div class="stat-content">
          <div class="stat-value display">{{ formatNum(forestArea) }}</div>
          <div class="stat-label">林地面积(亩)</div>
        </div>
        <div class="stat-trend up">+3.1%</div>
      </div>
      <div class="stat-card stat-card-4">
        <div class="stat-card-bg"></div>
        <div class="stat-icon">🏗️</div>
        <div class="stat-content">
          <div class="stat-value display">{{ formatNum(constructionArea) }}</div>
          <div class="stat-label">建设用地(亩)</div>
        </div>
        <div class="stat-trend down">-0.5%</div>
      </div>
    </div>

    <div class="content-row">
      <div class="chart-card">
        <div class="card-header">
          <h3 class="card-title">土地类型占比</h3>
          <span class="card-badge mono">PIE</span>
        </div>
        <div ref="pieRef" class="chart-content"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3 class="card-title">各省份土地面积排名 TOP10</h3>
          <span class="card-badge mono">BAR</span>
        </div>
        <div ref="barRef" class="chart-content"></div>
      </div>
    </div>

    <div class="detail-panel">
      <div class="panel-header">
        <h3 class="panel-title">土地划分详情</h3>
        <div class="panel-actions">
          <el-button type="text" @click="togglePanel">
            {{ panelExpanded ? '收起 ↑' : '展开 ↓' }}
          </el-button>
        </div>
      </div>
      <div class="panel-content" v-show="panelExpanded">
        <el-table :data="landDetailData" stripe class="data-table">
          <el-table-column prop="province" label="省份" width="120" />
          <el-table-column prop="total" label="总面积(亩)" width="150" />
          <el-table-column prop="cultivated" label="耕地(亩)" width="150" />
          <el-table-column prop="forest" label="林地(亩)" width="150" />
          <el-table-column prop="construction" label="建设用地(亩)" width="150" />
          <el-table-column prop="water" label="水域(亩)" width="150" />
          <el-table-column prop="ratio" label="耕地占比" width="120">
            <template #default="{ row }">
              <span class="ratio-tag" :class="getRatioClass(row.ratio)">{{ row.ratio }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { getLandPlotList } from '@/api/landPlot.js';

const mapRef = ref(null);
const pieRef = ref(null);
const barRef = ref(null);
const selectedYear = ref('2026');
const mapType = ref('area');
const panelExpanded = ref(true);
let mapChart = null;
let pieChart = null;
let barChart = null;
const isMapLoaded = ref(false);
const loading = ref(false);

const formatNum = (num) => {
  return num.toLocaleString();
};

onMounted(async () => {
  const { loadChinaMap } = await import('@/assets/chinaMap.js');
  const geoJSON = await loadChinaMap();
  echarts.registerMap('china', geoJSON);
  isMapLoaded.value = true;
  
  // 加载土地数据
  await loadLandData();
  
  initMap();
  initPie();
  initBar();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  mapChart?.dispose();
  pieChart?.dispose();
  barChart?.dispose();
});

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
      { label: '低风险', color: '#A8D5A2' },
      { label: '中风险', color: '#E8C9A0' },
      { label: '高风险', color: '#D4A574' },
      { label: '极高风险', color: '#B8864A' }
    ];
  }
  return [
    { label: '0-500万亩', color: '#F5EFE6' },
    { label: '500-1000万亩', color: '#E8DCC8' },
    { label: '1000-2000万亩', color: '#D4B87A' },
    { label: '2000-5000万亩', color: '#B89855' },
    { label: '5000万亩以上', color: '#8B7355' }
  ];
});

const landData = ref([]);

// 加载土地数据
const loadLandData = async () => {
  loading.value = true;
  try {
    const res = await getLandPlotList({ year: selectedYear.value });
    if (res.data?.list) {
      // 根据后端返回的数据结构进行适配
      landData.value = (res.data.list || []).map(item => ({
        name: item.name || item.provinceName || item.region,
        area: item.area || item.totalArea || 0,
        cultivated: item.cultivated || item.cultivatedArea || 0,
        forest: item.forest || item.forestArea || 0,
        construction: item.construction || item.constructionArea || 0,
        water: item.water || item.waterArea || 0,
        disaster: item.disaster || item.disasterRisk || '低'
      }));
      
      // 初始化图表
      if (isMapLoaded.value) {
        initMap();
        initPie();
        initBar();
      }
    }
  } catch (error) {
    console.error('加载土地数据失败:', error);
    ElMessage.error('加载土地数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
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
const getRatioClass = (ratio) => {
  const r = parseFloat(ratio);
  if (r > 50) return 'ratio-high';
  if (r > 30) return 'ratio-mid';
  return 'ratio-low';
};

const initMap = () => {
  if (!mapRef.value || !isMapLoaded.value) return;
  if (mapChart) mapChart.dispose();
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
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E8E2D9',
      borderWidth: 1,
      textStyle: { color: '#5D4E37' },
      padding: [12, 16],
      formatter: (params) => {
        const item = landData.value.find(d => d.name === params.name);
        if (!item) return '';
        return `
          <div style="font-weight:bold;margin-bottom:10px;font-size:15px;color:#5D4E37;">${params.name}</div>
          <div style="color:#8B7355;line-height:2;">总面积: <span style="color:#5D4E37;font-weight:500;">${(item.area / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#8B7355;line-height:2;">耕地: <span style="color:#A8D5A2;font-weight:500;">${(item.cultivated / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#8B7355;line-height:2;">林地: <span style="color:#D4B87A;font-weight:500;">${(item.forest / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#8B7355;line-height:2;">建设用地: <span style="color:#E8C9A0;font-weight:500;">${(item.construction / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#8B7355;line-height:2;">灾害风险: <span style="color:${item.disaster === '高' ? '#B8864A' : '#A8D5A2'};font-weight:500;">${item.disaster}</span></div>
        `;
      }
    },
    visualMap: {
      min: 0,
      max: mapType.value === 'disaster' ? 4 : 500000000,
      left: 24,
      bottom: 24,
      text: ['高', '低'],
      textStyle: {
        color: '#8B7355',
        fontSize: 11,
        fontFamily: 'var(--font-mono)'
      },
      inRange: {
        color: mapType.value === 'disaster'
          ? ['#A8D5A2', '#E8C9A0', '#D4A574', '#B8864A']
          : ['#F5EFE6', '#E8DCC8', '#D4B87A', '#B89855', '#8B7355']
      },
      itemWidth: 14,
      itemHeight: 120
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
          color: '#5D4E37',
          fontSize: 11,
          fontWeight: 500
        },
        itemStyle: {
          borderColor: 'rgba(201, 168, 108, 0.4)',
          borderWidth: 1,
          areaColor: '#F0EBE3'
        },
        emphasis: {
          label: {
            color: '#5D4E37',
            fontSize: 13,
            fontWeight: 'bold'
          },
          itemStyle: {
            areaColor: '#C9A86C',
            borderColor: '#C9A86C',
            borderWidth: 2,
            shadowColor: 'rgba(201, 168, 108, 0.4)',
            shadowBlur: 20
          }
        },
        select: {
          label: { color: '#5D4E37' },
          itemStyle: { areaColor: '#B89855' }
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
  if (!pieRef.value) return;
  if (pieChart) pieChart.dispose();
  pieChart = echarts.init(pieRef.value);
  const total = totalArea.value;
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}亩 ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E8E2D9',
      borderWidth: 1,
      textStyle: { color: '#5D4E37' }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#8B7355',
        fontSize: 12
      },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 16
    },
    series: [
      {
        name: '土地类型',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#FAFAF8',
          borderWidth: 3
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#5D4E37'
          },
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(139, 115, 85, 0.25)'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: cultivatedArea.value, name: '耕地', itemStyle: { color: '#A8D5A2' } },
          { value: forestArea.value, name: '林地', itemStyle: { color: '#D4B87A' } },
          { value: constructionArea.value, name: '建设用地', itemStyle: { color: '#E8C9A0' } },
          { value: landData.value.reduce((sum, item) => sum + item.water, 0), name: '水域', itemStyle: { color: '#8FB8CA' } },
          { value: total - cultivatedArea.value - forestArea.value - constructionArea.value - landData.value.reduce((sum, item) => sum + item.water, 0), name: '其他', itemStyle: { color: '#B89855' } }
        ]
      }
    ]
  };
  pieChart.setOption(option);
};

const initBar = () => {
  if (!barRef.value) return;
  if (barChart) barChart.dispose();
  barChart = echarts.init(barRef.value);
  const sortedData = [...landData.value].sort((a, b) => b.area - a.area).slice(0, 10);
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E8E2D9',
      borderWidth: 1,
      textStyle: { color: '#5D4E37' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedData.map(item => item.name),
      axisLabel: {
        color: '#8B7355',
        rotate: 35,
        fontSize: 11
      },
      axisLine: { lineStyle: { color: '#E8E2D9' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '面积(万亩)',
      nameTextStyle: {
        color: '#8B7355',
        fontSize: 11,
        fontFamily: 'var(--font-mono)'
      },
      axisLabel: {
        color: '#8B7355',
        fontSize: 11,
        formatter: (value) => (value / 10000).toFixed(0)
      },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: {
          color: 'rgba(232, 226, 217, 0.6)',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '土地面积',
        type: 'bar',
        barWidth: '55%',
        data: sortedData.map(item => item.area),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#D4B87A' },
            { offset: 1, color: '#B89855' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#E8C9A0' },
              { offset: 1, color: '#C9A86C' }
            ])
          }
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
window.addEventListener('resize', handleResize);
</script>

<style scoped>
.land-map-page {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--s-5);
  padding-bottom: var(--s-4);
  border-bottom: 1px solid #E8E2D9;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}

.page-title {
  font-size: 28px;
  font-weight: 300;
  color: #5D4E37;
  margin: 0;
}

.page-sub {
  font-size: 11px;
  color: #8B7355;
  letter-spacing: 0.15em;
}

.header-actions {
  display: flex;
  gap: var(--s-3);
  align-items: center;
}

.header-select {
  width: 140px;
}

.btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 12px;
}

.map-wrapper {
  margin-bottom: var(--s-5);
}

.map-container {
  position: relative;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: var(--s-4);
  min-height: 520px;
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.12);
}

.map-chart {
  height: 500px;
  width: 100%;
}

.map-legend {
  position: absolute;
  top: var(--s-4);
  right: var(--s-4);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(232, 226, 217, 0.8);
  padding: var(--s-3) var(--s-4);
  border-radius: 12px;
  z-index: 10;
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.12);
}

.legend-title {
  color: #5D4E37;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: var(--s-2);
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-color {
  width: 24px;
  height: 14px;
  border-radius: 3px;
  border: 1px solid rgba(139, 115, 85, 0.2);
}

.legend-label {
  color: #8B7355;
  font-size: 12px;
}

.map-tip {
  position: absolute;
  bottom: var(--s-4);
  right: var(--s-4);
  font-size: 11px;
  color: #8B7355;
  letter-spacing: 0.05em;
  opacity: 0.7;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-4);
  margin-bottom: var(--s-5);
}

.stat-card {
  position: relative;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: var(--s-5);
  display: flex;
  align-items: center;
  gap: var(--s-4);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.stat-card:hover {
  border-color: #C9A86C;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.16);
}

.stat-card-bg {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.12;
  filter: blur(20px);
}

.stat-card-1 .stat-card-bg { background: #C9A86C; }
.stat-card-2 .stat-card-bg { background: #A8D5A2; }
.stat-card-3 .stat-card-bg { background: #D4B87A; }
.stat-card-4 .stat-card-bg { background: #E8C9A0; }

.stat-icon {
  font-size: 36px;
  position: relative;
  z-index: 2;
}

.stat-content {
  flex: 1;
  position: relative;
  z-index: 2;
}

.stat-value {
  color: #5D4E37;
  font-size: 26px;
  font-weight: 300;
  line-height: 1.2;
}

.stat-label {
  color: #8B7355;
  font-size: 13px;
  margin-top: 4px;
}

.stat-trend {
  position: relative;
  z-index: 2;
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.stat-trend.up {
  color: #A8D5A2;
  background: rgba(168, 213, 162, 0.12);
  border: 1px solid rgba(168, 213, 162, 0.2);
}

.stat-trend.down {
  color: #D4A574;
  background: rgba(212, 165, 116, 0.12);
  border: 1px solid rgba(212, 165, 116, 0.2);
}

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--s-4);
  margin-bottom: var(--s-5);
}

.chart-card {
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: var(--s-5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.chart-card:hover {
  border-color: #C9A86C;
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s-4);
}

.card-title {
  color: #5D4E37;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.card-badge {
  font-size: 10px;
  color: #C9A86C;
  padding: 3px 10px;
  border: 1px solid #C9A86C;
  letter-spacing: 0.15em;
  border-radius: 8px;
}

.chart-content {
  height: 320px;
  width: 100%;
}

.detail-panel {
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: var(--s-5);
  margin-bottom: var(--s-5);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s-4);
}

.panel-title {
  color: #5D4E37;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.panel-content {
  max-height: 420px;
  overflow-y: auto;
}

.data-table {
  --el-table-bg-color: transparent;
  --el-table-text-color: #5D4E37;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(201, 168, 108, 0.06);
}

.data-table :deep(.el-table__header th) {
  background: linear-gradient(180deg, #F7F3ED 0%, #F0EBE3 100%) !important;
  color: #8B7355 !important;
  font-weight: 600;
  font-size: 12px;
  border-bottom: 1px solid #E8E2D9 !important;
}

.data-table :deep(.el-table__body td) {
  color: #5D4E37 !important;
  border-bottom: 1px solid #E8E2D9 !important;
}

.data-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(247, 243, 237, 0.6);
}

.ratio-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-mono);
}

.ratio-high {
  color: #A8D5A2;
  background: rgba(168, 213, 162, 0.12);
}

.ratio-mid {
  color: #D4B87A;
  background: rgba(212, 184, 122, 0.12);
}

.ratio-low {
  color: #B8864A;
  background: rgba(184, 134, 74, 0.12);
}
</style>
