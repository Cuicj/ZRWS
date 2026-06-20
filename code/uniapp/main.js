import {
  createSSRApp
} from 'vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)

  // 全局配置
  app.config.globalProperties.$baseUrl = 'https://api.zrws.example.com'
  app.config.globalProperties.$token = ''

  // 全局方法
  app.config.globalProperties.$showToast = (title, icon = 'none') => {
    uni.showToast({ title, icon, duration: 2000 })
  }
  app.config.globalProperties.$showLoading = (title = '加载中...') => {
    uni.showLoading({ title, mask: true })
  }
  app.config.globalProperties.$hideLoading = () => {
    uni.hideLoading()
  }
  app.config.globalProperties.$navigateTo = (url) => {
    uni.navigateTo({ url })
  }

  return {
    app
  }
}
