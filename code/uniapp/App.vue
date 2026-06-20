<script>
  export default {
    onLaunch: function() {
      console.log('智壤卫士 App Launch')
      // 初始化登录态
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          console.log('已登录用户:', userInfo.name)
        }
      } catch (e) {
        console.error('读取本地存储失败:', e)
      }
    },
    onShow: function() {
      console.log('App Show')
    },
    onHide: function() {
      console.log('App Hide')
    }
  }
</script>

<style>
  /* 全局样式 - 每个页面通过 page-layout 组件包裹 */
  page {
    background-color: #f0f2f5;
    font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
    font-size: 28rpx;
    color: #303133;
  }
</style>
