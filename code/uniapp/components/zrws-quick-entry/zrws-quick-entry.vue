<template>
  <view class="quick-grid">
    <view
      v-for="item in items"
      :key="item.key"
      class="quick-item"
      @tap="onTap(item)">
      <view class="qi-icon" :class="'qi-' + item.color">{{ item.icon }}</view>
      <text class="qi-label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup>
  defineProps({
    items: {
      type: Array,
      default: () => [
        { key: 'mission', icon: '📝', label: '新建任务', color: 'blue' },
        { key: 'flight', icon: '✈️', label: '飞行控制', color: 'green' },
        { key: 'soil', icon: '🌱', label: '土壤采样', color: 'orange' },
        { key: 'area', icon: '📐', label: '面积测量', color: 'purple' },
        { key: 'gps', icon: '📍', label: 'GPS航迹', color: 'cyan' },
        { key: 'disaster', icon: '⚠️', label: '灾害评估', color: 'yellow' },
        { key: 'approval', icon: '✅', label: '审批中心', color: 'pink' },
        { key: 'cad', icon: '🖥️', label: 'CAD查看', color: 'gray' }
      ]
    }
  })

  const emit = defineEmits(['tap'])

  function onTap(item) { emit('tap', item) }
</script>

<style lang="scss" scoped>
  .quick-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 30rpx 16rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10rpx 4rpx;
    transition: all .3s ease;
  }
  .quick-item:active {
    transform: translateY(-4rpx);
  }
  .qi-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
    margin-bottom: 10rpx;
  }
  .qi-blue   { background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary); }
  .qi-green  { background: linear-gradient(135deg, #AED581, $zrws-success); }
  .qi-orange { background: linear-gradient(135deg, #FFB74D, $zrws-warning); }
  .qi-purple { background: linear-gradient(135deg, $zrws-primary-light, $zrws-primary-dark); }
  .qi-cyan   { background: linear-gradient(135deg, #80DEEA, $zrws-info); }
  .qi-yellow { background: linear-gradient(135deg, #FFE082, $zrws-primary); }
  .qi-pink   { background: linear-gradient(135deg, #F48FB1, #E57373); }
  .qi-gray   { background: linear-gradient(135deg, $zrws-bg-tertiary, $zrws-border-medium); }
  .qi-label {
    font-size: 24rpx;
    color: $zrws-text-primary;
    text-align: center;
  }
</style>
