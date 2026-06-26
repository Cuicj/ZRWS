<template>
  <div class="cad-page">
    <div class="page-head">
      <div>
        <h1 class="page-title display">GIS / 3D 地理信息</h1>
        <div class="page-meta mono">CESIUM VIEWER · 三维地球 · 乔口镇地理信息 v3.0</div>
      </div>
      <div class="page-actions">
        <div class="view-toggle">
          <button
            class="view-btn"
            :class="{ active: viewMode === '2d' }"
            @click="viewMode = '2d'"
          >
            2D 地图
          </button>
          <button
            class="view-btn"
            :class="{ active: viewMode === '3d' }"
            @click="viewMode = '3d'"
          >
            3D 地球
          </button>
        </div>
        <button class="btn-primary btn-sm" @click="exportScene">💾 截图</button>
        <button class="btn-ghost btn-sm" @click="toggleFullscreen">⛶ 全屏</button>
      </div>
    </div>

    <div class="cad-container" ref="containerRef">
      <div class="cad-toolbar">
        <button class="tool-btn" @click="resetView" title="重置视图">↻</button>
        <button class="tool-btn" @click="zoomIn" title="放大">+</button>
        <button class="tool-btn" @click="zoomOut" title="缩小">−</button>
        <div class="tool-divider"></div>
        <button
          class="tool-btn"
          :class="{ active: showTerrain }"
          @click="toggleTerrain"
          title="地形"
        >
          ⛰
        </button>
        <button
          class="tool-btn"
          :class="{ active: showBuilding }"
          @click="toggleBuilding"
          title="建筑"
        >
          🏢
        </button>
        <div class="tool-divider"></div>
        <button
          class="tool-btn"
          :class="{ active: imageryLayer === 'osm' }"
          @click="setImageryLayer('osm')"
          title="OSM地图"
        >
          🗺
        </button>
        <button
          class="tool-btn"
          :class="{ active: imageryLayer === 'satellite' }"
          @click="setImageryLayer('satellite')"
          title="卫星影像"
        >
          🛰
        </button>
        <button
          class="tool-btn"
          :class="{ active: imageryLayer === 'dark' }"
          @click="setImageryLayer('dark')"
          title="暗色地图"
        >
          🌙
        </button>
        <div class="tool-divider"></div>
        <div class="cad-info mono">
          <span v-if="viewMode === '3d'">
            经度: {{ cameraInfo.lon }} · 纬度: {{ cameraInfo.lat }} · 高度: {{ cameraInfo.height }}m
          </span>
          <span v-else>
            比例尺 1:{{ mapScale }} · WGS84 · 面积 860 亩
          </span>
        </div>
      </div>

      <div class="cad-layers">
        <span
          v-for="l in layers"
          :key="l.id"
          class="layer-tag"
          :class="{ active: l.visible }"
          @click="toggleLayer(l.id)"
        >
          <span class="layer-dot" :style="{ background: l.color }"></span>
          {{ l.name }}
        </span>
      </div>

      <canvas
        v-show="viewMode === '2d'"
        ref="cadCanvas"
        class="cad-canvas"
      ></canvas>

      <div
        v-show="viewMode === '3d'"
        ref="cesiumContainer"
        class="cesium-container"
      ></div>

      <div v-if="viewMode === '3d'" class="info-panel">
        <div class="info-title mono">地理信息</div>
        <div class="info-item">
          <span class="info-label">中心经度</span>
          <span class="info-value">{{ cameraInfo.lon }}°E</span>
        </div>
        <div class="info-item">
          <span class="info-label">中心纬度</span>
          <span class="info-value">{{ cameraInfo.lat }}°N</span>
        </div>
        <div class="info-item">
          <span class="info-label">相机高度</span>
          <span class="info-value">{{ cameraInfo.height }} m</span>
        </div>
        <div class="info-item">
          <span class="info-label">覆盖面积</span>
          <span class="info-value">860 亩</span>
        </div>
        <div class="info-item">
          <span class="info-label">坐标系</span>
          <span class="info-value">WGS84</span>
        </div>
        <div class="info-divider"></div>
        <div class="info-title mono">操作提示</div>
        <div class="tip-item">🖱️ 左键拖动旋转地球</div>
        <div class="tip-item">🖱️ 右键拖动平移</div>
        <div class="tip-item">🖲️ 滚轮缩放</div>
        <div class="tip-item">🖱️ 中键拖动倾斜视角</div>
      </div>

      <div v-if="viewMode === '3d'" class="legend-panel">
        <div class="legend-title mono">图 例</div>
        <div class="legend-item">
          <span class="legend-color" style="background: #D4A853"></span>
          <span class="legend-text">土壤采样区</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #52c41a"></span>
          <span class="legend-text">农田保护区</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #4A7C9E"></span>
          <span class="legend-text">水体区域</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #faad14"></span>
          <span class="legend-text">主要道路</span>
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background: #ff4d4f"></span>
          <span class="legend-text">监测点位</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as Cesium from 'cesium';

