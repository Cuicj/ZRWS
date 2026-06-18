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
        <button class="btn-primary btn-sm" @click="showCreateDialog = true">+ 新建任务</button>
      </div>
    </div>

    <!-- 任务表格 -->
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
              <router-link to="/app/flight-control" class="btn-ghost btn-sm">详情</router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>

    <!-- 新建任务弹窗 -->
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
import { ref, computed } from 'vue';
import Panel from '@/components/common/Panel.vue';

const searchKey = ref('');
const statusFilter = ref('');
const showCreateDialog = ref(false);

const tasks = ref([
  { id: 'ZRS-2026-0617-001', area: '望城区乔口镇', operator: '王工', coverage: 860, altitude: 120, photos: 1247, date: '2026-06-17', status: 'completed' },
  { id: 'ZRS-2026-0616-003', area: '岳麓区莲花镇', operator: '李工', coverage: 1250, altitude: 100, photos: 2100, date: '2026-06-16', status: 'completed' },
  { id: 'ZRS-2026-0616-002', area: '雨花区跳马镇', operator: '王工', coverage: 680, altitude: 80, photos: 980, date: '2026-06-16', status: 'processing' },
  { id: 'ZRS-2026-0615-001', area: '开福区青竹湖', operator: '张工', coverage: 520, altitude: 110, photos: 760, date: '2026-06-15', status: 'completed' },
  { id: 'ZRS-2026-0614-002', area: '天心区暮云镇', operator: '李工', coverage: 320, altitude: 90, photos: 420, date: '2026-06-14', status: 'abnormal' }
]);

const newTask = ref({ name: '', area: '', altitude: 120 });

const total = computed(() => tasks.value.length);

const filteredTasks = computed(() => {
  return tasks.value.filter(t => {
    if (statusFilter.value && t.status !== statusFilter.value) return false;
    if (searchKey.value && !t.id.includes(searchKey.value) && !t.area.includes(searchKey.value)) return false;
    return true;
  });
});

const getStatusClass = (s) => ({
  completed: 'status-ok',
  processing: 'status-proc',
  pending: 'status-dim',
  abnormal: 'status-err'
}[s] || 'status-dim');

const getStatusText = (s) => ({
  completed: '已完成',
  processing: '进行中',
  pending: '待执行',
  abnormal: '异常'
}[s] || s);

const createTask = () => {
  const id = 'ZRS-2026-' + new Date().toISOString().slice(5,10).replace(/-/g,'') + '-' + String(tasks.value.length + 1).padStart(3,'0');
  tasks.value.unshift({ ...newTask.value, id, operator: '王工', coverage: 0, photos: 0, date: new Date().toISOString().slice(0,10), status: 'pending' });
  showCreateDialog.value = false;
};
</script>

<style scoped>
.page-container { padding: var(--s-5); }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: var(--s-4); margin-bottom: var(--s-5); border-bottom: var(--line); }
.page-title { font-size: 28px; font-weight: 200; }
.page-meta { font-size: 11px; color: var(--signal-dim); margin-top: 4px; }
.page-actions { display: flex; gap: var(--s-3); align-items: center; }
</style>