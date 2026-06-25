<template>
  <header class="app-header">
    <!-- 左侧：Logo + 标题 -->
    <div class="header-left">
      <router-link to="/" class="brand">
        <span class="brand-mark">◐</span>
        <span class="brand-name display">智壤卫士</span>
        <span class="brand-meta mono">ZRWS v1.0</span>
      </router-link>
    </div>

    <!-- 右侧：用户信息 + 操作 -->
    <div class="header-right">
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

const router = useRouter();

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
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--s-5);
  background: var(--ink-800);
  border-bottom: var(--line);
  z-index: 200;
}

.header-left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: baseline;
  gap: var(--s-3);
  text-decoration: none;
  color: var(--signal);
}

.brand-mark {
  font-size: 24px;
  color: var(--sand-500);
}

.brand-name {
  font-size: 18px;
  font-weight: 300;
}

.brand-meta {
  font-size: 10px;
  color: var(--signal-dim);
  letter-spacing: 0.2em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--s-3);
}

.icon-btn {
  color: var(--signal-dim);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.icon-btn:hover {
  color: var(--signal);
}

.user-avatar {
  background: var(--sand-500);
  color: var(--ink-900);
  font-family: var(--font-mono);
  font-size: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  color: var(--signal);
}

.user-role {
  font-size: 10px;
  color: var(--signal-dim);
}

.btn-logout {
  background: transparent;
  border: var(--line);
  color: var(--signal-dim);
  font-family: var(--font-mono);
  font-size: 11px;
}

.btn-logout:hover {
  border-color: var(--danger);
  color: var(--danger);
}
</style>