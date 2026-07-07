import request from '@/utils/request';

const BASE = '/v1/login-ip-rule';

export function getList(params) {
  return request({ url: BASE + '/list', method: 'get', params });
}

export function getActiveRules() {
  return request({ url: BASE + '/active', method: 'get' });
}

export function createRule(data) {
  return request({ url: BASE, method: 'post', data });
}

export function updateRule(id, data) {
  return request({ url: BASE + '/' + id, method: 'put', data });
}

export function deleteRule(id) {
  return request({ url: BASE + '/' + id, method: 'delete' });
}

export function batchDelete(ids) {
  return request({ url: BASE + '/batch', method: 'delete', data: ids });
}

export function toggleStatus(id, status) {
  return request({ url: BASE + '/' + id + '/status', method: 'put', params: { status } });
}
