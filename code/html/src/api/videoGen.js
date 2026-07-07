import request from '@/utils/request'

export const videoGenApi = {
  getConfig: () => request({ url: '/v1/video-gen/config', method: 'GET' }),
  generateVideo: (data) => request({ url: '/v1/video-gen/generate', method: 'POST', data }),
  getTask: (taskId) => request({ url: `/v1/video-gen/task/${taskId}`, method: 'GET' }),
  getTaskByNo: (taskNo) => request({ url: `/v1/video-gen/task/no/${taskNo}`, method: 'GET' }),
  listTasks: (params) => request({ url: '/v1/video-gen/tasks', method: 'GET', params }),
  cancelTask: (taskId) => request({ url: `/v1/video-gen/task/${taskId}/cancel`, method: 'POST' }),
  downloadUrl: (taskId) => `/approval/api/v1/video-gen/download/${taskId}`,
  playUrl: (taskId) => `/approval/api/v1/video-gen/play/${taskId}`
}

export default videoGenApi
