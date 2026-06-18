-- ============================================================
--  智壤卫士 - 审批与工作流服务 业务扩展表结构
--  Flowable 核心表 (ACT_*) 由引擎在启动时 database-schema-update=true 自动创建
--  本脚本仅定义 智壤卫士 的业务扩展表
--  数据库: MySQL 8.0 / MariaDB 10.x
--  字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS zrws_approval
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE zrws_approval;

-- -----------------------------------------------------------
-- 1. 审批任务主表 (zrws_approval_task)
--    记录每一个审批流程的业务状态，与 Flowable 的 process_instance_id 关联
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_task;
CREATE TABLE zrws_approval_task (
    task_id              BIGINT          NOT NULL  COMMENT '审批任务ID（雪花主键）',
    flow_instance_id     VARCHAR(64)              COMMENT 'Flowable 流程实例ID',
    biz_type             VARCHAR(30)     NOT NULL  COMMENT '业务类型: STANDARD/MATERIAL/DRONE_FLIGHT/EMERGENCY',
    biz_id               BIGINT                   COMMENT '关联业务ID（任务ID/物资ID）',
    biz_title            VARCHAR(200)    NOT NULL  COMMENT '业务标题（用于列表展示）',
    applicant_id         BIGINT          NOT NULL  COMMENT '申请人ID',
    applicant_name       VARCHAR(60)     NOT NULL  COMMENT '申请人姓名（冗余，便于列表展示）',
    applicant_dept       VARCHAR(100)             COMMENT '申请部门',
    cur_step             VARCHAR(60)              COMMENT '当前步骤名称（中文/英文）',
    cur_step_key         VARCHAR(60)              COMMENT '当前步骤Key（与BPMN UserTask id对应）',
    status               VARCHAR(20)     NOT NULL  COMMENT 'DRAFT/PROCESSING/PASSED/REJECTED/RETURNED/ARCHIVED',
    priority             INT             DEFAULT 0 COMMENT '优先级: 0-普通 1-高 2-紧急',
    sla_deadline         DATETIME                 COMMENT 'SLA 截止时间',
    biz_data             JSON                     COMMENT '业务数据(JSON，随业务类型不同)',
    is_deleted           TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除 0-否 1-是',
    created_by           BIGINT,
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by           BIGINT,
    updated_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id),
    KEY idx_flow_instance (flow_instance_id),
    KEY idx_biz_type (biz_type),
    KEY idx_status (status),
    KEY idx_applicant (applicant_id),
    KEY idx_sla (sla_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务主表';


-- -----------------------------------------------------------
-- 2. 审批意见表 (zrws_approval_comment)
--    记录每一步审批的通过/驳回/退回意见
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_comment;
CREATE TABLE zrws_approval_comment (
    comment_id           BIGINT          NOT NULL  COMMENT '意见ID',
    task_id              BIGINT          NOT NULL  COMMENT '审批任务ID',
    flow_instance_id     VARCHAR(64),
    step_key             VARCHAR(60)              COMMENT '审批步骤Key',
    step_name            VARCHAR(60)              COMMENT '审批步骤名称',
    approver_id          BIGINT          NOT NULL  COMMENT '审批人ID',
    approver_name        VARCHAR(60)     NOT NULL  COMMENT '审批人姓名',
    action               VARCHAR(20)     NOT NULL  COMMENT 'APPROVE/REJECT/RETURN/SUBMIT',
    opinion              VARCHAR(500)             COMMENT '审批意见',
    is_deleted           TINYINT(1)      DEFAULT 0,
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id),
    KEY idx_task (task_id),
    KEY idx_approver (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';


-- -----------------------------------------------------------
-- 3. 审批抄送人/抄送消息表 (zrws_approval_cc)
--    可选：审批过程中的抄送记录
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_cc;
CREATE TABLE zrws_approval_cc (
    cc_id                BIGINT NOT NULL,
    task_id              BIGINT NOT NULL,
    user_id              BIGINT NOT NULL,
    user_name            VARCHAR(60),
    cc_reason            VARCHAR(200),
    has_read             TINYINT(1) DEFAULT 0,
    created_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cc_id),
    KEY idx_task (task_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批抄送表';


-- -----------------------------------------------------------
-- 4. 流程定义元数据表 (zrws_process_definition_meta)
--    自定义流程定义的描述信息（与 ACT_RE_PROCDEF 关联）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_process_definition_meta;
CREATE TABLE zrws_process_definition_meta (
    meta_id              BIGINT          NOT NULL,
    process_key          VARCHAR(64)     NOT NULL  COMMENT 'BPMN process id',
    process_name         VARCHAR(100)    NOT NULL  COMMENT '流程名称',
    steps_json           JSON                      COMMENT '步骤描述JSON',
    sla_hours            INT                      COMMENT '总超时小时数',
    enabled              TINYINT(1)      DEFAULT 1 COMMENT '是否启用',
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (meta_id),
    UNIQUE KEY uk_process_key (process_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义元数据';


-- -----------------------------------------------------------
-- 5. 审批超时升级记录 (zrws_approval_escalation)
--    记录 SLA 超时后的升级动作（自动升级到上级 / 自动通过 / 自动驳回）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_escalation;
CREATE TABLE zrws_approval_escalation (
    escalation_id        BIGINT NOT NULL,
    task_id              BIGINT NOT NULL,
    original_step        VARCHAR(60),
    escalation_step      VARCHAR(60),
    reason               VARCHAR(200) COMMENT '如：8小时未处理，自动升级',
    action               VARCHAR(20),
    created_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (escalation_id),
    KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批超时升级记录';


-- ============================================================
--  初始化数据：流程定义元数据
-- ============================================================
INSERT INTO zrws_process_definition_meta (meta_id, process_key, process_name, steps_json, sla_hours, enabled)
VALUES
(1, 'STANDARD', '标准审批',
 JSON_ARRAY(
    JSON_OBJECT('key','checker','name','校核','assignee','${checker}','slaHours',8),
    JSON_OBJECT('key','reviewer','name','审核','assignee','${reviewer}','slaHours',24),
    JSON_OBJECT('key','approver','name','批准','assignee','${approver}','slaHours',48),
    JSON_OBJECT('key','archiver','name','归档','assignee','${archiver}','slaHours',24)
 ), 104, 1),

(2, 'MATERIAL', '物资申领',
 JSON_ARRAY(
    JSON_OBJECT('key','dept_approve','name','部门审批','slaHours',24),
    JSON_OBJECT('key','warehouse_check','name','库存核验','slaHours',4),
    JSON_OBJECT('key','leader_approve','name','领导审批','slaHours',48),
    JSON_OBJECT('key','signoff','name','签收确认','slaHours',72)
 ), 148, 1),

(3, 'DRONE_FLIGHT', '无人机外出报备',
 JSON_ARRAY(
    JSON_OBJECT('key','airspace','name','空域申请','slaHours',24),
    JSON_OBJECT('key','safety','name','安全审批','slaHours',4),
    JSON_OBJECT('key','leader','name','主管批准','slaHours',2),
    JSON_OBJECT('key','execute','name','执行确认','slaHours',24)
 ), 54, 1),

(4, 'EMERGENCY', '应急快速通道',
 JSON_ARRAY(
    JSON_OBJECT('key','emergency_submit','name','应急提交','slaHours',0.5),
    JSON_OBJECT('key','commander','name','指挥员批准','slaHours',0.5)
 ), 1, 1);


-- ============================================================
--  可选：Mock 演示数据（实际生产环境请移除）
-- ============================================================
INSERT INTO zrws_approval_task (task_id, flow_instance_id, biz_type, biz_id, biz_title, applicant_id, applicant_name, applicant_dept, cur_step, cur_step_key, status, priority, sla_deadline)
VALUES
(100001, 'INST-001', 'STANDARD', 2001, '望城区乔口镇测绘任务标准审批', 1001, '王工', '技术部', '校核', 'checker', 'PROCESSING', 1, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(100002, 'INST-002', 'MATERIAL', 3001, 'RTK基准站电池采购申请', 1002, '李工', '装备部', '库存核验', 'warehouse_check', 'PROCESSING', 0, DATE_ADD(NOW(), INTERVAL 4 HOUR)),
(100003, 'INST-003', 'DRONE_FLIGHT', 4001, '岳麓区莲花镇无人机航拍任务', 1003, '张工', '技术部', '空域申请', 'airspace', 'PROCESSING', 2, DATE_ADD(NOW(), INTERVAL 24 HOUR)),
(100004, 'INST-004', 'EMERGENCY', 4002, '应急地质灾害调查', 1004, '王工', '应急部', '指挥员批准', 'commander', 'PROCESSING', 2, DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(100005, 'INST-005', 'STANDARD', 2002, '数据处理归档申请', 1005, '李工', '技术部', NULL, NULL, 'PASSED', 0, NULL);


INSERT INTO zrws_approval_comment (comment_id, task_id, step_key, step_name, approver_id, approver_name, action, opinion)
VALUES
(200001, 100001, 'start', '提交申请', 1001, '王工', 'SUBMIT', '请相关部门审批，任务重要性高'),
(200002, 100002, 'dept_approve', '部门审批', 1010, '刘经理', 'APPROVE', '同意采购，确保及时到位'),
(200003, 100003, 'start', '提交报备', 1003, '张工', 'SUBMIT', '申请明日上午8点至12点飞行'),
(200004, 100004, 'emergency_submit', '应急提交', 1004, '王工', 'SUBMIT', '灾害现场紧急，需立即飞行'),
(200005, 100005, 'checker', '校核', 1020, '赵工', 'APPROVE', '数据质量合格，通过');


-- ============================================================
--  验证查询
-- ============================================================
-- SELECT '流程定义数量', COUNT(*) FROM zrws_process_definition_meta
-- UNION ALL SELECT '审批任务数量', COUNT(*) FROM zrws_approval_task;
