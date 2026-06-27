<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">质量校验</h1>
      <div class="page-meta mono">QUALITY CHECK · 合格率 {{ stats.passRate }}%</div>
    </div>

    <div class="stat-row">
      <StatCard label="合格" :value="stats.passCount" icon="✓" variant="ok" />
      <StatCard label="不合格" :value="stats.failCount" icon="!" variant="danger" />
      <StatCard label="合格率" :value="stats.passRate" unit="%" icon="%" variant="accent" />
      <StatCard label="待检" :value="stats.pendingCount" icon="○" />
    </div>

    <Panel title="质检记录">
      <table>
        <thead><tr><th>ID</th><th>任务</th><th>检查类型</th><th>结果</th><th>评分</th><th>问题</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="r in records" :key="r.id">
            <td class="mono">{{ r.id }}</td>
            <td class="mono">{{ r.taskId }}</td>
            <td>{{ r.checkType }}</td>
            <td><span class="status-badge" :class="r.result === 'pass' ? 'status-ok' : 'status-err'">{{ r.result === 'pass' ? '合格' : '不合格' }}</span></td>
            <td>{{ r.score }}</td>
            <td>{{ r.issues }}</td>
            <td class="mono">{{ r.checkTime }}</td>
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

const stats = ref({ passCount: 48, failCount: 3, passRate: 94.1, pendingCount: 5 });
const records = ref([
  { id: 'QC-001', taskId: 'ZRS-2026-0617-001', checkType: '影像质量', result: 'pass', score: 95, issues: '无', checkTime: '2026-06-17 11:00' },
  { id: 'QC-002', taskId: 'ZRS-2026-0616-003', checkType: '点云精度', result: 'pass', score: 92, issues: '边缘精度略低', checkTime: '2026-06-16 18:30' },
  { id: 'QC-003', taskId: 'ZRS-2026-0615-001', checkType: 'GPS精度', result: 'fail', score: 68, issues: 'RTK信号中断', checkTime: '2026-06-15 17:00' }
]);
</script>

<style scoped>
.page-container {
  padding: 32px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-head {
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
  margin: 0;
}

.page-meta {
  font-size: 12px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
}

thead th {
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  color: #5D4E37;
  font-weight: 600;
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 13px;
  letter-spacing: 0.3px;
}

thead th:first-child {
  border-top-left-radius: 12px;
}

thead th:last-child {
  border-top-right-radius: 12px;
}

tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tbody tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #8B7355;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-ok {
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.15) 0%, rgba(129, 199, 132, 0.1) 100%);
  color: #43A047;
  border: 1px solid rgba(102, 187, 106, 0.3);
}

.status-err {
  background: linear-gradient(135deg, rgba(239, 83, 80, 0.15) 0%, rgba(229, 115, 115, 0.1) 100%);
  color: #E53935;
  border: 1px solid rgba(239, 83, 80, 0.3);
}
</style>
