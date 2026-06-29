import request from '@/utils/request';

export function getQualityCheckList(params) {
  return request({
    url: '/v1/quality-check/list',
    method: 'get',
    params
  });
}

export function getQualityCheckStats() {
  return request({
    url: '/v1/quality-check/stats',
    method: 'get'
  });
}

export function createQualityCheck(data) {
  return request({
    url: '/v1/quality-check',
    method: 'post',
    data
  });
}
