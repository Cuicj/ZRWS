package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilClassification;
import com.zrws.approval.service.SoilClassificationService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<Map<String, Object>> list(
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
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询土壤分类列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<SoilClassification> getById(@PathVariable Long id) {
        try {
            SoilClassification record = soilClassificationService.getById(id);
            if (record == null) {
                return R.fail("记录不存在");
            }
            return R.ok(record);
        } catch (Exception e) {
            log.error("查询土壤分类详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    public R<SoilClassification> analyze(@RequestBody Map<String, Object> params) {
        try {
            SoilClassification result;
            if (params.containsKey("sampleId")) {
                Long sampleId = Long.valueOf(params.get("sampleId").toString());
                result = soilClassificationService.analyzeBySampleId(sampleId);
            } else {
                result = soilClassificationService.analyzeByParams(params);
            }
            return R.ok("分析完成", result);
        } catch (Exception e) {
            log.error("土壤分类分析失败", e);
            return R.fail("分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public R<Map<String, Object>> getHistory(
            @RequestParam(required = false) Long missionId) {
        try {
            List<SoilClassification> history = soilClassificationService.getHistory(missionId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", history);
            result.put("total", history.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询分析历史失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            soilClassificationService.delete(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除土壤分类记录失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
