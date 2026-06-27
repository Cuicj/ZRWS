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
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}
.page-head {
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
.upload-zone {
  padding: var(--s-8);
  border: 2px dashed #D4C4B0;
  text-align: center;
  cursor: pointer;
  border-radius: 12px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.upload-zone:hover {
  border-color: #C9A86C;
  background: linear-gradient(135deg, #FFFBF5 0%, #FAF6EF 100%);
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.15);
}
.upload-icon {
  font-size: 48px;
  color: #B8A898;
  margin-bottom: var(--s-4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.upload-zone:hover .upload-icon {
  color: #C9A86C;
  transform: translateY(-4px);
}
.upload-text {
  font-size: 14px;
  color: #8B7355;
  margin-bottom: var(--s-4);
  font-weight: 500;
}
.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  padding: 10px 24px;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 8px;
  background: transparent;
  border: 1px solid #E8E2D9;
  color: #5D4E37;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-ghost:hover {
  border-color: #C9A86C;
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.2);
}
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  font-family: var(--font-body);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(139, 115, 85, 0.08);
}
.status-ok {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #A5D6A7;
}
.status-proc {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1565C0;
  border: 1px solid #90CAF9;
}
.status-err {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}
.status-warn {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #F57C00;
  border: 1px solid #FFCC80;
}
:deep(.el-progress-bar__outer) {
  background-color: #F0EBE3;
  border-radius: 2px;
}
:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #C9A86C 0%, #D4B87A 100%);
  border-radius: 2px;
}
:deep(.el-progress__text) {
  color: #8B7355;
  font-size: 12px;
  font-weight: 500;
}
</style>
