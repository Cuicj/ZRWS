<template>
  <div class="playground">
    <aside class="sidebar">
      <div class="sidebar-head">
        <div class="logo-area">
          <span class="logo-icon">⚡</span>
          <span class="logo-text">API Playground</span>
        </div>
        <div class="search-area">
          <input v-model="searchKeyword" type="text" placeholder="搜索接口..." class="search-input" />
        </div>
      </div>
      <div class="collection-list">
        <div v-for="group in filteredCollections" :key="group.name" class="collection-group">
          <div class="group-header" @click="toggleGroup(group.name)">
            <span class="group-arrow">{{ expandedGroups.includes(group.name) ? '▼' : '▶' }}</span>
            <span class="group-icon">{{ group.icon }}</span>
            <span class="group-name">{{ group.name }}</span>
            <span class="group-count mono">{{ group.items.length }}</span>
          </div>
          <transition name="slide">
            <div v-if="expandedGroups.includes(group.name)" class="group-items">
              <div
                v-for="api in group.items"
                :key="api.path"
                :class="['api-item', { active: currentApi?.path === api.path }]"
                @click="selectApi(api)"
              >
                <span :class="['method-chip', api.method.toLowerCase()]">{{ api.method }}</span>
                <span class="api-name">{{ api.name }}</span>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </aside>

    <main class="main-area">
      <div class="request-panel">
        <div class="request-bar">
          <select v-model="requestData.method" class="method-select">
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="DELETE">DELETE</option>
            <option value="PATCH">PATCH</option>
          </select>
          <input v-model="requestData.url" type="text" class="url-input" placeholder="输入请求URL..." />
          <button class="send-btn" @click="sendRequest" :disabled="loading">
            <span v-if="loading" class="send-spinner"></span>
            <span v-else>{{ loading ? '发送中...' : 'Send' }}</span>
          </button>
        </div>

        <div class="request-tabs">
          <div
            v-for="tab in requestTabs"
            :key="tab.key"
            :class="['tab-item', { active: activeRequestTab === tab.key }]"
            @click="activeRequestTab = tab.key"
          >
            {{ tab.label }}
            <span v-if="tab.key === 'params' && requestParams.length" class="tab-badge">{{ requestParams.length }}</span>
            <span v-if="tab.key === 'headers' && requestHeaders.length" class="tab-badge">{{ requestHeaders.length }}</span>
          </div>
        </div>

        <div class="request-body">
          <div v-if="activeRequestTab === 'params'" class="params-editor">
            <div class="params-table">
              <div class="params-header">
                <span class="col-check"></span>
                <span class="col-key">Key</span>
                <span class="col-value">Value</span>
                <span class="col-desc">Description</span>
                <span class="col-action"></span>
              </div>
              <div v-for="(p, idx) in requestParams" :key="idx" class="params-row">
                <span class="col-check">
                  <input type="checkbox" v-model="p.enabled" />
                </span>
                <span class="col-key">
                  <input v-model="p.key" type="text" placeholder="参数名" />
                </span>
                <span class="col-value">
                  <input v-model="p.value" type="text" placeholder="参数值" />
                </span>
                <span class="col-desc">
                  <input v-model="p.desc" type="text" placeholder="描述" />
                </span>
                <span class="col-action">
                  <button class="row-del" @click="removeParam(idx)">×</button>
                </span>
              </div>
              <div class="params-row add-row" @click="addParam">
                <span class="col-check"></span>
                <span class="col-key add-text">+ 添加参数</span>
                <span class="col-value"></span>
                <span class="col-desc"></span>
                <span class="col-action"></span>
              </div>
            </div>
          </div>

          <div v-if="activeRequestTab === 'headers'" class="params-editor">
            <div class="params-table">
              <div class="params-header">
                <span class="col-check"></span>
                <span class="col-key">Key</span>
                <span class="col-value">Value</span>
                <span class="col-desc">Description</span>
                <span class="col-action"></span>
              </div>
              <div v-for="(h, idx) in requestHeaders" :key="idx" class="params-row">
                <span class="col-check">
                  <input type="checkbox" v-model="h.enabled" />
                </span>
                <span class="col-key">
                  <input v-model="h.key" type="text" placeholder="Header名" />
                </span>
                <span class="col-value">
                  <input v-model="h.value" type="text" placeholder="Header值" />
                </span>
                <span class="col-desc">
                  <input v-model="h.desc" type="text" placeholder="描述" />
                </span>
                <span class="col-action">
                  <button class="row-del" @click="removeHeader(idx)">×</button>
                </span>
              </div>
              <div class="params-row add-row" @click="addHeader">
                <span class="col-check"></span>
                <span class="col-key add-text">+ 添加Header</span>
                <span class="col-value"></span>
                <span class="col-desc"></span>
                <span class="col-action"></span>
              </div>
            </div>
          </div>

          <div v-if="activeRequestTab === 'body'" class="body-editor">
            <div class="body-types">
              <label class="type-option" v-for="t in bodyTypes" :key="t">
                <input type="radio" v-model="bodyType" :value="t" />
                <span>{{ t }}</span>
              </label>
            </div>
            <textarea
              v-if="bodyType === 'raw' || bodyType === 'json'"
              v-model="requestData.body"
              class="body-textarea"
              :placeholder="bodyPlaceholder"
              spellcheck="false"
            ></textarea>
            <div v-else-if="bodyType === 'form-data'" class="params-editor">
              <div class="params-table">
                <div class="params-header">
                  <span class="col-check"></span>
                  <span class="col-key">Key</span>
                  <span class="col-value">Value</span>
                  <span class="col-desc">Type</span>
                  <span class="col-action"></span>
                </div>
                <div v-for="(f, idx) in formData" :key="idx" class="params-row">
                  <span class="col-check">
                    <input type="checkbox" v-model="f.enabled" />
                  </span>
                  <span class="col-key">
                    <input v-model="f.key" type="text" placeholder="字段名" />
                  </span>
                  <span class="col-value">
                    <input v-model="f.value" type="text" placeholder="字段值" />
                  </span>
                  <span class="col-desc">
                    <select v-model="f.type" class="type-select">
                      <option value="text">Text</option>
                      <option value="file">File</option>
                    </select>
                  </span>
                  <span class="col-action">
                    <button class="row-del" @click="removeFormField(idx)">×</button>
                  </span>
                </div>
                <div class="params-row add-row" @click="addFormField">
                  <span class="col-check"></span>
                  <span class="col-key add-text">+ 添加字段</span>
                  <span class="col-value"></span>
                  <span class="col-desc"></span>
                  <span class="col-action"></span>
                </div>
              </div>
            </div>
            <div v-else class="body-placeholder">
              选择 body 类型
            </div>
          </div>

          <div v-if="activeRequestTab === 'docs'" class="docs-panel">
            <div v-if="currentApi" class="api-docs">
              <div class="docs-head">
                <span :class="['method-badge', currentApi.method.toLowerCase()]">{{ currentApi.method }}</span>
                <span class="docs-path mono">{{ currentApi.path }}</span>
              </div>
              <h3 class="docs-title">{{ currentApi.name }}</h3>
              <p class="docs-desc">{{ currentApi.description }}</p>

              <div v-if="currentApi.params?.length" class="docs-section">
                <h4 class="docs-section-title">请求参数</h4>
                <table class="docs-table">
                  <thead>
                    <tr>
                      <th>参数名</th>
                      <th>类型</th>
                      <th>必填</th>
                      <th>说明</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="p in currentApi.params" :key="p.name">
                      <td class="mono param-name">{{ p.name }}</td>
                      <td class="mono param-type">{{ p.type }}</td>
                      <td>
                        <span v-if="p.required" class="req-yes">是</span>
                        <span v-else class="req-no">否</span>
                      </td>
                      <td>{{ p.desc }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div v-if="currentApi.responseExample" class="docs-section">
                <h4 class="docs-section-title">响应示例</h4>
                <pre class="code-block"><code>{{ formatJson(currentApi.responseExample) }}</code></pre>
              </div>
            </div>
            <div v-else class="docs-empty">
              <span class="empty-icon">📖</span>
              <p>选择左侧接口查看文档</p>
            </div>
          </div>
        </div>
      </div>

      <div class="response-panel">
        <div class="response-head">
          <div class="response-tabs">
            <div
              v-for="tab in responseTabs"
              :key="tab.key"
              :class="['tab-item', { active: activeResponseTab === tab.key }]"
              @click="activeResponseTab = tab.key"
            >
              {{ tab.label }}
            </div>
          </div>
          <div class="response-meta" v-if="responseData">
            <span :class="['status-badge', statusClass]">{{ responseStatus }}</span>
            <span class="meta-item">
              <span class="meta-label">Time</span>
              <span class="meta-value mono">{{ responseTime }}ms</span>
            </span>
            <span class="meta-item">
              <span class="meta-label">Size</span>
              <span class="meta-value mono">{{ responseSize }}</span>
            </span>
          </div>
        </div>

        <div class="response-body">
          <div v-if="!responseData && !loading" class="response-empty">
            <span class="empty-icon">🚀</span>
            <p>点击 Send 发送请求</p>
            <p class="empty-hint">或选择左侧接口快速开始</p>
          </div>

          <div v-else-if="loading" class="response-loading">
            <div class="loading-spinner"></div>
            <p>正在发送请求...</p>
          </div>

          <div v-else-if="activeResponseTab === 'body' && responseData" class="response-content">
            <div class="response-view-tabs">
              <button
                v-for="v in responseViews"
                :key="v"
                :class="['view-btn', { active: responseView === v }]"
                @click="responseView = v"
              >{{ v }}</button>
            </div>
            <pre class="response-code" v-if="responseView === 'Pretty'"><code>{{ formatJson(responseData) }}</code></pre>
            <pre class="response-code raw" v-else>{{ JSON.stringify(responseData) }}</pre>
          </div>

          <div v-else-if="activeResponseTab === 'headers' && responseHeaders" class="response-headers">
            <table class="headers-table">
              <tbody>
                <tr v-for="(value, key) in responseHeaders" :key="key">
                  <td class="header-key mono">{{ key }}</td>
                  <td class="header-value mono">{{ value }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="activeResponseTab === 'test'" class="response-test">
            <div class="test-result">
              <div class="test-item pass">
                <span class="test-icon">✓</span>
                <span class="test-text">Status code is 200</span>
              </div>
              <div class="test-item pass">
                <span class="test-icon">✓</span>
                <span class="test-text">Response time is less than 200ms</span>
              </div>
              <div class="test-item pass">
                <span class="test-icon">✓</span>
                <span class="test-text">Content-Type is application/json</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import request from '@/utils/request'

const searchKeyword = ref('')
const expandedGroups = ref(['土壤标准', 'AI智能分析', '土壤采样'])
const activeRequestTab = ref('params')
const activeResponseTab = ref('body')
const responseView = ref('Pretty')
const bodyType = ref('json')
const loading = ref(false)

const currentApi = ref(null)
const responseData = ref(null)
const responseHeaders = ref({})
const responseStatus = ref(200)
const responseTime = ref(0)

const requestTabs = [
  { key: 'params', label: 'Params' },
  { key: 'headers', label: 'Headers' },
  { key: 'body', label: 'Body' },
  { key: 'docs', label: 'Docs' }
]

const responseTabs = [
  { key: 'body', label: 'Body' },
  { key: 'headers', label: 'Headers' },
  { key: 'test', label: 'Test Results' }
]

const responseViews = ['Pretty', 'Raw']
const bodyTypes = ['none', 'form-data', 'raw', 'json']

const bodyPlaceholder = computed(() => {
  return bodyType.value === 'json'
    ? '{\n  "key": "value"\n}'
    : '请求体内容...'
})

const apiCollections = ref([
  {
    name: '土壤标准',
    icon: '📋',
    items: [
      {
        method: 'GET',
        path: '/api/v1/geo-standards',
        name: '获取标准列表',
        description: '分页获取地质/土壤标准数据列表，支持按分类、关键词筛选。',
        params: [
          { name: 'page', type: 'int', required: false, desc: '页码，默认1' },
          { name: 'pageSize', type: 'int', required: false, desc: '每页数量，默认20' },
          { name: 'category', type: 'string', required: false, desc: '分类编码' },
          { name: 'keyword', type: 'string', required: false, desc: '搜索关键词' }
        ],
        responseExample: { code: 200, message: 'success', data: { total: 50, list: [{ standardId: 1, standardCode: 'CS-001', standardName: '红壤', category: 'SOIL_CHINA' }] } }
      },
      {
        method: 'GET',
        path: '/api/v1/geo-standards/1',
        name: '获取标准详情',
        description: '根据ID获取标准详细信息，包含物理性质、化学成分等。',
        params: [
          { name: 'id', type: 'int', required: true, desc: '标准ID' }
        ],
        responseExample: { code: 200, message: 'success', data: { standardId: 1, standardName: '红壤', ph: '4.5-6.0' } }
      },
      {
        method: 'GET',
        path: '/api/v1/geo-standards/category/SOIL_CHINA',
        name: '按分类查询',
        description: '获取指定分类下的所有标准条目。',
        params: [
          { name: 'category', type: 'string', required: true, desc: '分类编码' }
        ],
        responseExample: { code: 200, message: 'success', data: [] }
      }
    ]
  },
  {
    name: 'AI智能分析',
    icon: '🤖',
    items: [
      {
        method: 'POST',
        path: '/api/v1/geo-standards/ai/classify',
        name: '土壤分类比对',
        description: '根据土壤参数与标准数据库进行AI智能比对，返回最匹配的土壤类型及置信度。',
        params: [
          { name: 'ph', type: 'double', required: true, desc: 'pH值' },
          { name: 'organicMatter', type: 'double', required: false, desc: '有机质含量(%)' },
          { name: 'texture', type: 'string', required: false, desc: '质地类型' },
          { name: 'color', type: 'string', required: false, desc: '颜色' }
        ],
        responseExample: {
          code: 200,
          message: 'success',
          data: {
            matches: [
              { standardId: 1, standardName: '褐土', confidence: 92, category: '半淋溶土纲' }
            ]
          }
        }
      },
      {
        method: 'POST',
        path: '/api/v1/geo-standards/ai/compare',
        name: '批量比对分析',
        description: '批量土壤样本与标准数据库比对分析。',
        params: [
          { name: 'samples', type: 'array', required: true, desc: '土壤样本数组' }
        ],
        responseExample: { code: 200, message: 'success', data: { results: [] } }
      },
      {
        method: 'POST',
        path: '/api/v1/geo-standards/ai/diagram/1',
        name: '生成标准图示',
        description: 'AI生成指定标准的剖面图示和结构描述。',
        params: [
          { name: 'id', type: 'int', required: true, desc: '标准ID' }
        ],
        responseExample: { code: 200, message: 'success', data: { diagramUrl: '', description: '' } }
      }
    ]
  },
  {
    name: '土壤采样',
    icon: '🌱',
    items: [
      {
        method: 'GET',
        path: '/api/v1/soil-samples',
        name: '采样数据列表',
        description: '分页获取土壤采样数据，支持区域、时间筛选。',
        params: [
          { name: 'page', type: 'int', required: false, desc: '页码' },
          { name: 'area', type: 'string', required: false, desc: '区域名称' },
          { name: 'startDate', type: 'string', required: false, desc: '开始日期' }
        ],
        responseExample: { code: 200, message: 'success', data: { total: 100, list: [] } }
      },
      {
        method: 'POST',
        path: '/api/v1/soil-samples',
        name: '新增采样数据',
        description: '新增一条土壤采样记录。',
        params: [
          { name: 'area', type: 'string', required: true, desc: '采样区域' },
          { name: 'ph', type: 'double', required: true, desc: 'pH值' },
          { name: 'moisture', type: 'double', required: false, desc: '含水率' }
        ],
        responseExample: { code: 200, message: 'success', data: { id: 1 } }
      }
    ]
  },
  {
    name: '数据导入导出',
    icon: '📤',
    items: [
      {
        method: 'POST',
        path: '/api/v1/data-import/upload',
        name: '上传导入文件',
        description: '上传数据文件进行导入分析。',
        params: [
          { name: 'file', type: 'file', required: true, desc: 'Excel/CSV文件' },
          { name: 'boType', type: 'string', required: true, desc: '业务类型' }
        ],
        responseExample: { code: 200, message: 'success', data: { taskId: 'IMP001' } }
      },
      {
        method: 'POST',
        path: '/api/v1/data-export/soil-report',
        name: '导出土壤报告',
        description: '导出土壤检测报告，包含统计图表。',
        params: [
          { name: 'startDate', type: 'string', required: true, desc: '开始日期' },
          { name: 'endDate', type: 'string', required: true, desc: '结束日期' },
          { name: 'format', type: 'string', required: false, desc: '格式 EXCEL/PDF' }
        ],
        responseExample: { code: 200, message: 'success', data: { downloadUrl: '' } }
      }
    ]
  },
  {
    name: '对外OpenAPI',
    icon: '🔌',
    items: [
      {
        method: 'GET',
        path: '/openapi/v1/soil-standards',
        name: '土壤标准列表(对外)',
        description: '第三方系统调用的土壤标准查询接口，需API Key签名。',
        params: [
          { name: 'page', type: 'int', required: false, desc: '页码' },
          { name: 'category', type: 'string', required: false, desc: '分类' },
          { name: 'apiKey', type: 'string', required: true, desc: 'API密钥' },
          { name: 'sign', type: 'string', required: true, desc: '签名' }
        ],
        responseExample: { code: 200, message: 'success', data: { list: [] }, timestamp: 1700000000000 }
      },
      {
        method: 'POST',
        path: '/openapi/v1/soil-analysis/classify',
        name: 'AI土壤分类(对外)',
        description: '对外开放的AI土壤分类接口，支持字段自动适配。',
        params: [
          { name: 'phValue', type: 'double', required: true, desc: 'pH值(支持pH/phValue等多种命名)' },
          { name: 'organicContent', type: 'double', required: false, desc: '有机质' }
        ],
        responseExample: { code: 200, message: 'success', data: {}, timestamp: 1700000000000 }
      }
    ]
  },
  {
    name: '外部数据接入',
    icon: '📥',
    items: [
      {
        method: 'GET',
        path: '/api/v1/external-data/sources',
        name: '数据源列表',
        description: '获取可用的外部数据源列表。',
        params: [],
        responseExample: { code: 200, message: 'success', data: [{ code: 'soil_lab_api', name: '土壤检测平台' }] }
      },
      {
        method: 'POST',
        path: '/api/v1/external-data/fetch',
        name: '拉取外部数据',
        description: '从指定数据源拉取数据，自动进行字段映射和格式转换。',
        params: [
          { name: 'sourceCode', type: 'string', required: true, desc: '数据源编码' },
          { name: 'params', type: 'object', required: false, desc: '查询参数' }
        ],
        responseExample: { code: 200, message: 'success', data: { total: 10, records: [] } }
      },
      {
        method: 'GET',
        path: '/api/v1/external-data/field-mapping',
        name: '字段映射关系',
        description: '获取AI字段映射配置表。',
        params: [],
        responseExample: { code: 200, message: 'success', data: { mappings: [] } }
      }
    ]
  }
])

const filteredCollections = computed(() => {
  if (!searchKeyword.value) return apiCollections.value
  const kw = searchKeyword.value.toLowerCase()
  return apiCollections.value.map(g => ({
    ...g,
    items: g.items.filter(a =>
      a.name.toLowerCase().includes(kw) ||
      a.path.toLowerCase().includes(kw)
    )
  })).filter(g => g.items.length > 0)
})

const requestData = reactive({
  method: 'GET',
  url: '',
  body: ''
})

const requestParams = ref([
  { key: '', value: '', desc: '', enabled: true }
])

const requestHeaders = ref([
  { key: 'Content-Type', value: 'application/json', desc: '内容类型', enabled: true }
])

const formData = ref([
  { key: '', value: '', type: 'text', enabled: true }
])

const statusClass = computed(() => {
  const code = responseStatus.value
  if (code >= 200 && code < 300) return 'status-200'
  if (code >= 300 && code < 400) return 'status-300'
  if (code >= 400 && code < 500) return 'status-400'
  return 'status-500'
})

const responseSize = computed(() => {
  if (!responseData.value) return '0 B'
  const bytes = new Blob([JSON.stringify(responseData.value)]).size
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
})

const toggleGroup = (name) => {
  const idx = expandedGroups.value.indexOf(name)
  if (idx > -1) {
    expandedGroups.value.splice(idx, 1)
  } else {
    expandedGroups.value.push(name)
  }
}

const selectApi = (api) => {
  currentApi.value = api
  requestData.method = api.method
  requestData.url = api.path

  requestParams.value = api.params
    ? api.params.map(p => ({ key: p.name, value: '', desc: p.desc, enabled: true }))
    : [{ key: '', value: '', desc: '', enabled: true }]

  if (api.method === 'POST' && api.params && api.params.length > 0) {
    const body = {}
    api.params.forEach(p => {
      if (p.type !== 'file') body[p.name] = ''
    })
    requestData.body = JSON.stringify(body, null, 2)
    bodyType.value = 'json'
  }

  responseData.value = null
  activeRequestTab.value = api.method === 'GET' ? 'params' : 'body'
}

const addParam = () => {
  requestParams.value.push({ key: '', value: '', desc: '', enabled: true })
}

const removeParam = (idx) => {
  requestParams.value.splice(idx, 1)
}

const addHeader = () => {
  requestHeaders.value.push({ key: '', value: '', desc: '', enabled: true })
}

const removeHeader = (idx) => {
  requestHeaders.value.splice(idx, 1)
}

const addFormField = () => {
  formData.value.push({ key: '', value: '', type: 'text', enabled: true })
}

const removeFormField = (idx) => {
  formData.value.splice(idx, 1)
}

const formatJson = (data) => {
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data)
    } catch (e) {
      return data
    }
  }
  return JSON.stringify(data, null, 2)
}

