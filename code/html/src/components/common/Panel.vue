<template>
  <div class="panel">
    <!-- 标题栏 -->
    <div class="panel-title" v-if="title">
      <h3 class="display">{{ title }}</h3>
      <div class="panel-actions">
        <slot name="actions" />
        <span v-if="meta" class="panel-meta mono">{{ meta }}</span>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="panel-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
/**
 * Panel.vue - 面板容器组件
 * 统一的卡片容器，包含标题栏和内容区
 */
defineProps({
  title: String,
  meta: String
});
</script>

<style scoped>
.panel {
  background: var(--ink-800);
  border: var(--line);
  margin-bottom: var(--s-4);
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--s-4);
  border-bottom: var(--line-soft);
}

.panel-title h3 {
  font-size: 18px;
  font-weight: 300;
}

.panel-actions {
  display: flex;
  gap: var(--s-3);
  align-items: center;
}

.panel-meta {
  font-size: 10px;
  color: var(--signal-dim);
  letter-spacing: 0.1em;
}

.panel-content {
  padding: var(--s-4);
}
</style>