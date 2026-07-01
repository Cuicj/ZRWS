package com.zrws.approval.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * AI智能流程生成服务
 * <p>用户通过自然语言描述流程需求，AI自动生成BPMN流程定义并部署
 */
@Service
public class FlowableAIService {

    @Autowired
    private FlowableDeployService deployService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatModel chatModel;

    private final BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    // 存储对话会话
    private final Map<String, List<Map<String, Object>>> conversationHistory = new HashMap<>();

    // ============================================================
    // 1. AI生成流程定义
    // ============================================================

    /**
     * 通过自然语言生成流程定义
     */
    public Map<String, Object> generateProcessFromDescription(String userDescription) {
        String userPrompt = buildGeneratePrompt(userDescription);
        String aiResponse = callOpenAI(userPrompt);
        Map<String, Object> processJson = parseAiResponse(aiResponse);

        Map<String, Object> validation = validateGeneratedProcess(processJson);
        if (!(Boolean) validation.get("valid")) {
            throw new RuntimeException("AI生成的流程定义无效: " + validation.get("errors"));
        }

        return processJson;
    }

    /**
     * 生成并部署流程
     */
    public Map<String, Object> generateAndDeploy(String userDescription) {
        Map<String, Object> processJson = generateProcessFromDescription(userDescription);
        String bpmnXml = convertJsonToBpmnXml(processJson);

        String processKey = (String) processJson.get("processKey");
        String processName = (String) processJson.get("processName");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes());

        Map<String, Object> deployResult = deployService.deployBPMN(processKey, processName, inputStream);

