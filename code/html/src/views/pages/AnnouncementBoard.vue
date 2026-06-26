<template>
  <div class="announcement-page">
    <div class="page-head">
      <div>
        <h1 class="page-title display">📰 土地资源公告栏</h1>
        <div class="page-meta mono">NEWS · AI智能分析 · 省级/市级/县级/乡级 · 实时资讯</div>
      </div>
      <div class="page-actions">
        <button class="btn-primary btn-sm" @click="showPublishDialog = true">
          📝 发布公告
        </button>
        <button class="btn-ghost btn-sm" @click="refreshData">
          🔄 刷新
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.total || 0 }}</div>
          <div class="stat-label">全部公告</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📆</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.thisMonth || 0 }}</div>
          <div class="stat-label">本月新增</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🗺️</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.byAdminLevel?.PROVINCE || 0 }}</div>
          <div class="stat-label">省级</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏙️</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.byAdminLevel?.CITY || 0 }}</div>
          <div class="stat-label">市级</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏘️</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.byAdminLevel?.COUNTY || 0 }}</div>
          <div class="stat-label">县级</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏠</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.byAdminLevel?.TOWNSHIP || 0 }}</div>
          <div class="stat-label">乡级</div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧：行政区域筛选和时间线 -->
      <div class="left-sidebar">
        <!-- 行政区域筛选 -->
        <div class="filter-section">
          <div class="section-title mono">行政区域</div>
          <div class="filter-buttons">
            <button
              v-for="level in adminLevels"
              :key="level.code"
              class="filter-btn"
              :class="{ active: selectedLevel === level.code }"
              @click="filterByLevel(level.code)"
            >
              <span class="level-icon">{{ getLevelIcon(level.code) }}</span>
              <span>{{ level.name }}</span>
              <span class="level-count">({{ getLevelCount(level.code) }})</span>
            </button>
          </div>
        </div>

        <!-- AI时间线 -->
        <div class="timeline-section" v-if="selectedAnnouncement">
          <div class="section-title mono">⏱️ AI分析时间线</div>
          <div class="timeline-container">
            <div
              v-for="(event, index) in timeline"
              :key="index"
              class="timeline-item"
              :class="{ active: index === currentTimelineIndex }"
              @click="selectTimelineEvent(index)"
            >
              <div class="timeline-date">{{ event.date }}</div>
              <div class="timeline-dot"></div>
              <div class="timeline-content">{{ event.description }}</div>
            </div>
            <div v-if="timeline.length === 0" class="timeline-empty">
              暂无时间线数据
            </div>
          </div>
        </div>

        <!-- AI摘要 -->
        <div class="ai-summary" v-if="selectedAnnouncement">
          <div class="section-title mono">🤖 AI摘要</div>
          <div class="summary-content">
            {{ selectedAnnouncement.aiSummary || '暂无AI摘要' }}
          </div>
        </div>

        <!-- AI关键词 -->
        <div class="ai-keywords" v-if="selectedAnnouncement?.aiKeywords">
          <div class="section-title mono">🔑 关键词</div>
          <div class="keywords-list">
            <span
              v-for="keyword in selectedAnnouncement.aiKeywords.split(',')"
              :key="keyword"
              class="keyword-tag"
            >
              {{ keyword.trim() }}
            </span>
          </div>
        </div>
      </div>

      <!-- 中间：公告列表 -->
      <div class="center-content">
        <div class="announcements-header">
          <div class="tabs">
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'all' }"
              @click="activeTab = 'all'"
            >
              全部
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'province' }"
              @click="activeTab = 'province'"
            >
              🏛️ 省级
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'city' }"
              @click="activeTab = 'city'"
            >
              🏙️ 市级
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'county' }"
              @click="activeTab = 'county'"
            >
              🏘️ 县级
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'township' }"
              @click="activeTab = 'township'"
            >
              🏠 乡级
            </button>
          </div>
        </div>

        <div class="announcements-list">
          <div
            v-for="announcement in filteredAnnouncements"
            :key="announcement.announcementId"
            class="announcement-card"
            :class="{ active: selectedAnnouncement?.announcementId === announcement.announcementId }"
            @click="selectAnnouncement(announcement)"
          >
            <div class="card-header">
              <span class="level-badge" :class="announcement.adminLevel">
                {{ getLevelName(announcement.adminLevel) }}
              </span>
              <span class="land-type-tag">{{ announcement.landType || '综合' }}</span>
              <span class="publish-time mono">
                {{ formatTime(announcement.publishTime) }}
              </span>
            </div>
            <h3 class="card-title">{{ announcement.title }}</h3>
            <div class="card-summary">
              {{ announcement.summary || announcement.aiSummary || '暂无摘要' }}
            </div>
            <div class="card-footer">
              <div class="card-tags" v-if="announcement.aiKeywords">
                <span
                  v-for="tag in announcement.aiKeywords.split(',').slice(0, 3)"
                  :key="tag"
                  class="mini-tag"
                >
                  {{ tag.trim() }}
                </span>
              </div>
              <div class="card-stats">
                <span class="stat">👁️ {{ announcement.viewCount || 0 }}</span>
                <span class="stat">👍 {{ announcement.likeCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div v-if="filteredAnnouncements.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <div class="empty-text">暂无公告</div>
          </div>
        </div>
      </div>

      <!-- 右侧：公告详情 -->
      <div class="right-sidebar">
        <div v-if="selectedAnnouncement" class="detail-panel">
          <div class="detail-header">
            <h2 class="detail-title">{{ selectedAnnouncement.title }}</h2>
            <div class="detail-meta">
              <span class="level-badge large" :class="selectedAnnouncement.adminLevel">
                {{ getLevelName(selectedAnnouncement.adminLevel) }}
              </span>
              <span class="publish-time mono">
                {{ formatDateTime(selectedAnnouncement.publishTime) }}
              </span>
            </div>
          </div>

          <div class="detail-content">
            <div class="content-section">
              <div class="section-title mono">📋 公告内容</div>
              <div class="content-text">
                {{ selectedAnnouncement.content }}
              </div>
            </div>

            <div class="content-section" v-if="selectedAnnouncement.source">
              <div class="section-title mono">📌 来源信息</div>
              <div class="source-info">
                <div class="info-item">
                  <span class="info-label">来源：</span>
                  <span class="info-value">{{ selectedAnnouncement.source }}</span>
                </div>
                <div class="info-item" v-if="selectedAnnouncement.author">
                  <span class="info-label">作者：</span>
                  <span class="info-value">{{ selectedAnnouncement.author }}</span>
                </div>
              </div>
            </div>

            <div class="content-section" v-if="selectedAnnouncement.province || selectedAnnouncement.city">
              <div class="section-title mono">🗺️ 地理位置</div>
              <div class="location-info">
                <div class="info-item">
                  <span class="info-label">省份：</span>
                  <span class="info-value">{{ selectedAnnouncement.province || '未指定' }}</span>
                </div>
                <div class="info-item" v-if="selectedAnnouncement.city">
                  <span class="info-label">城市：</span>
                  <span class="info-value">{{ selectedAnnouncement.city }}</span>
                </div>
                <div class="info-item" v-if="selectedAnnouncement.county">
                  <span class="info-label">县区：</span>
                  <span class="info-value">{{ selectedAnnouncement.county }}</span>
                </div>
                <div class="info-item" v-if="selectedAnnouncement.township">
                  <span class="info-label">乡镇：</span>
                  <span class="info-value">{{ selectedAnnouncement.township }}</span>
                </div>
              </div>
            </div>

            <div class="content-section">
              <div class="section-title mono">📊 操作</div>
              <div class="action-buttons">
                <button class="action-btn" @click="analyzeAnnouncement">
                  🤖 AI分析
                </button>
                <button class="action-btn" @click="handleLike">
                  👍 点赞
                </button>
                <button class="action-btn" v-if="selectedAnnouncement.sourceUrl">
                  🔗 原文链接
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="detail-empty">
          <div class="empty-icon">👈</div>
          <div class="empty-text">请选择一个公告查看详情</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import {
  getAnnouncementList,
  getLatestAnnouncements,
  getAdminLevels,
  getAnnouncementsByLevel,
  getStatistics,
  analyzeWithAI as analyzeAPI,
  likeAnnouncement as likeAPI
} from '@/api/announcement';

const announcements = ref([]);
const adminLevels = ref([]);
const statistics = ref({});
const selectedLevel = ref(null);
const selectedAnnouncement = ref(null);
const timeline = ref([]);
const currentTimelineIndex = ref(-1);
const activeTab = ref('all');
const showPublishDialog = ref(false);

const filteredAnnouncements = computed(() => {
  if (activeTab.value === 'all') {
    return announcements.value;
  }
  return announcements.value.filter(a => a.adminLevel === activeTab.value.toUpperCase());
});

const getLevelIcon = (code) => {
  const icons = {
    'PROVINCE': '🏛️',
    'CITY': '🏙️',
    'COUNTY': '🏘️',
    'TOWNSHIP': '🏠'
  };
  return icons[code] || '📍';
};

const getLevelName = (code) => {
  const names = {
    'PROVINCE': '省级',
    'CITY': '市级',
    'COUNTY': '县级',
    'TOWNSHIP': '乡级'
  };
  return names[code] || '未分类';
};

const getLevelCount = (code) => {
  return statistics.value.byAdminLevel?.[code] || 0;
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN');
};

const formatDateTime = (time) => {
  if (!time) return '';
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const loadData = async () => {
  try {
    const [annRes, levelRes, statsRes] = await Promise.all([
      getLatestAnnouncements(100),
      getAdminLevels(),
      getStatistics()
    ]);

    if (annRes.success) {
      announcements.value = annRes.list || [];
    }
    if (levelRes.success) {
      adminLevels.value = levelRes.list || [];
    }
    if (statsRes.success) {
      statistics.value = statsRes.statistics || {};
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  }
};

const filterByLevel = async (level) => {
  selectedLevel.value = level;
  if (level) {
    const res = await getAnnouncementsByLevel(level);
    if (res.success) {
      announcements.value = res.list || [];
    }
  } else {
    const res = await getLatestAnnouncements(100);
    if (res.success) {
      announcements.value = res.list || [];
    }
  }
};

const selectAnnouncement = (announcement) => {
  selectedAnnouncement.value = announcement;
  currentTimelineIndex.value = -1;

  // 解析时间线
  if (announcement.aiTimeline) {
    try {
      timeline.value = JSON.parse(announcement.aiTimeline);
    } catch (e) {
      timeline.value = [];
    }
  } else {
    timeline.value = [];
  }
};

const selectTimelineEvent = (index) => {
  currentTimelineIndex.value = index;
};

const analyzeAnnouncement = async () => {
  if (!selectedAnnouncement.value) return;
  try {
    const res = await analyzeAPI(selectedAnnouncement.value.announcementId);
    if (res.success) {
      selectedAnnouncement.value = res.announcement;
      if (res.announcement.aiTimeline) {
        try {
          timeline.value = JSON.parse(res.announcement.aiTimeline);
        } catch (e) {
          timeline.value = [];
        }
      }
      await loadData();
    }
  } catch (error) {
    console.error('AI分析失败:', error);
  }
};

const handleLike = async () => {
  if (!selectedAnnouncement.value) return;
  try {
    await likeAPI(selectedAnnouncement.value.announcementId);
    selectedAnnouncement.value.likeCount = (selectedAnnouncement.value.likeCount || 0) + 1;
  } catch (error) {
    console.error('点赞失败:', error);
  }
};

const refreshData = () => {
  loadData();
};

watch(activeTab, (newTab) => {
  if (newTab !== 'all') {
    const levelMap = {
      'province': 'PROVINCE',
      'city': 'CITY',
      'county': 'COUNTY',
      'township': 'TOWNSHIP'
    };
    filterByLevel(levelMap[newTab]);
  } else {
    loadData();
  }
});

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.announcement-page {
  padding: var(--s-5);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-4);
  border-bottom: var(--line);
}

.page-title {
  font-size: 28px;
  font-weight: 200;
}

.page-meta {
  font-size: 11px;
  color: var(--signal-dim);
  margin-top: 4px;
}

.page-actions {
  display: flex;
  gap: var(--s-3);
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--s-3);
  margin-bottom: var(--s-4);
}

.stat-card {
  background: var(--ink-800);
  border: var(--line-soft);
  border-radius: var(--radius-md);
  padding: var(--s-4);
  display: flex;
  align-items: center;
  gap: var(--s-3);
}

.stat-icon {
  font-size: 28px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--sand-500);
  font-family: var(--font-mono);
}

.stat-label {
  font-size: 12px;
  color: var(--signal-dim);
}

.main-content {
  flex: 1;
  display: grid;
  grid-template-columns: 300px 1fr 400px;
  gap: var(--s-4);
  overflow: hidden;
}

.left-sidebar {
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  padding: var(--s-4);
  overflow-y: auto;
}

.filter-section {
  margin-bottom: var(--s-4);
}

.section-title {
  font-size: 11px;
  color: var(--sand-500);
  letter-spacing: 0.15em;
  margin-bottom: var(--s-3);
}

.filter-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  padding: var(--s-2) var(--s-3);
  background: var(--ink-700);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-sm);
  color: var(--signal-dim);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 13px;
}

