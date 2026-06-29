import { request } from './index.js'

export const rockStratumApi = {
  analysis: (params = {}) => request({ url: '/api/v1/rock-stratum/analysis', data: params }),
  sampleList: (params = {}) => request({ url: '/api/v1/rock-stratum/samples', data: params }),
  standards: (params = {}) => request({ url: '/api/v1/rock-stratum/standards', data: params })
}

export default rockStratumApi
