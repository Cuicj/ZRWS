import request from '@/utils/request';

export function getAnnouncementList(params) {
  return request({
    url: '/v1/announcement/list',
    method: 'get',
    params
  });
}

export function getAnnouncementById(id) {
  return request({
    url: `/v1/announcement/${id}`,
    method: 'get'
  });
}

export function getTopAnnouncements() {
  return request({
    url: '/v1/announcement/top',
    method: 'get'
  });
}

export function getRecommendAnnouncements() {
  return request({
    url: '/v1/announcement/recommend',
    method: 'get'
  });
}

export function getHotAnnouncements() {
  return request({
    url: '/v1/announcement/hot',
    method: 'get'
  });
}

export function getLatestAnnouncements(limit = 10) {
  return request({
    url: '/v1/announcement/latest',
    method: 'get',
    params: { limit }
  });
}

export function likeAnnouncement(id) {
  return request({
    url: `/v1/announcement/${id}/like`,
    method: 'post'
  });
}

export function getCategories() {
  return request({
    url: '/v1/announcement/category',
    method: 'get'
  });
}

export function getAdminLevels() {
  return request({
    url: '/v1/announcement/admin-levels',
    method: 'get'
  });
}

export function getAnnouncementsByLevel(level) {
  return request({
    url: '/v1/announcement/by-level',
    method: 'get',
    params: { level }
  });
}

export function getAnnouncementsByRegion(params) {
  return request({
    url: '/v1/announcement/by-region',
    method: 'get',
    params
  });
}

export function analyzeWithAI(id) {
  return request({
    url: `/v1/announcement/${id}/analyze`,
    method: 'post'
  });
}

export function batchAnalyzeWithAI(ids) {
  return request({
    url: '/v1/announcement/batch-analyze',
    method: 'post',
    data: { ids }
  });
}

export function getStatistics() {
  return request({
    url: '/v1/announcement/statistics',
    method: 'get'
  });
}

export function saveAnnouncement(data) {
  return request({
    url: '/v1/announcement',
    method: 'post',
    data
  });
}

export function deleteAnnouncement(id) {
  return request({
    url: `/v1/announcement/${id}`,
    method: 'delete'
  });
}

export function publishAnnouncement(id, operator) {
  return request({
    url: `/v1/announcement/${id}/publish`,
    method: 'post',
    data: operator
  });
}
