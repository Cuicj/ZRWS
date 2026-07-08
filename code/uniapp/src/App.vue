<script>
  export default {
    onLaunch: function() {
      console.log('智壤卫士 App Launch')
      // 启动检查登录态，未登录则跳转登录页
      try {
        const token = uni.getStorageSync('token')
        const userInfo = uni.getStorageSync('userInfo')
        if (token && userInfo) {
          console.log('已登录用户:', userInfo.name)
        } else {
          // 未登录，跳转登录页
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 100)
        }
      } catch (e) {
        console.error('启动登录检查失败:', e)
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/login' })
        }, 100)
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
    background-color: #FEFBF6;
    font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
    font-size: 28rpx;
    color: #5D4E37;
  }
</style>
