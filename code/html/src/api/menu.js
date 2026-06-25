import request from '@/utils/request';

export function getMenuTree() {
  return request({
    url: '/menu/tree',
    method: 'get'
  });
}

export function getMenuList() {
  return request({
    url: '/menu/list',
    method: 'get'
  });
}

export function getMenu(id) {
  return request({
    url: `/menu/${id}`,
    method: 'get'
  });
}

export function saveMenu(data) {
  return request({
    url: '/menu',
    method: 'post',
    data
  });
}

export function deleteMenu(id) {
  return request({
    url: `/menu/${id}`,
    method: 'delete'
  });
}
