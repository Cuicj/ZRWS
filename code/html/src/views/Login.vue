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

      <div class="login-hint mono">
        <span>演示账号: admin / admin</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const appVersion = __APP_VERSION__;
const username = ref('admin');
const password = ref('admin');
const role = ref('admin');

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

const initDefaultData = () => {
  const mockUsers = [
    { id: 'U001', username: 'admin', password: 'admin', name: '技术管理员', role: 'admin', dept: '技术部' },
    { id: 'U002', username: 'user', password: 'user', name: '普通用户', role: 'user', dept: '业务部' },
    { id: 'U003', username: 'approver', password: 'approver', name: '审批员', role: 'approver', dept: '审批部' }
  ];

  const mockMissions = [
    { id: 'ZRS-2026-0617-001', area: '望城区乔口镇', status: 'completed', date: '2026-06-17', operator: '王工', coverage: 860 },
    { id: 'ZRS-2026-0616-003', area: '岳麓区莲花镇', status: 'completed', date: '2026-06-16', operator: '李工', coverage: 1250 },
    { id: 'ZRS-2026-0616-002', area: '雨花区跳马镇', status: 'processing', date: '2026-06-16', operator: '王工', coverage: 680 },
    { id: 'ZRS-2026-0615-001', area: '开福区青竹湖', status: 'completed', date: '2026-06-15', operator: '张工', coverage: 520 },
    { id: 'ZRS-2026-0614-002', area: '天心区暮云镇', status: 'abnormal', date: '2026-06-14', operator: '李工', coverage: 320 }
  ];

  const mockApprovals = [
    { id: 'APR-001', title: '2026年度测绘项目审批', status: 'pending', applicant: '王工', createTime: '2026-06-17' },
    { id: 'APR-002', title: '无人机设备采购审批', status: 'approved', applicant: '李工', createTime: '2026-06-16' },
    { id: 'APR-003', title: '数据处理服务合同审批', status: 'rejected', applicant: '张工', createTime: '2026-06-15' }
  ];

  const mockSoilSamples = [
    { id: 'SP-001', lat: 28.45672, lng: 112.83521, pH: 6.8, moisture: 0.32, ec: 245, type: '壤土', date: '2026-06-17' },
    { id: 'SP-002', lat: 28.45718, lng: 112.83605, pH: 7.2, moisture: 0.45, ec: 312, type: '黏土', date: '2026-06-17' },
    { id: 'SP-003', lat: 28.45801, lng: 112.83489, pH: 5.9, moisture: 0.18, ec: 178, type: '砂土', date: '2026-06-17' }
  ];

  if (!localStorage.getItem('zrws_users')) {
    localStorage.setItem('zrws_users', JSON.stringify(mockUsers));
  }
  if (!localStorage.getItem('zrws_missions')) {
    localStorage.setItem('zrws_missions', JSON.stringify(mockMissions));
  }
  if (!localStorage.getItem('zrws_approvals')) {
    localStorage.setItem('zrws_approvals', JSON.stringify(mockApprovals));
  }
  if (!localStorage.getItem('zrws_soil_samples')) {
    localStorage.setItem('zrws_soil_samples', JSON.stringify(mockSoilSamples));
  }
};

const handleLogin = () => {
  if (username.value && password.value) {
    const users = JSON.parse(localStorage.getItem('zrws_users') || '[]');
    const user = users.find(u => u.username === username.value && u.password === password.value);
    
    if (user) {
      localStorage.setItem('token', 'mock-token-' + Date.now());
      localStorage.setItem('currentUser', JSON.stringify(user));
      router.push('/app/dashboard');
    } else {
      alert('用户名或密码错误');
    }
  }
};

onMounted(() => {
  initDefaultData();
  
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
  background: linear-gradient(135deg, var(--sand-500) 0%, var(--sand-400) 100%);
  color: #fff;
  font-family: var(--font-mono);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.3);
  transition: all var(--transition-normal);
  font-size: 14px;
  font-weight: 500;
}

.login-btn:hover {
  background: linear-gradient(135deg, var(--sand-600) 0%, var(--sand-500) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(201, 168, 108, 0.4);
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
</style>
