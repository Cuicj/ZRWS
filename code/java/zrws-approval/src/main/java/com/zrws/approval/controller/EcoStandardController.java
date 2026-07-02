package com.zrws.approval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.EcoStandard;
import com.zrws.approval.service.EcoStandardService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/eco-standard")
@CrossOrigin(origins = "*")
public class EcoStandardController {

    @Autowired
    private EcoStandardService ecoStandardService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String keyword) {
        try {
            Page<EcoStandard> page = ecoStandardService.getPage(pageNum, pageSize, category, subcategory, gradeLevel, keyword);
            Map<String, Object> data = new HashMap<>();
            data.put("list", page.getRecords());
            data.put("total", page.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            return R.ok(data);
        } catch (Exception e) {
            log.error("查询生态标准失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<EcoStandard> getById(@PathVariable Long id) {
        try {
            EcoStandard standard = ecoStandardService.getById(id);
            if (standard == null) {
                return R.fail("标准不存在");
            }
            return R.ok(standard);
        } catch (Exception e) {
            log.error("查询生态标准详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public R<Map<String, Object>> getByCategory(@PathVariable String category) {
        try {
            List<EcoStandard> list = ecoStandardService.getByCategory(category);
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", list.size());
            return R.ok(data);
        } catch (Exception e) {
            log.error("按分类查询生态标准失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}/grade/{gradeLevel}")
    public R<Map<String, Object>> getByCategoryAndGrade(
            @PathVariable String category,
            @PathVariable String gradeLevel) {
        try {
            List<EcoStandard> list = ecoStandardService.getByCategoryAndGrade(category, gradeLevel);
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", list.size());
            return R.ok(data);
        } catch (Exception e) {
            log.error("按等级查询生态标准失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public R<EcoStandard> create(@RequestBody EcoStandard standard) {
        try {
            standard.setStatus("ACTIVE");
            standard.setIsDeleted(0);
            ecoStandardService.add(standard);
            return R.ok("创建成功", standard);
        } catch (Exception e) {
            log.error("创建生态标准失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @RequestBody EcoStandard standard) {
        try {
            standard.setStandardId(id);
            ecoStandardService.update(standard);
            return R.ok("更新成功");
        } catch (Exception e) {
            log.error("更新生态标准失败", e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        try {
            ecoStandardService.delete(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除生态标准失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
