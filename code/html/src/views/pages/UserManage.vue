<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">用户管理</h1>
        <div class="page-meta mono">USER MANAGEMENT · {{ users.length }} 位用户</div>
      </div>
      <button class="btn btn-primary btn-sm">+ 添加用户</button>
    </div>

    <div class="stat-row">
      <StatCard label="总用户数" :value="users.length" icon="◉" variant="accent" />
      <StatCard label="在线用户" :value="onlineCount" icon="●" variant="ok" />
      <StatCard label="禁用用户" :value="disabledCount" icon="○" variant="danger" />
      <StatCard label="今日新增" :value="todayNew" icon="↗" variant="warn" />
    </div>

    <Panel title="用户列表">
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" size="small" style="width:240px" clearable />
        <el-select v-model="roleFilter" placeholder="角色筛选" size="small" clearable style="width:140px">
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
          <el-option label="审批员" value="approver" />
        </el-select>
      </div>
      <table>
        <thead><tr><th>用户ID</th><th>用户名</th><th>姓名</th><th>角色</th><th>部门</th><th>手机号</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="u in filteredUsers" :key="u.id">
            <td class="mono">{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.name }}</td>
            <td><span class="role-badge" :class="u.roleClass">{{ u.roleText }}</span></td>
            <td>{{ u.dept }}</td>
            <td class="mono">{{ u.phone }}</td>
            <td><span class="status-badge" :class="u.statusClass">{{ u.statusText }}</span></td>
            <td class="mono">{{ u.createTime }}</td>
            <td>
              <button class="btn btn-ghost btn-sm">编辑</button>
              <button class="btn btn-ghost btn-sm btn-danger" style="margin-left:4px">禁用</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';

const searchKeyword = ref('');
const roleFilter = ref('');

const users = ref([
  { id: 'USR001', username: 'admin', name: '系统管理员', role: 'admin', roleText: '管理员', roleClass: 'role-admin', dept: '技术部', phone: '138****0001', status: 'active', statusText: '正常', statusClass: 'status-ok', createTime: '2026-01-01 09:00' },
  { id: 'USR002', username: 'zhangsan', name: '张三', role: 'user', roleText: '普通用户', roleClass: 'role-user', dept: '采集组', phone: '138****0002', status: 'active', statusText: '正常', statusClass: 'status-ok', createTime: '2026-02-15 10:30' },
  { id: 'USR003', username: 'lisi', name: '李四', role: 'user', roleText: '普通用户', roleClass: 'role-user', dept: '处理组', phone: '138****0003', status: 'active', statusText: '正常', statusClass: 'status-ok', createTime: '2026-03-01 14:20' },
  { id: 'USR004', username: 'approver', name: '王审批', role: 'approver', roleText: '审批员', roleClass: 'role-approver', dept: '质量部', phone: '138****0004', status: 'active', statusText: '正常', statusClass: 'status-ok', createTime: '2026-01-10 11:00' },
  { id: 'USR005', username: 'wangwu', name: '王五', role: 'user', roleText: '普通用户', roleClass: 'role-user', dept: '采集组', phone: '138****0005', status: 'disabled', statusText: '禁用', statusClass: 'status-err', createTime: '2026-04-20 16:45' }
]);

const onlineCount = computed(() => users.value.filter(u => u.status === 'active').length - 1);
const disabledCount = computed(() => users.value.filter(u => u.status === 'disabled').length);
const todayNew = ref(2);

const filteredUsers = computed(() => {
  let list = users.value;
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    list = list.filter(u => u.username.toLowerCase().includes(kw) || u.name.includes(kw) || u.phone.includes(kw));
  }
  if (roleFilter.value) {
    list = list.filter(u => u.role === roleFilter.value);
  }
  return list;
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
.role-approver {
  background: rgba(106, 153, 106, 0.18);
  color: #6A996A;
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
thead th:first-child {
  border-top-left-radius: 12px;
}
thead th:last-child {
  border-top-right-radius: 12px;
}
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
tbody tr:last-child td {
  border-bottom: none;
}
.mono {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 13px;
  color: #8B7355;
}
.display {
  font-weight: 300;
  letter-spacing: -0.5px;
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
:deep(.el-select .el-input__wrapper) {
  background: #FAFAF8;
}
:deep(.el-select-dropdown) {
  --el-select-border-color-hover: #C9A86C;
}
:deep(.el-select-dropdown__item) {
  color: #5D4E37;
}
:deep(.el-select-dropdown__item:hover) {
  background: rgba(201, 168, 108, 0.1);
}
:deep(.el-select-dropdown__item.selected) {
  color: #C9A86C;
  font-weight: 500;
}
:deep(.el-select-dropdown) {
  background: #FAFAF8;
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.12);
}
:deep(.el-select-dropdown__list) {
  padding: 6px;
}
:deep(.el-icon) {
  color: #8B7355;
}
</style>
