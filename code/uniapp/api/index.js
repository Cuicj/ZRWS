/**
 * 智壤卫士 API 请求封装
 */

const BASE_URL = 'https://api.zrws.example.com'
const TIMEOUT = 15000

// 获取 token
function getToken() {
  try {
    return uni.getStorageSync('token') || ''
  } catch (e) {
    return ''
  }
}

// 通用请求
function request(options) {
  return new Promise((resolve, reject) => {
    const { url, method = 'GET', data = {}, header = {} } = options

    uni.request({
      url: url.startsWith('http') ? url : BASE_URL + url,
      method,
      data,
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        'Authorization': getToken() ? 'Bearer ' + getToken() : '',
        ...header
      },
      success: (res) => {
        const { statusCode, data: resp } = res
        if (statusCode === 200) {
          if (resp && resp.code === 0) {
            resolve(resp.data)
          } else if (resp && resp.code === 401) {
            uni.showToast({ title: '请先登录', icon: 'none' })
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            setTimeout(() => {
              uni.redirectTo({ url: '/pages/login/login' })
            }, 1000)
            reject(resp)
          } else {
            uni.showToast({
              title: resp?.message || '请求失败',
              icon: 'none'
            })
            reject(resp)
          }
        } else {
          uni.showToast({ title: '网络异常 (' + statusCode + ')', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// ============ 登录认证 ============
export const loginApi = {
  login: (username, password) => request({
    url: '/api/auth/login',
    method: 'POST',
    data: { username, password }
  }),
  logout: () => request({ url: '/api/auth/logout', method: 'POST' }),
  getUserInfo: () => request({ url: '/api/auth/userinfo' })
}

// ============ 任务管理 ============
export const missionApi = {
  list: (params = {}) => request({ url: '/api/missions', data: params }),
  detail: (id) => request({ url: '/api/missions/' + id }),
  create: (data) => request({ url: '/api/missions', method: 'POST', data }),
  update: (id, data) => request({ url: '/api/missions/' + id, method: 'PUT', data }),
  delete: (id) => request({ url: '/api/missions/' + id, method: 'DELETE' })
}

// ============ 飞行控制 ============
export const flightApi = {
  getStatus: (droneId) => request({ url: '/api/flight/status/' + droneId }),
  start: (missionId) => request({ url: '/api/flight/start', method: 'POST', data: { missionId } }),
  pause: (missionId) => request({ url: '/api/flight/pause', method: 'POST', data: { missionId } }),
  returnHome: (missionId) => request({ url: '/api/flight/return', method: 'POST', data: { missionId } }),
  telemetry: (droneId) => request({ url: '/api/flight/telemetry/' + droneId })
}

// ============ GPS 航迹 ============
export const gpsApi = {
  realtime: () => request({ url: '/api/gps/realtime' }),
  track: (missionId) => request({ url: '/api/gps/track/' + missionId }),
  export: (missionId) => request({ url: '/api/gps/export/' + missionId })
}

// ============ 土壤采样 ============
export const soilApi = {
  list: (params = {}) => request({ url: '/api/soil/samples', data: params }),
  create: (data) => request({ url: '/api/soil/samples', method: 'POST', data }),
  stats: () => request({ url: '/api/soil/stats' })
}

// ============ 面积测量 ============
export const areaApi = {
  list: () => request({ url: '/api/area/records' }),
  create: (data) => request({ url: '/api/area/records', method: 'POST', data }),
  calc: (points) => request({ url: '/api/area/calc', method: 'POST', data: { points } })
}

// ============ 灾害评估 ============
export const disasterApi = {
  summary: () => request({ url: '/api/disaster/summary' }),
  detail: (type) => request({ url: '/api/disaster/' + type })
}

// ============ 审批中心 ============
export const approvalApi = {
  pending: () => request({ url: '/api/approvals/pending' }),
  done: () => request({ url: '/api/approvals/done' }),
  approve: (id, comment = '') => request({
    url: '/api/approvals/' + id + '/approve',
    method: 'POST',
    data: { comment }
  }),
  reject: (id, reason) => request({
    url: '/api/approvals/' + id + '/reject',
    method: 'POST',
    data: { reason }
  })
}

// ============ 仪表盘 ============
export const dashboardApi = {
  stats: () => request({ url: '/api/dashboard/stats' }),
  recent: () => request({ url: '/api/dashboard/recent' })
}

export default {
  request,
  BASE_URL,
  ...loginApi,
  ...missionApi,
  ...flightApi,
  ...gpsApi,
  ...soilApi,
  ...areaApi,
  ...disasterApi,
  ...approvalApi,
  ...dashboardApi
}
