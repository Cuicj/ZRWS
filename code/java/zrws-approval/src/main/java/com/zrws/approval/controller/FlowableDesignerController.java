package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableDesignerService;
import com.zrws.common.core.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<Map<String, Object>> xmlToJson(@RequestBody String xml) {
        Map<String, Object> result = designerService.bpmnXmlToJson(xml);
        return R.ok(result);
    }

    @PostMapping("/json-to-xml")
    public R<Map<String, Object>> jsonToXml(@RequestBody Map<String, Object> json) {
        String xml = designerService.jsonToBpmnXml(json);
        return R.ok(Map.of("xml", xml));
    }

    @GetMapping("/definition/{processDefinitionId}")
    public R<Map<String, Object>> getDefinitionJson(@PathVariable String processDefinitionId) {
        Map<String, Object> result = designerService.getProcessDefinitionJson(processDefinitionId);
        if (result == null) {
            return R.fail("流程定义不存在");
        }
        return R.ok(result);
    }

    // ============================================================
    // 2. 流程生命周期管理（设计-保存-审核-发布-部署）
    // ============================================================

    @PostMapping("/drafts")
    public R<Map<String, Object>> saveDraft(@RequestBody Map<String, Object> json) {
        String processKey = (String) json.get("processKey");
        if (processKey == null) {
            return R.fail("流程Key不能为空");
        }
        Map<String, Object> result = designerService.saveDraft(processKey, json);
        return R.ok(result);
    }

    @GetMapping("/drafts")
    public R<Map<String, Object>> listDrafts() {
        List<Map<String, Object>> drafts = designerService.listDrafts();
        return R.ok(Map.of("total", drafts.size(), "list", drafts));
    }

    @GetMapping("/drafts/{processKey}")
    public R<Map<String, Object>> getDraft(@PathVariable String processKey) {
        Map<String, Object> draft = designerService.getDraft(processKey);
        if (draft == null) {
            return R.fail("流程草稿不存在");
        }
        return R.ok(draft);
    }

    @PostMapping("/drafts/{processKey}/submit")
    public R<Map<String, Object>> submitForReview(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.submitForReview(processKey);
            return R.ok(result);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/review")
    public R<Map<String, Object>> review(@PathVariable String processKey,
                                                        @RequestBody Map<String, Object> body) {
        try {
            boolean approved = Boolean.TRUE.equals(body.get("approved"));
            String comment = (String) body.get("comment");
            Map<String, Object> result = designerService.review(processKey, approved, comment);
            return R.ok(result);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/publish")
    public R<Map<String, Object>> publish(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.publish(processKey);
            return R.ok(result);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/drafts/{processKey}/deploy")
    public R<Map<String, Object>> deployPublished(@PathVariable String processKey) {
        try {
            Map<String, Object> result = designerService.deployPublished(processKey);
            return R.ok(result);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    // ============================================================
    // 3. 保存与部署（快速模式）
    // ============================================================

    @PostMapping("/save")
    public R<Map<String, Object>> saveAndDeploy(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.saveAndDeploy(json);
        return R.ok(result);
    }

    @PostMapping("/validate")
    public R<Map<String, Object>> validate(@RequestBody Map<String, Object> json) {
        Map<String, Object> result = designerService.validateBpmn(json);
        return R.ok(result);
    }

    // ============================================================
    // 4. 模板管理
    // ============================================================

    @GetMapping("/templates")
    public R<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = designerService.getTemplates();
        return R.ok(Map.of("total", templates.size(), "templates", templates));
    }

    @PostMapping("/template/{templateKey}/create")
    public R<Map<String, Object>> createFromTemplate(@PathVariable String templateKey) {
        try {
            Map<String, Object> result = designerService.createFromTemplate(templateKey);
            return R.ok(result);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }
}
