<template>
  <button
    :class="buttonClass"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <view v-if="loading" class="btn-spinner" />
    <slot v-else name="icon" />
    <text class="btn-text">
      <slot />
    </text>
  </button>
</template>

<script>
export default {
  name: 'ZrwsButton',
  props: {
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
    }
  },
  emits: ['click'],
  computed: {
    buttonClass() {
      return [
        'zrws-btn',
        `zrws-btn--${this.variant}`,
        `zrws-btn--${this.size}`,
        {
          'zrws-btn--icon-only': this.iconOnly,
          'zrws-btn--block': this.block,
          'zrws-btn--loading': this.loading,
          'zrws-btn--disabled': this.disabled
        }
      ]
    }
  },
  methods: {
    handleClick() {
      if (!this.disabled && !this.loading) {
        this.$emit('click')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.zrws-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 26rpx;
  font-weight: 500;
  line-height: 1.4;
  border: none;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.zrws-btn--disabled {
  opacity: 0.5;
}

.zrws-btn--loading {
  pointer-events: none;
  opacity: 0.7;
}

/* Primary - 沙金色主按钮 */
.zrws-btn--primary {
  background: linear-gradient(135deg, #D9C49A 0%, #C9A96E 100%);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(201, 169, 110, 0.3);
  font-weight: 600;
}

.zrws-btn--primary:active:not(.zrws-btn--disabled) {
  background: linear-gradient(135deg, #C9A96E 0%, #A88B4F 100%);
  box-shadow: 0 2rpx 8rpx rgba(201, 169, 110, 0.3);
}

/* Secondary - 次要按钮（暖米白） */
.zrws-btn--secondary {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  color: #5D4E37;
  border: 1px solid #E8E2D9;
}

.zrws-btn--secondary:active:not(.zrws-btn--disabled) {
  background: linear-gradient(135deg, #F5F2ED 0%, #E8E2D9 100%);
  border-color: #C9A96E;
  color: #C9A96E;
}

/* Outline - 线框按钮 */
.zrws-btn--outline {
  background: transparent;
  border: 1px solid #D8CFBE;
  color: #5D4E37;
}

.zrws-btn--outline:active:not(.zrws-btn--disabled) {
  border-color: #C9A96E;
  color: #C9A96E;
  background: rgba(201, 169, 110, 0.08);
}

/* Ghost - 幽灵按钮 */
.zrws-btn--ghost {
  background: transparent;
  border: 1px solid transparent;
  color: #8B7355;
}

.zrws-btn--ghost:active:not(.zrws-btn--disabled) {
  color: #C9A96E;
  background: rgba(201, 169, 110, 0.08);
}

/* Danger - 危险按钮 */
.zrws-btn--danger {
  background: linear-gradient(135deg, #E57373 0%, #E53935 100%);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(229, 57, 53, 0.25);
  font-weight: 600;
}

.zrws-btn--danger:active:not(.zrws-btn--disabled) {
  background: linear-gradient(135deg, #E53935 0%, #C62828 100%);
}

/* Success - 成功按钮 */
.zrws-btn--success {
  background: linear-gradient(135deg, #81C784 0%, #7CB342 100%);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(124, 179, 66, 0.25);
  font-weight: 600;
}

.zrws-btn--success:active:not(.zrws-btn--disabled) {
  background: linear-gradient(135deg, #7CB342 0%, #689F38 100%);
}

/* Sizes */
.zrws-btn--xs {
  padding: 8rpx 20rpx;
  font-size: 22rpx;
  border-radius: 12rpx;
  gap: 6rpx;
}

.zrws-btn--sm {
  padding: 12rpx 28rpx;
  font-size: 24rpx;
  border-radius: 14rpx;
  gap: 8rpx;
}

.zrws-btn--md {
  padding: 20rpx 40rpx;
  font-size: 26rpx;
}

.zrws-btn--lg {
  padding: 28rpx 56rpx;
  font-size: 28rpx;
  border-radius: 20rpx;
  gap: 12rpx;
  font-weight: 600;
}

/* Icon only */
.zrws-btn--icon-only {
  padding: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
}

.zrws-btn--icon-only.zrws-btn--xs {
  width: 48rpx;
  height: 48rpx;
}

.zrws-btn--icon-only.zrws-btn--sm {
  width: 60rpx;
  height: 60rpx;
}

.zrws-btn--icon-only.zrws-btn--md {
  width: 72rpx;
  height: 72rpx;
}

.zrws-btn--icon-only.zrws-btn--lg {
  width: 88rpx;
  height: 88rpx;
}

/* Block */
.zrws-btn--block {
  width: 100%;
  display: flex;
}

/* Loading spinner */
.btn-spinner {
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: zrws-btn-spin 0.7s linear infinite;
}

@keyframes zrws-btn-spin {
  to {
    transform: rotate(360deg);
  }
}

.btn-text {
  display: inline-flex;
  align-items: center;
}
</style>
