package com.zrws.approval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智壤卫士 - 审批与工作流微服务
 *
 * <p>基于 Flowable 6.8.1 流程引擎 + Spring Boot 2.7
 * <p>已预设 4 类审批流程：
 * <ul>
 *     <li>STANDARD      标准审批（5步：提交→校核→审核→批准→归档）</li>
 *     <li>MATERIAL      物资申领（5步：申领→部门审批→仓库→领导→签收）</li>
 *     <li>DRONE_FLIGHT  无人机外出报备（4步：报备→空域→安全→批准）</li>
 *     <li>EMERGENCY     应急快速通道（2步：快速提交→指挥员批准）</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.zrws.approval.mapper")
public class ZrwsApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZrwsApprovalApplication.class, args);
        System.out.println("\n====================================================");
        System.out.println("  智壤卫士 - 审批与工作流服务启动成功  ");
        System.out.println("  http://localhost:8083/approval ");
        System.out.println("  Flowable 6.8.1 / Spring Boot 2.7 ");
        System.out.println("====================================================\n");
    }
}
