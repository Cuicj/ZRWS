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
        <div class="login-sub mono">ZIRANG SHIELD v1.0</div>
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

// 初始化默认数据
const initDefaultData = () => {
  // Mock用户数据
  const mockUsers = [
    { id: 'U001', username: 'admin', password: 'admin', name: '技术管理员', role: 'admin', dept: '技术部' },
    { id: 'U002', username: 'user', password: 'user', name: '普通用户', role: 'user', dept: '业务部' },
    { id: 'U003', username: 'approver', password: 'approver', name: '审批员', role: 'approver', dept: '审批部' }
  ];

  // Mock飞行任务数据
  const mockMissions = [
    { id: 'ZRS-2026-0617-001', area: '望城区乔口镇', status: 'completed', date: '2026-06-17', operator: '王工', coverage: 860 },
    { id: 'ZRS-2026-0616-003', area: '岳麓区莲花镇', status: 'completed', date: '2026-06-16', operator: '李工', coverage: 1250 },
    { id: 'ZRS-2026-0616-002', area: '雨花区跳马镇', status: 'processing', date: '2026-06-16', operator: '王工', coverage: 680 },
    { id: 'ZRS-2026-0615-001', area: '开福区青竹湖', status: 'completed', date: '2026-06-15', operator: '张工', coverage: 520 },
    { id: 'ZRS-2026-0614-002', area: '天心区暮云镇', status: 'abnormal', date: '2026-06-14', operator: '李工', coverage: 320 }
  ];

  // Mock审批流程数据
  const mockApprovals = [
    { id: 'APR-001', title: '2026年度测绘项目审批', status: 'pending', applicant: '王工', createTime: '2026-06-17' },
    { id: 'APR-002', title: '无人机设备采购审批', status: 'approved', applicant: '李工', createTime: '2026-06-16' },
    { id: 'APR-003', title: '数据处理服务合同审批', status: 'rejected', applicant: '张工', createTime: '2026-06-15' }
  ];

  // Mock土壤采样数据
  const mockSoilSamples = [
    { id: 'SP-001', lat: 28.45672, lng: 112.83521, pH: 6.8, moisture: 0.32, ec: 245, type: '壤土', date: '2026-06-17' },
    { id: 'SP-002', lat: 28.45718, lng: 112.83605, pH: 7.2, moisture: 0.45, ec: 312, type: '黏土', date: '2026-06-17' },
    { id: 'SP-003', lat: 28.45801, lng: 112.83489, pH: 5.9, moisture: 0.18, ec: 178, type: '砂土', date: '2026-06-17' }
  ];

  // 存储到localStorage
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
    // 验证用户
    const users = JSON.parse(localStorage.getItem('zrws_users') || '[]');
    const user = users.find(u => u.username === username.value && u.password === password.value);
    
    if (user) {
      // 保存token和用户信息
      localStorage.setItem('token', 'mock-token-' + Date.now());
      localStorage.setItem('currentUser', JSON.stringify(user));
      router.push('/app/dashboard');
    } else {
      alert('用户名或密码错误');
    }
  }
};

onMounted(() => {
  // 页面加载时初始化默认数据
  initDefaultData();
  
  // 如果已有token，直接跳转到首页
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
  background-image: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=aerial%20view%20of%20agricultural%20fields%20with%20soil%20texture%20patterns%2C%20dark%20moody%20atmosphere%2C%20golden%20hour%20lighting%2C%20topographic%20contour%20lines%20overlay%2C%20deep%20earthy%20tones%2C%20charcoal%20grey%20and%20warm%20amber%20accents%2C%20cinematic%20landscape%2C%20ultra%20detailed%2C%208k%20quality&image_size=landscape_16_9');
  background-size: cover;
  background-position: center;
  filter: brightness(0.25) contrast(1.1) saturate(0.8);
  animation: bgKenBurns 20s ease-in-out infinite alternate;
}

@keyframes bgKenBurns {
  0% {
    transform: scale(1) translate(0, 0);
  }
  100% {
    transform: scale(1.08) translate(-1%, -1%);
  }
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 70%, rgba(201, 164, 92, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 30%, rgba(74, 124, 158, 0.12) 0%, transparent 50%),
    linear-gradient(180deg, rgba(15, 18, 24, 0.85) 0%, rgba(15, 18, 24, 0.95) 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(61, 68, 82, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(61, 68, 82, 0.3) 1px, transparent 1px);
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
  box-shadow: 0 0 6px var(--sand-500);
  animation: floatUp linear infinite;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.4;
  }
  100% {
    transform: translateY(-100vh) translateX(30px);
    opacity: 0;
  }
}

.login-card {
  width: 420px;
  padding: var(--s-7);
  background: rgba(28, 34, 46, 0.85);
  border: var(--line);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  z-index: 2;
  animation: cardFadeIn 0.8s ease-out;
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
  background: linear-gradient(135deg, var(--sand-500) 0%, var(--sand-600) 100%);
  margin-bottom: var(--s-4);
  animation: logoPulse 3s ease-in-out infinite;
  box-shadow: 0 0 30px rgba(201, 164, 92, 0.3);
}

@keyframes logoPulse {
  0%, 100% {
    box-shadow: 0 0 30px rgba(201, 164, 92, 0.3);
  }
  50% {
    box-shadow: 0 0 50px rgba(201, 164, 92, 0.5);
  }
}

.brand-mark {
  font-size: 32px;
  color: var(--ink-900);
  display: block;
  line-height: 1;
}

.login-title {
  font-size: 32px;
  font-weight: 200;
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

.login-btn {
  width: 100%;
  background: var(--sand-500);
  color: var(--ink-900);
  font-family: var(--font-mono);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition-fast);
}

.login-btn:hover {
  background: var(--sand-400);
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(201, 164, 92, 0.3);
}

.btn-arrow {
  transition: transform var(--transition-fast);
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
