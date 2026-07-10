import { request } from './index.js'

// 提取列表数据（后端返回 {list,total} 时取 list）
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.list)) return res.list
  return res || []
}

// 对齐 DroneController（/api/v1/drone/devices），后端无独立 DeviceController
export const deviceApi = {
  list: (params = {}) => request({ url: '/api/v1/drone/devices', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/drone/devices' }).then(pickList).then(arr => arr.find(d => d.deviceId == id) || arr[0] || null),
  stats: () => request({ url: '/api/v1/drone/devices' }).then(pickList).then(arr => ({
    total: arr.length,
    online: arr.filter(d => d.isConnected || d.status === 'ONLINE').length
  }))
}

export default deviceApi
