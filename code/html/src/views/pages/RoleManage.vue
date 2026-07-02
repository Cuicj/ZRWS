<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">角色管理</h1>
        <div class="page-meta mono">ROLE MANAGEMENT · {{ roles.length }} 个角色</div>
      </div>
      <button class="btn-primary" @click="openCreate">+ 添加角色</button>
    </div>

    <div class="stat-row">
      <StatCard label="角色总数" :value="roles.length" icon="◆" variant="accent" />
      <StatCard label="启用角色" :value="enabledCount" icon="✓" variant="ok" />
      <StatCard label="系统角色" :value="systemCount" icon="◉" variant="warn" />
      <StatCard label="自定义角色" :value="customCount" icon="⚿" variant="accent" />
    </div>

    <Panel title="角色列表">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="!roles.length" class="loading-tip">暂无角色数据</div>
      <table v-else>
        <thead><tr><th>ID</th><th>角色名称</th><th>角色编码</th><th>类型</th><th>数据范围</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in roles" :key="r.id">
            <td class="mono">{{ r.id }}</td>
            <td>{{ r.roleName }}</td>
            <td class="mono">{{ r.roleCode || '-' }}</td>
            <td><span class="role-badge" :class="r.roleType === 'SYSTEM' ? 'role-admin' : 'role-user'">{{ r.roleType === 'SYSTEM' ? '系统' : '自定义' }}</span></td>
            <td class="mono">{{ r.dataScope || '-' }}</td>
            <td><span class="status-badge" :class="r.status === 'ACTIVE' ? 'status-ok' : 'status-dim'">{{ r.status === 'ACTIVE' ? '启用' : '禁用' }}</span></td>
            <td class="mono">{{ formatTime(r.createdTime) }}</td>
            <td>
              <button class="btn-ghost btn-sm" @click="openEdit(r)">编辑</button>
              <button class="btn-ghost btn-sm btn-danger" @click="handleDelete(r)" style="margin-left:4px" :disabled="r.roleType === 'SYSTEM'">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <!-- 新建/编辑角色对话框 -->
    <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3>{{ editMode ? '编辑角色' : '新建角色' }}</h3>
          <button class="modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>角色名称<span class="req">*</span></label>
            <input v-model="form.roleName" placeholder="如：审批员" />
          </div>
          <div class="form-row">
            <label>角色编码<span class="req">*</span></label>
            <input v-model="form.roleCode" placeholder="如：approver" />
          </div>
          <div class="form-row">
            <label>数据范围</label>
            <select v-model="form.dataScope">
              <option value="ALL">全部数据</option>
              <option value="DEPT">本部门数据</option>
              <option value="SELF">仅本人数据</option>
            </select>
          </div>
          <div class="form-row">
            <label>状态</label>
            <select v-model="form.status">
              <option value="ACTIVE">启用</option>
              <option value="DISABLED">禁用</option>
            </select>
          </div>
          <div class="form-row">
            <label>权限配置（JSON）</label>
            <textarea v-model="form.permissions" rows="4" placeholder='如：["user:manage","role:manage"]' />
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { listRoles, createRole, updateRole, deleteRole } from '@/api/management';

const roles = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const editMode = ref(false);
const submitting = ref(false);
const form = ref({ roleName: '', roleCode: '', dataScope: 'ALL', status: 'ACTIVE', permissions: '' });
const editingId = ref(null);

const enabledCount = computed(() => roles.value.filter(r => r.status === 'ACTIVE').length);
const systemCount = computed(() => roles.value.filter(r => r.roleType === 'SYSTEM').length);
const customCount = computed(() => roles.value.filter(r => r.roleType !== 'SYSTEM').length);

function formatTime(t) {
  if (!t) return '-';
  return String(t).replace('T', ' ').substring(0, 16);
}

async function loadRoles() {
  loading.value = true;
  try {
    const res = await listRoles();
    roles.value = res.data?.list || [];
  } catch (e) {
    ElMessage.error('加载角色列表失败');
    roles.value = [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editMode.value = false;
  form.value = { roleName: '', roleCode: '', dataScope: 'ALL', status: 'ACTIVE', permissions: '' };
  editingId.value = null;
  dialogVisible.value = true;
}

function openEdit(r) {
  editMode.value = true;
  editingId.value = r.id;
  form.value = {
    roleName: r.roleName || '',
    roleCode: r.roleCode || '',
    dataScope: r.dataScope || 'ALL',
    status: r.status || 'ACTIVE',
    permissions: r.permissions || ''
  };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!form.value.roleName || !form.value.roleCode) {
    ElMessage.warning('请填写必填项');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      roleName: form.value.roleName,
      roleCode: form.value.roleCode,
      dataScope: form.value.dataScope,
      status: form.value.status,
      permissions: form.value.permissions || null
    };
    if (editMode.value) {
      await updateRole(editingId.value, payload);
      ElMessage.success('角色更新成功');
    } else {
      await createRole(payload);
      ElMessage.success('角色创建成功');
    }
    dialogVisible.value = false;
    loadRoles();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(r) {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${r.roleName}" 吗？`, '删除确认', { type: 'warning' });
    await deleteRole(r.id);
    ElMessage.success('删除成功');
    loadRoles();
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败');
  }
}

onMounted(() => {
  loadRoles();
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
.btn-ghost:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.btn-ghost.btn-danger:hover {
  color: #C47A6E;
  background: rgba(196, 122, 110, 0.1);
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
.status-dim {
  background: rgba(139, 115, 85, 0.12);
  color: #8B7355;
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
.form-row input, .form-row select, .form-row textarea {
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
  font-family: inherit;
}
.form-row textarea {
  font-family: 'SF Mono', 'Monaco', monospace;
  font-size: 13px;
  resize: vertical;
}
.form-row input:focus, .form-row select:focus, .form-row textarea:focus {
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
