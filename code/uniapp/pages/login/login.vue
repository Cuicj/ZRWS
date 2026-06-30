<template>
  <view class="login-page">
    <view class="login-top">
      <view class="logo-circle">🛰️</view>
      <text class="app-title">智壤卫士</text>
      <text class="app-sub">无人机地形数据采集 · 土质分析平台</text>
    </view>

    <view class="login-form">
      <view class="form-item">
        <text class="form-label">账号</text>
        <input
          class="form-input"
          type="text"
          v-model="form.username"
          placeholder="请输入账号"
          placeholder-class="placeholder"
          confirm-type="next" />
      </view>

      <view class="form-item">
        <text class="form-label">密码</text>
        <input
          class="form-input"
          type="password"
          v-model="form.password"
          password
          placeholder="请输入密码"
          placeholder-class="placeholder"
          confirm-type="done"
          @confirm="doLogin" />
      </view>

      <view class="form-extra">
        <label class="checkbox-label">
          <checkbox :checked="form.remember" @change="form.remember = !form.remember" color="#1e3a5f" />
          <text>记住密码</text>
        </label>
        <text class="forget" @tap="onForget">忘记密码？</text>
      </view>

      <zrws-button variant="primary" size="lg" block :loading="loading" @click="doLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </zrws-button>
    </view>

    <view class="login-footer">版本 v1.0.0 · uni-app</view>
  </view>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue'
  import { store, login as storeLogin, checkLogin } from '@/store/user.js'
  import { loginApi } from '@/api/index.js'
  import { toast, nav } from '@/utils/index.js'

  const loading = ref(false)
  const form = reactive({
    username: 'zrws',
    password: '123456',
    remember: true
  })

  onMounted(() => {
    // 自动读取缓存
    try {
      const saved = uni.getStorageSync('loginForm')
      if (saved) {
        Object.assign(form, saved)
      }
    } catch (e) {}

    // 已登录则直接跳转
    if (checkLogin()) {
      setTimeout(() => {
        nav.replace('/pages/dashboard/dashboard')
      }, 300)
    }
  })

  async function doLogin() {
    if (!form.username || !form.password) {
      toast.info('请输入账号密码')
      return
    }
    if (form.password.length < 6) {
      toast.info('密码至少6位')
      return
    }

    loading.value = true

    try {
      const res = await loginApi.login(form.username, form.password)
      const userData = {
        name: res?.name || res?.username || form.username,
        role: res?.role || '外业操作员',
        department: res?.department || '测绘事业部',
        phone: res?.phone || '',
        token: res?.token || res?.accessToken || ''
      }
      storeLogin(userData)

      // 记住账号
      if (form.remember) {
        uni.setStorageSync('loginForm', { username: form.username, password: form.password, remember: true })
      } else {
        uni.removeStorageSync('loginForm')
      }

      toast.success('登录成功')

      setTimeout(() => {
        nav.replace('/pages/dashboard/dashboard')
      }, 600)
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  function onForget() {
    toast.info('请联系管理员重置密码')
  }
</script>

<style lang="scss" scoped>
  .login-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #1e3a5f 0%, #2b6cb0 40%, #f0f2f5 100%);
    display: flex;
    flex-direction: column;
    padding: 120rpx 60rpx 40rpx;
  }
  .login-top {
    text-align: center;
    margin-bottom: 80rpx;
  }
  .logo-circle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    font-size: 80rpx;
    margin-bottom: 30rpx;
  }
  .app-title {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 12rpx;
  }
  .app-sub {
    display: block;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
  }
  .login-form {
    background: #fff;
    border-radius: 24rpx;
    padding: 50rpx 40rpx;
    box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.1);
  }
  .form-item {
    margin-bottom: 36rpx;
  }
  .form-label {
    display: block;
    font-size: 26rpx;
    color: #606266;
    margin-bottom: 12rpx;
  }
  .form-input {
    width: 100%;
    height: 88rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 30rpx;
    color: #303133;
  }
  .placeholder {
    color: #c0c4cc;
  }
  .form-extra {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    font-size: 24rpx;
  }
  .checkbox-label {
    display: inline-flex;
    align-items: center;
    color: #606266;
  }
  .forget {
    color: #2b6cb0;
  }

  .login-footer {
    margin-top: auto;
    padding-top: 60rpx;
    text-align: center;
    color: #c0c4cc;
    font-size: 22rpx;
  }
</style>
