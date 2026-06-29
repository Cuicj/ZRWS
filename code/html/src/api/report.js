import request from '@/utils/request'

export const reportApi = {
  listTemplates: (params) => request({ url: '/api/v1/report/templates', data: params }),
  getTemplate: (code) => request({ url: `/api/v1/report/templates/${code}` }),
  generateReport: (data) => request({ url: '/api/v1/report/generate', method: 'POST', data }),
  createTemplate: (data) => request({ url: '/api/v1/report/templates', method: 'POST', data }),
  updateTemplate: (id, data) => request({ url: `/api/v1/report/templates/${id}`, method: 'PUT', data }),
  deleteTemplate: (id) => request({ url: `/api/v1/report/templates/${id}`, method: 'DELETE' })
}

export default reportApi
