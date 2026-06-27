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
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
}

.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
  letter-spacing: 0.05em;
}

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-primary {
  background: linear-gradient(135deg, #C9A86C 0%, #B8956A 100%);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.4);
}

.btn-primary.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 10px;
}

.btn-ghost {
  background: #FAFAF8;
  color: #5D4E37;
  border: 1px solid #E8E2D9;
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-ghost:hover {
  background: #F5F2ED;
  border-color: #C9A86C;
  color: #C9A86C;
}

.btn-ghost.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: linear-gradient(145deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(139, 115, 85, 0.12);
}

.stat-icon {
  font-size: 28px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #C9A86C;
  font-family: 'Courier New', monospace;
}

.stat-label {
  font-size: 12px;
  color: #8B7355;
  margin-top: 2px;
}

.main-content {
  flex: 1;
  display: grid;
  grid-template-columns: 300px 1fr 400px;
  gap: 20px;
  overflow: hidden;
}

.left-sidebar {
  background: linear-gradient(180deg, #FAFAF8 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
  overflow-y: auto;
  box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
}

.filter-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 11px;
  color: #C9A86C;
  letter-spacing: 0.15em;
  margin-bottom: 14px;
  font-weight: 600;
}

.filter-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  color: #8B7355;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 13px;
}

.filter-btn:hover {
  border-color: #C9A86C;
  color: #5D4E37;
  background: #F5F2ED;
  transform: translateX(4px);
}

.filter-btn.active {
  background: linear-gradient(135deg, #C9A86C 0%, #B8956A 100%);
  color: #fff;
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}

.level-icon {
  font-size: 16px;
}

.level-count {
  margin-left: auto;
  font-family: 'Courier New', monospace;
  font-size: 11px;
}

.timeline-section {
  margin-bottom: 24px;
}

.timeline-container {
  position: relative;
  padding-left: 20px;
}

.timeline-item {
  position: relative;
  padding: 10px 0;
  padding-left: 14px;
  border-left: 2px solid #E8E2D9;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.timeline-item:hover {
  border-left-color: #C9A86C;
}

.timeline-item.active {
  border-left-color: #C9A86C;
  background: rgba(201, 168, 108, 0.1);
  border-radius: 0 8px 8px 0;
}

.timeline-date {
  font-size: 11px;
  font-family: 'Courier New', monospace;
  color: #C9A86C;
  margin-bottom: 4px;
  font-weight: 600;
}

.timeline-dot {
  position: absolute;
  left: -5px;
  top: 18px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #D4C8B8;
  border: 2px solid #FAFAF8;
}

.timeline-item.active .timeline-dot {
  background: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.2);
}

.timeline-content {
  font-size: 12px;
  color: #5D4E37;
  line-height: 1.6;
}

.timeline-empty {
  font-size: 12px;
  color: #8B7355;
  text-align: center;
  padding: 20px;
}

.ai-summary {
  margin-bottom: 24px;
}

.summary-content {
  font-size: 13px;
  color: #5D4E37;
  line-height: 1.7;
  padding: 16px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 12px;
}

.ai-keywords {
  margin-bottom: 24px;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  padding: 6px 12px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 20px;
  font-size: 11px;
  color: #C9A86C;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.keyword-tag:hover {
  background: #C9A86C;
  color: #fff;
  border-color: #C9A86C;
}

.center-content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.announcements-header {
  margin-bottom: 14px;
}

.tabs {
  display: flex;
  gap: 8px;
  padding: 10px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(139, 115, 85, 0.06);
}

.tab-btn {
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: #8B7355;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-btn:hover {
  background: #FEFBF6;
  color: #5D4E37;
}

.tab-btn.active {
  background: linear-gradient(135deg, #C9A86C 0%, #B8956A 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.3);
}

.announcements-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-right: 4px;
}

.announcement-card {
  background: linear-gradient(145deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 10px rgba(139, 115, 85, 0.06);
}

.announcement-card:hover {
  border-color: #D4C8B8;
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(139, 115, 85, 0.1);
}

.announcement-card.active {
  border-color: #C9A86C;
  background: linear-gradient(145deg, #FEFBF6 0%, #FAF4E8 100%);
  box-shadow: 0 4px 16px rgba(201, 168, 108, 0.15);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.level-badge {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
}

.level-badge.PROVINCE {
  background: rgba(201, 108, 108, 0.15);
  color: #C96C6C;
}

.level-badge.CITY {
  background: rgba(201, 168, 108, 0.2);
  color: #B8956A;
}

.level-badge.COUNTY {
  background: rgba(108, 168, 120, 0.15);
  color: #6CA878;
}

.level-badge.TOWNSHIP {
  background: rgba(108, 140, 201, 0.15);
  color: #6C8CC9;
}

.level-badge.large {
  padding: 6px 14px;
  font-size: 13px;
}

.land-type-tag {
  padding: 4px 10px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  font-size: 11px;
  color: #8B7355;
}

.publish-time {
  margin-left: auto;
  font-size: 11px;
  color: #8B7355;
  font-family: 'Courier New', monospace;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 10px;
  line-height: 1.5;
}

.card-summary {
  font-size: 13px;
  color: #8B7355;
  line-height: 1.7;
  margin-bottom: 14px;
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
  gap: 8px;
}

.mini-tag {
  padding: 3px 8px;
  background: #FEFBF6;
  border: 1px solid #E8E2D9;
  border-radius: 6px;
  font-size: 10px;
  color: #C9A86C;
  font-weight: 500;
}

.card-stats {
  display: flex;
  gap: 14px;
}

.stat {
  font-size: 11px;
  color: #8B7355;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #8B7355;
}

.right-sidebar {
  background: linear-gradient(180deg, #FAFAF8 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  overflow-y: auto;
  box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
}

.detail-panel {
  padding: 24px;
}

.detail-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E8E2D9;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 16px;
  line-height: 1.5;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-section {
  padding-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.content-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.content-text {
  font-size: 14px;
  color: #5D4E37;
  line-height: 1.9;
  white-space: pre-wrap;
}

.source-info,
.location-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.info-label {
  color: #8B7355;
  min-width: 60px;
}

.info-value {
  color: #5D4E37;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  padding: 12px 16px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  color: #5D4E37;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.action-btn:hover {
  border-color: #C9A86C;
  color: #C9A86C;
  background: #FEFBF6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.15);
}

.detail-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.detail-empty .empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.detail-empty .empty-text {
  font-size: 14px;
  color: #8B7355;
}
</style>
