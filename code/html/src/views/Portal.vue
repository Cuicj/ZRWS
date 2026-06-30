<template>
  <div class="portal">
    <!-- 顶栏 -->
    <header class="portal-top">
      <div class="brand">
        <span class="brand-mark">◐</span>
        <span class="brand-name display">智壤卫士</span>
        <span class="brand-meta mono">ZIRANG SHIELD v{{ appVersion }}</span>
      </div>
      <nav class="portal-nav">
        <a class="nav-link active" href="#modules">模块</a>
        <a class="nav-link" href="#specs">规范</a>
        <router-link class="nav-link" to="/login">登录</router-link>
        <router-link class="btn btn-primary" to="/app/dashboard">进入工作台 →</router-link>
      </nav>
    </header>

    <!-- 主视觉区 -->
    <section class="hero">
      <div class="hero-slides">
        <div
          v-for="(slide, index) in heroSlides"
          :key="index"
          class="hero-slide"
          :class="{ active: currentSlide === index }"
          :style="{ backgroundImage: `url(${slide.image})` }"
        ></div>
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-grid"></div>
      <div class="hero-content">
        <div class="hero-meta mono">
          <span>2026.06.18</span>
          <span>·</span>
          <span>湖南·望城</span>
          <span>·</span>
          <span class="status-dot"></span>
          <span>3 设备在线</span>
        </div>
        <h1 class="hero-title display">
          在 <em>40&nbsp;米</em>的空中<br>
          看见 <em>土层</em> 的语言。
        </h1>
        <p class="hero-sub">
          融合无人机航空摄影、RTK 高精度定位、AI 土质识别与多级审批工作流，
          为土地确权、地灾评估与高标准农田建设提供一站式数据中台。
        </p>
        <div class="hero-actions">
          <router-link class="btn btn-primary" to="/app/dashboard">进入工作台</router-link>
          <a class="btn btn-ghost" href="#modules">浏览模块 ↓</a>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-block">
          <div class="stat-num display">156</div>
          <div class="stat-label">采集任务</div>
        </div>
        <div class="stat-block">
          <div class="stat-num display">860<sub>亩</sub></div>
          <div class="stat-label">最新任务覆盖</div>
        </div>
        <div class="stat-block">
          <div class="stat-num display">±1.2<sub>cm</sub></div>
          <div class="stat-label">RTK 精度</div>
        </div>
        <div class="stat-block">
          <div class="stat-num display">94.1%</div>
          <div class="stat-label">质检合格率</div>
        </div>
      </div>
      <div class="hero-dots">
        <span
          v-for="(slide, index) in heroSlides"
          :key="index"
          class="dot"
          :class="{ active: currentSlide === index }"
          @click="currentSlide = index"
        ></span>
      </div>
    </section>

    <!-- 模块卡片陈列 -->
    <section class="modules" id="modules">
      <div class="section-head">
        <div class="section-num mono">§ 01</div>
        <h2 class="section-title display">功能模块</h2>
        <div class="section-hint mono">点击进入独立工作页</div>
      </div>

      <div class="module-grid">
        <!-- 模块卡片 -->
        <router-link
          v-for="(module, index) in modules"
          :key="module.path"
          :to="`/app/${module.path}`"
          class="module-card"
          :class="module.size"
        >
          <div class="card-head">
            <span class="card-num mono">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="card-tag mono">{{ module.tag }}</span>
          </div>
          <h3 class="card-title display">{{ module.name }}</h3>
          <p class="card-desc">{{ module.desc }}</p>
          <div class="card-bg" :style="module.bgStyle"></div>
        </router-link>
      </div>
    </section>

    <!-- 技术规范 -->
    <section class="specs" id="specs">
      <div class="section-head">
        <div class="section-num mono">§ 02</div>
        <h2 class="section-title display">技术规范</h2>
      </div>
      <div class="spec-grid">
        <div class="spec-item">
          <div class="spec-key mono">前端</div>
          <div class="spec-val">Vue 3 + Element Plus + ECharts 5 + bpmn-js</div>
        </div>
        <div class="spec-item">
          <div class="spec-key mono">后端</div>
          <div class="spec-val">Spring Cloud · Python AI · Flowable 7.0.0</div>
        </div>
        <div class="spec-item">
          <div class="spec-key mono">数据库</div>
          <div class="spec-val">MySQL 8 · Redis · InfluxDB</div>
        </div>
        <div class="spec-item">
          <div class="spec-key mono">定位精度</div>
          <div class="spec-val">RTK 平面 ±1.2cm · 高程 ±2.1cm</div>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="portal-footer">
      <div class="footer-line mono">ZRWS · 智壤卫士 · 2026</div>
      <div class="footer-line mono">DOMAIN: aerial-mapping · soil-analysis · cad · workflow</div>
    </footer>
  </div>
</template>

<script setup>
/**
 * Portal.vue - 门户首页
 * 杂志式模块陈列，极简测绘美学
 */
