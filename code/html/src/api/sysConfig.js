import request from '@/utils/request';

// 获取系统配置列表
export function getSysConfigList(params) {
  return request({
    url: '/v1/sysconfig',
    method: 'get',
    params
  });
}

// 获取配置分组
export function getConfigGroups() {
  return request({
    url: '/v1/sysconfig/groups',
    method: 'get'
  });
}

// 获取单个配置项
export function getSysConfig(key) {
  return request({
    url: `/v1/sysconfig/${key}`,
    method: 'get'
  });
}

// 更新配置项
export function updateSysConfig(key, data) {
  return request({
    url: `/v1/sysconfig/${key}`,
    method: 'put',
    data
  });
}

// 批量更新配置
export function batchUpdateConfig(data) {
  return request({
    url: '/v1/sysconfig/batch',
    method: 'put',
    data
  });
}