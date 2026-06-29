import request from '@/utils/request';

export function getDashboardStats() {
  return request({
    url: '/v1/dashboard/stats',
    method: 'get'
  });
}

export function getRecentTasks(params) {
  return request({
    url: '/v1/missions/recent',
    method: 'get',
    params
  });
}

export function getDeviceList(params) {
  return request({
    url: '/v1/device/list',
    method: 'get',
    params
  });
}

export function getSystemLogs(params) {
  return request({
    url: '/v1/dashboard/logs',
    method: 'get',
    params
  });
}