const sendRequest = async () => {
  if (!requestData.url) return

  loading.value = true
  responseData.value = null
  const startTime = Date.now()

  try {
    const params = {}
    requestParams.value.forEach(p => {
      if (p.enabled && p.key) {
        params[p.key] = p.value
      }
    })

    const headers = {}
    requestHeaders.value.forEach(h => {
      if (h.enabled && h.key) {
        headers[h.key] = h.value
      }
    })

    let bodyData = undefined
    if (requestData.method !== 'GET' && bodyType.value === 'json' && requestData.body) {
      try {
        bodyData = JSON.parse(requestData.body)
      } catch (e) {
        bodyData = requestData.body
      }
    }

    const res = await request({
      url: requestData.url,
      method: requestData.method,
      params,
      data: bodyData,
      headers
    }).catch(err => {
      console.warn('API请求失败，使用Mock数据:', err.message)
      return generateMockResponse(currentApi.value, requestData.url)
    })

    responseData.value = res.data || res
    responseStatus.value = res.code || 200
    responseHeaders.value = {
      'Content-Type': 'application/json; charset=utf-8',
      'Server': 'ZRWS-API-Gateway',
      'X-Request-ID': 'req_' + Math.random().toString(36).substr(2, 16),
      'Date': new Date().toUTCString()
    }
  } catch (e) {
    responseData.value = { code: 500, message: e.message, error: 'Internal Server Error' }
    responseStatus.value = 500
  } finally {
    responseTime.value = Date.now() - startTime
    loading.value = false
  }
}

