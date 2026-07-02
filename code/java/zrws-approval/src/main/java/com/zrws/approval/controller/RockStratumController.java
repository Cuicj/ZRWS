package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.RockSample;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import com.zrws.approval.mapper.RockSampleMapper;
import com.zrws.approval.mapper.RockStratumAnalysisMapper;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<Map<String, Object>> listAnalyses(
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
            return R.ok(data);
        } catch (Exception e) {
            log.error("查询岩层分析列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/analyses/{id}")
    public R<RockStratumAnalysis> getAnalysis(@PathVariable Long id) {
        try {
            RockStratumAnalysis analysis = analysisMapper.selectById(id);
            if (analysis == null) {
                return R.fail("分析任务不存在");
            }
            return R.ok(analysis);
        } catch (Exception e) {
            log.error("查询岩层分析详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyses")
    public R<RockStratumAnalysis> createAnalysis(@RequestBody RockStratumAnalysis analysis) {
        try {
            analysis.setStatus("ACTIVE");
            analysis.setIsDeleted(0);
            analysisMapper.insert(analysis);
            return R.ok("创建成功", analysis);
        } catch (Exception e) {
            log.error("创建岩层分析失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/analyses/{id}")
    public R<Void> updateAnalysis(
            @PathVariable Long id,
            @RequestBody RockStratumAnalysis analysis) {
        try {
            analysis.setAnalysisId(id);
            analysisMapper.updateById(analysis);
            return R.ok("更新成功", null);
        } catch (Exception e) {
            log.error("更新岩层分析失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/analyses/{id}")
    public R<Void> deleteAnalysis(@PathVariable Long id) {
        try {
            analysisMapper.deleteById(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除岩层分析失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }

    // ===== 岩矿样品 =====

    @GetMapping("/samples")
    public R<Map<String, Object>> listSamples(
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
            return R.ok(data);
        } catch (Exception e) {
            log.error("查询岩矿样品列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/samples/{id}")
    public R<RockSample> getSample(@PathVariable Long id) {
        try {
            RockSample sample = sampleMapper.selectById(id);
            if (sample == null) {
                return R.fail("样品不存在");
            }
            return R.ok(sample);
        } catch (Exception e) {
            log.error("查询岩矿样品详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/samples")
    public R<RockSample> createSample(@RequestBody RockSample sample) {
        try {
            sample.setStatus("ACTIVE");
            sample.setIsDeleted(0);
            sampleMapper.insert(sample);
            return R.ok("创建成功", sample);
        } catch (Exception e) {
            log.error("创建岩矿样品失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/samples/{id}")
    public R<Void> updateSample(
            @PathVariable Long id,
            @RequestBody RockSample sample) {
        try {
            sample.setSampleId(id);
            sampleMapper.updateById(sample);
            return R.ok("更新成功", null);
        } catch (Exception e) {
            log.error("更新岩矿样品失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/samples/{id}")
    public R<Void> deleteSample(@PathVariable Long id) {
        try {
            sampleMapper.deleteById(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除岩矿样品失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