const containerRef = ref(null);
const cadCanvas = ref(null);
const cesiumContainer = ref(null);
const viewMode = ref('3d');
const showTerrain = ref(true);
const showBuilding = ref(false);
const imageryLayer = ref('osm');
const mapScale = ref(1000);

const cameraInfo = ref({
  lon: '112.78',
  lat: '28.52',
  height: '5000'
});

const layers = ref([
  { id: 'sample', name: '采样点位', visible: true, color: '#ff4d4f' },
  { id: 'farmland', name: '农田保护区', visible: true, color: '#52c41a' },
  { id: 'water', name: '水体区域', visible: true, color: '#4A7C9E' },
  { id: 'road', name: '主要道路', visible: true, color: '#faad14' },
  { id: 'boundary', name: '行政边界', visible: true, color: '#D4A853' }
]);

let viewer = null;
let samplingPoints = [];
let farmlandEntities = [];
let waterEntities = [];
let roadEntities = [];
let boundaryEntity = null;

const CHANGSHA_LON = 112.78;
const CHANGSHA_LAT = 28.52;

const toggleLayer = (id) => {
  const layer = layers.value.find(l => l.id === id);
  if (layer) layer.visible = !layer.visible;
  if (viewMode.value === '2d') {
    render2D();
  } else {
    updateCesiumLayers();
  }
};

const updateCesiumLayers = () => {
  if (!viewer) return;

  const sampleLayer = layers.value.find(l => l.id === 'sample');
  const farmlandLayer = layers.value.find(l => l.id === 'farmland');
  const waterLayer = layers.value.find(l => l.id === 'water');
  const roadLayer = layers.value.find(l => l.id === 'road');
  const boundaryLayer = layers.value.find(l => l.id === 'boundary');

  samplingPoints.forEach(p => p.show = sampleLayer?.visible ?? true);
  farmlandEntities.forEach(e => e.show = farmlandLayer?.visible ?? true);
  waterEntities.forEach(e => e.show = waterLayer?.visible ?? true);
  roadEntities.forEach(e => e.show = roadLayer?.visible ?? true);
  if (boundaryEntity) boundaryEntity.show = boundaryLayer?.visible ?? true;
};

const toggleTerrain = () => {
  showTerrain.value = !showTerrain.value;
  if (viewer) {
    viewer.terrainProvider = showTerrain.value
      ? new Cesium.EllipsoidTerrainProvider()
      : new Cesium.EllipsoidTerrainProvider();
  }
};

const toggleBuilding = () => {
  showBuilding.value = !showBuilding.value;
  if (viewer && viewer.scene.primitives) {
    viewer.scene.globe.depthTestAgainstTerrain = showBuilding.value;
  }
};

const setImageryLayer = (type) => {
  imageryLayer.value = type;
  if (!viewer) return;

  viewer.imageryLayers.removeAll();

  switch (type) {
    case 'osm':
      viewer.imageryLayers.addImageryProvider(
        new Cesium.UrlTemplateImageryProvider({
          url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          credit: '© OpenStreetMap contributors',
          maximumLevel: 19
        })
      );
      break;
    case 'satellite':
      viewer.imageryLayers.addImageryProvider(
        new Cesium.UrlTemplateImageryProvider({
          url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
          credit: '© Esri',
          maximumLevel: 19
        })
      );
      break;
    case 'dark':
      viewer.imageryLayers.addImageryProvider(
        new Cesium.UrlTemplateImageryProvider({
          url: 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',
          credit: '© CARTO',
          subdomains: ['a', 'b', 'c', 'd'],
          maximumLevel: 19
        })
      );
      break;
  }
};

