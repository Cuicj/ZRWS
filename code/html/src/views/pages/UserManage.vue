<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">用户管理</h1>
        <div class="page-meta mono">USER MANAGEMENT · {{ users.length }} 位用户</div>
      </div>
      <button class="btn-primary btn-sm">+ 添加用户</button>
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
              <button class="btn-ghost btn-sm">编辑</button>
              <button class="btn-ghost btn-sm btn-danger" style="margin-left:4px">禁用</button>
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
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
.toolbar { display: flex; gap: var(--s-2); margin-bottom: var(--s-3); }
.role-badge { padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.role-admin { background: rgba(212, 168, 83, 0.15); color: var(--sand-400); }
.role-user { background: rgba(74, 144, 226, 0.15); color: #4a90e2; }
.role-approver { background: rgba(116, 192, 116, 0.15); color: #74c074; }
.btn-danger { color: #e74c3c; }
</style>
