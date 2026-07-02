import request from '@/utils/request';

// 获取土壤采样列表
export function getSoilSampleList(params) {
  return request({
    url: '/v1/soil-sample/list',
    method: 'get',
    params
  });
}

// 获取采样详情
export function getSoilSampleDetail(id) {
  return request({
    url: `/v1/soil-sample/${id}`,
    method: 'get'
  });
}

// 创建采样记录
export function createSoilSample(data) {
  return request({
    url: '/v1/soil-sample',
    method: 'post',
    data
  });
}

// 更新采样记录
export function updateSoilSample(id, data) {
  return request({
    url: `/v1/soil-sample/${id}`,
    method: 'put',
    data
  });
}

// 删除采样记录
export function deleteSoilSample(id) {
  return request({
    url: `/v1/soil-sample/${id}`,
    method: 'delete'
  });
}

// 获取采样统计数据
export function getSoilSampleStats(params) {
  return request({
    url: '/v1/soil-sample/stats',
    method: 'get',
    params
  });
}