.filter-btn:hover {
  border-color: var(--sand-500);
  color: var(--signal);
}

.filter-btn.active {
  background: var(--sand-500);
  color: var(--ink-900);
  border-color: var(--sand-500);
}

.level-icon {
  font-size: 16px;
}

.level-count {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 11px;
}

.timeline-section {
  margin-bottom: var(--s-4);
}

.timeline-container {
  position: relative;
  padding-left: 20px;
}

.timeline-item {
  position: relative;
  padding: var(--s-2) 0;
  padding-left: var(--s-3);
  border-left: 2px solid var(--ink-600);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.timeline-item:hover {
  border-left-color: var(--sand-500);
}

.timeline-item.active {
  border-left-color: var(--sand-500);
  background: rgba(212, 168, 83, 0.1);
}

.timeline-date {
  font-size: 11px;
  font-family: var(--font-mono);
  color: var(--sand-500);
  margin-bottom: 4px;
}

.timeline-dot {
  position: absolute;
  left: -5px;
  top: 18px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ink-500);
  border: 2px solid var(--ink-700);
}

.timeline-item.active .timeline-dot {
  background: var(--sand-500);
}

.timeline-content {
  font-size: 12px;
  color: var(--signal-dim);
  line-height: 1.5;
}

.timeline-empty {
  font-size: 12px;
  color: var(--signal-dim);
  text-align: center;
  padding: var(--s-4);
}

