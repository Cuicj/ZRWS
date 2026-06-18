<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">bpmn-js 流程设计器</h1>
        <div class="page-meta mono">WORKFLOW DESIGN · 拖拽绘制 → Flowable 部署</div>
      </div>
      <div class="page-actions">
        <button class="btn-primary btn-sm" @click="saveToFlowable">💾 保存到 Flowable</button>
        <button class="btn-ghost btn-sm" @click="downloadXML">📥 下载 XML</button>
      </div>
    </div>

    <Panel class="designer-panel">
      <div class="designer-toolbar">
        <input v-model="processName" placeholder="流程名称" style="width:200px" />
        <input v-model="processKey" placeholder="唯一标识 (如: STANDARD)" style="width:160px" />
        <span class="toolbar-hint mono">拖拽绘制 → 右侧属性面板配置 → 保存部署</span>
      </div>
      <div class="designer-main">
        <div ref="canvasRef" class="designer-canvas"></div>
        <div ref="propertiesRef" class="designer-properties"></div>
      </div>
      <div class="designer-status mono" id="designerStatus">就绪</div>
    </Panel>

    <Panel title="已部署流程">
      <table>
        <thead><tr><th>Key</th><th>名称</th><th>版本</th><th>部署时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in deployedProcesses" :key="p.key">
            <td class="mono">{{ p.key }}</td>
            <td>{{ p.name }}</td>
            <td>v{{ p.version }}</td>
            <td class="mono">{{ p.deployTime }}</td>
            <td><button class="btn-ghost btn-sm">查看</button></td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';

// bpmn-js 相关引用
const canvasRef = ref(null);
const propertiesRef = ref(null);
const processName = ref('');
const processKey = ref('');

const deployedProcesses = ref([
  { key: 'STANDARD', name: '标准审批', version: 1, deployTime: '2026-06-17' },
  { key: 'MATERIAL', name: '物资申领', version: 1, deployTime: '2026-06-17' },
  { key: 'DRONE_FLIGHT', name: '无人机外出报备', version: 1, deployTime: '2026-06-17' },
  { key: 'EMERGENCY', name: '应急快速通道', version: 1, deployTime: '2026-06-17' }
]);

// 初始化 bpmn-js（实际需要引入 bpmn-js 库）
const initBpmn = () => {
  // 这里是占位逻辑，实际需要：
  // import BpmnModeler from 'bpmn-js/lib/Modeler';
  // const modeler = new BpmnModeler({ container: canvasRef.value, propertiesPanel: { parent: propertiesRef.value } });
  console.log('bpmn-js 初始化占位 - 需引入 bpmn-js 库');
};

const saveToFlowable = () => {
  if (!processKey.value) { alert('请填写流程唯一标识'); return; }
  // 实际调用 Flowable REST API 部署
  alert('流程已保存到 Flowable 引擎（演示）');
};

const downloadXML = () => {
  // 实际从 modeler.saveXML() 获取
  const xml = '<?xml version="1.0"?><definitions>...</definitions>';
  const blob = new Blob([xml], { type: 'text/xml' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = processKey.value + '.bpmn20.xml';
  link.click();
};

onMounted(() => initBpmn());
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.page-actions { display: flex; gap: var(--s-2); }
.designer-panel { padding: 0; }
.designer-toolbar { display: flex; gap: var(--s-3); padding: var(--s-3); border-bottom: var(--line); align-items: center; }
.toolbar-hint { margin-left: auto; font-size: 11px; color: var(--signal-dim); }
.designer-main { display: grid; grid-template-columns: 1fr 280px; height: 500px; }
.designer-canvas { background: var(--ink-900); border-right: var(--line); }
.designer-properties { background: var(--ink-800); padding: var(--s-3); overflow-y: auto; }
.designer-status { padding: var(--s-2) var(--s-3); border-top: var(--line); font-size: 11px; color: var(--signal-dim); }
</style>