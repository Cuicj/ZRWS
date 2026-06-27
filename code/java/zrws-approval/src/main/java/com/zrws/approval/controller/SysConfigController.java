package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysConfig;
import com.zrws.approval.mapper.SysConfigMapper;
import com.zrws.approval.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", configService.getAllConfigs());
        return success(result);
    }

    /**
     * 获取配置组
     */
    @GetMapping("/group/{group}")
    public ResponseEntity<Map<String, Object>> getByGroup(@PathVariable String group) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", configService.getByGroup(group));
        return success(result);
    }

    /**
     * 获取全局设置
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobalSettings() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("settings", configService.getGlobalSettings());
        return success(result);
    }

    /**
     * 获取单个配置值
     */
    @GetMapping("/value/{key}")
    public ResponseEntity<Map<String, Object>> getValue(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("value", configService.getValue(key));
        return success(result);
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
            return success(Collections.singletonMap("message", "更新成功"));
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
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("config", config);
            return success(result);
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
            return success(Collections.singletonMap("message", "批量更新成功"));
        } catch (Exception e) {
            log.error("批量更新失败", e);
            return error("批量更新失败: " + e.getMessage());
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