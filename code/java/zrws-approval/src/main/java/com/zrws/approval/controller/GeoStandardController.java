package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.GeoStandard;
import com.zrws.approval.mapper.GeoStandardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地质标准数据库 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/geo-standards")
@CrossOrigin(origins = "*")
public class GeoStandardController {

    @Autowired
    private GeoStandardMapper geoStandardMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<GeoStandard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GeoStandard::getStatus, "ACTIVE");
            wrapper.eq(GeoStandard::getIsDeleted, 0);
            if (category != null && !category.isEmpty()) {
                wrapper.eq(GeoStandard::getCategory, category);
            }
            if (system != null && !system.isEmpty()) {
                wrapper.eq(GeoStandard::getClassificationSystem, system);
            }
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(GeoStandard::getStandardName, keyword);
            }
            wrapper.orderByAsc(GeoStandard::getSortOrder);
            List<GeoStandard> list = geoStandardMapper.selectList(wrapper);
            return success(Map.of("list", list, "total", list.size()));
        } catch (Exception e) {
            log.error("查询地质标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return error("标准不存在");
            }
            return success(Map.of("data", standard));
        } catch (Exception e) {
            log.error("查询地质标准详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getByCategory(@PathVariable String category) {
        try {
            List<GeoStandard> list = geoStandardMapper.selectByCategory(category);
            return success(Map.of("list", list, "total", list.size()));
        } catch (Exception e) {
            log.error("按分类查询地质标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        try {
            List<GeoStandard> list = geoStandardMapper.searchByName(keyword);
            return success(Map.of("list", list, "total", list.size()));
        } catch (Exception e) {
            log.error("搜索地质标准失败", e);
            return error("搜索失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody GeoStandard standard) {
        try {
            standard.setStatus("ACTIVE");
            standard.setIsDeleted(0);
            geoStandardMapper.insert(standard);
            return success(Map.of("data", standard, "message", "创建成功"));
        } catch (Exception e) {
            log.error("创建地质标准失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody GeoStandard standard) {
        try {
            standard.setStandardId(id);
            geoStandardMapper.updateById(standard);
            return success(Map.of("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新地质标准失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            geoStandardMapper.deleteById(id);
            return success(Map.of("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除地质标准失败", e);
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
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", message
        ));
    }
}
