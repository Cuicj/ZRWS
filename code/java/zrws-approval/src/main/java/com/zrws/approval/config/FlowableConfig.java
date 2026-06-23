package com.zrws.approval.config;

import com.zrws.approval.service.FlowableDeployService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable 6.8.1 流程引擎配置
 * <p>本项目采用 Spring Boot Starter 自动装配，实际流程引擎 bean (processEngine / runtimeService 等)
 * 由 flowable-spring-boot-starter-process 自动创建，此处仅提供：
 * <ul>
 *     <li>启动时的流程部署检查</li>
 *     <li>流程变量初始化</li>
 *     <li>Flowable 与 Spring 的事务桥接</li>
 * </ul>
 */
@Configuration
@EnableTransactionManagement
public class FlowableConfig implements CommandLineRunner {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private FlowableDeployService deployService;

    /**
     * 启动后自动部署流程并打印信息
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=====================================================");
        System.out.println("  [Flowable] 流程引擎初始化开始...");
        
        // 自动部署所有BPMN流程
        List<Map<String, Object>> deployResults = deployService.deployAllFromClasspath();
        
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println("  [Flowable] 已加载流程定义: " + count + " 个");
        for (Map<String, Object> result : deployResults) {
            System.out.println("  - " + result.get("processKey") + " (" + result.get("processName") + ") v" + result.get("version"));
        }
        
        // 打印流程引擎状态
        System.out.println("  [Flowable] 引擎状态: " + processEngine.getName());
        System.out.println("=====================================================\n");
    }

    /**
     * 为标准审批流程创建默认变量映射
     */
    public Map<String, Object> buildStandardVariables(String applicant, String bizTitle) {
        Map<String, Object> vars = new HashMap<>();
        // 标准审批流程：按角色分配（实际可从用户/角色表查询）
        vars.put("checker", "1020");          // 校核人
        vars.put("reviewer", "1030");          // 审核人
        vars.put("approver", "1040");          // 批准人
        vars.put("archiver", "1050");          // 归档人
        vars.put("applicant", applicant);      // 申请人
        vars.put("bizTitle", bizTitle);        // 业务标题
        // SLA deadline (由前端传入或在此处计算)
        vars.put("dueDateChecker", null);
        vars.put("dueDateReviewer", null);
        vars.put("dueDateApprover", null);
        return vars;
    }

    public Map<String, Object> buildMaterialVariables(String applicant) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("deptManager", "1010");
        vars.put("warehouse", "1011");
        vars.put("leader", "1040");
        vars.put("applicant", applicant);
        vars.put("stockStatus", "SUFFICIENT"); // 默认充足，可由人工/外部接口实际判断
        return vars;
    }

    public Map<String, Object> buildDroneFlightVariables(String applicant) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("airspaceAdmin", "1015");
        vars.put("safetyOfficer", "1016");
        vars.put("leader", "1040");
        vars.put("applicant", applicant);
        return vars;
    }

    public Map<String, Object> buildEmergencyVariables(String applicant) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("commander", "1060");
        vars.put("applicant", applicant);
        return vars;
    }
}
