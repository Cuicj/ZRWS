<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">数据导入</h1>
      <div class="page-meta mono">DATA IMPORT · 拖拽上传</div>
    </div>

    <Panel title="上传区域">
      <div class="upload-zone" @dragover.prevent @drop.prevent="handleDrop">
        <div class="upload-icon">↧</div>
        <div class="upload-text">拖拽文件到此处，或点击选择</div>
        <input type="file" id="fileInput" multiple @change="handleSelect" hidden />
        <button class="btn-ghost" @click="triggerUpload">选择文件</button>
      </div>
    </Panel>

    <Panel title="处理队列">
      <table>
        <thead><tr><th>文件名</th><th>类型</th><th>大小</th><th>进度</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="t in tasks" :key="t.id">
            <td>{{ t.name }}</td>
            <td class="mono">{{ t.type }}</td>
            <td>{{ t.size }}</td>
            <td><el-progress :percentage="t.progress" :stroke-width="4" /></td>
            <td><span class="status-badge" :class="t.statusClass">{{ t.statusText }}</span></td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Panel from '@/components/common/Panel.vue';

const tasks = ref([
  { id: 1, name: '乔口镇_影像.zip', type: 'IMAGE', size: '2.4 GB', progress: 100, statusText: '完成', statusClass: 'status-ok' },
  { id: 2, name: '莲花镇_点云.las', type: 'LAS', size: '8.7 GB', progress: 78, statusText: '处理中', statusClass: 'status-proc' }
]);

const triggerUpload = () => document.getElementById('fileInput').click();
const handleSelect = (e) => { if (e.target.files.length) alert('已选择 ' + e.target.files.length + ' 个文件'); };
const handleDrop = (e) => { if (e.dataTransfer.files.length) alert('拖拽上传 ' + e.dataTransfer.files.length + ' 个文件'); };
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.upload-zone { padding: var(--s-8); border: 2px dashed var(--ink-600); text-align: center; cursor: pointer; }
.upload-zone:hover { border-color: var(--sand-500); }
.upload-icon { font-size: 48px; color: var(--signal-dim); margin-bottom: var(--s-4); }
.upload-text { font-size: 14px; color: var(--signal-dim); margin-bottom: var(--s-4); }
</style>