<template>
  <div class="app-shell">
    <AppHeader />

    <AppSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />

    <main class="shell-main" :class="{ expanded: sidebarCollapsed }">
      <div class="breadcrumb-bar">
        <span class="mono">工作台</span>
        <span class="sep">/</span>
        <span class="mono" v-if="currentRoute.meta?.group">{{ currentRoute.meta.group }}</span>
        <span class="sep" v-if="currentRoute.meta?.group">/</span>
        <span>{{ currentRoute.meta?.title || currentRoute.name }}</span>
      </div>

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
  display: flex;
  min-height: 100vh;
}

.shell-main {
  flex: 1;
  margin-left: 220px;
  background: var(--ink-900);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left var(--transition-normal);
}

.shell-main.expanded {
  margin-left: 48px;
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
  position: sticky;
  top: 0;
  z-index: 10;
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