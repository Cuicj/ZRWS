import request from '@/utils/request'

export const openApi = {
  listCompanies: () => request({ url: '/v1/external-company/list' }),
  createCompany: (data) => request({ url: '/v1/external-company/create', method: 'POST', data }),
  listApiKeys: () => request({ url: '/v1/external-company/api-keys' }),
  generateApiKey: (companyId) => request({ url: `/v1/external-company/${companyId}/api-key`, method: 'POST' }),
  revokeApiKey: (keyId) => request({ url: `/v1/external-company/api-key/${keyId}/revoke`, method: 'POST' })
}

export default openApi
