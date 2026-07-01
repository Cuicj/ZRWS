<template>
  <div class="page-container workflow-designer">
    <div class="page-head">
      <div>
        <h1 class="page-title display">流程设计器</h1>
        <div class="page-meta mono">WORKFLOW DESIGN · 拖拽绘制 → 保存审核 → 发布部署</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-ghost btn-sm" @click="createNew">📄 新建</button>
        <el-select v-model="selectedDraft" placeholder="加载草稿" size="small" style="width:160px" @change="loadDraft">
          <el-option v-for="d in drafts" :key="d.processKey" :label="d.processName + ' (' + d.processKey + ')'" :value="d.processKey" />
        </el-select>
        <button class="btn btn-ghost btn-sm" @click="saveDraft">💾 保存草稿</button>
        <button class="btn btn-ghost btn-sm" @click="submitReview">📝 提交审核</button>
        <button class="btn btn-primary btn-sm" @click="deployFlow">🚀 部署到引擎</button>
        <el-dropdown @command="handleCommand">
          <button class="btn btn-ghost btn-sm">
            ⚙ 更多
            <el-icon style="margin-left:4px"><arrow-down /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="downloadXml">下载 XML</el-dropdown-item>
              <el-dropdown-item command="downloadSvg">下载 SVG</el-dropdown-item>
              <el-dropdown-item command="validate">验证流程</el-dropdown-item>
              <el-dropdown-item command="zoomIn">放大</el-dropdown-item>
              <el-dropdown-item command="zoomOut">缩小</el-dropdown-item>
              <el-dropdown-item command="zoomFit">适应屏幕</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="designer-container">
      <div class="designer-palette">
        <div class="palette-title mono">组件库</div>
        
        <div class="palette-group">
          <div class="palette-group-title">开始/结束</div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'startEvent')" @click="addElement('startEvent')">
            <span class="palette-icon start-icon">●</span>
            <span>开始事件</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'endEvent')" @click="addElement('endEvent')">
            <span class="palette-icon end-icon">◉</span>
            <span>结束事件</span>
          </div>
        </div>

        <div class="palette-group">
          <div class="palette-group-title">任务</div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'task')" @click="addElement('task')">
            <span class="palette-icon task-icon">▭</span>
            <span>任务</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'userTask')" @click="addElement('userTask')">
            <span class="palette-icon usertask-icon">👤</span>
            <span>用户任务</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'serviceTask')" @click="addElement('serviceTask')">
            <span class="palette-icon servicetask-icon">⚙</span>
            <span>服务任务</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'scriptTask')" @click="addElement('scriptTask')">
            <span class="palette-icon scripttask-icon">📜</span>
            <span>脚本任务</span>
          </div>
        </div>

        <div class="palette-group">
          <div class="palette-group-title">网关</div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'exclusiveGateway')" @click="addElement('exclusiveGateway')">
            <span class="palette-icon gateway-icon">◆</span>
            <span>排他网关</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'parallelGateway')" @click="addElement('parallelGateway')">
            <span class="palette-icon parallel-icon">✚</span>
            <span>并行网关</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'inclusiveGateway')" @click="addElement('inclusiveGateway')">
            <span class="palette-icon inclusive-icon">◈</span>
            <span>包容网关</span>
          </div>
        </div>

        <div class="palette-group">
          <div class="palette-group-title">子流程</div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'subProcess')" @click="addElement('subProcess')">
            <span class="palette-icon sub-icon">▢</span>
            <span>子流程</span>
          </div>
          <div class="palette-item" draggable="true" @dragstart="onDragStart($event, 'callActivity')" @click="addElement('callActivity')">
            <span class="palette-icon call-icon">⊞</span>
            <span>调用活动</span>
          </div>
        </div>

        <div class="palette-group">
          <div class="palette-group-title">流向</div>
          <div class="palette-item" @click="connectElements">
            <span class="palette-icon flow-icon">→</span>
            <span>连接元素</span>
          </div>
        </div>

        <div class="palette-tips mono">
          <p>💡 操作提示：</p>
          <p>• 拖拽组件到画布</p>
          <p>• 点击选中后可编辑</p>
          <p>• 双击元素修改名称</p>
          <p>• Delete 键删除</p>
          <p>• 滚轮缩放画布</p>
        </div>
      </div>

      <div 
        ref="canvasContainer" 
        class="designer-canvas"
        @dragover.prevent="onDragOver"
        @drop="onDrop"
      >
        <div ref="canvasRef" class="bpmn-canvas-inner"></div>
      </div>

      <div class="designer-properties">
        <div class="properties-title mono">属性面板</div>
        <div v-if="selectedElement" class="properties-content">
          <div class="prop-section">
            <div class="prop-section-title">基础信息</div>
            <div class="prop-item">
              <label>ID</label>
              <el-input v-model="elementProps.id" size="small" @change="updateProperty('id', $event)" />
            </div>
            <div class="prop-item">
              <label>名称</label>
              <el-input v-model="elementProps.name" size="small" @change="updateProperty('name', $event)" />
            </div>
            <div v-if="isFlowNode" class="prop-item">
              <label>描述</label>
              <el-input v-model="elementProps.documentation" type="textarea" :rows="2" size="small" @change="updateProperty('documentation', $event)" />
            </div>
          </div>

          <div v-if="elementType === 'userTask'" class="prop-section">
            <div class="prop-section-title">用户任务</div>
            <div class="prop-item">
              <label>处理人</label>
              <el-input v-model="elementProps.assignee" size="small" @change="updateProperty('assignee', $event)" placeholder="用户ID或表达式" />
            </div>
            <div class="prop-item">
              <label>候选人</label>
              <el-input v-model="elementProps.candidateUsers" size="small" @change="updateProperty('candidateUsers', $event)" placeholder="逗号分隔" />
            </div>
            <div class="prop-item">
              <label>候选组</label>
              <el-input v-model="elementProps.candidateGroups" size="small" @change="updateProperty('candidateGroups', $event)" placeholder="逗号分隔" />
            </div>
            <div class="prop-item">
              <label>优先级</label>
              <el-input-number v-model="elementProps.priority" size="small" :min="0" :max="100" @change="updateProperty('priority', $event)" />
            </div>
            <div class="prop-item">
              <label>到期日</label>
              <el-input v-model="elementProps.dueDate" size="small" @change="updateProperty('dueDate', $event)" placeholder="表达式或日期" />
            </div>
          </div>

          <div v-if="elementType === 'serviceTask'" class="prop-section">
            <div class="prop-section-title">服务任务</div>
            <div class="prop-item">
              <label>Java类</label>
              <el-input v-model="elementProps.javaClass" size="small" @change="updateProperty('javaClass', $event)" />
            </div>
            <div class="prop-item">
              <label>表达式</label>
              <el-input v-model="elementProps.expression" size="small" @change="updateProperty('expression', $event)" />
            </div>
            <div class="prop-item">
              <label>代理表达式</label>
              <el-input v-model="elementProps.delegateExpression" size="small" @change="updateProperty('delegateExpression', $event)" />
            </div>
          </div>

          <div v-if="elementType === 'scriptTask'" class="prop-section">
            <div class="prop-section-title">脚本任务</div>
            <div class="prop-item">
              <label>脚本格式</label>
              <el-select v-model="elementProps.scriptFormat" size="small" @change="updateProperty('scriptFormat', $event)">
                <el-option label="JavaScript" value="javascript" />
                <el-option label="Groovy" value="groovy" />
                <el-option label="Python" value="python" />
              </el-select>
            </div>
            <div class="prop-item">
              <label>脚本内容</label>
              <el-input v-model="elementProps.script" type="textarea" :rows="4" size="small" @change="updateProperty('script', $event)" />
            </div>
          </div>

          <div v-if="isGateway" class="prop-section">
            <div class="prop-section-title">网关</div>
            <div class="prop-item">
              <label>网关方向</label>
              <el-select v-model="elementProps.gatewayDirection" size="small" @change="updateProperty('gatewayDirection', $event)">
                <el-option label="未指定" value="Unspecified" />
                <el-option label="汇聚" value="Converging" />
                <el-option label="发散" value="Diverging" />
                <el-option label="混合" value="Mixed" />
              </el-select>
            </div>
            <div v-if="elementType === 'exclusiveGateway'" class="prop-item">
              <label>默认流转</label>
              <el-input v-model="elementProps.default" size="small" @change="updateProperty('default', $event)" />
            </div>
          </div>

          <div v-if="elementType === 'sequenceFlow'" class="prop-section">
            <div class="prop-section-title">连线</div>
            <div class="prop-item">
              <label>条件表达式</label>
              <el-input v-model="elementProps.conditionExpression" type="textarea" :rows="2" size="small" @change="updateProperty('conditionExpression', $event)" placeholder="${变量 == '值'}" />
            </div>
            <div class="prop-item">
              <label>流转来源</label>
              <span class="prop-readonly">{{ elementProps.sourceRef || '-' }}</span>
            </div>
            <div class="prop-item">
              <label>流转目标</label>
              <span class="prop-readonly">{{ elementProps.targetRef || '-' }}</span>
            </div>
          </div>

          <div v-if="isEvent" class="prop-section">
            <div class="prop-section-title">事件</div>
            <div class="prop-item">
              <label>事件类型</label>
              <span class="prop-readonly">{{ elementType }}</span>
            </div>
            <div v-if="elementType === 'startEvent'" class="prop-item">
              <label>启动器</label>
              <el-input v-model="elementProps.initiator" size="small" @change="updateProperty('initiator', $event)" placeholder="流程发起人变量名" />
            </div>
          </div>
        </div>
        <div v-else class="properties-empty">
          <p>👈 选择画布中的元素</p>
          <p>查看和编辑属性</p>
        </div>
      </div>
    </div>

    <div class="designer-status mono">
      <span class="status-item">缩放: {{ zoomLevel }}%</span>
      <span class="status-item">元素: {{ elementCount }}</span>
      <span class="status-item">流程: {{ processName || '未命名' }}</span>
      <span class="status-item">状态: {{ statusText }}</span>
    </div>

    <!-- 已部署流程列表 -->
    <Panel title="已部署流程" style="margin-top: var(--s-4)">
      <table>
        <thead><tr><th>Key</th><th>名称</th><th>版本</th><th>状态</th><th>部署时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in deployedProcesses" :key="p.key">
            <td class="mono">{{ p.key }}</td>
            <td>{{ p.name }}</td>
            <td>v{{ p.version }}</td>
            <td><span class="status-badge status-ok">已部署</span></td>
            <td class="mono">{{ p.deployTime }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="loadProcess(p.key)">加载</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Panel>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, markRaw } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import Panel from '@/components/common/Panel.vue';
