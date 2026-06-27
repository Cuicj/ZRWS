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
/* ===== 明亮柔和风格 ===== */
.stat-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 16px;
  border: 1px solid #E8E2D9;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  transition: width 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.12);
  border-color: #D4C4B0;
}

.stat-card:hover::before {
  width: 6px;
}

.stat-card.accent::before {
  background: linear-gradient(180deg, #C9A86C 0%, #D4B87A 100%);
}
.stat-card.ok::before {
  background: linear-gradient(180deg, #66BB6A 0%, #81C784 100%);
}
.stat-card.warn::before {
  background: linear-gradient(180deg, #FFA726 0%, #FFB74D 100%);
}
.stat-card.danger::before {
  background: linear-gradient(180deg, #EF5350 0%, #E57373 100%);
}
.stat-card.default::before {
  background: linear-gradient(180deg, #8B7355 0%, #A89F91 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #8B7355;
  font-weight: 600;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.stat-value {
  font-size: 36px;
  font-weight: 300;
  line-height: 1;
  margin-bottom: 8px;
  color: #5D4E37;
}

.stat-unit {
  font-size: 14px;
  color: #A89F91;
  margin-left: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #A89F91;
  font-weight: 500;
}

.stat-trend.trend-up {
  color: #66BB6A;
}

.stat-trend.trend-down {
  color: #EF5350;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #8B7355;
  background: linear-gradient(135deg, #F5F2ED 0%, #EBE5DB 100%);
  border-radius: 12px;
  margin-left: 12px;
}
</style>