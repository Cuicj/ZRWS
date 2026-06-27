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
      { path: 'announcement-board', name: '公告栏', icon: '📰' },
      { path: 'soil-classify', name: '土质分类', icon: '✦' },
      { path: 'disaster-risk', name: '灾害风险', icon: '◬' },
      { path: 'area-calc', name: '面积计算', icon: '◭' }
    ]
  },
  {
    title: 'GIS',
    items: [
      { path: 'cad-viewer', name: '三维地球', icon: '🌍' },
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
  width: 240px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-right: 1px solid #E8E2D9;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 2px 0 12px rgba(139, 115, 85, 0.06);
}

.app-sidebar.collapsed {
  width: 64px;
}

.sidebar-toggle {
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8B7355;
  border-bottom: 1px solid #E8E2D9;
  transition: all 0.2s ease;
}

.sidebar-toggle:hover {
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.08);
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
}

.sidebar-nav::-webkit-scrollbar { width: 4px; }
.sidebar-nav::-webkit-scrollbar-track { background: transparent; }
.sidebar-nav::-webkit-scrollbar-thumb { background: #D4C4B0; border-radius: 2px; }

.nav-group {
  margin-bottom: 8px;
}

.group-title {
  padding: 10px 14px;
  font-size: 11px;
  color: #A89F91;
  letter-spacing: 0.1em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 4px;
  font-weight: 600;
}

.group-title:hover {
  color: #5D4E37;
  background: rgba(201, 168, 108, 0.1);
}

.title-text {
  flex: 1;
}

.expand-icon {
  transition: transform 0.2s ease;
}

.group-items {
  overflow: hidden;
  padding: 0 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  color: #6B5D4D;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  border-radius: 10px;
  margin: 2px 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-item:hover {
  background: rgba(201, 168, 108, 0.12);
  color: #5D4E37;
  transform: translateX(4px);
}

.nav-item.active {
  color: #fff;
  background: linear-gradient(135deg, #8B7355 0%, #6B5344 100%);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
  transform: translateX(0);
}

.nav-item.collapsed-item {
  justify-content: center;
  padding: 10px 0;
  margin: 4px;
  border-radius: 8px;
}

.item-icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.item-name {
  flex: 1;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid #E8E2D9;
}

.sidebar-footer .nav-item {
  margin: 0 4px;
}

.app-sidebar.collapsed .group-title {
  display: none;
}

.app-sidebar.collapsed .group-items {
  display: none;
}

.app-sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 12px 0;
  margin: 4px;
}
</style>
