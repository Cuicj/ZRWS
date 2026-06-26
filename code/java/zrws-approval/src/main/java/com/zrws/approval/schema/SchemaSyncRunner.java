package com.zrws.approval.schema;

import com.zrws.approval.config.SchemaSyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Schema 同步启动监听器
 * Spring Boot 启动完成后自动执行数据库 Schema 校验与同步
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SchemaSyncRunner {

    private final SchemaSyncService schemaSyncService;
    private final SchemaSyncProperties properties;

    /**
     * 监听应用启动完成事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!properties.isAutoSyncOnStartup()) {
            log.info("[DBA] 启动时自动同步已禁用 (zrws.dba.auto-sync-on-startup=false)");
            return;
        }
        log.info("[DBA] 应用启动完成，开始执行 Schema 自动校验...");
        schemaSyncService.sync();
    }
}
