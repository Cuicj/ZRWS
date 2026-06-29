import request from '@/utils/request';

export function getDeviceList(params) {
  return request({
    url: '/v1/device/list',
    method: 'get',
    params
  });
}

export function getDevice(id) {
  return request({
    url: `/v1/device/${id}`,
    method: 'get'
  });
}

export function createDevice(data) {
  return request({
    url: '/v1/device',
    method: 'post',
    data
  });
}

export function updateDevice(id, data) {
  return request({
    url: `/v1/device/${id}`,
    method: 'put',
    data
  });
}

export function deleteDevice(id) {
  return request({
    url: `/v1/device/${id}`,
    method: 'delete'
  });
}

export function getDeviceStatus(id) {
  return request({
    url: `/v1/device/${id}/status`,
    method: 'get'
  });
}
