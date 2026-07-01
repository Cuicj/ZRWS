<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-img"></div>
      <div class="bg-overlay"></div>
      <div class="bg-grid"></div>
      <div class="bg-particles">
        <span v-for="n in 20" :key="n" class="particle" :style="getParticleStyle(n)"></span>
      </div>
    </div>
    <div class="login-card">
      <div class="login-head">
        <div class="brand-logo">
          <span class="brand-mark">◐</span>
        </div>
        <h1 class="login-title display">智壤卫士</h1>
        <div class="login-sub mono">ZIRANG SHIELD v{{ appVersion }}</div>
      </div>

      <el-form class="login-form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-select v-model="role" placeholder="选择角色" size="large" style="width:100%">
            <el-option label="技术管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="审批员" value="approver" />
          </el-select>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="handleLogin">
          <span>登录</span>
          <span class="btn-arrow">→</span>
        </el-button>
      </el-form>

      <div class="register-entry">
        <span class="reg-label">还没有账号？</span>
        <a class="reg-link" @click="goToRegister">扫码注册</a>
      </div>

      <div class="app-download">
        <div class="qr-code-box">
          <img :src="qrCodeUrl" alt="下载APP" class="qr-code" />
          <div class="qr-tip">
            <span class="qr-icon">📱</span>
            <span>扫码下载APP</span>
          </div>
        </div>
        <div class="download-links">
          <a class="dl-link android" @click="downloadAndroid">
            <span class="dl-icon">🤖</span>
            <span>安卓下载</span>
          </a>
          <a class="dl-link ios" @click="goToH5">
            <span class="dl-icon">🍎</span>
            <span>iOS版(H5)</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { loginApi } from '@/api/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const appVersion = __APP_VERSION__;
const username = ref('');
const password = ref('');
const role = ref('admin');
const loading = ref(false);

const downloadPageUrl = window.location.origin + '/app-download.html';
const qrCodeUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=' + encodeURIComponent(downloadPageUrl);
const registerUrl = window.location.origin + '/register.html';

const downloadAndroid = () => {
  window.open('/apk/zrws.apk', '_blank');
};

const goToH5 = () => {
  window.open('/h5/', '_blank');
};

const goToRegister = () => {
  window.open(registerUrl, '_blank');
};