const render2D = () => {
  const canvas = cadCanvas.value;
  if (!canvas) return;
  canvas.width = canvas.offsetWidth;
  canvas.height = canvas.offsetHeight - 100;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#1F2633';
  ctx.fillRect(0, 0, w, h);

  ctx.save();
  ctx.translate(w / 2, h / 2);

  const boundaryLayer = layers.value.find(l => l.id === 'boundary');
  const farmlandLayer = layers.value.find(l => l.id === 'farmland');
  const waterLayer = layers.value.find(l => l.id === 'water');
  const roadLayer = layers.value.find(l => l.id === 'road');
  const sampleLayer = layers.value.find(l => l.id === 'sample');

  ctx.strokeStyle = 'rgba(61, 72, 99, 0.3)';
  ctx.lineWidth = 0.5;
  for (let i = -w; i < w; i += 40) {
    ctx.beginPath();
    ctx.moveTo(i, -h);
    ctx.lineTo(i, h);
    ctx.stroke();
  }
  for (let i = -h; i < h; i += 40) {
    ctx.beginPath();
    ctx.moveTo(-w, i);
    ctx.lineTo(w, i);
    ctx.stroke();
  }

  if (boundaryLayer?.visible) {
    ctx.fillStyle = 'rgba(212, 168, 83, 0.1)';
    ctx.strokeStyle = '#D4A853';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(-180, -100);
    ctx.lineTo(-100, -140);
    ctx.lineTo(60, -120);
    ctx.lineTo(160, -40);
    ctx.lineTo(180, 60);
    ctx.lineTo(100, 140);
    ctx.lineTo(-40, 120);
    ctx.lineTo(-140, 80);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
  }

  if (farmlandLayer?.visible) {
    ctx.fillStyle = 'rgba(82, 196, 26, 0.2)';
    ctx.strokeStyle = '#52c41a';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(-120, -60);
    ctx.lineTo(-40, -90);
    ctx.lineTo(40, -70);
    ctx.lineTo(80, -20);
    ctx.lineTo(60, 40);
    ctx.lineTo(-20, 60);
    ctx.lineTo(-80, 30);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    ctx.strokeStyle = 'rgba(82, 196, 26, 0.3)';
    ctx.lineWidth = 0.5;
    for (let i = -100; i < 60; i += 12) {
      ctx.beginPath();
      ctx.moveTo(i, -70);
      ctx.lineTo(i + 20, 40);
      ctx.stroke();
    }
  }

  if (waterLayer?.visible) {
    ctx.fillStyle = 'rgba(74, 124, 158, 0.4)';
    ctx.strokeStyle = '#4A7C9E';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(-160, 20);
    ctx.bezierCurveTo(-100, 50, -40, 20, 20, 40);
    ctx.bezierCurveTo(80, 60, 140, 30, 160, 20);
    ctx.lineTo(160, 50);
    ctx.bezierCurveTo(100, 90, 40, 60, -20, 80);
    ctx.bezierCurveTo(-80, 100, -140, 70, -160, 50);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)';
    ctx.lineWidth = 1;
    for (let i = 0; i < 3; i++) {
      ctx.beginPath();
      ctx.moveTo(-120 + i * 40, 35 + i * 8);
      ctx.quadraticCurveTo(-80 + i * 40, 45 + i * 8, -40 + i * 40, 35 + i * 8);
      ctx.stroke();
    }
  }

  if (roadLayer?.visible) {
    ctx.strokeStyle = '#faad14';
    ctx.lineWidth = 5;
    ctx.lineCap = 'round';
    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.moveTo(-200, -20);
    ctx.lineTo(-100, -50);
    ctx.quadraticCurveTo(0, -20, 100, 20);
    ctx.lineTo(200, -10);
    ctx.stroke();

    ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
    ctx.lineWidth = 1;
    ctx.setLineDash([10, 8]);
    ctx.beginPath();
    ctx.moveTo(-200, -20);
    ctx.lineTo(-100, -50);
    ctx.quadraticCurveTo(0, -20, 100, 20);
    ctx.lineTo(200, -10);
    ctx.stroke();
    ctx.setLineDash([]);
  }

  if (sampleLayer?.visible) {
    const points = [
      [-110, -50], [-50, -30], [10, -10], [70, 30],
      [20, 50], [-60, 20], [-30, -70], [50, -40],
      [120, 10], [-100, 40]
    ];
    points.forEach((p, i) => {
      const gradient = ctx.createRadialGradient(p[0], p[1], 0, p[0], p[1], 15);
      gradient.addColorStop(0, i % 2 === 0 ? 'rgba(255, 77, 79, 0.4)' : 'rgba(212, 168, 83, 0.4)');
      gradient.addColorStop(1, 'rgba(255, 77, 79, 0)');
      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(p[0], p[1], 15, 0, Math.PI * 2);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(p[0], p[1], 6, 0, Math.PI * 2);
      ctx.fillStyle = i % 2 === 0 ? '#ff4d4f' : '#D4A853';
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.stroke();
    });
  }

  ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
  ctx.font = '11px monospace';
  ctx.fillText('N 28.52°', -80, -130);
  ctx.fillText('E 112.78°', 60, -130);

  ctx.restore();
};

