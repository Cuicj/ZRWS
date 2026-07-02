package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysConfig;
import com.zrws.approval.mapper.SysConfigMapper;
import com.zrws.approval.service.SysConfigService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<List<SysConfig>> getAllConfigs() {
        return R.ok(configService.getAllConfigs());
    }

    /**
     * 获取配置组
     */
    @GetMapping("/group/{group}")
    public R<List<SysConfig>> getByGroup(@PathVariable String group) {
        return R.ok(configService.getByGroup(group));
    }

    /**
     * 获取全局设置
     */
    @GetMapping("/global")
    public R<Map<String, Object>> getGlobalSettings() {
        return R.ok(configService.getGlobalSettings());
    }

    /**
     * 获取单个配置值
     */
    @GetMapping("/value/{key}")
    public R<String> getValue(@PathVariable String key) {
        return R.ok(configService.getValue(key));
    }

    /**
     * 更新配置
     */
    @PutMapping("/{key}")
    public R<Void> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body.get("value");
        boolean result = configService.updateConfig(key, value);
        if (result) {
            return R.ok("更新成功", null);
        } else {
            return R.fail("更新失败");
        }
    }

    /**
     * 保存配置
     */
    @PostMapping
    public R<SysConfig> saveConfig(@RequestBody SysConfig config) {
        try {
            configService.saveConfig(config);
            return R.ok(config);
        } catch (Exception e) {
            log.error("保存配置失败", e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public R<Void> batchUpdate(@RequestBody Map<String, String> configs) {
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                configService.updateConfig(entry.getKey(), entry.getValue());
            }
            return R.ok("批量更新成功", null);
        } catch (Exception e) {
            log.error("批量更新失败", e);
            return R.fail("批量更新失败: " + e.getMessage());
        }
    }
}
