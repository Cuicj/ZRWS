<template>
  <div class="geo-page">
    <section class="geo-hero">
      <div class="hero-bg">
        <div class="hero-pattern"></div>
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <div class="hero-meta mono">GEO STANDARD DATABASE · 地质标准数据库</div>
        <h1 class="hero-title">土壤与岩石<br/>标准分类体系</h1>
        <p class="hero-desc">
          基于中国土壤分类系统、国际WRB分类及IUGS岩石分类，
          构建完整的地学标准数据库，支持AI智能比对与专业分析。
        </p>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-value">{{ totalCount }}</span>
            <span class="stat-label">标准条目</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ categoryCount }}</span>
            <span class="stat-label">分类体系</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">AI</span>
            <span class="stat-label">智能比对</span>
          </div>
        </div>
      </div>
    </section>

    <section class="geo-categories">
      <div class="section-head">
        <h2 class="section-title">分类体系</h2>
        <p class="section-sub">选择分类查看详细标准</p>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.key"
          :class="['category-card', { active: activeCategory === cat.key }]"
          @click="selectCategory(cat.key)"
        >
          <div class="cat-icon">{{ cat.icon }}</div>
          <div class="cat-info">
            <h3 class="cat-name">{{ cat.name }}</h3>
            <p class="cat-count mono">{{ cat.count }} 条目</p>
          </div>
          <div class="cat-arrow">→</div>
        </div>
      </div>
    </section>

    <section class="geo-content">
      <div class="content-toolbar">
        <div class="toolbar-left">
          <div class="search-box">
            <span class="search-icon">⌕</span>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索标准名称、成分、特征..."
              @input="handleSearch"
            />
          </div>
        </div>
        <div class="toolbar-right">
          <select v-model="viewMode" class="view-select mono">
            <option value="list">列表视图</option>
            <option value="grid">卡片视图</option>
          </select>
        </div>
      </div>

      <div v-if="viewMode === 'list'" class="standard-list">
        <div class="list-header">
          <span class="col-code mono">编码</span>
          <span class="col-name">名称</span>
          <span class="col-cat">分类</span>
          <span class="col-prop">关键参数</span>
          <span class="col-action">操作</span>
        </div>
        <div
          v-for="item in filteredStandards"
          :key="item.standardId"
          class="list-row"
          @click="openDetail(item)"
        >
          <span class="col-code mono">{{ item.standardCode }}</span>
          <span class="col-name">
            <span class="name-dot" :style="{ background: getCategoryColor(item.category) }"></span>
            {{ item.standardName }}
          </span>
          <span class="col-cat">{{ item.subcategory || item.category }}</span>
          <span class="col-prop">
            <span v-if="item.hardnessMin != null" class="prop-tag">
              硬度 {{ item.hardnessMin }}-{{ item.hardnessMax }}
            </span>
            <span v-if="item.densityMin != null" class="prop-tag">
              密度 {{ item.densityMin }}-{{ item.densityMax }}
            </span>
            <span v-if="item.colorDescription" class="prop-tag">
              {{ item.colorDescription }}
            </span>
          </span>
          <span class="col-action">
            <button class="btn-ghost" @click.stop="openDetail(item)">查看</button>
          </span>
        </div>
        <div v-if="filteredStandards.length === 0" class="empty-state">
          <div class="empty-icon">◌</div>
          <p>暂无数据</p>
        </div>
      </div>

      <div v-else class="standard-grid">
        <div
          v-for="item in filteredStandards"
          :key="item.standardId"
          class="grid-card"
          @click="openDetail(item)"
        >
          <div class="card-visual" :style="{ background: getCardGradient(item.category) }">
            <div class="card-code mono">{{ item.standardCode }}</div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.standardName }}</h3>
            <p class="card-sub">{{ item.subcategory || item.category }}</p>
            <div class="card-props">
              <div v-if="item.hardnessMin != null" class="prop-item">
                <span class="prop-label">硬度</span>
                <span class="prop-value">{{ item.hardnessMin }}-{{ item.hardnessMax }}</span>
              </div>
              <div v-if="item.densityMin != null" class="prop-item">
                <span class="prop-label">密度</span>
                <span class="prop-value">{{ item.densityMin }}-{{ item.densityMax }}</span>
              </div>
              <div v-if="item.porosityMin != null" class="prop-item">
                <span class="prop-label">孔隙度</span>
                <span class="prop-value">{{ item.porosityMin }}-{{ item.porosityMax }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <transition name="fade">
      <div v-if="detailVisible" class="detail-overlay" @click="closeDetail">
        <div class="detail-panel" @click.stop>
          <button class="detail-close" @click="closeDetail">×</button>
          <div v-if="currentDetail" class="detail-content">
            <div class="detail-header">
              <div class="detail-cat mono" :style="{ color: getCategoryColor(currentDetail.category) }">
                {{ currentDetail.category }}
              </div>
              <h2 class="detail-title">{{ currentDetail.standardName }}</h2>
              <p class="detail-code mono">{{ currentDetail.standardCode }}</p>
            </div>

            <div class="detail-section">
              <h4 class="detail-section-title">基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="detail-label">分类体系</span>
                  <span class="detail-value">{{ currentDetail.classificationSystem }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">母质/成因</span>
                  <span class="detail-value">{{ currentDetail.parentMaterial || '-' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">形成环境</span>
                  <span class="detail-value">{{ currentDetail.formationEnvironment || '-' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">分布区域</span>
                  <span class="detail-value">{{ currentDetail.distribution || '-' }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4 class="detail-section-title">物理性质</h4>
              <div class="detail-grid">
                <div v-if="currentDetail.colorDescription" class="detail-item">
                  <span class="detail-label">颜色</span>
                  <span class="detail-value">{{ currentDetail.colorDescription }}</span>
                </div>
                <div v-if="currentDetail.textureDescription" class="detail-item">
                  <span class="detail-label">质地</span>
                  <span class="detail-value">{{ currentDetail.textureDescription }}</span>
                </div>
                <div v-if="currentDetail.structureDescription" class="detail-item">
                  <span class="detail-label">结构</span>
                  <span class="detail-value">{{ currentDetail.structureDescription }}</span>
                </div>
                <div v-if="currentDetail.hardnessMin != null" class="detail-item">
                  <span class="detail-label">硬度</span>
                  <span class="detail-value">{{ currentDetail.hardnessMin }} - {{ currentDetail.hardnessMax }}</span>
                </div>
                <div v-if="currentDetail.densityMin != null" class="detail-item">
                  <span class="detail-label">密度 (g/cm³)</span>
                  <span class="detail-value">{{ currentDetail.densityMin }} - {{ currentDetail.densityMax }}</span>
                </div>
                <div v-if="currentDetail.porosityMin != null" class="detail-item">
                  <span class="detail-label">孔隙度 (%)</span>
                  <span class="detail-value">{{ currentDetail.porosityMin }} - {{ currentDetail.porosityMax }}</span>
                </div>
                <div v-if="currentDetail.permeabilityMin != null" class="detail-item">
                  <span class="detail-label">渗透性</span>
                  <span class="detail-value">{{ currentDetail.permeabilityMin }} - {{ currentDetail.permeabilityMax }}</span>
                </div>
              </div>
            </div>

            <div v-if="currentDetail.chemicalComposition || currentDetail.mineralComposition" class="detail-section">
              <h4 class="detail-section-title">化学成分与矿物组成</h4>
              <div v-if="currentDetail.chemicalComposition" class="detail-item-full">
                <span class="detail-label">化学成分</span>
                <span class="detail-value">{{ currentDetail.chemicalComposition }}</span>
              </div>
              <div v-if="currentDetail.mineralComposition" class="detail-item-full">
                <span class="detail-label">矿物组成</span>
                <span class="detail-value">{{ currentDetail.mineralComposition }}</span>
              </div>
            </div>

            <div v-if="currentDetail.typicalElements" class="detail-section">
              <h4 class="detail-section-title">典型特征元素</h4>
              <div class="detail-tags">
                <span v-for="el in parseElements(currentDetail.typicalElements)" :key="el" class="element-tag">
                  {{ el }}
                </span>
              </div>
            </div>

            <div v-if="currentDetail.remark" class="detail-section">
              <h4 class="detail-section-title">备注说明</h4>
              <p class="detail-remark">{{ currentDetail.remark }}</p>
            </div>

            <div class="detail-actions">
              <button class="btn-primary" @click="startCompare(currentDetail)">
                AI比对分析
              </button>
              <button class="btn-outline">导出标准卡</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import geoStandardApi from '@/api/geoStandard'

const categories = ref([
  { key: '', name: '全部分类', icon: '◉', count: 0 },
  { key: 'SOIL_CHINA', name: '中国土壤分类', icon: '◈', count: 0 },
  { key: 'SOIL_WRB', name: '国际WRB分类', icon: '◇', count: 0 },
  { key: 'SOIL_TEXTURE', name: '土壤质地', icon: '◆', count: 0 },
  { key: 'SOIL_STRUCTURE', name: '土壤结构', icon: '▦', count: 0 },
  { key: 'ROCK_IGNEOUS', name: '岩浆岩', icon: '▲', count: 0 },
  { key: 'ROCK_SEDIMENTARY', name: '沉积岩', icon: '▬', count: 0 },
  { key: 'ROCK_METAMORPHIC', name: '变质岩', icon: '◐', count: 0 },
  { key: 'MINERAL', name: '矿物', icon: '✦', count: 0 }
])

const standards = ref([])
const activeCategory = ref('')
const searchKeyword = ref('')
const viewMode = ref('list')
const detailVisible = ref(false)
const currentDetail = ref(null)
const loading = ref(false)

const totalCount = computed(() => standards.value.length)
const categoryCount = computed(() => categories.value.filter(c => c.key !== '').length)

const filteredStandards = computed(() => {
  let list = standards.value
  if (activeCategory.value) {
    list = list.filter(s => s.category === activeCategory.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(s =>
      (s.standardName && s.standardName.toLowerCase().includes(kw)) ||
      (s.standardCode && s.standardCode.toLowerCase().includes(kw)) ||
      (s.description && s.description.toLowerCase().includes(kw)) ||
      (s.typicalElements && s.typicalElements.toLowerCase().includes(kw))
    )
  }
  return list
})

const getCategoryColor = (category) => {
  const colors = {
    SOIL_CHINA: '#8B6914',
    SOIL_WRB: '#5D7A3E',
    SOIL_TEXTURE: '#C9A86C',
    SOIL_STRUCTURE: '#A67C52',
    ROCK_IGNEOUS: '#7A3E3E',
    ROCK_SEDIMENTARY: '#6B5B4F',
    ROCK_METAMORPHIC: '#5E4A6B',
    MINERAL: '#3E6B7A'
  }
  return colors[category] || '#8B7355'
}

const getCardGradient = (category) => {
  const gradients = {
    SOIL_CHINA: 'linear-gradient(135deg, #8B6914 0%, #C9A86C 100%)',
    SOIL_WRB: 'linear-gradient(135deg, #5D7A3E 0%, #8BA86C 100%)',
    SOIL_TEXTURE: 'linear-gradient(135deg, #C9A86C 0%, #E8D4A8 100%)',
    SOIL_STRUCTURE: 'linear-gradient(135deg, #A67C52 0%, #D4B896 100%)',
    ROCK_IGNEOUS: 'linear-gradient(135deg, #7A3E3E 0%, #B06868 100%)',
    ROCK_SEDIMENTARY: 'linear-gradient(135deg, #6B5B4F 0%, #A89888 100%)',
    ROCK_METAMORPHIC: 'linear-gradient(135deg, #5E4A6B 0%, #9A8AB0 100%)',
    MINERAL: 'linear-gradient(135deg, #3E6B7A 0%, #6E9BAB 100%)'
  }
  return gradients[category] || 'linear-gradient(135deg, #8B7355 0%, #C9A86C 100%)'
}

const selectCategory = (key) => {
  activeCategory.value = key
}

const handleSearch = () => {
}

const openDetail = (item) => {
  currentDetail.value = item
  detailVisible.value = true
}

const closeDetail = () => {
  detailVisible.value = false
}

const startCompare = (item) => {
  closeDetail()
  console.log('开始比对:', item)
}

const parseElements = (str) => {
  if (!str) return []
  return str.split(/[,，、;；\s]+/).filter(Boolean)
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await geoStandardApi.list()
    if (res && res.list) {
      standards.value = res.list
      categories.value.forEach(cat => {
        if (cat.key === '') {
          cat.count = res.list.length
        } else {
          cat.count = res.list.filter(s => s.category === cat.key).length
        }
      })
    }
  } catch (e) {
    console.warn('加载地质标准数据失败，使用模拟数据:', e.message)
    standards.value = generateMockData()
    categories.value.forEach(cat => {
      if (cat.key === '') {
        cat.count = standards.value.length
      } else {
        cat.count = standards.value.filter(s => s.category === cat.key).length
      }
    })
  } finally {
    loading.value = false
  }
}

const generateMockData = () => {
  return [
    {
      standardId: 1, standardCode: 'CS-001', standardName: '红壤',
      category: 'SOIL_CHINA', subcategory: '铁铝土纲',
      classificationSystem: '中国土壤分类系统',
      parentMaterial: '第四纪红色黏土、红砂岩风化物',
      chemicalComposition: 'SiO2 55%, Al2O3 18%, Fe2O3 12%',
      mineralComposition: '高岭石、赤铁矿、针铁矿',
      physicalProperties: '黏重、块状结构',
      typicalElements: 'Fe, Al, Si',
      formationEnvironment: '热带、亚热带湿润气候',
      distribution: '长江以南广大丘陵地区',
      colorDescription: '红色、棕红色',
      textureDescription: '黏壤土至黏土',
      structureDescription: '块状、核状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.2, densityMax: 1.6,
      porosityMin: 40, porosityMax: 55,
      permeabilityMin: '低', permeabilityMax: '中',
      sortOrder: 1, status: 'ACTIVE'
    },
    {
      standardId: 2, standardCode: 'CS-002', standardName: '黄壤',
      category: 'SOIL_CHINA', subcategory: '铁铝土纲',
      classificationSystem: '中国土壤分类系统',
      parentMaterial: '酸性岩风化物、第四纪沉积物',
      chemicalComposition: 'SiO2 58%, Al2O3 16%, Fe2O3 8%',
      mineralComposition: '高岭石、多水高岭石、针铁矿',
      physicalProperties: '质地偏黏、核粒状结构',
      typicalElements: 'Fe, Al, Si, 腐殖质',
      formationEnvironment: '亚热带湿润山地、云雾多日照少',
      distribution: '云贵高原、四川盆地周边山地',
      colorDescription: '黄色、蜡黄色',
      textureDescription: '黏壤土',
      structureDescription: '核粒状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.1, densityMax: 1.5,
      porosityMin: 45, porosityMax: 60,
      permeabilityMin: '中', permeabilityMax: '较高',
      sortOrder: 2, status: 'ACTIVE'
    },
    {
      standardId: 3, standardCode: 'CS-003', standardName: '褐土',
      category: 'SOIL_CHINA', subcategory: '半淋溶土纲',
      classificationSystem: '中国土壤分类系统',
      parentMaterial: '黄土、碳酸盐类风化物',
      chemicalComposition: 'CaO丰富, SiO2 60%, Al2O3 12%',
      mineralComposition: '蒙脱石、水云母、方解石',
      physicalProperties: '质地中等、团粒结构发育',
      typicalElements: 'Ca, Mg, Si',
      formationEnvironment: '暖温带半湿润半干旱气候',
      distribution: '华北平原、黄土高原东南部',
      colorDescription: '棕褐色、灰褐色',
      textureDescription: '壤土至黏壤土',
      structureDescription: '团粒状、核状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.3, densityMax: 1.5,
      porosityMin: 45, porosityMax: 52,
      permeabilityMin: '中', permeabilityMax: '高',
      sortOrder: 3, status: 'ACTIVE'
    },
    {
      standardId: 4, standardCode: 'WRB-001', standardName: '高活性强酸土',
      category: 'SOIL_WRB', subcategory: '参考土类',
      classificationSystem: '世界土壤资源参比基础(WRB)',
      parentMaterial: '酸性岩、沉积物',
      chemicalComposition: '盐基高度不饱和, Al饱和度高',
      mineralComposition: '高岭石、三水铝石',
      physicalProperties: '质地黏重、持水性强',
      typicalElements: 'Al, Fe, Si',
      formationEnvironment: '热带、亚热带高温多雨',
      distribution: '赤道附近、热带地区',
      colorDescription: '红色、赤红色',
      textureDescription: '黏土',
      structureDescription: '块状、棱柱状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.0, densityMax: 1.4,
      porosityMin: 50, porosityMax: 65,
      permeabilityMin: '低', permeabilityMax: '中',
      sortOrder: 4, status: 'ACTIVE'
    },
    {
      standardId: 5, standardCode: 'STX-001', standardName: '砂土',
      category: 'SOIL_TEXTURE', subcategory: '质地分类',
      classificationSystem: '国际制土壤质地分类',
      parentMaterial: '风积砂、河流冲积物',
      chemicalComposition: 'SiO2 > 80%',
      mineralComposition: '石英为主，含少量长石',
      physicalProperties: '颗粒粗大、疏松多孔',
      typicalElements: 'Si, O',
      formationEnvironment: '干旱、半干旱或河流冲积区',
      distribution: '西北内陆、河流沿岸',
      colorDescription: '浅黄色、灰白色',
      textureDescription: '砂粒 > 85%',
      structureDescription: '单粒状、无结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.4, densityMax: 1.7,
      porosityMin: 30, porosityMax: 45,
      permeabilityMin: '极高', permeabilityMax: '极高',
      sortOrder: 5, status: 'ACTIVE'
    },
    {
      standardId: 6, standardCode: 'STX-002', standardName: '壤土',
      category: 'SOIL_TEXTURE', subcategory: '质地分类',
      classificationSystem: '国际制土壤质地分类',
      parentMaterial: '多种沉积物混合',
      chemicalComposition: '矿物组成复杂,养分均衡',
      mineralComposition: '石英、长石、云母、黏土矿物',
      physicalProperties: '砂黏适中、耕性良好',
      typicalElements: 'Si, Al, K, Ca',
      formationEnvironment: '冲积平原、丘陵缓坡',
      distribution: '各大河流冲积平原',
      colorDescription: '棕黄色、灰褐色',
      textureDescription: '砂粒 20-45%, 黏粒 < 27%',
      structureDescription: '团粒状、粒状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.2, densityMax: 1.4,
      porosityMin: 45, porosityMax: 55,
      permeabilityMin: '中', permeabilityMax: '高',
      sortOrder: 6, status: 'ACTIVE'
    },
    {
      standardId: 7, standardCode: 'STX-003', standardName: '黏土',
      category: 'SOIL_TEXTURE', subcategory: '质地分类',
      classificationSystem: '国际制土壤质地分类',
      parentMaterial: '细颗粒沉积物、岩石风化',
      chemicalComposition: 'Al2O3, Fe2O3 含量较高',
      mineralComposition: '高岭石、蒙脱石、伊利石',
      physicalProperties: '颗粒细小、黏结性强',
      typicalElements: 'Al, Si, Fe',
      formationEnvironment: '低洼积水区、静水沉积',
      distribution: '湖泊周边、三角洲、低平地',
      colorDescription: '深灰色、灰黑色',
      textureDescription: '黏粒 > 45%',
      structureDescription: '块状、棱柱状结构',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.1, densityMax: 1.5,
      porosityMin: 40, porosityMax: 60,
      permeabilityMin: '极低', permeabilityMax: '低',
      sortOrder: 7, status: 'ACTIVE'
    },
    {
      standardId: 8, standardCode: 'ROCK-001', standardName: '花岗岩',
      category: 'ROCK_IGNEOUS', subcategory: '酸性侵入岩',
      classificationSystem: '国际地科联岩石分类(IUGS)',
      parentMaterial: '岩浆侵入冷却结晶',
      chemicalComposition: 'SiO2 70-77%, K2O+Na2O 7-8%',
      mineralComposition: '石英、正长石、斜长石、黑云母',
      physicalProperties: '块状构造、全晶质等粒结构',
      mechanicalProperties: '抗压强度 100-250 MPa',
      typicalElements: 'Si, K, Na, Al',
      formationEnvironment: '地壳深部岩浆房冷却',
      distribution: '各大陆地台、造山带',
      colorDescription: '肉红色、灰白色',
      textureDescription: '中粗粒结构',
      structureDescription: '块状构造',
      hardnessMin: 6, hardnessMax: 7,
      densityMin: 2.6, densityMax: 2.8,
      porosityMin: 0.5, porosityMax: 2,
      permeabilityMin: '极低', permeabilityMax: '低',
      sortOrder: 8, status: 'ACTIVE'
    },
    {
      standardId: 9, standardCode: 'ROCK-002', standardName: '玄武岩',
      category: 'ROCK_IGNEOUS', subcategory: '基性喷出岩',
      classificationSystem: '国际地科联岩石分类(IUGS)',
      parentMaterial: '基性岩浆喷出地表',
      chemicalComposition: 'SiO2 45-52%, FeO+MgO 15-20%',
      mineralComposition: '斜长石、辉石、橄榄石',
      physicalProperties: '致密块状、气孔状、杏仁状构造',
      mechanicalProperties: '抗压强度 150-300 MPa',
      typicalElements: 'Fe, Mg, Ca, Si',
      formationEnvironment: '火山喷发、洋中脊',
      distribution: '大洋地壳、火山活动带',
      colorDescription: '深灰色、黑色',
      textureDescription: '隐晶质、斑状结构',
      structureDescription: '块状、气孔状构造',
      hardnessMin: 5, hardnessMax: 7,
      densityMin: 2.8, densityMax: 3.1,
      porosityMin: 1, porosityMax: 15,
      permeabilityMin: '低', permeabilityMax: '中',
      sortOrder: 9, status: 'ACTIVE'
    },
    {
      standardId: 10, standardCode: 'ROCK-003', standardName: '石灰岩',
      category: 'ROCK_SEDIMENTARY', subcategory: '碳酸盐岩',
      classificationSystem: '中国岩石分类GB/T 17412',
      parentMaterial: '海相生物化学沉积',
      chemicalComposition: 'CaCO3 > 50%',
      mineralComposition: '方解石、少量白云石',
      physicalProperties: '结晶质或微晶质、层理发育',
      mechanicalProperties: '抗压强度 60-200 MPa',
      typicalElements: 'Ca, C, O',
      formationEnvironment: '浅海、湖相沉积环境',
      distribution: '华北地台、华南岩溶地区',
      colorDescription: '灰色、深灰色',
      textureDescription: '微晶、亮晶结构',
      structureDescription: '层状、块状构造',
      hardnessMin: 3, hardnessMax: 4,
      densityMin: 2.5, densityMax: 2.8,
      porosityMin: 1, porosityMax: 10,
      permeabilityMin: '低', permeabilityMax: '高(岩溶)',
      sortOrder: 10, status: 'ACTIVE'
    },
    {
      standardId: 11, standardCode: 'ROCK-004', standardName: '大理岩',
      category: 'ROCK_METAMORPHIC', subcategory: '接触变质',
      classificationSystem: '国际地科联岩石分类(IUGS)',
      parentMaterial: '石灰岩/白云岩变质',
      chemicalComposition: 'CaCO3 90%以上',
      mineralComposition: '方解石、白云石',
      physicalProperties: '结晶粒状、等粒变晶结构',
      mechanicalProperties: '抗压强度 80-180 MPa',
      typicalElements: 'Ca, Mg, C, O',
      formationEnvironment: '区域变质或接触热变质',
      distribution: '变质岩区、接触变质带',
      colorDescription: '白色、灰色，含杂质色带',
      textureDescription: '等粒变晶结构',
      structureDescription: '块状、条带状构造',
      hardnessMin: 3, hardnessMax: 4.5,
      densityMin: 2.6, densityMax: 2.9,
      porosityMin: 0.5, porosityMax: 3,
      permeabilityMin: '极低', permeabilityMax: '低',
      sortOrder: 11, status: 'ACTIVE'
    },
    {
      standardId: 12, standardCode: 'MIN-001', standardName: '石英',
      category: 'MINERAL', subcategory: '氧化物矿物',
      classificationSystem: ' Dana矿物分类',
      parentMaterial: '岩浆结晶、热液、沉积',
      chemicalComposition: 'SiO2',
      mineralComposition: '纯二氧化硅',
      physicalProperties: '玻璃光泽、贝壳状断口',
      typicalElements: 'Si, O',
      formationEnvironment: '多种地质环境',
      distribution: '各类岩石中均有分布',
      colorDescription: '无色、乳白色',
      textureDescription: '晶质体',
      structureDescription: '三方晶系',
      hardnessMin: 7, hardnessMax: 7,
      densityMin: 2.65, densityMax: 2.66,
      porosityMin: null, porosityMax: null,
      permeabilityMin: null, permeabilityMax: null,
      sortOrder: 12, status: 'ACTIVE'
    },
    {
      standardId: 13, standardCode: 'STR-001', standardName: '团粒结构',
      category: 'SOIL_STRUCTURE', subcategory: '良好结构',
      classificationSystem: '土壤结构分类',
      parentMaterial: '腐殖质胶结、生物作用',
      chemicalComposition: '有机质丰富',
      mineralComposition: '有机-无机复合体',
      physicalProperties: '疏松多孔、水稳性强',
      typicalElements: 'C, O, N, P',
      formationEnvironment: '肥沃土壤、根系活动区',
      distribution: '黑土、潮土、水稻土耕作层',
      colorDescription: '暗褐色',
      textureDescription: '粒径 0.25-10mm',
      structureDescription: '圆形、似圆形团聚体',
      hardnessMin: null, hardnessMax: null,
      densityMin: 1.0, densityMax: 1.3,
      porosityMin: 50, porosityMax: 65,
      permeabilityMin: '高', permeabilityMax: '极高',
      sortOrder: 13, status: 'ACTIVE'
    }
  ]
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.geo-page {
  background: linear-gradient(180deg, #F7F3ED 0%, #FEFBF6 100%);
  min-height: 100vh;
}

/* ===== Hero Section ===== */
.geo-hero {
  position: relative;
  overflow: hidden;
  padding: 80px 40px 100px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(139, 115, 85, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(201, 168, 108, 0.08) 0%, transparent 50%),
    repeating-linear-gradient(
      45deg,
      transparent,
      transparent 40px,
      rgba(139, 115, 85, 0.02) 40px,
      rgba(139, 115, 85, 0.02) 41px
    );
}

.hero-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, transparent, #F7F3ED);
}

.hero-content {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
  animation: heroFadeIn 1s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes heroFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-meta {
  font-size: 11px;
  color: #A68B6A;
  letter-spacing: 3px;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: 200;
  color: #3D2B1F;
  line-height: 1.2;
  margin-bottom: 24px;
  letter-spacing: -1px;
}

.hero-desc {
  font-size: 15px;
  color: #6B5344;
  line-height: 1.8;
  max-width: 560px;
  margin-bottom: 40px;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 300;
  color: #5D4E37;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.stat-label {
  font-size: 12px;
  color: #8B7355;
  letter-spacing: 1px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #E8E2D9;
}

/* ===== Categories Section ===== */
.geo-categories {
  padding: 0 40px 60px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-head {
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 400;
  color: #3D2B1F;
  margin-bottom: 6px;
}

.section-sub {
  font-size: 13px;
  color: #8B7355;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.category-card:hover {
  border-color: #C9A86C;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.12);
}

.category-card.active {
  background: linear-gradient(135deg, #FEFBF6 0%, #F5EFE2 100%);
  border-color: #C9A86C;
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.2);
}

.cat-icon {
  font-size: 24px;
  color: #8B7355;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F2ED;
  border-radius: 10px;
  flex-shrink: 0;
}

.category-card.active .cat-icon {
  background: rgba(201, 168, 108, 0.15);
  color: #8B6914;
}

.cat-info {
  flex: 1;
  min-width: 0;
}

.cat-name {
  font-size: 14px;
  font-weight: 500;
  color: #3D2B1F;
  margin-bottom: 4px;
}

.cat-count {
  font-size: 11px;
  color: #A68B6A;
}

.cat-arrow {
  color: #C9A86C;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s, transform 0.3s;
}

.category-card:hover .cat-arrow {
  opacity: 1;
  transform: translateX(2px);
}

.category-card.active .cat-arrow {
  opacity: 1;
}

/* ===== Content Section ===== */
.geo-content {
  padding: 0 40px 80px;
  max-width: 1200px;
  margin: 0 auto;
}

.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.search-box {
  position: relative;
  width: 360px;
  max-width: 100%;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #A68B6A;
  font-size: 16px;
}

.search-box input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 1px solid #E8E2D9;
  border-radius: 10px;
  background: #FEFBF6;
  font-size: 14px;
  color: #3D2B1F;
  outline: none;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.search-box input:focus {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.1);
}

.search-box input::placeholder {
  color: #B5A58D;
}

.view-select {
  padding: 10px 14px;
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  background: #FEFBF6;
  font-size: 12px;
  color: #5D4E37;
  cursor: pointer;
  outline: none;
}

/* ===== List View ===== */
.standard-list {
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 14px;
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 100px 200px 160px 1fr 80px;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E0 100%);
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #E8E2D9;
}

.list-row {
  display: grid;
  grid-template-columns: 100px 200px 160px 1fr 80px;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #EDE9E0;
  cursor: pointer;
  transition: background 0.2s ease;
  align-items: center;
}

.list-row:last-child {
  border-bottom: none;
}

.list-row:hover {
  background: rgba(201, 168, 108, 0.06);
}

.list-row .col-code {
  color: #8B7355;
  font-size: 12px;
}

.list-row .col-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #3D2B1F;
  font-weight: 500;
}

.name-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.list-row .col-cat {
  font-size: 13px;
  color: #5D4E37;
}

.prop-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #F5F2ED;
  border-radius: 6px;
  font-size: 12px;
  color: #6B5344;
  margin-right: 6px;
  margin-bottom: 4px;
}

.btn-ghost {
  padding: 6px 14px;
  border: 1px solid #E8E2D9;
  background: transparent;
  border-radius: 6px;
  font-size: 12px;
  color: #5D4E37;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-ghost:hover {
  border-color: #C9A86C;
  color: #8B6914;
  background: rgba(201, 168, 108, 0.08);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #A68B6A;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* ===== Grid View ===== */
.standard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.grid-card {
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.grid-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(139, 115, 85, 0.15);
  border-color: #C9A86C;
}

.card-visual {
  height: 100px;
  position: relative;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
}

.card-code {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.85);
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
}

.card-body {
  padding: 20px;
}

.card-title {
  font-size: 17px;
  font-weight: 500;
  color: #3D2B1F;
  margin-bottom: 6px;
}

.card-sub {
  font-size: 12px;
  color: #8B7355;
  margin-bottom: 16px;
}

.card-props {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.prop-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prop-label {
  font-size: 11px;
  color: #A68B6A;
  letter-spacing: 0.5px;
}

.prop-value {
  font-size: 13px;
  color: #5D4E37;
  font-weight: 500;
}

/* ===== Detail Panel ===== */
.detail-overlay {
  position: fixed;
  inset: 0;
  background: rgba(61, 43, 31, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.detail-panel {
  position: relative;
  width: 100%;
  max-width: 720px;
  max-height: 85vh;
  overflow-y: auto;
  background: #FEFBF6;
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(61, 43, 31, 0.25);
  animation: panelSlideUp 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes panelSlideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.detail-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  border: none;
  background: #F5F2ED;
  border-radius: 50%;
  font-size: 20px;
  color: #5D4E37;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-close:hover {
  background: #E8E2D9;
  color: #3D2B1F;
}

.detail-content {
  padding: 32px;
}

.detail-header {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E8E2D9;
}

.detail-cat {
  font-size: 11px;
  letter-spacing: 2px;
  margin-bottom: 12px;
  font-weight: 500;
}

.detail-title {
  font-size: 28px;
  font-weight: 300;
  color: #3D2B1F;
  margin-bottom: 8px;
}

.detail-code {
  font-size: 13px;
  color: #8B7355;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #3D2B1F;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #C9A86C;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  background: #F5F2ED;
  border-radius: 10px;
}

.detail-item-full {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  background: #F5F2ED;
  border-radius: 10px;
  margin-bottom: 10px;
}

.detail-label {
  font-size: 11px;
  color: #8B7355;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 14px;
  color: #3D2B1F;
  line-height: 1.5;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.element-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.15) 0%, rgba(139, 105, 20, 0.1) 100%);
  border: 1px solid rgba(201, 168, 108, 0.3);
  border-radius: 20px;
  font-size: 13px;
  color: #5D4E37;
  font-weight: 500;
}

.detail-remark {
  font-size: 14px;
  color: #5D4E37;
  line-height: 1.7;
  padding: 14px;
  background: #F5F2ED;
  border-radius: 10px;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #E8E2D9;
}

.btn-primary {
  flex: 1;
  padding: 12px 24px;
  background: linear-gradient(135deg, #C9A86C 0%, #A67C52 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(201, 168, 108, 0.4);
}

.btn-outline {
  padding: 12px 24px;
  background: transparent;
  border: 1px solid #C9A86C;
  border-radius: 10px;
  color: #8B6914;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-outline:hover {
  background: rgba(201, 168, 108, 0.1);
}

/* ===== Transitions ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
}

@media (max-width: 768px) {
  .geo-hero { padding: 48px 20px 60px; }
  .hero-title { font-size: 32px; }
  .hero-stats { gap: 20px; }
  .stat-value { font-size: 22px; }
  .geo-categories { padding: 0 20px 40px; }
  .geo-content { padding: 0 20px 60px; }
  .content-toolbar { flex-direction: column; align-items: stretch; }
  .search-box { width: 100%; }
  .list-header, .list-row {
    grid-template-columns: 80px 1fr 80px;
  }
  .list-header .col-prop, .list-row .col-prop,
  .list-header .col-cat, .list-row .col-cat { display: none; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-actions { flex-direction: column; }
}
</style>
