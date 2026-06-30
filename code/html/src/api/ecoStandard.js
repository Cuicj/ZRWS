import request from '@/utils/request';

export function getEcoStandardList(params) {
  return request({
    url: '/v1/eco-standard/list',
    method: 'get',
    params
  });
}

export function getEcoStandardPage(params) {
  return request({
    url: '/v1/eco-standard/page',
    method: 'get',
    params
  });
}

export function getEcoStandardDetail(id) {
  return request({
    url: `/v1/eco-standard/${id}`,
    method: 'get'
  });
}

export function getEcoStandardByCategory(category) {
  return request({
    url: `/v1/eco-standard/category/${category}`,
    method: 'get'
  });
}

export function getEcoStandardSubcategories(category) {
  return request({
    url: `/v1/eco-standard/subcategories`,
    method: 'get',
    params: { category }
  });
}

export function createEcoStandard(data) {
  return request({
    url: '/v1/eco-standard',
    method: 'post',
    data
  });
}

export function updateEcoStandard(id, data) {
  return request({
    url: `/v1/eco-standard/${id}`,
    method: 'put',
    data
  });
}

export function deleteEcoStandard(id) {
  return request({
    url: `/v1/eco-standard/${id}`,
    method: 'delete'
  });
}
