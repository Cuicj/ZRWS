<template>
  <button
    :class="buttonClass"
    :disabled="disabled || loading"
    :type="htmlType"
    @click="handleClick"
  >
    <span v-if="loading" class="btn-spinner" />
    <slot v-else name="icon" />
    <span class="btn-text">
      <slot />
    </span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'outline', 'ghost', 'danger', 'success'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['xs', 'sm', 'md', 'lg'].includes(v)
  },
  iconOnly: {
    type: Boolean,
    default: false
  },
  block: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  htmlType: {
    type: String,
    default: 'button'
  }
})

const emit = defineEmits(['click'])

const buttonClass = computed(() => [
  'btn',
  `btn-${props.variant}`,
  `btn-${props.size}`,
  {
    'btn-icon-only': props.iconOnly,
    'btn-block': props.block,
    'btn-loading': props.loading
  }
])

const handleClick = (e) => {
  if (!props.disabled && !props.loading) {
    emit('click', e)
  }
}
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  user-select: none;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.btn:focus-visible {
  outline: 2px solid var(--sand-500);
  outline-offset: 2px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* Primary */
.btn-primary {
  background: linear-gradient(135deg, var(--sand-500) 0%, var(--sand-400) 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
  font-weight: 600;
}
.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--sand-600) 0%, var(--sand-500) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.4);
}
.btn-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(201, 168, 108, 0.3);
}

/* Secondary */
.btn-secondary {
  background: linear-gradient(135deg, var(--ink-700) 0%, var(--ink-600) 100%);
  color: var(--signal);
  border: 1px solid var(--ink-500);
}
.btn-secondary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--ink-600) 0%, var(--ink-500) 100%);
  border-color: var(--sand-500);
  color: var(--sand-600);
}
.btn-secondary:active:not(:disabled) {
  background: var(--ink-500);
}

/* Outline */
.btn-outline {
  background: transparent;
  border: 1px solid var(--ink-500);
  color: var(--signal);
}
.btn-outline:hover:not(:disabled) {
  border-color: var(--sand-500);
  color: var(--sand-600);
  background: rgba(201, 168, 108, 0.06);
}
.btn-outline:active:not(:disabled) {
  background: rgba(201, 168, 108, 0.12);
}

/* Ghost */
.btn-ghost {
  background: transparent;
  border: 1px solid transparent;
  color: var(--signal-dim);
}
.btn-ghost:hover:not(:disabled) {
  color: var(--sand-600);
  background: rgba(201, 168, 108, 0.08);
}
.btn-ghost:active:not(:disabled) {
  background: rgba(201, 168, 108, 0.15);
}

/* Danger */
.btn-danger {
  background: linear-gradient(135deg, #EF5350 0%, #E57373 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(239, 83, 80, 0.3);
  font-weight: 600;
}
.btn-danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #E53935 0%, #EF5350 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 83, 80, 0.4);
}
.btn-danger:active:not(:disabled) {
  transform: translateY(0);
}

/* Success */
.btn-success {
  background: linear-gradient(135deg, #66BB6A 0%, #81C784 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(102, 187, 106, 0.3);
  font-weight: 600;
}
.btn-success:hover:not(:disabled) {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
}
.btn-success:active:not(:disabled) {
  transform: translateY(0);
}

/* Sizes */
.btn-xs { padding: 4px 10px; font-size: 11px; border-radius: 6px; gap: 4px; }
.btn-sm { padding: 6px 14px; font-size: 12px; border-radius: var(--radius-md); gap: 6px; }
.btn-md { padding: 10px 20px; font-size: 13px; }
.btn-lg { padding: 14px 28px; font-size: 14px; border-radius: var(--radius-lg); gap: 10px; font-weight: 600; }

/* Icon only */
.btn-icon-only {
  padding: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
}
.btn-icon-only.btn-xs { width: 24px; height: 24px; }
.btn-icon-only.btn-sm { width: 30px; height: 30px; }
.btn-icon-only.btn-md { width: 36px; height: 36px; }
.btn-icon-only.btn-lg { width: 44px; height: 44px; }

/* Block */
.btn-block {
  width: 100%;
  display: flex;
}

/* Loading */
.btn-loading {
  pointer-events: none;
  opacity: 0.7;
}
.btn-spinner {
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: btn-spin 0.7s linear infinite;
}
@keyframes btn-spin {
  to { transform: rotate(360deg); }
}

.btn-text {
  display: inline-flex;
  align-items: center;
}
</style>
