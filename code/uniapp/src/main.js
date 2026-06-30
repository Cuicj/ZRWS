import {
  createSSRApp
} from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)

  const pinia = createPinia()
  app.use(pinia)

  app.config.globalProperties.$baseUrl = 'https://api.zrws.example.com'
  app.config.globalProperties.$token = ''

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
    app,
    pinia
  }
}
