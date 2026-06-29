import request from '@/utils/request';

export function getDisasterRiskList(params) {
  return request({
    url: '/v1/disaster-risk/list',
    method: 'get',
    params
  });
}

export function getDisasterRiskStats() {
  return request({
    url: '/v1/disaster-risk/stats',
    method: 'get'
  });
}

export function getDisasterRiskDetail(id) {
  return request({
    url: `/v1/disaster-risk/${id}`,
    method: 'get'
  });
}
