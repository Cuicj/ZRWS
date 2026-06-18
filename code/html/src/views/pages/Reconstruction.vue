<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">3D 重建</h1>
      <div class="page-meta mono">RECONSTRUCTION · DSM/DEM/正射影像</div>
    </div>

    <div class="recon-row">
      <Panel title="重建参数">
        <el-form label-width="80px">
          <el-form-item label="分辨率"><el-input-number v-model="params.resolution" :min="0.05" :max="1" :step="0.05" /></el-form-item>
          <el-form-item label="输出类型">
            <el-select v-model="params.type">
              <el-option label="DSM" value="dsm" />
              <el-option label="DEM" value="dem" />
              <el-option label="正射影像" value="ortho" />
            </el-select>
          </el-form-item>
        </el-form>
      </Panel>

      <Panel title="重建进度" meta="PROCESSING">
        <div class="progress-info">
          <div class="step mono">{{ currentStep }}</div>
          <el-progress :percentage="progress" :stroke-width="8" />
          <div class="remaining mono">预计剩余 {{ remaining }} 分钟</div>
        </div>
      </Panel>
    </div>

    <Panel title="历史记录">
      <table>
        <thead><tr><th>ID</th><th>名称</th><th>类型</th><th>面积</th><th>分辨率</th><th>状态</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="h in history" :key="h.id">
            <td class="mono">{{ h.id }}</td>
            <td>{{ h.name }}</td>
            <td>{{ h.type }}</td>
            <td>{{ h.area }} 亩</td>
            <td>{{ h.resolution }} m</td>
            <td><span class="status-badge" :class="h.status === 'completed' ? 'status-ok' : 'status-proc'">{{ h.status === 'completed' ? '完成' : '处理中' }}</span></td>
            <td class="mono">{{ h.createTime }}</td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';

const params = ref({ resolution: 0.1, type: 'dsm' });
const progress = ref(45);
const currentStep = ref('正在生成正射影像...');
const remaining = ref(8);
const history = ref([
  { id: 'REC-001', name: '乔口镇 DSM', type: 'DSM', area: 860, resolution: 0.1, status: 'completed', createTime: '2026-06-17 10:30' },
  { id: 'REC-002', name: '莲花镇 DEM', type: 'DEM', area: 1250, resolution: 0.2, status: 'processing', createTime: '2026-06-17 09:00' }
]);
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.recon-row { display: grid; grid-template-columns: 1fr 2fr; gap: var(--s-3); margin-bottom: var(--s-4); }
.progress-info { padding: var(--s-4); }
.step { font-size: 14px; margin-bottom: var(--s-3); }
.remaining { font-size: 12px; color: var(--signal-dim); margin-top: var(--s-3); }
</style>