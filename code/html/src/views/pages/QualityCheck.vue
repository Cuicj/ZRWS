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
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
</style>