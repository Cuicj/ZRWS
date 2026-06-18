/**
 * Vue 3 应用入口 - 智壤卫士
 */
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// Element Plus
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

// ECharts
import * as echarts from 'echarts';

// 设计代币
import './styles/design-tokens.css';

// Mock 数据
import './mock/mockData.js';

// 创建应用
const app = createApp(App);

// 注册 Element Plus
app.use(ElementPlus);

// 注册所有图标
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component);
}

// 全局属性：ECharts
app.config.globalProperties.$echarts = echarts;

// 挂载路由
app.use(router);

// 挂载
app.mount('#app');