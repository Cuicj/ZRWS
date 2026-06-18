<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">CAD 图纸查看器</h1>
        <div class="page-meta mono">CAD VIEWER · F 全屏 · Esc 退出 · Ctrl+S 保存</div>
      </div>
      <div class="page-actions">
        <button class="btn-primary btn-sm" @click="saveCAD">💾 保存</button>
        <button class="btn-ghost btn-sm" @click="toggleFullscreen">⛶ 全屏</button>
      </div>
    </div>

    <Panel class="cad-panel">
      <div class="cad-toolbar">
        <button class="btn-ghost btn-sm" @click="resetView">↻ 重置</button>
        <button class="btn-ghost btn-sm" @click="zoomIn">+ 放大</button>
        <button class="btn-ghost btn-sm" @click="zoomOut">- 缩小</button>
        <div class="cad-info mono">乔口镇地形图 v2.0 | 1:1000 | WGS84</div>
      </div>
      <div class="cad-layers">
        <span v-for="l in layers" :key="l.id" class="layer-tag" :class="{ active: l.visible }" @click="l.visible = !l.visible">
          {{ l.name }}
        </span>
      </div>
      <canvas ref="cadCanvas" class="cad-canvas"></canvas>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';

const cadCanvas = ref(null);
const layers = ref([
  { id: 'base', name: '地形底图', visible: true },
  { id: 'plot', name: '地块边界', visible: true },
  { id: 'contour', name: '等高线', visible: true },
  { id: 'sample', name: '采样点', visible: true }
]);

let zoom = 1;

const renderCAD = () => {
  const canvas = cadCanvas.value;
  if (!canvas) return;
  canvas.width = canvas.offsetWidth;
  canvas.height = 400;
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = '#0A0E14';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.save();
  ctx.translate(canvas.width / 2, canvas.height / 2);
  ctx.scale(zoom, zoom);

  // 绘制网格
  if (layers.value[0].visible) {
    ctx.strokeStyle = '#2A303D';
    ctx.lineWidth = 0.5;
    for (let i = -200; i <= 200; i += 20) {
      ctx.beginPath(); ctx.moveTo(i, -200); ctx.lineTo(i, 200); ctx.stroke();
      ctx.beginPath(); ctx.moveTo(-200, i); ctx.lineTo(200, i); ctx.stroke();
    }
  }

  // 绘制地块
  if (layers.value[1].visible) {
    ctx.strokeStyle = '#C9A45C';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(-80, -40);
    ctx.lineTo(-40, -80);
    ctx.lineTo(60, -40);
    ctx.lineTo(80, 20);
    ctx.lineTo(-40, 60);
    ctx.closePath();
    ctx.stroke();
    ctx.fillStyle = 'rgba(201, 164, 92, 0.1)';
    ctx.fill();
  }

  // 绘制采样点
  if (layers.value[3].visible) {
    [[-60, -30], [0, 0], [40, 20]].forEach((p, i) => {
      ctx.beginPath();
      ctx.arc(p[0], p[1], 6, 0, Math.PI * 2);
      ctx.fillStyle = i % 2 === 0 ? '#B8860B' : '#6B8E5A';
      ctx.fill();
    });
  }

  ctx.restore();
};

const saveCAD = () => {
  const canvas = cadCanvas.value;
  if (!canvas) return;
  const link = document.createElement('a');
  link.download = 'cad-' + Date.now() + '.png';
  link.href = canvas.toDataURL('image/png');
  link.click();
};

const toggleFullscreen = () => {
  const el = document.querySelector('.cad-panel');
  if (!document.fullscreenElement) el.requestFullscreen();
  else document.exitFullscreen();
};

const resetView = () => { zoom = 1; renderCAD(); };
const zoomIn = () => { zoom = Math.min(5, zoom * 1.2); renderCAD(); };
const zoomOut = () => { zoom = Math.max(0.2, zoom / 1.2); renderCAD(); };

onMounted(() => renderCAD());
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.page-actions { display: flex; gap: var(--s-2); }
.cad-panel { padding: 0; }
.cad-toolbar { display: flex; gap: var(--s-2); padding: var(--s-3); border-bottom: var(--line); align-items: center; }
.cad-info { margin-left: auto; font-size: 11px; color: var(--signal-dim); }
.cad-layers { display: flex; gap: var(--s-2); padding: var(--s-2) var(--s-3); border-bottom: var(--line-soft); }
.layer-tag { padding: 4px 12px; font-size: 11px; color: var(--signal-dim); border: 1px solid var(--ink-600); cursor: pointer; }
.layer-tag.active { color: var(--sand-500); border-color: var(--sand-500); }
.cad-canvas { width: 100%; height: 400px; display: block; }
</style>