const generateMockResponse = (api, url) => {
  if (api?.responseExample) {
    return { data: api.responseExample, code: api.responseExample.code || 200 }
  }

  if (url.includes('geo-standards') && !url.includes('ai')) {
    return {
      data: {
        code: 200,
        message: 'success',
        data: {
          total: 13,
          page: 1,
          pageSize: 10,
          list: [
            { standardId: 1, standardCode: 'CS-001', standardName: '红壤', category: 'SOIL_CHINA', subcategory: '铁铝土纲' },
            { standardId: 2, standardCode: 'CS-002', standardName: '黄壤', category: 'SOIL_CHINA', subcategory: '铁铝土纲' },
            { standardId: 3, standardCode: 'CS-003', standardName: '褐土', category: 'SOIL_CHINA', subcategory: '半淋溶土纲' }
          ]
        }
      },
      code: 200
    }
  }

  if (url.includes('ai/classify')) {
    return {
      data: {
        code: 200,
        message: 'success',
        data: {
          matches: [
            { standardId: 3, standardName: '褐土', confidence: 92, category: '半淋溶土纲', ph: '6.5-7.5' },
            { standardId: 5, standardName: '潮土', confidence: 76, category: '半水成土纲', ph: '7.0-8.5' },
            { standardId: 6, standardName: '棕壤', confidence: 65, category: '淋溶土纲', ph: '5.5-6.5' }
          ],
          analysisTime: 0.23,
          modelVersion: 'v1.2.0'
        }
      },
      code: 200
    }
  }

  if (url.includes('external-data/sources')) {
    return {
      data: {
        code: 200,
        message: 'success',
        data: [
          { code: 'soil_lab_api', name: '土壤检测API平台', status: 'online', description: '第三方土壤检测数据接口' },
          { code: 'agri_data_platform', name: '农业大数据平台', status: 'online', description: '省级农业数据共享平台' },
          { code: 'geo_monitor_system', name: '地质监测系统', status: 'testing', description: '地质环境监测数据接入' }
        ]
      },
      code: 200
    }
  }

  return {
    data: { code: 200, message: 'success', data: null },
    code: 200
  }
}
</script>

