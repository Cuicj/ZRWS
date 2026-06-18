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

    <!-- 审批弹窗 -->
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
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
</style>