package com.zrws.approval.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Spring AI 2.0 GA 智能流程生成服务
 * <p>用户通过自然语言描述流程需求，AI自动生成BPMN流程定义并部署
 * 
 * 功能：
 * 1. 自然语言生成流程定义
 * 2. AI优化现有流程
 * 3. 流程分析与改进建议
 * 4. 流程模板推荐
 * 5. 多轮对话式流程设计
 * 6. 智能表单生成
 */
@Service
public class FlowableAIService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private FlowableDeployService deployService;

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

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        // 解析AI返回的JSON
        Map<String, Object> processJson = parseAiResponse(aiResponse);

        // 验证生成的流程
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
        // 1. AI生成流程定义
        Map<String, Object> processJson = generateProcessFromDescription(userDescription);

        // 2. 转换为BPMN XML
        String bpmnXml = convertJsonToBpmnXml(processJson);

        // 3. 部署流程
        String processKey = (String) processJson.get("processKey");
        String processName = (String) processJson.get("processName");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes());

        Map<String, Object> deployResult = deployService.deployBPMN(processKey, processName, inputStream);

        // 4. 返回完整结果
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
        // 1. 获取现有流程定义
        String existingXml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (existingXml == null) {
            throw new IllegalArgumentException("流程定义不存在");
        }

        // 2. 获取现有流程JSON
        Map<String, Object> existingJson = bpmnXmlToJson(existingXml);

        // 3. AI优化
        String userPrompt = String.format(
                "现有流程定义：\n%s\n\n优化需求：\n%s\n\n请返回优化后的完整流程定义JSON。",
                JSON.toJSONString(existingJson), optimizationRequest
        );

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        // 4. 解析优化后的流程
        Map<String, Object> optimizedJson = parseAiResponse(aiResponse);

        // 5. 部署新版本
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

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

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

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return parseAiResponse(aiResponse);
    }

    // ============================================================
    // 5. 多轮对话式流程设计
    // ============================================================

    /**
     * 多轮对话式流程设计
     */
    public Map<String, Object> chatDesign(String sessionId, String userMessage) {
        // 获取或创建会话历史
        List<Map<String, Object>> history = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // 构建对话上下文
        StringBuilder contextBuilder = new StringBuilder();
        for (Map<String, Object> msg : history) {
            contextBuilder.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }
        contextBuilder.append("用户: ").append(userMessage);

        // AI响应
        String aiResponse = chatClient.prompt()
                .user(contextBuilder.toString())
                .call()
                .content();

        // 记录对话
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

        // 尝试解析为流程定义
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("response", aiResponse);
        result.put("history", history);

        // 如果AI返回了流程定义JSON，尝试提取
        try {
            Map<String, Object> processJson = parseAiResponse(aiResponse);
            if (processJson.containsKey("processKey")) {
                result.put("processDefinition", processJson);
                result.put("readyToDeploy", true);
            }
        } catch (Exception ignored) {
            // 不是流程定义，继续对话
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
                processKey, stepKey
        );

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return parseAiResponse(aiResponse);
    }

    // ============================================================
    // 7. 辅助方法
    // ============================================================

    private String buildGeneratePrompt(String userDescription) {
        return String.format("""
                请根据以下需求生成审批流程定义JSON：
                
                用户需求：%s
                
                输出格式要求（JSON）：
                {
                    "processKey": "PROCESS_KEY",
                    "processName": "流程名称",
                    "description": "流程描述",
                    "steps": [
                        {
                            "key": "step_key",
                            "name": "步骤名称",
                            "assignee": "${assignee_var}",
                            "slaHours": 24,
                            "formProperties": [
                                {"id": "opinion", "name": "审批意见", "type": "string", "required": true}
                            ]
                        }
                    ],
                    "hasRejectPath": true,
                    "hasReturnPath": true
                }
                
                请只返回JSON，不要添加其他说明文字。
                """, userDescription);
    }

    private Map<String, Object> parseAiResponse(String aiResponse) {
        // 清理可能的Markdown标记
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
        Process process = new Process();

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

            // 添加连线
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
        BpmnModel model = bpmnXMLConverter.convertToBpmnModel(bpmnXml.getBytes());

        Map<String, Object> result = new HashMap<>();
        for (Process process : model.getProcesses()) {
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