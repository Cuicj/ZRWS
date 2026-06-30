<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">飞行控制</h1>
        <div class="page-meta mono">FLIGHT CONTROL · 实时遥测</div>
      </div>
      <div class="flight-status">
        <span class="status-badge" :class="flightStatusClass">{{ flightStatusText }}</span>
      </div>
    </div>

    <!-- 遥测面板 -->
    <div class="telemetry-row">
      <Panel title="实时遥测" meta="LIVE">
        <div class="telemetry-grid">
          <div class="telemetry-item">
            <div class="label">经度</div>
            <div class="value mono">{{ telemetry.lng.toFixed(6) }}°</div>
          </div>
          <div class="telemetry-item">
            <div class="label">纬度</div>
            <div class="value mono">{{ telemetry.lat.toFixed(6) }}°</div>
          </div>
          <div class="telemetry-item">
            <div class="label">高度</div>
            <div class="value mono">{{ telemetry.alt }} m</div>
          </div>
          <div class="telemetry-item">
            <div class="label">速度</div>
            <div class="value mono">{{ telemetry.speed }} m/s</div>
          </div>
          <div class="telemetry-item">
            <div class="label">电量</div>
            <div class="value mono">{{ telemetry.battery }}%</div>
          </div>
          <div class="telemetry-item">
            <div class="label">卫星</div>
            <div class="value mono">{{ telemetry.satellites }}</div>
          </div>
        </div>
      </Panel>

      <Panel title="采集进度" meta="TASK">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="progress-info mono">
          <span>{{ captured }} / {{ total }} 张</span>
          <span>{{ progress }}%</span>
        </div>
      </Panel>
    </div>

    <!-- 控制按钮 -->
    <div class="control-row">
      <button class="btn btn-primary" @click="startFlight" :disabled="flightStatus === 'flying'">
        ▶ 开始采集
      </button>
      <button class="btn btn-ghost" @click="pauseFlight" :disabled="flightStatus !== 'flying'">
        ⏸ 暂停
      </button>
      <button class="btn btn-ghost" @click="returnHome" :disabled="flightStatus === 'standby'">
        ↩ 返航
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';

const flightStatus = ref('standby');
const telemetry = ref({ lng: 112.83521, lat: 28.45672, alt: 120, speed: 8.2, battery: 78, satellites: 18 });
const captured = ref(774);
const total = ref(1247);

const progress = computed(() => Math.round(captured.value / total.value * 100));

const flightStatusClass = computed(() => ({
  standby: 'status-dim',
  flying: 'status-proc',
  paused: 'status-warn',
  returning: 'status-ok'
}[flightStatus.value]));

const flightStatusText = computed(() => ({
  standby: '待机',
  flying: '飞行中',
  paused: '已暂停',
  returning: '返航中'
}[flightStatus.value]));

const startFlight = () => { flightStatus.value = 'flying'; };
const pauseFlight = () => { flightStatus.value = 'paused'; };
const returnHome = () => { flightStatus.value = 'returning'; };
</script>

<style scoped>
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100%;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
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
.telemetry-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--s-3);
  margin-bottom: var(--s-4);
}
.telemetry-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--s-3);
}
.telemetry-item {
  padding: var(--s-3);
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.telemetry-item:hover {
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.18);
  transform: translateY(-2px);
}
.telemetry-item .label {
  font-size: 10px;
  color: #8B7355;
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}
.telemetry-item .value {
  font-size: 16px;
  color: #5D4E37;
}
.progress-bar {
  height: 8px;
  background: #E8E2D9;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: var(--s-2);
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A86C 0%, #D4B97E 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8B7355;
}
.control-row {
  display: flex;
  gap: var(--s-3);
}
.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.05em;
}
.status-dim {
  background: #F5F2ED;
  color: #8B7355;
  border: 1px solid #E8E2D9;
}
.status-proc {
  background: linear-gradient(135deg, #C9A86C 0%, #D4B97E 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}
.status-warn {
  background: #FFF3E0;
  color: #E6A23C;
  border: 1px solid #F5DAB0;
}
.status-ok {
  background: #F0F7EB;
  color: #67C23A;
  border: 1px solid #C8E6C9;
}
</style>
