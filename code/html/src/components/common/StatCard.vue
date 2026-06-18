<template>
  <div class="stat-card" :class="variant">
    <div class="stat-info">
      <div class="stat-label mono">{{ label }}</div>
      <div class="stat-value display">
        {{ value }}
        <span v-if="unit" class="stat-unit">{{ unit }}</span>
      </div>
      <div v-if="trend" class="stat-trend mono" :class="trendClass">
        {{ trendPrefix }}{{ trend }}% 较昨日
      </div>
    </div>
    <div class="stat-icon">
      <span>{{ icon }}</span>
    </div>
  </div>
</template>

<script setup>
/**
 * StatCard.vue - 统计卡片组件
 * 用于展示关键指标（任务数、精度、合格率等）
 */
import { computed } from 'vue';

const props = defineProps({
  label: String,
  value: [String, Number],
  unit: String,
  trend: Number,
  icon: String,
  variant: {
    type: String,
    default: 'default',
    validator: v => ['default', 'accent', 'ok', 'warn', 'danger'].includes(v)
  }
});

const trendClass = computed(() => {
  if (!props.trend) return '';
  return props.trend > 0 ? 'trend-up' : props.trend < 0 ? 'trend-down' : '';
});

const trendPrefix = computed(() => {
  if (!props.trend) return '';
  return props.trend > 0 ? '↑ ' : props.trend < 0 ? '↓ ' : '';
});
</script>

<style scoped>
.stat-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--s-4);
  background: var(--ink-800);
  border-left: 2px solid var(--ink-600);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  background: var(--ink-700);
}

.stat-card.accent {
  border-left-color: var(--sand-500);
}

.stat-card.ok {
  border-left-color: var(--ok);
}

.stat-card.warn {
  border-left-color: var(--warn);
}

.stat-card.danger {
  border-left-color: var(--danger);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 10px;
  color: var(--signal-dim);
  letter-spacing: 0.15em;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 300;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-unit {
  font-size: 12px;
  color: var(--signal-dim);
  margin-left: 2px;
}

.stat-trend {
  font-size: 11px;
  color: var(--signal-dim);
}

.stat-trend.trend-up {
  color: var(--ok);
}

.stat-trend.trend-down {
  color: var(--danger);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--signal-dim);
  background: var(--ink-700);
}
</style>