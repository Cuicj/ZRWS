/**
 * 智壤卫士 API 请求封装
 * 接口路径对齐后端 Controller(zrws-approval)
 */

// 线上环境
const BASE_URL = 'https://www.zrws.cloud/approval'
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
          if (resp && (resp.code === 200 || resp.success === true)) {
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
              title: resp?.msg || resp?.message || '请求失败',
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

// 提取列表数据：后端多数分页接口返回 {list,total,...}，统一取 list 便于页面遍历
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.list)) return res.list
  if (res && Array.isArray(res.templates)) return res.templates
  return res
}

// 获取当前审批人信息（从本地存储）
function getApprover() {
  try {
    const u = uni.getStorageSync('userInfo') || {}
    return {
      id: u.id || u.userId || 0,
      name: u.name || u.username || '操作员'
    }
  } catch (e) {
    return { id: 0, name: '操作员' }
  }
}

// ============ 登录认证 ============
export const loginApi = {
  login: (username, password, captchaUuid, captcha) => request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data: { username, password, captchaUuid, captcha }
  }),
  logout: () => request({ url: '/api/v1/auth/logout', method: 'POST' }),
  getUserInfo: () => request({ url: '/api/v1/auth/info' }),
  getCaptcha: () => request({ url: '/api/v1/auth/captcha' })
}

// ============ 菜单 ============
export const menuApi = {
  tree: () => request({ url: '/api/v1/menu/tree' })
}

// ============ 任务管理 ============
// 后端无独立 MissionController，复用 DroneController 飞行任务接口（/api/v1/drone/missions）
export const missionApi = {
  list: (params = {}) => request({ url: '/api/v1/drone/missions', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/drone/missions', data: { missionId: id } }).then(pickList),
  create: (data) => request({ url: '/api/v1/drone/missions/waypoint', method: 'POST', data }),
  update: (id, data) => request({ url: '/api/v1/drone/missions/' + id, method: 'PUT', data }),
  delete: (id) => request({ url: '/api/v1/drone/missions/' + id, method: 'DELETE' })
}

// ============ 飞行控制 ============
// 对齐 DroneController（/api/v1/drone）
export const flightApi = {
  list: (params = {}) => request({ url: '/api/v1/drone/devices', data: params }).then(pickList),
  getStatus: (droneId) => request({ url: '/api/v1/drone/telemetry/' + droneId }),
  start: (droneId) => request({ url: '/api/v1/drone/missions/' + droneId + '/start', method: 'POST' }),
  pause: (droneId) => request({ url: '/api/v1/drone/missions/' + droneId + '/stop', method: 'POST' }),
  returnHome: (droneId) => request({ url: '/api/v1/drone/missions/' + droneId + '/return', method: 'POST' }),
  telemetry: (droneId) => request({ url: '/api/v1/drone/telemetry/' + droneId })
}

// ============ GPS 航迹 ============
// 对齐 GpsTrackController（/api/v1/gps-track）
export const gpsApi = {
  realtime: () => request({ url: '/api/v1/gps-track/list' }).then(pickList),
  track: (missionId) => request({ url: '/api/v1/gps-track/list', data: { missionId } }).then(pickList),
  export: (trackId) => request({ url: '/api/v1/gps-track/' + trackId })
}

// ============ 土壤采样 ============
// 对齐 SoilSampleController（/api/v1/soil-sample）
export const soilApi = {
  list: (params = {}) => request({ url: '/api/v1/soil-sample/list', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/soil-sample/' + id }),
  create: (data) => request({ url: '/api/v1/soil-sample', method: 'POST', data }),
  update: (id, data) => request({ url: '/api/v1/soil-sample/' + id, method: 'PUT', data }),
  delete: (id) => request({ url: '/api/v1/soil-sample/' + id, method: 'DELETE' }),
  stats: () => request({ url: '/api/v1/soil-sample/list', data: { pageSize: 1 } })
}

// ============ 面积测量 ============
// 对齐 LandPlotController（/api/v1/land-plot）
export const areaApi = {
  list: (params = {}) => request({ url: '/api/v1/land-plot/list', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/land-plot/' + id }),
  create: (data) => request({ url: '/api/v1/land-plot', method: 'POST', data }),
  calc: (points) => request({ url: '/api/v1/land-plot/area-stats' })
}

// ============ 灾害评估 ============
// 对齐 DisasterRiskController（/api/v1/disaster-risk）
export const disasterApi = {
  list: (params = {}) => request({ url: '/api/v1/disaster-risk/list', data: params }).then(pickList),
  summary: () => request({ url: '/api/v1/disaster-risk/stats' }),
  detail: (id) => request({ url: '/api/v1/disaster-risk/' + id })
}

// ============ 审批中心 ============
// 对齐 ApprovalController（base /api/v1，无 approval 前缀）
export const approvalApi = {
  list: (params = {}) => {
    const a = getApprover()
    return request({ url: '/api/v1/todo', data: { assignee: String(a.id), ...params } }).then(pickList)
  },
  pending: () => {
    const a = getApprover()
    return request({ url: '/api/v1/todo', data: { assignee: String(a.id) } }).then(pickList)
  },
  done: () => {
    const a = getApprover()
    return request({ url: '/api/v1/done', data: { assignee: String(a.id) } }).then(pickList)
  },
  approve: (id, comment = '') => {
    const a = getApprover()
    const q = `?approverId=${a.id}&approverName=${encodeURIComponent(a.name)}&opinion=${encodeURIComponent(comment || '同意')}`
    return request({ url: '/api/v1/' + id + '/approve' + q, method: 'POST' })
  },
  reject: (id, reason) => {
    const a = getApprover()
    const q = `?approverId=${a.id}&approverName=${encodeURIComponent(a.name)}&reason=${encodeURIComponent(reason || '驳回')}`
    return request({ url: '/api/v1/' + id + '/reject' + q, method: 'POST' })
  }
}

// ============ 仪表盘 ============
// 后端暂无独立 Dashboard 聚合接口，复用现有模块拼装统计
export const dashboardApi = {
  stats: () => Promise.all([
    request({ url: '/api/v1/drone/missions' }).then(pickList).catch(() => []),
    request({ url: '/api/v1/soil-sample/list', data: { pageSize: 1 } }).catch(() => ({ total: 0 })),
    request({ url: '/api/v1/drone/devices' }).then(pickList).catch(() => []),
    request({ url: '/api/v1/todo', data: { assignee: String(getApprover().id) } }).then(pickList).catch(() => [])
  ]).then(([missions, soil, devices, todos]) => ({
    missionTotal: missions.length,
    missionToday: 0,
    pendingApproval: todos.length,
    soilSamples: soil.total || 0,
    soilToday: 0,
    droneOnline: devices.filter(d => d.isConnected || d.status === 'ONLINE').length
  })),
  recent: () => request({ url: '/api/v1/drone/missions' }).then(pickList).catch(() => [])
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