const initCesium = async () => {
  const container = cesiumContainer.value;
  if (!container) return;

  Cesium.Ion.defaultAccessToken = '';

  viewer = new Cesium.Viewer(container, {
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    navigationHelpButton: false,
    fullscreenButton: false,
    infoBox: false,
    selectionIndicator: false,
    shouldAnimate: true
  });

  viewer.scene.backgroundColor = new Cesium.Color(0.12, 0.15, 0.2, 1);
  viewer.scene.globe.enableLighting = true;

  setImageryLayer('osm');

  viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider();

  addBoundary();
  addFarmland();
  addWaterBodies();
  addRoads();
  addSamplingPoints();

  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(CHANGSHA_LON, CHANGSHA_LAT, 15000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-45),
      roll: 0
    },
    duration: 2
  });

  viewer.scene.postRender.addEventListener(updateCameraInfo);
};

const addBoundary = () => {
  if (!viewer) return;

  const coordinates = [
    112.72, 28.48,
    112.76, 28.46,
    112.82, 28.47,
    112.86, 28.51,
    112.85, 28.56,
    112.80, 28.59,
    112.74, 28.58,
    112.71, 28.54
  ];

  const hierarchy = Cesium.Cartesian3.fromDegreesArray(coordinates);

  boundaryEntity = viewer.entities.add({
    name: '行政边界',
    polygon: {
      hierarchy: hierarchy,
      material: Cesium.Color.fromCssColorString('#D4A853').withAlpha(0.15),
      outline: true,
      outlineColor: Cesium.Color.fromCssColorString('#D4A853'),
      outlineWidth: 3
    }
  });
};

const addFarmland = () => {
  if (!viewer) return;

  const farmAreas = [
    {
      coords: [
        112.74, 28.50,
        112.78, 28.49,
        112.81, 28.51,
        112.80, 28.54,
        112.76, 28.55,
        112.73, 28.53
      ],
      height: 0
    }
  ];

  farmAreas.forEach(area => {
    const entity = viewer.entities.add({
      name: '农田保护区',
      polygon: {
        hierarchy: Cesium.Cartesian3.fromDegreesArray(area.coords),
        height: area.height,
        material: Cesium.Color.fromCssColorString('#52c41a').withAlpha(0.35),
        outline: true,
        outlineColor: Cesium.Color.fromCssColorString('#52c41a'),
        outlineWidth: 2
      }
    });
    farmlandEntities.push(entity);
  });
};

const addWaterBodies = () => {
  if (!viewer) return;

  const waterAreas = [
    {
      coords: [
        112.73, 28.53,
        112.77, 28.54,
        112.82, 28.53,
        112.84, 28.54,
        112.83, 28.56,
        112.78, 28.57,
        112.74, 28.56
      ],
      height: 0
    }
  ];

  waterAreas.forEach(area => {
    const entity = viewer.entities.add({
      name: '水体区域',
      polygon: {
        hierarchy: Cesium.Cartesian3.fromDegreesArray(area.coords),
        height: area.height,
        material: Cesium.Color.fromCssColorString('#4A7C9E').withAlpha(0.5),
        outline: true,
        outlineColor: Cesium.Color.fromCssColorString('#4A7C9E'),
        outlineWidth: 2
      }
    });
    waterEntities.push(entity);
  });
};

const addRoads = () => {
  if (!viewer) return;

  const roads = [
    {
      coords: [
        112.70, 28.52,
        112.75, 28.50,
        112.80, 28.52,
        112.86, 28.51
      ],
      width: 20
    }
  ];

  roads.forEach(road => {
    const entity = viewer.entities.add({
      name: '主要道路',
      polyline: {
        positions: Cesium.Cartesian3.fromDegreesArray(road.coords),
        width: road.width,
        material: Cesium.Color.fromCssColorString('#faad14'),
        clampToGround: true
      }
    });
    roadEntities.push(entity);
  });
};

