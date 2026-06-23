package com.zrws.approval.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 1.0 配置类（已简化）
 * <p>AI调用现在使用 RestTemplate 直接访问 OpenAI API
 * <p>配置项在 application.yml 中定义
 */
@Configuration
public class SpringAIConfig {

    // AI服务现在使用 RestTemplate 直接调用 OpenAI API
    // 配置项（见 application.yml）：
    // - spring.ai.openai.api-key
    // - spring.ai.openai.base-url
    // - spring.ai.openai.chat.model
    // - spring.ai.analyzer.temperature

}
