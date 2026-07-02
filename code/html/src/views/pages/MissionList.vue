<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">采集任务列表</h1>
        <div class="page-meta mono">MISSION LIST · {{ total }} 条记录</div>
      </div>
      <div class="page-actions">
        <el-input v-model="searchKey" placeholder="搜索任务 ID / 区域" size="small" clearable style="width:200px" />
        <el-select v-model="statusFilter" placeholder="状态" size="small" clearable style="width:120px">
          <el-option label="全部" value="" />
          <el-option label="已完成" value="completed" />
          <el-option label="进行中" value="processing" />
          <el-option label="待执行" value="pending" />
          <el-option label="异常" value="abnormal" />
        </el-select>
        <button class="btn btn-primary btn-sm" @click="showCreateDialog = true">+ 新建任务</button>
      </div>
    </div>

    <Panel>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>区域</th>
            <th>操作员</th>
            <th>覆盖面积</th>
            <th>航高</th>
            <th>照片数</th>
            <th>日期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in filteredTasks" :key="task.id">
            <td class="mono">{{ task.id }}</td>
            <td>{{ task.area }}</td>
            <td>{{ task.operator }}</td>
            <td>{{ task.coverage }} 亩</td>
            <td>{{ task.altitude }} m</td>
            <td>{{ task.photos }}</td>
            <td class="mono">{{ task.date }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(task.status)">
                {{ getStatusText(task.status) }}
              </span>
            </td>
            <td>
              <router-link to="/app/flight-control" class="btn btn-ghost btn-sm">详情</router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <el-dialog v-model="showCreateDialog" title="新建采集任务" width="500px">
      <el-form label-width="80px">
        <el-form-item label="任务名称">
          <el-input v-model="newTask.name" placeholder="如：望城区乔口镇测绘" />
        </el-form-item>
        <el-form-item label="目标区域">
          <el-input v-model="newTask.area" placeholder="行政区划或坐标范围" />
        </el-form-item>
        <el-form-item label="航高">
          <el-input-number v-model="newTask.altitude" :min="50" :max="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTask">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import { getMissionList, createMission } from '@/api/mission';

const searchKey = ref('');
const statusFilter = ref('');
const showCreateDialog = ref(false);
const loading = ref(true);

const tasks = ref([]);

const newTask = ref({ name: '', area: '', altitude: 120 });

const total = computed(() => tasks.value.length);

const filteredTasks = computed(() => {
  return tasks.value.filter(t => {
    if (statusFilter.value && t.status !== statusFilter.value) return false;
    if (searchKey.value && !(t.missionCode || t.id || '').includes(searchKey.value) && !(t.location || t.area || '').includes(searchKey.value)) return false;
    return true;
  });
});

const getStatusClass = (s) => ({
  COMPLETED: 'status-ok',
  PROCESSING: 'status-proc',
  PENDING: 'status-dim',
  ABNORMAL: 'status-err'
}[s] || 'status-dim');

const getStatusText = (s) => ({
  COMPLETED: '已完成',
  PROCESSING: '进行中',
  PENDING: '待执行',
  ABNORMAL: '异常'
}[s] || s);

const loadData = async () => {
  try {
    loading.value = true;
    const res = await getMissionList();
    if (res && res.list) {
      tasks.value = res.list.map(t => ({
        id: t.missionCode || t.id,
        area: t.location || t.area || '-',
        operator: t.operator || '-',
        coverage: t.coverageArea || t.coverage || 0,
        altitude: t.flightAltitude || t.altitude || 0,
        photos: t.photoCount || t.photos || 0,
        date: t.flightTime || t.createTime || '-',
        status: t.status || 'PENDING'
      }));
    }
  } catch (e) {
    console.warn('加载任务列表失败:', e.message);
  } finally {
    loading.value = false;
  }
};

const createTask = async () => {
  try {
    await createMission({
      missionName: newTask.value.name,
      location: newTask.value.area,
      flightAltitude: newTask.value.altitude
    });
    showCreateDialog.value = false;
    newTask.value = { name: '', area: '', altitude: 120 };
    loadData();
  } catch (e) {
    console.warn('创建任务失败:', e.message);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page-container {
  padding: 24px 32px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
  position: relative;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 400;
  color: #5D4E37;
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
  letter-spacing: -0.02em;
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.page-actions :deep(.el-input__wrapper) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-actions :deep(.el-input__wrapper:hover) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.15);
}

.page-actions :deep(.el-input__wrapper.is-focus) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.2);
}

.page-actions :deep(.el-input__inner) {
  color: #5D4E37;
}

.page-actions :deep(.el-input__inner::placeholder) {
  color: #8B7355;
  opacity: 0.6;
}

.page-actions :deep(.el-select__wrapper) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-actions :deep(.el-select__wrapper:hover) {
  border-color: #C9A86C;
}

.page-actions :deep(.el-select__wrapper.is-focused) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.2);
}

.page-actions :deep(.el-select__placeholder) {
  color: #8B7355;
  opacity: 0.6;
}

.page-actions :deep(.el-select__selected-item) {
  color: #5D4E37;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

th {
  text-align: left;
  padding: 12px 14px;
  font-size: 11px;
  color: #8B7355;
  font-weight: 600;
  letter-spacing: 0.1em;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(180deg, #F7F3ED 0%, #F0EBE3 100%);
}

th:first-child {
  border-top-left-radius: 12px;
}

th:last-child {
  border-top-right-radius: 12px;
}

td {
  padding: 14px 12px;
  border-bottom: 1px solid #F0EBE3;
  transition: background 0.15s ease;
  color: #5D4E37;
  font-size: 13px;
}

tr:hover td {
  background: rgba(201, 168, 108, 0.04);
}

tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.mono {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: #8B7355;
  font-size: 12px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(139, 115, 85, 0.06);
}

.status-ok {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #A5D6A7;
}

.status-proc {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1565C0;
  border: 1px solid #90CAF9;
}

.status-err {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.status-dim {
  background: linear-gradient(135deg, #F5F0E8 0%, #EDE8DF 100%);
  color: #8B7355;
  border: 1px solid #E8E2D9;
}

.display {
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
}

:deep(.el-dialog) {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(139, 115, 85, 0.16);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #E8E2D9;
  padding: 20px 24px;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: #5D4E37;
  font-family: 'Fraunces', 'Source Han Serif SC', serif;
  font-weight: 400;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  color: #5D4E37;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #E8E2D9;
  padding: 16px 24px;
}

:deep(.el-form-item__label) {
  color: #8B7355;
  font-size: 13px;
}

:deep(.el-input__wrapper) {
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-input__wrapper:hover) {
  border-color: #C9A86C;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #C9A86C;
  box-shadow: 0 2px 8px rgba(201, 168, 108, 0.2);
}

:deep(.el-input__inner) {
  color: #5D4E37;
}
</style>
