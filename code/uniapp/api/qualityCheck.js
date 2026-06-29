import { request } from './index.js'

export const qualityCheckApi = {
  list: (params = {}) => request({ url: '/api/v1/quality-check/list', data: params }),
  detail: (id) => request({ url: '/api/v1/quality-check/' + id }),
  stats: () => request({ url: '/api/v1/quality-check/stats' })
}

export default qualityCheckApi
