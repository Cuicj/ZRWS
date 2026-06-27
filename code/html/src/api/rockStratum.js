import request from '@/utils/request';

export function getAnalysisList() {
  return request({
    url: '/v1/rock-stratum/analyses',
    method: 'get'
  });
}

export function getAnalysis(id) {
  return request({
    url: `/v1/rock-stratum/analyses/${id}`,
    method: 'get'
  });
}

export function createAnalysis(data) {
  return request({
    url: '/v1/rock-stratum/analyses',
    method: 'post',
    data
  });
}

export function updateAnalysis(id, data) {
  return request({
    url: `/v1/rock-stratum/analyses/${id}`,
    method: 'put',
    data
  });
}

export function deleteAnalysis(id) {
  return request({
    url: `/v1/rock-stratum/analyses/${id}`,
    method: 'delete'
  });
}

export function getSampleList(analysisId) {
  return request({
    url: '/v1/rock-stratum/samples',
    method: 'get',
    params: { analysisId }
  });
}

export function getSample(id) {
  return request({
    url: `/v1/rock-stratum/samples/${id}`,
    method: 'get'
  });
}

export function createSample(data) {
  return request({
    url: '/v1/rock-stratum/samples',
    method: 'post',
    data
  });
}

export function updateSample(id, data) {
  return request({
    url: `/v1/rock-stratum/samples/${id}`,
    method: 'put',
    data
  });
}

export function deleteSample(id) {
  return request({
    url: `/v1/rock-stratum/samples/${id}`,
    method: 'delete'
  });
}

export function getStandardList(params) {
  return request({
    url: '/v1/geo-standards',
    method: 'get',
    params
  });
}

export function getStandard(id) {
  return request({
    url: `/v1/geo-standards/${id}`,
    method: 'get'
  });
}

export function getStandardsByCategory(category) {
  return request({
    url: `/v1/geo-standards/category/${category}`,
    method: 'get'
  });
}

export function searchStandards(keyword) {
  return request({
    url: '/v1/geo-standards/search',
    method: 'get',
    params: { keyword }
  });
}
