package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.ReportTemplate;
import com.zrws.approval.service.ReportGeneratorService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public R<Map<String, Object>> listTemplates(
            @RequestParam(required = false) String category) {
        try {
            List<ReportTemplate> templates = reportGeneratorService.listTemplates(category);
            Map<String, Object> result = new HashMap<>();
            result.put("templates", templates);
            result.put("total", templates.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询报表模板列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{code}")
    public R<ReportTemplate> getTemplateByCode(@PathVariable String code) {
        try {
            ReportTemplate template = reportGeneratorService.getTemplateByCode(code);
            if (template == null) {
                return R.fail("模板不存在: " + code);
            }
            return R.ok(template);
        } catch (Exception e) {
            log.error("查询报表模板详情失败, code={}", code, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public R<Map<String, Object>> generateReport(@RequestBody Map<String, Object> request) {
        String templateCode = (String) request.get("templateCode");
        if (templateCode == null || templateCode.isEmpty()) {
            return R.fail("请提供报表模板编码");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            Map<String, Object> reportData = reportGeneratorService.generateReportData(templateCode, parameters);
            return R.ok(reportData);
        } catch (Exception e) {
            log.error("生成报表失败, templateCode={}", templateCode, e);
            return R.fail("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/templates")
    public R<ReportTemplate> createTemplate(@RequestBody ReportTemplate template) {
        try {
            ReportTemplate created = reportGeneratorService.createTemplate(template);
            return R.ok(created);
        } catch (Exception e) {
            log.error("创建报表模板失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/templates/{id}")
    public R<ReportTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody ReportTemplate template) {
        try {
            template.setTemplateId(id);
            reportGeneratorService.updateTemplate(template);
            return R.ok(template);
        } catch (Exception e) {
            log.error("更新报表模板失败, id={}", id, e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/templates/{id}")
    public R<Void> deleteTemplate(@PathVariable Long id) {
        try {
            reportGeneratorService.deleteTemplate(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除报表模板失败, id={}", id, e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
