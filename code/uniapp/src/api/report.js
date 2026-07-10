import { request } from './index.js'

// 提取列表数据（后端返回 {templates,total} 时取 templates）
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.templates)) return res.templates
  if (res && Array.isArray(res.list)) return res.list
  return res || []
}

// 对齐 ReportController（/api/v1/report）
export const reportApi = {
  templates: (params = {}) => request({ url: '/api/v1/report/templates', data: params }).then(pickList),
  generate: (data = {}) => request({ url: '/api/v1/report/generate', method: 'POST', data }),
  // 后端无 /history 端点，复用模板列表兜底
  history: (params = {}) => request({ url: '/api/v1/report/templates', data: params }).then(pickList)
}

export default reportApi
