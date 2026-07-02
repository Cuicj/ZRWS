<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">组织管理</h1>
        <div class="page-meta mono">ORGANIZATION MANAGEMENT · {{ orgs.length }} 个组织</div>
      </div>
    </div>

    <div class="stat-row">
      <StatCard label="组织总数" :value="orgs.length" icon="◎" variant="accent" />
      <StatCard label="启用组织" :value="enabledCount" icon="✓" variant="ok" />
      <StatCard label="企业版" :value="enterpriseCount" icon="◉" variant="warn" />
      <StatCard label="个人版" :value="personalCount" icon="○" variant="accent" />
    </div>

    <Panel title="组织列表">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="!orgs.length" class="loading-tip">暂无组织数据</div>
      <table v-else>
        <thead><tr><th>ID</th><th>组织名称</th><th>类型</th><th>订阅级别</th><th>最大成员数</th><th>到期时间</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="o in orgs" :key="o.id">
            <td class="mono">{{ o.id }}</td>
            <td>{{ o.orgName }}</td>
            <td><span class="role-badge" :class="o.orgType === 'ENTERPRISE' ? 'role-admin' : 'role-user'">{{ o.orgType === 'ENTERPRISE' ? '企业' : '个人' }}</span></td>
            <td class="mono">{{ o.subscriptionLevel || '-' }}</td>
            <td class="mono">{{ o.maxMembers === null || o.maxMembers === undefined ? '不限' : o.maxMembers }}</td>
            <td class="mono">{{ formatTime(o.subscriptionExpireTime) }}</td>
            <td><span class="status-badge" :class="o.status === 'ACTIVE' ? 'status-ok' : 'status-err'">{{ o.status === 'ACTIVE' ? '正常' : '禁用' }}</span></td>
            <td class="mono">{{ formatTime(o.createdTime) }}</td>
            <td>
              <button class="btn-ghost btn-sm" @click="openEdit(o)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <!-- 编辑组织对话框 -->
    <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3>编辑组织 - {{ form.orgName }}</h3>
          <button class="modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>组织名称<span class="req">*</span></label>
            <input v-model="form.orgName" placeholder="组织名称" />
          </div>
          <div class="form-row">
            <label>组织类型</label>
            <select v-model="form.orgType">
              <option value="PERSONAL">个人</option>
              <option value="ENTERPRISE">企业</option>
            </select>
          </div>
          <div class="form-row">
            <label>订阅级别</label>
            <select v-model="form.subscriptionLevel">
              <option value="FREE">免费版</option>
              <option value="PRO">专业版</option>
              <option value="ENTERPRISE">企业版</option>
            </select>
          </div>
          <div class="form-row">
            <label>最大成员数</label>
            <input v-model.number="form.maxMembers" type="number" placeholder="留空表示不限" />
          </div>
          <div class="form-row">
            <label>状态</label>
            <select v-model="form.status">
              <option value="ACTIVE">正常</option>
              <option value="DISABLED">禁用</option>
            </select>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn-ghost" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '确定' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { ElMessage } from 'element-plus';
import { listOrgs, updateOrg } from '@/api/management';

const orgs = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const submitting = ref(false);
const form = ref({ orgName: '', orgType: 'ENTERPRISE', subscriptionLevel: 'FREE', maxMembers: null, status: 'ACTIVE' });
const editingId = ref(null);

const enabledCount = computed(() => orgs.value.filter(o => o.status === 'ACTIVE').length);
const enterpriseCount = computed(() => orgs.value.filter(o => o.orgType === 'ENTERPRISE').length);
const personalCount = computed(() => orgs.value.filter(o => o.orgType === 'PERSONAL').length);

function formatTime(t) {
  if (!t) return '-';
  return String(t).replace('T', ' ').substring(0, 16);
}

