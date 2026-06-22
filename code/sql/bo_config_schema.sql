-- ============================================================
-- 智壤卫士 - BO配置与数据分析器表
-- 版本: v2.1 BO数据配置 + AI分析
-- ============================================================

USE zrws_approval;

-- -----------------------------------------------------------
-- 6. BO配置与数据分析表
-- -----------------------------------------------------------

-- -----------------------------------------------------------
-- 6.1 BO业务对象定义表 (bo_definition)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS bo_definition;
CREATE TABLE bo_definition (
    bo_id            BIGINT          NOT NULL  COMMENT 'BO定义ID',
    bo_code         VARCHAR(50)     NOT NULL  COMMENT 'BO编码',
    bo_name         VARCHAR(100)    NOT NULL  COMMENT 'BO名称',
    bo_type         VARCHAR(30)     NOT NULL  COMMENT 'BO类型: EXCEL/CSV/JSON/XML/TEXT/FIXED_WIDTH',
    target_table    VARCHAR(100)    NOT NULL  COMMENT '目标表名',
    description     VARCHAR(500)    COMMENT '描述说明',
    version         VARCHAR(20)     DEFAULT '1.0' COMMENT '版本号',
    status          TINYINT(1)      DEFAULT 1  COMMENT '状态: 0-禁用 1-启用',
    ai_prompt       TEXT            COMMENT 'AI解析提示词',
    validation_rule TEXT            COMMENT '校验规则JSON',
    transform_rule  TEXT            COMMENT '数据转换规则JSON',
    is_deleted      TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by      BIGINT,
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_time    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (bo_id),
    UNIQUE KEY uk_bo_code (bo_code),
    KEY idx_status (status),
    KEY idx_type (bo_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BO业务对象定义表';

-- -----------------------------------------------------------
-- 6.2 BO字段配置表 (bo_field)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS bo_field;
CREATE TABLE bo_field (
    field_id         BIGINT          NOT NULL  COMMENT '字段ID',
    bo_id            BIGINT          NOT NULL  COMMENT 'BO定义ID',
    field_code       VARCHAR(50)     NOT NULL  COMMENT '字段编码',
    field_name       VARCHAR(100)    NOT NULL  COMMENT '字段名称',
    field_type       VARCHAR(30)     NOT NULL  COMMENT '字段类型: STRING/INT/DECIMAL/DATE/DATETIME/BOOLEAN/ENUM/JSON',
    source_names     VARCHAR(500)    COMMENT '源文件列名(逗号分隔,AI匹配用)',
    target_column    VARCHAR(50)     NOT NULL  COMMENT '目标表列名',
    default_value    VARCHAR(200)    COMMENT '默认值',
    is_required      TINYINT(1)      DEFAULT 0  COMMENT '是否必填',
    is_unique        TINYINT(1)      DEFAULT 0  COMMENT '是否唯一',
    validation_type  VARCHAR(30)     COMMENT '校验类型: RANGE/LENGTH/REGEX/ENUM/CUSTOM',
    validation_rule  VARCHAR(200)    COMMENT '校验规则表达式',
    transform_expr   VARCHAR(500)    COMMENT '转换表达式',
    enum_values      VARCHAR(1000)   COMMENT '枚举值列表(JSON)',
    sort_order       INT             DEFAULT 0  COMMENT '排序',
    status           TINYINT(1)      DEFAULT 1  COMMENT '状态',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (field_id),
    KEY idx_bo (bo_id),
    KEY idx_field_code (field_code),
    KEY idx_required (is_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BO字段配置表';

-- -----------------------------------------------------------
-- 6.3 数据导入批次表 (data_import_batch)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS data_import_batch;
CREATE TABLE data_import_batch (
    batch_id         BIGINT          NOT NULL  COMMENT '批次ID',
    batch_no         VARCHAR(50)     NOT NULL  COMMENT '批次号',
    bo_id            BIGINT          NOT NULL  COMMENT 'BO定义ID',
    bo_code          VARCHAR(50)     NOT NULL  COMMENT 'BO编码',
    file_name        VARCHAR(200)    NOT NULL  COMMENT '原始文件名',
    file_path        VARCHAR(500)    NOT NULL  COMMENT '文件存储路径',
    file_size        BIGINT          COMMENT '文件大小(字节)',
    file_hash        VARCHAR(64)     COMMENT '文件MD5哈希',
    total_rows       INT             COMMENT '总行数',
    success_rows     INT             DEFAULT 0  COMMENT '成功行数',
    failed_rows      INT             DEFAULT 0  COMMENT '失败行数',
    skipped_rows     INT             DEFAULT 0  COMMENT '跳过行数',
    import_mode      VARCHAR(20)     DEFAULT 'INSERT_UPDATE' COMMENT '导入模式: INSERT/UPDATE/INSERT_UPDATE/REPLACE',
    status           VARCHAR(20)     DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/SUCCESS/FAILED/PARTIAL',
    error_log        TEXT            COMMENT '错误日志JSON',
    ai_analysis      TEXT            COMMENT 'AI分析结果JSON',
    start_time       DATETIME        COMMENT '开始时间',
    end_time         DATETIME        COMMENT '结束时间',
    operator_id      BIGINT          COMMENT '操作人ID',
    operator_name    VARCHAR(60)     COMMENT '操作人姓名',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (batch_id),
    UNIQUE KEY uk_batch_no (batch_no),
    KEY idx_bo (bo_id),
    KEY idx_status (status),
    KEY idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导入批次表';

-- -----------------------------------------------------------
-- 6.4 数据导入明细表 (data_import_detail)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS data_import_detail;
CREATE TABLE data_import_detail (
    detail_id        BIGINT          NOT NULL  COMMENT '明细ID',
    batch_id         BIGINT          NOT NULL  COMMENT '批次ID',
    row_number       INT             NOT NULL  COMMENT '源文件行号',
    source_data      TEXT            NOT NULL  COMMENT '源数据JSON',
    target_data      TEXT            COMMENT '目标数据JSON(转换后)',
    field_mapping    TEXT            COMMENT '字段映射JSON',
    validation_result TEXT           COMMENT '校验结果JSON',
    ai_suggestion    TEXT            COMMENT 'AI建议JSON',
    status           VARCHAR(20)     DEFAULT 'PENDING' COMMENT '状态: PENDING/VALID/PROCESSING/SUCCESS/WARNING/SKIPPED/FAILED',
    error_message    VARCHAR(500)    COMMENT '错误信息',
    target_id        BIGINT          COMMENT '目标表主键ID',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (detail_id),
    KEY idx_batch (batch_id),
    KEY idx_status (status),
    KEY idx_target (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导入明细表';

-- -----------------------------------------------------------
-- 6.5 数据转换规则表 (data_transform_rule)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS data_transform_rule;
CREATE TABLE data_transform_rule (
    rule_id          BIGINT          NOT NULL  COMMENT '规则ID',
    bo_id            BIGINT          NOT NULL  COMMENT 'BO定义ID',
    rule_code        VARCHAR(50)     NOT NULL  COMMENT '规则编码',
    rule_name        VARCHAR(100)    NOT NULL  COMMENT '规则名称',
    rule_type        VARCHAR(30)     NOT NULL  COMMENT '规则类型: MAPPING/CALCULATION/CONDITION/FORMULA',
    source_field     VARCHAR(50)     COMMENT '源字段',
    target_field     VARCHAR(50)     COMMENT '目标字段',
    expression       TEXT            NOT NULL  COMMENT '规则表达式/公式',
    priority         INT             DEFAULT 0  COMMENT '优先级',
    condition_expr   VARCHAR(500)    COMMENT '触发条件表达式',
    status           TINYINT(1)      DEFAULT 1  COMMENT '状态',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (rule_id),
    KEY idx_bo (bo_id),
    KEY idx_type (rule_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据转换规则表';

-- -----------------------------------------------------------
-- 6.6 AI分析模板表 (ai_analysis_template)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS ai_analysis_template;
CREATE TABLE ai_analysis_template (
    template_id      BIGINT          NOT NULL  COMMENT '模板ID',
    template_code    VARCHAR(50)     NOT NULL  COMMENT '模板编码',
    template_name    VARCHAR(100)    NOT NULL  COMMENT '模板名称',
    bo_type          VARCHAR(30)     COMMENT '适用的BO类型',
    system_prompt    TEXT            NOT NULL  COMMENT '系统提示词',
    user_prompt_template TEXT        NOT NULL  COMMENT '用户提示词模板',
    output_format    VARCHAR(30)     DEFAULT 'JSON' COMMENT '输出格式: JSON/XML/MARKDOWN/TABLE',
    temperature      DECIMAL(3,2)   DEFAULT 0.3 COMMENT '温度参数',
    max_tokens       INT             DEFAULT 2000 COMMENT '最大令牌数',
    status           TINYINT(1)      DEFAULT 1  COMMENT '状态',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by       BIGINT,
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by       BIGINT,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (template_id),
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_bo_type (bo_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI分析模板表';

-- -----------------------------------------------------------
-- 6.7 枚举值配置表 (enum_config)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS enum_config;
CREATE TABLE enum_config (
    enum_id          BIGINT          NOT NULL  COMMENT '枚举ID',
    enum_code        VARCHAR(50)     NOT NULL  COMMENT '枚举编码',
    enum_name        VARCHAR(100)    NOT NULL  COMMENT '枚举名称',
    item_value       VARCHAR(100)    NOT NULL  COMMENT '枚举项值',
    item_label       VARCHAR(100)    NOT NULL  COMMENT '枚举项标签',
    item_order       INT             DEFAULT 0  COMMENT '排序',
    is_active        TINYINT(1)      DEFAULT 1  COMMENT '是否启用',
    is_deleted       TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (enum_id),
    KEY idx_code (enum_code),
    KEY idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='枚举值配置表';

-- ============================================================
-- 初始化BO配置数据示例
-- ============================================================

-- 插入BO定义示例：土壤检测数据导入
INSERT INTO bo_definition (bo_id, bo_code, bo_name, bo_type, target_table, description, version, status, ai_prompt, created_by, created_time) VALUES
(1, 'SOIL_SAMPLE', '土壤采样数据导入', 'EXCEL', 'soil_sample', '土壤采样数据从Excel导入', '1.0', 1,
 '你是土壤数据分析专家。请根据字段名称和表头，自动匹配源数据列与目标表字段。对于土壤检测数据，关注: pH值、有机质含量、氮磷钾含量、重金属含量等指标。数据校验时注意: pH值范围3-10，有机质0-100%，氮磷钾非负数。',
 1, NOW()),

(2, 'WEATHER_DATA', '气象数据导入', 'EXCEL', 'weather_data', '气象站数据导入', '1.0', 1,
 '你是气象数据分析专家。处理气象数据时，关注: 温度(℃)、湿度(%)、降水量(mm)、风速(m/s)、气压(hPa)等指标。校验规则: 温度范围-50~60℃，湿度0-100%，降水量非负。',
 1, NOW()),

(3, 'PLOT_INFO', '地块信息导入', 'EXCEL', 'plot', '地块基础信息导入', '1.0', 1,
 '你是土地资源数据分析专家。地块数据包括: 地块编号、面积、地块类型、边界坐标等。注意: 面积必须大于0，地块类型必须符合预定义枚举。',
 1, NOW());

-- 插入字段配置示例：土壤采样字段
INSERT INTO bo_field (field_id, bo_id, field_code, field_name, field_type, source_names, target_column, is_required, validation_type, validation_rule, sort_order) VALUES
(1, 1, 'mission_id', '任务ID', 'BIGINT', '任务ID,mission_id,任务编号', 'mission_id', 1, 'EXISTS', 'SELECT mission_id FROM flight_mission WHERE is_deleted=0', 1),
(2, 1, 'sample_no', '样本编号', 'STRING', '样本编号,sample_no,样本号', 'sample_no', 1, 'UNIQUE', '', 2),
(3, 1, 'latitude', '纬度', 'DECIMAL', '纬度,lat,y', 'latitude', 1, 'RANGE', '-90~90', 3),
(4, 1, 'longitude', '经度', 'DECIMAL', '经度,lng,x', 'longitude', 1, 'RANGE', '-180~180', 4),
(5, 1, 'sample_depth', '采样深度(cm)', 'DECIMAL', '深度,depth,采样深度', 'sample_depth', 0, 'RANGE', '0~200', 5),
(6, 1, 'ph_value', 'pH值', 'DECIMAL', 'pH,pH值,酸碱度', 'ph_value', 0, 'RANGE', '3~10', 6),
(7, 1, 'organic_matter', '有机质含量(%)', 'DECIMAL', '有机质,有机质含量,OM', 'organic_matter', 0, 'RANGE', '0~100', 7),
(8, 1, 'total_nitrogen', '全氮含量(g/kg)', 'DECIMAL', '全氮,TN,总氮', 'total_nitrogen', 0, 'RANGE', '0~10', 8),
(9, 1, 'available_p', '有效磷(mg/kg)', 'DECIMAL', '有效磷,AP,速效磷', 'available_p', 0, 'RANGE', '0~100', 9),
(10, 1, 'available_k', '速效钾(mg/kg)', 'DECIMAL', '速效钾,AK,钾', 'available_k', 0, 'RANGE', '0~500', 10);

-- 插入枚举配置示例
INSERT INTO enum_config (enum_id, enum_code, enum_name, item_value, item_label, item_order) VALUES
(1, 'PLOT_TYPE', '地块类型', 'PADDY', '水田', 1),
(2, 'PLOT_TYPE', '地块类型', 'DRY', '旱地', 2),
(3, 'PLOT_TYPE', '地块类型', 'FOREST', '林地', 3),
(4, 'PLOT_TYPE', '地块类型', 'GRASS', '草地', 4),
(5, 'PLOT_TYPE', '地块类型', 'BUILDING', '建设用地', 5),

(11, 'DISASTER_TYPE', '灾害类型', 'LANDSLIDE', '滑坡', 1),
(12, 'DISASTER_TYPE', '灾害类型', 'MUDFLOW', '泥石流', 2),
(13, 'DISASTER_TYPE', '灾害类型', 'FLOOD', '洪涝', 3),
(14, 'DISASTER_TYPE', '灾害类型', 'SUBSIDENCE', '地面塌陷', 4),
(15, 'DISASTER_TYPE', '灾害类型', 'EROSION', '水土流失', 5),

(21, 'RISK_LEVEL', '风险等级', 'LOW', '低风险', 1),
(22, 'RISK_LEVEL', '风险等级', 'MEDIUM', '中风险', 2),
(23, 'RISK_LEVEL', '风险等级', 'HIGH', '高风险', 3),
(24, 'RISK_LEVEL', '风险等级', 'CRITICAL', '极高风险', 4);

-- 插入AI分析模板
INSERT INTO ai_analysis_template (template_id, template_code, template_name, bo_type, system_prompt, user_prompt_template, output_format, temperature, max_tokens, created_by, created_time) VALUES
(1, 'DATA_MATCHING', '数据字段智能匹配', NULL,
 '你是数据匹配专家。你的任务是分析客户上传的数据文件，根据源数据的列名/表头，自动识别并匹配到目标BO的字段定义。',
 '{"fileName": "${fileName}", "sheetName": "${sheetName}", "headers": ${headers}, "sampleData": ${sampleData}, "boDefinition": ${boDefinition}}',
 'JSON', 0.2, 1500, 1, NOW()),

(2, 'DATA_VALIDATION', '数据质量校验', NULL,
 '你是数据质量分析专家。你的任务是校验导入数据的质量，识别数据问题并提供修复建议。',
 '{"rows": ${rows}, "fieldConfigs": ${fieldConfigs}, "validationRules": ${validationRules}}',
 'JSON', 0.3, 2000, 1, NOW()),

(3, 'DATA_ANALYSIS', '数据分析报告', NULL,
 '你是数据分析专家。你的任务是分析导入的数据，生成数据质量报告，包括: 数据分布、异常值识别、数据趋势、问题汇总等。',
 '{"importData": ${importData}, "boType": "${boType}", "statistics": ${statistics}}',
 'JSON', 0.4, 3000, 1, NOW());