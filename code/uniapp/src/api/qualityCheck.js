import { request } from './index.js'

// 注意：后端暂无 QualityCheckController，以下接口调用会返回 404。
// 前端页面应做 try/catch 并使用 mock 数据兜底（见 utils/mock.js）。
export const qualityCheckApi = {
  list: (params = {}) => request({ url: '/api/v1/quality-check/list', data: params }),
  detail: (id) => request({ url: '/api/v1/quality-check/' + id }),
  stats: () => request({ url: '/api/v1/quality-check/stats' })
}

export default qualityCheckApi