const getParticleStyle = (n) => {
  const left = Math.random() * 100;
  const delay = Math.random() * 8;
  const duration = 8 + Math.random() * 12;
  const size = 2 + Math.random() * 4;
  const opacity = 0.2 + Math.random() * 0.4;
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`,
    opacity: opacity
  };
};

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  try {
    loading.value = true;
    // 调用真实后端API
    const res = await fetch('/approval/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    });
    const result = await res.json();

    if (result.success && result.data) {
      const { token, userId, username: uname, realName, orgId, orgName, tenantId, subscriptionLevel } = result.data;
      localStorage.setItem('token', token);
      localStorage.setItem('currentUser', JSON.stringify({
        id: userId,
        username: uname,
        name: realName || uname,
        orgId,
        orgName,
        tenantId,
        subscriptionLevel,
        role: role.value
      }));
      ElMessage.success('登录成功');
      router.push('/app/dashboard');
    } else {
      ElMessage.error(result.msg || '用户名或密码错误');
    }
  } catch (e) {
    console.error('登录失败:', e.message);
    ElMessage.error('登录失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (localStorage.getItem('token')) {
    router.push('/app/dashboard');
  }
});
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-img {
  position: absolute;
  inset: 0;
  background-image: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=aerial%20view%20of%20agricultural%20fields%20with%20soil%20texture%20patterns%2C%20soft%20natural%20daylight%2C%20warm%20earthy%20tones%2C%20rolling%20hills%2C%20golden%20wheat%20fields%2C%20gentle%20morning%20mist%2C%20peaceful%20countryside%20landscape%2C%20ultra%20detailed%2C%208k%20quality&image_size=landscape_16_9');
  background-size: cover;
  background-position: center;
  filter: brightness(0.95) contrast(1.05) saturate(0.9);
  animation: bgKenBurns 25s ease-in-out infinite alternate;
  opacity: 0.4;
}

@keyframes bgKenBurns {
  0% {
    transform: scale(1) translate(0, 0);
  }
  100% {
    transform: scale(1.05) translate(-1%, -1%);
  }
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 70%, rgba(201, 168, 108, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 30%, rgba(139, 115, 85, 0.08) 0%, transparent 50%),
    linear-gradient(135deg, rgba(254, 251, 246, 0.85) 0%, rgba(247, 243, 237, 0.92) 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(200, 185, 160, 0.2) 1px, transparent 1px),
    linear-gradient(90deg, rgba(200, 185, 160, 0.2) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 80%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 80%);
}

.bg-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  bottom: -10px;
  background: var(--sand-500);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(201, 168, 108, 0.5);
  animation: floatUp linear infinite;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.5;
  }
  90% {
    opacity: 0.3;
  }
  100% {
    transform: translateY(-100vh) translateX(30px);
    opacity: 0;
  }
}

.login-card {
  width: 420px;
  padding: var(--s-7);
  background: linear-gradient(145deg, rgba(250, 250, 248, 0.92) 0%, rgba(245, 242, 237, 0.88) 100%);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-xl);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  z-index: 2;
  animation: cardFadeIn 0.8s ease-out;
  box-shadow: var(--shadow-lg);
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-head {
  text-align: center;
  margin-bottom: var(--s-6);
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--sand-500) 0%, var(--sand-400) 100%);
  margin-bottom: var(--s-4);
  animation: logoPulse 3s ease-in-out infinite;
  box-shadow: 0 4px 20px rgba(201, 168, 108, 0.35);
}

@keyframes logoPulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(201, 168, 108, 0.35);
  }
  50% {
    box-shadow: 0 6px 35px rgba(201, 168, 108, 0.5);
  }
}

.brand-mark {
  font-size: 32px;
  color: #fff;
  display: block;
  line-height: 1;
}

.login-title {
  font-size: 32px;
  font-weight: 400;
  color: var(--signal);
}

.login-sub {
  font-size: 11px;
  color: var(--signal-dim);
  letter-spacing: 0.2em;
  margin-top: var(--s-2);
}

.login-form {
  margin-bottom: var(--s-4);
}

:deep(.el-input__wrapper) {
  background: var(--ink-800);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-md);
  box-shadow: none;
  transition: all var(--transition-normal);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--sand-500);
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--sand-500);
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.15);
}

:deep(.el-input__inner) {
  color: var(--signal);
}

:deep(.el-input__inner::placeholder) {
  color: var(--ink-400);
}

:deep(.el-input__prefix-inner) {
  color: var(--signal-dim);
}

:deep(.el-select .el-input__wrapper) {
  background: var(--ink-800);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-md);
  box-shadow: none;
}

:deep(.el-select:hover .el-input__wrapper) {
  border-color: var(--sand-500);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: var(--sand-500);
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.15);
}

:deep(.el-select-dropdown) {
  background: var(--ink-800);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
}

:deep(.el-select-dropdown__item) {
  color: var(--signal);
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(201, 168, 108, 0.1);
}

:deep(.el-select-dropdown__item.selected) {
  color: var(--sand-500);
  font-weight: 600;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-family: var(--font-mono);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.login-btn:hover {
  transform: translateY(-2px);
}

.btn-arrow {
  transition: transform var(--transition-normal);
}

.login-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.login-hint {
  text-align: center;
  font-size: 11px;
  color: var(--signal-dim);
}

.register-entry {
  text-align: center;
  margin-top: var(--s-2);
  font-size: 13px;
}

.reg-label {
  color: var(--signal-dim);
}

.reg-link {
  color: #C9A96E;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s ease;
}

.reg-link:hover {
  color: #A88B4F;
  text-decoration: underline;
}

.app-download {
  margin-top: var(--s-5);
  padding-top: var(--s-5);
  border-top: 1px solid var(--ink-600);
  display: flex;
  align-items: center;
  gap: var(--s-4);
}

.qr-code-box {
  flex-shrink: 0;
  text-align: center;
}

.qr-code {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-md);
  background: #fff;
  padding: 6px;
  box-shadow: 0 2px 8px rgba(93, 78, 55, 0.1);
}

.qr-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--signal-dim);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.qr-icon {
  font-size: 14px;
}

.download-links {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dl-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-normal);
  text-decoration: none;
}

.dl-link.android {
  background: linear-gradient(135deg, var(--sand-500) 0%, var(--sand-400) 100%);
  color: #fff;
}

.dl-link.android:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.35);
}

.dl-link.ios {
  background: var(--ink-800);
  border: 1px solid var(--ink-600);
  color: var(--signal);
}

.dl-link.ios:hover {
  border-color: var(--sand-500);
  color: var(--sand-500);
}

.dl-icon {
  font-size: 16px;
}
</style>