<style scoped>
.playground {
  display: flex;
  height: 100vh;
  background: #1e1e1e;
  color: #ccc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 280px;
  background: #252526;
  border-right: 1px solid #333;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-head {
  padding: 16px;
  border-bottom: 1px solid #333;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.logo-icon {
  font-size: 22px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.search-area {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 6px;
  color: #ccc;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #FF6C37;
}

.collection-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.collection-group {
  margin-bottom: 2px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.15s;
  user-select: none;
}

.group-header:hover {
  background: #2a2d2e;
}

.group-arrow {
  font-size: 9px;
  color: #888;
  width: 14px;
}

.group-icon {
  font-size: 14px;
}

.group-name {
  flex: 1;
  font-size: 13px;
  color: #ddd;
  font-weight: 500;
}

.group-count {
  font-size: 11px;
  color: #777;
  background: #333;
  padding: 2px 8px;
  border-radius: 10px;
}

.group-items {
  padding-left: 12px;
}

.api-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px 8px 12px;
  cursor: pointer;
  transition: background 0.15s;
  border-left: 2px solid transparent;
}

.api-item:hover {
  background: #2a2d2e;
}

.api-item.active {
  background: #37373d;
  border-left-color: #FF6C37;
}

.method-chip {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 3px;
  min-width: 40px;
  text-align: center;
  font-family: 'SF Mono', Menlo, monospace;
}

