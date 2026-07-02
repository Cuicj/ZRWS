<template>
  <div class="dashboard-page">
    <!-- 页面标题 -->
    <div class="page-head">
      <div>
        <h1 class="page-title display">运行仪表盘</h1>
        <div class="page-meta mono">DASHBOARD · {{ currentDate }} · {{ currentUser.name }}</div>
      </div>
      <button class="btn btn-ghost btn-sm" @click="refreshData">
        <span>↻</span> 刷新
      </button>
    </div>

    <!-- 指标卡 -->
    <div class="stat-row">
      <StatCard
        label="采集任务"
        :value="stats.taskCount"
        :trend="stats.taskTrend"
        icon="◧"
        variant="accent"
      />
      <StatCard
        label="已完成"
        :value="stats.completedCount"
        :trend="stats.completedTrend"
        icon="✓"
        variant="ok"
      />
      <StatCard
        label="进行中"
        :value="stats.processingCount"
        :trend="stats.processingTrend"
        icon="➤"
        variant="warn"
      />
      <StatCard
        label="异常"
        :value="stats.abnormalCount"
        :trend="stats.abnormalTrend"
        icon="!"
        variant="danger"
      />
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <Panel title="采集趋势" meta="LAST 7 DAYS" class="chart-panel">
        <div ref="trendChartRef" class="chart-box"></div>
      </Panel>

      <Panel title="设备状态" meta="LIVE">
        <table>
          <tbody>
            <tr v-for="device in deviceList" :key="device.id">
              <td>{{ device.name }}</td>
              <td>
                <span class="status-badge" :class="device.statusClass">
                  {{ device.statusText }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </Panel>
    </div>

    <!-- 最近任务 -->
    <Panel title="最近任务">
      <template #actions>
        <router-link to="/app/mission-list" class="btn btn-ghost btn-sm">查看全部 →</router-link>
      </template>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>区域</th>
            <th>操作员</th>
            <th>覆盖</th>
            <th>日期</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in recentTasks" :key="task.id">
            <td class="mono">{{ task.id }}</td>
            <td>{{ task.area }}</td>
            <td>{{ task.operator }}</td>
            <td>{{ task.coverage }} 亩</td>
            <td class="mono">{{ task.date }}</td>
            <td>
              <span class="status-badge" :class="task.statusClass">
                {{ task.statusText }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <!-- 系统日志 -->
    <Panel title="系统日志" meta="RECENT">
      <div class="log-list">
        <div v-for="log in systemLogs" :key="log.time" class="log-item">
          <span class="log-time mono">{{ log.time }}</span>
          <span class="log-dot" :style="{ background: log.color }"></span>
          <span class="log-msg">{{ log.msg }}</span>
        </div>
      </div>
    </Panel>
  </div>
</template>

<script setup>
/**
 * Dashboard.vue - 运行仪表盘页面
 */
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import StatCard from '@/components/common/StatCard.vue';
import Panel from '@/components/common/Panel.vue';
import { getDashboardStats, getRecentTasks, getDeviceList } from '@/api/dashboard';

const currentDate = ref(new Date().toLocaleDateString('zh-CN'));

const currentUser = ref({ name: '王工' });

const stats = ref({
  taskCount: 0,
  taskTrend: 0,
  completedCount: 0,
  completedTrend: 0,
  processingCount: 0,
  processingTrend: 0,
  abnormalCount: 0,
  abnormalTrend: 0
});

const deviceList = ref([]);

const recentTasks = ref([]);

const systemLogs = ref([
  { time: '10:32', color: '#6B8E5A', msg: '系统运行正常' }
]);

const loading = ref(true);

const loadData = async () => {
  try {
    loading.value = true;
    const [statsRes, tasksRes, devicesRes] = await Promise.all([
      getDashboardStats().catch(() => ({ data: {} })),
      getRecentTasks({ limit: 5 }).catch(() => ({ list: [] })),
      getDeviceList().catch(() => ({ list: [] }))
    ]);
    
    if (statsRes.data) {
      stats.value = {
        taskCount: statsRes.data.taskCount || 0,
        taskTrend: statsRes.data.taskTrend || 0,
        completedCount: statsRes.data.completedCount || 0,
        completedTrend: statsRes.data.completedTrend || 0,
        processingCount: statsRes.data.processingCount || 0,
        processingTrend: statsRes.data.processingTrend || 0,
        abnormalCount: statsRes.data.abnormalCount || 0,
        abnormalTrend: statsRes.data.abnormalTrend || 0
      };
    }
    
    if (tasksRes.list && Array.isArray(tasksRes.list)) {
      recentTasks.value = tasksRes.list.slice(0, 5).map(t => ({
        id: t.missionCode || t.id,
        area: t.location || t.area || '-',
        operator: t.operator || '-',
        coverage: t.coverageArea || t.coverage || 0,
        date: t.flightTime || t.createTime || '-',
        statusText: getStatusText(t.status),
        statusClass: getStatusClass(t.status)
      }));
    }
    
    if (devicesRes.list && Array.isArray(devicesRes.list)) {
      deviceList.value = devicesRes.list.slice(0, 5).map(d => ({
        id: d.deviceId || d.id,
        name: d.deviceName || d.name,
        statusText: d.status || 'UNKNOWN',
        statusClass: getDeviceStatusClass(d.status)
      }));
    }
  } catch (e) {
    console.warn('Dashboard API加载失败，使用默认数据:', e.message);
  } finally {
    loading.value = false;
  }
};

const getStatusText = (status) => {
  const map = { 'COMPLETED': '完成', 'PROCESSING': '进行中', 'ABNORMAL': '异常', 'PENDING': '待执行' };
  return map[status] || status || '-';
};

const getStatusClass = (status) => {
  const map = { 'COMPLETED': 'status-ok', 'PROCESSING': 'status-proc', 'ABNORMAL': 'status-danger', 'PENDING': 'status-warn' };
  return map[status] || 'status-warn';
};

const getDeviceStatusClass = (status) => {
  const map = { 'ONLINE': 'status-ok', 'OFFLINE': 'status-danger', 'MAINTENANCE': 'status-warn', 'BUSY': 'status-proc' };
  return map[status] || 'status-warn';
};

// 图表引用
const trendChartRef = ref(null);
let trendChart = null;

// 初始化图表
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption({
      backgroundColor: 'transparent',
      grid: { top: 30, right: 30, bottom: 40, left: 60 },
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E8E2D9',
        borderWidth: 1,
        textStyle: { color: '#5D4E37', fontSize: 12 },
        borderRadius: 8
      },
      legend: {
        data: ['完成任务', '新建任务'],
        right: 10,
        top: 0,
        textStyle: { color: '#8B7355', fontSize: 12 }
      },
      xAxis: {
        type: 'category',
        data: ['06-11', '06-12', '06-13', '06-14', '06-15', '06-16', '06-17'],
        axisLine: { lineStyle: { color: '#E8E2D9' } },
        axisTick: { show: false },
        axisLabel: { color: '#8B7355', fontFamily: 'JetBrains Mono', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#F0EBE3', type: 'dashed' } },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#8B7355', fontFamily: 'JetBrains Mono', fontSize: 11 }
      },
      series: [
        {
          name: '完成任务',
          type: 'bar',
          data: [18, 22, 15, 28, 24, 31, 36],
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#D4B87A' },
                { offset: 1, color: '#C9A86C' }
              ]
            },
            borderRadius: [6, 6, 0, 0]
          },
          barWidth: '30%'
        },
        {
          name: '新建任务',
          type: 'line',
          data: [12, 15, 10, 20, 18, 24, 28],
          smooth: true,
          lineStyle: { 
            color: '#7FB3D5', 
            width: 2.5,
            type: 'solid'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(127, 179, 213, 0.3)' },
                { offset: 1, color: 'rgba(127, 179, 213, 0.02)' }
              ]
            }
          },
          symbol: 'circle',
          symbolSize: 8,
          itemStyle: { 
            color: '#fff', 
            borderColor: '#7FB3D5', 
            borderWidth: 2
          }
        }
      ]
    });
  }
};

// 刷新数据
const refreshData = () => {
  loadData();
  initCharts();
};

// 窗口 resize
const handleResize = () => {
  if (trendChart) trendChart.resize();
};

onMounted(() => {
  loadData();
  nextTick(() => initCharts());
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (trendChart) trendChart.dispose();
});
</script>

<style scoped>
.dashboard-page {
  padding: var(--s-5);
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: var(--line);
}

.page-title {
  font-size: 32px;
  font-weight: 200;
}

.page-meta {
  font-size: 11px;
  color: var(--signal-dim);
  margin-top: 4px;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-3);
  margin-bottom: var(--s-5);
}

.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--s-3);
  margin-bottom: var(--s-4);
}

.chart-panel {
  min-height: 400px;
}

.chart-box {
  height: 320px;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}

.log-item {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  padding: 8px 0;
  border-bottom: var(--line-soft);
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  font-size: 12px;
  color: var(--signal-dim);
}

.log-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.log-msg {
  font-size: 13px;
  color: var(--signal);
}
</style>