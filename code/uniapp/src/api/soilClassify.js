import { request } from './index.js'

// 提取列表数据（后端返回 {list,total} 时取 list）
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.list)) return res.list
  return res || []
}

// 对齐 SoilClassificationController（/api/v1/soil-classification）
export const soilClassifyApi = {
  list: (params = {}) => request({ url: '/api/v1/soil-classification/list', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/soil-classification/' + id }),
  // 后端无 /stats 端点，用 /history 兜底
  stats: () => request({ url: '/api/v1/soil-classification/history' }).then(pickList)
}

export default soilClassifyApi