import { 
  saveDraft as saveDraftApi, 
  getDraftList, 
  getDraft, 
  deployBPMN, 
  validateBPMN,
  submitForReview as submitReviewApi
} from '@/api/flowable';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';

const canvasRef = ref(null);
const canvasContainer = ref(null);
let modeler = null;
let elementRegistry = null;
let modeling = null;
let moddle = null;
let canvas = null;

const processName = ref('');
const processKey = ref('');
const selectedDraft = ref('');
const drafts = ref([]);
const zoomLevel = ref(100);
const elementCount = ref(0);
const statusText = ref('就绪');
const selectedElement = ref(null);

const deployedProcesses = ref([
  { key: 'STANDARD', name: '标准审批', version: 1, deployTime: '2026-06-17' },
  { key: 'MATERIAL', name: '物资申领', version: 1, deployTime: '2026-06-17' },
  { key: 'DRONE_FLIGHT', name: '无人机外出报备', version: 1, deployTime: '2026-06-17' },
  { key: 'EMERGENCY', name: '应急快速通道', version: 1, deployTime: '2026-06-17' }
]);

const elementProps = reactive({
  id: '',
  name: '',
  documentation: '',
  assignee: '',
  candidateUsers: '',
  candidateGroups: '',
  priority: 50,
  dueDate: '',
  javaClass: '',
  expression: '',
  delegateExpression: '',
  scriptFormat: 'javascript',
  script: '',
  conditionExpression: '',
  sourceRef: '',
  targetRef: '',
  gatewayDirection: 'Unspecified',
  default: '',
  initiator: ''
});

