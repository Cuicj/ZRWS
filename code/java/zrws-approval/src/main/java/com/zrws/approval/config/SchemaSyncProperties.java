package com.zrws.approval.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DBA Schema 自动同步配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "zrws.dba")
public class SchemaSyncProperties {

    /**
     * 是否启用自动同步
     */
    private boolean enabled = true;

    /**
     * 启动时是否自动执行同步
     */
    private boolean autoSyncOnStartup = true;

    /**
     * 实体类扫描基础包
     */
    private String entityPackage = "com.zrws.approval.domain.entity";

    /**
     * Mapper XML 扫描路径
     */
    private String mapperLocations = "classpath*:/mapper/**/*.xml";

    /**
     * 初始化 SQL 脚本目录
     */
    private String sqlInitLocation = "classpath*:/sql/*.sql";

    /**
     * 需要排除的表前缀（Flowable 框架表等）
     */
    private String[] excludeTablePrefixes = {"ACT_", "act_", "FLW_", "flw_"};

    /**
     * 是否打印详细日志
     */
    private boolean verbose = true;

    /**
     * 同步失败时是否中断启动
     */
    private boolean failOnSyncError = false;
}