.method-chip.get { background: rgba(73, 197, 133, 0.2); color: #49c585; }
.method-chip.post { background: rgba(255, 171, 0, 0.2); color: #ffab00; }
.method-chip.put { background: rgba(61, 143, 225, 0.2); color: #3d8fe1; }
.method-chip.delete { background: rgba(255, 75, 75, 0.2); color: #ff4b4b; }
.method-chip.patch { background: rgba(156, 88, 219, 0.2); color: #9c58db; }

.api-name {
  font-size: 13px;
  color: #ccc;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Main Area */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Request Panel */
.request-panel {
  border-bottom: 1px solid #333;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.request-bar {
  display: flex;
  gap: 8px;
  padding: 14px 16px;
  background: #2d2d30;
  border-bottom: 1px solid #333;
}

.method-select {
  padding: 8px 12px;
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  color: #49c585;
  font-size: 13px;
  font-weight: 700;
  font-family: 'SF Mono', Menlo, monospace;
  cursor: pointer;
  outline: none;
  min-width: 90px;
}

.url-input {
  flex: 1;
  padding: 8px 14px;
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  color: #ddd;
  font-size: 13px;
  font-family: 'SF Mono', Menlo, monospace;
  outline: none;
  transition: border-color 0.2s;
}

.url-input:focus {
  border-color: #FF6C37;
}

.send-btn {
  padding: 8px 28px;
  background: #FF6C37;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  background: #ff5a1f;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.request-tabs {
  display: flex;
  gap: 0;
  padding: 0 16px;
  background: #252526;
  border-bottom: 1px solid #333;
}

.tab-item {
  padding: 10px 16px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-item:hover {
  color: #ddd;
}

.tab-item.active {
  color: #fff;
  border-bottom-color: #FF6C37;
}

.tab-badge {
  background: #3c3c3c;
  color: #aaa;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.request-body {
  background: #1e1e1e;
  max-height: 280px;
  overflow-y: auto;
}

/* Params Editor */
.params-editor {
  padding: 8px 0;
}

.params-table {
  display: flex;
  flex-direction: column;
}

.params-header,
.params-row {
  display: grid;
  grid-template-columns: 40px 1fr 1fr 150px 40px;
  gap: 8px;
  padding: 6px 16px;
  align-items: center;
}

.params-header {
  font-size: 11px;
  font-weight: 600;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: #252526;
}

.params-row {
  border-bottom: 1px solid #2a2d2e;
  min-height: 36px;
}

.params-row:hover {
  background: #252526;
}

.add-row {
  cursor: pointer;
  color: #666;
}

.add-row:hover {
  background: #2a2d2e;
  color: #aaa;
}

.add-text {
  font-size: 13px;
  color: #666;
}

.params-row input,
.params-row select {
  width: 100%;
  background: transparent;
  border: none;
  color: #ddd;
  font-size: 13px;
  outline: none;
  padding: 4px 0;
}

.params-row input:focus {
  background: #2a2d2e;
  padding: 4px 8px;
  margin: -4px -8px;
  border-radius: 4px;
}

.row-del {
  background: none;
  border: none;
  color: #666;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.row-del:hover {
  background: rgba(255, 75, 75, 0.2);
  color: #ff4b4b;
}

/* Body Editor */
.body-editor {
  padding: 12px 16px;
}

.body-types {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #ccc;
}

.type-option input {
  accent-color: #FF6C37;
}

.body-textarea {
  width: 100%;
  min-height: 180px;
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  color: #d4d4d4;
  font-family: 'SF Mono', Menlo, 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  padding: 12px;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.body-textarea:focus {
  border-color: #FF6C37;
}

.body-placeholder {
  padding: 40px;
  text-align: center;
  color: #666;
  font-size: 13px;
}

.type-select {
  padding: 4px 8px;
  background: #2a2d2e;
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  color: #ccc;
  font-size: 12px;
  cursor: pointer;
}

/* Docs Panel */
.docs-panel {
  padding: 20px;
  max-height: 280px;
  overflow-y: auto;
}

.docs-empty {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-icon {
  font-size: 36px;
  margin-bottom: 12px;
  display: block;
}

.api-docs {
  color: #ccc;
}

.docs-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.method-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  font-family: 'SF Mono', Menlo, monospace;
}

.method-badge.get { background: rgba(73, 197, 133, 0.2); color: #49c585; }
.method-badge.post { background: rgba(255, 171, 0, 0.2); color: #ffab00; }
.method-badge.put { background: rgba(61, 143, 225, 0.2); color: #3d8fe1; }
.method-badge.delete { background: rgba(255, 75, 75, 0.2); color: #ff4b4b; }

.docs-path {
  color: #d4d4d4;
  font-size: 13px;
}

.docs-title {
  font-size: 18px;
  color: #fff;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.docs-desc {
  font-size: 13px;
  color: #aaa;
  line-height: 1.6;
  margin-bottom: 20px;
}

.docs-section {
  margin-bottom: 20px;
}

.docs-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #ddd;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.docs-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.docs-table th,
.docs-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #333;
}

.docs-table th {
  background: #252526;
  color: #888;
  font-weight: 600;
}

.docs-table td {
  color: #ccc;
}

.param-name {
  color: #9cdcfe;
  font-weight: 500;
}

.param-type {
  color: #4ec9b0;
}

.req-yes {
  color: #ff4b4b;
  font-weight: 600;
}

.req-no {
  color: #666;
}

.code-block {
  background: #1e1e1e;
  border: 1px solid #3c3c3c;
  border-radius: 6px;
  padding: 14px;
  margin: 0;
  overflow-x: auto;
  font-family: 'SF Mono', Menlo, monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #d4d4d4;
}

/* Response Panel */
.response-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #1e1e1e;
}

.response-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background: #252526;
  border-bottom: 1px solid #333;
}

.response-tabs {
  display: flex;
  gap: 0;
}

.response-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-badge {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  font-family: 'SF Mono', Menlo, monospace;
}

.status-200 { background: rgba(73, 197, 133, 0.2); color: #49c585; }
.status-300 { background: rgba(61, 143, 225, 0.2); color: #3d8fe1; }
.status-400 { background: rgba(255, 171, 0, 0.2); color: #ffab00; }
.status-500 { background: rgba(255, 75, 75, 0.2); color: #ff4b4b; }

.meta-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.meta-label {
  font-size: 9px;
  color: #777;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-value {
  font-size: 12px;
  color: #aaa;
}

.response-body {
  flex: 1;
  overflow: auto;
  position: relative;
}

.response-empty,
.response-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
  gap: 10px;
}

.response-empty p {
  margin: 0;
  font-size: 14px;
}

.empty-hint {
  font-size: 12px !important;
  color: #555;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #333;
  border-top-color: #FF6C37;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 10px;
}

.response-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.response-view-tabs {
  display: flex;
  gap: 0;
  padding: 8px 16px;
  border-bottom: 1px solid #2a2d2e;
  background: #252526;
}

.view-btn {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid #3c3c3c;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn:first-child {
  border-radius: 4px 0 0 4px;
}

.view-btn:last-child {
  border-radius: 0 4px 4px 0;
  border-left: none;
}

.view-btn.active {
  background: #FF6C37;
  border-color: #FF6C37;
  color: white;
}

.response-code {
  flex: 1;
  margin: 0;
  padding: 16px;
  font-family: 'SF Mono', Menlo, 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #d4d4d4;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.response-code.raw {
  color: #b5b5b5;
}

.response-headers {
  padding: 16px;
}

.headers-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.headers-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #2a2d2e;
}

.header-key {
  color: #9cdcfe;
  width: 200px;
}

.header-value {
  color: #ce9178;
}

.response-test {
  padding: 20px;
}

.test-result {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.test-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #252526;
  border-radius: 6px;
  font-size: 13px;
}

.test-item.pass .test-icon {
  color: #49c585;
}

.test-icon {
  font-weight: 700;
}

.test-text {
  color: #ccc;
}

/* Slide transition */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
}

.slide-enter-to,
.slide-leave-from {
  max-height: 500px;
  opacity: 1;
}

.mono {
  font-family: 'SF Mono', Menlo, 'Consolas', monospace;
}
</style>