const elementType = computed(() => {
  if (!selectedElement.value) return '';
  const type = selectedElement.value.type || '';
  if (type.includes('StartEvent')) return 'startEvent';
  if (type.includes('EndEvent')) return 'endEvent';
  if (type.includes('UserTask')) return 'userTask';
  if (type.includes('ServiceTask')) return 'serviceTask';
  if (type.includes('ScriptTask')) return 'scriptTask';
  if (type.includes('Task')) return 'task';
  if (type.includes('ExclusiveGateway')) return 'exclusiveGateway';
  if (type.includes('ParallelGateway')) return 'parallelGateway';
  if (type.includes('InclusiveGateway')) return 'inclusiveGateway';
  if (type.includes('SequenceFlow')) return 'sequenceFlow';
  if (type.includes('SubProcess')) return 'subProcess';
  if (type.includes('CallActivity')) return 'callActivity';
  return type;
});

const isFlowNode = computed(() => {
  return selectedElement.value && !['sequenceFlow', ''].includes(elementType.value);
});

const isGateway = computed(() => {
  return ['exclusiveGateway', 'parallelGateway', 'inclusiveGateway'].includes(elementType.value);
});

const isEvent = computed(() => {
  return ['startEvent', 'endEvent'].includes(elementType.value);
});

const createNew = () => {
  ElMessageBox.confirm('确定要新建流程吗？当前未保存的内容将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    initNewDiagram();
    processName.value = '';
    processKey.value = '';
    selectedElement.value = null;
    ElMessage.success('已创建新流程');
  }).catch(() => {});
};