        Map<String, Object> result = new HashMap<>();
        result.put("aiGenerated", processJson);
        result.put("bpmnXml", bpmnXml);
        result.put("deployment", deployResult);
        result.put("success", true);
        return result;
    }

    // ============================================================
    // 2. AI优化现有流程
    // ============================================================

    /**
     * AI优化现有流程定义
     */
    public Map<String, Object> optimizeProcess(String processDefinitionId, String optimizationRequest) {
        String existingXml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (existingXml == null) {
            throw new IllegalArgumentException("流程定义不存在");
        }

        Map<String, Object> existingJson = bpmnXmlToJson(existingXml);

        String userPrompt = "现有流程定义：\n" + JSON.toJSONString(existingJson) +
                "\n\n优化需求：\n" + optimizationRequest +
                "\n\n请返回优化后的完整流程定义JSON。";

        String aiResponse = callOpenAI(userPrompt);
        Map<String, Object> optimizedJson = parseAiResponse(aiResponse);

        String bpmnXml = convertJsonToBpmnXml(optimizedJson);
        String processKey = (String) optimizedJson.get("processKey");
        String processName = (String) optimizedJson.get("processName");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes());
        Map<String, Object> deployResult = deployService.deployBPMN(processKey, processName, inputStream);

        Map<String, Object> result = new HashMap<>();
        result.put("original", existingJson);
        result.put("optimized", optimizedJson);
        result.put("deployment", deployResult);
        return result;
    }

    // ============================================================
    // 3. AI分析流程
    // ============================================================

    /**
     * AI分析流程并提出改进建议
     */
    public Map<String, Object> analyzeProcess(String processDefinitionId) {
        String existingXml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (existingXml == null) {
            throw new IllegalArgumentException("流程定义不存在");
        }

        Map<String, Object> existingJson = bpmnXmlToJson(existingXml);

        String userPrompt = "请分析以下流程定义并提出改进建议：\n" + JSON.toJSONString(existingJson) +
                "\n\n请从以下维度分析：\n1. 流程效率\n2. SLA设置\n3. 风险控制\n4. 用户体验\n5. 扩展性\n\n返回JSON格式分析报告。";

        String aiResponse = callOpenAI(userPrompt);
        Map<String, Object> analysis = parseAiResponse(aiResponse);

        Map<String, Object> result = new HashMap<>();
        result.put("processDefinitionId", processDefinitionId);
        result.put("processJson", existingJson);
        result.put("analysis", analysis);
        return result;
    }

    // ============================================================
    // 4. AI推荐流程模板
    // ============================================================

    /**
     * 根据业务场景推荐合适的流程模板
     */
    public Map<String, Object> recommendTemplate(String businessScenario) {
        String userPrompt = "业务场景描述：\n" + businessScenario +
                "\n\n请推荐最合适的审批流程模板。可用模板：\n" +
                "1. STANDARD - 标准审批：校核→审核→批准→归档\n" +
                "2. MATERIAL - 物资申领：部门审批→库存核验→领导审批→签收\n" +
                "3. DRONE_FLIGHT - 无人机报备：空域申请→安全审批→主管批准→执行\n" +
                "4. EMERGENCY - 应急快速通道：应急提交→指挥员批准\n\n" +
                "请返回JSON格式：{\"recommendedTemplate\":\"模板KEY\",\"reason\":\"推荐理由\",\"suggestedModifications\":[]}";

        String aiResponse = callOpenAI(userPrompt);
        return parseAiResponse(aiResponse);
    }

    // ============================================================
    // 5. 多轮对话式流程设计
    // ============================================================

    /**
     * 多轮对话式流程设计
     */
    public Map<String, Object> chatDesign(String sessionId, String userMessage) {
        List<Map<String, Object>> history = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        StringBuilder contextBuilder = new StringBuilder();
        for (Map<String, Object> msg : history) {
            contextBuilder.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }
        contextBuilder.append("用户: ").append(userMessage);

        String aiResponse = callOpenAI(contextBuilder.toString());

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "用户");
        userMsg.put("content", userMessage);
        userMsg.put("timestamp", System.currentTimeMillis());
        history.add(userMsg);

        Map<String, Object> aiMsg = new HashMap<>();
        aiMsg.put("role", "AI");
        aiMsg.put("content", aiResponse);
        aiMsg.put("timestamp", System.currentTimeMillis());
        history.add(aiMsg);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("response", aiResponse);
        result.put("history", history);

        try {
            Map<String, Object> processJson = parseAiResponse(aiResponse);
            if (processJson.containsKey("processKey")) {
                result.put("processDefinition", processJson);
                result.put("readyToDeploy", true);
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    /**
     * 清除对话会话
     */
    public void clearSession(String sessionId) {
        conversationHistory.remove(sessionId);
    }

    // ============================================================
    // 6. 智能表单生成
    // ============================================================

    /**
     * 根据流程步骤智能生成表单字段
     */
    public Map<String, Object> generateFormFields(String processKey, String stepKey) {
        String userPrompt = String.format(
                "请为审批流程 %s 的步骤 %s 生成表单字段定义。\n\n" +
                "表单应包含：\n" +
                "- 审批意见（必填）\n" +
                "- 审批结果（通过/驳回）\n" +
                "- 相关附件上传\n" +
                "- 根据步骤类型添加特定字段\n\n" +
                "返回JSON格式表单定义。",
                processKey, stepKey);

        String aiResponse = callOpenAI(userPrompt);
        return parseAiResponse(aiResponse);
    }

    // ============================================================
    // 7. OpenAI API调用
    // ============================================================

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) {
        return chatModel.call(prompt);
    }

    // ============================================================
    // 8. 辅助方法
    // ============================================================

    private String buildGeneratePrompt(String userDescription) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下需求生成审批流程定义JSON：\n\n");
        prompt.append("用户需求：").append(userDescription).append("\n\n");
        prompt.append("输出格式要求（JSON）：\n");
        prompt.append("{\n");
        prompt.append("    \"processKey\": \"PROCESS_KEY\",\n");
        prompt.append("    \"processName\": \"流程名称\",\n");
        prompt.append("    \"description\": \"流程描述\",\n");
        prompt.append("    \"steps\": [\n");
        prompt.append("        {\n");
        prompt.append("            \"key\": \"step_key\",\n");
        prompt.append("            \"name\": \"步骤名称\",\n");
        prompt.append("            \"assignee\": \"${assignee_var}\",\n");
        prompt.append("            \"slaHours\": 24,\n");
        prompt.append("            \"formProperties\": [\n");
        prompt.append("                {\"id\": \"opinion\", \"name\": \"审批意见\", \"type\": \"string\", \"required\": true}\n");
        prompt.append("            ]\n");
        prompt.append("        }\n");
        prompt.append("    ],\n");
        prompt.append("    \"hasRejectPath\": true,\n");
        prompt.append("    \"hasReturnPath\": true\n");
        prompt.append("}\n\n");
        prompt.append("请只返回JSON，不要添加其他说明文字。");
        return prompt.toString();
    }

    private Map<String, Object> parseAiResponse(String aiResponse) {
        String cleaned = aiResponse
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        try {
            return JSON.parseObject(cleaned, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("AI返回内容解析失败: " + aiResponse, e);
        }
    }

    private String convertJsonToBpmnXml(Map<String, Object> processJson) {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();

        String processKey = (String) processJson.get("processKey");
        String processName = (String) processJson.get("processName");

        process.setId(processKey);
        process.setName(processName);
        process.setExecutable(true);

        // 开始事件
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        process.addFlowElement(startEvent);

        // 用户任务
        List<Map<String, Object>> steps = (List<Map<String, Object>>) processJson.get("steps");
        String prevElement = "start";

        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            String stepKey = (String) step.get("key");
            String stepName = (String) step.get("name");
            String assignee = (String) step.get("assignee");

            UserTask userTask = new UserTask();
            userTask.setId(stepKey);
            userTask.setName(stepName);
            userTask.setAssignee(assignee);

            SequenceFlow flow = new SequenceFlow(prevElement, stepKey);
            flow.setId("flow_" + prevElement + "_to_" + stepKey);
            process.addFlowElement(flow);

            process.addFlowElement(userTask);
            prevElement = stepKey;
        }

        // 结束事件
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        process.addFlowElement(endEvent);

        SequenceFlow endFlow = new SequenceFlow(prevElement, "end");
        endFlow.setId("flow_" + prevElement + "_to_end");
        process.addFlowElement(endFlow);

        model.addProcess(process);

        byte[] xmlBytes = bpmnXMLConverter.convertToXML(model);
        return new String(xmlBytes);
    }

    private Map<String, Object> bpmnXmlToJson(String bpmnXml) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(bpmnXml.getBytes()));
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);

            Map<String, Object> result = new HashMap<>();
            for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
                result.put("processKey", process.getId());
                result.put("processName", process.getName());

                List<Map<String, Object>> steps = new ArrayList<>();
                for (FlowElement element : process.getFlowElements()) {
                    if (element instanceof UserTask) {
                        UserTask task = (UserTask) element;
                        Map<String, Object> step = new HashMap<>();
                        step.put("key", task.getId());
                        step.put("name", task.getName());
                        step.put("assignee", task.getAssignee());
                        steps.add(step);
                    }
                }
                result.put("steps", steps);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("BPMN XML解析失败", e);
        }
    }

    private Map<String, Object> validateGeneratedProcess(Map<String, Object> processJson) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        if (processJson.get("processKey") == null) {
            errors.add("缺少processKey");
        }
        if (processJson.get("processName") == null) {
            errors.add("缺少processName");
        }
        if (processJson.get("steps") == null || ((List<?>) processJson.get("steps")).isEmpty()) {
            errors.add("缺少审批步骤");
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        return result;
    }
}
