package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableDesignerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程设计器 REST API 控制器
 * <p>提供BPMN模型与JSON的相互转换、模板管理、设计验证等接口
 */
@RestController
@RequestMapping("/api/v1/designer")
@CrossOrigin(origins = "*")
public class FlowableDesignerController {

    @Autowired
    private FlowableDesignerService designerService;

    // ============================================================
    // 1. BPMN模型转换
    // ============================================================

    /**
     * BPMN XML转JSON
     * POST /api/v1/designer/xml-to-json
     */
    @PostMapping("/xml-to-json")
    public ResponseEntity<Map<String, Object>> xmlToJson(@RequestBody String xml) {
        Map<String, Object> result = designerService.bpmnXmlToJson(xml);
        return success(result);
    }

    /**
     * JSON转BPMN XML
     * POST /api/v1/designer/json-to-xml
     */
    @PostMapping("/json-to-xml")
    public ResponseEntity<Map<String, Object>> jsonToXml(@RequestBody Map<String, Object> json) {
        String xml = designerService.jsonToBpmnXml(json);
        Map<String, Object> result = new HashMap<>();
        result.put("xml", xml);
        return success(result);
    }

    /**
     * 获取流程定义的JSON格式
     * GET /api/v1/designer/definition/{processDefinitionId}
     */
    @GetMapping("/definition/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> getDefinitionJson(@PathVariable String processDefinitionId) {
        Map<String, Object> result = designerService.getProcessDefinitionJson(processDefinitionId);
        if (result == null) {
            return error("流程定义不存在");
        }
        return success(result);
    }

    // ============================================================
    // 2. 保存与部署
    // ============================================================

    /**
     * 保存流程设计并部署
     * POST /api/v1/designer/save
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveAndDeploy(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.saveAndDeploy(json);
        return success(result);
    }

    /**
     * 验证BPMN模型
     * POST /api/v1/designer/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.validateBpmn(json);
        return success(result);
    }

    // ============================================================
    // 3. 模板管理
    // ============================================================

    /**
     * 获取流程模板列表
     * GET /api/v1/designer/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = designerService.getTemplates();
        Map<String, Object> result = new HashMap<>();
        result.put("total", templates.size());
        result.put("templates", templates);
        return success(result);
    }

    /**
     * 根据模板创建流程定义
     * POST /api/v1/designer/template/{templateKey}/create
     */
    @PostMapping("/template/{templateKey}/create")
    public ResponseEntity<Map<String, Object>> createFromTemplate(@PathVariable String templateKey) {
        try {
            Map<String, Object> result = designerService.createFromTemplate(templateKey);
            return success(result);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
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
