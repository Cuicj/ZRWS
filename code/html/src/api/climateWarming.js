import request from '@/utils/request';

export function getClimateWarmingList(params) {
  return request({
    url: '/v1/climate-warming/list',
    method: 'get',
    params
  });
}

export function getClimateWarmingPage(params) {
  return request({
    url: '/v1/climate-warming/page',
    method: 'get',
    params
  });
}

export function getClimateWarmingStats(params) {
  return request({
    url: '/v1/climate-warming/stats',
    method: 'get',
    params
  });
}

export function getClimateWarmingDetail(id) {
  return request({
    url: `/v1/climate-warming/${id}`,
    method: 'get'
  });
}

export function getClimateWarmingTrend(params) {
  return request({
    url: '/v1/climate-warming/trend',
    method: 'get',
    params
  });
}

export function getClimateWarmingRiskDistribution(params) {
  return request({
    url: '/v1/climate-warming/risk-distribution',
    method: 'get',
    params
  });
}

export function createClimateWarming(data) {
  return request({
    url: '/v1/climate-warming',
    method: 'post',
    data
  });
}

export function updateClimateWarming(id, data) {
  return request({
    url: `/v1/climate-warming/${id}`,
    method: 'put',
    data
  });
}

export function deleteClimateWarming(id) {
  return request({
    url: `/v1/climate-warming/${id}`,
    method: 'delete'
  });
}

export function assessClimateWarmingRisk(id) {
  return request({
    url: `/v1/climate-warming/${id}/assess`,
    method: 'post'
  });
}
