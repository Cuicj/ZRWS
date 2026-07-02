package com.zrws.approval.controller;

import com.zrws.approval.config.MockDataInitializer;
import com.zrws.approval.task.DailyDataGenerator;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

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
    public R<Void> initBusinessData() {
        try {
            mockDataInitializer.run(null);
            log.info("[数据管理] 手动执行业务数据初始化");
            return R.ok();
        } catch (Exception e) {
            log.error("[数据管理] 业务数据初始化失败", e);
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/generate-daily-data")
    public R<Void> generateDailyData() {
        try {
            dailyDataGenerator.generateDailyData();
            log.info("[数据管理] 手动执行每日数据生成");
            return R.ok();
        } catch (Exception e) {
            log.error("[数据管理] 每日数据生成失败", e);
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/generate-all-data")
    public R<Void> generateAllData() {
        try {
            mockDataInitializer.run(null);
            dailyDataGenerator.generateDailyData();
            log.info("[数据管理] 手动执行全部数据生成");
            return R.ok();
        } catch (Exception e) {
            log.error("[数据管理] 全部数据生成失败", e);
            return R.fail(e.getMessage());
        }
    }
}
