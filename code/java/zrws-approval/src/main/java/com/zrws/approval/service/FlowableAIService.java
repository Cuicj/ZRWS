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
 * Spring AI 智能流程生成服务
 * <p>用户通过自然语言描述流程需求，AI自动生成BPMN流程定义并部署
 */
@Service
public class FlowableAIService {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private FlowableDeployService deployService;

    private final BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    // ============================================================
    // 1. AI生成流程定义
    // ============================================================

    /**
     * 通过自然语言生成流程定义
     */
    public Map<String, Object> generateProcessFromDescription(String userDescription) {
        ChatClient chatClient = chatClientBuilder.build();

        String systemPrompt = buildSystemPrompt();
        String userPrompt = "请根据以下需求生成审批流程定义JSON：\n\n" + userDescription;

        Prompt prompt = new Prompt(systemPrompt + "\n\n" + userPrompt);

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
        ChatClient chatClient = chatClientBuilder.build();

        String systemPrompt = buildOptimizationPrompt();
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

        ChatClient chatClient = chatClientBuilder.build();

        String systemPrompt = """
                你是流程优化专家，请分析审批流程并提出改进建议。
                
                分析维度：
                1. 流程效率：步骤是否冗余，审批路径是否合理
                2. SLA设置：时限是否合理，是否需要分级处理
                3. 风险控制：是否有必要的审批节点缺失
                4. 用户体验：流程是否清晰，是否有退回/驳回机制
                5. 扩展性：是否支持并行审批、委托、转交等
                
                请返回JSON格式的分析报告。
                """;

        String userPrompt = "请分析以下流程定义：\n" + JSON.toJSONString(existingJson);

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
        ChatClient chatClient = chatClientBuilder.build();

        String systemPrompt = """
                你是流程设计顾问，请根据业务场景推荐最合适的审批流程模板。
                
                可用模板：
                1. STANDARD - 标准审批：校核→审核→批准→归档
                2. MATERIAL - 物资申领：部门审批→库存核验→领导审批→签收
                3. DRONE_FLIGHT - 无人机报备：空域申请→安全审批→主管批准→执行
                4. EMERGENCY - 应急快速通道：应急提交→指挥员批准
                
                请返回JSON格式：
                {
                    "recommendedTemplate": "模板KEY",
                    "reason": "推荐理由",
                    "suggestedModifications": ["建议修改项"],
                    "estimatedSLA": "预估完成时间"
                }
                """;

        String userPrompt = "业务场景描述：\n" + businessScenario;

        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return parseAiResponse(aiResponse);
    }

    // ============================================================
    // 5. 辅助方法
    // ============================================================

    private String buildSystemPrompt() {
        return """
                你是Flowable BPMN流程设计专家，请根据用户需求生成审批流程定义。
                
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
                    "gateways": [
                        {
                            "id": "gateway_id",
                            "type": "exclusive",
                            "conditions": [
                                {"expression": "${action == 'PASS'}", "target": "next_step"},
                                {"expression": "${action == 'REJECT'}", "target": "reject_step"}
                            ]
                        }
                    ],
                    "hasRejectPath": true,
                    "hasReturnPath": true
                }
                
                设计原则：
                1. 每个流程必须有开始和结束事件
                2. 用户任务必须有明确的assignee（使用${变量}格式）
                3. 重要审批节点应设置SLA时限
                4. 应包含驳回和退回机制
                5. 表单属性至少包含审批意见字段
                
                请只返回JSON，不要添加其他说明文字。
                """;
    }

    private String buildOptimizationPrompt() {
        return """
                你是流程优化专家，请根据用户需求优化现有流程定义。
                
                优化原则：
                1. 减少冗余步骤，合并相似审批
                2. 添加必要的驳回/退回路径
                3. 优化SLA时限设置
                4. 添加条件网关支持并行审批
                5. 保持流程清晰易懂
                
                请返回优化后的完整流程定义JSON，格式与原始定义一致。
                """;
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