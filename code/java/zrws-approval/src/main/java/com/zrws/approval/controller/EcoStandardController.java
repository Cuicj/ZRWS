package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.EcoStandard;
import com.zrws.approval.service.EcoStandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/eco-standard")
@CrossOrigin(origins = "*")
public class EcoStandardController {

    @Autowired
    private EcoStandardService ecoStandardService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String keyword) {
        try {
            Page<EcoStandard> page = ecoStandardService.getPage(pageNum, pageSize, category, subcategory, gradeLevel, keyword);
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return success(result);
        } catch (Exception e) {
            log.error("查询生态标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            EcoStandard standard = ecoStandardService.getById(id);
            if (standard == null) {
                return error("标准不存在");
            }
            return success(Collections.singletonMap("data", standard));
        } catch (Exception e) {
            log.error("查询生态标准详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getByCategory(@PathVariable String category) {
        try {
            List<EcoStandard> list = ecoStandardService.getByCategory(category);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", list.size());
            return success(result);
        } catch (Exception e) {
            log.error("按分类查询生态标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}/grade/{gradeLevel}")
    public ResponseEntity<Map<String, Object>> getByCategoryAndGrade(
            @PathVariable String category,
            @PathVariable String gradeLevel) {
        try {
            List<EcoStandard> list = ecoStandardService.getByCategoryAndGrade(category, gradeLevel);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", list.size());
            return success(result);
        } catch (Exception e) {
            log.error("按等级查询生态标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody EcoStandard standard) {
        try {
            standard.setStatus("ACTIVE");
            standard.setIsDeleted(0);
            ecoStandardService.add(standard);
            Map<String, Object> result = new HashMap<>();
            result.put("data", standard);
            result.put("message", "创建成功");
            return success(result);
        } catch (Exception e) {
            log.error("创建生态标准失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody EcoStandard standard) {
        try {
            standard.setStandardId(id);
            ecoStandardService.update(standard);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新生态标准失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            ecoStandardService.delete(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除生态标准失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<String, Object>) data);
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
