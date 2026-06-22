-- ============================================================
-- 智壤卫士 - AI数据适配器扩展表
-- 版本: v2.2 审批流配置 + 数据统计
-- ============================================================

USE zrws_approval;

-- -----------------------------------------------------------
-- 6.8 审批流配置表 (approval_flow_config)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS approval_flow_config;
CREATE TABLE approval_flow_config (
    config_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    bo_code         VARCHAR(50)     NOT NULL COMMENT 'BO编码',
    bo_name         VARCHAR(100)    COMMENT 'BO名称',
    operation_type  VARCHAR(20)     NOT NULL COMMENT '操作类型: INSERT/UPDATE/DELETE/IMPORT',
    operation_desc  VARCHAR(100)    COMMENT '操作类型描述',
    enable_approval TINYINT(1)      DEFAULT 0 COMMENT '是否启用审批流',
    process_definition_id VARCHAR(64) COMMENT '流程定义ID',
    process_key     VARCHAR(64)     COMMENT '流程Key',
    process_name    VARCHAR(100)    COMMENT '流程名称',
    approval_level  VARCHAR(20)     DEFAULT 'SINGLE' COMMENT '审批级别: SINGLE/MULTI/CUSTOM',
    approver_config TEXT            COMMENT '审批人配置(JSON)',
    condition_expr  VARCHAR(500)    COMMENT '条件表达式',
    priority        INT             DEFAULT 1 COMMENT '优先级',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    description     VARCHAR(500)    COMMENT '描述说明',
    is_deleted      TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_by      BIGINT,
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_time    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_bo_operation (bo_code, operation_type),
    KEY idx_status (status),
    KEY idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流配置表';

-- -----------------------------------------------------------
-- 6.9 数据统计表 (data_statistics)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS data_statistics;
CREATE TABLE data_statistics (
    stats_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    stats_date      VARCHAR(8)      NOT NULL COMMENT '统计日期(yyyyMMdd)',
    bo_code         VARCHAR(50)     COMMENT 'BO编码',
    bo_name         VARCHAR(100)    COMMENT 'BO名称',
    operation_type  VARCHAR(20)     COMMENT '操作类型',
    total_count     INT             DEFAULT 0 COMMENT '总数量',
    success_count   INT             DEFAULT 0 COMMENT '成功数量',
    failed_count    INT             DEFAULT 0 COMMENT '失败数量',
    approved_count  INT             DEFAULT 0 COMMENT '审批通过数量',
    rejected_count  INT             DEFAULT 0 COMMENT '审批驳回数量',
    pending_count   INT             DEFAULT 0 COMMENT '待审批数量',
    file_count      INT             DEFAULT 0 COMMENT '导入文件数',
    total_records   INT             DEFAULT 0 COMMENT '总记录数',
    quality_score   INT             COMMENT '数据质量评分',
    avg_process_time DECIMAL(10,2)  COMMENT '平均处理时间(秒)',
    period_type     VARCHAR(20)     DEFAULT 'DAILY' COMMENT '统计周期: DAILY/WEEKLY/MONTHLY',
    is_deleted      TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (stats_id),
    UNIQUE KEY uk_date_bo (stats_date, bo_code),
    KEY idx_date (stats_date),
    KEY idx_period (period_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据统计表';

-- ============================================================
-- 初始化审批流配置示例数据
-- ============================================================

INSERT INTO approval_flow_config (config_id, bo_code, bo_name, operation_type, operation_desc, enable_approval, approval_level, priority, status, description) VALUES
(1, 'SOIL_SAMPLE', '土壤采样数据', 'INSERT', '新增', 1, 'SINGLE', 1, 1, '土壤采样数据新增需要审批'),
(2, 'SOIL_SAMPLE', '土壤采样数据', 'UPDATE', '修改', 0, 'SINGLE', 2, 1, '土壤采样数据修改需要审批'),
(3, 'SOIL_SAMPLE', '土壤采样数据', 'IMPORT', '导入', 1, 'MULTI', 1, 1, '土壤采样数据批量导入需要多级审批'),
(4, 'WEATHER_DATA', '气象数据', 'IMPORT', '导入', 0, 'SINGLE', 2, 1, '气象数据导入审批配置'),
(5, 'PLOT_INFO', '地块信息', 'INSERT', '新增', 1, 'CUSTOM', 1, 1, '地块信息新增需要自定义审批流程'),
(6, 'PLOT_INFO', '地块信息', 'DELETE', '删除', 1, 'MULTI', 1, 1, '地块信息删除需要多级审批'),
(7, 'DISASTER_RISK', '灾害风险', 'INSERT', '新增', 1, 'MULTI', 1, 1, '灾害风险新增需要紧急审批'),
(8, 'FLIGHT_MISSION', '飞行任务', 'INSERT', '新增', 1, 'SINGLE', 1, 1, '飞行任务新增需要审批');

-- ============================================================
-- 初始化统计数据（最近7天模拟数据）
-- ============================================================

INSERT INTO data_statistics (stats_id, stats_date, bo_code, bo_name, total_count, success_count, failed_count, file_count, total_records, quality_score) VALUES
(1, '20260616', 'SOIL_SAMPLE', '土壤采样数据', 15, 14, 1, 8, 1500, 95),
(2, '20260617', 'SOIL_SAMPLE', '土壤采样数据', 12, 10, 2, 6, 1200, 88),
(3, '20260618', 'WEATHER_DATA', '气象数据', 20, 20, 0, 4, 4000, 100),
(4, '20260619', 'PLOT_INFO', '地块信息', 8, 7, 1, 3, 80, 92),
(5, '20260620', 'SOIL_SAMPLE', '土壤采样数据', 18, 17, 1, 10, 1800, 96),
(6, '20260621', 'DISASTER_RISK', '灾害风险', 5, 4, 1, 2, 50, 85),
(7, '20260622', 'SOIL_SAMPLE', '土壤采样数据', 10, 9, 1, 5, 1000, 93);