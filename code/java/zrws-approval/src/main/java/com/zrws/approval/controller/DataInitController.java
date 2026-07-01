package com.zrws.approval.controller;

import com.zrws.approval.config.MockDataInitializer;
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
@ConditionalOnProperty(name = "zrws.mock.enabled", havingValue = "true")
public class DataInitController {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @PostMapping("/init-data")
    public ResponseEntity<Map<String, Object>> initData() {
        Map<String, Object> result = new HashMap<>();
        try {
            mockDataInitializer.run(null);
            result.put("success", true);
            result.put("message", "数据初始化完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("数据初始化失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
