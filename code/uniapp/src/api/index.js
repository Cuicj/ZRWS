/**
 * 智壤卫士 API 请求封装
 */

// 开发环境使用本地后端，生产环境使用真实域名
const BASE_URL = process.env.NODE_ENV === 'development' 
  ? 'http://localhost:5571/approval' 
  : 'https://www.zrws.cloud'
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
export function request(options) {
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
    url: '/api/v1/auth/login',
    method: 'POST',
    data: { username, password }
  }),
  logout: () => request({ url: '/api/v1/auth/logout', method: 'POST' }),
  getUserInfo: () => request({ url: '/api/v1/auth/userinfo' })
}

// ============ 菜单 ============
export const menuApi = {
  tree: () => request({ url: '/api/v1/menu/tree' })
}

// ============ 任务管理 ============
export const missionApi = {
  list: (params = {}) => request({ url: '/api/v1/missions', data: params }),
  detail: (id) => request({ url: '/api/v1/missions/' + id }),
  create: (data) => request({ url: '/api/v1/missions', method: 'POST', data }),
  update: (id, data) => request({ url: '/api/v1/missions/' + id, method: 'PUT', data }),
  delete: (id) => request({ url: '/api/v1/missions/' + id, method: 'DELETE' })
}

// ============ 飞行控制 ============
export const flightApi = {
  list: (params = {}) => request({ url: '/api/v1/drone/list', data: params }),
  getStatus: (droneId) => request({ url: '/api/v1/drone/' + droneId + '/status' }),
  start: (missionId) => request({ url: '/api/v1/drone/mission/start', method: 'POST', data: { missionId } }),
  pause: (missionId) => request({ url: '/api/v1/drone/mission/pause', method: 'POST', data: { missionId } }),
  returnHome: (missionId) => request({ url: '/api/v1/drone/mission/return', method: 'POST', data: { missionId } }),
  telemetry: (droneId) => request({ url: '/api/v1/drone/' + droneId + '/telemetry' })
}

// ============ GPS 航迹 ============
export const gpsApi = {
  realtime: () => request({ url: '/api/v1/gps/realtime' }),
  track: (missionId) => request({ url: '/api/v1/gps/track/' + missionId }),
  export: (missionId) => request({ url: '/api/v1/gps/export/' + missionId })
}

// ============ 土壤采样 ============
export const soilApi = {
  list: (params = {}) => request({ url: '/api/v1/soil/samples', data: params }),
  create: (data) => request({ url: '/api/v1/soil/samples', method: 'POST', data }),
  stats: () => request({ url: '/api/v1/soil/stats' })
}

// ============ 面积测量 ============
export const areaApi = {
  list: () => request({ url: '/api/v1/area/records' }),
  create: (data) => request({ url: '/api/v1/area/records', method: 'POST', data }),
  calc: (points) => request({ url: '/api/v1/area/calc', method: 'POST', data: { points } })
}

// ============ 灾害评估 ============
export const disasterApi = {
  list: (params = {}) => request({ url: '/api/v1/disaster/list', data: params }),
  summary: () => request({ url: '/api/v1/disaster/summary' }),
  detail: (type) => request({ url: '/api/v1/disaster/' + type })
}

// ============ 审批中心 ============
export const approvalApi = {
  list: (params = {}) => request({ url: '/api/v1/approval/list', data: params }),
  pending: () => request({ url: '/api/v1/approval/pending' }),
  done: () => request({ url: '/api/v1/approval/done' }),
  approve: (id, comment = '') => request({
    url: '/api/v1/approval/' + id + '/approve',
    method: 'POST',
    data: { comment }
  }),
  reject: (id, reason) => request({
    url: '/api/v1/approval/' + id + '/reject',
    method: 'POST',
    data: { reason }
  })
}

// ============ 仪表盘 ============
export const dashboardApi = {
  stats: () => request({ url: '/api/v1/dashboard/stats' }),
  recent: () => request({ url: '/api/v1/dashboard/recent' })
}

// ============ 土质分类 ============
export { soilClassifyApi } from './soilClassify.js'

// ============ 岩层分析 ============
export { rockStratumApi } from './rockStratum.js'

// ============ 设备管理 ============
export { deviceApi } from './device.js'

// ============ 质量校验 ============
export { qualityCheckApi } from './qualityCheck.js'

// ============ 地质标准 ============
export { geoStandardApi } from './geoStandard.js'

// ============ 报表中心 ============
export { reportApi } from './report.js'

export default {
  request,
  BASE_URL,
  ...loginApi,
  ...menuApi,
  ...missionApi,
  ...flightApi,
  ...gpsApi,
  ...soilApi,
  ...areaApi,
  ...disasterApi,
  ...approvalApi,
  ...dashboardApi
}
