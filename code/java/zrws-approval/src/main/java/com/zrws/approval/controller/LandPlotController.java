package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.LandPlot;
import com.zrws.approval.service.LandPlotService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/land-plot")
@CrossOrigin(origins = "*")
public class LandPlotController {

    @Autowired
    private LandPlotService landPlotService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String landType,
            @RequestParam(required = false) String status) {
        try {
            Page<LandPlot> page = landPlotService.getPage(pageNum, pageSize, region, landType, status);
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询地块列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<LandPlot> getById(@PathVariable Long id) {
        try {
            LandPlot record = landPlotService.getById(id);
            if (record == null) {
                return R.fail("记录不存在");
            }
            return R.ok(record);
        } catch (Exception e) {
            log.error("查询地块详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/area-stats")
    public R<Map<String, Object>> getAreaStats() {
        try {
            Map<String, Object> stats = landPlotService.getAreaStats();
            return R.ok(stats);
        } catch (Exception e) {
            log.error("查询面积统计失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public R<LandPlot> create(@RequestBody LandPlot landPlot) {
        try {
            landPlot.setIsDeleted(0);
            landPlotService.add(landPlot);
            return R.ok("创建成功", landPlot);
        } catch (Exception e) {
            log.error("创建地块失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody LandPlot landPlot) {
        try {
            landPlot.setPlotId(id);
            landPlotService.update(landPlot);
            return R.ok("更新成功", null);
        } catch (Exception e) {
            log.error("更新地块失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            landPlotService.delete(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除地块失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
