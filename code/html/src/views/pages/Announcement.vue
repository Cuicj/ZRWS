<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">公告管理</h1>
        <div class="page-meta mono">ANNOUNCEMENT · {{ announcements.length }} 条公告</div>
      </div>
      <button class="btn btn-primary btn-sm" @click="showCreate = true">+ 发布公告</button>
    </div>

    <div class="stat-row">
      <StatCard label="公告总数" :value="announcements.length" icon="✉" variant="accent" />
      <StatCard label="已发布" :value="publishedCount" icon="✓" variant="ok" />
      <StatCard label="草稿" :value="draftCount" icon="✎" variant="warn" />
      <StatCard label="置顶公告" :value="topCount" icon="★" variant="accent" />
    </div>

    <Panel title="公告列表">
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索公告标题" size="small" style="width:240px" clearable />
        <el-select v-model="categoryFilter" placeholder="分类筛选" size="small" clearable style="width:140px">
          <el-option label="系统公告" value="system" />
          <el-option label="业务通知" value="business" />
          <el-option label="维护通知" value="maintenance" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态筛选" size="small" clearable style="width:140px">
          <el-option label="已发布" value="published" />
          <el-option label="草稿" value="draft" />
          <el-option label="已下线" value="offline" />
        </el-select>
      </div>
      <table>
        <thead><tr><th>ID</th><th>标题</th><th>分类</th><th>状态</th><th>置顶</th><th>发布人</th><th>发布时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="a in filteredAnnouncements" :key="a.id">
            <td class="mono">{{ a.id }}</td>
            <td class="title-cell">
              <span v-if="a.isTop" class="top-badge">置顶</span>
              {{ a.title }}
            </td>
            <td><span class="category-badge" :class="'cat-' + a.category">{{ a.categoryText }}</span></td>
            <td><span class="status-badge" :class="a.statusClass">{{ a.statusText }}</span></td>
            <td>{{ a.isTop ? '是' : '否' }}</td>
            <td>{{ a.publisher }}</td>
            <td class="mono">{{ a.publishTime }}</td>
            <td>
              <button class="btn btn-ghost btn-sm">编辑</button>
              <button class="btn btn-ghost btn-sm" style="margin-left:4px">查看</button>
              <button class="btn btn-ghost btn-sm btn-danger" style="margin-left:4px">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <!-- 发布公告弹窗 -->
    <el-dialog v-model="showCreate" title="发布公告" width="600px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="系统公告" value="system" />
            <el-option label="业务通知" value="business" />
            <el-option label="维护通知" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="publish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { getAnnouncementList } from '@/api/announcement.js';

const searchKeyword = ref('');
const categoryFilter = ref('');
const statusFilter = ref('');
const showCreate = ref(false);
const loading = ref(false);

const form = ref({
  title: '',
  category: 'system',
  content: '',
  isTop: false
});

const announcements = ref([]);

const publishedCount = computed(() => announcements.value.filter(a => a.status === 'published').length);
const draftCount = computed(() => announcements.value.filter(a => a.status === 'draft').length);
const topCount = computed(() => announcements.value.filter(a => a.isTop).length);

const filteredAnnouncements = computed(() => {
  let list = announcements.value;
  if (searchKeyword.value) {
    list = list.filter(a => a.title.includes(searchKeyword.value));
  }
  if (categoryFilter.value) {
    list = list.filter(a => a.category === categoryFilter.value);
  }
  if (statusFilter.value) {
    list = list.filter(a => a.status === statusFilter.value);
  }
  return list;
});

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true;
  try {
    const res = await getAnnouncementList();
    if (res.code === 0 || res.code === 200) {
      // 根据后端返回的数据结构进行适配
      announcements.value = (res.data || []).map(item => ({
        id: item.id || item.announcementId,
        title: item.title,
        category: item.category || 'system',
        categoryText: getCategoryText(item.category),
        status: item.status || 'published',
        statusText: getStatusText(item.status),
        statusClass: getStatusClass(item.status),
        isTop: item.isTop || false,
        publisher: item.publisher || item.author,
        publishTime: item.publishTime || item.createTime,
        content: item.content
      }));
    } else {
      ElMessage.error(res.msg || '加载公告失败');
    }
  } catch (error) {
    console.error('加载公告失败:', error);
    ElMessage.error('加载公告失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 辅助函数：获取分类文本
const getCategoryText = (category) => {
  const map = {
    system: '系统公告',
    business: '业务通知',
    maintenance: '维护通知'
  };
  return map[category] || category;
};

// 辅助函数：获取状态文本
const getStatusText = (status) => {
  const map = {
    published: '已发布',
    draft: '草稿',
    offline: '已下线'
  };
  return map[status] || status;
};

// 辅助函数：获取状态样式类
const getStatusClass = (status) => {
  const map = {
    published: 'status-ok',
    draft: 'status-warn',
    offline: 'status-dim'
  };
  return map[status] || 'status-dim';
};

const saveDraft = () => {
  console.log('保存草稿');
  showCreate.value = false;
};

const publish = () => {
  console.log('发布公告');
  showCreate.value = false;
};

onMounted(() => {
  loadAnnouncements();
});
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-4); }
.toolbar { display: flex; gap: var(--s-2); margin-bottom: var(--s-3); }
.title-cell { display: flex; align-items: center; gap: 6px; }
.top-badge { padding: 1px 6px; background: var(--sand-500); color: var(--ink-900); border-radius: 3px; font-size: 11px; font-weight: 500; }
.category-badge { padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.cat-system { background: rgba(74, 144, 226, 0.15); color: #4a90e2; }
.cat-business { background: rgba(116, 192, 116, 0.15); color: #74c074; }
.cat-maintenance { background: rgba(230, 162, 60, 0.15); color: #e6a23c; }
</style>
