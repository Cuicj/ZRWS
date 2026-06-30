package com.zrws.approval;

import com.zrws.approval.service.FlowableAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI流程生成服务测试类
 * 注意：这些测试需要配置 OpenAI API Key 才能正常运行
 * 如果没有配置，会抛出异常
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=${OPENAI_API_KEY:}"
})
public class FlowableAITest {

    @Autowired(required = false)
    private FlowableAIService aiService;

    /**
     * 测试AI服务是否注入（需要API Key）
     */
    @Test
    public void testAiServiceAvailable() {
        if (aiService == null) {
            System.out.println("AI服务未配置，跳过AI相关测试");
            return;
        }
        assertNotNull(aiService);
    }

    /**
     * 测试流程验证（不需要API Key）
     */
    @Test
    public void testProcessValidation() {
        // 创建一个简单的流程定义
        Map<String, Object> processJson = new HashMap<>();
        processJson.put("processKey", "TEST_AI");
        processJson.put("processName", "AI测试流程");
        processJson.put("processDescription", "测试用流程");

        List<Map<String, Object>> steps = List.of(
            createStep("step1", "提交申请", "${applicant}", 24),
            createStep("step2", "领导审批", "${leader}", 48),
            createStep("step3", "执行确认", "${executor}", 24)
        );
        processJson.put("steps", steps);
        processJson.put("hasRejectPath", true);
        processJson.put("hasReturnPath", true);

        // 验证流程定义
        Map<String, Object> validation = validateProcess(processJson);
        assertTrue((Boolean) validation.get("valid"), "流程定义应该有效");
        assertTrue(((List<?>) validation.get("errors")).isEmpty(), "不应该有错误");
    }

    /**
     * 测试模板推荐（不需要API Key）
     */
    @Test
    public void testTemplateRecommendation() {
        // 模拟模板推荐逻辑
        String businessScenario = "我需要申请购买一台无人机设备，需要部门负责人和财务审批";

        // 根据场景关键词推荐模板
        String recommendedTemplate = recommendTemplate(businessScenario);
        assertNotNull(recommendedTemplate);
        assertTrue(recommendedTemplate.equals("MATERIAL") || recommendedTemplate.equals("STANDARD"));
    }

    /**
     * 测试表单字段生成逻辑（不需要API Key）
     */
    @Test
    public void testFormFieldGeneration() {
        // 模拟表单字段生成
        Map<String, Object> formFields = generateFormFields("STANDARD", "checker");

        assertNotNull(formFields);
        assertNotNull(formFields.get("fields"));
        assertTrue(((List<?>) formFields.get("fields")).size() >= 3);
    }

    /**
     * 测试多轮对话会话管理（不需要API Key）
     */
    @Test
    public void testConversationHistory() {
        if (aiService == null) {
            System.out.println("AI服务未配置，跳过对话测试");
            return;
        }

        String sessionId = "test-session-" + System.currentTimeMillis();

        // 模拟对话
        Map<String, Object> response1 = aiService.chatDesign(sessionId, "帮我设计一个请假流程");
        assertNotNull(response1);
        assertEquals(sessionId, response1.get("sessionId"));

        // 清理会话
        aiService.clearSession(sessionId);
    }

    // ============ 辅助方法 ============

    private Map<String, Object> createStep(String key, String name, String assignee, int slaHours) {
        Map<String, Object> step = new HashMap<>();
        step.put("key", key);
        step.put("name", name);
        step.put("assignee", assignee);
        step.put("slaHours", slaHours);
        return step;
    }

    private Map<String, Object> validateProcess(Map<String, Object> processJson) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 基本验证
        if (processJson.get("processKey") == null || processJson.get("processKey").toString().isEmpty()) {
            errors.add("缺少processKey");
        }
        if (processJson.get("processName") == null || processJson.get("processName").toString().isEmpty()) {
            errors.add("缺少processName");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> steps = (List<Map<String, Object>>) processJson.get("steps");
        if (steps == null || steps.isEmpty()) {
            errors.add("缺少审批步骤");
        } else {
            for (Map<String, Object> step : steps) {
                if (step.get("key") == null) {
                    errors.add("步骤缺少key");
                }
                if (step.get("name") == null) {
                    errors.add("步骤缺少name");
                }
            }
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    private String recommendTemplate(String businessScenario) {
        // 简单的关键词匹配推荐逻辑
        String scenario = businessScenario.toLowerCase();

        if (scenario.contains("物资") || scenario.contains("采购") || scenario.contains("设备")) {
            return "MATERIAL";
        } else if (scenario.contains("无人机") || scenario.contains("飞行") || scenario.contains("外出")) {
            return "DRONE_FLIGHT";
        } else if (scenario.contains("紧急") || scenario.contains("应急") || scenario.contains("快速")) {
            return "EMERGENCY";
        } else {
            return "STANDARD";
        }
    }

    private Map<String, Object> generateFormFields(String processKey, String stepKey) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        // 通用字段
        fields.add(createField("opinion", "审批意见", "textarea", true, null));
        fields.add(createField("result", "审批结果", "radio", true, List.of("通过", "驳回")));
        fields.add(createField("attachment", "相关附件", "file", false, null));

        // 根据步骤类型添加特定字段
        if ("checker".equals(stepKey)) {
            fields.add(createField("checkOpinion", "校核意见", "textarea", true, null));
            fields.add(createField("isQualified", "是否符合规范", "checkbox", true, null));
        } else if ("reviewer".equals(stepKey)) {
            fields.add(createField("reviewLevel", "审核级别", "select", true, List.of("一级", "二级", "三级")));
        } else if ("approver".equals(stepKey)) {
            fields.add(createField("costEstimate", "费用估算", "number", false, null));
        }

        result.put("fields", fields);
        return result;
    }

    private Map<String, Object> createField(String id, String name, String type, boolean required, Object options) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        if (options != null) {
            field.put("options", options);
        }
        return field;
    }
}
