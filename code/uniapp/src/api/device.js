import { request } from './index.js'

export const deviceApi = {
  list: (params = {}) => request({ url: '/api/v1/devices', data: params }),
  detail: (id) => request({ url: '/api/v1/devices/' + id }),
  stats: () => request({ url: '/api/v1/devices/stats' })
}

export default deviceApi
