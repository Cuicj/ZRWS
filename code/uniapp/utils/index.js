/**
 * 智壤卫士 工具函数
 */

// 格式化时间
export function formatDate(date, format = 'YYYY-MM-DD') {
  const d = typeof date === 'object' ? date : new Date(date)
  const pad = (n) => (n < 10 ? '0' + n : String(n))
  const map = {
    YYYY: d.getFullYear(),
    MM: pad(d.getMonth() + 1),
    DD: pad(d.getDate()),
    HH: pad(d.getHours()),
    mm: pad(d.getMinutes()),
    ss: pad(d.getSeconds())
  }
  return format.replace(/YYYY|MM|DD|HH|mm|ss/g, (match) => map[match])
}

export function now(format = 'YYYY-MM-DD HH:mm:ss') {
  return formatDate(new Date(), format)
}

// 防抖
export function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

// 节流
export function throttle(fn, delay = 300) {
  let last = 0
  return function(...args) {
    const now = Date.now()
    if (now - last > delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

// 多边形面积计算（经纬度 → 平方米 → 亩）
export function polygonArea(points) {
  if (!points || points.length < 3) return 0
  let area = 0
  const n = points.length
  for (let i = 0; i < n; i++) {
    const j = (i + 1) % n
    area += points[i].lng * points[j].lat
    area -= points[j].lng * points[i].lat
  }
  // 地球半径近似 + 度转米
  const sqMeters = Math.abs(area / 2) * 111319.9 * 110574.3
  return {
    sqMeters: Math.round(sqMeters),
    mu: +(sqMeters / 666.6667).toFixed(2)
  }
}

// Toast 快捷方法
export const toast = {
  success: (msg) => uni.showToast({ title: msg, icon: 'success', duration: 1500 }),
  error: (msg) => uni.showToast({ title: msg, icon: 'none', duration: 2000 }),
  info: (msg) => uni.showToast({ title: msg, icon: 'none', duration: 1500 }),
  loading: (msg = '加载中...') => uni.showLoading({ title: msg, mask: true }),
  hide: () => uni.hideLoading()
}

// 路由跳转
export const nav = {
  to: (url) => uni.navigateTo({ url }),
  back: () => uni.navigateBack(),
  replace: (url) => uni.redirectTo({ url }),
  tab: (url) => uni.switchTab({ url })
}

// 确认对话框
export function confirm(title, content, options = {}) {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title,
      content,
      confirmText: options.confirmText || '确定',
      cancelText: options.cancelText || '取消',
      confirmColor: options.confirmColor || '#1e3a5f',
      success: (res) => {
        if (res.confirm) resolve(true)
        else reject(false)
      },
      fail: () => reject(false)
    })
  })
}

// 输入对话框
export function prompt(title, placeholder = '') {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      editable: true,
      placeholderText: placeholder,
      success: (res) => {
        if (res.confirm) resolve(res.content || '')
        else resolve(null)
      }
    })
  })
}

// 距离计算（两点间经纬度距离，单位: 米）
export function distance(p1, p2) {
  const R = 6371000
  const toRad = (deg) => (deg * Math.PI) / 180
  const dLat = toRad(p2.lat - p1.lat)
  const dLng = toRad(p2.lng - p1.lng)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(p1.lat)) * Math.cos(toRad(p2.lat)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

// 数字格式化（千分位）
export function formatNumber(n, decimals = 0) {
  const num = Number(n) || 0
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}

// 状态映射辅助
export const statusHelper = {
  mission: {
    text: (s) => ({ processing: '执行中', pending: '待执行', completed: '已完成', abnormal: '异常' }[s] || s),
    color: (s) => ({ processing: 'primary', pending: 'warning', completed: 'success', abnormal: 'danger' }[s] || 'info')
  }
}

export default {
  formatDate,
  now,
  debounce,
  throttle,
  polygonArea,
  toast,
  nav,
  confirm,
  prompt,
  distance,
  formatNumber,
  statusHelper
}
