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
const isMapLoaded = ref(false);

const formatNum = (num) => {
  return num.toLocaleString();
};

onMounted(async () => {
  const { loadChinaMap } = await import('@/assets/chinaMap.js');
  const geoJSON = await loadChinaMap();
  echarts.registerMap('china', geoJSON);
  isMapLoaded.value = true;
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
      { label: '低风险', color: '#52c41a' },
      { label: '中风险', color: '#faad14' },
      { label: '高风险', color: '#ff4d4f' },
      { label: '极高风险', color: '#722ed1' }
    ];
  }
  return [
    { label: '0-500万亩', color: '#E3F2FD' },
    { label: '500-1000万亩', color: '#90CAF9' },
    { label: '1000-2000万亩', color: '#42A5F5' },
    { label: '2000-5000万亩', color: '#1976D2' },
    { label: '5000万亩以上', color: '#0D47A1' }
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
      backgroundColor: 'rgba(31, 38, 51, 0.95)',
      borderColor: 'var(--ink-600)',
      borderWidth: 1,
      textStyle: { color: '#F5F0E6' },
      padding: [12, 16],
      formatter: (params) => {
        const item = landData.value.find(d => d.name === params.name);
        if (!item) return '';
        return `
          <div style="font-weight:bold;margin-bottom:10px;font-size:15px;">${params.name}</div>
          <div style="color:#B5AF9F;line-height:2;">总面积: <span style="color:#F5F0E6;font-weight:500;">${(item.area / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#B5AF9F;line-height:2;">耕地: <span style="color:#52c41a;font-weight:500;">${(item.cultivated / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#B5AF9F;line-height:2;">林地: <span style="color:#13c2c2;font-weight:500;">${(item.forest / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#B5AF9F;line-height:2;">建设用地: <span style="color:#faad14;font-weight:500;">${(item.construction / 10000).toFixed(1)}万亩</span></div>
          <div style="color:#B5AF9F;line-height:2;">灾害风险: <span style="color:${item.disaster === '高' ? '#ff4d4f' : '#52c41a'};font-weight:500;">${item.disaster}</span></div>
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
        color: '#B5AF9F',
        fontSize: 11,
        fontFamily: 'var(--font-mono)'
      },
      inRange: {
        color: mapType.value === 'disaster'
          ? ['#52c41a', '#faad14', '#ff4d4f', '#722ed1']
          : ['#E3F2FD', '#90CAF9', '#42A5F5', '#1976D2', '#0D47A1']
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
          color: '#F5F0E6',
          fontSize: 11,
          fontWeight: 500,
          textBorderColor: 'rgba(0,0,0,0.5)',
          textBorderWidth: 2
        },
        itemStyle: {
          borderColor: 'rgba(255,255,255,0.3)',
          borderWidth: 1,
          areaColor: '#2B3447'
        },
        emphasis: {
          label: {
            color: '#fff',
            fontSize: 13,
            fontWeight: 'bold'
          },
          itemStyle: {
            areaColor: '#D4A853',
            borderColor: '#fff',
            borderWidth: 2,
            shadowColor: 'rgba(212, 168, 83, 0.5)',
            shadowBlur: 20
          }
        },
        select: {
          label: { color: '#fff' },
          itemStyle: { areaColor: '#B8923F' }
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
      backgroundColor: 'rgba(31, 38, 51, 0.95)',
      borderColor: 'var(--ink-600)',
      borderWidth: 1,
      textStyle: { color: '#F5F0E6' }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#B5AF9F',
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
          borderColor: '#1F2633',
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
            color: '#F5F0E6'
          },
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: false
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
  if (!barRef.value) return;
  if (barChart) barChart.dispose();
  barChart = echarts.init(barRef.value);
  const sortedData = [...landData.value].sort((a, b) => b.area - a.area).slice(0, 10);
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(31, 38, 51, 0.95)',
      borderColor: 'var(--ink-600)',
      borderWidth: 1,
      textStyle: { color: '#F5F0E6' }
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
        color: '#B5AF9F',
        rotate: 35,
        fontSize: 11
      },
      axisLine: { lineStyle: { color: '#3D4863' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '面积(万亩)',
      nameTextStyle: {
        color: '#B5AF9F',
        fontSize: 11,
        fontFamily: 'var(--font-mono)'
      },
      axisLabel: {
        color: '#B5AF9F',
        fontSize: 11,
        formatter: (value) => (value / 10000).toFixed(0)
      },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: {
          color: 'rgba(61, 72, 99, 0.3)',
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
            { offset: 0, color: '#42A5F5' },
            { offset: 1, color: '#0D47A1' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#D4A853' },
              { offset: 1, color: '#B8923F' }
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
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--s-5);
  padding-bottom: var(--s-4);
  border-bottom: var(--line);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}

.page-title {
  font-size: 28px;
  font-weight: 300;
  color: var(--signal);
  margin: 0;
}

.page-sub {
  font-size: 11px;
  color: var(--signal-dim);
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
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  padding: var(--s-4);
  min-height: 520px;
}

.map-chart {
  height: 500px;
  width: 100%;
}

.map-legend {
  position: absolute;
  top: var(--s-4);
  right: var(--s-4);
  background: rgba(31, 38, 51, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: var(--line-soft);
  padding: var(--s-3) var(--s-4);
  border-radius: var(--radius-md);
  z-index: 10;
}

.legend-title {
  color: var(--signal);
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
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.legend-label {
  color: var(--signal-dim);
  font-size: 12px;
}

.map-tip {
  position: absolute;
  bottom: var(--s-4);
  right: var(--s-4);
  font-size: 11px;
  color: var(--signal-dim);
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
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  padding: var(--s-5);
  display: flex;
  align-items: center;
  gap: var(--s-4);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.stat-card:hover {
  border-color: var(--sand-500);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.stat-card-bg {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.08;
  filter: blur(20px);
}

.stat-card-1 .stat-card-bg { background: #D4A853; }
.stat-card-2 .stat-card-bg { background: #52c41a; }
.stat-card-3 .stat-card-bg { background: #13c2c2; }
.stat-card-4 .stat-card-bg { background: #faad14; }

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
  color: var(--signal);
  font-size: 26px;
  font-weight: 300;
  line-height: 1.2;
}

.stat-label {
  color: var(--signal-dim);
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
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
  border: 1px solid rgba(82, 196, 26, 0.2);
}

.stat-trend.down {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
  border: 1px solid rgba(255, 77, 79, 0.2);
}

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--s-4);
  margin-bottom: var(--s-5);
}

.chart-card {
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  padding: var(--s-5);
  transition: all var(--transition-normal);
}

.chart-card:hover {
  border-color: var(--ink-600);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s-4);
}

.card-title {
  color: var(--signal);
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.card-badge {
  font-size: 10px;
  color: var(--sand-500);
  padding: 3px 10px;
  border: 1px solid var(--sand-600);
  letter-spacing: 0.15em;
}

.chart-content {
  height: 320px;
  width: 100%;
}

.detail-panel {
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  padding: var(--s-5);
  margin-bottom: var(--s-5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s-4);
}

.panel-title {
  color: var(--signal);
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
  --el-table-text-color: var(--signal);
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(212, 168, 83, 0.05);
}

.data-table :deep(.el-table__header th) {
  background: rgba(61, 72, 99, 0.3) !important;
  color: var(--signal-dim) !important;
  font-weight: 600;
  font-size: 12px;
}

.data-table :deep(.el-table__body td) {
  color: var(--signal) !important;
}

.data-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(43, 52, 71, 0.5);
}

.ratio-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-mono);
}

.ratio-high {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.ratio-mid {
  color: #faad14;
  background: rgba(250, 173, 20, 0.1);
}

.ratio-low {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}
</style>
