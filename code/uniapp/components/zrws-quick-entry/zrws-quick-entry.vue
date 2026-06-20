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
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx 16rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10rpx 4rpx;
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
  .qi-blue   { background: #ecf5ff; }
  .qi-green  { background: #f0f9eb; }
  .qi-orange { background: #fdf6ec; }
  .qi-purple { background: #f9f0ff; }
  .qi-cyan   { background: #e0f5f5; }
  .qi-yellow { background: #fffbe6; }
  .qi-pink   { background: #fef0f0; }
  .qi-gray   { background: #f4f4f5; }
  .qi-label {
    font-size: 24rpx;
    color: #606266;
    text-align: center;
  }
</style>
