<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">用户管理</h1>
        <div class="page-meta mono">USER MANAGEMENT · {{ total }} 位用户</div>
      </div>
      <button class="btn-primary" @click="openCreate">+ 添加用户</button>
    </div>

    <div class="stat-row">
      <StatCard label="总用户数" :value="total" icon="◉" variant="accent" />
      <StatCard label="启用用户" :value="activeCount" icon="●" variant="ok" />
      <StatCard label="禁用用户" :value="disabledCount" icon="○" variant="danger" />
      <StatCard label="当前页" :value="users.length" icon="↗" variant="warn" />
    </div>

    <Panel title="用户列表">
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/姓名/手机号" size="small" style="width:260px" clearable @keyup.enter="loadUsers" @clear="loadUsers" />
        <button class="btn-outline btn-sm" @click="loadUsers">搜索</button>
        <button class="btn-ghost btn-sm" @click="resetSearch">重置</button>
      </div>
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="!users.length" class="loading-tip">暂无用户数据</div>
      <table v-else>
        <thead><tr><th>ID</th><th>用户名</th><th>姓名</th><th>手机号</th><th>邮箱</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td class="mono">{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.realName || '-' }}</td>
            <td class="mono">{{ u.phone || '-' }}</td>
            <td class="mono">{{ u.email || '-' }}</td>
            <td><span class="status-badge" :class="u.status === 'ACTIVE' ? 'status-ok' : 'status-err'">{{ u.status === 'ACTIVE' ? '正常' : '禁用' }}</span></td>
            <td class="mono">{{ formatTime(u.createdTime) }}</td>
            <td>
              <button class="btn-ghost btn-sm" @click="openEdit(u)">编辑</button>
              <button class="btn-ghost btn-sm" @click="openReset(u)" style="margin-left:4px">重置密码</button>
              <button class="btn-ghost btn-sm btn-danger" @click="handleDelete(u)" style="margin-left:4px">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > size">
        <button class="btn-ghost btn-sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span class="page-info mono">第 {{ page }} / {{ totalPages }} 页</span>
        <button class="btn-ghost btn-sm" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </Panel>

    <!-- 新建/编辑用户对话框 -->
    <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3>{{ editMode ? '编辑用户' : '新建用户' }}</h3>
          <button class="modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>用户名<span class="req">*</span></label>
            <input v-model="form.username" :disabled="editMode" placeholder="登录用户名" />
          </div>
          <div class="form-row" v-if="!editMode">
            <label>密码<span class="req">*</span></label>
            <input v-model="form.password" type="password" placeholder="登录密码" />
          </div>
          <div class="form-row">
            <label>真实姓名</label>
            <input v-model="form.realName" placeholder="真实姓名" />
          </div>
          <div class="form-row">
            <label>手机号</label>
            <input v-model="form.phone" placeholder="手机号" />
          </div>
          <div class="form-row">
            <label>邮箱</label>
            <input v-model="form.email" placeholder="邮箱" />
          </div>
          <div class="form-row" v-if="editMode">
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

    <!-- 重置密码对话框 -->
    <div v-if="resetVisible" class="modal-mask" @click.self="resetVisible = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3>重置密码 - {{ resetUser.username }}</h3>
          <button class="modal-close" @click="resetVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>新密码<span class="req">*</span></label>
            <input v-model="newPassword" type="password" placeholder="新密码" />
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn-ghost" @click="resetVisible = false">取消</button>
          <button class="btn-primary" :disabled="submitting" @click="handleReset">{{ submitting ? '提交中...' : '确定' }}</button>
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
import { listUsers, createUser, updateUser, deleteUser, resetPassword } from '@/api/management';

const users = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const searchKeyword = ref('');
const loading = ref(false);

const dialogVisible = ref(false);
const editMode = ref(false);
const submitting = ref(false);
const form = ref({ username: '', password: '', realName: '', phone: '', email: '', status: 'ACTIVE' });
const editingId = ref(null);

const resetVisible = ref(false);
const resetUser = ref({});
const newPassword = ref('');

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
const activeCount = computed(() => users.value.filter(u => u.status === 'ACTIVE').length);
const disabledCount = computed(() => users.value.filter(u => u.status === 'DISABLED').length);

function formatTime(t) {
  if (!t) return '-';
  return String(t).replace('T', ' ').substring(0, 16);
}

async function loadUsers() {
  loading.value = true;
  try {
    const res = await listUsers({ page: page.value, size: size.value, keyword: searchKeyword.value });
    users.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    ElMessage.error('加载用户列表失败');
    users.value = [];
  } finally {
    loading.value = false;
  }
}

function resetSearch() {
  searchKeyword.value = '';
  page.value = 1;
  loadUsers();
}

function changePage(p) {
  page.value = p;
  loadUsers();
}

function openCreate() {
  editMode.value = false;
  form.value = { username: '', password: '', realName: '', phone: '', email: '', status: 'ACTIVE' };
  editingId.value = null;
  dialogVisible.value = true;
}

function openEdit(u) {
  editMode.value = true;
  editingId.value = u.id;
  form.value = { username: u.username, password: '', realName: u.realName || '', phone: u.phone || '', email: u.email || '', status: u.status };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!form.value.username || (!editMode.value && !form.value.password)) {
    ElMessage.warning('请填写必填项');
    return;
  }
  submitting.value = true;
  try {
    if (editMode.value) {
      await updateUser(editingId.value, {
        realName: form.value.realName,
        phone: form.value.phone,
        email: form.value.email,
        status: form.value.status
      });
      ElMessage.success('用户更新成功');
    } else {
      await createUser({
        username: form.value.username,
        password: form.value.password,
        realName: form.value.realName,
        phone: form.value.phone,
        email: form.value.email
      });
      ElMessage.success('用户创建成功');
    }
    dialogVisible.value = false;
    loadUsers();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(u) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${u.username}" 吗？`, '删除确认', { type: 'warning' });
    await deleteUser(u.id);
    ElMessage.success('删除成功');
    loadUsers();
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败');
  }
}

function openReset(u) {
  resetUser.value = u;
  newPassword.value = '';
  resetVisible.value = true;
}

async function handleReset() {
  if (!newPassword.value) {
    ElMessage.warning('请输入新密码');
    return;
  }
  submitting.value = true;
  try {
    await resetPassword(resetUser.value.id, { password: newPassword.value });
    ElMessage.success('密码重置成功');
    resetVisible.value = false;
  } catch (e) {
    ElMessage.error('重置失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadUsers();
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
.btn-outline {
  padding: 6px 14px;
  font-size: 12px;
  color: #5D4E37;
  background: transparent;
  border: 1px solid #E8E2D9;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-outline:hover {
  border-color: #C9A86C;
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.06);
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
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}
.loading-tip {
  padding: 40px 0;
  text-align: center;
  color: #8B7355;
  font-size: 14px;
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
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
.page-info {
  font-size: 13px;
  color: #8B7355;
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
.form-row input:disabled {
  background: #F0EDE7;
  color: #A89F91;
  cursor: not-allowed;
}
.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
}

:deep(.el-input__wrapper) {
  background: #FAFAF8;
  border: 1px solid #E8E2D9;
  border-radius: 10px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
:deep(.el-input__wrapper:hover) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.15);
}
:deep(.el-input__wrapper.is-focus) {
  border-color: #C9A86C;
  box-shadow: 0 2px 12px rgba(201, 168, 108, 0.25);
}
:deep(.el-input__inner) {
  color: #5D4E37;
}
:deep(.el-input__inner::placeholder) {
  color: #B8A890;
}
</style>
