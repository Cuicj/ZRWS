package com.zrws.approval.tools;

import java.sql.*;
import java.io.*;

/**
 * 数据库初始化工具
 * 直接通过JDBC连接数据库，执行BPMN草稿表初始化
 * <p>
 * 使用方式:
 * java -cp zrws-approval.jar:lib/* com.zrws.approval.tools.DbInitTool
 */
public class DbInitTool {

    private static final String DB_URL = "jdbc:mysql://rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com:3306/zrws_approval?useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "test_admin";
    private static final String DB_PASS = "Test_admin";

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS `zrws_bpmn_draft` (\n" +
        "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',\n" +
        "  `process_key` VARCHAR(100) NOT NULL COMMENT '流程唯一标识',\n" +
        "  `process_name` VARCHAR(200) DEFAULT NULL COMMENT '流程名称',\n" +
        "  `description` TEXT COMMENT '流程描述',\n" +
        "  `xml` LONGTEXT COMMENT 'BPMN XML内容',\n" +
        "  `json_def` LONGTEXT COMMENT 'JSON格式流程定义',\n" +
        "  `version` INT DEFAULT 1 COMMENT '版本号',\n" +
        "  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态',\n" +
        "  `submit_time` DATETIME DEFAULT NULL COMMENT '提交审核时间',\n" +
        "  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',\n" +
        "  `review_comment` TEXT COMMENT '审核意见',\n" +
        "  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',\n" +
        "  `deployment_id` VARCHAR(100) DEFAULT NULL COMMENT '部署ID',\n" +
        "  `process_definition_id` VARCHAR(100) DEFAULT NULL COMMENT '流程定义ID',\n" +
        "  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
        "  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
        "  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',\n" +
        "  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人名称',\n" +
        "  `updater_id` BIGINT DEFAULT NULL COMMENT '更新人ID',\n" +
        "  `updater_name` VARCHAR(100) DEFAULT NULL COMMENT '更新人名称',\n" +
        "  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',\n" +
        "  PRIMARY KEY (`id`),\n" +
        "  UNIQUE KEY `uk_process_key` (`process_key`, `is_deleted`),\n" +
        "  KEY `idx_status` (`status`),\n" +
        "  KEY `idx_updated_time` (`updated_time`)\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程草稿表'";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  BPMN草稿表初始化工具");
        System.out.println("========================================");

        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("  数据库驱动: com.mysql.cj.jdbc.Driver");

            System.out.println("  连接数据库...");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("  ✅ 连接成功");

            stmt = conn.createStatement();

            // 1. 创建表
            System.out.println("\n[1/3] 创建表 zrws_bpmn_draft ...");
            stmt.execute(CREATE_TABLE_SQL);
            System.out.println("  ✅ 表创建成功");

            // 2. 检查数据量
            System.out.println("\n[2/3] 检查数据量...");
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zrws_bpmn_draft WHERE is_deleted = 0");
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            System.out.println("  当前数据量: " + count + " 条");

            // 3. 插入初始化数据（如果为空）
            if (count == 0) {
                System.out.println("\n[3/3] 插入初始化数据 ...");
                insertInitData(stmt);
                System.out.println("  ✅ 数据插入完成");
            } else {
                System.out.println("\n[3/3] 数据已存在，跳过插入");
            }

            // 4. 验证
            System.out.println("\n========================================");
            System.out.println("  验证结果");
            System.out.println("========================================");
            rs = stmt.executeQuery("SELECT process_key, process_name, status, version FROM zrws_bpmn_draft WHERE is_deleted = 0 ORDER BY id");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1) + ": " + rs.getString(2)
                    + " [" + rs.getString(3) + "] v" + rs.getInt(4));
            }
            rs.close();

            System.out.println("\n✅ 初始化完成！");

        } catch (Exception e) {
            System.err.println("\n❌ 错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private static void insertInitData(Statement stmt) throws SQLException {
        String[][] drafts = {
            {"STANDARD", "标准审批流程", "标准四级审批流程：校核→审核→批准→归档",
                generateBpmnXml("STANDARD", "标准审批流程",
                    new String[]{"checker", "审核"},
                    new String[]{"校核", "审核", "批准", "归档"})},
            {"MATERIAL", "物资申领流程", "物资采购审批流程：部门审批→库存核验→领导审批→签收",
                generateBpmnXml("MATERIAL", "物资申领流程",
                    new String[]{"dept_approve", "warehouse_check", "leader_approve", "signoff"},
                    new String[]{"部门审批", "库存核验", "领导审批", "签收确认"})},
            {"DRONE_FLIGHT", "无人机外出报备", "无人机飞行任务审批：空域申请→安全审批→主管批准",
                generateBpmnXml("DRONE_FLIGHT", "无人机外出报备",
                    new String[]{"airspace", "safety", "leader", "execute"},
                    new String[]{"空域申请", "安全审批", "主管批准", "执行确认"})},
            {"EMERGENCY", "应急快速通道", "紧急事件快速响应：应急提交→指挥员批准",
                generateBpmnXml("EMERGENCY", "应急快速通道",
                    new String[]{"emergency_submit", "commander"},
                    new String[]{"应急提交", "指挥员批准"})},
        };

        for (String[] draft : drafts) {
            String sql = "INSERT INTO zrws_bpmn_draft " +
                "(process_key, process_name, description, xml, status, version, creator_name) " +
                "VALUES ('" + draft[0] + "', '" + draft[1] + "', '" + draft[2] + "', '"
                + escapeSql(draft[3]) + "', 'DEPLOYED', 1, '系统')";
            stmt.execute(sql);
            System.out.println("  - 已插入: " + draft[1]);
        }
    }

    private static String generateBpmnXml(String processId, String processName,
                                          String[] taskIds, String[] taskNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ");
        sb.append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" ");
        sb.append("id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n");
        sb.append("  <bpmn:process id=\"").append(processId).append("\" name=\"").append(processName).append("\" isExecutable=\"true\">\n");
        sb.append("    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>\n");

        for (int i = 0; i < taskIds.length; i++) {
            sb.append("    <bpmn:userTask id=\"").append(taskIds[i]).append("\" name=\"").append(taskNames[i]).append("\"/>\n");
        }

        sb.append("    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>\n");

        // 连线
        sb.append("    <bpmn:sequenceFlow id=\"Flow_0\" sourceRef=\"StartEvent_1\" targetRef=\"").append(taskIds[0]).append("\"/>\n");
        for (int i = 0; i < taskIds.length - 1; i++) {
            sb.append("    <bpmn:sequenceFlow id=\"Flow_").append(i + 1).append("\" sourceRef=\"").append(taskIds[i]).append("\" targetRef=\"").append(taskIds[i + 1]).append("\"/>\n");
        }
        sb.append("    <bpmn:sequenceFlow id=\"Flow_").append(taskIds.length).append("\" sourceRef=\"").append(taskIds[taskIds.length - 1]).append("\" targetRef=\"EndEvent_1\"/>\n");

        sb.append("  </bpmn:process>\n");
        sb.append("</bpmn:definitions>");
        return sb.toString();
    }

    private static String escapeSql(String str) {
        return str.replace("'", "''");
    }
}
