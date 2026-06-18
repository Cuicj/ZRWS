<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-card">
      <div class="login-head">
        <span class="brand-mark">◐</span>
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
        <el-button type="primary" size="large" class="login-btn" @click="handleLogin">登录</el-button>
      </el-form>

      <div class="login-hint mono">
        <span>演示账号: admin / admin</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const username = ref('admin');
const password = ref('admin');
const role = ref('admin');

const handleLogin = () => {
  if (username.value && password.value) {
    router.push('/app/dashboard');
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 30% 70%, rgba(201, 164, 92, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 70% 30%, rgba(74, 124, 158, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.login-card {
  width: 400px;
  padding: var(--s-7);
  background: var(--ink-800);
  border: var(--line);
  position: relative;
  z-index: 2;
}

.login-head {
  text-align: center;
  margin-bottom: var(--s-6);
}

.brand-mark {
  font-size: 48px;
  color: var(--sand-500);
  display: block;
  margin-bottom: var(--s-3);
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
}

.login-btn:hover {
  background: var(--sand-400);
}

.login-hint {
  text-align: center;
  font-size: 11px;
  color: var(--signal-dim);
}
</style>