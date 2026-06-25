import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';

import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

import * as echarts from 'echarts';

import './styles/design-tokens.css';

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);

app.use(ElementPlus);

for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component);
}

app.config.globalProperties.$echarts = echarts;

app.use(router);

app.mount('#app');