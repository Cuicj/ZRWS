package com.zrws.approval.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * 流程设计器服务
 * <p>提供BPMN模型与JSON格式的相互转换，支持前端流程设计器
 */
@Service
public class FlowableDesignerService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FlowableDeployService deployService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    // ============================================================
    // 1. BPMN模型转JSON（用于前端展示）
    // ============================================================

    /**
     * 将BPMN XML转换为流程设计器JSON格式
     */
    public Map<String, Object> bpmnXmlToJson(String bpmnXml) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(bpmnXml.getBytes()));
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);
            return bpmnModelToJson(model);
        } catch (Exception e) {
            throw new RuntimeException("BPMN XML解析失败", e);
        }
    }

    /**
     * 将BPMN模型转换为JSON格式
     */
    public Map<String, Object> bpmnModelToJson(BpmnModel model) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> processes = new ArrayList<>();

        for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
            Map<String, Object> processMap = new HashMap<>();
            processMap.put("id", process.getId());
            processMap.put("name", process.getName());
            processMap.put("isExecutable", process.isExecutable());

            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> edges = new ArrayList<>();

            // 解析流程元素
            for (FlowElement element : process.getFlowElements()) {
                if (element instanceof StartEvent) {
                    StartEvent start = (StartEvent) element;
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", start.getId());
                    node.put("type", "startEvent");
                    node.put("name", "开始");
                    nodes.add(node);
                } else if (element instanceof EndEvent) {
                    EndEvent end = (EndEvent) element;
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", end.getId());
                    node.put("type", "endEvent");
                    node.put("name", "结束");
                    nodes.add(node);
                } else if (element instanceof UserTask) {
                    UserTask task = (UserTask) element;
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", task.getId());
                    node.put("type", "userTask");
                    node.put("name", task.getName());
                    node.put("assignee", task.getAssignee());
                    // 提取表单属性
                    if (task.getFormProperties() != null) {
                        List<Map<String, Object>> formProps = new ArrayList<>();
                        task.getFormProperties().forEach(prop -> {
                            Map<String, Object> fp = new HashMap<>();
                            fp.put("id", prop.getId());
                            fp.put("name", prop.getName());
                            fp.put("type", prop.getType());
                            fp.put("required", prop.isRequired());
                            formProps.add(fp);
                        });
                        node.put("formProperties", formProps);
                    }
                    nodes.add(node);
                } else if (element instanceof SequenceFlow) {
                    SequenceFlow flow = (SequenceFlow) element;
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("id", flow.getId());
                    edge.put("sourceRef", flow.getSourceRef());
                    edge.put("targetRef", flow.getTargetRef());
                    edge.put("conditionExpression", flow.getConditionExpression());
                    edges.add(edge);
                }
            }

            processMap.put("nodes", nodes);
            processMap.put("edges", edges);
            processes.add(processMap);
        }

        result.put("processes", processes);
        return result;
    }

    /**
     * 根据流程定义ID获取JSON格式
     */
    public Map<String, Object> getProcessDefinitionJson(String processDefinitionId) {
        String xml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (xml == null) {
            return null;
        }
        return bpmnXmlToJson(xml);
    }

    // ============================================================
    // 2. JSON转BPMN（保存设计结果）
    // ============================================================

    /**
     * 将JSON格式转换为BPMN XML
     */
    public String jsonToBpmnXml(Map<String, Object> json) {
        try {
            BpmnModel model = jsonToBpmnModel(json);
            byte[] xmlBytes = bpmnXMLConverter.convertToXML(model);
            return new String(xmlBytes);
        } catch (Exception e) {
            throw new RuntimeException("BPMN JSON解析失败", e);
        }
    }

    /**
     * 将JSON转换为BPMN模型
     */
    public BpmnModel jsonToBpmnModel(Map<String, Object> json) {
        BpmnModel model = new BpmnModel();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> processes = (List<Map<String, Object>>) json.get("processes");
        if (processes != null) {
            for (Map<String, Object> processJson : processes) {
                org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
                process.setId((String) processJson.get("id"));
                process.setName((String) processJson.get("name"));
                process.setExecutable(true);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) processJson.get("nodes");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> edges = (List<Map<String, Object>>) processJson.get("edges");

                // 添加节点
                if (nodes != null) {
                    for (Map<String, Object> node : nodes) {
                        String type = (String) node.get("type");
                        String id = (String) node.get("id");
                        String name = (String) node.get("name");

                        if ("startEvent".equals(type)) {
                            StartEvent start = new StartEvent();
                            start.setId(id);
                            process.addFlowElement(start);
                        } else if ("endEvent".equals(type)) {
                            EndEvent end = new EndEvent();
                            end.setId(id);
                            process.addFlowElement(end);
                        } else if ("userTask".equals(type)) {
                            UserTask task = new UserTask();
                            task.setId(id);
                            task.setName(name);
                            task.setAssignee((String) node.get("assignee"));
                            process.addFlowElement(task);
                        }
                    }
                }

                // 添加连线
                if (edges != null) {
                    for (Map<String, Object> edge : edges) {
                        SequenceFlow flow = new SequenceFlow();
                        flow.setId((String) edge.get("id"));
                        flow.setSourceRef((String) edge.get("sourceRef"));
                        flow.setTargetRef((String) edge.get("targetRef"));
                        flow.setConditionExpression((String) edge.get("conditionExpression"));
                        process.addFlowElement(flow);
                    }
                }

                model.addProcess(process);
            }
        }

        return model;
    }

    /**
     * 保存流程设计并部署
     */
    public Map<String, Object> saveAndDeploy(Map<String, Object> json) {
        String xml = jsonToBpmnXml(json);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> processes = (List<Map<String, Object>>) json.get("processes");
        if (processes != null && !processes.isEmpty()) {
            String processKey = (String) processes.get(0).get("id");
            String processName = (String) processes.get(0).get("name");
            java.io.InputStream inputStream = new java.io.ByteArrayInputStream(xml.getBytes());
            return deployService.deployBPMN(processKey, processName, inputStream);
        }
        throw new IllegalArgumentException("流程定义为空");
    }

    // ============================================================
    // 3. 流程模板管理
    // ============================================================

    /**
     * 获取流程模板列表
     */
    public List<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

        // 标准审批模板
        Map<String, Object> standard = new HashMap<>();
        standard.put("key", "STANDARD");
        standard.put("name", "标准审批");
        standard.put("description", "标准四级审批流程：校核→审核→批准→归档");
        standard.put("steps", Arrays.asList(
                createStep("checker", "校核", "${checker}", "8"),
                createStep("reviewer", "审核", "${reviewer}", "24"),
                createStep("approver", "批准", "${approver}", "48"),
                createStep("archiver", "归档", "${archiver}", "24")
        ));
        templates.add(standard);

        // 物资申领模板
        Map<String, Object> material = new HashMap<>();
        material.put("key", "MATERIAL");
        material.put("name", "物资申领");
        material.put("description", "物资采购审批流程：部门审批→库存核验→领导审批→签收");
        material.put("steps", Arrays.asList(
                createStep("dept_approve", "部门审批", "${deptManager}", "24"),
                createStep("warehouse_check", "库存核验", "${warehouse}", "4"),
                createStep("leader_approve", "领导审批", "${leader}", "48"),
                createStep("signoff", "签收确认", "${applicant}", "72")
        ));
        templates.add(material);

        // 无人机报备模板
        Map<String, Object> drone = new HashMap<>();
        drone.put("key", "DRONE_FLIGHT");
        drone.put("name", "无人机外出报备");
        drone.put("description", "无人机飞行任务审批：空域申请→安全审批→主管批准");
        drone.put("steps", Arrays.asList(
                createStep("airspace", "空域申请", "${airspaceAdmin}", "24"),
                createStep("safety", "安全审批", "${safetyOfficer}", "4"),
                createStep("leader", "主管批准", "${leader}", "2"),
                createStep("execute", "执行确认", "${applicant}", "24")
        ));
        templates.add(drone);

        // 应急流程模板
        Map<String, Object> emergency = new HashMap<>();
        emergency.put("key", "EMERGENCY");
        emergency.put("name", "应急快速通道");
        emergency.put("description", "紧急事件快速响应：应急提交→指挥员批准");
        emergency.put("steps", Arrays.asList(
                createStep("emergency_submit", "应急提交", "${applicant}", "0.5"),
                createStep("commander", "指挥员批准", "${commander}", "0.5")
        ));
        templates.add(emergency);

        return templates;
    }

    /**
     * 根据模板创建流程定义
     */
    public Map<String, Object> createFromTemplate(String templateKey) {
        List<Map<String, Object>> templates = getTemplates();
        for (Map<String, Object> template : templates) {
            if (templateKey.equals(template.get("key"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> steps = (List<Map<String, String>>) template.get("steps");
                return deployService.createStandardProcess(
                        (String) template.get("key"),
                        (String) template.get("name"),
                        steps
                );
            }
        }
        throw new IllegalArgumentException("模板不存在: " + templateKey);
    }

    private Map<String, String> createStep(String key, String name, String assignee, String slaHours) {
        Map<String, String> step = new HashMap<>();
        step.put("key", key);
        step.put("name", name);
        step.put("assignee", assignee);
        step.put("slaHours", slaHours);
        return step;
    }

    // ============================================================
    // 4. 流程设计验证
    // ============================================================

    /**
     * 验证BPMN模型是否有效
     */
    public Map<String, Object> validateBpmn(Map<String, Object> json) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            BpmnModel model = jsonToBpmnModel(json);

            // 验证流程定义
            for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
                // 检查是否有开始事件
                boolean hasStart = process.getFlowElements().stream()
                        .anyMatch(e -> e instanceof StartEvent);
                if (!hasStart) {
                    errors.add("流程 " + process.getName() + " 缺少开始事件");
                }

                // 检查是否有结束事件
                boolean hasEnd = process.getFlowElements().stream()
                        .anyMatch(e -> e instanceof EndEvent);
                if (!hasEnd) {
                    errors.add("流程 " + process.getName() + " 缺少结束事件");
                }

                // 检查是否有用户任务
                long taskCount = process.getFlowElements().stream()
                        .filter(e -> e instanceof UserTask).count();
                if (taskCount == 0) {
                    warnings.add("流程 " + process.getName() + " 没有用户任务节点");
                }

                // 检查连线完整性
                for (FlowElement element : process.getFlowElements()) {
                    if (element instanceof SequenceFlow) {
                        SequenceFlow flow = (SequenceFlow) element;
                        boolean sourceExists = process.getFlowElements().stream()
                                .anyMatch(e -> e.getId().equals(flow.getSourceRef()));
                        boolean targetExists = process.getFlowElements().stream()
                                .anyMatch(e -> e.getId().equals(flow.getTargetRef()));
                        if (!sourceExists) {
                            errors.add("连线 " + flow.getId() + " 的源节点不存在");
                        }
                        if (!targetExists) {
                            errors.add("连线 " + flow.getId() + " 的目标节点不存在");
                        }
                    }
                }
            }

        } catch (Exception e) {
            errors.add("BPMN解析错误: " + e.getMessage());
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }
}
