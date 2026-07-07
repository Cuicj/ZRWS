package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.DisasterRisk;
import com.zrws.approval.mapper.DisasterRiskMapper;
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
@RequestMapping("/api/v1/disaster-risk")
@CrossOrigin(origins = "*")
public class DisasterRiskController {

    @Autowired
    private DisasterRiskMapper disasterRiskMapper;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LambdaQueryWrapper<DisasterRisk> wrapper = new LambdaQueryWrapper<DisasterRisk>()
                    .eq(DisasterRisk::getIsDeleted, 0)
                    .orderByDesc(DisasterRisk::getAssessmentTime);
            if (region != null && !region.isEmpty()) {
                wrapper.like(DisasterRisk::getRegion, region);
            }
            if (disasterType != null && !disasterType.isEmpty()) {
                wrapper.eq(DisasterRisk::getDisasterType, disasterType);
            }
            if (riskLevel != null && !riskLevel.isEmpty()) {
                wrapper.eq(DisasterRisk::getRiskLevel, riskLevel);
            }
            if (startDate != null) {
                wrapper.ge(DisasterRisk::getAssessmentTime, startDate.atStartOfDay());
            }
            if (endDate != null) {
                wrapper.le(DisasterRisk::getAssessmentTime, endDate.plusDays(1).atStartOfDay());
            }
            Page<DisasterRisk> page = new Page<>(pageNum, pageSize);
            Page<DisasterRisk> result = disasterRiskMapper.selectPage(page, wrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            return R.ok(data);
        } catch (Exception e) {
            log.error("查询灾害风险列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<DisasterRisk> getById(@PathVariable Long id) {
        try {
            DisasterRisk record = disasterRiskMapper.selectById(id);
            if (record == null) {
                return R.fail("记录不存在");
            }
            return R.ok(record);
        } catch (Exception e) {
            log.error("查询灾害风险详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            Long total = disasterRiskMapper.selectCount(
                    new LambdaQueryWrapper<DisasterRisk>().eq(DisasterRisk::getIsDeleted, 0)
            );
            stats.put("totalRecords", total);

            Long highRisk = disasterRiskMapper.selectCount(
                    new LambdaQueryWrapper<DisasterRisk>()
                            .eq(DisasterRisk::getIsDeleted, 0)
                            .in(DisasterRisk::getRiskLevel, "HIGH", "EXTREME")
            );
            stats.put("highRiskCount", highRisk);

            Long soilErosionCount = disasterRiskMapper.selectCount(
                    new LambdaQueryWrapper<DisasterRisk>()
                            .eq(DisasterRisk::getIsDeleted, 0)
                            .eq(DisasterRisk::getDisasterType, "SOIL_EROSION")
            );
            stats.put("soilErosionCount", soilErosionCount);

            List<DisasterRisk> allRisks = disasterRiskMapper.selectList(
                    new LambdaQueryWrapper<DisasterRisk>()
                            .eq(DisasterRisk::getIsDeleted, 0)
                            .orderByDesc(DisasterRisk::getAssessmentTime)
            );

            double avgRiskScore = allRisks.stream()
                    .mapToDouble(r -> r.getRiskScore() != null ? r.getRiskScore() : 0)
                    .average()
                    .orElse(0);
            stats.put("avgRiskScore", Math.round(avgRiskScore * 10) / 10.0);

            Map<String, Integer> typeCount = new HashMap<>();
            for (DisasterRisk r : allRisks) {
                String type = r.getDisasterType();
                if (type != null) {
                    typeCount.merge(type, 1, Integer::sum);
                }
            }
            stats.put("typeDistribution", typeCount);

            return R.ok(stats);
        } catch (Exception e) {
            log.error("查询灾害风险统计失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
