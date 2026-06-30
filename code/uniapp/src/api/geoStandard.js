import { request } from './index.js'

export const geoStandardApi = {
  list: (params = {}) => request({ url: '/api/v1/geo-standards', data: params }),
  detail: (id) => request({ url: '/api/v1/geo-standards/' + id }),
  categories: () => request({ url: '/api/v1/geo-standards/categories' })
}

export default geoStandardApi
