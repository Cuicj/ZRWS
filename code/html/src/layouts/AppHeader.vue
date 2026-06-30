<template>
  <header class="app-header">
    <!-- 左侧：Logo + 标题 -->
    <div class="header-left">
      <router-link to="/" class="brand">
        <span class="brand-mark">◐</span>
        <span class="brand-name display">智壤卫士</span>
        <span class="brand-meta mono">ZRWS v{{ appVersion }}</span>
      </router-link>
    </div>

    <!-- 右侧：用户信息 + 操作 -->
    <div class="header-right">
      <!-- 公告栏跳转 -->
      <a href="https://www.zrws.cloud/TZ" target="_blank" class="header-link-outer" title="公告栏">
        <el-icon :size="18" class="icon-btn"><Notification /></el-icon>
        <span class="link-text">公告栏</span>
      </a>

      <!-- 消息通知 -->
      <el-badge :value="notificationCount" class="notification-badge">
        <el-icon :size="18" class="icon-btn"><Bell /></el-icon>
      </el-badge>

      <el-divider direction="vertical" />

      <!-- 用户头像 -->
      <el-avatar :size="28" class="user-avatar">{{ user.name.charAt(0) }}</el-avatar>

      <!-- 用户信息 -->
      <div class="user-info">
        <span class="user-name">{{ user.name }}</span>
        <span class="user-role mono">{{ user.role }}</span>
      </div>

      <!-- 退出 -->
      <el-button size="small" class="btn-logout" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出</span>
      </el-button>
    </div>
  </header>
</template>

<script setup>
/**
 * AppHeader.vue - 应用顶栏
 */
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { Notification } from '@element-plus/icons-vue';

const router = useRouter();
const appVersion = __APP_VERSION__;

// 用户信息
const user = ref({
  name: '王工',
  role: '技术管理员'
});

// 通知数量
const notificationCount = ref(3);

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    });
    router.push('/login');
  } catch {
    // 取消
  }
};
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  border-bottom: 1px solid #E8E2D9;
  z-index: 200;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #5D4E37;
}

.brand-mark {
  font-size: 28px;
  color: #C9A86C;
  filter: drop-shadow(0 2px 4px rgba(201, 168, 108, 0.3));
}

.brand-name {
  font-size: 22px;
  font-weight: 400;
  letter-spacing: -0.5px;
}

.brand-meta {
  font-size: 11px;
  color: #A89F91;
  letter-spacing: 0.15em;
  padding: 2px 8px;
  background: rgba(139, 115, 85, 0.08);
  border-radius: 10px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-link-outer {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: #8B7355;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.header-link-outer:hover {
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.1);
  transform: translateY(-1px);
}

.header-link-outer .icon-btn {
  padding: 0;
}

.header-link-outer:hover .icon-btn {
  color: #C9A86C;
  background: transparent;
  transform: none;
}

.link-text {
  font-size: 13px;
}

.icon-btn {
  color: #8B7355;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 6px;
  border-radius: 8px;
}

.icon-btn:hover {
  color: #C9A86C;
  background: rgba(201, 168, 108, 0.1);
  transform: translateY(-1px);
}

.notification-badge {
  margin-right: 4px;
}

.user-avatar {
  background: linear-gradient(135deg, #C9A86C 0%, #D4B87A 100%);
  color: #fff;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  color: #5D4E37;
  font-weight: 500;
}

.user-role {
  font-size: 11px;
  color: #A89F91;
}

.btn-logout {
  background: transparent;
  border: 1px solid #D4C4B0;
  color: #8B7355;
  font-family: var(--font-body);
  font-size: 12px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-logout:hover {
  border-color: #E57373;
  color: #E57373;
  background: rgba(229, 115, 115, 0.05);
  transform: translateY(-1px);
}
</style>