<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">GPS 航迹</h1>
        <div class="page-meta mono">GPS TRACK · RTK FIXED</div>
      </div>
    </div>

    <div class="gps-row">
      <Panel title="实时坐标" meta="RTK">
        <div class="coord-grid">
          <div class="coord-item">
            <div class="label">经度</div>
            <div class="value mono">{{ gps.lng.toFixed(8) }}°</div>
          </div>
          <div class="coord-item">
            <div class="label">纬度</div>
            <div class="value mono">{{ gps.lat.toFixed(8) }}°</div>
          </div>
          <div class="coord-item">
            <div class="label">高程</div>
            <div class="value mono">{{ gps.alt }} m</div>
          </div>
          <div class="coord-item">
            <div class="label">平面精度</div>
            <div class="value mono">±{{ gps.hAcc }} cm</div>
          </div>
          <div class="coord-item">
            <div class="label">高程精度</div>
            <div class="value mono">±{{ gps.vAcc }} cm</div>
          </div>
          <div class="coord-item">
            <div class="label">卫星数</div>
            <div class="value mono">{{ gps.satellites }}</div>
          </div>
        </div>
      </Panel>

      <Panel title="航迹点列表">
        <table>
          <thead><tr><th>#</th><th>时间</th><th>纬度</th><th>经度</th><th>高程</th></tr></thead>
          <tbody>
            <tr v-for="pt in trackPoints" :key="pt.seq">
              <td class="mono">{{ pt.seq }}</td>
              <td class="mono">{{ pt.time }}</td>
              <td class="mono">{{ pt.lat.toFixed(6) }}</td>
              <td class="mono">{{ pt.lng.toFixed(6) }}</td>
              <td>{{ pt.alt }} m</td>
            </tr>
          </tbody>
        </table>
      </Panel>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';

const gps = ref({ lng: 112.835210, lat: 28.456720, alt: 120.5, hAcc: 1.2, vAcc: 2.1, satellites: 18 });
const trackPoints = ref([
  { seq: 1, time: '08:30:01', lat: 28.456720, lng: 112.835210, alt: 120.0 },
  { seq: 2, time: '08:30:02', lat: 28.456735, lng: 112.835325, alt: 120.2 },
  { seq: 3, time: '08:30:03', lat: 28.456750, lng: 112.835440, alt: 119.8 },
  { seq: 4, time: '08:30:04', lat: 28.456768, lng: 112.835560, alt: 120.1 },
  { seq: 5, time: '08:30:05', lat: 28.456785, lng: 112.835680, alt: 120.0 }
]);
</script>

<style scoped>
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}
.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: 1px solid #E8E2D9;
}
.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
  font-family: var(--font-display);
}
.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
  letter-spacing: 0.1em;
  font-weight: 500;
}
.gps-row {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--s-3);
}
.coord-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--s-3);
}
.coord-item {
  padding: var(--s-3);
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.06);
}
.coord-item:hover {
  border-color: #C9A86C;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.1);
  transform: translateY(-2px);
}
.coord-item .label {
  font-size: 10px;
  color: #8B7355;
  letter-spacing: 0.1em;
  margin-bottom: 6px;
  text-transform: uppercase;
  font-weight: 600;
}
.coord-item .value {
  font-size: 14px;
  color: #5D4E37;
  font-weight: 500;
}
</style>
