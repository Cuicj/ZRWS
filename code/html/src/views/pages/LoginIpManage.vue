<template>
  <div class="page-container">
    <div class="page-head">
      <div>
        <h1 class="page-title display">登录IP限制</h1>
        <div class="page-meta mono">LOGIN IP RULES · {{ total }} 条规则</div>
      </div>
      <button class="btn-primary" @click="openCreate">+ 添加规则</button>
    </div>

    <div class="stat-row">
      <StatCard label="总规则数" :value="total" icon="⊞" variant="accent" />
      <StatCard label="白名单" :value="whitelistCount" icon="●" variant="ok" />
      <StatCard label="黑名单" :value="blacklistCount" icon="○" variant="danger" />
      <StatCard label="启用中" :value="enabledCount" icon="◉" variant="warn" />
    </div>

    <Panel title="IP规则列表">
      <div class="toolbar">
        <el-select v-model="filterType" placeholder="规则类型" size="small" style="width:120px" clearable @change="loadRules">
          <el-option label="白名单" value="WHITELIST" />
          <el-option label="黑名单" value="BLACKLIST" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" size="small" style="width:120px" clearable @change="loadRules">
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <button class="btn-outline btn-sm" @click="loadRules">筛选</button>
        <button class="btn-ghost btn-sm" @click="resetFilter">重置</button>
      </div>
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="!rules.length" class="loading-tip">暂无规则数据</div>
      <table v-else>
        <thead><tr><th>ID</th><th>IP地址</th><th>类型</th><th>状态</th><th>备注</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in rules" :key="r.id">
            <td class="mono">{{ r.id }}</td>
            <td class="mono">{{ r.ipAddress }}</td>
            <td>
              <span class="status-badge" :class="r.ruleType === 'WHITELIST' ? 'status-ok' : 'status-err'">
                {{ r.ruleType === 'WHITELIST' ? '白名单' : '黑名单' }}
              </span>
            </td>
            <td>
              <span class="status-badge" :class="r.status === 'ENABLED' ? 'status-ok' : 'status-warn'">
                {{ r.status === 'ENABLED' ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ r.remark || '-' }}</td>
            <td class="mono">{{ formatTime(r.createdTime) }}</td>
            <td>
              <button class="btn-ghost btn-sm" @click="openEdit(r)">编辑</button>
              <button class="btn-ghost btn-sm" @click="toggle(r)" style="margin-left:4px">{{ r.status === 'ENABLED' ? '禁用' : '启用' }}</button>
              <button class="btn-ghost btn-sm btn-danger" @click="handleDelete(r)" style="margin-left:4px">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > size">
        <button class="btn-ghost btn-sm" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span class="page-info mono">第 {{ page }} / {{ totalPages }} 页</span>
        <button class="btn-ghost btn-sm" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </Panel>

    <!-- 新建/编辑对话框 -->
    <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
      <div class="modal-card">
        <div class="modal-head">
          <h3>{{ editMode ? '编辑规则' : '添加规则' }}</h3>
          <button class="modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>IP地址<span class="req">*</span></label>
            <input v-model="form.ipAddress" placeholder="如: 192.168.1.1 或 192.168.1.0/24" />
            <div class="form-hint">支持精确IP或CIDR网段（如 10.0.0.0/8）</div>
          </div>
          <div class="form-row">
            <label>规则类型<span class="req">*</span></label>
            <select v-model="form.ruleType">
              <option value="BLACKLIST">黑名单（禁止登录）</option>
              <option value="WHITELIST">白名单（允许登录）</option>
            </select>
            <div class="form-hint">白名单优先：存在白名单时，仅白名单中的IP可登录</div>
          </div>
          <div class="form-row">
            <label>备注</label>
            <input v-model="form.remark" placeholder="可选备注" />
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn-ghost" @click="dialogVisible = false">取消</button>
          <button class="btn-primary" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '确定' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Panel from '@/components/common/Panel.vue';
import StatCard from '@/components/common/StatCard.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getList, createRule, updateRule, deleteRule, toggleStatus } from '@/api/loginIpRule';

const rules = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const filterType = ref('');
const filterStatus = ref('');
const loading = ref(false);

const dialogVisible = ref(false);
const editMode = ref(false);
const submitting = ref(false);
const form = ref({ ipAddress: '', ruleType: 'BLACKLIST', remark: '' });
const editingId = ref(null);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
const whitelistCount = computed(() => rules.value.filter(r => r.ruleType === 'WHITELIST').length);
const blacklistCount = computed(() => rules.value.filter(r => r.ruleType === 'BLACKLIST').length);
const enabledCount = computed(() => rules.value.filter(r => r.status === 'ENABLED').length);

function formatTime(t) {
  if (!t) return '-';
  return String(t).replace('T', ' ').substring(0, 16);
}

async function loadRules() {
  loading.value = true;
  try {
    const res = await getList({ page: page.value, size: size.value, ruleType: filterType.value, status: filterStatus.value });
    rules.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } catch (e) {
    ElMessage.error('加载规则列表失败');
    rules.value = [];
  } finally {
    loading.value = false;
  }
}

function resetFilter() {
  filterType.value = '';
  filterStatus.value = '';
  page.value = 1;
  loadRules();
}

function changePage(p) {
  page.value = p;
  loadRules();
}

function openCreate() {
  editMode.value = false;
  form.value = { ipAddress: '', ruleType: 'BLACKLIST', remark: '' };
  editingId.value = null;
  dialogVisible.value = true;
}

function openEdit(r) {
  editMode.value = true;
  editingId.value = r.id;
  form.value = { ipAddress: r.ipAddress, ruleType: r.ruleType, remark: r.remark || '' };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!form.value.ipAddress) {
    ElMessage.warning('请填写IP地址');
    return;
  }
  submitting.value = true;
  try {
    const data = {
      ipAddress: form.value.ipAddress,
      ruleType: form.value.ruleType,
      remark: form.value.remark,
      status: 'ENABLED'
    };
    if (editMode.value) {
      await updateRule(editingId.value, data);
      ElMessage.success('修改成功');
    } else {
      await createRule(data);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    loadRules();
  } catch (e) {
    ElMessage.error(editMode.value ? '修改失败' : '添加失败');
  } finally {
    submitting.value = false;
  }
}

async function toggle(r) {
  const newStatus = r.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  try {
    await toggleStatus(r.id, newStatus);
    ElMessage.success(newStatus === 'ENABLED' ? '已启用' : '已禁用');
    loadRules();
  } catch (e) {
    ElMessage.error('操作失败');
  }
}

async function handleDelete(r) {
  try {
    await ElMessageBox.confirm('确认删除该IP规则?', '提示', { type: 'warning' });
    await deleteRule(r.id);
    ElMessage.success('删除成功');
    loadRules();
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败');
  }
}

onMounted(() => {
  loadRules();
});
</script>

<style scoped>
.form-hint {
  font-size: 11px;
  color: var(--signal-dim);
  margin-top: 4px;
}
</style>
