<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">岩层结构分析</h1>
        <div class="page-meta mono">ROCK STRATUM ANALYSIS · AI智能识别</div>
      </div>
      <div class="page-actions">
        <el-select v-model="selectedAnalysis" placeholder="选择分析任务" size="small" style="width:240px" @change="loadAnalysis">
          <el-option
            v-for="a in analysisList"
            :key="a.analysisId"
            :label="a.projectName + ' (' + a.analysisCode + ')'"
            :value="a.analysisId"
          />
        </el-select>
        <button class="btn btn-primary btn-sm">+ 新建分析</button>
      </div>
    </div>

    <div v-if="currentAnalysis" class="main-content">
      <!-- 顶部Tabs -->
      <div class="tabs-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
        </button>
      </div>

      <!-- 综合分析视图 -->
      <div v-if="activeTab === 'overview'" class="tab-content overview-view">
        <div class="section-left">
          <Panel title="岩层剖面" :meta="getAnalysisTypeName(currentAnalysis.analysisType)">
            <div class="stratum-visual">
              <div
                v-for="(layer, index) in stratumList"
                :key="index"
                class="stratum-layer"
                :style="{ height: getLayerHeight(layer) + '%', background: getLayerColor(layer.lithology) }"
              >
                <div class="layer-info">
                  <div class="layer-name">{{ layer.lithology }}</div>
                  <div class="layer-depth mono">{{ layer.depth }}</div>
                  <div class="layer-thickness mono">厚 {{ layer.thickness }}m</div>
                </div>
              </div>
            </div>
          </Panel>

          <Panel title="岩层详情">
            <table>
              <thead>
                <tr>
                  <th>层序</th>
                  <th>岩性</th>
                  <th>深度</th>
                  <th>厚度</th>
                  <th>强度</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(layer, index) in lithologyList" :key="index">
                  <td class="mono">{{ index + 1 }}</td>
                  <td>{{ layer.name }}</td>
                  <td class="mono">{{ layer.depth }}</td>
                  <td class="mono">{{ layer.thickness || '-' }}</td>
                  <td>{{ layer.strength || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </Panel>
        </div>

        <div class="section-center">
          <Panel title="AI分析摘要">
            <div class="ai-summary">
              <div class="ai-header">
                <div class="ai-algorithm">
                  <span class="label mono">算法</span>
                  <span class="value">{{ getAlgorithmName(currentAnalysis.aiAlgorithm) }}</span>
                </div>
                <div class="ai-confidence">
                  <span class="label mono">置信度</span>
                  <span class="value accent">{{ currentAnalysis.aiConfidence }}%</span>
                </div>
              </div>
              <div class="ai-text">{{ currentAnalysis.aiSummary }}</div>
            </div>
          </Panel>

          <Panel title="地质构造">
            <div v-if="structureInfo" class="structure-info">
              <div class="structure-item">
                <span class="label">构造类型</span>
                <span class="value">{{ structureInfo.structure }}</span>
              </div>
              <div class="structure-item">
                <span class="label">岩层倾角</span>
                <span class="value">{{ structureInfo.dip }}</span>
              </div>
              <div class="structure-item">
                <span class="label">倾向</span>
                <span class="value">{{ structureInfo.dipDirection }}</span>
              </div>
              <div class="structure-item">
                <span class="label">节理/裂隙</span>
                <span class="value">{{ structureInfo.fractures }} 组</span>
              </div>
            </div>
          </Panel>

          <Panel title="AI详细分析" class="ai-detail-panel">
            <div class="ai-detail">
              <div class="detail-item" v-for="(item, key) in aiDetail" :key="key">
                <span class="detail-key">{{ getDetailLabel(key) }}</span>
                <span class="detail-value">{{ item }}</span>
              </div>
            </div>
          </Panel>
        </div>

        <div class="section-right">
          <Panel title="风险评估">
            <div class="risk-assessment">
              <div class="risk-badge" :class="getRiskClass(currentAnalysis.riskLevel)">
                {{ getRiskName(currentAnalysis.riskLevel) }}
              </div>
              <div class="risk-level-text">地质风险等级</div>
            </div>
          </Panel>

          <Panel title="基本信息">
            <div class="info-grid">
              <div class="info-item">
                <span class="label mono">分析编号</span>
                <span class="value mono">{{ currentAnalysis.analysisCode }}</span>
              </div>
              <div class="info-item">
                <span class="label mono">项目名称</span>
                <span class="value">{{ currentAnalysis.projectName }}</span>
              </div>
              <div class="info-item">
                <span class="label mono">位置</span>
                <span class="value">{{ currentAnalysis.location }}</span>
              </div>
              <div class="info-item">
                <span class="label mono">最大深度</span>
                <span class="value">{{ currentAnalysis.maxDepth }} m</span>
              </div>
              <div class="info-item">
                <span class="label mono">地层数</span>
                <span class="value">{{ currentAnalysis.stratumCount }} 层</span>
              </div>
              <div class="info-item">
                <span class="label mono">数据来源</span>
                <span class="value">{{ currentAnalysis.dataSource }}</span>
              </div>
              <div class="info-item">
                <span class="label mono">分析人员</span>
                <span class="value">{{ currentAnalysis.analyst }}</span>
              </div>
              <div class="info-item">
                <span class="label mono">分析时间</span>
                <span class="value mono">{{ formatDate(currentAnalysis.analysisTime) }}</span>
              </div>
            </div>
          </Panel>

          <Panel title="工程建议">
            <div class="suggestion-box">
              {{ currentAnalysis.suggestion }}
            </div>
          </Panel>
        </div>
      </div>

      <!-- 取样法分析视图 -->
      <div v-if="activeTab === 'sampling'" class="tab-content sampling-view">
        <!-- 左侧：钻孔柱状图 + 取样点 -->
        <div class="sampling-left">
          <Panel title="钻孔柱状图" :meta="'ZK1号孔 · ' + currentAnalysis.maxDepth + 'm'">
            <div class="borehole-column">
              <div class="column-header">
                <span class="h-depth mono">深度(m)</span>
                <span class="h-lith">岩性</span>
                <span class="h-sample">取样</span>
              </div>
              <div class="column-body">
                <div
                  v-for="(layer, idx) in stratumList"
                  :key="idx"
                  class="col-layer"
                  :style="{ height: getLayerHeight(layer) + '%', background: getLayerColor(layer.lithology) }"
                >
                  <span class="col-depth mono">{{ layer.depth.split('-')[1] }}</span>
                  <span class="col-name">{{ layer.lithology }}</span>
                  <span class="col-samples">
                    <span
                      v-for="(s, si) in getLayerSamples(layer)"
                      :key="si"
                      class="sample-dot"
                      :class="{ active: selectedSample?.sampleId === s.sampleId }"
                      @click="selectSample(s)"
                      :title="s.sampleName"
                    >●</span>
                  </span>
                </div>
              </div>
            </div>
          </Panel>

          <Panel title="样品列表" :meta="rockSamples.length + ' 件样品'">
            <div class="sample-list">
              <div
                v-for="s in rockSamples"
                :key="s.sampleId"
                class="sample-item"
                :class="{ active: selectedSample?.sampleId === s.sampleId }"
                @click="selectSample(s)"
              >
                <div class="sample-head">
                  <span class="sample-code mono">{{ s.sampleCode }}</span>
                  <span class="sample-depth mono">{{ s.depth }}m</span>
                </div>
                <div class="sample-body">
                  <div class="sample-lith">{{ s.lithologyEstimate }}</div>
                  <div class="sample-meta">
                    <span class="confidence" :style="{ color: getConfidenceColor(s.aiConfidence) }">
                      AI {{ s.aiConfidence }}%
                    </span>
                    <span class="method">{{ getMethodShort(s.analysisMethod) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </Panel>
        </div>

        <!-- 右侧：样品成分详情 -->
        <div class="sampling-right" v-if="selectedSample">
          <Panel title="样品基本信息">
            <div class="sample-info-grid">
              <div class="info-pair"><span class="k mono">样品编号</span><span class="v mono">{{ selectedSample.sampleCode }}</span></div>
              <div class="info-pair"><span class="k mono">取样深度</span><span class="v">{{ selectedSample.depth }} {{ selectedSample.depthUnit }}</span></div>
              <div class="info-pair"><span class="k mono">样品重量</span><span class="v">{{ selectedSample.weight }} kg</span></div>
              <div class="info-pair"><span class="k mono">岩性鉴定</span><span class="v">{{ selectedSample.lithologyEstimate }}</span></div>
              <div class="info-pair"><span class="k mono">颜色</span><span class="v">{{ selectedSample.color }}</span></div>
              <div class="info-pair"><span class="k mono">结构</span><span class="v">{{ selectedSample.structure }}</span></div>
              <div class="info-pair"><span class="k mono">风化程度</span><span class="v">{{ getWeatheringName(selectedSample.weatheringDegree) }}</span></div>
              <div class="info-pair"><span class="k mono">分析方法</span><span class="v">{{ getMethodName(selectedSample.analysisMethod) }}</span></div>
            </div>
          </Panel>

          <Panel title="化学成分 (%)">
            <div class="composition-chart">
              <div v-for="(val, key) in chemicalData" :key="key" class="comp-bar">
                <div class="comp-label">{{ key }}</div>
                <div class="comp-track">
                  <div class="comp-fill" :style="{ width: val + '%', background: getCompColor(key) }"></div>
                </div>
                <div class="comp-value mono">{{ val }}%</div>
              </div>
            </div>
          </Panel>

          <Panel title="矿物成分 (%)">
            <div class="composition-chart">
              <div v-for="(val, key) in mineralData" :key="key" class="comp-bar">
                <div class="comp-label">{{ key }}</div>
                <div class="comp-track">
                  <div class="comp-fill" :style="{ width: val + '%', background: getMineralColor(key) }"></div>
                </div>
                <div class="comp-value mono">{{ val }}%</div>
              </div>
            </div>
          </Panel>

          <Panel title="微量元素 (ppm)">
            <div class="trace-grid">
              <div v-for="(val, key) in traceData" :key="key" class="trace-item">
                <span class="trace-k">{{ key }}</span>
                <span class="trace-v mono">{{ val }}</span>
              </div>
            </div>
          </Panel>

          <Panel title="物理性质">
            <div class="physical-grid">
              <div v-for="(val, key) in physicalData" :key="key" class="phys-item">
                <span class="phys-k">{{ getPhysicalLabel(key) }}</span>
                <span class="phys-v">{{ val }}</span>
              </div>
            </div>
          </Panel>

          <Panel title="AI识别结果">
            <div class="ai-result-box">
              <div class="ai-result-header">
                <div class="ai-identified">{{ selectedSample.aiIdentification }}</div>
                <div class="ai-conf-badge" :style="{ color: getConfidenceColor(selectedSample.aiConfidence) }">
                  置信度 {{ selectedSample.aiConfidence }}%
                </div>
              </div>
              <div class="ai-matched">
                <div class="matched-label mono">匹配标准</div>
                <div class="matched-tags">
                  <span v-for="std in matchedStandards" :key="std" class="matched-tag">{{ getStandardName(std) }}</span>
                </div>
              </div>
            </div>
          </Panel>
        </div>

        <div v-else class="sampling-right empty-state">
          <div class="empty-icon">🔬</div>
          <div class="empty-text">选择左侧样品查看详细成分分析</div>
        </div>
      </div>

      <!-- 标准数据库视图 -->
      <div v-if="activeTab === 'standards'" class="tab-content standards-view">
        <div class="standards-sidebar">
          <div class="std-category">
            <div class="cat-title">分类导航</div>
            <div
              v-for="cat in standardCategories"
              :key="cat.key"
              class="cat-item"
              :class="{ active: selectedCategory === cat.key }"
              @click="selectedCategory = cat.key"
            >
              <span class="cat-icon">{{ cat.icon }}</span>
              <span class="cat-name">{{ cat.name }}</span>
              <span class="cat-count mono">{{ getCategoryCount(cat.key) }}</span>
            </div>
          </div>
        </div>

        <div class="standards-main">
          <div class="std-toolbar">
            <el-input v-model="standardSearch" placeholder="搜索标准名称..." size="small" style="width:240px" clearable>
              <template #prefix>🔍</template>
            </el-input>
            <div class="std-count mono">共 {{ filteredStandards.length }} 条标准</div>
          </div>

          <div class="std-grid">
            <div
              v-for="std in filteredStandards"
              :key="std.standardId"
              class="std-card"
              @click="openStandardDetail(std)"
            >
              <div class="std-card-head">
                <span class="std-code mono">{{ std.standardCode }}</span>
                <span class="std-cat">{{ std.subcategory || std.category }}</span>
              </div>
              <div class="std-card-name">{{ std.standardName }}</div>
              <div class="std-card-body">
                <div class="std-color" :style="{ background: getStdColor(std.standardName) }"></div>
                <div class="std-props">
                  <div class="prop-row">
                    <span class="p-k">密度</span>
                    <span class="p-v mono">{{ std.densityMin }}-{{ std.densityMax }} g/cm³</span>
                  </div>
                  <div class="prop-row">
                    <span class="p-k">硬度</span>
                    <span class="p-v mono">{{ std.hardnessMin }}-{{ std.hardnessMax }}</span>
                  </div>
                  <div class="prop-row">
                    <span class="p-k">孔隙度</span>
                    <span class="p-v mono">{{ std.porosityMin }}-{{ std.porosityMax }}%</span>
                  </div>
                </div>
              </div>
              <div class="std-card-foot">
                <span class="std-system">{{ getSystemShort(std.classificationSystem) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 标准详情抽屉 -->
        <div v-if="detailStandard" class="std-drawer-mask" @click="detailStandard = null">
          <div class="std-drawer" @click.stop>
            <div class="drawer-head">
              <div>
                <div class="drawer-title">{{ detailStandard.standardName }}</div>
                <div class="drawer-sub mono">{{ detailStandard.standardCode }} · {{ getSystemName(detailStandard.classificationSystem) }}</div>
              </div>
              <button class="drawer-close" @click="detailStandard = null">✕</button>
            </div>
            <div class="drawer-body">
              <div class="drawer-section">
                <div class="sec-title">基本特征</div>
                <div class="feat-grid">
                  <div class="feat-item"><span class="f-k">分类</span><span class="f-v">{{ detailStandard.subcategory || '-' }}</span></div>
                  <div class="feat-item"><span class="f-k">颜色</span><span class="f-v">{{ detailStandard.colorDescription || '-' }}</span></div>
                  <div class="feat-item"><span class="f-k">结构</span><span class="f-v">{{ detailStandard.structureDescription || '-' }}</span></div>
                  <div class="feat-item"><span class="f-k">构造</span><span class="f-v">{{ detailStandard.textureDescription || '-' }}</span></div>
                  <div class="feat-item"><span class="f-k">分布</span><span class="f-v">{{ detailStandard.distribution || '-' }}</span></div>
                  <div class="feat-item"><span class="f-k">成因</span><span class="f-v">{{ detailStandard.formationEnvironment || '-' }}</span></div>
                </div>
              </div>

              <div class="drawer-section">
                <div class="sec-title">物理性质</div>
                <div class="feat-grid">
                  <div class="feat-item"><span class="f-k">密度范围</span><span class="f-v mono">{{ detailStandard.densityMin }} - {{ detailStandard.densityMax }} g/cm³</span></div>
                  <div class="feat-item"><span class="f-k">硬度范围</span><span class="f-v mono">{{ detailStandard.hardnessMin }} - {{ detailStandard.hardnessMax }}</span></div>
                  <div class="feat-item"><span class="f-k">孔隙度</span><span class="f-v mono">{{ detailStandard.porosityMin }} - {{ detailStandard.porosityMax }}%</span></div>
                  <div class="feat-item"><span class="f-k">渗透性</span><span class="f-v mono">{{ formatPerm(detailStandard.permeabilityMin) }} - {{ formatPerm(detailStandard.permeabilityMax) }} m/s</span></div>
                </div>
              </div>

              <div v-if="detailStandard.chemicalComposition" class="drawer-section">
                <div class="sec-title">化学成分</div>
                <div class="drawer-composition">
                  <div v-for="(val, key) in parseJson(detailStandard.chemicalComposition)" :key="key" class="drawer-comp">
                    <span class="dc-k">{{ key }}</span>
                    <div class="dc-track"><div class="dc-fill" :style="{ width: val + '%' }"></div></div>
                    <span class="dc-v mono">{{ val }}%</span>
                  </div>
                </div>
              </div>

              <div v-if="detailStandard.mineralComposition" class="drawer-section">
                <div class="sec-title">矿物成分</div>
                <div class="drawer-composition">
                  <div v-for="(val, key) in parseJson(detailStandard.mineralComposition)" :key="key" class="drawer-comp">
                    <span class="dc-k">{{ key }}</span>
                    <div class="dc-track"><div class="dc-fill mineral-fill" :style="{ width: val + '%' }"></div></div>
                    <span class="dc-v mono">{{ val }}%</span>
                  </div>
                </div>
              </div>

              <div v-if="detailStandard.mechanicalProperties" class="drawer-section">
                <div class="sec-title">力学性质</div>
                <div class="feat-grid">
                  <div v-for="(val, key) in parseJson(detailStandard.mechanicalProperties)" :key="key" class="feat-item">
                    <span class="f-k">{{ getMechLabel(key) }}</span>
                    <span class="f-v">{{ val }}</span>
                  </div>
                </div>
              </div>

              <div v-if="detailStandard.typicalElements" class="drawer-section">
                <div class="sec-title">特征元素</div>
                <div class="element-tags">
                  <span v-for="el in detailStandard.typicalElements.split('、')" :key="el" class="el-tag">{{ el }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import {
  getAnalysisList,
  getAnalysis,
  getSampleList,
  getStandardList
} from '@/api/rockStratum';

const tabs = [
  { key: 'overview', label: '综合分析', icon: '📊' },
  { key: 'sampling', label: '取样法分析', icon: '🔬' },
  { key: 'standards', label: '标准数据库', icon: '📚' }
];

const activeTab = ref('overview');
const analysisList = ref([]);
const selectedAnalysis = ref(null);
const currentAnalysis = ref(null);
const rockSamples = ref([]);
const selectedSample = ref(null);
const standardList = ref([]);
const selectedCategory = ref('SOIL_CHINA');
const standardSearch = ref('');
const detailStandard = ref(null);

const standardCategories = [
  { key: 'SOIL_CHINA', name: '中国土壤分类', icon: '🇨🇳' },
  { key: 'SOIL_WRB', name: '国际WRB分类', icon: '🌍' },
  { key: 'ROCK_IGNEOUS', name: '岩浆岩', icon: '🔥' },
  { key: 'ROCK_SEDIMENTARY', name: '沉积岩', icon: '🪨' },
  { key: 'ROCK_METAMORPHIC', name: '变质岩', icon: '💎' }
];

// ===== 计算属性 =====
const stratumList = computed(() => {
  if (!currentAnalysis.value?.stratumData) return [];
  try { return JSON.parse(currentAnalysis.value.stratumData); } catch (e) { return []; }
});

const lithologyList = computed(() => {
  if (!currentAnalysis.value?.lithologyData) return [];
  try { return JSON.parse(currentAnalysis.value.lithologyData); } catch (e) { return []; }
});

const structureInfo = computed(() => {
  if (!currentAnalysis.value?.structureData) return null;
  try { return JSON.parse(currentAnalysis.value.structureData); } catch (e) { return null; }
});

const aiDetail = computed(() => {
  if (!currentAnalysis.value?.aiDetail) return {};
  try { return JSON.parse(currentAnalysis.value.aiDetail); } catch (e) { return {}; }
});

const chemicalData = computed(() => {
  if (!selectedSample.value?.chemicalData) return {};
  try { return JSON.parse(selectedSample.value.chemicalData); } catch (e) { return {}; }
});

const mineralData = computed(() => {
  if (!selectedSample.value?.mineralData) return {};
  try { return JSON.parse(selectedSample.value.mineralData); } catch (e) { return {}; }
});

const traceData = computed(() => {
  if (!selectedSample.value?.traceElements) return {};
  try { return JSON.parse(selectedSample.value.traceElements); } catch (e) { return {}; }
});

const physicalData = computed(() => {
  if (!selectedSample.value?.physicalData) return {};
  try { return JSON.parse(selectedSample.value.physicalData); } catch (e) { return {}; }
});

const matchedStandards = computed(() => {
  if (!selectedSample.value?.aiMatchedStandards) return [];
  try { return JSON.parse(selectedSample.value.aiMatchedStandards); } catch (e) { return []; }
});

const filteredStandards = computed(() => {
  let list = standardList.value.filter(s => s.category === selectedCategory.value);
  if (standardSearch.value) {
    const kw = standardSearch.value.toLowerCase();
    list = list.filter(s =>
      s.standardName.toLowerCase().includes(kw) ||
      s.standardCode.toLowerCase().includes(kw)
    );
  }
  return list;
});

// ===== 方法 =====
const getLayerHeight = (layer) => {
  const total = stratumList.value.reduce((s, l) => s + (l.thickness || 0), 0);
  return total > 0 ? (layer.thickness / total) * 100 : 20;
};

const getLayerColor = (lithology) => {
  const colors = {
    '粉质黏土': 'linear-gradient(180deg, #8B7355 0%, #6B5344 100%)',
    '砂砾石层': 'linear-gradient(180deg, #A89070 0%, #8B7355 100%)',
    '强风化泥岩': 'linear-gradient(180deg, #6B4423 0%, #4A3018 100%)',
    '中风化泥岩': 'linear-gradient(180deg, #556B2F 0%, #3D4F22 100%)',
    '微风化石灰岩': 'linear-gradient(180deg, #708090 0%, #4A5568 100%)',
    '耕植土': 'linear-gradient(180deg, #5D4E37 0%, #4A3F2E 100%)',
    '强风化砂岩': 'linear-gradient(180deg, #8B4513 0%, #6B3410 100%)',
    '中风化砂岩': 'linear-gradient(180deg, #CD853F 0%, #A0652E 100%)'
  };
  return colors[lithology] || 'linear-gradient(180deg, #666 0%, #444 100%)';
};

const getAlgorithmName = (alg) => {
  const map = { CNN: 'CNN卷积神经网络', TRANSFORMER: 'Transformer模型', RANDOM_FOREST: '随机森林', SVM: '支持向量机', ENSEMBLE: '集成学习', DEEP_LEARNING: '深度学习' };
  return map[alg] || alg;
};

const getAnalysisTypeName = (type) => {
  const map = { COMPREHENSIVE: '综合分析', BOREHOLE: '钻孔分析', GEOPHYSICAL: '物探分析', SAMPLING: '取样分析' };
  return map[type] || type;
};

const getRiskName = (l) => { const m = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', VERY_HIGH: '极高风险' }; return m[l] || l; };
const getRiskClass = (l) => { const m = { LOW: 'risk-low', MEDIUM: 'risk-medium', HIGH: 'risk-high', VERY_HIGH: 'risk-very-high' }; return m[l] || 'risk-low'; };

const getDetailLabel = (key) => {
  const labels = { boreholeCount: '钻孔数量', avgRQD: '平均RQD', rockQuality: '岩体质量', bearingCapacity: '地基承载力', seismicLevel: '抗震设防烈度', gprFrequency: '雷达频率', penetrationDepth: '探测深度', resolution: '分辨率' };
  return labels[key] || key;
};

const formatDate = (d) => { if (!d) return '-'; return new Date(d).toLocaleString('zh-CN'); };

const getConfidenceColor = (c) => {
  if (c >= 95) return '#52c41a';
  if (c >= 85) return '#faad14';
  if (c >= 70) return '#fa8c16';
  return '#ff4d4f';
};

const getMethodShort = (m) => {
  const map = { XRF: 'XRF', ICP: 'ICP', XRD: 'XRD', AAS: 'AAS', ICP_MS: 'ICP-MS', CHEMICAL: '化学法', SPECTROSCOPY: '光谱', MICROSCOPE: '镜鉴', COMPREHENSIVE: '综合' };
  return map[m] || m;
};

const getMethodName = (m) => {
  const map = { XRF: 'X射线荧光光谱(XRF)', ICP: '等离子体发射光谱(ICP)', XRD: 'X射线衍射(XRD)', AAS: '原子吸收光谱(AAS)', ICP_MS: '电感耦合等离子体质谱(ICP-MS)', CHEMICAL: '化学分析', SPECTROSCOPY: '光谱分析', MICROSCOPE: '显微镜鉴定', COMPREHENSIVE: '综合分析' };
  return map[m] || m;
};

const getWeatheringName = (w) => {
  const map = { FRESH: '新鲜', SLIGHTLY_WEATHERED: '微风化', MODERATELY_WEATHERED: '中等风化', HIGHLY_WEATHERED: '强风化', COMPLETELY_WEATHERED: '全风化', RESIDUAL_SOIL: '残积土' };
  return map[w] || w;
};

const getCompColor = (key) => {
  const colors = { SiO2: '#4A90D9', Al2O3: '#D97706', Fe2O3: '#DC2626', CaO: '#059669', MgO: '#7C3AED', K2O: '#DB2777', Na2O: '#0891B2', '烧失量': '#6B7280', 有机质: '#065F46' };
  return colors[key] || '#666';
};

const getMineralColor = (key) => {
  const colors = { 石英: '#F59E0B', 长石: '#3B82F6', 黏土矿物: '#8B5CF6', 云母: '#EC4899', 方解石: '#10B981', 白云石: '#14B8A6', 赤铁矿: '#EF4444', 高岭石: '#06B6D4', 伊利石: '#84CC16' };
  return colors[key] || '#666';
};

const getPhysicalLabel = (k) => {
  const m = { 含水率: '含水率', 密度: '密度', 孔隙比: '孔隙比', 液限: '液限', 塑限: '塑限', 塑性指数: '塑性指数', 孔隙度: '孔隙度', 渗透系数: '渗透系数', 抗压强度: '抗压强度', 弹性模量: '弹性模量' };
  return m[k] || k;
};

const getStandardName = (code) => {
  const std = standardList.value.find(s => s.standardCode === code);
  return std ? std.standardName : code;
};

const getCategoryCount = (cat) => standardList.value.filter(s => s.category === cat).length;

const getSystemShort = (s) => { const m = { CST: '中国土壤', WRB: 'WRB国际', USDA: 'USDA', IUGS: 'IUGS', GB_T_17412: 'GB国标' }; return m[s] || s; };
const getSystemName = (s) => { const m = { CST: '中国土壤分类系统', WRB: '世界土壤资源参比基础(WRB)', USDA: '美国农业部土壤分类(USDA)', IUGS: '国际地科联岩石分类(IUGS)', GB_T_17412: '中国岩石分类GB/T 17412' }; return m[s] || s; };

const getStdColor = (name) => {
  const cmap = { '花岗岩': '#F59E0B', '玄武岩': '#374151', '闪长岩': '#6B7280', '流纹岩': '#EC4899', '橄榄岩': '#059669', '石灰岩': '#9CA3AF', '砂岩': '#D97706', '页岩': '#4B5563', '白云岩': '#D1D5DB', '砾岩': '#92400E', '片麻岩': '#7C3AED', '大理岩': '#F3F4F6', '板岩': '#1F2937', '千枚岩': '#065F46', '石英岩': '#FEF3C7', '水稻土': '#8B7355', '红壤': '#DC2626', '黄棕壤': '#92400E', '黄壤': '#EAB308', '紫色土': '#A855F7', '石灰土': '#78716C', '潮土': '#64748B', '菜园土': '#166534' };
  return cmap[name] || '#666';
};

const formatPerm = (v) => { if (!v) return '-'; if (v >= 1e-3) return (v*1000).toFixed(1) + 'e-3'; if (v >= 1e-5) return (v*1e5).toFixed(1) + 'e-5'; if (v >= 1e-7) return (v*1e7).toFixed(1) + 'e-7'; return v.toExponential(1); };

const parseJson = (s) => { try { return JSON.parse(s); } catch (e) { return {}; } };

const getMechLabel = (k) => { const m = { 抗压强度: '抗压强度', 抗拉强度: '抗拉强度', 弹性模量: '弹性模量', 泊松比: '泊松比' }; return m[k] || k; };

const loadAnalysis = async (id) => {
  try {
    const res = await getAnalysis(id);
    if (res.success && res.data) {
      currentAnalysis.value = res.data;
      selectedSample.value = null;
      await loadSamples(id);
    } else {
      throw new Error('无数据');
    }
  } catch (e) {
    const found = analysisList.value.find(a => a.analysisId === id);
    if (found) {
      currentAnalysis.value = found;
      selectedSample.value = null;
      loadSamples(id);
    }
  }
};

const loadSamples = async (analysisId) => {
  try {
    const res = await getSampleList(analysisId);
    if (res.success && res.data?.list) {
      rockSamples.value = res.data.list;
      if (rockSamples.value.length > 0) {
        selectedSample.value = rockSamples.value[0];
      }
    } else {
      throw new Error('无样品数据');
    }
  } catch (e) {
    console.error('加载样品数据失败:', e.message);
  }
};

const selectSample = (s) => { selectedSample.value = s; };

const getLayerSamples = (layer) => {
  const [start, end] = layer.depth.replace('m', '').split('-').map(Number);
  return rockSamples.value.filter(s => s.depth >= start && s.depth <= end);
};

const openStandardDetail = (std) => { detailStandard.value = std; };

onMounted(async () => {
  try {
    const res = await getAnalysisList();
    if (res.success && res.data?.list && res.data.list.length > 0) {
      analysisList.value = res.data.list;
      if (analysisList.value.length > 0) {
        selectedAnalysis.value = analysisList.value[0].analysisId;
        currentAnalysis.value = analysisList.value[0];
        await loadSamples(analysisList.value[0].analysisId);
      }
    } else {
      throw new Error('无数据');
    }
  } catch (e) {
    console.error('API加载失败:', e);
  }

  try {
    const stdRes = await getStandardList();
    if (stdRes.success && stdRes.data?.list && stdRes.data.list.length > 0) {
      standardList.value = stdRes.data.list;
    } else {
      throw new Error('无标准数据');
    }
  } catch (e) {
    console.error('标准数据API加载失败:', e);
  }
});

</script>

<style scoped>
/* ===== 明亮柔和自然风格 ===== */
.page-container { 
  padding: 24px 32px; 
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-head { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-end; 
  padding-bottom: 20px; 
  margin-bottom: 24px; 
  border-bottom: 2px solid #E8E2D9;
}
.page-title { 
  font-size: 32px; 
  font-weight: 300; 
  color: #5D4E37;
  letter-spacing: -0.5px;
}
.page-meta { 
  font-size: 12px; 
  color: #A89F91; 
  margin-top: 6px;
  letter-spacing: 0.5px;
}
.page-actions { 
  display: flex; 
  gap: 12px; 
  align-items: center; 
}
.mono { font-family: var(--font-mono); }

/* Tab 导航 - 柔和圆润 */
.tabs-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #E8E2D9;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  border: none;
  color: #8B7355;
  cursor: pointer;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.tab-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #D4C4B0 0%, #C9B89A 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 24px;
}
.tab-btn:hover { 
  color: #5D4E37;
  transform: translateY(-2px);
}
.tab-btn:hover::before { opacity: 0.15; }
.tab-btn.active { 
  background: linear-gradient(135deg, #8B7355 0%, #6B5344 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
}
.tab-btn.active::before { opacity: 0; }
.tab-icon { font-size: 18px; }
.tab-label { font-weight: 500; }

/* ===== 综合分析视图 ===== */
.overview-view {
  display: grid;
  grid-template-columns: 300px 1fr 320px;
  gap: 24px;
}
.section-left, .section-center, .section-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 岩层剖面可视化 */
.stratum-visual {
  height: 420px;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  box-shadow: 0 4px 16px rgba(93, 78, 55, 0.08);
  border: 1px solid #E8E2D9;
}
.stratum-layer {
  position: relative;
  min-height: 50px;
  border-bottom: 1px solid rgba(255,255,255,0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}
.stratum-layer:hover { 
  filter: brightness(1.08);
  transform: scaleY(1.02);
  box-shadow: inset 0 0 20px rgba(255,255,255,0.15);
}
.layer-info {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}
.layer-name { 
  font-size: 14px; 
  font-weight: 600; 
  letter-spacing: 0.3px;
}
.layer-depth { 
  font-size: 11px; 
  opacity: 0.9; 
  margin-top: 4px;
}
.layer-thickness { 
  font-size: 11px; 
  opacity: 0.8; 
  margin-top: 2px; 
  font-style: italic;
}

/* 表格样式 - 柔和 */
table { 
  width: 100%; 
  border-collapse: separate; 
  border-spacing: 0;
  font-size: 13px;
}
th, td { 
  padding: 12px 14px; 
  text-align: left; 
  border-bottom: 1px solid #F0EBE3;
}
th { 
  color: #8B7355; 
  font-weight: 600; 
  font-size: 11px; 
  text-transform: uppercase;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
}
tr:hover td {
  background: rgba(212, 196, 176, 0.1);
}

/* AI分析摘要 */
.ai-summary { 
  padding: 20px; 
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.ai-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E8E2D9;
}
.ai-algorithm, .ai-confidence { 
  display: flex; 
  flex-direction: column; 
  gap: 6px;
}
.label { 
  font-size: 11px; 
  color: #A89F91;
  font-weight: 500;
  letter-spacing: 0.5px;
}
.value { 
  font-size: 14px; 
  font-weight: 600;
  color: #5D4E37;
}
.accent { 
  color: #C9A86C; 
  font-size: 16px;
}
.ai-text { 
  font-size: 14px; 
  line-height: 1.8; 
  color: #6B5D4D;
}

/* 地质构造 */
.structure-info {
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.structure-item { 
  display: flex; 
  flex-direction: column; 
  gap: 6px;
  padding: 12px;
  background: rgba(255,255,255,0.6);
  border-radius: 8px;
}
.structure-item .label { 
  font-size: 11px; 
  color: #A89F91;
}
.structure-item .value { 
  font-size: 14px;
  color: #5D4E37;
  font-weight: 500;
}

.ai-detail-panel { flex: 1; }
.ai-detail {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #F0EBE3;
  transition: background 0.2s;
}
.detail-item:hover {
  background: rgba(201, 168, 108, 0.1);
  margin: 0 -16px;
  padding: 10px 16px;
  border-radius: 6px;
}
.detail-key { 
  font-size: 12px; 
  color: #8B7355; 
}
.detail-value { 
  font-size: 13px; 
  font-weight: 600;
  color: #5D4E37;
}

/* 风险评估 - 柔和配色 */
.risk-assessment { 
  padding: 24px; 
  text-align: center;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.risk-badge {
  display: inline-block;
  padding: 14px 36px;
  border-radius: 24px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.risk-low { 
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%); 
  color: #2E7D32; 
  border: 1px solid #A5D6A7;
}
.risk-medium { 
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%); 
  color: #F57C00; 
  border: 1px solid #FFCC80;
}
.risk-high { 
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%); 
  color: #C62828; 
  border: 1px solid #EF9A9A;
}
.risk-very-high { 
  background: linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%); 
  color: #7B1FA2; 
  border: 1px solid #CE93D8;
}
.risk-level-text { 
  font-size: 12px; 
  color: #A89F91;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.info-item .label { 
  font-size: 11px; 
  color: #A89F91; 
  font-weight: 500;
}
.info-item .value { 
  font-size: 13px;
  color: #5D4E37;
}

.suggestion-box {
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #6B5D4D;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-left: 4px solid #C9A86C;
  border-radius: 0 12px 12px 0;
}

/* ===== 取样法分析视图 ===== */
.sampling-view {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 24px;
}
.sampling-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.borehole-column {
  display: flex;
  flex-direction: column;
  height: 520px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(93, 78, 55, 0.08);
  border: 1px solid #E8E2D9;
}
.column-header {
  display: grid;
  grid-template-columns: 60px 1fr 50px;
  background: linear-gradient(180deg, #F5F2ED 0%, #EBE5DB 100%);
  padding: 12px 14px;
  font-size: 11px;
  color: #8B7355;
  text-transform: uppercase;
  font-weight: 600;
  border-bottom: 1px solid #E8E2D9;
}
.column-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}
.col-layer {
  display: grid;
  grid-template-columns: 60px 1fr 50px;
  align-items: center;
  padding: 0 14px;
  font-size: 13px;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.15);
  min-height: 40px;
  text-shadow: 0 1px 3px rgba(0,0,0,0.3);
  transition: all 0.3s ease;
  cursor: pointer;
}
.col-layer:hover {
  transform: scaleY(1.03);
  box-shadow: inset 0 0 15px rgba(255,255,255,0.15);
}
.col-depth { 
  font-family: var(--font-mono); 
  font-size: 11px; 
  opacity: 0.9;
}
.col-name { font-size: 13px; }
.col-samples { 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  gap: 4px;
}
.sample-dot {
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #C9A86C;
  filter: drop-shadow(0 1px 3px rgba(0,0,0,0.3));
}
.sample-dot:hover { 
  transform: scale(1.4) translateY(-2px);
  color: #FFE082;
}
.sample-dot.active { 
  color: #fff; 
  transform: scale(1.5);
  text-shadow: 0 0 8px rgba(255,255,255,0.8);
}

.sample-list {
  max-height: 320px;
  overflow-y: auto;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.sample-list::-webkit-scrollbar { width: 6px; }
.sample-list::-webkit-scrollbar-track { background: #F5F2ED; border-radius: 3px; }
.sample-list::-webkit-scrollbar-thumb { background: #D4C4B0; border-radius: 3px; }

.sample-item {
  padding: 14px 16px;
  border-bottom: 1px solid #F0EBE3;
  cursor: pointer;
  transition: all 0.3s ease;
}
.sample-item:hover { 
  background: rgba(201, 168, 108, 0.08);
  transform: translateX(4px);
}
.sample-item.active { 
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.15) 0%, rgba(201, 168, 108, 0.08) 100%);
  border-left: 4px solid #C9A86C;
}
.sample-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.sample-code { 
  font-size: 12px; 
  color: #C9A86C;
  font-weight: 600;
}
.sample-depth { 
  font-size: 11px; 
  color: #A89F91;
}
.sample-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sample-lith { 
  font-size: 14px; 
  color: #5D4E37;
  font-weight: 500;
}
.sample-meta { 
  display: flex; 
  gap: 10px; 
  font-size: 11px;
}
.confidence { 
  font-weight: 700;
  padding: 2px 8px;
  background: rgba(201, 168, 108, 0.15);
  border-radius: 10px;
}
.method { 
  color: #A89F91; 
  padding: 2px 8px;
  background: rgba(168, 159, 145, 0.15);
  border-radius: 10px;
}

.sampling-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.sampling-right.empty-state {
  align-items: center;
  justify-content: center;
  min-height: 420px;
  color: #A89F91;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 16px;
  border: 2px dashed #E8E2D9;
}
.empty-icon { 
  font-size: 56px; 
  margin-bottom: 16px;
  opacity: 0.6;
}
.empty-text { 
  font-size: 15px;
}

.sample-info-grid {
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.info-pair { 
  display: flex; 
  flex-direction: column; 
  gap: 4px;
  padding: 10px;
  background: rgba(255,255,255,0.6);
  border-radius: 8px;
}
.info-pair .k { 
  font-size: 10px; 
  color: #A89F91;
  font-weight: 500;
}
.info-pair .v { 
  font-size: 13px;
  color: #5D4E37;
  font-weight: 500;
}

.composition-chart {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.comp-bar {
  display: grid;
  grid-template-columns: 60px 1fr 55px;
  align-items: center;
  gap: 12px;
}
.comp-label { 
  font-size: 11px; 
  color: #8B7355;
  font-weight: 500;
}
.comp-track {
  height: 10px;
  background: #F0EBE3;
  border-radius: 5px;
  overflow: hidden;
}
.comp-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}
.comp-value { 
  font-size: 12px; 
  text-align: right; 
  color: #5D4E37;
  font-weight: 600;
}

.trace-grid {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.trace-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: rgba(255,255,255,0.6);
  border-radius: 10px;
  transition: all 0.3s ease;
}
.trace-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.2);
}
.trace-k { 
  font-size: 10px; 
  color: #A89F91;
  margin-bottom: 4px;
}
.trace-v { 
  font-size: 14px; 
  font-weight: 700; 
  color: #C9A86C;
}

.physical-grid {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.phys-item {
  display: flex;
  flex-direction: column;
  padding: 12px;
  background: rgba(255,255,255,0.6);
  border-radius: 10px;
  transition: all 0.3s ease;
}
.phys-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.15);
}
.phys-k { 
  font-size: 10px; 
  color: #A89F91;
  margin-bottom: 6px;
}
.phys-v { 
  font-size: 14px; 
  font-weight: 600;
  color: #5D4E37;
}

.ai-result-box { 
  padding: 20px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.ai-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid #E8E2D9;
}
.ai-identified { 
  font-size: 18px; 
  font-weight: 700; 
  color: #5D4E37;
}
.ai-conf-badge { 
  font-size: 14px; 
  font-weight: 600;
  padding: 6px 14px;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  border-radius: 16px;
  border: 1px solid #FFCC80;
}
.ai-matched { 
  display: flex; 
  flex-direction: column; 
  gap: 10px;
}
.matched-label { 
  font-size: 11px; 
  color: #A89F91;
  font-weight: 500;
}
.matched-tags { 
  display: flex; 
  flex-wrap: wrap; 
  gap: 8px;
}
.matched-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, #FFFBF5 0%, #F5F2ED 100%);
  border: 1px solid #D4C4B0;
  color: #8B7355;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}
.matched-tag:hover {
  background: linear-gradient(135deg, #D4C4B0 0%, #C9B89A 100%);
  color: #fff;
  transform: scale(1.05);
}

/* ===== 标准数据库视图 ===== */
.standards-view {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
  min-height: 640px;
}
.standards-sidebar {
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #E8E2D9;
}
.std-category { 
  display: flex; 
  flex-direction: column; 
  gap: 6px;
}
.cat-title {
  font-size: 11px;
  color: #8B7355;
  text-transform: uppercase;
  padding: 0 12px 12px;
  border-bottom: 1px solid #E8E2D9;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.cat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  color: #6B5D4D;
  transition: all 0.3s ease;
}
.cat-item:hover { 
  background: rgba(201, 168, 108, 0.12);
  transform: translateX(4px);
}
.cat-item.active { 
  background: linear-gradient(135deg, #8B7355 0%, #6B5344 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
}
.cat-icon { font-size: 16px; }
.cat-name { flex: 1; font-weight: 500; }
.cat-count { 
  font-size: 11px; 
  opacity: 0.7;
  padding: 2px 8px;
  background: rgba(255,255,255,0.3);
  border-radius: 10px;
}
.cat-item.active .cat-count {
  background: rgba(255,255,255,0.2);
}

.standards-main { 
  display: flex; 
  flex-direction: column; 
  gap: 20px;
}
.std-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}
.std-count { 
  font-size: 12px; 
  color: #A89F91;
}

.std-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  align-content: start;
}
.std-card {
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.std-card:hover {
  border-color: #C9A86C;
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(139, 115, 85, 0.18);
}
.std-card-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.std-code { 
  font-size: 11px; 
  color: #C9A86C;
  font-weight: 600;
}
.std-cat { 
  font-size: 10px; 
  color: #A89F91;
  padding: 2px 8px;
  background: rgba(168, 159, 145, 0.15);
  border-radius: 8px;
}
.std-card-name {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 14px;
  color: #5D4E37;
  line-height: 1.3;
}
.std-card-body {
  display: flex;
  gap: 14px;
  margin-bottom: 14px;
}
.std-color {
  width: 28px;
  height: 70px;
  border-radius: 8px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.std-props { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  gap: 6px;
}
.prop-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  padding: 4px 0;
}
.p-k { 
  color: #A89F91;
}
.p-v { 
  color: #5D4E37;
  font-weight: 500;
}
.std-card-foot {
  padding-top: 12px;
  border-top: 1px solid #E8E2D9;
  font-size: 11px;
  color: #A89F91;
  text-align: right;
}

/* 详情抽屉 - 柔和过渡 */
.std-drawer-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(93, 78, 55, 0.35);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
}
.std-drawer {
  width: 580px;
  height: 100%;
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideInSoft 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: -8px 0 32px rgba(93, 78, 55, 0.15);
}
@keyframes slideInSoft {
  from { transform: translateX(60px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 28px 32px;
  border-bottom: 2px solid #E8E2D9;
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
}
.drawer-title { 
  font-size: 24px; 
  font-weight: 700; 
  color: #5D4E37;
}
.drawer-sub { 
  font-size: 12px; 
  color: #A89F91; 
  margin-top: 6px;
}
.drawer-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F5F2ED 0%, #EBE5DB 100%);
  border: 1px solid #E8E2D9;
  color: #8B7355;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s ease;
}
.drawer-close:hover { 
  background: linear-gradient(135deg, #8B7355 0%, #6B5344 100%);
  color: #fff;
  transform: rotate(90deg);
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px;
}
.drawer-section { 
  margin-bottom: 28px;
}
.sec-title {
  font-size: 13px;
  font-weight: 700;
  color: #C9A86C;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 2px solid #E8E2D9;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.feat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.feat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
  transition: all 0.3s ease;
}
.feat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.12);
}
.f-k { 
  font-size: 11px; 
  color: #A89F91;
  font-weight: 500;
}
.f-v { 
  font-size: 13px; 
  color: #5D4E37;
  font-weight: 600;
}

.drawer-composition {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.drawer-comp {
  display: grid;
  grid-template-columns: 80px 1fr 65px;
  align-items: center;
  gap: 14px;
}
.dc-k { 
  font-size: 12px; 
  color: #8B7355;
  font-weight: 500;
}
.dc-track {
  height: 12px;
  background: #F0EBE3;
  border-radius: 6px;
  overflow: hidden;
}
.dc-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A86C, #D4B87A);
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}
.mineral-fill {
  background: linear-gradient(90deg, #7EB8DA, #A8D4EA);
}
.dc-v { 
  font-size: 12px; 
  text-align: right; 
  color: #5D4E37;
  font-weight: 600;
}

.element-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.el-tag {
  padding: 8px 16px;
  background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
  border: 1px solid #D4C4B0;
  border-radius: 20px;
  font-size: 13px;
  color: #8B7355;
  font-weight: 500;
  transition: all 0.3s ease;
}
.el-tag:hover {
  background: linear-gradient(135deg, #C9A86C 0%, #D4B87A 100%);
  color: #fff;
  transform: scale(1.05);
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: #F5F2ED; border-radius: 4px; }
::-webkit-scrollbar-thumb { background: #D4C4B0; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #C9B89A; }
</style>
