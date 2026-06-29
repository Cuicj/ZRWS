package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.ReportTemplate;
import com.zrws.approval.service.ReportGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/report")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportGeneratorService reportGeneratorService;

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> listTemplates(
            @RequestParam(required = false) String category) {
        try {
            List<ReportTemplate> templates = reportGeneratorService.listTemplates(category);
            Map<String, Object> result = new HashMap<>();
            result.put("templates", templates);
            result.put("total", templates.size());
            return success(result);
        } catch (Exception e) {
            log.error("查询报表模板列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{code}")
    public ResponseEntity<Map<String, Object>> getTemplateByCode(@PathVariable String code) {
        try {
            ReportTemplate template = reportGeneratorService.getTemplateByCode(code);
            if (template == null) {
                return error("模板不存在: " + code);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("template", template);
            return success(result);
        } catch (Exception e) {
            log.error("查询报表模板详情失败, code={}", code, e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody Map<String, Object> request) {
        String templateCode = (String) request.get("templateCode");
        if (templateCode == null || templateCode.isEmpty()) {
            return error("请提供报表模板编码");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            Map<String, Object> reportData = reportGeneratorService.generateReportData(templateCode, parameters);
            return success(reportData);
        } catch (Exception e) {
            log.error("生成报表失败, templateCode={}", templateCode, e);
            return error("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody ReportTemplate template) {
        try {
            ReportTemplate created = reportGeneratorService.createTemplate(template);
            Map<String, Object> result = new HashMap<>();
            result.put("template", created);
            return success(result);
        } catch (Exception e) {
            log.error("创建报表模板失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long id,
            @RequestBody ReportTemplate template) {
        try {
            template.setTemplateId(id);
            reportGeneratorService.updateTemplate(template);
            Map<String, Object> result = new HashMap<>();
            result.put("template", template);
            return success(result);
        } catch (Exception e) {
            log.error("更新报表模板失败, id={}", id, e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        try {
            reportGeneratorService.deleteTemplate(id);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "删除成功");
            return success(result);
        } catch (Exception e) {
            log.error("删除报表模板失败, id={}", id, e);
            return error("删除失败: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            result.putAll(dataMap);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return ResponseEntity.badRequest().body(result);
    }
}