const addSamplingPoints = () => {
  if (!viewer) return;

  const points = [
    { lon: 112.74, lat: 28.51, name: '采样点01' },
    { lon: 112.76, lat: 28.50, name: '采样点02' },
    { lon: 112.79, lat: 28.51, name: '采样点03' },
    { lon: 112.81, lat: 28.53, name: '采样点04' },
    { lon: 112.78, lat: 28.55, name: '采样点05' },
    { lon: 112.75, lat: 28.54, name: '采样点06' },
    { lon: 112.77, lat: 28.49, name: '采样点07' },
    { lon: 112.80, lat: 28.50, name: '采样点08' },
    { lon: 112.83, lat: 28.52, name: '采样点09' },
    { lon: 112.73, lat: 28.53, name: '采样点10' }
  ];

  points.forEach((p, i) => {
    const point = viewer.entities.add({
      name: p.name,
      position: Cesium.Cartesian3.fromDegrees(p.lon, p.lat),
      point: {
        pixelSize: 12,
        color: i % 2 === 0
          ? Cesium.Color.fromCssColorString('#ff4d4f')
          : Cesium.Color.fromCssColorString('#D4A853'),
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 3,
        heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
      },
      label: {
        text: p.name,
        font: '12px sans-serif',
        fillColor: Cesium.Color.WHITE,
        outlineColor: Cesium.Color.BLACK,
        outlineWidth: 2,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        pixelOffset: new Cesium.Cartesian2(0, -20),
        heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
        disableDepthTestDistance: Number.POSITIVE_INFINITY
      }
    });
    samplingPoints.push(point);
  });
};

const updateCameraInfo = () => {
  if (!viewer) return;

  const camera = viewer.camera;
  const position = camera.position;
  const cartographic = Cesium.Cartographic.fromCartesian(position);

  cameraInfo.value = {
    lon: Cesium.Math.toDegrees(cartographic.longitude).toFixed(4),
    lat: Cesium.Math.toDegrees(cartographic.latitude).toFixed(4),
    height: Math.round(cartographic.height).toLocaleString()
  };
};

const resetView = () => {
  if (viewMode.value === '2d') {
    render2D();
  } else {
    if (viewer) {
      viewer.camera.flyTo({
        destination: Cesium.Cartesian3.fromDegrees(CHANGSHA_LON, CHANGSHA_LAT, 15000),
        orientation: {
          heading: Cesium.Math.toRadians(0),
          pitch: Cesium.Math.toRadians(-45),
          roll: 0
        },
        duration: 1.5
      });
    }
  }
};

const zoomIn = () => {
  if (viewMode.value === '2d') {
    render2D();
  } else {
    if (viewer) {
      viewer.camera.zoomIn(viewer.camera.positionCartographic.height * 0.2);
    }
  }
};

const zoomOut = () => {
  if (viewMode.value === '2d') {
    render2D();
  } else {
    if (viewer) {
      viewer.camera.zoomOut(viewer.camera.positionCartographic.height * 0.2);
    }
  }
};

const exportScene = () => {
  if (viewMode.value === '2d') {
    const canvas = cadCanvas.value;
    if (!canvas) return;
    const link = document.createElement('a');
    link.download = 'gis-2d-' + Date.now() + '.png';
    link.href = canvas.toDataURL('image/png');
    link.click();
  } else {
    if (!viewer) return;
    viewer.render();
    const canvas = viewer.canvas;
    const link = document.createElement('a');
    link.download = 'cesium-3d-' + Date.now() + '.png';
    link.href = canvas.toDataURL('image/png');
    link.click();
  }
};

const toggleFullscreen = () => {
  const container = containerRef.value;
  if (!document.fullscreenElement) {
    container?.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
};

watch(viewMode, async (val) => {
  await nextTick();
  if (val === '2d') {
    render2D();
  } else {
    if (!viewer) {
      await nextTick();
      initCesium();
    }
  }
});

onMounted(async () => {
  await nextTick();
  if (viewMode.value === '3d') {
    initCesium();
  } else {
    render2D();
  }
  window.addEventListener('resize', handleResize);
});

const handleResize = () => {
  if (viewMode.value === '2d') {
    render2D();
  }
};

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (viewer) {
    viewer.destroy();
    viewer = null;
  }
});
</script>

