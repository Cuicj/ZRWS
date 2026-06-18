<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">设备管理</h1>
        <div class="page-meta mono">DEVICE · {{ overview.total }} 台设备</div>
      </div>
      <button class="btn-primary btn-sm">+ 添加设备</button>
    </div>

    <div class="stat-row">
      <StatCard label="在线" :value="overview.online" icon="◉" variant="ok" />
      <StatCard label="离线" :value="overview.offline" icon="○" variant="danger" />
      <StatCard label="总数" :value="overview.total" icon="□" variant="accent" />
      <StatCard label="告警" :value="overview.alarm" icon="!" variant="warn" />
    </div>

    <div class="device-row">
      <Panel title="无人机">
        <table>
          <thead><tr><th>ID</th><th>型号</th><th>状态</th><th>电量</th></tr></thead>
          <tbody>
            <tr v-for="d in drones" :key="d.id">
              <td class="mono">{{ d.id }}</td>
              <td>{{ d.model }}</td>
              <td><span class="status-badge" :class="d.statusClass">{{ d.statusText }}</span></td>
              <td>{{ d.battery }}%</td>
            </tr>
          </tbody>
        </table>
      </Panel>

      <Panel title="RTK 基准站">
        <table>
          <thead><tr><th>ID</th><th>型号</th><th>状态</th><th>卫星</th></tr></thead>
          <tbody>
            <tr v-for="r in rtkStations" :key="r.id">
              <td class="mono">{{ r.id }}</td>
              <td>{{ r.model }}</td>
              <td><span class="status-badge" :class="r.statusClass">{{ r.statusText }}</span></td>
              <td>{{ r.satellites }}</td>
            </tr>
          </tbody>
        </table>
      </Panel>

      <Panel title="处理节点">
        <table>
          <thead><tr><th>ID</th><th>状态</th><th>CPU</th><th>内存</th></tr></thead>
          <tbody>
            <tr v-for="n in computeNodes" :key="n.id">
              <td class="mono">{{ n.id }}</td>
              <td><span class="status-badge" :class="n.statusClass">{{ n.statusText }}</span></td>
              <td>{{ nodeStats.cpu }}%</td>
              <td>{{ nodeStats.memory }}</td>
            </tr>
          </tbody>
        </table>
      </Panel>
    </div>

    <Panel title="传感器列表">
      <table>
        <thead><tr><th>ID</th><th>类型</th><th>型号</th><th>校准时间</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="s in sensors" :key="s.id">
            <td class="mono">{{ s.id }}</td>
            <td>{{ s.type }}</td>
            <td>{{ s.model }}</td>
            <td class="mono">{{ s.calibrateTime }}</td>
            <td><span class="status-badge" :class="s.statusClass">{{ s.statusText }}</span></td>
            <td><button class="btn-ghost btn-sm">校准</button></td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';

const overview = ref({ online: 5, offline: 1, total: 6, alarm: 1 });
const nodeStats = ref({ cpu: 62, memory: '8.2/16G' });

const drones = ref([
  { id: 'UAV-M350-003', model: 'DJI M350 RTK', statusText: '在线', statusClass: 'status-ok', battery: 78 },
  { id: 'UAV-M300-001', model: 'DJI M300 RTK', statusText: '离线', statusClass: 'status-dim', battery: 0 },
  { id: 'UAV-P4-002', model: 'DJI P4 RTK', statusText: '在线', statusClass: 'status-ok', battery: 92 }
]);

const rtkStations = ref([
  { id: 'RTK-BS-001', model: '华测 i93', statusText: '连接', statusClass: 'status-ok', satellites: 18 },
  { id: 'RTK-BS-002', model: '南方 S86', statusText: '连接', statusClass: 'status-ok', satellites: 16 }
]);

const computeNodes = ref([
  { id: 'NODE-01', statusText: '繁忙', statusClass: 'status-warn' },
  { id: 'NODE-02', statusText: '空闲', statusClass: 'status-ok' },
  { id: 'NODE-03', statusText: '空闲', statusClass: 'status-ok' }
]);

const sensors = ref([
  { id: 'SNS-LIDAR-01', type: 'LiDAR', model: 'Livox Avia', calibrateTime: '2026-05-01', statusText: '正常', statusClass: 'status-ok' },
  { id: 'SNS-CAM-01', type: '多光谱相机', model: 'Micasense MX', calibrateTime: '2026-06-10', statusText: '待校准', statusClass: 'status-warn' },
  { id: 'SNS-PROBE-01', type: '土壤探针', model: 'Decagon 5TE', calibrateTime: '2026-04-15', statusText: '正常', statusClass: 'status-ok' }
]);
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
.device-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
</style>