const initNewDiagram = async () => {
  if (!modeler) return;
  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
    id="Definitions_1"
    targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" name="新流程" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="UserTask_1" name="审批任务">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="EndEvent_1" name="结束">
      <bpmn:incoming>Flow_2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_1"/>
    <bpmn:sequenceFlow id="Flow_2" sourceRef="UserTask_1" targetRef="EndEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="150" y="100" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="UserTask_1_di" bpmnElement="UserTask_1">
        <dc:Bounds x="250" y="80" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="410" y="100" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="186" y="118"/>
        <di:waypoint x="250" y="120"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
        <di:waypoint x="350" y="120"/>
        <di:waypoint x="410" y="118"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
  try {
    await modeler.importXML(xml);
    updateElementCount();
    statusText.value = '已新建流程';
  } catch (e) {
    console.error('创建新流程失败:', e);
    ElMessage.error('创建新流程失败');
  }
};

const initModeler = async () => {
  try {
    const BpmnModeler = (await import('bpmn-js/lib/Modeler')).default;
    await nextTick();
    
    modeler = new BpmnModeler({
      container: canvasRef.value,
      keyboard: {
        bindTo: document
      }
    });

    const eventBus = modeler.get('eventBus');
    elementRegistry = modeler.get('elementRegistry');
    modeling = modeler.get('modeling');
    moddle = modeler.get('moddle');
    canvas = modeler.get('canvas');

    eventBus.on('element.click', (event) => {
      const element = event.element;
      if (element) {
        handleElementSelect(element);
      }
    });

    eventBus.on('element.changed', () => {
      updateElementCount();
    });

    eventBus.on('canvas.viewbox.changed', (event) => {
      if (event.viewbox) {
        zoomLevel.value = Math.round(event.viewbox.scale * 100);
      }
    });

    eventBus.on('commandStack.changed', () => {
      statusText.value = '已修改';
    });

    await initNewDiagram();
    await loadDrafts();
    
    statusText.value = '就绪';
  } catch (e) {
    console.error('初始化 bpmn-js 失败:', e);
    statusText.value = '初始化失败';
    ElMessage.error('流程设计器初始化失败: ' + e.message);
  }
};

const handleElementSelect = (element) => {
  selectedElement.value = element;
  const businessObject = element.businessObject;
  
  elementProps.id = element.id || '';
  elementProps.name = businessObject.name || '';
  elementProps.documentation = businessObject.documentation?.[0]?.text || '';
  
  if (businessObject.$type === 'bpmn:UserTask') {
    elementProps.assignee = businessObject.assignee || '';
    elementProps.candidateUsers = businessObject.candidateUsers || '';
    elementProps.candidateGroups = businessObject.candidateGroups || '';
    elementProps.priority = parseInt(businessObject.priority || '50');
    elementProps.dueDate = businessObject.dueDate || '';
  }
  
  if (businessObject.$type === 'bpmn:ServiceTask') {
    elementProps.javaClass = businessObject.javaClass || '';
    elementProps.expression = businessObject.expression || '';
    elementProps.delegateExpression = businessObject.delegateExpression || '';
  }
  
  if (businessObject.$type === 'bpmn:ScriptTask') {
    elementProps.scriptFormat = businessObject.scriptFormat || 'javascript';
    elementProps.script = businessObject.script || '';
  }
  
  if (businessObject.$type === 'bpmn:SequenceFlow') {
    elementProps.conditionExpression = businessObject.conditionExpression?.body || '';
    elementProps.sourceRef = businessObject.sourceRef?.id || '';
    elementProps.targetRef = businessObject.targetRef?.id || '';
  }
  
  if (element.type && element.type.includes('Gateway')) {
    elementProps.gatewayDirection = businessObject.gatewayDirection || 'Unspecified';
    elementProps.default = businessObject.default?.id || '';
  }
  
  if (businessObject.$type === 'bpmn:StartEvent') {
    elementProps.initiator = businessObject.initiator || '';
  }
};