async function loadOrgs() {
  loading.value = true;
  try {
    const res = await listOrgs();
    orgs.value = res.data?.list || [];
  } catch (e) {
    ElMessage.error('加载组织列表失败');
    orgs.value = [];
  } finally {
    loading.value = false;
  }
}

function openEdit(o) {
  editingId.value = o.id;
  form.value = {
    orgName: o.orgName || '',
    orgType: o.orgType || 'ENTERPRISE',
    subscriptionLevel: o.subscriptionLevel || 'FREE',
    maxMembers: o.maxMembers,
    status: o.status || 'ACTIVE'
  };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!form.value.orgName) {
    ElMessage.warning('请填写组织名称');
    return;
  }
  submitting.value = true;
  try {
    await updateOrg(editingId.value, {
      orgName: form.value.orgName,
      orgType: form.value.orgType,
      subscriptionLevel: form.value.subscriptionLevel,
      maxMembers: form.value.maxMembers === '' ? null : form.value.maxMembers,
      status: form.value.status
    });
    ElMessage.success('组织更新成功');
    dialogVisible.value = false;
    loadOrgs();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadOrgs();
});
</script>

<style scoped>
.page-container {
  padding: 32px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}
.page-title {
  font-size: 28px;
  font-weight: 300;
  color: #5D4E37;
  margin: 0;
}
.page-meta {
  font-size: 12px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 1px;
}
.btn-primary {
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #C9A86C 0%, #D4B87A 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.4);
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-ghost {
  padding: 6px 14px;
  font-size: 12px;
  color: #8B7355;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-ghost:hover:not(:disabled) {
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.08);
}
.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.loading-tip {
  padding: 40px 0;
  text-align: center;
  color: #8B7355;
  font-size: 14px;
}
.role-badge {
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.role-admin {
  background: rgba(201, 168, 108, 0.18);
  color: #C9A86C;
}
.role-user {
  background: rgba(139, 115, 85, 0.12);
  color: #8B7355;
}
.status-badge {
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.status-ok {
  background: rgba(106, 153, 106, 0.15);
  color: #6A996A;
}
.status-err {
  background: rgba(196, 122, 110, 0.15);
  color: #C47A6E;
}
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}
thead th {
  text-align: left;
  padding: 14px 16px;
  font-weight: 500;
  font-size: 13px;
  color: #8B7355;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-bottom: 1px solid #E8E2D9;
}
thead th:first-child { border-top-left-radius: 12px; }
thead th:last-child { border-top-right-radius: 12px; }
tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  font-size: 14px;
}
tbody tr {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
tbody tr:hover {
  background: rgba(201, 168, 108, 0.06);
}
tbody tr:last-child td { border-bottom: none; }
.mono {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 13px;
  color: #8B7355;
}
.display {
  font-weight: 300;
  letter-spacing: -0.5px;
}
/* 对话框 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(93, 78, 55, 0.35);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-card {
  width: 480px;
  max-width: 92vw;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(139, 115, 85, 0.25);
  overflow: hidden;
  animation: modalIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(20px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.modal-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
}
.modal-head h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 500;
  color: #5D4E37;
}
.modal-close {
  background: transparent;
  border: none;
  font-size: 22px;
  color: #8B7355;
  cursor: pointer;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.modal-close:hover {
  background: rgba(201, 168, 108, 0.15);
  color: #5D4E37;
}
.modal-body {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}
.form-row {
  margin-bottom: 16px;
}
.form-row label {
  display: block;
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 6px;
  font-weight: 500;
}
.req {
  color: #C47A6E;
  margin-left: 2px;
}
.form-row input, .form-row select {
  width: 100%;
  padding: 9px 14px;
  font-size: 14px;
  color: #5D4E37;
  background: #FAFAF8;
  border: 1px solid #E8E2D9;
  border-radius: 10px;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}
.form-row input:focus, .form-row select:focus {
  border-color: #C9A86C;
  box-shadow: 0 2px 12px rgba(201, 168, 108, 0.25);
  background: #FEFBF6;
}
.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
}
</style>
