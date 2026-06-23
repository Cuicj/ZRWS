package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysConfig;
import com.zrws.approval.mapper.SysConfigMapper;
import com.zrws.approval.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局配置 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sysconfig")
@CrossOrigin(origins = "*")
public class SysConfigController {

    @Autowired
    private SysConfigService configService;

    @Autowired
    private SysConfigMapper configMapper;

    /**
     * 获取所有配置
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        return success(Map.of(
                "success", true,
                "list", configService.getAllConfigs()
        ));
    }

    /**
     * 获取配置组
     */
    @GetMapping("/group/{group}")
    public ResponseEntity<Map<String, Object>> getByGroup(@PathVariable String group) {
        return success(Map.of(
                "success", true,
                "list", configService.getByGroup(group)
        ));
    }

    /**
     * 获取全局设置
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobalSettings() {
        return success(Map.of(
                "success", true,
                "settings", configService.getGlobalSettings()
        ));
    }

    /**
     * 获取单个配置值
     */
    @GetMapping("/value/{key}")
    public ResponseEntity<Map<String, Object>> getValue(@PathVariable String key) {
        return success(Map.of(
                "success", true,
                "value", configService.getValue(key)
        ));
    }

    /**
     * 更新配置
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body.get("value");
        boolean result = configService.updateConfig(key, value);
        if (result) {
            return success(Map.of("success", true, "message", "更新成功"));
        } else {
            return error("更新失败");
        }
    }

    /**
     * 保存配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody SysConfig config) {
        try {
            configService.saveConfig(config);
            return success(Map.of("success", true, "config", config));
        } catch (Exception e) {
            log.error("保存配置失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchUpdate(@RequestBody Map<String, String> configs) {
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                configService.updateConfig(entry.getKey(), entry.getValue());
            }
            return success(Map.of("success", true, "message", "批量更新成功"));
        } catch (Exception e) {
            log.error("批量更新失败", e);
            return error("批量更新失败: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<?, ?>) data);
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