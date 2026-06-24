package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableDesignerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/designer")
@CrossOrigin(origins = "*")
public class FlowableDesignerController {

    @Autowired
    private FlowableDesignerService designerService;

    // ============================================================
    // 1. BPMN模型转换
    // ============================================================

    @PostMapping("/xml-to-json")
    public ResponseEntity<Map<String, Object>> xmlToJson(@RequestBody String xml) {
        Map<String, Object> result = designerService.bpmnXmlToJson(xml);
        return success(result);
    }

    @PostMapping("/json-to-xml")
    public ResponseEntity<Map<String, Object>> jsonToXml(@RequestBody Map<String, Object> json) {
        String xml = designerService.jsonToBpmnXml(json);
        Map<String, Object> result = new HashMap<>();
        result.put("xml", xml);
        return success(result);
    }

    @GetMapping("/definition/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> getDefinitionJson(@PathVariable String processDefinitionId) {
        Map<String, Object> result = designerService.getProcessDefinitionJson(processDefinitionId);
        if (result == null) {
            return error("流程定义不存在");
        }
        return success(result);
    }

    // ============================================================
    // 2. 流程生命周期管理（设计-保存-审核-发布-部署）
    // ============================================================

    @PostMapping("/drafts")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody Map<String, Object> json) {
        String processKey = (String) json.get("processKey");
        if (processKey == null) {
            return error("流程Key不能为空");
        }
        Map<String, Object> result = designerService.saveDraft(processKey, json);
        return success(result);
    }

    @GetMapping("/drafts")
    public ResponseEntity<Map<String, Object>> listDrafts() {
        List<Map<String, Object>> drafts = designerService.listDrafts();
        Map<String, Object> result = new HashMap<>();
        result.put("total", drafts.size());
        result.put("list", drafts);
        return success(result);
    }

    @GetMapping("/drafts/{processKey}")
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable String processKey) {
        Map<String, Object> draft = designerService.getDraft(processKey);
        if (draft == null) {
            return error("流程草稿不存在");
        }
        return success(draft);
    }

    @PostMapping("/drafts/{processKey}/submit")
    public ResponseEntity<Map<String, Object>> submitForReview(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.submitForReview(processKey);
            return success(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/review")
    public ResponseEntity<Map<String, Object>> review(@PathVariable String processKey,
                                                        @RequestBody Map<String, Object> body) {
        try {
            boolean approved = Boolean.TRUE.equals(body.get("approved"));
            String comment = (String) body.get("comment");
            Map<String, Object> result = designerService.review(processKey, approved, comment);
            return success(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/publish")
    public ResponseEntity<Map<String, Object>> publish(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.publish(processKey);
            return success(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/deploy")
    public ResponseEntity<Map<String, Object>> deployPublished(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.deployPublished(processKey);
            return success(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    // ============================================================
    // 3. 保存与部署（快速模式）
    // ============================================================

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveAndDeploy(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.saveAndDeploy(json);
        return success(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.validateBpmn(json);
        return success(result);
    }

    // ============================================================
    // 4. 模板管理
    // ============================================================

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = designerService.getTemplates();
        Map<String, Object> result = new HashMap<>();
        result.put("total", templates.size());
        result.put("templates", templates);
        return success(result);
    }

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
