import request from '@/utils/request';

export function getMenuTree() {
  return request({
    url: '/v1/menu/tree',
    method: 'get'
  });
}

export function getMenuList() {
  return request({
    url: '/v1/menu/list',
    method: 'get'
  });
}

export function getMenu(id) {
  return request({
    url: `/v1/menu/${id}`,
    method: 'get'
  });
}

export function saveMenu(data) {
  return request({
    url: '/v1/menu',
    method: 'post',
    data
  });
}

export function deleteMenu(id) {
  return request({
    url: `/v1/menu/${id}`,
    method: 'delete'
  });
}
