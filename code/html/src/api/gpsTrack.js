import request from '@/utils/request';

// 获取航迹列表
export function getGpsTrackList(params) {
  return request({
    url: '/v1/gps-track/list',
    method: 'get',
    params
  });
}

// 获取航迹详情
export function getGpsTrackDetail(trackId) {
  return request({
    url: `/v1/gps-track/${trackId}`,
    method: 'get'
  });
}

// 获取航迹点列表
export function getTrackPoints(trackId, params) {
  return request({
    url: `/v1/gps-track/points/${trackId}`,
    method: 'get',
    params
  });
}

// 创建航迹记录
export function createGpsTrack(data) {
  return request({
    url: '/v1/gps-track',
    method: 'post',
    data
  });
}

// 删除航迹记录
export function deleteGpsTrack(trackId) {
  return request({
    url: `/v1/gps-track/${trackId}`,
    method: 'delete'
  });
}