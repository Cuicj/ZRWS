package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.service.SoilSampleService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<Map<String, Object>> list(
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
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询土壤采样列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<SoilSample> getById(@PathVariable Long id) {
        try {
            SoilSample record = soilSampleService.getById(id);
            if (record == null) {
                return R.fail("记录不存在");
            }
            return R.ok(record);
        } catch (Exception e) {
            log.error("查询土壤采样详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public R<SoilSample> create(@RequestBody SoilSample soilSample) {
        try {
            soilSample.setIsDeleted(0);
            soilSampleService.add(soilSample);
            return R.ok("创建成功", soilSample);
        } catch (Exception e) {
            log.error("创建土壤采样记录失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SoilSample soilSample) {
        try {
            soilSample.setSampleId(id);
            soilSampleService.update(soilSample);
            return R.ok("更新成功", null);
        } catch (Exception e) {
            log.error("更新土壤采样记录失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            soilSampleService.delete(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除土壤采样记录失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
