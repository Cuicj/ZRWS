<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">系统配置</h1>
        <div class="page-meta mono">SYSTEM CONFIGURATION · {{ configCount }} 项配置</div>
      </div>
      <button class="btn-primary btn-sm" @click="saveAll">保存配置</button>
    </div>

    <div class="config-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key" 
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.name }}
      </button>
    </div>

    <Panel :title="currentTab.name">
      <div class="config-form">
        <div v-for="config in currentTab.configs" :key="config.key" class="config-item">
          <div class="config-label">
            <span class="config-name">{{ config.name }}</span>
            <span class="config-desc">{{ config.description }}</span>
          </div>
          <div class="config-value">
            <el-switch v-if="config.type === 'boolean'" v-model="config.value" />
            <el-input v-else-if="config.type === 'string'" v-model="config.value" size="small" style="width:300px" />
            <el-input-number v-else-if="config.type === 'number'" v-model="config.value" size="small" :min="config.min || 0" :max="config.max || 999" />
            <el-select v-else-if="config.type === 'select'" v-model="config.value" size="small" style="width:200px">
              <el-option v-for="opt in config.options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
        </div>
      </div>
    </Panel>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';

const activeTab = ref('general');

const tabs = ref([
  {
    key: 'general',
    name: '通用设置',
    configs: [
      { key: 'site_name', name: '站点名称', description: '系统显示的名称', type: 'string', value: '智壤卫士' },
      { key: 'site_logo', name: '站点Logo', description: '系统Logo图片地址', type: 'string', value: '/logo.png' },
      { key: 'default_theme', name: '默认主题', description: '新用户默认使用的主题', type: 'select', value: 'dark', options: [
        { label: '深色主题', value: 'dark' },
        { label: '浅色主题', value: 'light' }
      ]},
      { key: 'enable_register', name: '开放注册', description: '是否允许用户自行注册', type: 'boolean', value: false },
      { key: 'session_timeout', name: '会话超时', description: '用户登录超时时间（分钟）', type: 'number', value: 30, min: 5, max: 120 }
    ]
  },
  {
    key: 'display',
    name: '显示设置',
    configs: [
      { key: 'font_size', name: '字体大小', description: '全局默认字体大小', type: 'select', value: 'medium', options: [
        { label: '小', value: 'small' },
        { label: '中', value: 'medium' },
        { label: '大', value: 'large' }
      ]},
      { key: 'sidebar_width', name: '侧边栏宽度', description: '左侧菜单宽度（像素）', type: 'number', value: 220, min: 180, max: 300 },
      { key: 'show_breadcrumb', name: '显示面包屑', description: '是否显示页面顶部面包屑导航', type: 'boolean', value: true },
      { key: 'animation_enabled', name: '启用动画', description: '是否启用页面切换动画', type: 'boolean', value: true }
    ]
  },
  {
    key: 'security',
    name: '安全设置',
    configs: [
      { key: 'password_min_length', name: '密码最小长度', description: '用户密码最小字符数', type: 'number', value: 8, min: 6, max: 32 },
      { key: 'password_strong', name: '强密码要求', description: '要求包含大小写字母、数字、特殊字符', type: 'boolean', value: true },
      { key: 'login_max_attempts', name: '最大登录尝试', description: '连续失败次数后锁定账户', type: 'number', value: 5, min: 3, max: 10 },
      { key: 'enable_captcha', name: '启用验证码', description: '登录时显示图形验证码', type: 'boolean', value: false }
    ]
  },
  {
    key: 'content',
    name: '内容设置',
    configs: [
      { key: 'disable_words', name: '禁用词列表', description: '内容审核禁用词，逗号分隔', type: 'string', value: '敏感词1,敏感词2,敏感词3' },
      { key: 'enable_audit', name: '内容审核', description: '发布内容是否需要审核', type: 'boolean', value: true },
      { key: 'announcement_enabled', name: '启用公告', description: '是否显示系统公告栏', type: 'boolean', value: true },
      { key: 'file_max_size', name: '文件上传大小', description: '单文件最大上传大小（MB）', type: 'number', value: 50, min: 1, max: 500 }
    ]
  }
]);

const currentTab = computed(() => tabs.value.find(t => t.key === activeTab.value));
const configCount = computed(() => tabs.value.reduce((sum, t) => sum + t.configs.length, 0));

const saveAll = () => {
  console.log('保存配置');
};
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.config-tabs { display: flex; gap: var(--s-1); margin-bottom: var(--s-4); border-bottom: var(--line); }
.tab-btn { padding: 10px 20px; background: none; border: none; color: var(--signal-dim); cursor: pointer; font-size: 14px; border-bottom: 2px solid transparent; transition: all var(--transition-fast); }
.tab-btn:hover { color: var(--signal); }
.tab-btn.active { color: var(--sand-400); border-bottom-color: var(--sand-400); }
.config-form { display: flex; flex-direction: column; gap: var(--s-4); }
.config-item { display: flex; align-items: center; justify-content: space-between; padding: var(--s-3); background: var(--ink-800); border: var(--line); border-radius: 6px; }
.config-label { display: flex; flex-direction: column; gap: 4px; }
.config-name { font-size: 14px; color: var(--signal); }
.config-desc { font-size: 12px; color: var(--signal-dim); }
.config-value { display: flex; align-items: center; }
</style>
