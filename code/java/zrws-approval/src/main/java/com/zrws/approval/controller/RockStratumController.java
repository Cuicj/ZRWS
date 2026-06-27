package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.RockSample;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import com.zrws.approval.mapper.RockSampleMapper;
import com.zrws.approval.mapper.RockStratumAnalysisMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 岩层结构分析 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rock-stratum")
@CrossOrigin(origins = "*")
public class RockStratumController {

    @Autowired
    private RockStratumAnalysisMapper analysisMapper;

    @Autowired
    private RockSampleMapper sampleMapper;

    // ===== 分析任务 =====

    @GetMapping("/analyses")
    public ResponseEntity<Map<String, Object>> listAnalyses(
            @RequestParam(required = false) Long missionId) {
        try {
            List<RockStratumAnalysis> list;
            if (missionId != null) {
                list = analysisMapper.selectByMissionId(missionId);
            } else {
                list = analysisMapper.selectAll();
            }
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", list.size());
            return success(data);
        } catch (Exception e) {
            log.error("查询岩层分析列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<Map<String, Object>> getAnalysis(@PathVariable Long id) {
        try {
            RockStratumAnalysis analysis = analysisMapper.selectById(id);
            if (analysis == null) {
                return error("分析任务不存在");
            }
            return success(Collections.singletonMap("data", analysis));
        } catch (Exception e) {
            log.error("查询岩层分析详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyses")
    public ResponseEntity<Map<String, Object>> createAnalysis(@RequestBody RockStratumAnalysis analysis) {
        try {
            analysis.setStatus("ACTIVE");
            analysis.setIsDeleted(0);
            analysisMapper.insert(analysis);
            Map<String, Object> data = new HashMap<>();
            data.put("data", analysis);
            data.put("message", "创建成功");
            return success(data);
        } catch (Exception e) {
            log.error("创建岩层分析失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/analyses/{id}")
    public ResponseEntity<Map<String, Object>> updateAnalysis(
            @PathVariable Long id,
            @RequestBody RockStratumAnalysis analysis) {
        try {
            analysis.setAnalysisId(id);
            analysisMapper.updateById(analysis);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新岩层分析失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/analyses/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnalysis(@PathVariable Long id) {
        try {
            analysisMapper.deleteById(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除岩层分析失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    // ===== 岩矿样品 =====

    @GetMapping("/samples")
    public ResponseEntity<Map<String, Object>> listSamples(
            @RequestParam(required = false) Long analysisId,
            @RequestParam(required = false) Long missionId) {
        try {
            List<RockSample> list;
            if (analysisId != null) {
                list = sampleMapper.selectByAnalysisId(analysisId);
            } else if (missionId != null) {
                list = sampleMapper.selectByMissionId(missionId);
            } else {
                list = sampleMapper.selectList(new LambdaQueryWrapper<RockSample>()
                        .eq(RockSample::getIsDeleted, 0)
                        .orderByDesc(RockSample::getCreatedTime));
            }
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", list.size());
            return success(data);
        } catch (Exception e) {
            log.error("查询岩矿样品列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/samples/{id}")
    public ResponseEntity<Map<String, Object>> getSample(@PathVariable Long id) {
        try {
            RockSample sample = sampleMapper.selectById(id);
            if (sample == null) {
                return error("样品不存在");
            }
            return success(Collections.singletonMap("data", sample));
        } catch (Exception e) {
            log.error("查询岩矿样品详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/samples")
    public ResponseEntity<Map<String, Object>> createSample(@RequestBody RockSample sample) {
        try {
            sample.setStatus("ACTIVE");
            sample.setIsDeleted(0);
            sampleMapper.insert(sample);
            Map<String, Object> data = new HashMap<>();
            data.put("data", sample);
            data.put("message", "创建成功");
            return success(data);
        } catch (Exception e) {
            log.error("创建岩矿样品失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/samples/{id}")
    public ResponseEntity<Map<String, Object>> updateSample(
            @PathVariable Long id,
            @RequestBody RockSample sample) {
        try {
            sample.setSampleId(id);
            sampleMapper.updateById(sample);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新岩矿样品失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/samples/{id}")
    public ResponseEntity<Map<String, Object>> deleteSample(@PathVariable Long id) {
        try {
            sampleMapper.deleteById(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除岩矿样品失败", e);
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