import { ref, onMounted, onUnmounted } from 'vue';

const appVersion = __APP_VERSION__;
const IMG_BASE = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image';

const heroSlides = ref([
  {
    image: `${IMG_BASE}?prompt=aerial%20drone%20photography%20of%20vast%20agricultural%20fields%2C%20patchwork%20of%20golden%20and%20green%20farmland%2C%20top%20down%20bird%20eye%20view%2C%20golden%20hour%20sunlight%2C%20cinematic%20landscape%2C%20ultra%20detailed%2C%208k%20quality%2C%20professional%20photography&image_size=landscape_16_9`,
    title: '无人机航空测绘'
  },
  {
    image: `${IMG_BASE}?prompt=close%20up%20of%20rich%20soil%20texture%20with%20organic%20matter%2C%20dark%20earthy%20tones%2C%20golden%20light%20rays%2C%20macro%20photography%2C%20scientific%20analysis%20aesthetic%2C%20depth%20of%20field%2C%20ultra%20detailed%2C%20cinematic&image_size=landscape_16_9`,
    title: 'AI 土质智能分析'
  },
  {
    image: `${IMG_BASE}?prompt=technical%20blueprint%20CAD%20drawing%20of%20land%20surveying%20with%20contour%20lines%20and%20measurements%2C%20warm%20amber%20light%20on%20dark%20background%2C%20engineering%20aesthetic%2C%20architectural%20style%2C%20ultra%20detailed&image_size=landscape_16_9`,
    title: 'CAD 图纸与工程'
  }
]);

const currentSlide = ref(0);
let slideTimer = null;

const startAutoSlide = () => {
  slideTimer = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % heroSlides.value.length;
  }, 5000);
};

onMounted(() => {
  startAutoSlide();
});

onUnmounted(() => {
  if (slideTimer) clearInterval(slideTimer);
});

// 模块配置
const modules = ref([
  {
    path: 'dashboard',
    name: '运行仪表盘',
    tag: 'DASHBOARD',
    desc: '总览任务、设备、日志与关键指标',
    size: 'size-xl',
    bgStyle: 'background: radial-gradient(circle at 70% 30%, #C9A45C22 0%, transparent 60%);'
  },
  {
    path: 'mission-list',
    name: '采集任务管理',
    tag: 'FLIGHT',
    desc: '任务编排 · 飞行控制 · GPS 航迹 · 土壤采样',
    size: ''
  },
  {
    path: 'data-import',
    name: '数据处理与质检',
    tag: 'PROCESS',
    desc: '导入 · 流水线 · 质检 · 3D 重建',
    size: ''
  },
  {
    path: 'soil-classify',
    name: 'AI 智能分析',
    tag: 'AI',
    desc: '土质分类 · 灾害评估 · 面积计算',
    size: ''
  },
  {
    path: 'cad-viewer',
    name: 'CAD 图纸管理',
    tag: 'CAD',
    desc: 'F 全屏 · Esc 退出 · Ctrl+S 保存',
    size: ''
  },
  {
    path: 'approval-list',
    name: '审批与工作流',
    tag: 'WORKFLOW',
    desc: 'Flowable 引擎 · 4 类流程 · SLA 升级',
    size: ''
  },
  {
    path: 'device',
    name: '设备管理',
    tag: 'DEVICE',
    desc: '无人机 · RTK · 处理节点 · 传感器',
    size: ''
  },
  {
    path: 'workflow-design',
    name: 'bpmn-js 流程设计器',
    tag: 'DESIGNER',
    desc: '拖拽绘制 → Flowable 一键部署',
    size: 'size-wide',
    bgStyle: 'background: linear-gradient(135deg, #4A7C9E11 0%, transparent 50%);'
  }
]);
</script>

<style scoped>
.portal {
  min-height: 100vh;
  position: relative;
  z-index: 2;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
}

/* 顶栏 */
.portal-top {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--s-5) var(--s-7);
  border-bottom: 1px solid var(--ink-600);
  background: rgba(254, 251, 246, 0.85);
  backdrop-filter: blur(12px);
}

.brand {
  display: flex;
  align-items: baseline;
  gap: var(--s-3);
}

.brand-mark {
  font-size: 28px;
  color: var(--sand-500);
}

.brand-name {
  font-size: 22px;
  font-weight: 400;
  color: var(--signal);
}

.brand-meta {
  font-size: 11px;
  color: var(--signal-dim);
  letter-spacing: 0.2em;
}

.portal-nav {
  display: flex;
  gap: var(--s-5);
  align-items: center;
}

.nav-link {
  font-size: 13px;
  color: var(--signal-dim);
  text-decoration: none;
  padding: 6px 0;
  border-bottom: 1px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-link:hover,
.nav-link.active {
  color: var(--signal);
  border-bottom-color: var(--sand-500);
}

/* Hero */
.hero {
  position: relative;
  padding: var(--s-10) var(--s-7) var(--s-9);
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--s-7);
  border-bottom: 1px solid var(--ink-600);
  overflow: hidden;
}

