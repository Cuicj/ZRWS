package com.zrws.approval.controller;

import com.zrws.approval.config.MockDataInitializer;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@ConditionalOnProperty(name = "zrws.mock.enabled", havingValue = "true")
public class DataInitController {

    @Autowired
    private MockDataInitializer mockDataInitializer;

    @PostMapping("/init-data")
    public R<Void> initData() {
        try {
            mockDataInitializer.run(null);
            return R.ok();
        } catch (Exception e) {
            log.error("数据初始化失败", e);
            return R.fail(e.getMessage());
        }
    }
}
