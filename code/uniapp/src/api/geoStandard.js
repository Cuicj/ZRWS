import { request } from './index.js'

// 提取列表数据（后端返回 {list,total} 时取 list）
function pickList(res) {
  if (Array.isArray(res)) return res
  if (res && Array.isArray(res.list)) return res.list
  return res || []
}

// 对齐 GeoStandardController（/api/v1/geo-standards）
export const geoStandardApi = {
  list: (params = {}) => request({ url: '/api/v1/geo-standards', data: params }).then(pickList),
  detail: (id) => request({ url: '/api/v1/geo-standards/' + id }),
  // 后端无 /categories 端点，用全部标准聚合分类
  categories: () => request({ url: '/api/v1/geo-standards' }).then(pickList).then(list => {
    const map = {}
    list.forEach(s => { if (s.category) map[s.category] = (map[s.category] || 0) + 1 })
    return Object.keys(map).map(k => ({ name: k, count: map[k] }))
  })
}

export default geoStandardApi
