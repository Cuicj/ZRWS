import { request } from './index.js'

export const soilClassifyApi = {
  list: (params = {}) => request({ url: '/api/v1/soil-classify/list', data: params }),
  detail: (id) => request({ url: '/api/v1/soil-classify/' + id }),
  stats: () => request({ url: '/api/v1/soil-classify/stats' })
}

export default soilClassifyApi
