import request from '@/utils/request';

const BASE = '/management';

// ==================== 用户管理 ====================

export function listUsers(params) {
  return request({
    url: `${BASE}/users`,
    method: 'get',
    params
  });
}

export function createUser(data) {
  return request({
    url: `${BASE}/users`,
    method: 'post',
    data
  });
}

export function updateUser(id, data) {
  return request({
    url: `${BASE}/users/${id}`,
    method: 'put',
    data
  });
}

export function deleteUser(id) {
  return request({
    url: `${BASE}/users/${id}`,
    method: 'delete'
  });
}

export function resetPassword(id, data) {
  return request({
    url: `${BASE}/users/${id}/password`,
    method: 'put',
    data
  });
}

export function getUserRoles(id) {
  return request({
    url: `${BASE}/users/${id}/roles`,
    method: 'get'
  });
}

// ==================== 角色管理 ====================

export function listRoles() {
  return request({
    url: `${BASE}/roles`,
    method: 'get'
  });
}

export function createRole(data) {
  return request({
    url: `${BASE}/roles`,
    method: 'post',
    data
  });
}

export function updateRole(id, data) {
  return request({
    url: `${BASE}/roles/${id}`,
    method: 'put',
    data
  });
}

export function deleteRole(id) {
  return request({
    url: `${BASE}/roles/${id}`,
    method: 'delete'
  });
}

// ==================== 组织管理 ====================

export function listOrgs() {
  return request({
    url: `${BASE}/orgs`,
    method: 'get'
  });
}

export function getOrg(id) {
  return request({
    url: `${BASE}/orgs/${id}`,
    method: 'get'
  });
}

export function updateOrg(id, data) {
  return request({
    url: `${BASE}/orgs/${id}`,
    method: 'put',
    data
  });
}

// ==================== 当前用户信息 ====================

export function getCurrentUser() {
  return request({
    url: `${BASE}/current`,
    method: 'get'
  });
}

export default {
  listUsers,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  getUserRoles,
  listRoles,
  createRole,
  updateRole,
  deleteRole,
  listOrgs,
  getOrg,
  updateOrg,
  getCurrentUser
};
