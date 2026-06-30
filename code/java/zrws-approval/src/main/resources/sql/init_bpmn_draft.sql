-- 流程草稿表 zrws_bpmn_draft
-- 用于持久化流程设计器的草稿数据

CREATE TABLE IF NOT EXISTS `zrws_bpmn_draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` VARCHAR(100) NOT NULL COMMENT '流程唯一标识',
  `process_name` VARCHAR(200) DEFAULT NULL COMMENT '流程名称',
  `description` TEXT COMMENT '流程描述',
  `xml` LONGTEXT COMMENT 'BPMN XML内容',
  `json_def` LONGTEXT COMMENT 'JSON格式流程定义',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_REVIEW/APPROVED/REJECTED/PUBLISHED/DEPLOYED',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程草稿表';

-- 初始化一些测试草稿数据
INSERT INTO `zrws_bpmn_draft` (`process_key`, `process_name`, `description`, `status`, `version`, `creator_name`) VALUES
('STANDARD', '标准审批流程', '标准四级审批流程：校核→审核→批准→归档', 'DEPLOYED', 1, '系统'),
('MATERIAL', '物资申领流程', '物资采购审批流程：部门审批→库存核验→领导审批→签收', 'DEPLOYED', 1, '系统'),
('DRONE_FLIGHT', '无人机外出报备', '无人机飞行任务审批：空域申请→安全审批→主管批准', 'DEPLOYED', 1, '系统'),
('EMERGENCY', '应急快速通道', '紧急事件快速响应：应急提交→指挥员批准', 'DEPLOYED', 1, '系统')
ON DUPLICATE KEY UPDATE `updated_time` = CURRENT_TIMESTAMP;
