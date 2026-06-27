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
  margin-left: 240px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding-top: 64px;
}

.shell-main.expanded {
  margin-left: 64px;
}

.breadcrumb-bar {
  padding: 12px 28px;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #FAFAF8 0%, #F7F3ED 100%);
  font-size: 12px;
  color: #8B7355;
  display: flex;
  align-items: center;
  gap: 8px;
  position: sticky;
  top: 64px;
  z-index: 10;
  font-weight: 500;
}

.sep {
  color: #D4C4B0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  position: relative;
}

/* 页面过渡动画优化 */
.slide-enter-active, .slide-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>