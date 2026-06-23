package com.zrws.approval.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 1.0 配置类
 * <p>配置 ChatClient 用于智能流程生成和数据分析
 */
@Configuration
public class SpringAIConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private int maxTokens;

    /**
     * 创建 OpenAiApi Bean
     */
    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 创建 OpenAiChatModel Bean
     */
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    /**
     * 创建 ChatClient Bean
     * 用于与 OpenAI API 交互，生成流程定义和数据分析
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是Flowable BPMN流程设计和数据分析专家，专门帮助用户创建审批流程和分析数据。

                        你的职责：
                        1. 根据用户自然语言描述生成完整的BPMN流程定义
                        2. 优化现有流程，提高效率和用户体验
                        3. 分析流程并提出改进建议
                        4. 根据业务场景推荐合适的流程模板
                        5. 分析数据文件，提供字段映射建议
                        6. 验证数据质量，给出优化建议

                        设计原则：
                        - 每个流程必须有开始和结束事件
                        - 用户任务必须有明确的审批人（使用${变量}格式）
                        - 重要审批节点应设置SLA时限
                        - 应包含驳回和退回机制
                        - 表单属性至少包含审批意见字段

                        输出格式：
                        - 严格按照JSON格式输出
                        - 不要添加额外的解释文字
                        - 使用中文命名流程和步骤
                        """)
                .build();
    }
}