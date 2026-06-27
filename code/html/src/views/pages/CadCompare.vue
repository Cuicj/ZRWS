<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">CAD 图纸对比</h1>
      <div class="page-meta mono">CAD COMPARE · 新旧版本差异</div>
    </div>

    <div class="compare-row">
      <Panel title="原始图纸 (v1.0)">
        <canvas ref="oldCanvas" class="compare-canvas"></canvas>
      </Panel>
      <Panel title="最新图纸 (v2.0)">
        <canvas ref="newCanvas" class="compare-canvas"></canvas>
      </Panel>
    </div>

    <Panel title="差异统计">
      <div class="diff-row">
        <StatCard label="面积变化" value="+12.5" unit="亩" icon="+" variant="warn" />
        <StatCard label="新增地块" value="3" unit="个" icon="+" variant="ok" />
        <StatCard label="消失地块" value="1" unit="个" icon="-" variant="danger" />
        <StatCard label="边界调整" value="5" unit="处" icon="↔" variant="accent" />
      </div>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';

const oldCanvas = ref(null);
const newCanvas = ref(null);

const renderCanvas = (canvas, version) => {
  if (!canvas) return;
  canvas.width = canvas.offsetWidth;
  canvas.height = 300;
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = '#FEFBF6';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.strokeStyle = version === 'old' ? '#B8A98F' : '#C9A86C';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.moveTo(100, 80);
  ctx.lineTo(200, 50);
  ctx.lineTo(350, 100);
  ctx.lineTo(300, 200);
  ctx.lineTo(150, 180);
  ctx.closePath();
  ctx.stroke();
};

onMounted(() => {
  renderCanvas(oldCanvas.value, 'old');
  renderCanvas(newCanvas.value, 'new');
});
</script>

<style scoped>
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}
.page-head {
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: 1px solid #E8E2D9;
}
.page-title {
  font-size: 28px;
  font-weight: 200;
  color: #5D4E37;
}
.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
}
.compare-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-3);
  margin-bottom: var(--s-4);
}
.compare-canvas {
  width: 100%;
  height: 300px;
  display: block;
  border-radius: 12px;
}
.diff-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-3);
}
.mono {
  color: #8B7355;
}
</style>
