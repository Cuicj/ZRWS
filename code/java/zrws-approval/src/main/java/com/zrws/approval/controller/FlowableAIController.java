package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spring AI 智能流程生成 REST API
 * <p>用户通过自然语言描述流程需求，AI自动生成并部署流程
 */
@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
public class FlowableAIController {

    @Autowired
    private FlowableAIService aiService;

    // ============================================================
    // 1. AI生成流程
    // ============================================================

    /**
     * AI生成流程定义（不部署）
     * POST /api/v1/ai/generate
     * Body: {"description": "我需要一个请假审批流程，员工提交后经理审批，超过3天需要总监审批"}
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateProcess(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isEmpty()) {
            return error("请提供流程描述");
        }

        try {
            Map<String, Object> result = aiService.generateProcessFromDescription(description);
            return success(result);
        } catch (Exception e) {
            return error("AI生成失败: " + e.getMessage());
        }
    }

    /**
     * AI生成并部署流程
     * POST /api/v1/ai/generate-deploy
     * Body: {"description": "我需要一个请假审批流程..."}
     */
    @PostMapping("/generate-deploy")
    public ResponseEntity<Map<String, Object>> generateAndDeploy(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isEmpty()) {
            return error("请提供流程描述");
        }

        try {
            Map<String, Object> result = aiService.generateAndDeploy(description);
            return success(result);
        } catch (Exception e) {
            return error("AI生成部署失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 2. AI优化流程
    // ============================================================

    /**
     * AI优化现有流程
     * POST /api/v1/ai/optimize/{processDefinitionId}
     * Body: {"request": "请添加并行审批节点，缩短审批时间"}
     */
    @PostMapping("/optimize/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> optimizeProcess(
            @PathVariable String processDefinitionId,
            @RequestBody Map<String, String> body) {

        String optimizationRequest = body.get("request");
        if (optimizationRequest == null || optimizationRequest.isEmpty()) {
            return error("请提供优化需求");
        }

        try {
            Map<String, Object> result = aiService.optimizeProcess(processDefinitionId, optimizationRequest);
            return success(result);
        } catch (Exception e) {
            return error("AI优化失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 3. AI分析流程
    // ============================================================

    /**
     * AI分析流程并提出改进建议
     * GET /api/v1/ai/analyze/{processDefinitionId}
     */
    @GetMapping("/analyze/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> analyzeProcess(@PathVariable String processDefinitionId) {
        try {
            Map<String, Object> result = aiService.analyzeProcess(processDefinitionId);
            return success(result);
        } catch (Exception e) {
            return error("AI分析失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 4. AI推荐模板
    // ============================================================

    /**
     * 根据业务场景推荐流程模板
     * POST /api/v1/ai/recommend
     * Body: {"scenario": "员工请假申请，需要经理和总监审批"}
     */
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> recommendTemplate(@RequestBody Map<String, String> body) {
        String scenario = body.get("scenario");
        if (scenario == null || scenario.isEmpty()) {
            return error("请提供业务场景描述");
        }

        try {
            Map<String, Object> result = aiService.recommendTemplate(scenario);
            return success(result);
        } catch (Exception e) {
            return error("AI推荐失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 5. AI对话式流程设计
    // ============================================================

    /**
     * AI对话式流程设计（多轮交互）
     * POST /api/v1/ai/chat
     * Body: {"sessionId": "xxx", "message": "我想创建一个采购审批流程"}
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatDesign(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String sessionId = body.get("sessionId");

        if (message == null || message.isEmpty()) {
            return error("请输入消息");
        }

        // 如果没有sessionId，创建新的
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            Map<String, Object> result = aiService.chatDesign(sessionId, message);
            return success(result);
        } catch (Exception e) {
            return error("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 清除对话会话
     * DELETE /api/v1/ai/chat/{sessionId}
     */
    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        aiService.clearSession(sessionId);
        return successMsg("会话已清除");
    }

    // ============================================================
    // 6. 智能表单生成
    // ============================================================

    /**
     * AI生成表单字段
     * GET /api/v1/ai/form/{processKey}/{stepKey}
     */
    @GetMapping("/form/{processKey}/{stepKey}")
    public ResponseEntity<Map<String, Object>> generateForm(
            @PathVariable String processKey,
            @PathVariable String stepKey) {
        try {
            Map<String, Object> result = aiService.generateFormFields(processKey, stepKey);
            return success(result);
        } catch (Exception e) {
            return error("表单生成失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("msg", "success");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", -1);
        body.put("msg", msg);
        return ResponseEntity.ok(body);
    }
}