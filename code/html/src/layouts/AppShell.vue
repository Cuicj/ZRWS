<template>
  <div class="app-shell">
    <!-- 顶栏 -->
    <AppHeader />

    <!-- 侧边栏 -->
    <AppSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />

    <!-- 主内容区 -->
    <main class="shell-main" :class="{ expanded: sidebarCollapsed }">
      <!-- 面包屑 -->
      <div class="breadcrumb-bar">
        <span class="mono">工作台</span>
        <span class="sep">/</span>
        <span class="mono" v-if="currentRoute.meta?.group">{{ currentRoute.meta.group }}</span>
        <span class="sep" v-if="currentRoute.meta?.group">/</span>
        <span>{{ currentRoute.meta?.title || currentRoute.name }}</span>
      </div>

      <!-- 子路由内容 -->
      <div class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
/**
 * AppShell.vue - 应用框架壳
 * 包含：顶栏 + 侧边栏 + 主内容区
 */
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import AppHeader from './AppHeader.vue';
import AppSidebar from './AppSidebar.vue';

const route = useRoute();
const currentRoute = computed(() => route);

const sidebarCollapsed = ref(false);
</script>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: 56px 1fr;
  min-height: 100vh;
}

.shell-main {
  grid-column: 2;
  grid-row: 2;
  background: var(--ink-900);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left var(--transition-normal);
}

.breadcrumb-bar {
  padding: 8px var(--s-5);
  border-bottom: var(--line);
  background: var(--ink-800);
  font-size: 12px;
  color: var(--signal-dim);
  display: flex;
  align-items: center;
  gap: var(--s-2);
}

.sep {
  color: var(--ink-500);
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--s-5);
  position: relative;
}
</style>