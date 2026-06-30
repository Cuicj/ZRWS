"""
直接执行BPMN草稿表初始化SQL脚本
通过pymysql连接阿里云RDS数据库
"""

import sys
import os

# 数据库配置（从application.yml读取）
DB_CONFIG = {
    "host": "rm-bp1g8sw85a5z9e8gzeo.mysql.rds.aliyuncs.com",
    "port": 3306,
    "user": "test_admin",
    "password": "Test_admin",
    "database": "zrws_approval",
    "charset": "utf8mb4"
}

CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS `zrws_bpmn_draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` VARCHAR(100) NOT NULL COMMENT '流程唯一标识',
  `process_name` VARCHAR(200) DEFAULT NULL COMMENT '流程名称',
  `description` TEXT COMMENT '流程描述',
  `xml` LONGTEXT COMMENT 'BPMN XML内容',
  `json_def` LONGTEXT COMMENT 'JSON格式流程定义',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交审核时间',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `review_comment` TEXT COMMENT '审核意见',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `deployment_id` VARCHAR(100) DEFAULT NULL COMMENT '部署ID',
  `process_definition_id` VARCHAR(100) DEFAULT NULL COMMENT '流程定义ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人名称',
  `updater_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `updater_name` VARCHAR(100) DEFAULT NULL COMMENT '更新人名称',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_key` (`process_key`, `is_deleted`),
  KEY `idx_status` (`status`),
  KEY `idx_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程草稿表'
"""

INSERT_DATA_SQL = """
INSERT INTO `zrws_bpmn_draft` (`process_key`, `process_name`, `description`, `xml`, `status`, `version`, `creator_name`) VALUES
('STANDARD', '标准审批流程', '标准四级审批流程：校核→审核→批准→归档',
'<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">
  <bpmn:process id=\"STANDARD\" name=\"标准审批流程\" isExecutable=\"true\">
    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>
    <bpmn:userTask id=\"checker\" name=\"校核\"/>
    <bpmn:userTask id=\"reviewer\" name=\"审核\"/>
    <bpmn:userTask id=\"approver\" name=\"批准\"/>
    <bpmn:userTask id=\"archiver\" name=\"归档\"/>
    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>
    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"checker\"/>
    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"checker\" targetRef=\"reviewer\"/>
    <bpmn:sequenceFlow id=\"Flow_3\" sourceRef=\"reviewer\" targetRef=\"approver\"/>
    <bpmn:sequenceFlow id=\"Flow_4\" sourceRef=\"approver\" targetRef=\"archiver\"/>
    <bpmn:sequenceFlow id=\"Flow_5\" sourceRef=\"archiver\" targetRef=\"EndEvent_1\"/>
  </bpmn:process>
</bpmn:definitions>',
'DEPLOYED', 1, '系统'),
('MATERIAL', '物资申领流程', '物资采购审批流程：部门审批→库存核验→领导审批→签收',
'<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">
  <bpmn:process id=\"MATERIAL\" name=\"物资申领流程\" isExecutable=\"true\">
    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>
    <bpmn:userTask id=\"dept_approve\" name=\"部门审批\"/>
    <bpmn:userTask id=\"warehouse_check\" name=\"库存核验\"/>
    <bpmn:userTask id=\"leader_approve\" name=\"领导审批\"/>
    <bpmn:userTask id=\"signoff\" name=\"签收确认\"/>
    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>
    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"dept_approve\"/>
    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"dept_approve\" targetRef=\"warehouse_check\"/>
    <bpmn:sequenceFlow id=\"Flow_3\" sourceRef=\"warehouse_check\" targetRef=\"leader_approve\"/>
    <bpmn:sequenceFlow id=\"Flow_4\" sourceRef=\"leader_approve\" targetRef=\"signoff\"/>
    <bpmn:sequenceFlow id=\"Flow_5\" sourceRef=\"signoff\" targetRef=\"EndEvent_1\"/>
  </bpmn:process>
</bpmn:definitions>',
'DEPLOYED', 1, '系统'),
('DRONE_FLIGHT', '无人机外出报备', '无人机飞行任务审批：空域申请→安全审批→主管批准',
'<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">
  <bpmn:process id=\"DRONE_FLIGHT\" name=\"无人机外出报备\" isExecutable=\"true\">
    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>
    <bpmn:userTask id=\"airspace\" name=\"空域申请\"/>
    <bpmn:userTask id=\"safety\" name=\"安全审批\"/>
    <bpmn:userTask id=\"leader\" name=\"主管批准\"/>
    <bpmn:userTask id=\"execute\" name=\"执行确认\"/>
    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>
    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"airspace\"/>
    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"airspace\" targetRef=\"safety\"/>
    <bpmn:sequenceFlow id=\"Flow_3\" sourceRef=\"safety\" targetRef=\"leader\"/>
    <bpmn:sequenceFlow id=\"Flow_4\" sourceRef=\"leader\" targetRef=\"execute\"/>
    <bpmn:sequenceFlow id=\"Flow_5\" sourceRef=\"execute\" targetRef=\"EndEvent_1\"/>
  </bpmn:process>
</bpmn:definitions>',
'DEPLOYED', 1, '系统'),
('EMERGENCY', '应急快速通道', '紧急事件快速响应：应急提交→指挥员批准',
'<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">
  <bpmn:process id=\"EMERGENCY\" name=\"应急快速通道\" isExecutable=\"true\">
    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>
    <bpmn:userTask id=\"emergency_submit\" name=\"应急提交\"/>
    <bpmn:userTask id=\"commander\" name=\"指挥员批准\"/>
    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>
    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"emergency_submit\"/>
    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"emergency_submit\" targetRef=\"commander\"/>
    <bpmn:sequenceFlow id=\"Flow_3\" sourceRef=\"commander\" targetRef=\"EndEvent_1\"/>
  </bpmn:process>
</bpmn:definitions>',
'DEPLOYED', 1, '系统')
ON DUPLICATE KEY UPDATE `updated_time` = CURRENT_TIMESTAMP
"""

def main():
    try:
        import pymysql
    except ImportError:
        print("正在安装 pymysql ...")
        os.system(f"{sys.executable} -m pip install pymysql")
        import pymysql

    print("=" * 60)
    print("  BPMN草稿表初始化")
    print("=" * 60)
    print(f"  数据库: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
    print(f"  用户: {DB_CONFIG['user']}")
    print("=" * 60)

    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # 1. 创建表
        print("\n[1/3] 创建表 zrws_bpmn_draft ...")
        cursor.execute(CREATE_TABLE_SQL)
        print("  ✅ 表创建成功")

        # 2. 检查是否已有数据
        cursor.execute("SELECT COUNT(*) FROM zrws_bpmn_draft WHERE is_deleted = 0")
        count = cursor.fetchone()[0]
        print(f"\n[2/3] 当前数据量: {count} 条")

        # 3. 插入初始化数据
        if count == 0:
            print("\n[3/3] 插入初始化数据 ...")
            cursor.execute(INSERT_DATA_SQL)
            conn.commit()
            cursor.execute("SELECT COUNT(*) FROM zrws_bpmn_draft WHERE is_deleted = 0")
            new_count = cursor.fetchone()[0]
            print(f"  ✅ 插入完成，当前数据量: {new_count} 条")
        else:
            print("\n[3/3] 数据已存在，跳过插入")

        # 4. 查询验证
        print("\n" + "=" * 60)
        print("  验证结果")
        print("=" * 60)
        cursor.execute("SELECT process_key, process_name, status, version FROM zrws_bpmn_draft WHERE is_deleted = 0 ORDER BY id")
        rows = cursor.fetchall()
        for row in rows:
            print(f"  - {row[0]}: {row[1]} [{row[2]}] v{row[3]}")

        cursor.close()
        conn.close()
        print("\n✅ 初始化完成！")

    except Exception as e:
        print(f"\n❌ 错误: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
