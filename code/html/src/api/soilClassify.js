import request from '@/utils/request';

export function getSoilClassifyList(params) {
  return request({
    url: '/v1/soil-classify/list',
    method: 'get',
    params
  });
}

export function getSoilClassifyResult(id) {
  return request({
    url: `/v1/soil-classify/result/${id}`,
    method: 'get'
  });
}

export function createClassifyTask(data) {
  return request({
    url: '/v1/soil-classify/task',
    method: 'post',
    data
  });
}

export function getClassifyAnalysis(params) {
  return request({
    url: '/v1/soil-classify/analysis',
    method: 'get',
    params
  });
}

export function getSoilClassificationList(params) {
  return request({
    url: '/v1/geo-standards',
    method: 'get',
    params: { ...params, category: 'SOIL_CHINA' }
  });
}

// 获取分类历史记录
export function getClassifyHistory(params) {
  return request({
    url: '/v1/soil-classification/history',
    method: 'get',
    params
  });
}
