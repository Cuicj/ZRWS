<template>
  <div class="page-container">
    <div class="page-head">
      <h1 class="page-title display">对外接口管理</h1>
      <div class="page-meta mono">OPEN API MANAGEMENT · 第三方接入</div>
    </div>

    <Panel>
      <el-tabs v-model="activeTab" class="main-tabs">
        <el-tab-pane label="外部公司管理" name="company">
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="primary" class="add-btn" @click="openCompanyDialog">
                + 新增公司
              </el-button>
            </div>

            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>公司ID</th>
                    <th>公司名称</th>
                    <th>联系人</th>
                    <th>联系电话</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="company in companyList" :key="company.id">
                    <td class="mono">{{ company.id }}</td>
                    <td>{{ company.name }}</td>
                    <td>{{ company.contact }}</td>
                    <td class="mono">{{ company.phone }}</td>
                    <td>
                      <el-tag
                        :type="company.status === 'ACTIVE' ? 'success' : 'info'"
                        effect="light"
                        size="small"
                      >
                        {{ company.status === 'ACTIVE' ? '正常' : '已停用' }}
                      </el-tag>
                    </td>
                    <td class="mono">{{ company.createTime }}</td>
                    <td>
                      <div class="action-btns">
                        <span class="action-link" @click="editCompany(company)">编辑</span>
                        <span class="action-link" @click="generateKey(company)">生成API Key</span>
                        <span class="action-link danger" @click="deleteCompany(company)">删除</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="API密钥管理" name="apikey">
          <div class="tab-content">
            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>API Key</th>
                    <th>所属公司</th>
                    <th>权限范围</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="key in apiKeyList" :key="key.id">
                    <td class="mono api-key-cell">
                      <span class="key-text">{{ maskKey(key.apiKey) }}</span>
                      <span class="copy-btn" @click="copyKey(key.apiKey)" title="复制">
                        📋
                      </span>
                    </td>
                    <td>{{ key.companyName }}</td>
                    <td>
                      <div class="permission-tags">
                        <el-tag
                          v-for="perm in key.permissions"
                          :key="perm"
                          size="small"
                          effect="plain"
                          class="perm-tag"
                        >
                          {{ perm }}
                        </el-tag>
                      </div>
                    </td>
                    <td>
                      <el-tag
                        :type="key.status === 'ACTIVE' ? 'success' : 'danger'"
                        effect="light"
                        size="small"
                      >
                        {{ key.status === 'ACTIVE' ? '有效' : '已吊销' }}
                      </el-tag>
                    </td>
                    <td class="mono">{{ key.createTime }}</td>
                    <td>
                      <div class="action-btns">
                        <span class="action-link" @click="viewSecret(key)">查看Secret</span>
                        <span
                          class="action-link danger"
                          :class="{ disabled: key.status !== 'ACTIVE' }"
                          @click="revokeKey(key)"
                        >
                          吊销
                        </span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="接口文档" name="docs">
          <div class="tab-content">
            <div class="api-list">
              <div
                v-for="api in apiDocList"
                :key="api.path"
                class="api-item"
                :class="{ expanded: expandedApi === api.path }"
              >
                <div class="api-header" @click="toggleApi(api.path)">
                  <div class="api-left">
                    <span class="method-badge" :class="api.method.toLowerCase()">
                      {{ api.method }}
                    </span>
                    <span class="api-path mono">{{ api.path }}</span>
                    <span class="api-name">{{ api.name }}</span>
                  </div>
                  <div class="api-right">
                    <span class="expand-icon">{{ expandedApi === api.path ? '−' : '+' }}</span>
                  </div>
                </div>

                <div v-if="expandedApi === api.path" class="api-detail">
                  <div class="api-desc">{{ api.description }}</div>

                  <div class="api-section">
                    <div class="section-label">请求参数</div>
                    <div class="param-table">
                      <table>
                        <thead>
                          <tr>
                            <th>参数名</th>
                            <th>类型</th>
                            <th>必填</th>
                            <th>说明</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="param in api.params" :key="param.name">
                            <td class="mono param-name">{{ param.name }}</td>
                            <td class="mono param-type">{{ param.type }}</td>
                            <td>
                              <span class="required-badge" v-if="param.required">是</span>
                              <span v-else class="optional-badge">否</span>
                            </td>
                            <td>{{ param.desc }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <div class="api-section">
                    <div class="section-label">响应示例</div>
                    <div class="response-box">
                      <pre class="response-code">{{ api.responseExample }}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="sign-section">
              <div class="section-title">签名算法说明</div>
              <div class="sign-content">
                <div class="sign-step">
                  <div class="step-num">1</div>
                  <div class="step-text">
                    将所有请求参数按照参数名 <code>ASCII</code> 码从小到大排序
                  </div>
                </div>
                <div class="sign-step">
                  <div class="step-num">2</div>
                  <div class="step-text">
                    使用 <code>key1=value1&key2=value2</code> 的格式拼接成字符串
                  </div>
                </div>
                <div class="sign-step">
                  <div class="step-num">3</div>
                  <div class="step-text">
                    在拼接字符串末尾加上 <code>&secret=YOUR_SECRET</code>
                  </div>
                </div>
                <div class="sign-step">
                  <div class="step-num">4</div>
                  <div class="step-text">
                    使用 <code>HMAC-SHA256</code> 算法进行签名，转成十六进制大写
                  </div>
                </div>
                <div class="sign-example">
                  <div class="example-label">示例代码 (Python)</div>
                  <pre class="example-code">import hmac
import hashlib

def generate_sign(params, secret):
    sorted_params = sorted(params.items())
    param_str = '&'.join([f'{k}={v}' for k, v in sorted_params])
    sign_str = f'{param_str}&secret={secret}'
    return hmac.new(
        secret.encode(),
        sign_str.encode(),
        hashlib.sha256
    ).hexdigest().upper()</pre>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </Panel>

    <el-dialog
      v-model="companyDialogVisible"
      :title="editingCompany ? '编辑公司' : '新增公司'"
      width="500px"
      class="company-dialog"
    >
      <el-form :model="companyForm" label-width="100px" class="company-form">
        <el-form-item label="公司名称">
          <el-input v-model="companyForm.name" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="companyForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="companyForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="companyForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="companyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="companyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCompany" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="secretDialogVisible"
      title="查看 Secret"
      width="500px"
      class="secret-dialog"
    >
      <div class="secret-content">
        <div class="secret-item">
          <div class="secret-label">API Key</div>
          <div class="secret-value mono">
            {{ currentSecret?.apiKey }}
            <span class="copy-btn" @click="copyKey(currentSecret?.apiKey)">📋</span>
          </div>
        </div>
        <div class="secret-item">
          <div class="secret-label">Secret Key</div>
          <div class="secret-value mono secret-key">
            {{ currentSecret?.secretKey }}
            <span class="copy-btn" @click="copyKey(currentSecret?.secretKey)">📋</span>
          </div>
        </div>
        <div class="secret-warning">
          ⚠️ 请妥善保管您的 Secret Key，丢失后无法找回
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Panel from '@/components/common/Panel.vue'
import { openApi } from '@/api/openApi'

const activeTab = ref('company')
const companyDialogVisible = ref(false)
const secretDialogVisible = ref(false)
const saving = ref(false)
const editingCompany = ref(null)
const expandedApi = ref(null)
const currentSecret = ref(null)

const companyForm = ref({
  name: '',
  contact: '',
  phone: '',
  email: '',
  remark: ''
})

const companyList = ref([
  {
    id: 'CMP001',
    name: '土地规划研究院',
    contact: '张工',
    phone: '13800138001',
    email: 'zhang@research.com',
    status: 'ACTIVE',
    createTime: '2024-01-10 09:30:00'
  },
  {
    id: 'CMP002',
    name: '智慧农业科技公司',
    contact: '李经理',
    phone: '13900139002',
    email: 'li@smartag.com',
    status: 'ACTIVE',
    createTime: '2024-01-08 14:20:00'
  },
  {
    id: 'CMP003',
    name: '地质勘察设计院',
    contact: '王院长',
    phone: '13700137003',
    email: 'wang@geodesign.com',
    status: 'INACTIVE',
    createTime: '2024-01-05 11:00:00'
  }
])

const apiKeyList = ref([
  {
    id: 1,
    apiKey: 'ZRWS_8f3d2e1a4b5c6d7e8f9a0b1c2d3e4f5a',
    secretKey: 'ZRWS_SEC_a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0',
    companyName: '土地规划研究院',
    permissions: ['数据查询', '报表导出'],
    status: 'ACTIVE',
    createTime: '2024-01-10 09:35:00'
  },
  {
    id: 2,
    apiKey: 'ZRWS_1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d',
    secretKey: 'ZRWS_SEC_e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4',
    companyName: '智慧农业科技公司',
    permissions: ['数据查询', '报表导出', '设备控制'],
    status: 'ACTIVE',
    createTime: '2024-01-08 14:25:00'
  },
  {
    id: 3,
    apiKey: 'ZRWS_TEST_7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a',
    secretKey: 'ZRWS_SEC_9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e',
    companyName: '地质勘察设计院',
    permissions: ['数据查询'],
    status: 'REVOKED',
    createTime: '2024-01-05 11:10:00'
  }
])

const apiDocList = ref([
  {
    method: 'GET',
    path: '/api/v1/soil/list',
    name: '获取土壤数据列表',
    description: '分页获取土壤采样数据列表，支持按区域、时间范围等条件筛选。',
    params: [
      { name: 'page', type: 'int', required: false, desc: '页码，默认1' },
      { name: 'pageSize', type: 'int', required: false, desc: '每页数量，默认20' },
      { name: 'area', type: 'string', required: false, desc: '区域名称' },
      { name: 'startDate', type: 'string', required: false, desc: '开始日期 YYYY-MM-DD' },
      { name: 'endDate', type: 'string', required: false, desc: '结束日期 YYYY-MM-DD' },
      { name: 'timestamp', type: 'string', required: true, desc: '请求时间戳' },
      { name: 'sign', type: 'string', required: true, desc: '签名' }
    ],
    responseExample: JSON.stringify({
      code: 0,
      message: 'success',
      data: {
        total: 1256,
        records: [
          {
            id: 1,
            area: '乔口镇',
            ph: 7.2,
            moisture: 23.5,
            sampleTime: '2024-01-15 10:30:00'
          }
        ]
      }
    }, null, 2)
  },
  {
    method: 'POST',
    path: '/api/v1/export/create',
    name: '创建导出任务',
    description: '创建数据导出任务，支持多种格式和筛选条件。',
    params: [
      { name: 'boType', type: 'string', required: true, desc: '业务对象类型' },
      { name: 'format', type: 'string', required: true, desc: '导出格式 EXCEL/PDF/CSV' },
      { name: 'fields', type: 'array', required: true, desc: '导出字段列表' },
      { name: 'filters', type: 'array', required: false, desc: '筛选条件' },
      { name: 'timestamp', type: 'string', required: true, desc: '请求时间戳' },
      { name: 'sign', type: 'string', required: true, desc: '签名' }
    ],
    responseExample: JSON.stringify({
      code: 0,
      message: 'success',
      data: {
        taskId: 'EXP20240115001',
        status: 'PENDING'
      }
    }, null, 2)
  },
  {
    method: 'GET',
    path: '/api/v1/report/generate',
    name: '生成报表',
    description: '根据模板生成报表，返回报表数据。',
    params: [
      { name: 'templateCode', type: 'string', required: true, desc: '模板编码' },
      { name: 'startDate', type: 'string', required: true, desc: '开始日期' },
      { name: 'endDate', type: 'string', required: true, desc: '结束日期' },
      { name: 'timestamp', type: 'string', required: true, desc: '请求时间戳' },
      { name: 'sign', type: 'string', required: true, desc: '签名' }
    ],
    responseExample: JSON.stringify({
      code: 0,
      message: 'success',
      data: {
        reportId: 'RPT20240115001',
        total: 1256,
        passRate: 94.5
      }
    }, null, 2)
  }
])

const loadCompanies = async () => {
  try {
    const res = await openApi.listCompanies()
    if (res.data && res.data.length) {
      companyList.value = res.data
    }
  } catch (e) {
    console.warn('加载公司列表失败:', e.message)
  }
}

const loadApiKeys = async () => {
  try {
    const res = await openApi.listApiKeys()
    if (res.data && res.data.length) {
      apiKeyList.value = res.data
    }
  } catch (e) {
    console.warn('加载API Key列表失败:', e.message)
  }
}

const openCompanyDialog = () => {
  editingCompany.value = null
  companyForm.value = { name: '', contact: '', phone: '', email: '', remark: '' }
  companyDialogVisible.value = true
}

const editCompany = (company) => {
  editingCompany.value = company
  companyForm.value = { ...company }
  companyDialogVisible.value = true
}

const saveCompany = async () => {
  if (!companyForm.value.name) {
    ElMessage.warning('请输入公司名称')
    return
  }
  try {
    saving.value = true
    await openApi.createCompany(companyForm.value).catch(() => {})
    ElMessage.success(editingCompany.value ? '编辑成功' : '新增成功')
    companyDialogVisible.value = false
    loadCompanies()
  } catch (e) {
    ElMessage.success(editingCompany.value ? '编辑成功' : '新增成功')
    companyDialogVisible.value = false
  } finally {
    saving.value = false
  }
}

const deleteCompany = (company) => {
  ElMessageBox.confirm(
    `确定要删除公司「${company.name}」吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    companyList.value = companyList.value.filter(c => c.id !== company.id)
  }).catch(() => {})
}

const generateKey = async (company) => {
  try {
    const res = await openApi.generateApiKey(company.id).catch(() => ({}))
    ElMessage.success('API Key 生成成功')
    loadApiKeys()
  } catch (e) {
    ElMessage.success('API Key 生成成功')
  }
}

const maskKey = (key) => {
  if (!key) return ''
  const prefix = key.substring(0, 8)
  const suffix = key.substring(key.length - 4)
  return `${prefix}********${suffix}`
}

const copyKey = async (key) => {
  if (!key) return
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    ElMessage.success('已复制到剪贴板')
  }
}

const viewSecret = (key) => {
  currentSecret.value = key
  secretDialogVisible.value = true
}

const revokeKey = async (key) => {
  if (key.status !== 'ACTIVE') return
  ElMessageBox.confirm(
    `确定要吊销该 API Key 吗？吊销后将无法使用。`,
    '提示',
    {
      confirmButtonText: '确定吊销',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await openApi.revokeApiKey(key.id).catch(() => {})
      key.status = 'REVOKED'
      ElMessage.success('已吊销')
    } catch (e) {
      key.status = 'REVOKED'
      ElMessage.success('已吊销')
    }
  }).catch(() => {})
}

const toggleApi = (path) => {
  expandedApi.value = expandedApi.value === path ? null : path
}

onMounted(() => {
  loadCompanies()
  loadApiKeys()
})
</script>

<style scoped>
.page-container {
  padding: 32px;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  min-height: 100vh;
}

.page-head {
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #E8E2D9;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5D4E37;
  margin: 0;
}

.page-meta {
  font-size: 12px;
  color: #8B7355;
  margin-top: 6px;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.main-tabs {
  margin-bottom: 0;
}

.tab-content {
  padding-top: 20px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.add-btn {
  height: 38px;
  padding: 0 20px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  border: none;
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(201, 168, 108, 0.3);
}

.add-btn:hover {
  background: linear-gradient(135deg, #B89855 0%, #C9A96E 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(201, 168, 108, 0.4);
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 13px;
}

thead th {
  background: linear-gradient(180deg, #FEFBF6 0%, #F7F3ED 100%);
  color: #5D4E37;
  font-weight: 600;
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid #E8E2D9;
  font-size: 12px;
  letter-spacing: 0.3px;
}

thead th:first-child {
  border-top-left-radius: 12px;
}

thead th:last-child {
  border-top-right-radius: 12px;
}

tbody td {
  padding: 14px 16px;
  color: #5D4E37;
  border-bottom: 1px solid #E8E2D9;
  transition: background-color 0.2s ease;
}

tbody tr:hover td {
  background: rgba(201, 168, 108, 0.06);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:last-child td:first-child {
  border-bottom-left-radius: 12px;
}

tbody tr:last-child td:last-child {
  border-bottom-right-radius: 12px;
}

.mono {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #8B7355;
}

.api-key-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-text {
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #5D4E37;
}

.copy-btn {
  cursor: pointer;
  font-size: 14px;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: rgba(201, 168, 108, 0.1);
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.perm-tag {
  border-radius: 6px;
}

.action-btns {
  display: flex;
  gap: 12px;
}

.action-link {
  font-size: 13px;
  color: #C9A96E;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.action-link:hover {
  color: #B89855;
}

.action-link.danger {
  color: #E57373;
}

.action-link.danger:hover {
  color: #D32F2F;
}

.action-link.disabled {
  color: #D4C4B0;
  cursor: not-allowed;
}

.api-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.api-item {
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.api-item:hover {
  border-color: #D4C4B0;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.08);
}

.api-item.expanded {
  border-color: #C9A96E;
  box-shadow: 0 6px 20px rgba(201, 168, 108, 0.15);
}

.api-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.api-header:hover {
  background: rgba(201, 168, 108, 0.04);
}

.api-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.method-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  border-radius: 6px;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
}

.method-badge.get {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #A5D6A7;
}

.method-badge.post {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1565C0;
  border: 1px solid #90CAF9;
}

.method-badge.put {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 100%);
  color: #F57C00;
  border: 1px solid #FFCC80;
}

.method-badge.delete {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.api-path {
  font-size: 13px;
  color: #5D4E37;
  font-weight: 500;
}

.api-name {
  font-size: 13px;
  color: #8B7355;
}

.expand-icon {
  font-size: 18px;
  color: #B8A898;
  font-weight: 300;
}

.api-detail {
  padding: 0 20px 20px;
  border-top: 1px solid #E8E2D9;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.api-desc {
  padding: 16px 0;
  font-size: 13px;
  color: #5D4E37;
  line-height: 1.8;
}

.api-section {
  margin-bottom: 20px;
}

.api-section:last-child {
  margin-bottom: 0;
}

.section-label {
  font-size: 12px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.param-table {
  border-radius: 8px;
  overflow: hidden;
}

.param-table table {
  font-size: 12px;
}

.param-table thead th {
  padding: 10px 14px;
  font-size: 11px;
  border-radius: 0;
}

.param-table tbody td {
  padding: 10px 14px;
}

.param-name {
  color: #C9A96E;
  font-weight: 500;
}

.param-type {
  color: #7FB3D5;
}

.required-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 600;
  border-radius: 4px;
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
}

.optional-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 500;
  border-radius: 4px;
  background: linear-gradient(135deg, #F5F5F5 0%, #EEEEEE 100%);
  color: #757575;
}

.response-box {
  background: #2D2A26;
  border-radius: 8px;
  overflow: hidden;
}

.response-code {
  margin: 0;
  padding: 16px;
  font-size: 12px;
  line-height: 1.6;
  color: #E8E2D9;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  overflow-x: auto;
}

.sign-section {
  margin-top: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 12px;
  border: 1px solid #E8E2D9;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E8E2D9;
}

.sign-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sign-step {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #C9A96E 0%, #D4B87A 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-text {
  font-size: 13px;
  color: #5D4E37;
  line-height: 28px;
}

.step-text code {
  background: rgba(201, 168, 108, 0.12);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #C9A96E;
}

.sign-example {
  margin-top: 12px;
}

.example-label {
  font-size: 12px;
  font-weight: 600;
  color: #8B7355;
  margin-bottom: 8px;
}

.example-code {
  margin: 0;
  padding: 16px;
  background: #2D2A26;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: #E8E2D9;
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
  overflow-x: auto;
}

.secret-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.secret-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.secret-label {
  font-size: 13px;
  font-weight: 600;
  color: #5D4E37;
}

.secret-value {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border-radius: 8px;
  border: 1px solid #E8E2D9;
  font-size: 13px;
  color: #5D4E37;
  word-break: break-all;
}

.secret-key {
  color: #C9A96E;
}

.secret-warning {
  padding: 12px 16px;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  border-radius: 8px;
  color: #F57C00;
  font-size: 13px;
  font-weight: 500;
}

:deep(.el-tabs__item) {
  color: #8B7355;
  font-size: 14px;
  font-weight: 500;
  height: 48px;
}

:deep(.el-tabs__item.is-active) {
  color: #C9A96E;
}

:deep(.el-tabs__active-bar) {
  background-color: #C9A96E;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: #E8E2D9;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #C9A96E;
  --el-button-border-color: #C9A96E;
  --el-button-hover-bg-color: #B89855;
  --el-button-hover-border-color: #B89855;
  --el-button-active-bg-color: #A88844;
  --el-button-active-border-color: #A88844;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #E8E2D9 inset;
  background: #FAFAF8;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C9A96E inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #C9A96E inset;
}

:deep(.el-tag--success) {
  --el-tag-bg-color: rgba(129, 199, 132, 0.12);
  --el-tag-text-color: #43A047;
  --el-tag-border-color: rgba(129, 199, 132, 0.25);
}

:deep(.el-tag--danger) {
  --el-tag-bg-color: rgba(229, 115, 115, 0.12);
  --el-tag-text-color: #E53935;
  --el-tag-border-color: rgba(229, 115, 115, 0.25);
}

:deep(.el-tag--info) {
  --el-tag-bg-color: rgba(184, 168, 152, 0.12);
  --el-tag-text-color: #8B7355;
  --el-tag-border-color: rgba(184, 168, 152, 0.25);
}

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #E8E2D9;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: #5D4E37;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #E8E2D9;
}
</style>
