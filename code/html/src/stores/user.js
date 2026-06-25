import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref(JSON.parse(localStorage.getItem('currentUser') || 'null'));

  const login = (userData) => {
    token.value = userData.token || 'mock-token';
    userInfo.value = userData;
    localStorage.setItem('token', token.value);
    localStorage.setItem('currentUser', JSON.stringify(userData));
  };

  const logout = () => {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
  };

  return { token, userInfo, login, logout };
});
