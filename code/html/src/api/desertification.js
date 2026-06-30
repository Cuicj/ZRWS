import request from '@/utils/request';

export function getDesertificationList(params) {
  return request({
    url: '/v1/desertification/list',
    method: 'get',
    params
  });
}

export function getDesertificationPage(params) {
  return request({
    url: '/v1/desertification/page',
    method: 'get',
    params
  });
}

export function getDesertificationStats(params) {
  return request({
    url: '/v1/desertification/stats',
    method: 'get',
    params
  });
}

export function getDesertificationDetail(id) {
  return request({
    url: `/v1/desertification/${id}`,
    method: 'get'
  });
}

export function getDesertificationTrend(params) {
  return request({
    url: '/v1/desertification/trend',
    method: 'get',
    params
  });
}

export function getDesertificationRiskDistribution(params) {
  return request({
    url: '/v1/desertification/risk-distribution',
    method: 'get',
    params
  });
}

export function createDesertification(data) {
  return request({
    url: '/v1/desertification',
    method: 'post',
    data
  });
}

export function updateDesertification(id, data) {
  return request({
    url: `/v1/desertification/${id}`,
    method: 'put',
    data
  });
}

export function deleteDesertification(id) {
  return request({
    url: `/v1/desertification/${id}`,
    method: 'delete'
  });
}

export function assessDesertificationRisk(id) {
  return request({
    url: `/v1/desertification/${id}/assess`,
    method: 'post'
  });
}
