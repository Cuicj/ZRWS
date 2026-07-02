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
              <button class="btn btn-ghost btn-sm" @click="openApproval(a)">审批</button>
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
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import { getTodoList, getMyAppliedList, approveApproval, rejectApproval } from '@/api/approval.js';

const statusFilter = ref('');
const showDialog = ref(false);
const currentApproval = ref(null);
const approvalComment = ref('');
const loading = ref(false);

const approvals = ref([]);

const filteredApprovals = computed(() => {
  if (!statusFilter.value) return approvals.value;
  return approvals.value.filter(a => a.status === statusFilter.value);
});

const getStatusClass = (s) => ({ pending: 'status-proc', approved: 'status-ok', rejected: 'status-err' }[s] || 'status-dim');
const getStatusText = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s);

// 加载审批列表
const loadApprovals = async () => {
  loading.value = true;
  try {
    // 获取待办审批列表
    const todoRes = await getTodoList();
    // 获取我申请的审批列表
    const appliedRes = await getMyAppliedList();
    
    const todoList = todoRes.list || [];
    const appliedList = appliedRes.list || [];
    
    // 合并列表并适配数据结构
    approvals.value = [...todoList, ...appliedList].map(item => ({
      id: item.id || item.approvalId || item.processInstanceId,
      type: item.type || item.processName,
      title: item.title || item.businessTitle,
      applicant: item.applicant || item.applyUser,
      dept: item.dept || item.department,
      status: mapStatus(item.status),
      createTime: item.createTime || item.applyTime
    }));
  } catch (error) {
    console.error('加载审批列表失败:', error);
    ElMessage.error('加载审批列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 状态映射
const mapStatus = (status) => {
  const map = {
    0: 'pending',
    1: 'approved',
    2: 'rejected',
    'pending': 'pending',
    'approved': 'approved',
    'rejected': 'rejected',
    'PENDING': 'pending',
    'APPROVED': 'approved',
    'REJECTED': 'rejected'
  };
  return map[status] || 'pending';
};

const openApproval = (a) => { 
  currentApproval.value = a; 
  approvalComment.value = ''; 
  showDialog.value = true; 
};

const approve = async () => {
  if (!currentApproval.value) return;
  
  try {
    const res = await approveApproval(currentApproval.value.id, { comment: approvalComment.value });
    if (res.code === 0 || res.code === 200) {
      currentApproval.value.status = 'approved';
      ElMessage.success('审批通过');
      showDialog.value = false;
      loadApprovals(); // 重新加载列表
    } else {
      ElMessage.error(res.msg || '审批失败');
    }
  } catch (error) {
    console.error('审批失败:', error);
    ElMessage.error('审批失败，请稍后重试');
  }
};

const reject = async () => {
  if (!currentApproval.value) return;
  
  try {
    const res = await rejectApproval(currentApproval.value.id, { comment: approvalComment.value });
    if (res.code === 0 || res.code === 200) {
      currentApproval.value.status = 'rejected';
      ElMessage.success('审批已驳回');
      showDialog.value = false;
      loadApprovals(); // 重新加载列表
    } else {
      ElMessage.error(res.msg || '驳回失败');
    }
  } catch (error) {
    console.error('驳回失败:', error);
    ElMessage.error('驳回失败，请稍后重试');
  }
};

onMounted(() => {
  loadApprovals();
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
</style>
