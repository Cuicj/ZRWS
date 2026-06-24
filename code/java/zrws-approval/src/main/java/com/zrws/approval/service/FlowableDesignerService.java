package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

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

    public Map<String, Object> bpmnModelToJson(BpmnModel model) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> processes = new ArrayList<>();

        for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
            Map<String, Object> processMap = new HashMap<>();
            processMap.put("id", process.getId());
            processMap.put("name", process.getName());
            processMap.put("isExecutable", process.isExecutable());
            processMap.put("documentation", process.getDocumentation());

            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> edges = new ArrayList<>();

            for (FlowElement element : process.getFlowElements()) {
                if (element instanceof StartEvent) {
                    nodes.add(convertStartEvent((StartEvent) element));
                } else if (element instanceof EndEvent) {
                    nodes.add(convertEndEvent((EndEvent) element));
                } else if (element instanceof UserTask) {
                    nodes.add(convertUserTask((UserTask) element));
                } else if (element instanceof ServiceTask) {
                    nodes.add(convertServiceTask((ServiceTask) element));
                } else if (element instanceof ExclusiveGateway) {
                    nodes.add(convertGateway((ExclusiveGateway) element, "exclusiveGateway"));
                } else if (element instanceof ParallelGateway) {
                    nodes.add(convertGateway((ParallelGateway) element, "parallelGateway"));
                } else if (element instanceof InclusiveGateway) {
                    nodes.add(convertGateway((InclusiveGateway) element, "inclusiveGateway"));
                } else if (element instanceof EventGateway) {
                    nodes.add(convertGateway((EventGateway) element, "eventGateway"));
                } else if (element instanceof SubProcess) {
                    nodes.add(convertSubProcess((SubProcess) element));
                } else if (element instanceof SequenceFlow) {
                    edges.add(convertSequenceFlow((SequenceFlow) element));
                }
            }

            processMap.put("nodes", nodes);
            processMap.put("edges", edges);
            processes.add(processMap);
        }

        result.put("processes", processes);
        return result;
    }

    private Map<String, Object> convertStartEvent(StartEvent event) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", event.getId());
        node.put("type", "startEvent");
        node.put("name", event.getName() != null ? event.getName() : "开始");
        node.put("documentation", event.getDocumentation());
        return node;
    }

    private Map<String, Object> convertEndEvent(EndEvent event) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", event.getId());
        node.put("type", "endEvent");
        node.put("name", event.getName() != null ? event.getName() : "结束");
        node.put("documentation", event.getDocumentation());
        return node;
    }

    private Map<String, Object> convertUserTask(UserTask task) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", task.getId());
        node.put("type", "userTask");
        node.put("name", task.getName());
        node.put("assignee", task.getAssignee());
        node.put("candidateUsers", task.getCandidateUsers());
        node.put("candidateGroups", task.getCandidateGroups());
        node.put("dueDate", task.getDueDate());
        node.put("priority", task.getPriority());
        node.put("documentation", task.getDocumentation());

        if (task.getFormProperties() != null && !task.getFormProperties().isEmpty()) {
            List<Map<String, Object>> formProps = new ArrayList<>();
            task.getFormProperties().forEach(prop -> {
                Map<String, Object> fp = new HashMap<>();
                fp.put("id", prop.getId());
                fp.put("name", prop.getName());
                fp.put("type", prop.getType());
                fp.put("required", prop.isRequired());
                fp.put("defaultValue", prop.getDefaultExpression());
                formProps.add(fp);
            });
            node.put("formProperties", formProps);
        }
        return node;
    }

    private Map<String, Object> convertServiceTask(ServiceTask task) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", task.getId());
        node.put("type", "serviceTask");
        node.put("name", task.getName());
        node.put("implementation", task.getImplementation());
        node.put("implementationType", task.getImplementationType());
        node.put("documentation", task.getDocumentation());
        return node;
    }

    private Map<String, Object> convertGateway(Gateway gateway, String type) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", gateway.getId());
        node.put("type", type);
        node.put("name", gateway.getName());
        node.put("documentation", gateway.getDocumentation());
        return node;
    }

    private Map<String, Object> convertSubProcess(SubProcess subProcess) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", subProcess.getId());
        node.put("type", "subProcess");
        node.put("name", subProcess.getName());
        node.put("documentation", subProcess.getDocumentation());

        List<Map<String, Object>> subNodes = new ArrayList<>();
        List<Map<String, Object>> subEdges = new ArrayList<>();

        for (FlowElement element : subProcess.getFlowElements()) {
            if (element instanceof StartEvent) {
                subNodes.add(convertStartEvent((StartEvent) element));
            } else if (element instanceof EndEvent) {
                subNodes.add(convertEndEvent((EndEvent) element));
            } else if (element instanceof UserTask) {
                subNodes.add(convertUserTask((UserTask) element));
            } else if (element instanceof ExclusiveGateway) {
                subNodes.add(convertGateway((ExclusiveGateway) element, "exclusiveGateway"));
            } else if (element instanceof ParallelGateway) {
                subNodes.add(convertGateway((ParallelGateway) element, "parallelGateway"));
            } else if (element instanceof SequenceFlow) {
                subEdges.add(convertSequenceFlow((SequenceFlow) element));
            }
        }

        node.put("nodes", subNodes);
        node.put("edges", subEdges);
        return node;
    }

    private Map<String, Object> convertSequenceFlow(SequenceFlow flow) {
        Map<String, Object> edge = new HashMap<>();
        edge.put("id", flow.getId());
        edge.put("sourceRef", flow.getSourceRef());
        edge.put("targetRef", flow.getTargetRef());
        edge.put("name", flow.getName());
        edge.put("conditionExpression", flow.getConditionExpression());
        edge.put("documentation", flow.getDocumentation());
        return edge;
    }

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

    public String jsonToBpmnXml(Map<String, Object> json) {
        try {
            BpmnModel model = jsonToBpmnModel(json);
            byte[] xmlBytes = bpmnXMLConverter.convertToXML(model);
            return new String(xmlBytes);
        } catch (Exception e) {
            throw new RuntimeException("BPMN JSON解析失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public BpmnModel jsonToBpmnModel(Map<String, Object> json) {
        BpmnModel model = new BpmnModel();

        List<Map<String, Object>> processes = (List<Map<String, Object>>) json.get("processes");
        if (processes != null) {
            for (Map<String, Object> processJson : processes) {
                org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
                process.setId((String) processJson.get("id"));
                process.setName((String) processJson.get("name"));
                process.setExecutable(true);

                if (processJson.get("documentation") != null) {
                    process.setDocumentation((String) processJson.get("documentation"));
                }

                List<Map<String, Object>> nodes = (List<Map<String, Object>>) processJson.get("nodes");
                List<Map<String, Object>> edges = (List<Map<String, Object>>) processJson.get("edges");

                if (nodes != null) {
                    for (Map<String, Object> node : nodes) {
                        FlowElement element = createFlowElement(node);
                        if (element != null) {
                            process.addFlowElement(element);
                        }
                    }
                }

                if (edges != null) {
                    for (Map<String, Object> edge : edges) {
                        SequenceFlow flow = new SequenceFlow();
                        flow.setId((String) edge.get("id"));
                        flow.setSourceRef((String) edge.get("sourceRef"));
                        flow.setTargetRef((String) edge.get("targetRef"));
                        if (edge.get("name") != null) {
                            flow.setName((String) edge.get("name"));
                        }
                        if (edge.get("conditionExpression") != null) {
                            flow.setConditionExpression((String) edge.get("conditionExpression"));
                        }
                        if (edge.get("documentation") != null) {
                            flow.setDocumentation((String) edge.get("documentation"));
                        }
                        process.addFlowElement(flow);
                    }
                }

                model.addProcess(process);
            }
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    private FlowElement createFlowElement(Map<String, Object> node) {
        String type = (String) node.get("type");
        String id = (String) node.get("id");
        String name = (String) node.get("name");
        String documentation = (String) node.get("documentation");

        switch (type) {
            case "startEvent": {
                StartEvent start = new StartEvent();
                start.setId(id);
                if (name != null) start.setName(name);
                if (documentation != null) start.setDocumentation(documentation);
                return start;
            }
            case "endEvent": {
                EndEvent end = new EndEvent();
                end.setId(id);
                if (name != null) end.setName(name);
                if (documentation != null) end.setDocumentation(documentation);
                return end;
            }
            case "userTask": {
                UserTask task = new UserTask();
                task.setId(id);
                task.setName(name);
                if (node.get("assignee") != null) {
                    task.setAssignee((String) node.get("assignee"));
                }
                if (node.get("candidateUsers") != null) {
                    task.setCandidateUsers((List<String>) node.get("candidateUsers"));
                }
                if (node.get("candidateGroups") != null) {
                    task.setCandidateGroups((List<String>) node.get("candidateGroups"));
                }
                if (node.get("dueDate") != null) {
                    task.setDueDate((String) node.get("dueDate"));
                }
                if (node.get("priority") != null) {
                    task.setPriority(String.valueOf(node.get("priority")));
                }
                if (documentation != null) task.setDocumentation(documentation);

                if (node.get("formProperties") != null) {
                    List<Map<String, Object>> formProps = (List<Map<String, Object>>) node.get("formProperties");
                    for (Map<String, Object> fp : formProps) {
                        FormProperty prop = new FormProperty();
                        prop.setId((String) fp.get("id"));
                        prop.setName((String) fp.get("name"));
                        prop.setType((String) fp.get("type"));
                        if (fp.get("required") != null) {
                            prop.setRequired((Boolean) fp.get("required"));
                        }
                        if (fp.get("defaultValue") != null) {
                            prop.setDefaultExpression((String) fp.get("defaultValue"));
                        }
                        task.getFormProperties().add(prop);
                    }
                }
                return task;
            }
            case "serviceTask": {
                ServiceTask task = new ServiceTask();
                task.setId(id);
                task.setName(name);
                if (node.get("implementation") != null) {
                    task.setImplementation((String) node.get("implementation"));
                }
                if (node.get("implementationType") != null) {
                    task.setImplementationType((String) node.get("implementationType"));
                }
                if (documentation != null) task.setDocumentation(documentation);
                return task;
            }
            case "exclusiveGateway": {
                ExclusiveGateway gateway = new ExclusiveGateway();
                gateway.setId(id);
                if (name != null) gateway.setName(name);
                if (documentation != null) gateway.setDocumentation(documentation);
                return gateway;
            }
            case "parallelGateway": {
                ParallelGateway gateway = new ParallelGateway();
                gateway.setId(id);
                if (name != null) gateway.setName(name);
                if (documentation != null) gateway.setDocumentation(documentation);
                return gateway;
            }
            case "inclusiveGateway": {
                InclusiveGateway gateway = new InclusiveGateway();
                gateway.setId(id);
                if (name != null) gateway.setName(name);
                if (documentation != null) gateway.setDocumentation(documentation);
                return gateway;
            }
            case "eventGateway": {
                EventGateway gateway = new EventGateway();
                gateway.setId(id);
                if (name != null) gateway.setName(name);
                if (documentation != null) gateway.setDocumentation(documentation);
                return gateway;
            }
            case "subProcess": {
                SubProcess subProcess = new SubProcess();
                subProcess.setId(id);
                subProcess.setName(name);
                if (documentation != null) subProcess.setDocumentation(documentation);

                List<Map<String, Object>> subNodes = (List<Map<String, Object>>) node.get("nodes");
                List<Map<String, Object>> subEdges = (List<Map<String, Object>>) node.get("edges");

                if (subNodes != null) {
                    for (Map<String, Object> subNode : subNodes) {
                        FlowElement subElement = createFlowElement(subNode);
                        if (subElement != null) {
                            subProcess.addFlowElement(subElement);
                        }
                    }
                }

                if (subEdges != null) {
                    for (Map<String, Object> subEdge : subEdges) {
                        SequenceFlow flow = new SequenceFlow();
                        flow.setId((String) subEdge.get("id"));
                        flow.setSourceRef((String) subEdge.get("sourceRef"));
                        flow.setTargetRef((String) subEdge.get("targetRef"));
                        if (subEdge.get("name") != null) {
                            flow.setName((String) subEdge.get("name"));
                        }
                        if (subEdge.get("conditionExpression") != null) {
                            flow.setConditionExpression((String) subEdge.get("conditionExpression"));
                        }
                        subProcess.addFlowElement(flow);
                    }
                }

                return subProcess;
            }
            default:
                return null;
        }
    }

    // ============================================================
    // 3. 流程生命周期管理（设计-保存-审核-发布-部署）
    // ============================================================

    private final Map<String, Map<String, Object>> draftStore = new HashMap<>();

    public Map<String, Object> saveDraft(String processKey, Map<String, Object> json) {
        Map<String, Object> draft = new HashMap<>();
        draft.put("processKey", processKey);
        draft.put("processName", json.get("processName"));
        draft.put("json", json);
        draft.put("status", "DRAFT");
        draft.put("version", 1);
        draft.put("createTime", new Date());
        draft.put("updateTime", new Date());

        if (draftStore.containsKey(processKey)) {
            Map<String, Object> old = draftStore.get(processKey);
            int oldVersion = (Integer) old.get("version");
            draft.put("version", oldVersion + 1);
        }

        draftStore.put(processKey, draft);
        return draft;
    }

    public Map<String, Object> getDraft(String processKey) {
        return draftStore.get(processKey);
    }

    public List<Map<String, Object>> listDrafts() {
        return new ArrayList<>(draftStore.values());
    }

    public Map<String, Object> submitForReview(String processKey) {
        Map<String, Object> draft = draftStore.get(processKey);
        if (draft == null) {
            throw new RuntimeException("流程草稿不存在");
        }
        draft.put("status", "PENDING_REVIEW");
        draft.put("submitTime", new Date());
        draft.put("updateTime", new Date());
        return draft;
    }

    public Map<String, Object> review(String processKey, boolean approved, String comment) {
        Map<String, Object> draft = draftStore.get(processKey);
        if (draft == null) {
            throw new RuntimeException("流程草稿不存在");
        }
        draft.put("status", approved ? "APPROVED" : "REJECTED");
        draft.put("reviewComment", comment);
        draft.put("reviewTime", new Date());
        draft.put("updateTime", new Date());
        return draft;
    }

    public Map<String, Object> publish(String processKey) {
        Map<String, Object> draft = draftStore.get(processKey);
        if (draft == null) {
            throw new RuntimeException("流程草稿不存在");
        }
        if (!"APPROVED".equals(draft.get("status"))) {
            throw new RuntimeException("流程未通过审核，无法发布");
        }
        draft.put("status", "PUBLISHED");
        draft.put("publishTime", new Date());
        draft.put("updateTime", new Date());
        return draft;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> deployPublished(String processKey) {
        Map<String, Object> draft = draftStore.get(processKey);
        if (draft == null) {
            throw new RuntimeException("流程草稿不存在");
        }
        if (!"PUBLISHED".equals(draft.get("status"))) {
            throw new RuntimeException("流程未发布，无法部署");
        }

        Map<String, Object> json = (Map<String, Object>) draft.get("json");
        String xml = jsonToBpmnXml(json);
        String processName = (String) draft.get("processName");

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Map<String, Object> result = deployService.deployBPMN(processKey, processName, inputStream);

        draft.put("status", "DEPLOYED");
        draft.put("deployTime", new Date());
        draft.put("deploymentId", result.get("deploymentId"));
        draft.put("processDefinitionId", result.get("processDefinitionId"));
        draft.put("updateTime", new Date());

        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> saveAndDeploy(Map<String, Object> json) {
        String xml = jsonToBpmnXml(json);
        List<Map<String, Object>> processes = (List<Map<String, Object>>) json.get("processes");
        if (processes != null && !processes.isEmpty()) {
            String processKey = (String) processes.get(0).get("id");
            String processName = (String) processes.get(0).get("name");
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
            return deployService.deployBPMN(processKey, processName, inputStream);
        }
        throw new IllegalArgumentException("流程定义为空");
    }

    // ============================================================
    // 4. 流程模板管理
    // ============================================================

    public List<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

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
    // 5. 流程设计验证
    // ============================================================

    @SuppressWarnings("unchecked")
    public Map<String, Object> validateBpmn(Map<String, Object> json) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            BpmnModel model = jsonToBpmnModel(json);

            for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
                if (process.getId() == null || process.getId().isEmpty()) {
                    errors.add("流程ID不能为空");
                }

                boolean hasStart = process.getFlowElements().stream()
                        .anyMatch(e -> e instanceof StartEvent);
                if (!hasStart) {
                    errors.add("流程 " + process.getName() + " 缺少开始事件");
                }

                boolean hasEnd = process.getFlowElements().stream()
                        .anyMatch(e -> e instanceof EndEvent);
                if (!hasEnd) {
                    errors.add("流程 " + process.getName() + " 缺少结束事件");
                }

                long taskCount = process.getFlowElements().stream()
                        .filter(e -> e instanceof UserTask).count();
                if (taskCount == 0) {
                    warnings.add("流程 " + process.getName() + " 没有用户任务节点");
                }

                Set<String> elementIds = new HashSet<>();
                for (FlowElement element : process.getFlowElements()) {
                    if (elementIds.contains(element.getId())) {
                        errors.add("元素ID重复: " + element.getId());
                    }
                    elementIds.add(element.getId());
                }

                for (FlowElement element : process.getFlowElements()) {
                    if (element instanceof SequenceFlow) {
                        SequenceFlow flow = (SequenceFlow) element;
                        boolean sourceExists = elementIds.contains(flow.getSourceRef());
                        boolean targetExists = elementIds.contains(flow.getTargetRef());
                        if (!sourceExists) {
                            errors.add("连线 " + flow.getId() + " 的源节点不存在: " + flow.getSourceRef());
                        }
                        if (!targetExists) {
                            errors.add("连线 " + flow.getId() + " 的目标节点不存在: " + flow.getTargetRef());
                        }
                    }

                    if (element instanceof Gateway) {
                        Gateway gateway = (Gateway) element;
                        long incoming = process.getFlowElements().stream()
                                .filter(e -> e instanceof SequenceFlow)
                                .map(e -> (SequenceFlow) e)
                                .filter(f -> f.getTargetRef().equals(gateway.getId()))
                                .count();
                        long outgoing = process.getFlowElements().stream()
                                .filter(e -> e instanceof SequenceFlow)
                                .map(e -> (SequenceFlow) e)
                                .filter(f -> f.getSourceRef().equals(gateway.getId()))
                                .count();

                        if (gateway instanceof ExclusiveGateway) {
                            if (outgoing < 2) {
                                warnings.add("排他网关 " + gateway.getName() + " 建议至少有2条流出连线");
                            }
                        }
                    }
                }

                validateSubProcess(process, errors, warnings);
            }

        } catch (Exception e) {
            errors.add("BPMN解析错误: " + e.getMessage());
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    private void validateSubProcess(FlowElementsContainer container, List<String> errors, List<String> warnings) {
        for (FlowElement element : container.getFlowElements()) {
            if (element instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) element;

                boolean hasStart = subProcess.getFlowElements().stream()
                        .anyMatch(e -> e instanceof StartEvent);
                if (!hasStart) {
                    errors.add("子流程 " + subProcess.getName() + " 缺少开始事件");
                }

                boolean hasEnd = subProcess.getFlowElements().stream()
                        .anyMatch(e -> e instanceof EndEvent);
                if (!hasEnd) {
                    errors.add("子流程 " + subProcess.getName() + " 缺少结束事件");
                }

                Set<String> subIds = new HashSet<>();
                for (FlowElement subEl : subProcess.getFlowElements()) {
                    if (subIds.contains(subEl.getId())) {
                        errors.add("子流程元素ID重复: " + subEl.getId());
                    }
                    subIds.add(subEl.getId());
                }

                validateSubProcess(subProcess, errors, warnings);
            }
        }
    }
}
