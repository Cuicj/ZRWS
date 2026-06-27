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
/* ===== 明亮柔和风格 ===== */
.panel {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.06);
  transition: all 0.3s ease;
  overflow: hidden;
}

.panel:hover {
  box-shadow: 0 6px 24px rgba(139, 115, 85, 0.1);
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
}

.panel-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #5D4E37;
  margin: 0;
}

.panel-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.panel-meta {
  font-size: 11px;
  color: #A89F91;
  letter-spacing: 0.5px;
  font-weight: 500;
  padding: 4px 10px;
  background: rgba(139, 115, 85, 0.08);
  border-radius: 10px;
}

.panel-content {
  padding: 20px;
}
</style>