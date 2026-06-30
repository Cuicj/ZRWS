import request from '@/utils/request'

export const exportApi = {
  createTask: (data) => request({ url: '/v1/export/create', method: 'POST', data }),
  executeTask: (taskId) => request({ url: `/v1/export/execute/${taskId}`, method: 'POST' }),
  downloadUrl: (taskId) => `/v1/export/download/${taskId}`,
  listTasks: (params) => request({ url: '/v1/export/tasks', data: params }),
  getBoList: () => request({ url: '/v1/dataanalyzer/bo' })
}

export default exportApi
