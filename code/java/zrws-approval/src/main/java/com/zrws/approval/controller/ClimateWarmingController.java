package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.ClimateWarming;
import com.zrws.approval.service.ClimateWarmingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/climate-warming")
@CrossOrigin(origins = "*")
public class ClimateWarmingController {

    @Autowired
    private ClimateWarmingService climateWarmingService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Page<ClimateWarming> page = climateWarmingService.getPage(pageNum, pageSize, region, riskLevel, startDate, endDate);
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return success(result);
        } catch (Exception e) {
            log.error("查询气候变暖数据失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            ClimateWarming record = climateWarmingService.getById(id);
            if (record == null) {
                return error("记录不存在");
            }
            return success(Collections.singletonMap("data", record));
        } catch (Exception e) {
            log.error("查询气候变暖详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = climateWarmingService.getStats();
            return success(Collections.singletonMap("data", stats));
        } catch (Exception e) {
            log.error("查询气候变暖统计失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getTrend(@RequestParam(required = false) String region) {
        try {
            Map<String, Object> trend = climateWarmingService.getTrendData(region);
            return success(Collections.singletonMap("data", trend));
        } catch (Exception e) {
            log.error("查询气候变暖趋势失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/risk-distribution")
    public ResponseEntity<Map<String, Object>> getRiskDistribution() {
        try {
            List<Map<String, Object>> distribution = climateWarmingService.getRiskDistribution();
            Map<String, Object> result = new HashMap<>();
            result.put("list", distribution);
            return success(result);
        } catch (Exception e) {
            log.error("查询风险分布失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ClimateWarming climateWarming) {
        try {
            climateWarming.setIsDeleted(0);
            climateWarmingService.add(climateWarming);
            Map<String, Object> result = new HashMap<>();
            result.put("data", climateWarming);
            result.put("message", "创建成功");
            return success(result);
        } catch (Exception e) {
            log.error("创建气候变暖记录失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ClimateWarming climateWarming) {
        try {
            climateWarming.setRecordId(id);
            climateWarmingService.update(climateWarming);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新气候变暖记录失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            climateWarmingService.delete(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除气候变暖记录失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/assess")
    public ResponseEntity<Map<String, Object>> assess(@PathVariable Long id) {
        try {
            ClimateWarming result = climateWarmingService.assessRisk(id);
            if (result == null) {
                return error("记录不存在");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("data", result);
            data.put("message", "评估完成");
            return success(data);
        } catch (Exception e) {
            log.error("气候变暖风险评估失败", e);
            return error("评估失败: " + e.getMessage());
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
