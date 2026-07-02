import axios from 'axios';
import router from '@/router';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/approval/api',
  timeout: 30000
});

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

function handleAuthFail() {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  const currentPath = router.currentRoute.value.fullPath;
  if (currentPath !== '/login') {
    router.replace({ path: '/login', query: { redirect: currentPath } });
  }
}

request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 0 && res.code !== 200) {
      console.warn('API Warning:', res.msg);
    }
    return res;
  },
  error => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      handleAuthFail();
    }
    return Promise.reject(error);
  }
);

export default request;
