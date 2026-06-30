/**
 * 智壤卫士 全局状态管理（轻量级，不依赖 pinia，避免引入额外包冲突）
 */
import { reactive } from 'vue'

export const store = reactive({
  // 用户信息
  user: {
    isLogin: false,
    name: '',
    role: '',
    department: '',
    phone: '',
    avatar: '',
    token: ''
  },

  // 全局统计
  stats: {
    missionTotal: 0,
    missionToday: 0,
    pendingApproval: 0,
    soilSamples: 0,
    soilToday: 0
  },

  // 当前任务缓存
  currentMission: null
})

// ============ 方法 ============
export function login(userData) {
  store.user.isLogin = true
  store.user.name = userData.name || '工程师'
  store.user.role = userData.role || '操作员'
  store.user.department = userData.department || '测绘事业部'
  store.user.phone = userData.phone || ''
  store.user.token = userData.token || ''
  try {
    uni.setStorageSync('userInfo', store.user)
    uni.setStorageSync('token', store.user.token)
  } catch (e) {
    console.warn('登录信息持久化失败', e)
  }
}

export function logout() {
  store.user.isLogin = false
  store.user.name = ''
  store.user.role = ''
  store.user.token = ''
  try {
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('token')
  } catch (e) {
    console.warn('清除登录信息失败', e)
  }
}

export function checkLogin() {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.token) {
      Object.assign(store.user, userInfo, { isLogin: true })
      return true
    }
  } catch (e) {}
  return false
}

export function updateStats(newStats) {
  Object.assign(store.stats, newStats)
}

export function setCurrentMission(mission) {
  store.currentMission = mission
}

export default {
  store,
  login,
  logout,
  checkLogin,
  updateStats,
  setCurrentMission
}
