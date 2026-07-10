import { request } from './index.js'

// 提取列表数据（后端返回 {list,total} 时取 list）
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.list)) return res.list
  return res || []
}

// 对齐 RockStratumController（/api/v1/rock-stratum）
export const rockStratumApi = {
  // 后端为复数 /analyses
  analysis: (params = {}) => request({ url: '/api/v1/rock-stratum/analyses', data: params }).then(pickList),
  sampleList: (params = {}) => request({ url: '/api/v1/rock-stratum/samples', data: params }).then(pickList),
  // 后端无 /standards 端点，复用 analyses 兜底
  standards: (params = {}) => request({ url: '/api/v1/rock-stratum/analyses', data: params }).then(pickList)
}

export default rockStratumApi
