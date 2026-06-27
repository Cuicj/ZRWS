import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import cesium from 'vite-plugin-cesium';
import pkg from './package.json';

export default defineConfig({
  plugins: [vue(), cesium()],
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version)
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    open: '/#/login',
    proxy: {
      '/api': {
        target: 'http://localhost:5571/approval',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          echarts: ['echarts'],
          'bpmn-js': ['bpmn-js']
        }
      }
    }
  }
});