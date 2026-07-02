package com.zrws.approval.controller;

import com.zrws.approval.config.MockDataInitializer;
import com.zrws.approval.task.DailyDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "zrws.mock.enabled", havingValue = "true")
public class DataAdminController {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @Autowired
    private DailyDataGenerator dailyDataGenerator;

    @PostMapping("/init-business-data")
    public ResponseEntity<Map<String, Object>> initBusinessData() {
        Map<String, Object> result = new HashMap<>();
        try {
            mockDataInitializer.run(null);
            result.put("success", true);
            result.put("message", "业务数据初始化完成");
            log.info("[数据管理] 手动执行业务数据初始化");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[数据管理] 业务数据初始化失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/generate-daily-data")
    public ResponseEntity<Map<String, Object>> generateDailyData() {
        Map<String, Object> result = new HashMap<>();
        try {
            dailyDataGenerator.generateDailyData();
            result.put("success", true);
            result.put("message", "每日数据生成完成");
            log.info("[数据管理] 手动执行每日数据生成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[数据管理] 每日数据生成失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/generate-all-data")
    public ResponseEntity<Map<String, Object>> generateAllData() {
        Map<String, Object> result = new HashMap<>();
        try {
            mockDataInitializer.run(null);
            dailyDataGenerator.generateDailyData();
            result.put("success", true);
            result.put("message", "全部数据生成完成（初始化 + 每日数据）");
            log.info("[数据管理] 手动执行全部数据生成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[数据管理] 全部数据生成失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