const updateProperty = (prop, value) => {
  if (!selectedElement.value || !modeling) return;
  
  const element = selectedElement.value;
  const businessObject = element.businessObject;
  
  if (prop === 'name' || prop === 'id') {
    if (prop === 'name') {
      modeling.updateProperties(element, { name: value });
    }
    return;
  }
  
  const updateProps = {};
  
  switch (prop) {
    case 'documentation':
      if (value) {
        const doc = moddle.create('bpmn:Documentation', { text: value });
        updateProps.documentation = [doc];
      } else {
        updateProps.documentation = [];
      }
      break;
    case 'assignee':
      updateProps.assignee = value;
      break;
    case 'candidateUsers':
      updateProps.candidateUsers = value;
      break;
    case 'candidateGroups':
      updateProps.candidateGroups = value;
      break;
    case 'priority':
      updateProps.priority = String(value);
      break;
    case 'dueDate':
      updateProps.dueDate = value;
      break;
    case 'javaClass':
      updateProps.javaClass = value;
      break;
    case 'expression':
      updateProps.expression = value;
      break;
    case 'delegateExpression':
      updateProps.delegateExpression = value;
      break;
    case 'scriptFormat':
      updateProps.scriptFormat = value;
      break;
    case 'script':
      updateProps.script = value;
      break;
    case 'conditionExpression':
      if (value) {
        const expr = moddle.create('bpmn:FormalExpression', { body: value });
        updateProps.conditionExpression = expr;
      } else {
        updateProps.conditionExpression = null;
      }
      break;
    case 'gatewayDirection':
      updateProps.gatewayDirection = value;
      break;
    case 'initiator':
      updateProps.initiator = value;
      break;
  }
  
  if (Object.keys(updateProps).length > 0) {
    modeling.updateProperties(element, updateProps);
  }
};

const onDragStart = (event, type) => {
  event.dataTransfer.setData('bpmnType', type);
  event.dataTransfer.effectAllowed = 'copy';
};

const onDragOver = (event) => {
  event.dataTransfer.dropEffect = 'copy';
};

const onDrop = (event) => {
  const bpmnType = event.dataTransfer.getData('bpmnType');
  if (!bpmnType || !modeler) return;
  
  const canvasRect = canvasRef.value.getBoundingClientRect();
  const x = event.clientX - canvasRect.left;
  const y = event.clientY - canvasRect.top;
  
  addElementAtPosition(bpmnType, x, y);
};

const addElement = (type) => {
  const viewbox = canvas.viewbox();
  const x = viewbox.x + viewbox.width / 2;
  const y = viewbox.y + viewbox.height / 2;
  addElementAtPosition(type, x, y);
};

const addElementAtPosition = (type, x, y) => {
  if (!modeling || !elementRegistry) return;
  
  const shape = createBpmnElement(type);
  if (!shape) return;
  
  const parent = getRootProcess();
  if (!parent) return;
  
  modeling.createShape(
    { type: shape.type, businessObject: shape.businessObject },
    { x: x, y: y },
    parent
  );
  
  statusText.value = `已添加 ${type}`;
};

