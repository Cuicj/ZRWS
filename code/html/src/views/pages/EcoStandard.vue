<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">生态标准库</h1>
      <div class="page-meta mono">ECO STANDARD · 气候变暖 / 沙漠化 / 水土流失 / 生态安全</div>
    </div>

    <div class="stat-row">
      <StatCard label="标准总数" :value="stats.total || '-'" icon="📋" variant="info" />
      <StatCard label="气候变暖标准" :value="stats.climateWarming || '-'" icon="🌡️" variant="warn" />
      <StatCard label="沙漠化标准" :value="stats.desertification || '-'" icon="🏜️" variant="warn" />
      <StatCard label="水土流失标准" :value="stats.soilErosion || '-'" icon="🌊" variant="info" />
    </div>

    <Panel title="标准列表">
      <div class="table-toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索标准名称/代码..." style="width: 240px;" clearable />
        <el-select v-model="categoryFilter" placeholder="标准分类" style="width: 160px;" clearable>
          <el-option label="气候变暖" value="CLIMATE_WARMING" />
          <el-option label="沙漠化" value="DESERTIFICATION" />
          <el-option label="水土流失" value="SOIL_EROSION" />
          <el-option label="生态安全" value="ECO_SAFETY" />
          <el-option label="气候分区" value="CLIMATE_ZONE" />
        </el-select>
        <el-select v-model="subcategoryFilter" placeholder="子分类" style="width: 160px;" clearable>
          <el-option v-for="s in subcategories" :key="s" :label="s" :value="s" />
        </el-select>
      </div>
      <table>
        <thead>
          <tr>
            <th>标准代码</th>
            <th>标准名称</th>
            <th>分类</th>
            <th>子分类</th>
            <th>阈值范围</th>
            <th>单位</th>
            <th>等级</th>
            <th>标准体系</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in filteredStandards" :key="s.standardId">
            <td class="mono">{{ s.standardCode }}</td>
            <td>{{ s.standardName }}</td>
            <td>{{ getCategoryText(s.category) }}</td>
            <td>{{ s.subcategory }}</td>
            <td>{{ formatThreshold(s) }}</td>
            <td>{{ s.unit || '-' }}</td>
            <td><span class="status-badge" :class="getGradeClass(s.gradeLevel)">{{ getGradeText(s.gradeLevel) }}</span></td>
            <td class="mono">{{ s.standardSystem || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getEcoStandardList, getEcoStandardSubcategories } from '@/api/ecoStandard';

const stats = ref({ total: 0, climateWarming: 0, desertification: 0, soilErosion: 0 });
const standards = ref([]);
const subcategories = ref([]);
const searchKeyword = ref('');
const categoryFilter = ref('');
const subcategoryFilter = ref('');
const loading = ref(true);

const getCategoryText = (cat) => {
  const map = {
    'CLIMATE_WARMING': '气候变暖',
    'DESERTIFICATION': '沙漠化',
    'SOIL_EROSION': '水土流失',
    'ECO_SAFETY': '生态安全',
    'CLIMATE_ZONE': '气候分区'
  };
  return map[cat] || cat || '-';
};

const getGradeText = (grade) => {
  const map = {
    'LOW': '低',
    'MILD': '轻度',
    'MEDIUM': '中等',
    'MODERATE': '中度',
    'MODERATE_HIGH': '中高',
    'HIGH': '高',
    'SEVERE': '重度/强烈',
    'VERY_SEVERE': '极重度/极强烈',
    'EXTREME': '极端/剧烈',
    'VERY_LOW': '极低',
    'HUMID': '湿润',
    'SEMI_HUMID': '半湿润',
    'SEMI_ARID': '半干旱',
    'ARID': '干旱',
    'HYPER_ARID': '极干旱'
  };
  return map[grade] || grade || '-';
};

const getGradeClass = (grade) => {
  if (!grade) return 'status-ok';
  const lowGrades = ['LOW', 'MILD', 'VERY_LOW', 'HUMID'];
  const midGrades = ['MEDIUM', 'MODERATE', 'MODERATE_HIGH', 'SEMI_HUMID', 'SEMI_ARID'];
  const highGrades = ['HIGH', 'SEVERE', 'VERY_SEVERE', 'EXTREME', 'ARID', 'HYPER_ARID'];
  if (lowGrades.includes(grade)) return 'status-ok';
  if (midGrades.includes(grade)) return 'status-warn';
  if (highGrades.includes(grade)) return 'status-err';
  return 'status-ok';
};

const formatThreshold = (s) => {
  if (s.thresholdMin == null && s.thresholdMax == null) return '-';
  if (s.thresholdMin == null) return `< ${s.thresholdMax}`;
  if (s.thresholdMax == null) return `> ${s.thresholdMin}`;
  return `${s.thresholdMin} ~ ${s.thresholdMax}`;
};

const filteredStandards = computed(() => {
  let result = standards.value;
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    result = result.filter(s =>
      (s.standardName && s.standardName.toLowerCase().includes(kw)) ||
      (s.standardCode && s.standardCode.toLowerCase().includes(kw))
    );
  }
  if (categoryFilter.value) {
    result = result.filter(s => s.category === categoryFilter.value);
  }
  if (subcategoryFilter.value) {
    result = result.filter(s => s.subcategory === subcategoryFilter.value);
  }
  return result;
});

const loadSubcategories = async () => {
  if (!categoryFilter.value) {
    subcategories.value = [];
    return;
  }
  try {
    const res = await getEcoStandardSubcategories(categoryFilter.value);
    if (res.list) {
      subcategories.value = res.list;
    } else if (res.data) {
      subcategories.value = res.data;
    }
  } catch (e) {
    console.warn('加载子分类失败:', e.message);
  }
};

const loadData = async () => {
  try {
    loading.value = true;
    const res = await getEcoStandardList();
    if (res.list) {
      standards.value = res.list;
    } else if (res.data) {
      standards.value = res.data;
    }
    
    const cw = standards.value.filter(s => s.category === 'CLIMATE_WARMING').length;
    const ds = standards.value.filter(s => s.category === 'DESERTIFICATION').length;
    const se = standards.value.filter(s => s.category === 'SOIL_EROSION').length;
    
    stats.value = {
      total: standards.value.length,
      climateWarming: cw,
      desertification: ds,
      soilErosion: se
    };
  } catch (e) {
    console.warn('生态标准数据加载失败:', e.message);
  } finally {
    loading.value = false;
  }
};

watch(categoryFilter, () => {
  subcategoryFilter.value = '';
  loadSubcategories();
});

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
.table-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.mono { font-family: var(--font-mono); font-size: 12px; }
</style>
