<template>
  <!-- 根组件：根据路由 meta.layout 决定是否使用布局壳 -->
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
/**
 * App.vue - Vue 3 根组件
 * 仅作为路由出口，不包含任何布局逻辑
 */
</script>

<style>
/* 确保根容器全屏 */
#app {
  min-height: 100vh;
  position: relative;
  z-index: 2; /* 在背景纹理之上 */
}
</style>