.hero-slides {
  position: absolute;
  inset: 0;
}

.hero-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.hero-slide.active {
  opacity: 1;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(254, 251, 246, 0.88) 0%, rgba(247, 243, 237, 0.75) 100%);
  z-index: 1;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--ink-600) 1px, transparent 1px),
    linear-gradient(90deg, var(--ink-600) 1px, transparent 1px);
  background-size: 80px 80px;
  opacity: 0.25;
  pointer-events: none;
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-meta {
  display: flex;
  gap: var(--s-3);
  font-size: 12px;
  color: var(--signal-dim);
  margin-bottom: var(--s-5);
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: var(--ok);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(129, 199, 132, 0.25);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-size: clamp(48px, 7vw, 96px);
  line-height: 1.05;
  font-weight: 300;
  margin-bottom: var(--s-5);
  color: var(--signal);
}

.hero-title em {
  font-style: italic;
  color: var(--sand-500);
  font-weight: 500;
}

.hero-sub {
  font-size: 15px;
  line-height: 1.7;
  color: var(--signal-dim);
  max-width: 540px;
  margin-bottom: var(--s-6);
}

.hero-actions {
  display: flex;
  gap: var(--s-3);
}

.hero-stats {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-4);
  align-content: end;
}

.stat-block {
  padding: var(--s-4) var(--s-5);
  border-left: 2px solid var(--sand-500);
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.stat-num {
  font-size: 36px;
  line-height: 1;
  margin-bottom: 4px;
  color: var(--signal);
}

.stat-num sub {
  font-size: 12px;
  color: var(--signal-dim);
  margin-left: 2px;
}

.stat-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--signal-dim);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.hero-dots {
  position: absolute;
  bottom: var(--s-5);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--s-2);
  z-index: 3;
}

.hero-dots .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ink-500);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-dots .dot.active {
  background: var(--sand-500);
  width: 24px;
  border-radius: 4px;
}

.hero-dots .dot:hover {
  background: var(--sand-400);
}

/* Section */
.section-head {
  display: flex;
  align-items: baseline;
  gap: var(--s-4);
  padding: var(--s-7) var(--s-7) var(--s-5);
}

.section-num {
  font-size: 11px;
  color: var(--sand-500);
}

.section-title {
  font-size: 32px;
  font-weight: 300;
  color: var(--signal);
}

.section-hint {
  font-size: 11px;
  color: var(--signal-dim);
  margin-left: auto;
}

/* 模块网格 */
.modules {
  padding-bottom: var(--s-9);
  border-bottom: 1px solid var(--ink-600);
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--s-4);
  padding: 0 var(--s-7);
}

.module-card {
  position: relative;
  overflow: hidden;
  grid-column: span 4;
  padding: var(--s-5);
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-xl);
  text-decoration: none;
  color: var(--signal);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.module-card.size-xl {
  grid-column: span 6;
  min-height: 280px;
}

.module-card.size-wide {
  grid-column: span 8;
  min-height: 200px;
}

.module-card:hover {
  border-color: var(--sand-500);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.15);
}

.module-card:hover .card-bg {
  opacity: 1;
}

.card-bg {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  border-radius: var(--radius-xl);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s-4);
  position: relative;
  z-index: 2;
}

.card-num {
  font-size: 11px;
  color: var(--sand-500);
}

.card-tag {
  font-size: 10px;
  color: var(--signal-dim);
  padding: 3px 10px;
  border: 1px solid var(--ink-500);
  border-radius: var(--radius-sm);
  letter-spacing: 0.15em;
  background: rgba(255, 255, 255, 0.5);
}

.card-title {
  font-size: 26px;
  font-weight: 400;
  line-height: 1.2;
  margin-bottom: var(--s-3);
  position: relative;
  z-index: 2;
  color: var(--signal);
}

.card-desc {
  font-size: 13px;
  color: var(--signal-dim);
  line-height: 1.6;
  position: relative;
  z-index: 2;
}

/* 规范区 */
.specs {
  padding-bottom: var(--s-9);
  border-bottom: 1px solid var(--ink-600);
}

.spec-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-3);
  padding: 0 var(--s-7);
}

.spec-item {
  padding: var(--s-5);
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.spec-item:hover {
  border-color: var(--sand-500);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.12);
}

.spec-key {
  font-size: 10px;
  color: var(--sand-500);
  letter-spacing: 0.15em;
  margin-bottom: var(--s-2);
  font-weight: 600;
}

.spec-val {
  font-size: 14px;
  color: var(--signal);
  line-height: 1.6;
}

/* Footer */
.portal-footer {
  padding: var(--s-6) var(--s-7);
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--signal-dim);
  background: var(--ink-800);
  border-top: 1px solid var(--ink-600);
}
</style>