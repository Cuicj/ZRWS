package com.zrws.approval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智壤卫士 - 审批服务
 * <p>基于 Flowable 7.0 + Spring AI 1.0 的智能审批流程微服务
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.zrws.**.feign")
@EnableScheduling
public class ZrwsApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZrwsApprovalApplication.class, args);
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════╗
                ║                                                           ║
                ║     智壤卫士 - 审批服务工作流服务                          ║
                ║                                                           ║
                ║     Spring Boot: 3.2.5                                    ║
                ║     Spring Cloud: 2023.0.1                                ║
                ║*     Flowable: 7.0.0                                       ║
                ║     Spring AI: 1.0.0-M2                                   ║
                ║     Java: 17                                              ║
                ║                                                           ║
                ║     服务地址: http://localhost:5571                        ║
                ║     API文档: http://localhost:5571/swagger-ui.html        ║
                ║                                                           ║
                ╚═══════════════════════════════════════════════════════════╝
                """);
    }
}