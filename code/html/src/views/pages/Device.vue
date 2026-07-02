<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">设备管理</h1>
        <div class="page-meta mono">DEVICE · {{ overview.total }} 台设备</div>
      </div>
      <button class="btn btn-primary btn-sm">+ 添加设备</button>
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
            <td><button class="btn btn-ghost btn-sm">校准</button></td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getDeviceList } from '@/api/device';

const overview = ref({ online: 0, offline: 0, total: 0, alarm: 0 });
const nodeStats = ref({ cpu: 0, memory: '0/0G' });

const drones = ref([]);
const rtkStations = ref([]);
const computeNodes = ref([]);
const sensors = ref([]);
const loading = ref(true);

const getStatusClass = (status) => ({
  ONLINE: 'status-ok',
  OFFLINE: 'status-dim',
  MAINTENANCE: 'status-warn',
  BUSY: 'status-warn'
}[status] || 'status-dim');

const getStatusText = (status) => ({
  ONLINE: '在线',
  OFFLINE: '离线',
  MAINTENANCE: '维护中',
  BUSY: '繁忙',
  IDLE: '空闲',
  CONNECTED: '已连接'
}[status] || status || '未知');

const loadData = async () => {
  try {
    loading.value = true;
    const res = await getDeviceList();
    if (res && res.data?.list) {
      const devices = res.data.list;
      overview.value.total = devices.length;
      overview.value.online = devices.filter(d => d.status === 'ONLINE').length;
      overview.value.offline = devices.filter(d => d.status === 'OFFLINE').length;
      
      drones.value = devices.filter(d => d.deviceType === 'UAV').map(d => ({
        id: d.deviceCode || d.id,
        model: d.deviceName || d.model || '-',
        statusText: getStatusText(d.status),
        statusClass: getStatusClass(d.status),
        battery: d.batteryLevel || 0
      }));
      
      rtkStations.value = devices.filter(d => d.deviceType === 'GNSS').map(d => ({
        id: d.deviceCode || d.id,
        model: d.deviceName || d.model || '-',
        statusText: getStatusText(d.status),
        statusClass: getStatusClass(d.status),
        satellites: '-'
      }));
      
      computeNodes.value = devices.filter(d => d.deviceType === 'COMPUTE').map(d => ({
        id: d.deviceCode || d.id,
        statusText: getStatusText(d.status),
        statusClass: getStatusClass(d.status)
      }));
      
      sensors.value = devices.filter(d => d.deviceType === 'SENSOR').map(d => ({
        id: d.deviceCode || d.id,
        type: d.manufacturer || '-',
        model: d.deviceName || d.model || '-',
        calibrateTime: d.lastMaintenance || '-',
        statusText: getStatusText(d.status),
        statusClass: getStatusClass(d.status)
      }));
    }
  } catch (e) {
    console.warn('加载设备列表失败:', e.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
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
  border-bottom: 1px solid #E8E2D9;
}
.page-title { 
  font-size: 32px; 
  font-weight: 300; 
  color: #5D4E37;
  letter-spacing: -0.5px;
}
.page-meta { 
  font-size: 12px; 
  color: #8B7355; 
  margin-top: 6px;
  letter-spacing: 0.5px;
  opacity: 0.8;
}

.stat-row { 
  display: grid; 
  grid-template-columns: repeat(4, 1fr); 
  gap: 20px; 
  margin-bottom: 24px;
}

.device-row { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 24px; 
  margin-bottom: 24px;
}

table { 
  width: 100%; 
  border-collapse: separate; 
  border-spacing: 0;
  font-size: 13px;
  color: #5D4E37;
}
th, td { 
  padding: 14px 16px; 
  text-align: left; 
  border-bottom: 1px solid #E8E2D9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
th { 
  color: #8B7355; 
  font-weight: 600; 
  font-size: 11px; 
  text-transform: uppercase;
  letter-spacing: 0.08em;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-bottom: 1px solid #E8E2D9;
}
th:first-child {
  border-top-left-radius: 12px;
}
th:last-child {
  border-top-right-radius: 12px;
}
tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}
tr:last-child td {
  border-bottom: none;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.03em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.status-ok { 
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%); 
  color: #2E7D32;
  border: 1px solid #A5D6A7;
  box-shadow: 0 2px 6px rgba(46, 125, 50, 0.1);
}
.status-dim { 
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE8E0 100%); 
  color: #8B7355;
  border: 1px solid #D4C4B0;
  box-shadow: 0 2px 6px rgba(139, 115, 85, 0.08);
  opacity: 0.7;
}
.status-warn { 
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%); 
  color: #E65100;
  border: 1px solid #FFCC80;
  box-shadow: 0 2px 6px rgba(245, 124, 0, 0.1);
}
.status-danger { 
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%); 
  color: #C62828;
  border: 1px solid #EF9A9A;
  box-shadow: 0 2px 6px rgba(198, 40, 40, 0.1);
}
.status-accent { 
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%); 
  color: #E65100;
  border: 1px solid #FFCC80;
  box-shadow: 0 2px 6px rgba(230, 81, 0, 0.1);
}

.mono {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.display {
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
}
</style>