.ai-summary {
  margin-bottom: var(--s-4);
}

.summary-content {
  font-size: 13px;
  color: var(--signal-dim);
  line-height: 1.6;
  padding: var(--s-3);
  background: var(--ink-700);
  border-radius: var(--radius-sm);
}

.ai-keywords {
  margin-bottom: var(--s-4);
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-2);
}

.keyword-tag {
  padding: 4px 10px;
  background: var(--ink-700);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--sand-500);
}

.center-content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.announcements-header {
  margin-bottom: var(--s-3);
}

.tabs {
  display: flex;
  gap: var(--s-2);
  padding: var(--s-2);
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
}

.tab-btn {
  padding: var(--s-2) var(--s-3);
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--signal-dim);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  background: var(--ink-700);
  color: var(--signal);
}

.tab-btn.active {
  background: var(--sand-500);
  color: var(--ink-900);
  font-weight: 600;
}

.announcements-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--s-3);
}

.announcement-card {
  background: var(--ink-800);
  border: var(--line-soft);
  border-radius: var(--radius-md);
  padding: var(--s-4);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.announcement-card:hover {
  border-color: var(--ink-500);
  transform: translateX(4px);
}

.announcement-card.active {
  border-color: var(--sand-500);
  background: rgba(212, 168, 83, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  margin-bottom: var(--s-2);
}

.level-badge {
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
}

.level-badge.PROVINCE {
  background: rgba(255, 77, 79, 0.2);
  color: #ff4d4f;
}

.level-badge.CITY {
  background: rgba(250, 173, 20, 0.2);
  color: #faad14;
}

.level-badge.COUNTY {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.level-badge.TOWNSHIP {
  background: rgba(24, 144, 255, 0.2);
  color: #1890ff;
}

.level-badge.large {
  padding: 4px 12px;
  font-size: 13px;
}

.land-type-tag {
  padding: 2px 8px;
  background: var(--ink-700);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--signal-dim);
}

.publish-time {
  margin-left: auto;
  font-size: 11px;
  color: var(--signal-dim);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--signal);
  margin-bottom: var(--s-2);
  line-height: 1.4;
}

.card-summary {
  font-size: 13px;
  color: var(--signal-dim);
  line-height: 1.6;
  margin-bottom: var(--s-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-tags {
  display: flex;
  gap: var(--s-2);
}

.mini-tag {
  padding: 2px 6px;
  background: var(--ink-700);
  border-radius: 2px;
  font-size: 10px;
  color: var(--sand-500);
}

.card-stats {
  display: flex;
  gap: var(--s-3);
}

.stat {
  font-size: 11px;
  color: var(--signal-dim);
}

.empty-state {
  text-align: center;
  padding: var(--s-10);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--s-3);
}

.empty-text {
  font-size: 14px;
  color: var(--signal-dim);
}

.right-sidebar {
  background: var(--ink-800);
  border: var(--line);
  border-radius: var(--radius-md);
  overflow-y: auto;
}

.detail-panel {
  padding: var(--s-4);
}

.detail-header {
  margin-bottom: var(--s-4);
  padding-bottom: var(--s-4);
  border-bottom: var(--line);
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--signal);
  margin-bottom: var(--s-3);
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--s-3);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
}

.content-section {
  padding-bottom: var(--s-4);
  border-bottom: 1px solid var(--ink-700);
}

.content-section:last-child {
  border-bottom: none;
}

.content-text {
  font-size: 14px;
  color: var(--signal-dim);
  line-height: 1.8;
  white-space: pre-wrap;
}

.source-info,
.location-info {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  font-size: 13px;
}

.info-label {
  color: var(--signal-dim);
  min-width: 60px;
}

.info-value {
  color: var(--signal);
}

.action-buttons {
  display: flex;
  gap: var(--s-2);
}

.action-btn {
  flex: 1;
  padding: var(--s-2) var(--s-3);
  background: var(--ink-700);
  border: 1px solid var(--ink-600);
  border-radius: var(--radius-sm);
  color: var(--signal-dim);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  border-color: var(--sand-500);
  color: var(--sand-500);
}

.detail-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--s-10);
}

.detail-empty .empty-icon {
  font-size: 48px;
  margin-bottom: var(--s-3);
  opacity: 0.5;
}

.detail-empty .empty-text {
  font-size: 14px;
  color: var(--signal-dim);
}
</style>
