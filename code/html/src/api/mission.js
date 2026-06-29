import request from '@/utils/request';

export function getMissionList(params) {
  return request({
    url: '/v1/mission/list',
    method: 'get',
    params
  });
}

export function getMission(id) {
  return request({
    url: `/v1/mission/${id}`,
    method: 'get'
  });
}

export function createMission(data) {
  return request({
    url: '/v1/mission',
    method: 'post',
    data
  });
}

export function updateMission(id, data) {
  return request({
    url: `/v1/mission/${id}`,
    method: 'put',
    data
  });
}

export function deleteMission(id) {
  return request({
    url: `/v1/mission/${id}`,
    method: 'delete'
  });
}

export function getMissionStats() {
  return request({
    url: '/v1/mission/stats',
    method: 'get'
  });
}

export function getGpsTrack(missionId) {
  return request({
    url: `/v1/gps/track/${missionId}`,
    method: 'get'
  });
}

export function getGpsList(params) {
  return request({
    url: '/v1/gps/list',
    method: 'get',
    params
  });
}
