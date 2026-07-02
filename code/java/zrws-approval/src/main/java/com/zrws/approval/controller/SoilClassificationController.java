package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilClassification;
import com.zrws.approval.service.SoilClassificationService;
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
@RequestMapping("/api/v1/soil-classification")
@CrossOrigin(origins = "*")
public class SoilClassificationController {

    @Autowired
    private SoilClassificationService soilClassificationService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) String soilType,
            @RequestParam(required = false) String status) {
        try {
            Page<SoilClassification> page = soilClassificationService.getPage(pageNum, pageSize, missionId, soilType, status);
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return success(result);
        } catch (Exception e) {
            log.error("查询土壤分类列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            SoilClassification record = soilClassificationService.getById(id);
            if (record == null) {
                return error("记录不存在");
            }
            return success(Collections.singletonMap("data", record));
        } catch (Exception e) {
            log.error("查询土壤分类详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, Object> params) {
        try {
            SoilClassification result;
            if (params.containsKey("sampleId")) {
                Long sampleId = Long.valueOf(params.get("sampleId").toString());
                result = soilClassificationService.analyzeBySampleId(sampleId);
            } else {
                result = soilClassificationService.analyzeByParams(params);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("data", result);
            data.put("message", "分析完成");
            return success(data);
        } catch (Exception e) {
            log.error("土壤分类分析失败", e);
            return error("分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam(required = false) Long missionId) {
        try {
            List<SoilClassification> history = soilClassificationService.getHistory(missionId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", history);
            result.put("total", history.size());
            return success(result);
        } catch (Exception e) {
            log.error("查询分析历史失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            soilClassificationService.delete(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除土壤分类记录失败", e);
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
        result.put("msg", message);
        return ResponseEntity.badRequest().body(result);
    }
}