<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">审批列表</h1>
        <div class="page-meta mono">APPROVAL LIST · Flowable 工作流</div>
      </div>
      <el-select v-model="statusFilter" placeholder="状态" size="small" clearable style="width:120px">
        <el-option label="全部" value="" />
        <el-option label="待审批" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
    </div>

    <Panel title="审批任务">
      <table>
        <thead><tr><th>审批单号</th><th>类型</th><th>标题</th><th>申请人</th><th>部门</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="a in filteredApprovals" :key="a.id">
            <td class="mono">{{ a.id }}</td>
            <td>{{ a.type }}</td>
            <td>{{ a.title }}</td>
            <td>{{ a.applicant }}</td>
            <td>{{ a.dept }}</td>
            <td><span class="status-badge" :class="getStatusClass(a.status)">{{ getStatusText(a.status) }}</span></td>
            <td class="mono">{{ a.createTime }}</td>
            <td>
              <button class="btn-ghost btn-sm" @click="openApproval(a)">审批</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <el-dialog v-model="showDialog" title="审批详情" width="600px">
      <el-descriptions :column="2" border v-if="currentApproval">
        <el-descriptions-item label="审批单号">{{ currentApproval.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentApproval.type }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApproval.applicant }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentApproval.dept }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ currentApproval.title }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:16px">
        <h4 style="font-size:14px;margin-bottom:8px">审批意见</h4>
        <el-input v-model="approvalComment" type="textarea" :rows="3" placeholder="请输入审批意见..." />
      </div>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="danger" @click="reject">驳回</el-button>
        <el-button type="primary" @click="approve">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';

const statusFilter = ref('');
const showDialog = ref(false);
const currentApproval = ref(null);
const approvalComment = ref('');

const approvals = ref([
  { id: 'APR-2026-001', type: '外出报备', title: '望城区乔口镇采集任务外出报备', applicant: '王工', dept: '技术部', status: 'pending', createTime: '2026-06-17 08:00' },
  { id: 'APR-2026-002', type: '物资申领', title: 'RTK 基准站电池采购申请', applicant: '李工', dept: '装备部', status: 'pending', createTime: '2026-06-16 14:30' },
  { id: 'APR-2026-003', type: '数据导出', title: '岳麓区莲花镇测图数据导出申请', applicant: '张工', dept: '技术部', status: 'approved', createTime: '2026-06-15 10:00' }
]);

const filteredApprovals = computed(() => {
  if (!statusFilter.value) return approvals.value;
  return approvals.value.filter(a => a.status === statusFilter.value);
});

const getStatusClass = (s) => ({ pending: 'status-proc', approved: 'status-ok', rejected: 'status-err' }[s] || 'status-dim');
const getStatusText = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s);

const openApproval = (a) => { currentApproval.value = a; approvalComment.value = ''; showDialog.value = true; };
const approve = () => { if (currentApproval.value) currentApproval.value.status = 'approved'; showDialog.value = false; };
const reject = () => { if (currentApproval.value) currentApproval.value.status = 'rejected'; showDialog.value = false; };
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
  align-items: flex-start;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 400;
  color: #5D4E37;
  letter-spacing: -0.02em;
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 500;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

th {
  text-align: left;
  padding: 14px 16px;
  font-size: 11px;
  color: #8B7355;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #F7F3ED 0%, #F0EBE3 100%);
}

th:first-child {
  border-top-left-radius: 12px;
}

th:last-child {
  border-top-right-radius: 12px;
}

td {
  padding: 16px;
  border-bottom: 1px solid #F0EBE3;
  color: #5D4E37;
  font-size: 13px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.status-badge {
  display: inline-block;
  padding: 5px 14px;
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
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #E65100;
  border: 1px solid #FFCC80;
}

.status-err {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.status-dim {
  background: linear-gradient(135deg, #F5F5F5 0%, #EEEEEE 100%);
  color: #757575;
  border: 1px solid #E0E0E0;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 500;
  background: transparent;
  border: 1px solid #E8E2D9;
  color: #5D4E37;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-ghost:hover {
  border-color: #C9A86C;
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.08);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.15);
}

.btn-sm {
  padding: 5px 12px;
  font-size: 11px;
}

.mono {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 12px;
}

.display {
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
}

:deep(.el-select__wrapper) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-select__wrapper:hover) {
  border-color: #C9A86C;
}

:deep(.el-select__wrapper.is-focused) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.2);
}

:deep(.el-select__placeholder) {
  color: #8B7355;
  opacity: 0.6;
}

:deep(.el-select__selected-item) {
  color: #5D4E37;
}

:deep(.el-dialog) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(139, 115, 85, 0.16);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #E8E2D9;
  padding: 20px 24px;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: #5D4E37;
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
  font-weight: 400;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  color: #5D4E37;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #E8E2D9;
  padding: 16px 24px;
}

:deep(.el-descriptions) {
  --el-descriptions-bg-color: transparent;
  --el-descriptions-text-color: #5D4E37;
  --el-descriptions-label-text-color: #8B7355;
  --el-descriptions-border-color: #E8E2D9;
}

:deep(.el-textarea__inner) {
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  color: #5D4E37;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-textarea__inner:hover) {
  border-color: #C9A86C;
}

:deep(.el-textarea__inner:focus) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.2);
}

:deep(.el-button--default) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  color: #5D4E37;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--default:hover) {
  border-color: #C9A86C;
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.05);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #C9A86C 0%, #D4B87A 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #B89855 0%, #C9A86C 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.4);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #EF5350 0%, #E57373 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(239, 83, 80, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #E53935 0%, #EF5350 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 83, 80, 0.4);
}
</style>
