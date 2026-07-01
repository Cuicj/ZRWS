<template>
  <div class="news-page">
    <!-- Hero区域：头条置顶 -->
    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-label mono">
          <span class="label-dot"></span>
          头条 · 置顶公告
        </div>
        <div class="hero-headline" v-if="topAnnouncements.length > 0">
          <h1 class="hero-title" @click="openDetail(topAnnouncements[0])">
            {{ topAnnouncements[0].title }}
          </h1>
          <p class="hero-summary">
            {{ topAnnouncements[0].summary || topAnnouncements[0].aiSummary }}
          </p>
          <div class="hero-meta">
            <span class="meta-tag category">{{ topAnnouncements[0].categoryName }}</span>
            <span class="meta-tag level" :class="topAnnouncements[0].adminLevel">
              {{ getLevelName(topAnnouncements[0].adminLevel) }}
            </span>
            <span class="meta-text mono">{{ formatDate(topAnnouncements[0].publishTime) }}</span>
            <span class="meta-text">👁️ {{ topAnnouncements[0].viewCount || 0 }}</span>
            <button class="read-more-btn" @click="openDetail(topAnnouncements[0])">
              阅读全文 →
            </button>
          </div>
        </div>
        <div class="hero-sub" v-if="topAnnouncements.length > 1">
          <div
            v-for="item in topAnnouncements.slice(1, 3)"
            :key="item.announcementId"
            class="sub-item"
            @click="openDetail(item)"
          >
            <span class="sub-tag mono">{{ item.categoryName }}</span>
            <span class="sub-title">{{ item.title }}</span>
            <span class="sub-time mono">{{ formatDate(item.publishTime) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 分类导航 -->
    <nav class="category-nav">
      <div class="category-inner">
        <button
          v-for="cat in allCategories"
          :key="cat.categoryId"
          :class="['cat-btn', { active: selectedCategoryId === cat.categoryId }]"
          @click="selectCategory(cat.categoryId)"
        >
          <span class="cat-icon">{{ getCategoryIcon(cat.categoryName) }}</span>
          <span class="cat-name">{{ cat.categoryName }}</span>
        </button>
      </div>
    </nav>

    <!-- 主体内容 -->
    <div class="main-wrapper">
      <div class="content-grid">
        <!-- 左侧：新闻列表 -->
        <div class="news-main">
          <!-- 列表头部 -->
          <div class="list-header">
            <div class="list-tabs">
              <button
                :class="['list-tab', { active: sortType === 'latest' }]"
                @click="sortType = 'latest'"
              >最新发布</button>
              <button
                :class="['list-tab', { active: sortType === 'hot' }]"
                @click="sortType = 'hot'"
              >热门阅读</button>
              <button
                :class="['list-tab', { active: sortType === 'recommend' }]"
                @click="sortType = 'recommend'"
              >编辑推荐</button>
            </div>
            <div class="search-box">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索公告..."
                class="search-input"
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">🔍</button>
            </div>
          </div>

          <!-- 新闻列表 -->
          <div class="news-list">
            <article
              v-for="item in displayList"
              :key="item.announcementId"
              class="news-item"
              @click="openDetail(item)"
            >
              <div class="news-cover" v-if="item.coverImage">
                <img :src="item.coverImage" :alt="item.title" />
              </div>
              <div class="news-cover placeholder" v-else>
                <span class="cover-icon">{{ getCategoryIcon(item.categoryName) }}</span>
              </div>
              <div class="news-body">
                <div class="news-tags">
                  <span class="tag primary">{{ item.categoryName }}</span>
                  <span class="tag level" :class="item.adminLevel">
                    {{ getLevelName(item.adminLevel) }}
                  </span>
                  <span v-if="item.isTop" class="tag top">置顶</span>
                  <span v-if="item.isHot" class="tag hot">热门</span>
                  <span v-if="item.isRecommend" class="tag recommend">推荐</span>
                </div>
                <h3 class="news-title">{{ item.title }}</h3>
                <p class="news-summary">
                  {{ item.summary || item.aiSummary }}
                </p>
                <div class="news-footer">
                  <div class="footer-left">
                    <span class="author">{{ item.author || item.source || '智壤卫士' }}</span>
                    <span class="dot">·</span>
                    <span class="time mono">{{ formatDate(item.publishTime) }}</span>
                  </div>
                  <div class="footer-right">
                    <span class="stat">👁️ {{ item.viewCount || 0 }}</span>
                    <span class="stat">👍 {{ item.likeCount || 0 }}</span>
                    <span v-if="item.landType" class="land-type">{{ item.landType }}</span>
                  </div>
                </div>
              </div>
            </article>

            <div v-if="displayList.length === 0" class="empty-list">
              <span class="empty-icon">📭</span>
              <p>暂无相关公告</p>
            </div>
          </div>

          <!-- 加载更多 -->
          <div class="load-more" v-if="displayList.length > 0">
            <button class="load-more-btn" @click="loadMore">
              加载更多
            </button>
          </div>
        </div>

        <!-- 右侧：热门排行 + 推荐 -->
        <aside class="news-side">
          <!-- 热门排行 -->
          <div class="side-card">
            <div class="side-head">
              <span class="side-icon">🔥</span>
              <span class="side-title">热门排行</span>
            </div>
            <div class="rank-list">
              <div
                v-for="(item, index) in hotList"
                :key="item.announcementId"
                class="rank-item"
                @click="openDetail(item)"
              >
                <span :class="['rank-num', { top: index < 3 }]">{{ index + 1 }}</span>
                <span class="rank-title">{{ item.title }}</span>
                <span class="rank-views mono">{{ item.viewCount || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 编辑推荐 -->
          <div class="side-card">
            <div class="side-head">
              <span class="side-icon">⭐</span>
              <span class="side-title">编辑推荐</span>
            </div>
            <div class="recommend-list">
              <div
                v-for="item in recommendList"
                :key="item.announcementId"
                class="recommend-item"
                @click="openDetail(item)"
              >
                <div class="rec-cover">
                  <span class="rec-icon">{{ getCategoryIcon(item.categoryName) }}</span>
                </div>
                <div class="rec-body">
                  <h4 class="rec-title">{{ item.title }}</h4>
                  <span class="rec-time mono">{{ formatDate(item.publishTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 土地类型分布 -->
          <div class="side-card">
            <div class="side-head">
              <span class="side-icon">🌱</span>
              <span class="side-title">土地类型</span>
            </div>
            <div class="land-tags">
              <span
                v-for="(count, type) in statistics.byLandType"
                :key="type"
                class="land-tag"
                :class="{ active: selectedLandType === type }"
                @click="filterByLandType(type)"
              >
                {{ type }}
                <span class="tag-count mono">{{ count }}</span>
              </span>
            </div>
          </div>

          <!-- 数据统计 -->
          <div class="side-card stats-card">
            <div class="side-head">
              <span class="side-icon">📊</span>
              <span class="side-title">数据统计</span>
            </div>
            <div class="stats-grid-mini">
              <div class="stat-mini">
                <div class="stat-num">{{ statistics.total || 0 }}</div>
                <div class="stat-label">全部公告</div>
              </div>
              <div class="stat-mini">
                <div class="stat-num">{{ statistics.thisMonth || 0 }}</div>
                <div class="stat-label">本月新增</div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetail" class="detail-overlay" @click.self="closeDetail">
      <div class="detail-modal">
        <button class="close-btn" @click="closeDetail">×</button>
        <div class="detail-scroll" v-if="currentDetail">
          <article class="detail-article">
            <header class="article-header">
              <div class="article-tags">
                <span class="tag primary">{{ currentDetail.categoryName }}</span>
                <span class="tag level" :class="currentDetail.adminLevel">
                  {{ getLevelName(currentDetail.adminLevel) }}
                </span>
                <span v-if="currentDetail.isTop" class="tag top">置顶</span>
              </div>
              <h1 class="article-title">{{ currentDetail.title }}</h1>
              <div class="article-meta">
                <span>{{ currentDetail.author || '智壤卫士编辑部' }}</span>
                <span class="dot">·</span>
                <span class="mono">{{ formatDateTime(currentDetail.publishTime) }}</span>
                <span class="dot">·</span>
                <span>阅读 {{ currentDetail.viewCount || 0 }}</span>
                <span class="dot">·</span>
                <span>{{ currentDetail.source || '原创' }}</span>
              </div>
              <div v-if="currentDetail.summary" class="article-summary">
                <span class="summary-label">摘要</span>
                <p>{{ currentDetail.summary }}</p>
              </div>
            </header>

            <div class="article-content">
              {{ currentDetail.content }}
            </div>

            <footer class="article-footer">
              <div class="footer-actions">
                <button class="action-btn like" @click="handleLike">
                  👍 点赞 ({{ currentDetail.likeCount || 0 }})
                </button>
                <button class="action-btn share">
                  🔗 分享
                </button>
                <button class="action-btn ai" @click="analyzeAnnouncement">
                  🤖 AI分析
                </button>
              </div>

              <div v-if="currentDetail.aiKeywords" class="footer-keywords">
                <span class="kw-label">关键词：</span>
                <span
                  v-for="kw in currentDetail.aiKeywords.split(',')"
                  :key="kw"
                  class="kw-tag"
                >{{ kw.trim() }}</span>
              </div>

              <div v-if="currentDetail.aiSummary" class="footer-ai-summary">
                <div class="ai-head">
                  <span class="ai-icon">🤖</span>
                  <span>AI智能摘要</span>
                </div>
                <p>{{ currentDetail.aiSummary }}</p>
              </div>
            </footer>
          </article>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import {
  getAnnouncementList,
  getAnnouncementById,
  getTopAnnouncements,
  getHotAnnouncements,
  getRecommendAnnouncements,
  getAnnouncementsByCategory,
  getCategoryList,
  getStatistics,
  likeAnnouncement as likeAPI,
  analyzeWithAI as analyzeAPI,
  searchAnnouncements
} from '@/api/announcement';

const allAnnouncements = ref([]);
const topAnnouncements = ref([]);
const hotList = ref([]);
const recommendList = ref([]);
const allCategories = ref([]);
const statistics = ref({ byLandType: {} });

const selectedCategoryId = ref(null);
const selectedLandType = ref(null);
const sortType = ref('latest');
const searchKeyword = ref('');
const showDetail = ref(false);
const currentDetail = ref(null);

const displayList = computed(() => {
  let list = [...allAnnouncements.value];

  if (sortType.value === 'hot') {
    list.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0));
  } else if (sortType.value === 'recommend') {
    list = list.filter(a => a.isRecommend);
  }

  if (selectedLandType.value) {
    list = list.filter(a => a.landType === selectedLandType.value);
  }

  return list;
});

const getCategoryIcon = (name) => {
  const icons = {
    '政策法规': '📜',
    '通知公告': '📢',
    '土地新闻': '🗞️',
    '行业动态': '📈',
    '技术指南': '🔬',
    '市场信息': '💹'
  };
  return icons[name] || '📰';
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

const formatDate = (time) => {
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

const loadAllData = async () => {
  try {
    const [topRes, hotRes, recRes, catRes, statsRes, listRes] = await Promise.all([
      getTopAnnouncements(),
      getHotAnnouncements(),
      getRecommendAnnouncements(),
      getCategoryList(),
      getStatistics(),
      getAnnouncementList()
    ]);

    if (topRes.success) topAnnouncements.value = topRes.list || [];
    if (hotRes.success) hotList.value = hotRes.list || [];
    if (recRes.success) recommendList.value = recRes.list || [];
    if (catRes.success) allCategories.value = catRes.list || [];
    if (statsRes.success) statistics.value = statsRes.statistics || { byLandType: {} };
    if (listRes.success) allAnnouncements.value = listRes.list || [];
  } catch (e) {
    console.error('加载数据失败:', e);
  }
};

const selectCategory = async (categoryId) => {
  selectedCategoryId.value = selectedCategoryId.value === categoryId ? null : categoryId;
  selectedLandType.value = null;

  try {
    if (selectedCategoryId.value) {
      const res = await getAnnouncementsByCategory(selectedCategoryId.value);
      if (res.success) {
        allAnnouncements.value = res.list || [];
      }
    } else {
      const res = await getAnnouncementList();
      if (res.success) {
        allAnnouncements.value = res.list || [];
      }
    }
  } catch (e) {
    console.error('分类查询失败:', e);
  }
};

const filterByLandType = (type) => {
  selectedLandType.value = selectedLandType.value === type ? null : type;
};

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadAllData();
    return;
  }
  try {
    const res = await searchAnnouncements(searchKeyword.value);
    if (res.success) {
      allAnnouncements.value = res.list || [];
    }
  } catch (e) {
    console.error('搜索失败:', e);
  }
};

const openDetail = async (item) => {
  showDetail.value = true;
  currentDetail.value = item;

  try {
    const res = await getAnnouncementById(item.announcementId);
    if (res.success && res.announcement) {
      currentDetail.value = res.announcement;
      const idx = allAnnouncements.value.findIndex(a => a.announcementId === item.announcementId);
      if (idx > -1 && res.announcement.viewCount) {
        allAnnouncements.value[idx].viewCount = res.announcement.viewCount;
      }
    }
  } catch (e) {
    console.error('获取详情失败:', e);
  }
};

const closeDetail = () => {
  showDetail.value = false;
  currentDetail.value = null;
};

const handleLike = async () => {
  if (!currentDetail.value) return;
  try {
    await likeAPI(currentDetail.value.announcementId);
    currentDetail.value.likeCount = (currentDetail.value.likeCount || 0) + 1;
  } catch (e) {
    console.error('点赞失败:', e);
  }
};

const analyzeAnnouncement = async () => {
  if (!currentDetail.value) return;
  try {
    const res = await analyzeAPI(currentDetail.value.announcementId);
    if (res.success && res.announcement) {
      currentDetail.value = res.announcement;
    }
  } catch (e) {
    console.error('AI分析失败:', e);
  }
};

const loadMore = () => {
  console.log('加载更多');
};

onMounted(() => {
  loadAllData();
});
</script>

<style scoped>
.news-page {
  min-height: 100vh;
  background: #F5F2ED;
}

/* Hero */
.hero-section {
  background: linear-gradient(135deg, #5D4E37 0%, #7A6548 50%, #C9A86C 100%);
  padding: 56px 0 48px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(201, 168, 108, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
  position: relative;
  z-index: 1;
}

.hero-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  opacity: 0.9;
  margin-bottom: 20px;
}

.label-dot {
  width: 6px;
  height: 6px;
  background: #FF6C37;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.4;
  margin: 0 0 16px 0;
  max-width: 800px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.hero-title:hover {
  opacity: 0.9;
}

.hero-summary {
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.85;
  max-width: 700px;
  margin: 0 0 24px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-tag {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.meta-tag.category {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.meta-tag.level {
  background: rgba(255, 108, 55, 0.9);
}

.meta-text {
  font-size: 12px;
  opacity: 0.8;
}

.read-more-btn {
  background: #fff;
  color: #5D4E37;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: auto;
}

.read-more-btn:hover {
  background: #FF6C37;
  color: #fff;
  transform: translateX(2px);
}

.hero-sub {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.sub-item:hover {
  opacity: 0.8;
}

.sub-tag {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4px;
  font-size: 10px;
  flex-shrink: 0;
}

.sub-title {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-time {
  font-size: 11px;
  opacity: 0.7;
  flex-shrink: 0;
}

/* 分类导航 */
.category-nav {
  background: #fff;
  border-bottom: 1px solid #E8E2D9;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.06);
}

.category-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  gap: 0;
}

.cat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: #8B7355;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.cat-btn:hover {
  color: #5D4E37;
  background: #FAFAF8;
}

.cat-btn.active {
  color: #C9A86C;
  border-bottom-color: #C9A86C;
}

.cat-icon {
  font-size: 16px;
}

/* 主体内容 */
.main-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
}

/* 列表头部 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-tabs {
  display: flex;
  gap: 0;
}

.list-tab {
  padding: 10px 20px;
  background: none;
  border: none;
  color: #8B7355;
  font-size: 14px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  font-weight: 500;
}

.list-tab:hover {
  color: #5D4E37;
}

.list-tab.active {
  color: #C9A86C;
  border-bottom-color: #C9A86C;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #E8E2D9;
  border-radius: 24px;
  padding: 4px 8px 4px 16px;
  transition: border-color 0.2s;
}

.search-box:focus-within {
  border-color: #C9A86C;
}

.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: #5D4E37;
  width: 200px;
}

.search-btn {
  background: #C9A86C;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #B8956A;
}

/* 新闻列表 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.news-item {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  background: #fff;
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.news-item:hover {
  border-color: #D4C8B8;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.1);
}

.news-cover {
  width: 220px;
  height: 150px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F2ED 0%, #E8E2D9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.news-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-icon {
  font-size: 48px;
  opacity: 0.5;
}

.news-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.news-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

.tag.primary {
  background: rgba(201, 168, 108, 0.15);
  color: #B8956A;
}

.tag.level.PROVINCE {
  background: rgba(201, 108, 108, 0.15);
  color: #C96C6C;
}

.tag.level.CITY {
  background: rgba(201, 168, 108, 0.2);
  color: #B8956A;
}

.tag.level.COUNTY {
  background: rgba(108, 168, 120, 0.15);
  color: #6CA878;
}

.tag.level.TOWNSHIP {
  background: rgba(108, 140, 201, 0.15);
  color: #6C8CC9;
}

.tag.top {
  background: rgba(255, 108, 55, 0.15);
  color: #FF6C37;
}

.tag.hot {
  background: rgba(255, 75, 75, 0.15);
  color: #ff4b4b;
}

.tag.recommend {
  background: rgba(108, 168, 120, 0.15);
  color: #6CA878;
}

.news-title {
  font-size: 18px;
  font-weight: 600;
  color: #2C261E;
  margin: 0 0 10px 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.news-summary {
  font-size: 13px;
  color: #8B7355;
  line-height: 1.7;
  margin: 0 0 14px 0;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #A09080;
}

.author {
  color: #8B7355;
  font-weight: 500;
}

.dot {
  opacity: 0.5;
}

.time {
  color: #A09080;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: #A09080;
}

.land-type {
  padding: 3px 10px;
  background: #F7F3ED;
  border-radius: 6px;
  font-size: 11px;
  color: #8B7355;
}

.empty-list {
  text-align: center;
  padding: 60px 20px;
  color: #A09080;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.load-more {
  margin-top: 24px;
  text-align: center;
}

.load-more-btn {
  padding: 12px 48px;
  background: #fff;
  border: 1px solid #E8E2D9;
  border-radius: 24px;
  color: #8B7355;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.load-more-btn:hover {
  border-color: #C9A86C;
  color: #C9A86C;
}

/* 侧边栏 */
.news-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-card {
  background: #fff;
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  padding: 20px;
}

.side-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F0EBE3;
}

.side-icon {
  font-size: 18px;
}

.side-title {
  font-size: 15px;
  font-weight: 600;
  color: #2C261E;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.rank-item:hover {
  background: #FAFAF8;
}

.rank-num {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #A09080;
  background: #F5F2ED;
  border-radius: 4px;
  flex-shrink: 0;
}

.rank-num.top {
  background: linear-gradient(135deg, #FF6C37 0%, #FF8F5E 100%);
  color: #fff;
}

.rank-title {
  flex: 1;
  font-size: 13px;
  color: #5D4E37;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.rank-views {
  font-size: 11px;
  color: #A09080;
  flex-shrink: 0;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.recommend-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: background 0.2s;
}

.recommend-item:hover {
  background: #FAFAF8;
}

.rec-cover {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  background: linear-gradient(135deg, #F5F2ED 0%, #E8E2D9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rec-icon {
  font-size: 24px;
  opacity: 0.5;
}

.rec-body {
  flex: 1;
  min-width: 0;
}

.rec-title {
  font-size: 13px;
  color: #5D4E37;
  line-height: 1.5;
  margin: 0 0 4px 0;
  font-weight: 500;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rec-time {
  font-size: 11px;
  color: #A09080;
}

.land-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.land-tag {
  padding: 6px 12px;
  background: #F7F3ED;
  border: 1px solid #E8E2D9;
  border-radius: 20px;
  font-size: 12px;
  color: #8B7355;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.land-tag:hover {
  border-color: #C9A86C;
  color: #C9A86C;
}

.land-tag.active {
  background: #C9A86C;
  border-color: #C9A86C;
  color: #fff;
}

.tag-count {
  font-size: 10px;
  opacity: 0.8;
}

.stats-grid-mini {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-mini {
  text-align: center;
  padding: 16px 8px;
  background: #FAFAF8;
  border-radius: 12px;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #C9A86C;
  font-family: 'Courier New', monospace;
}

.stat-label {
  font-size: 12px;
  color: #8B7355;
  margin-top: 4px;
}

/* 详情弹窗 */
.detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(44, 38, 30, 0.6);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.detail-modal {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 800px;
  max-height: 85vh;
  position: relative;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 20px;
  width: 36px;
  height: 36px;
  border: none;
  background: #F7F3ED;
  border-radius: 50%;
  font-size: 24px;
  color: #8B7355;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #E8E2D9;
  color: #5D4E37;
}

.detail-scroll {
  max-height: 85vh;
  overflow-y: auto;
}

.detail-article {
  padding: 40px 48px;
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.article-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  color: #2C261E;
  line-height: 1.5;
  margin: 0 0 16px 0;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8B7355;
  flex-wrap: wrap;
}

.article-summary {
  margin-top: 20px;
  padding: 16px 20px;
  background: #FAFAF8;
  border-left: 4px solid #C9A86C;
  border-radius: 0 8px 8px 0;
}

.summary-label {
  font-size: 11px;
  letter-spacing: 0.15em;
  color: #C9A86C;
  font-weight: 600;
  text-transform: uppercase;
}

.article-summary p {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #5D4E37;
  line-height: 1.8;
}

.article-content {
  font-size: 15px;
  line-height: 2;
  color: #3D3528;
  margin-bottom: 32px;
  white-space: pre-wrap;
}

.article-footer {
  border-top: 1px solid #E8E2D9;
  padding-top: 24px;
}

.footer-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  padding: 12px 24px;
  border: 1px solid #E8E2D9;
  background: #fff;
  border-radius: 12px;
  font-size: 14px;
  color: #5D4E37;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.action-btn:hover {
  border-color: #C9A86C;
  color: #C9A86C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.15);
}

.action-btn.like {
  background: linear-gradient(135deg, #FF6C37 0%, #FF8F5E 100%);
  border-color: #FF6C37;
  color: #fff;
}

.footer-keywords {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.kw-label {
  font-size: 13px;
  color: #8B7355;
}

.kw-tag {
  padding: 4px 12px;
  background: #F7F3ED;
  border-radius: 16px;
  font-size: 12px;
  color: #8B7355;
}

.footer-ai-summary {
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.08) 0%, rgba(184, 149, 106, 0.08) 100%);
  border: 1px solid rgba(201, 168, 108, 0.2);
  border-radius: 12px;
  padding: 16px 20px;
}

.ai-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #B8956A;
  margin-bottom: 8px;
}

.footer-ai-summary p {
  margin: 0;
  font-size: 13px;
  color: #5D4E37;
  line-height: 1.8;
}

.mono {
  font-family: 'SF Mono', Menlo, 'Courier New', monospace;
}
</style>