const createBpmnElement = (type) => {
  if (!moddle) return null;
  
  const typeMap = {
    startEvent: { type: 'bpmn:StartEvent', name: '开始' },
    endEvent: { type: 'bpmn:EndEvent', name: '结束' },
    task: { type: 'bpmn:Task', name: '任务' },
    userTask: { type: 'bpmn:UserTask', name: '用户任务' },
    serviceTask: { type: 'bpmn:ServiceTask', name: '服务任务' },
    scriptTask: { type: 'bpmn:ScriptTask', name: '脚本任务' },
    exclusiveGateway: { type: 'bpmn:ExclusiveGateway', name: '' },
    parallelGateway: { type: 'bpmn:ParallelGateway', name: '' },
    inclusiveGateway: { type: 'bpmn:InclusiveGateway', name: '' },
    subProcess: { type: 'bpmn:SubProcess', name: '子流程' },
    callActivity: { type: 'bpmn:CallActivity', name: '调用活动' }
  };
  
  const config = typeMap[type];
  if (!config) return null;
  
  const id = `${type.charAt(0).toUpperCase() + type.slice(1)}_${Date.now().toString(36)}`;
  const businessObject = moddle.create(config.type, {
    id: id,
    name: config.name
  });
  
  return { type: config.type, businessObject };
};

const getRootProcess = () => {
  if (!elementRegistry) return null;
  const elements = elementRegistry.filter(e => e.type === 'bpmn:Process');
  return elements[0] || null;
};

const connectElements = () => {
  ElMessage.info('请先选择起点元素，再选择终点元素进行连接');
  statusText.value = '等待选择连接元素';
};

const updateElementCount = () => {
  if (!elementRegistry) return;
  elementCount.value = elementRegistry.filter(e => e.type !== 'label').length;
};

const saveDraft = async () => {
  if (!processKey.value) {
    try {
      const { value } = await ElMessageBox.prompt('请输入流程唯一标识', '保存草稿', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
        inputErrorMessage: '只能包含字母、数字和下划线，且以字母或下划线开头'
      });
      processKey.value = value;
    } catch {
      return;
    }
  }
  
  try {
    const { xml } = await modeler.saveXML({ format: true });
    const processNameVal = processName.value || '未命名流程';
    
    await saveDraftApi({
      processKey: processKey.value,
      processName: processNameVal,
      xml: xml
    });
    
    statusText.value = '已保存';
    ElMessage.success('草稿保存成功');
    await loadDrafts();
  } catch (e) {
    console.error('保存草稿失败:', e);
    ElMessage.error('保存失败: ' + (e.message || '未知错误'));
  }
};

const loadDrafts = async () => {
  try {
    const res = await getDraftList();
    if (res && res.list) {
      drafts.value = res.list;
    } else if (res && res.data) {
      drafts.value = res.data.list || res.data || [];
    }
  } catch (e) {
    console.warn('加载草稿列表失败:', e.message);
  }
};

const loadDraft = async (processKeyVal) => {
  if (!processKeyVal) return;
  
  try {
    const res = await getDraft(processKeyVal);
    const data = res.data || res;
    
    if (data && data.xml) {
      await modeler.importXML(data.xml);
      processKey.value = processKeyVal;
      processName.value = data.processName || '';
      selectedElement.value = null;
      updateElementCount();
      statusText.value = '已加载草稿';
      ElMessage.success('草稿加载成功');
    }
  } catch (e) {
    console.error('加载草稿失败:', e);
    ElMessage.error('加载失败: ' + e.message);
  }
};

const submitReview = async () => {
  if (!processKey.value) {
    ElMessage.warning('请先保存草稿');
    return;
  }
  
  try {
    await submitReviewApi(processKey.value);
    statusText.value = '已提交审核';
    ElMessage.success('已提交审核');
  } catch (e) {
    ElMessage.error('提交失败: ' + (e.message || '未知错误'));
  }
};

const deployFlow = async () => {
  if (!processKey.value) {
    ElMessage.warning('请先设置流程标识');
    return;
  }
  
  try {
    const { xml } = await modeler.saveXML({ format: true });
    
    const res = await deployBPMN({
      processKey: processKey.value,
      processName: processName.value || '未命名流程',
      xml: xml
    });
    
    if (res.success || res.code === 0) {
      statusText.value = '已部署';
      ElMessage.success('流程部署成功');
    } else {
      ElMessage.error(res.msg || '部署失败');
    }
  } catch (e) {
    console.error('部署失败:', e);
    ElMessage.error('部署失败: ' + (e.message || '未知错误'));
  }
};

const handleCommand = (command) => {
  switch (command) {
    case 'downloadXml':
      downloadXML();
      break;
    case 'downloadSvg':
      downloadSVG();
      break;
    case 'validate':
      validateFlow();
      break;
    case 'zoomIn':
      zoomIn();
      break;
    case 'zoomOut':
      zoomOut();
      break;
    case 'zoomFit':
      zoomFit();
      break;
  }
};

const downloadXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true });
    const blob = new Blob([xml], { type: 'text/xml' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = (processKey.value || 'process') + '.bpmn20.xml';
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (e) {
    ElMessage.error('导出XML失败: ' + e.message);
  }
};

const downloadSVG = async () => {
  try {
    const { svg } = await modeler.saveSVG();
    const blob = new Blob([svg], { type: 'image/svg+xml' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = (processKey.value || 'process') + '.svg';
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (e) {
    ElMessage.error('导出SVG失败: ' + e.message);
  }
};

const validateFlow = async () => {
  try {
    const { xml } = await modeler.saveXML();
    const res = await validateBPMN({ xml });
    
    if (res.success || res.code === 0) {
      ElMessage.success('流程验证通过');
      statusText.value = '验证通过';
    } else {
      ElMessage.warning(res.msg || '验证未通过');
    }
  } catch (e) {
    ElMessage.error('验证失败: ' + e.message);
  }
};

const zoomIn = () => {
  if (!canvas) return;
  const currentZoom = canvas.zoom();
  canvas.zoom(currentZoom * 1.2, true);
};

const zoomOut = () => {
  if (!canvas) return;
  const currentZoom = canvas.zoom();
  canvas.zoom(currentZoom * 0.8, true);
};

const zoomFit = () => {
  if (!canvas) return;
  canvas.zoom('fit-viewport', 'auto');
};

const loadProcess = (key) => {
  ElMessage.info(`加载流程 ${key}`);
};

onMounted(() => {
  nextTick(() => {
    initModeler();
  });
});
</script>

<style scoped>
.workflow-designer {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.page-container {
  padding: var(--s-3) var(--s-5);
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
}

.page-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: var(--s-2);
  margin-bottom: var(--s-2);
  border-bottom: 1px solid #E8E2D9;
  flex-shrink: 0;
}

.page-title {
  font-size: 28px;
  font-weight: 200;
  color: #5D4E37;
}

.page-meta {
  font-size: 11px;
  color: #8B7355;
  margin-top: 4px;
}

.page-actions {
  display: flex;
  gap: var(--s-2);
  align-items: center;
}

.designer-container {
  display: flex;
  flex: 1;
  border: 1px solid #E8E2D9;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  box-shadow: 0 4px 20px rgba(139, 115, 85, 0.12);
}

.designer-palette {
  width: 200px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-right: 1px solid #E8E2D9;
  overflow-y: auto;
  flex-shrink: 0;
}

.palette-title {
  padding: var(--s-3) var(--s-3);
  font-size: 11px;
  color: #8B7355;
  letter-spacing: 0.1em;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E2 100%);
}

.palette-group {
  padding: var(--s-2) 0;
  border-bottom: 1px solid #E8E2D9;
}

.palette-group-title {
  padding: 4px var(--s-3);
  font-size: 11px;
  color: #C9A86C;
  margin-bottom: 4px;
  font-weight: 500;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  padding: 8px var(--s-3);
  cursor: grab;
  color: #5D4E37;
  font-size: 13px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  border-radius: 8px;
  margin: 2px 8px;
}

.palette-item:hover {
  background: rgba(201, 168, 108, 0.15);
  transform: translateX(2px);
}

.palette-item:active {
  cursor: grabbing;
}

.palette-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  border-radius: 8px;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E2 100%);
  flex-shrink: 0;
  border: 1px solid #E8E2D9;
}

.start-icon { color: #67c23a; }
.end-icon { color: #f56c6c; }
.task-icon { color: #409eff; }
.usertask-icon { color: #9c27b0; }
.servicetask-icon { color: #e6a23c; }
.scripttask-icon { color: #00bcd4; }
.gateway-icon { color: #f0c78a; }
.parallel-icon { color: #f0c78a; }
.inclusive-icon { color: #f0c78a; }
.sub-icon { color: #b88230; }
.call-icon { color: #909399; }
.flow-icon { color: #8B7355; }

.palette-tips {
  padding: var(--s-3);
  font-size: 11px;
  color: #8B7355;
  line-height: 1.8;
}

.palette-tips p {
  margin: 2px 0;
}

.designer-canvas {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%);
  overflow: hidden;
}

.bpmn-canvas-inner {
  width: 100%;
  height: 100%;
}

.designer-properties {
  width: 300px;
  background: linear-gradient(180deg, #FAFAF8 0%, #F5F2ED 100%);
  border-left: 1px solid #E8E2D9;
  overflow-y: auto;
  flex-shrink: 0;
}

.properties-title {
  padding: var(--s-3) var(--s-3);
  font-size: 11px;
  color: #8B7355;
  letter-spacing: 0.1em;
  border-bottom: 1px solid #E8E2D9;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E2 100%);
}

.properties-content {
  padding: var(--s-3);
}

.prop-section {
  margin-bottom: var(--s-4);
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.08);
}

.prop-section-title {
  font-size: 12px;
  color: #C9A86C;
  margin-bottom: var(--s-2);
  padding-bottom: 4px;
  border-bottom: 1px solid #E8E2D9;
  font-weight: 500;
}

.prop-item {
  margin-bottom: var(--s-2);
}

.prop-item label {
  display: block;
  font-size: 12px;
  color: #8B7355;
  margin-bottom: 4px;
}

.prop-readonly {
  display: block;
  padding: 6px 8px;
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E2 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  font-size: 13px;
  color: #5D4E37;
}

.properties-empty {
  padding: 40px 20px;
  text-align: center;
  color: #8B7355;
  font-size: 13px;
}

.properties-empty p {
  margin: 4px 0;
}

.designer-status {
  display: flex;
  gap: var(--s-4);
  padding: var(--s-2) var(--s-3);
  background: linear-gradient(135deg, #FAFAF8 0%, #F5F2ED 100%);
  border: 1px solid #E8E2D9;
  border-top: none;
  border-radius: 0 0 16px 16px;
  font-size: 11px;
  color: #8B7355;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.08);
}

.status-item {
  display: flex;
  align-items: center;
}

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-ok {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.1) 100%);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

:deep(.bjs-container) {
  background: linear-gradient(135deg, #FEFBF6 0%, #F7F3ED 100%) !important;
}

:deep(.djs-visual) {
  fill: #FAFAF8;
  stroke: #C9A86C;
  stroke-width: 2px;
}

:deep(.djs-connection .djs-visual) {
  stroke: #C9A86C;
  stroke-width: 2px;
}

:deep(.djs-label) {
  fill: #5D4E37 !important;
  font-family: inherit !important;
}

:deep(.djs-outline) {
  stroke: #C9A86C;
  stroke-width: 2px;
  stroke-dasharray: 4,4;
}

:deep(.djs-palette) {
  display: none;
}

:deep(.bjs-powered-by) {
  display: none;
}

:deep(.el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-input__wrapper:hover) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 2px rgba(201, 168, 108, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.2);
}

:deep(.el-input__inner) {
  color: #5D4E37;
}

:deep(.el-input__inner::placeholder) {
  color: #B8A98F;
}

:deep(.el-textarea__inner) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 8px;
  color: #5D4E37;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-textarea__inner:hover) {
  border-color: #C9A86C;
}

:deep(.el-textarea__inner:focus) {
  border-color: #C9A86C;
  box-shadow: 0 0 0 3px rgba(201, 168, 108, 0.2);
}

:deep(.el-select .el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
}

:deep(.el-input-number) {
  --el-input-number-bg-color: #FFFFFF;
  --el-input-number-border-color: #E8E2D9;
  --el-text-color-primary: #5D4E37;
}

:deep(.el-input-number .el-input__wrapper) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
}

:deep(.el-dropdown-menu) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFAF8 100%);
  border: 1px solid #E8E2D9;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(139, 115, 85, 0.15);
}

:deep(.el-dropdown-menu__item) {
  color: #5D4E37;
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(201, 168, 108, 0.15);
  color: #C9A86C;
}

:deep(table) {
  width: 100%;
  border-collapse: collapse;
}

:deep(thead th) {
  background: linear-gradient(135deg, #F5F2ED 0%, #EDE9E2 100%);
  color: #5D4E37;
  font-weight: 500;
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #E8E2D9;
  font-size: 13px;
}

:deep(tbody td) {
  padding: 12px 16px;
  border-bottom: 1px solid #E8E2D9;
  color: #5D4E37;
  font-size: 13px;
}

:deep(tbody tr:hover) {
  background: rgba(201, 168, 108, 0.08);
}

:deep(tbody tr:last-child td) {
  border-bottom: none;
}

:deep(.mono) {
  color: #8B7355;
}
</style>
