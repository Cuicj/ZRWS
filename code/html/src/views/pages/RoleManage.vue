<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">角色管理</h1>
        <div class="page-meta mono">ROLE MANAGEMENT · {{ roles.length }} 个角色</div>
      </div>
      <button class="btn-primary btn-sm">+ 添加角色</button>
    </div>

    <div class="stat-row">
      <StatCard label="角色总数" :value="roles.length" icon="◆" variant="accent" />
      <StatCard label="启用角色" :value="enabledCount" icon="✓" variant="ok" />
      <StatCard label="用户总数" :value="userTotal" icon="◉" variant="warn" />
      <StatCard label="权限项数" :value="permissionCount" icon="⚿" variant="accent" />
    </div>

    <Panel title="角色列表">
      <table>
        <thead><tr><th>角色ID</th><th>角色名称</th><th>角色标识</th><th>用户数</th><th>权限数</th><th>状态</th><th>描述</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in roles" :key="r.id">
            <td class="mono">{{ r.id }}</td>
            <td>{{ r.name }}</td>
            <td class="mono">{{ r.code }}</td>
            <td>{{ r.userCount }}</td>
            <td>{{ r.permCount }}</td>
            <td><span class="status-badge" :class="r.statusClass">{{ r.statusText }}</span></td>
            <td>{{ r.description }}</td>
            <td>
              <button class="btn btn-ghost btn-sm">编辑</button>
              <button class="btn btn-ghost btn-sm" style="margin-left:4px">权限</button>
              <button class="btn btn-ghost btn-sm btn-danger" style="margin-left:4px">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <Panel title="权限配置">
      <div class="perm-tree">
        <div v-for="group in permissionGroups" :key="group.name" class="perm-group">
          <div class="perm-group-title">
            <el-checkbox v-model="group.checked">{{ group.name }}</el-checkbox>
          </div>
          <div class="perm-items">
            <el-checkbox v-for="perm in group.items" :key="perm.code" v-model="perm.checked" class="perm-item">
              {{ perm.name }}
            </el-checkbox>
          </div>
        </div>
      </div>
    </Panel>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';

const roles = ref([
  { id: 'ROLE001', name: '超级管理员', code: 'admin', userCount: 1, permCount: 128, status: 'active', statusText: '启用', statusClass: 'status-ok', description: '拥有系统所有权限' },
  { id: 'ROLE002', name: '普通用户', code: 'user', userCount: 15, permCount: 45, status: 'active', statusText: '启用', statusClass: 'status-ok', description: '基础数据查看和操作权限' },
  { id: 'ROLE003', name: '审批员', code: 'approver', userCount: 3, permCount: 32, status: 'active', statusText: '启用', statusClass: 'status-ok', description: '审批流程相关权限' },
  { id: 'ROLE004', name: '数据管理员', code: 'data_admin', userCount: 2, permCount: 56, status: 'active', statusText: '启用', statusClass: 'status-ok', description: '数据导入、质量校验权限' },
  { id: 'ROLE005', name: '访客', code: 'guest', userCount: 0, permCount: 8, status: 'disabled', statusText: '禁用', statusClass: 'status-dim', description: '只读权限，已禁用' }
]);

const permissionGroups = ref([
  {
    name: '总览管理',
    checked: true,
    items: [
      { code: 'dashboard:view', name: '查看仪表盘', checked: true },
      { code: 'dashboard:export', name: '导出数据', checked: true }
    ]
  },
  {
    name: '采集管理',
    checked: true,
    items: [
      { code: 'mission:list', name: '任务列表', checked: true },
      { code: 'mission:create', name: '创建任务', checked: true },
      { code: 'flight:control', name: '飞行控制', checked: false },
      { code: 'gps:track', name: 'GPS航迹', checked: true },
      { code: 'soil:sample', name: '土壤采样', checked: true }
    ]
  },
  {
    name: '系统管理',
    checked: false,
    items: [
      { code: 'user:manage', name: '用户管理', checked: false },
      { code: 'role:manage', name: '角色管理', checked: false },
      { code: 'config:manage', name: '系统配置', checked: false }
    ]
  }
]);

const enabledCount = computed(() => roles.value.filter(r => r.status === 'active').length);
const userTotal = computed(() => roles.value.reduce((sum, r) => sum + r.userCount, 0));
const permissionCount = ref(128);
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
.perm-tree {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.perm-group {
  padding: 20px;
  background: linear-gradient(145deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.perm-group:hover {
  box-shadow: 0 6px 20px rgba(139, 115, 85, 0.12);
  transform: translateY(-2px);
}
.perm-group-title {
  font-weight: 500;
  color: #5D4E37;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 15px;
}
.perm-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.perm-item {
  margin-left: 16px;
}

:deep(.el-checkbox) {
  --el-checkbox-font-size: 14px;
}
:deep(.el-checkbox__label) {
  color: #5D4E37;
}
:deep(.el-checkbox__inner) {
  background: #FAFAF8;
  border: 1px solid #E8E2D9;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #C9A86C;
  border-color: #C9A86C;
}
:deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background: #C9A86C;
  border-color: #C9A86C;
}
:deep(.el-checkbox:hover .el-checkbox__inner) {
  border-color: #C9A86C;
}
:deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.2);
}
</style>