<style scoped>
.cad-page {
  padding: var(--s-5);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-4);
  border-bottom: var(--line);
}

.page-title {
  font-size: 28px;
  font-weight: 200;
}

.page-meta {
  font-size: 11px;
  color: var(--signal-dim);
  margin-top: 4px;
}

.page-actions {
  display: flex;
  gap: var(--s-3);
  align-items: center;
}

.view-toggle {
  display: flex;
  border: var(--line);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.view-btn {
  padding: 6px 16px;
  background: transparent;
  border: none;
  color: var(--signal-dim);
  font-family: var(--font-mono);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.view-btn:hover {
  color: var(--signal);
  background: var(--ink-700);
}

.view-btn.active {
  background: var(--sand-500);
  color: var(--ink-900);
  font-weight: 600;
}

.cad-container {
  flex: 1;
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.cad-toolbar {
  display: flex;
  gap: var(--s-2);
  padding: var(--s-3) var(--s-4);
  border-bottom: var(--line);
  align-items: center;
  background: var(--ink-800);
  z-index: 10;
}

.tool-btn {
  width: 36px;
  height: 36px;
  background: var(--ink-700);
  border: var(--line-soft);
  color: var(--signal-dim);
  font-size: 16px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.tool-btn:hover {
  background: var(--ink-600);
  color: var(--signal);
  border-color: var(--ink-500);
}

.tool-btn.active {
  background: var(--sand-500);
  color: var(--ink-900);
  border-color: var(--sand-500);
}

.tool-divider {
  width: 1px;
  height: 24px;
  background: var(--ink-600);
  margin: 0 var(--s-1);
}

.cad-info {
  margin-left: auto;
  font-size: 11px;
  color: var(--signal-dim);
  letter-spacing: 0.05em;
}

.cad-layers {
  display: flex;
  gap: var(--s-2);
  padding: var(--s-2) var(--s-4);
  border-bottom: var(--line-soft);
  background: rgba(43, 52, 71, 0.5);
  flex-wrap: wrap;
}

.layer-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  font-size: 12px;
  color: var(--signal-dim);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
}

.layer-tag:hover {
  color: var(--signal);
  border-color: var(--ink-500);
}

.layer-tag.active {
  color: var(--sand-500);
  border-color: var(--sand-500);
  background: rgba(212, 168, 83, 0.08);
}

.layer-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.cad-canvas {
  flex: 1;
  width: 100%;
  display: block;
}

.cesium-container {
  flex: 1;
  width: 100%;
  position: relative;
}

:deep(.cesium-viewer-bottom) {
  display: none !important;
}

:deep(.cesium-viewer-toolbar) {
  display: none !important;
}

:deep(.cesium-credit-lightbox) {
  display: none !important;
}

.info-panel {
  position: absolute;
  top: 100px;
  right: var(--s-4);
  width: 200px;
  background: rgba(31, 38, 51, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: var(--line-soft);
  border-radius: var(--radius-md);
  padding: var(--s-4);
  z-index: 10;
}

.info-title {
  font-size: 11px;
  color: var(--sand-500);
  letter-spacing: 0.15em;
  margin-bottom: var(--s-3);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid rgba(61, 72, 99, 0.3);
}

.info-label {
  font-size: 12px;
  color: var(--signal-dim);
}

.info-value {
  font-size: 13px;
  color: var(--signal);
  font-weight: 500;
  font-family: var(--font-mono);
}

.info-divider {
  height: 1px;
  background: var(--ink-600);
  margin: var(--s-3) 0;
}

.tip-item {
  font-size: 12px;
  color: var(--signal-dim);
  padding: 4px 0;
  line-height: 1.6;
}

.legend-panel {
  position: absolute;
  bottom: var(--s-4);
  left: var(--s-4);
  width: 160px;
  background: rgba(31, 38, 51, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: var(--line-soft);
  border-radius: var(--radius-md);
  padding: var(--s-3) var(--s-4);
  z-index: 10;
}

.legend-title {
  font-size: 11px;
  color: var(--sand-500);
  letter-spacing: 0.15em;
  margin-bottom: var(--s-3);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 0;
  font-size: 12px;
  color: var(--signal-dim);
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.legend-text {
  flex: 1;
}
</style>
