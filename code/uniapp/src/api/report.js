import { request } from './index.js'

export const reportApi = {
  templates: (params = {}) => request({ url: '/api/v1/report/templates', data: params }),
  generate: (data = {}) => request({ url: '/api/v1/report/generate', method: 'POST', data }),
  history: (params = {}) => request({ url: '/api/v1/report/history', data: params })
}

export default reportApi
