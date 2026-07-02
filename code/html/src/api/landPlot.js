import request from '@/utils/request';

// 获取地块列表
export function getLandPlotList(params) {
  return request({
    url: '/v1/land-plot/list',
    method: 'get',
    params
  });
}

// 获取地块详情
export function getLandPlotDetail(id) {
  return request({
    url: `/v1/land-plot/${id}`,
    method: 'get'
  });
}

// 获取面积统计数据
export function getAreaStats(params) {
  return request({
    url: '/v1/land-plot/area-stats',
    method: 'get',
    params
  });
}

// 创建地块记录
export function createLandPlot(data) {
  return request({
    url: '/v1/land-plot',
    method: 'post',
    data
  });
}

// 更新地块记录
export function updateLandPlot(id, data) {
  return request({
    url: `/v1/land-plot/${id}`,
    method: 'put',
    data
  });
}

// 删除地块记录
export function deleteLandPlot(id) {
  return request({
    url: `/v1/land-plot/${id}`,
    method: 'delete'
  });
}