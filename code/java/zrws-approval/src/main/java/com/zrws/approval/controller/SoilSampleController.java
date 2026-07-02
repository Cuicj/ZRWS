package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.service.SoilSampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/soil-sample")
@CrossOrigin(origins = "*")
public class SoilSampleController {

    @Autowired
    private SoilSampleService soilSampleService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) String soilType,
            @RequestParam(required = false) String status) {
        try {
            Page<SoilSample> page = soilSampleService.getPage(pageNum, pageSize, missionId, soilType, status);
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return success(result);
        } catch (Exception e) {
            log.error("查询土壤采样列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            SoilSample record = soilSampleService.getById(id);
            if (record == null) {
                return error("记录不存在");
            }
            return success(Collections.singletonMap("data", record));
        } catch (Exception e) {
            log.error("查询土壤采样详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody SoilSample soilSample) {
        try {
            soilSample.setIsDeleted(0);
            soilSampleService.add(soilSample);
            Map<String, Object> result = new HashMap<>();
            result.put("data", soilSample);
            result.put("message", "创建成功");
            return success(result);
        } catch (Exception e) {
            log.error("创建土壤采样记录失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody SoilSample soilSample) {
        try {
            soilSample.setSampleId(id);
            soilSampleService.update(soilSample);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新土壤采样记录失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            soilSampleService.delete(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除土壤采样记录失败", e);
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