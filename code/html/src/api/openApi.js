import request from '@/utils/request'

export const openApi = {
  listCompanies: () => request({ url: '/api/v1/external-company/list' }),
  createCompany: (data) => request({ url: '/api/v1/external-company/create', method: 'POST', data }),
  listApiKeys: () => request({ url: '/api/v1/external-company/api-keys' }),
  generateApiKey: (companyId) => request({ url: `/api/v1/external-company/${companyId}/api-key`, method: 'POST' }),
  revokeApiKey: (keyId) => request({ url: `/api/v1/external-company/api-key/${keyId}/revoke`, method: 'POST' })
}

export default openApi
