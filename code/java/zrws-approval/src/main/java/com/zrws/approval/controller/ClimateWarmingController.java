package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.ClimateWarming;
import com.zrws.approval.service.ClimateWarmingService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            Page<ClimateWarming> page = climateWarmingService.getPage(pageNum, pageSize, region, riskLevel, startDate, endDate);
            Map<String, Object> data = new HashMap<>();
            data.put("list", page.getRecords());
            data.put("total", page.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            return R.ok(data);
        } catch (Exception e) {
            log.error("查询气候变暖数据失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<ClimateWarming> getById(@PathVariable Long id) {
        try {
            ClimateWarming record = climateWarmingService.getById(id);
            if (record == null) {
                return R.fail("记录不存在");
            }
            return R.ok(record);
        } catch (Exception e) {
            log.error("查询气候变暖详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = climateWarmingService.getStats();
            return R.ok(stats);
        } catch (Exception e) {
            log.error("查询气候变暖统计失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/trend")
    public R<Map<String, Object>> getTrend(@RequestParam(required = false) String region) {
        try {
            Map<String, Object> trend = climateWarmingService.getTrendData(region);
            return R.ok(trend);
        } catch (Exception e) {
            log.error("查询气候变暖趋势失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/risk-distribution")
    public R<List<Map<String, Object>>> getRiskDistribution() {
        try {
            List<Map<String, Object>> distribution = climateWarmingService.getRiskDistribution();
            return R.ok(distribution);
        } catch (Exception e) {
            log.error("查询风险分布失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public R<ClimateWarming> create(@RequestBody ClimateWarming climateWarming) {
        try {
            climateWarming.setIsDeleted(0);
            climateWarmingService.add(climateWarming);
            return R.ok("创建成功", climateWarming);
        } catch (Exception e) {
            log.error("创建气候变暖记录失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @RequestBody ClimateWarming climateWarming) {
        try {
            climateWarming.setRecordId(id);
            climateWarmingService.update(climateWarming);
            return R.ok("更新成功");
        } catch (Exception e) {
            log.error("更新气候变暖记录失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        try {
            climateWarmingService.delete(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除气候变暖记录失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/assess")
    public R<ClimateWarming> assess(@PathVariable Long id) {
        try {
            ClimateWarming result = climateWarmingService.assessRisk(id);
            if (result == null) {
                return R.fail("记录不存在");
            }
            return R.ok("评估完成", result);
        } catch (Exception e) {
            log.error("气候变暖风险评估失败", e);
            return R.fail("评估失败: " + e.getMessage());
        }
    }
}
