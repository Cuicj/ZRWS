<template>
  <aside class="app-sidebar" :class="{ collapsed }">
    <!-- 折叠按钮 -->
    <div class="sidebar-toggle" @click="$emit('toggle')">
      <el-icon :size="16">
        <component :is="collapsed ? 'Expand' : 'Fold'" />
      </el-icon>
    </div>

    <!-- 导航分组 -->
    <nav class="sidebar-nav">
      <div v-for="group in navGroups" :key="group.title" class="nav-group">
        <!-- 分组标题 -->
        <div class="group-title mono" v-if="!collapsed">{{ group.title }}</div>

        <!-- 分组项 -->
        <router-link
          v-for="item in group.items"
          :key="item.path"
          :to="`/app/${item.path}`"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="item-icon">{{ item.icon }}</span>
          <span class="item-name" v-if="!collapsed">{{ item.name }}</span>
        </router-link>
      </div>
    </nav>

    <!-- 底部：返回门户 -->
    <div class="sidebar-footer">
      <router-link to="/" class="nav-item">
        <span class="item-icon">↩</span>
        <span class="item-name" v-if="!collapsed">返回门户</span>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
/**
 * AppSidebar.vue - 应用侧边栏
 */
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const props = defineProps({
  collapsed: Boolean
});

defineEmits(['toggle']);

const route = useRoute();

// 导航分组配置
const navGroups = [
  {
    title: '总览',
    items: [
      { path: 'dashboard', name: '运行仪表盘', icon: '◧' }
    ]
  },
  {
    title: '采集',
    items: [
      { path: 'mission-list', name: '任务列表', icon: '◫' },
      { path: 'flight-control', name: '飞行控制', icon: '➤' },
      { path: 'gps-track', name: 'GPS 航迹', icon: '◎' },
      { path: 'soil-sample', name: '土壤采样', icon: '◉' }
    ]
  },
  {
    title: '处理',
    items: [
      { path: 'data-import', name: '数据导入', icon: '↧' },
      { path: 'quality-check', name: '质量校验', icon: '✓' },
      { path: 'reconstruction', name: '3D 重建', icon: '◰' }
    ]
  },
  {
    title: 'AI',
    items: [
      { path: 'soil-classify', name: '土质分类', icon: '✦' },
      { path: 'disaster-risk', name: '灾害风险', icon: '◬' },
      { path: 'area-calc', name: '面积计算', icon: '◭' }
    ]
  },
  {
    title: 'CAD',
    items: [
      { path: 'cad-viewer', name: '图纸查看器', icon: '▣' },
      { path: 'cad-compare', name: '图纸对比', icon: '◫' }
    ]
  },
  {
    title: '审批',
    items: [
      { path: 'approval-list', name: '审批列表', icon: '◐' },
      { path: 'workflow-design', name: '流程设计', icon: '◭' }
    ]
  },
  {
    title: '系统',
    items: [
      { path: 'device', name: '设备管理', icon: '⊞' }
    ]
  }
];

// 判断是否激活
const isActive = (path) => {
  return route.path === `/app/${path}`;
};
</script>

<style scoped>
.app-sidebar {
  grid-column: 1;
  grid-row: 2;
  width: 220px;
  background: var(--ink-800);
  border-right: var(--line);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  overflow-y: auto;
}

.app-sidebar.collapsed {
  width: 48px;
}

.sidebar-toggle {
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--signal-dim);
  border-bottom: var(--line);
  transition: color var(--transition-fast);
}

.sidebar-toggle:hover {
  color: var(--sand-500);
}

.sidebar-nav {
  flex: 1;
  padding: var(--s-3) 0;
}

.nav-group {
  margin-bottom: var(--s-4);
}

.group-title {
  padding: var(--s-2) var(--s-4);
  font-size: 10px;
  color: var(--signal-dim);
  letter-spacing: 0.15em;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  padding: 10px var(--s-4);
  color: var(--signal-dim);
  text-decoration: none;
  font-size: 13px;
  border-left: 2px solid transparent;
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background: var(--ink-700);
  color: var(--signal);
}

.nav-item.active {
  color: var(--sand-500);
  border-left-color: var(--sand-500);
  background: var(--ink-700);
}

.item-icon {
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.item-name {
  flex: 1;
}

.sidebar-footer {
  padding: var(--s-3) 0;
  border-top: var(--line);
}

/* 折叠状态 */
.app-sidebar.collapsed .group-title {
  display: none;
}

.app-sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 10px 0;
}
</style>