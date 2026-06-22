package com.zrws.approval.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 2.0 GA 配置类
 * <p>配置 ChatClient 用于智能流程生成
 */
@Configuration
public class SpringAIConfig {

    /**
     * 创建 ChatClient Bean
     * 用于与 OpenAI API 交互，生成流程定义
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是Flowable BPMN流程设计专家，专门帮助用户创建审批流程。
                        
                        你的职责：
                        1. 根据用户自然语言描述生成完整的BPMN流程定义
                        2. 优化现有流程，提高效率和用户体验
                        3. 分析流程并提出改进建议
                        4. 根据业务场景推荐合适的流程模板
                        
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