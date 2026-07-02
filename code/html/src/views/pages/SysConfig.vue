<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">系统配置</h1>
        <div class="page-meta mono">SYSTEM CONFIGURATION · {{ configCount }} 项配置</div>
      </div>
      <button class="btn btn-primary btn-sm" @click="saveAll">保存配置</button>
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
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import { getSysConfigList, batchUpdateConfig } from '@/api/sysConfig.js';

const activeTab = ref('general');
const loading = ref(false);

const tabs = ref([]);

const currentTab = computed(() => tabs.value.find(t => t.key === activeTab.value));
const configCount = computed(() => tabs.value.reduce((sum, t) => sum + (t.configs?.length || 0), 0));

// 加载系统配置
const loadConfig = async () => {
  loading.value = true;
  try {
    const res = await getSysConfigList();
    if (res.data) {
      // 根据后端返回的数据结构进行适配
      const configData = res.data || [];
      
      // 将配置按照分组进行整理
      const groupedConfigs = {};
      configData.forEach(item => {
        const group = item.group || item.category || 'general';
        if (!groupedConfigs[group]) {
          groupedConfigs[group] = [];
        }
        groupedConfigs[group].push({
          key: item.key || item.configKey,
          name: item.name || item.configName,
          description: item.description || item.desc,
          type: item.type || 'string',
          value: item.value || item.configValue,
          min: item.min,
          max: item.max,
          options: item.options || []
        });
      });
      
      // 转换为 tabs 格式
      tabs.value = Object.keys(groupedConfigs).map(groupKey => ({
        key: groupKey,
        name: getGroupName(groupKey),
        configs: groupedConfigs[groupKey]
      }));
      
      // 如果没有配置分组，使用默认配置
      if (tabs.value.length === 0) {
        initDefaultConfig();
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error);
    ElMessage.error('加载配置失败，请稍后重试');
    // 使用默认配置
    initDefaultConfig();
  } finally {
    loading.value = false;
  }
};

// 初始化默认配置（作为后备）
const initDefaultConfig = () => {
  tabs.value = [
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
  ];
};

// 获取分组名称
const getGroupName = (groupKey) => {
  const nameMap = {
    general: '通用设置',
    display: '显示设置',
    security: '安全设置',
    content: '内容设置',
    system: '系统设置',
    notification: '通知设置'
  };
  return nameMap[groupKey] || groupKey;
};

const saveAll = async () => {
  loading.value = true;
  try {
    // 收集所有配置项
    const allConfigs = [];
    tabs.value.forEach(tab => {
      tab.configs.forEach(config => {
        allConfigs.push({
          key: config.key,
          value: config.value
        });
      });
    });
    
    const res = await batchUpdateConfig({ configs: allConfigs });
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('配置保存成功');
    } else {
      ElMessage.error(res.msg || '配置保存失败');
    }
  } catch (error) {
    console.error('保存配置失败:', error);
    ElMessage.error('保存配置失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadConfig();
});
</script>

<style scoped>
.page-container {
  padding: var(--s-5);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}
.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-5);
  border-bottom: 1px solid #E8E2D9;
}
.page-title {
  font-size: 28px;
  font-weight: 200;
  color: #5D4E37;
}
.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
}
.config-tabs {
  display: flex;
  gap: var(--s-1);
  margin-bottom: var(--s-4);
  border-bottom: 1px solid #E8E2D9;
}
.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  color: #8B7355;
  cursor: pointer;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px 8px 0 0;
}
.tab-btn:hover {
  color: #5D4E37;
  background: rgba(201, 168, 108, 0.08);
}
.tab-btn.active {
  color: #C9A86C;
  border-bottom-color: #C9A86C;
  font-weight: 500;
}
.config-form {
  display: flex;
  flex-direction: column;
  gap: var(--s-3);
}
.config-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--s-4);
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}
.config-item:hover {
  border-color: #C9A86C;
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.12);
  transform: translateY(-1px);
}
.config-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.config-name {
  font-size: 14px;
  color: #5D4E37;
  font-weight: 500;
}
.config-desc {
  font-size: 12px;
  color: #8B7355;
}
.config-value {
  display: flex;
  align-items: center;
}
.mono {
  color: #8B7355;
}

:deep(.el-switch) {
  --el-switch-on-color: #C9A86C;
  --el-switch-off-color: #E8E2D9;
}

:deep(.el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-input__wrapper:hover) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 2px rgba(201, 168, 108, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.2);
}

:deep(.el-input__inner) {
  color: #5D4E37;
}

:deep(.el-input__inner::placeholder) {
  color: #B8A98F;
}

:deep(.el-input-number) {
  --el-input-number-bg-color: #FFFFFF;
  --el-input-number-border-color: #E8E2D9;
  --el-text-color-primary: #5D4E37;
}

:deep(.el-input-number .el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
}

:deep(.el-select .el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
}
</style>
