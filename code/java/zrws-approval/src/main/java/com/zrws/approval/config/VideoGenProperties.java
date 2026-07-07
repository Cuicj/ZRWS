package com.zrws.approval.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AI视频生成配置
 * 支持多家视频生成API提供商
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zrws.video-gen")
public class VideoGenProperties {

    private boolean enabled = true;

    private String defaultProvider = "KUAISHOU_KELING";

    private String storagePath = "uploads/videos";

    private int maxConcurrentTasks = 3;

    private int maxRetryCount = 2;

    private int pollIntervalSeconds = 10;

    private int taskTimeoutMinutes = 30;

    private Map<String, ProviderConfig> providers = new HashMap<>();

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String apiSecret;
        private String baseUrl;
        private String modelName;
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 120;
        private Map<String, String> extraParams = new HashMap<>();
    }
}
