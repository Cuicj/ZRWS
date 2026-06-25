<template>
  <aside class="app-sidebar" :class="{ collapsed }">
    <div class="sidebar-toggle" @click="$emit('toggle')">
      <el-icon :size="16">
        <component :is="collapsed ? 'Expand' : 'Fold'" />
      </el-icon>
    </div>

    <nav class="sidebar-nav">
      <div v-for="group in navGroups" :key="group.title" class="nav-group">
        <div 
          class="group-title mono" 
          v-if="!collapsed"
          @click="toggleGroup(group.title)"
        >
          <span class="title-text">{{ group.title }}</span>
          <el-icon :size="14" class="expand-icon">
            <component :is="expandedGroups.includes(group.title) ? 'ArrowDown' : 'ArrowRight'" />
          </el-icon>
        </div>

        <div class="group-items" v-show="!collapsed && expandedGroups.includes(group.title)">
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="`/app/${item.path}`"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="item-icon">{{ item.icon }}</span>
            <span class="item-name">{{ item.name }}</span>
          </router-link>
        </div>

        <router-link
          v-for="item in group.items"
          :key="item.path"
          :to="`/app/${item.path}`"
          class="nav-item collapsed-item"
          :class="{ active: isActive(item.path) }"
          v-show="collapsed"
        >
          <span class="item-icon">{{ item.icon }}</span>
          <span class="item-name" v-if="!collapsed">{{ item.name }}</span>
        </router-link>
      </div>
    </nav>

    <div class="sidebar-footer">
      <router-link to="/" class="nav-item">
        <span class="item-icon">↩</span>
        <span class="item-name" v-if="!collapsed">返回门户</span>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getMenuTree } from '@/api/menu';

const props = defineProps({
  collapsed: Boolean
});

defineEmits(['toggle']);

const route = useRoute();

const expandedGroups = ref(['总览']);
const navGroups = ref([]);
const loading = ref(true);

const defaultMenus = [
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
    title: '土地资源',
    items: [
      { path: 'land-map', name: '土地地图', icon: '◍' },
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
      { path: 'device', name: '设备管理', icon: '⊞' },
      { path: 'user-manage', name: '用户管理', icon: '◉' },
      { path: 'role-manage', name: '角色管理', icon: '◆' },
      { path: 'sys-config', name: '系统配置', icon: '⚙' },
      { path: 'announcement', name: '公告管理', icon: '✉' }
    ]
  }
];

const toggleGroup = (title) => {
  const index = expandedGroups.value.indexOf(title);
  if (index > -1) {
    if (expandedGroups.value.length > 1) {
      expandedGroups.value.splice(index, 1);
    }
  } else {
    expandedGroups.value = [title];
  }
};

const isActive = (path) => {
  return route.path === `/app/${path}`;
};

const loadMenus = async () => {
  try {
    const res = await getMenuTree();
    if (res && res.success && res.data && res.data.length > 0) {
      navGroups.value = res.data;
    } else {
      navGroups.value = defaultMenus;
    }
  } catch (e) {
    console.warn('加载菜单失败，使用默认菜单:', e.message);
    navGroups.value = defaultMenus;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadMenus();
});
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
  position: fixed;
  top: 56px;
  left: 0;
  bottom: 0;
  z-index: 100;
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
  overflow-y: auto;
}

.nav-group {
  margin-bottom: var(--s-2);
}

.group-title {
  padding: var(--s-2) var(--s-4);
  font-size: 10px;
  color: var(--signal-dim);
  letter-spacing: 0.15em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all var(--transition-fast);
}

.group-title:hover {
  color: var(--signal);
  background: var(--ink-700);
}

.title-text {
  flex: 1;
}

.expand-icon {
  transition: transform var(--transition-fast);
}

.group-items {
  overflow: hidden;
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

.nav-item.collapsed-item {
  justify-content: center;
  padding: 10px 0;
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

.app-sidebar.collapsed .group-title {
  display: none;
}

.app-sidebar.collapsed .group-items {
  display: none;
}

.app-sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 10px 0;
}
</style>
