import request from '@/utils/request'

export const geoStandardApi = {
  list: (params) => {
    return request({ url: '/api/v1/geo-standards', method: 'GET', params })
  },

  getById: (id) => {
    return request({ url: `/api/v1/geo-standards/${id}`, method: 'GET' })
  },

  getByCategory: (category) => {
    return request({ url: `/api/v1/geo-standards/category/${category}`, method: 'GET' })
  },

  search: (keyword) => {
    return request({ url: '/api/v1/geo-standards/search', method: 'GET', params: { keyword } })
  },

  classify: (params) => {
    return request({ url: '/api/v1/geo-standards/ai/classify', method: 'POST', data: params })
  },

  compare: (params) => {
    return request({ url: '/api/v1/geo-standards/ai/compare', method: 'POST', data: params })
  },

  generateDiagram: (standardId) => {
    return request({ url: `/api/v1/geo-standards/ai/diagram/${standardId}`, method: 'POST' })
  }
}

export default geoStandardApi
