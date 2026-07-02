import request from '@/utils/request';

export function login(data) {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data
  });
}

export function logout() {
  return request({
    url: '/v1/auth/logout',
    method: 'post'
  });
}

export function getUserInfo() {
  return request({
    url: '/v1/auth/info',
    method: 'get'
  });
}

export const loginApi = {
  login: (username, password) => request({
    url: '/v1/auth/login',
    method: 'post',
    data: { username, password }
  }),
  logout: () => request({
    url: '/v1/auth/logout',
    method: 'post'
  }),
  getUserInfo: () => request({
    url: '/v1/auth/info',
    method: 'get'
  })
};

export default loginApi;
