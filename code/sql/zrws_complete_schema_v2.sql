-- ============================================================
--  智壤卫士 - 完整数据库初始化脚本
--  数据库: MySQL 8.0 / 阿里云RDS
--  字符集: utf8mb4
--  版本: v2.0 完整业务表 + Mock数据
-- ============================================================

-- -----------------------------------------------------------
-- 1. 创建数据库
-- -----------------------------------------------------------
CREATE DATABASE IF NOT EXISTS zrws_approval
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE zrws_approval;

-- ============================================================
-- 2. Flowable 核心表 (ACT_*)
--    由 Spring Boot 启动时 database-schema-update=true 自动创建
--    此处仅记录表分类供参考
-- ============================================================
-- ACT_RE_*      : 流程定义表
-- ACT_RU_*      : 运行时流程实例表
-- ACT_HI_*      : 历史流程实例表
-- ACT_GE_*      : 通用数据表
-- ACT_ID_*      : 身份标识表

-- ============================================================
-- 3. 系统管理基础表
-- ============================================================

-- -----------------------------------------------------------
-- 3.1 部门表 (sys_dept)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    dept_id       BIGINT          NOT NULL  COMMENT '部门ID',
    parent_id     BIGINT          DEFAULT 0  COMMENT '父部门ID',
    dept_name     VARCHAR(50)     NOT NULL  COMMENT '部门名称',
    dept_code     VARCHAR(20)     NOT NULL  COMMENT '部门编码',
    dept_type     VARCHAR(10)     DEFAULT 'DEPT' COMMENT '部门类型: DEPT/WING/BASE',
    leader        VARCHAR(50)     COMMENT '部门负责人',
    phone         VARCHAR(20)     COMMENT '联系电话',
    sort_order    INT             DEFAULT 0  COMMENT '排序',
    status        TINYINT(1)      DEFAULT 1  COMMENT '状态: 0-禁用 1-正常',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_id),
    KEY idx_parent (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- -----------------------------------------------------------
-- 3.2 用户表 (sys_user)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id       BIGINT          NOT NULL  COMMENT '用户ID',
    username      VARCHAR(50)     NOT NULL  COMMENT '用户名',
    password      VARCHAR(200)    NOT NULL  COMMENT '密码(加密)',
    real_name     VARCHAR(50)     NOT NULL  COMMENT '真实姓名',
    nick_name     VARCHAR(50)     COMMENT '昵称',
    email         VARCHAR(100)    COMMENT '邮箱',
    phone         VARCHAR(20)     COMMENT '手机号',
    avatar        VARCHAR(500)    COMMENT '头像URL',
    dept_id       BIGINT          COMMENT '所属部门ID',
    post          VARCHAR(50)     COMMENT '岗位',
    role_key      VARCHAR(50)     DEFAULT 'operator' COMMENT '角色标识: admin/reviewer/approver/operator',
    status        TINYINT(1)      DEFAULT 1  COMMENT '状态: 0-禁用 1-正常',
    login_ip      VARCHAR(128)    COMMENT '最后登录IP',
    login_date    DATETIME        COMMENT '最后登录时间',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    KEY idx_username (username),
    KEY idx_dept (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 3.3 字典表 (sys_dict)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    dict_id       BIGINT          NOT NULL  COMMENT '字典ID',
    dict_name     VARCHAR(100)    NOT NULL  COMMENT '字典名称',
    dict_code     VARCHAR(50)     NOT NULL  COMMENT '字典编码',
    description   VARCHAR(500)   COMMENT '描述',
    sort_order    INT             DEFAULT 0  COMMENT '排序',
    status        TINYINT(1)      DEFAULT 1  COMMENT '状态',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dict_id),
    UNIQUE KEY uk_dict_code (dict_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    item_id       BIGINT          NOT NULL  COMMENT '字典项ID',
    dict_id       BIGINT          NOT NULL  COMMENT '字典ID',
    item_text     VARCHAR(100)    NOT NULL  COMMENT '字典项文本',
    item_value    VARCHAR(100)    NOT NULL  COMMENT '字典项值',
    sort_order    INT             DEFAULT 0  COMMENT '排序',
    css_class     VARCHAR(100)   COMMENT '样式类',
    list_class    VARCHAR(100)   COMMENT '表格样式',
    is_default    TINYINT(1)      DEFAULT 0  COMMENT '是否默认',
    status        TINYINT(1)      DEFAULT 1  COMMENT '状态',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    KEY idx_dict (dict_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

-- ============================================================
-- 4. 业务核心表
-- ============================================================

-- -----------------------------------------------------------
-- 4.1 设备表 (device)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS device;
CREATE TABLE device (
    device_id     BIGINT          NOT NULL  COMMENT '设备ID',
    device_code   VARCHAR(50)     NOT NULL  COMMENT '设备编码',
    device_name   VARCHAR(100)    NOT NULL  COMMENT '设备名称',
    device_type   VARCHAR(30)     NOT NULL  COMMENT '设备类型: UAV/DRONE/RTK/SENSOR/CAMERA',
    device_model  VARCHAR(100)   COMMENT '设备型号',
    manufacturer  VARCHAR(100)   COMMENT '生产厂家',
    serial_no     VARCHAR(100)    COMMENT '序列号',
    purchase_date DATE            COMMENT '购置日期',
    dept_id       BIGINT          COMMENT '所属部门',
    status        VARCHAR(20)     DEFAULT 'IDLE' COMMENT '状态: IDLE/ON_MISSION/MAINTENANCE/RETIRED',
    battery_level INT             COMMENT '电池电量%',
    work_hours    DECIMAL(10,2)  COMMENT '累计工作时数',
    location      VARCHAR(200)    COMMENT '当前位置',
    last_sync     DATETIME        COMMENT '最后同步时间',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (device_id),
    KEY idx_code (device_code),
    KEY idx_type (device_type),
    KEY idx_status (status),
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- -----------------------------------------------------------
-- 4.2 飞行任务表 (flight_mission)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS flight_mission;
CREATE TABLE flight_mission (
    mission_id    BIGINT          NOT NULL  COMMENT '任务ID',
    mission_no    VARCHAR(50)     NOT NULL  COMMENT '任务编号',
    mission_name  VARCHAR(200)   NOT NULL  COMMENT '任务名称',
    mission_type  VARCHAR(30)     NOT NULL  COMMENT '任务类型: SURVEY/MAPPING/SPRAY/SAMPLE/INSPECT',
    area_name     VARCHAR(200)   NOT NULL  COMMENT '作业区域名称',
    area_address  VARCHAR(500)   COMMENT '详细地址',
    latitude      DECIMAL(10,6)  COMMENT '中心点纬度',
    longitude     DECIMAL(10,6)  COMMENT '中心点经度',
    device_id     BIGINT          COMMENT '作业设备ID',
    operator_id  BIGINT          NOT NULL  COMMENT '操作员ID',
    flight_date   DATE            COMMENT '飞行日期',
    start_time    DATETIME        COMMENT '实际开始时间',
    end_time      DATETIME        COMMENT '实际结束时间',
    duration      INT             COMMENT '飞行时长(分钟)',
    coverage      DECIMAL(10,2)  COMMENT '作业面积(亩)',
    altitude      INT             COMMENT '飞行高度(m)',
    overlap_fwd   DECIMAL(4,2)   COMMENT '前向重叠率',
    overlap_side  DECIMAL(4,2)   COMMENT '旁向重叠率',
    photo_count   INT             COMMENT '照片数量',
    lidar_points  BIGINT          COMMENT 'LiDAR点数',
    soil_samples  INT             COMMENT '土壤采样数',
    gps_accuracy_h VARCHAR(20)   COMMENT 'GPS平面精度',
    gps_accuracy_v VARCHAR(20)   COMMENT 'GPS高程精度',
    status        VARCHAR(20)     NOT NULL  COMMENT '状态: DRAFT/APPROVED/FLYING/COMPLETED/ABNORMAL/CANCELLED',
    flow_instance_id VARCHAR(64) COMMENT '审批流程实例ID',
    remark        TEXT            COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mission_id),
    UNIQUE KEY uk_mission_no (mission_no),
    KEY idx_type (mission_type),
    KEY idx_status (status),
    KEY idx_operator (operator_id),
    KEY idx_device (device_id),
    KEY idx_date (flight_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行任务表';

-- -----------------------------------------------------------
-- 4.3 GPS航迹点表 (gps_track_point)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS gps_track_point;
CREATE TABLE gps_track_point (
    point_id      BIGINT          NOT NULL  COMMENT '航迹点ID',
    mission_id    BIGINT          NOT NULL  COMMENT '任务ID',
    seq_no        INT             NOT NULL  COMMENT '顺序号',
    latitude      DECIMAL(10,6)  NOT NULL  COMMENT '纬度',
    longitude     DECIMAL(10,6)  NOT NULL  COMMENT '经度',
    altitude      DECIMAL(10,2)  COMMENT '高程(m)',
    speed         DECIMAL(6,2)   COMMENT '速度(m/s)',
    heading       INT             COMMENT '航向角(度)',
    record_time   DATETIME        NOT NULL  COMMENT '记录时间',
    satellites    INT             COMMENT '卫星数',
    fix_type      VARCHAR(20)    COMMENT '定位类型: SINGLE/FIXED/FLOAT',
    accuracy_h    DECIMAL(6,3)  COMMENT '水平精度(m)',
    accuracy_v    DECIMAL(6,3)  COMMENT '垂直精度(m)',
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (point_id),
    KEY idx_mission (mission_id),
    KEY idx_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GPS航迹点表';

-- -----------------------------------------------------------
-- 4.4 土壤采样表 (soil_sample)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS soil_sample;
CREATE TABLE soil_sample (
    sample_id     BIGINT          NOT NULL  COMMENT '采样ID',
    sample_code   VARCHAR(50)     NOT NULL  COMMENT '采样编号',
    mission_id    BIGINT          COMMENT '关联任务ID',
    latitude      DECIMAL(10,6)  NOT NULL  COMMENT '纬度',
    longitude     DECIMAL(10,6)  NOT NULL  COMMENT '经度',
    altitude      DECIMAL(10,2)  COMMENT '高程(m)',
    depth         DECIMAL(6,2)   COMMENT '采样深度(cm)',
    soil_type     VARCHAR(30)    COMMENT '土壤类型: SANDY/LOAM/CLAY/SILT',
    ph_value      DECIMAL(4,2)   COMMENT 'pH值',
    moisture      DECIMAL(5,2)   COMMENT '含水量(%)',
    ec_value      INT             COMMENT '电导率(μs/cm)',
    organic_matter DECIMAL(6,2) COMMENT '有机质(g/kg)',
    total_nitrogen DECIMAL(5,2)  COMMENT '全氮(g/kg)',
    available_p   DECIMAL(5,2)  COMMENT '有效磷(mg/kg)',
    available_k   DECIMAL(5,2)  COMMENT '速效钾(mg/kg)',
    cec_value     DECIMAL(6,2)  COMMENT '阳离子交换量(cmol/kg)',
    nitrogen_level VARCHAR(10)   COMMENT '氮素等级',
    phosphorus_level VARCHAR(10)  COMMENT '磷素等级',
    potassium_level VARCHAR(10)   COMMENT '钾素等级',
    fertility_grade VARCHAR(10)  COMMENT '肥力等级: I/II/III/IV',
    collector_id  BIGINT          COMMENT '采集人ID',
    collect_time  DATETIME        COMMENT '采集时间',
    lab_id        VARCHAR(50)     COMMENT '实验室编号',
    test_time     DATETIME        COMMENT '检测时间',
    report_url    VARCHAR(500)   COMMENT '检测报告URL',
    remark        VARCHAR(500)   COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (sample_id),
    UNIQUE KEY uk_sample_code (sample_code),
    KEY idx_mission (mission_id),
    KEY idx_type (soil_type),
    KEY idx_grade (fertility_grade),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='土壤采样表';

-- -----------------------------------------------------------
-- 4.5 数据处理表 (data_processing)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS data_processing;
CREATE TABLE data_processing (
    process_id    BIGINT          NOT NULL  COMMENT '处理ID',
    mission_id    BIGINT          NOT NULL  COMMENT '关联任务ID',
    process_type  VARCHAR(30)     NOT NULL  COMMENT '处理类型: ORTHO/LIDAR/DSM/DEM/VOLUME/DIFFERENCE',
    stage_name    VARCHAR(50)    NOT NULL  COMMENT '阶段名称',
    stage_order   INT             NOT NULL  COMMENT '阶段顺序',
    progress      INT             DEFAULT 0  COMMENT '进度(0-100)',
    status        VARCHAR(20)     NOT NULL  COMMENT '状态: PENDING/PROCESSING/DONE/FAILED',
    start_time    DATETIME        COMMENT '开始时间',
    end_time      DATETIME        COMMENT '结束时间',
    duration      INT             COMMENT '耗时(秒)',
    input_files   JSON            COMMENT '输入文件列表',
    output_files  JSON            COMMENT '输出文件列表',
    quality_score DECIMAL(5,2)   COMMENT '质量评分',
    error_msg     TEXT            COMMENT '错误信息',
    processed_by  BIGINT          COMMENT '处理人ID',
    remark        TEXT            COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (process_id),
    KEY idx_mission (mission_id),
    KEY idx_type (process_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据处理表';

-- -----------------------------------------------------------
-- 4.6 面积测量表 (area_measurement)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS area_measurement;
CREATE TABLE area_measurement (
    measure_id    BIGINT          NOT NULL  COMMENT '测量ID',
    measure_no    VARCHAR(50)     NOT NULL  COMMENT '测量编号',
    plot_name     VARCHAR(200)    NOT NULL  COMMENT '地块名称',
    owner_name    VARCHAR(100)   COMMENT '所有者姓名',
    mission_id    BIGINT          COMMENT '关联任务ID',
    gps_area      DECIMAL(10,4)  NOT NULL  COMMENT 'GPS测量面积(亩)',
    registered_area DECIMAL(10,4) COMMENT '登记面积(亩)',
    area_diff     DECIMAL(10,4)  COMMENT '面积差值(亩)',
    diff_ratio    DECIMAL(6,4)  COMMENT '差值比例(%)',
    status        VARCHAR(20)     NOT NULL  COMMENT '状态: NORMAL/ABNORMAL/REVIEW',
    boundary_json TEXT            COMMENT '边界坐标JSON',
    perimeter     DECIMAL(10,2)  COMMENT '周长(m)',
    accuracy      VARCHAR(20)    COMMENT '测量精度',
    measurer_id   BIGINT          COMMENT '测量员ID',
    measure_time  DATETIME        COMMENT '测量时间',
    remark        VARCHAR(500)   COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (measure_id),
    KEY idx_measure_no (measure_no),
    KEY idx_status (status),
    KEY idx_mission (mission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面积测量表';

-- -----------------------------------------------------------
-- 4.7 天气数据表 (weather_data)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS weather_data;
CREATE TABLE weather_data (
    weather_id    BIGINT          NOT NULL  COMMENT '气象ID',
    record_date   DATE            NOT NULL  COMMENT '记录日期',
    station_code  VARCHAR(50)    COMMENT '站点编码',
    station_name  VARCHAR(100)   COMMENT '站点名称',
    latitude      DECIMAL(10,6)  COMMENT '纬度',
    longitude     DECIMAL(10,6)  COMMENT '经度',
    temperature   DECIMAL(5,1)   COMMENT '温度(℃)',
    humidity      INT             COMMENT '湿度(%)',
    wind_speed    DECIMAL(5,1)  COMMENT '风速(m/s)',
    wind_direction VARCHAR(10)   COMMENT '风向',
    rainfall      DECIMAL(6,2)  COMMENT '降雨量(mm)',
    pressure      DECIMAL(7,1)  COMMENT '气压(hPa)',
    visibility    VARCHAR(20)    COMMENT '能见度',
    weather_type  VARCHAR(30)   COMMENT '天气现象',
    cloud_cover   INT             COMMENT '云量(%)',
    uv_index      INT             COMMENT '紫外线指数',
    data_source   VARCHAR(20)    COMMENT '数据来源: AUTO/MANUAL/API',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (weather_id),
    KEY idx_date (record_date),
    KEY idx_station (station_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天气数据表';

-- -----------------------------------------------------------
-- 4.8 降雨量记录表 (rainfall_record)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS rainfall_record;
CREATE TABLE rainfall_record (
    record_id     BIGINT          NOT NULL  COMMENT '记录ID',
    station_code  VARCHAR(50)    NOT NULL  COMMENT '站点编码',
    station_name  VARCHAR(100)   COMMENT '站点名称',
    record_date   DATE            NOT NULL  COMMENT '记录日期',
    record_hour   INT             COMMENT '记录小时(0-23)',
    rainfall      DECIMAL(6,2)  NOT NULL  COMMENT '降雨量(mm)',
    duration      INT             COMMENT '降雨时长(分钟)',
    intensity     VARCHAR(20)    COMMENT '降雨强度: LIGHT/MODERATE/HEAVY/TORRENTIAL',
    data_type     VARCHAR(20)    COMMENT '数据类型: AUTO/MANUAL',
    collector_id  BIGINT          COMMENT '采集人ID',
    remark        VARCHAR(500)   COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (record_id),
    KEY idx_station (station_code),
    KEY idx_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='降雨量记录表';

-- -----------------------------------------------------------
-- 4.9 灾害风险评估表 (disaster_risk)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS disaster_risk;
CREATE TABLE disaster_risk (
    risk_id       BIGINT          NOT NULL  COMMENT '风险ID',
    mission_id    BIGINT          COMMENT '关联任务ID',
    latitude      DECIMAL(10,6)  NOT NULL  COMMENT '纬度',
    longitude     DECIMAL(10,6)  NOT NULL  COMMENT '经度',
    disaster_type VARCHAR(30)     NOT NULL  COMMENT '灾害类型: LANDSLIDE/MUDFLOW/FLOOD/SUBSIDENCE/EROSION/DROUGHT',
    risk_level    VARCHAR(10)     NOT NULL  COMMENT '风险等级: LOW/MEDIUM/HIGH/CRITICAL',
    hazard_score  DECIMAL(4,2)   COMMENT '危险指数',
    vulnerability DECIMAL(4,2)   COMMENT '脆弱性指数',
    exposure      DECIMAL(4,2)   COMMENT '暴露度指数',
    risk_score    DECIMAL(4,2)  COMMENT '综合风险指数',
    area_affected DECIMAL(10,2)  COMMENT '影响面积(亩)',
    population    INT             COMMENT '影响人口',
    asset_value   DECIMAL(15,2)  COMMENT '影响资产(元)',
    description   TEXT            COMMENT '风险描述',
    mitigation    TEXT            COMMENT '减灾措施',
    assessor_id   BIGINT          COMMENT '评估人ID',
    assess_time   DATETIME        COMMENT '评估时间',
    remark        VARCHAR(500)   COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (risk_id),
    KEY idx_type (disaster_type),
    KEY idx_level (risk_level),
    KEY idx_mission (mission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灾害风险评估表';

-- -----------------------------------------------------------
-- 4.10 土壤历史数据表 (soil_history)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS soil_history;
CREATE TABLE soil_history (
    history_id    BIGINT          NOT NULL  COMMENT '历史ID',
    sample_id      BIGINT          NOT NULL  COMMENT '采样ID',
    record_year    INT             NOT NULL  COMMENT '记录年份',
    organic_matter DECIMAL(6,2)  COMMENT '有机质(g/kg)',
    total_nitrogen DECIMAL(5,2)  COMMENT '全氮(g/kg)',
    available_p   DECIMAL(5,2)  COMMENT '有效磷(mg/kg)',
    available_k   DECIMAL(5,2)  COMMENT '速效钾(mg/kg)',
    ph_value      DECIMAL(4,2)   COMMENT 'pH值',
    bulk_density  DECIMAL(5,3)  COMMENT '容重(g/cm³)',
    cec_value     DECIMAL(6,2)  COMMENT '阳离子交换量',
    fertility_score DECIMAL(5,2) COMMENT '肥力综合评分',
    fertility_grade VARCHAR(10)  COMMENT '肥力等级',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    KEY idx_sample (sample_id),
    KEY idx_year (record_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='土壤历史数据表';

-- -----------------------------------------------------------
-- 4.11 地块管理表 (plot)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS plot;
CREATE TABLE plot (
    plot_id       BIGINT          NOT NULL  COMMENT '地块ID',
    plot_no       VARCHAR(50)     NOT NULL  COMMENT '地块编号',
    plot_name     VARCHAR(200)   NOT NULL  COMMENT '地块名称',
    plot_type     VARCHAR(30)     COMMENT '地块类型: PADDY/DRY/FOREST/GRASS/BUILDING',
    area          DECIMAL(10,4)  COMMENT '面积(亩)',
    perimeter     DECIMAL(10,2)  COMMENT '周长(m)',
    boundary_json TEXT            COMMENT '边界坐标JSON',
    center_lat    DECIMAL(10,6)  COMMENT '中心纬度',
    center_lng    DECIMAL(10,6)  COMMENT '中心经度',
    altitude      DECIMAL(10,2)  COMMENT '海拔(m)',
    soil_type     VARCHAR(30)    COMMENT '土壤类型',
    land_use      VARCHAR(50)    COMMENT '土地利用类型',
    owner_name    VARCHAR(100)   COMMENT '承包人',
    owner_phone   VARCHAR(20)    COMMENT '联系电话',
    dept_id       BIGINT          COMMENT '所属区域/部门',
    remark        TEXT            COMMENT '备注',
    is_deleted    TINYINT(1)      DEFAULT 0  COMMENT '逻辑删除',
    created_by    BIGINT,
    created_time  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (plot_id),
    KEY idx_no (plot_no),
    KEY idx_type (plot_type),
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地块管理表';

-- ============================================================
-- 5. 审批业务扩展表（完善版）
-- ============================================================

-- -----------------------------------------------------------
-- 5.1 审批任务主表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_task;
CREATE TABLE zrws_approval_task (
    task_id              BIGINT          NOT NULL  COMMENT '审批任务ID',
    flow_instance_id     VARCHAR(64)              COMMENT 'Flowable流程实例ID',
    biz_type             VARCHAR(30)     NOT NULL  COMMENT '业务类型',
    biz_id               BIGINT                   COMMENT '关联业务ID',
    biz_title            VARCHAR(200)    NOT NULL  COMMENT '业务标题',
    applicant_id         BIGINT          NOT NULL  COMMENT '申请人ID',
    applicant_name       VARCHAR(60)     NOT NULL  COMMENT '申请人姓名',
    applicant_dept       VARCHAR(100)             COMMENT '申请部门',
    cur_step             VARCHAR(60)              COMMENT '当前步骤',
    cur_step_key         VARCHAR(60)              COMMENT '当前步骤Key',
    status               VARCHAR(20)     NOT NULL  COMMENT '状态',
    priority             INT             DEFAULT 0 COMMENT '优先级',
    sla_deadline         DATETIME                 COMMENT 'SLA截止时间',
    biz_data             JSON                     COMMENT '业务数据',
    is_deleted           TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
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
-- 5.2 审批意见表
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
    action               VARCHAR(20)     NOT NULL  COMMENT '操作类型',
    opinion              VARCHAR(500)             COMMENT '审批意见',
    is_deleted           TINYINT(1)      DEFAULT 0,
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id),
    KEY idx_task (task_id),
    KEY idx_approver (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';

-- -----------------------------------------------------------
-- 5.3 审批抄送表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_cc;
CREATE TABLE zrws_approval_cc (
    cc_id                BIGINT          NOT NULL,
    task_id              BIGINT          NOT NULL,
    user_id              BIGINT          NOT NULL,
    user_name            VARCHAR(60),
    cc_reason            VARCHAR(200),
    has_read             TINYINT(1)      DEFAULT 0,
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cc_id),
    KEY idx_task (task_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批抄送表';

-- -----------------------------------------------------------
-- 5.4 流程定义元数据表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_process_definition_meta;
CREATE TABLE zrws_process_definition_meta (
    meta_id              BIGINT          NOT NULL,
    process_key          VARCHAR(64)     NOT NULL  COMMENT '流程Key',
    process_name         VARCHAR(100)    NOT NULL  COMMENT '流程名称',
    steps_json           JSON                      COMMENT '步骤描述',
    sla_hours            INT                      COMMENT '总超时小时',
    enabled              TINYINT(1)      DEFAULT 1 COMMENT '是否启用',
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (meta_id),
    UNIQUE KEY uk_process_key (process_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义元数据';

-- -----------------------------------------------------------
-- 5.5 审批超时升级记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS zrws_approval_escalation;
CREATE TABLE zrws_approval_escalation (
    escalation_id        BIGINT          NOT NULL,
    task_id              BIGINT          NOT NULL,
    original_step        VARCHAR(60),
    escalation_step      VARCHAR(60),
    reason               VARCHAR(200),
    action               VARCHAR(20),
    created_time         DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (escalation_id),
    KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批超时升级记录';

-- ============================================================
-- 6. 初始化数据
-- ============================================================

-- -----------------------------------------------------------
-- 6.1 部门数据
-- -----------------------------------------------------------
INSERT INTO sys_dept (dept_id, parent_id, dept_name, dept_code, dept_type, leader, phone, sort_order) VALUES
(1, 0, '智壤卫士科技', 'HQ', 'HQ', '陈总', '0731-88888888', 0),
(100, 1, '技术部', 'TECH', 'DEPT', '张总工', '0731-88888801', 1),
(101, 1, '外业采集部', 'FIELD', 'DEPT', '李主任', '0731-88888802', 2),
(102, 1, '数据分析部', 'DATA', 'DEPT', '王高工', '0731-88888803', 3),
(103, 1, '装备部', 'EQUIP', 'DEPT', '刘经理', '0731-88888804', 4),
(104, 1, '应急部', 'EMERGENCY', 'DEPT', '赵指挥', '0731-88888805', 5),
(105, 1, '财务部', 'FINANCE', 'DEPT', '周会计', '0731-88888806', 6),
(200, 100, '软件开发组', 'TECH-SW', 'WING', '张总工', '0731-88888811', 10),
(201, 100, '算法组', 'TECH-ALGO', 'WING', '刘工', '0731-88888812', 11),
(300, 101, '望城采集站', 'FIELD-WC', 'BASE', '王工', '0731-88888821', 20),
(301, 101, '岳麓采集站', 'FIELD-YY', 'BASE', '李工', '0731-88888822', 21),
(302, 101, '雨花采集站', 'FIELD-YH', 'BASE', '张工', '0731-88888823', 22);

-- -----------------------------------------------------------
-- 6.2 用户数据
-- -----------------------------------------------------------
INSERT INTO sys_user (user_id, username, password, real_name, dept_id, post, role_key, email, phone) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, '总经理', 'admin', 'admin@zrws.com', '13800138000'),
(1001, 'wang_gis', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '王工', 300, '外业组长', 'operator', 'wang@zrws.com', '13800138001'),
(1002, 'li_gis', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '李工', 300, '外业技术员', 'operator', 'li@zrws.com', '13800138002'),
(1003, 'zhang_gis', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '张工', 301, '外业组长', 'operator', 'zhang@zrws.com', '13800138003'),
(1010, 'liu_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '刘经理', 103, '装备部经理', 'approver', 'liu_mgr@zrws.com', '13800138010'),
(1020, 'zhao_senior', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '赵高工', 102, '高级工程师', 'reviewer', 'zhao@zrws.com', '13800138020'),
(1030, 'chen_dir', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '陈局长', 1, '局长', 'admin', 'chen@zrws.com', '13800138030'),
(1040, 'wang_cmd', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '王指挥', 104, '应急指挥', 'reviewer', 'wang_cmd@zrws.com', '13800138040'),
(1050, 'li_data', '$2a$10$N.zmdr9k7uOCQb376NoUnu', '李工', 102, '数据分析师', 'operator', 'li_data@zrws.com', '13800138050');

-- -----------------------------------------------------------
-- 6.3 字典数据
-- -----------------------------------------------------------
INSERT INTO sys_dict (dict_id, dict_name, dict_code, description, sort_order) VALUES
(1, '任务状态', 'mission_status', '飞行任务状态', 1),
(2, '任务类型', 'mission_type', '飞行任务类型', 2),
(3, '土壤类型', 'soil_type', '土壤类型分类', 3),
(4, '肥力等级', 'fertility_grade', '土壤肥力等级', 4),
(5, '风险等级', 'risk_level', '灾害风险等级', 5),
(6, '审批状态', 'approval_status', '审批流程状态', 6),
(7, '设备状态', 'device_status', '设备状态', 7),
(8, '处理状态', 'process_status', '数据处理状态', 8),
(9, '地块类型', 'plot_type', '地块类型分类', 9),
(10, '审批动作', 'approval_action', '审批操作动作', 10);

INSERT INTO sys_dict_item (item_id, dict_id, item_text, item_value, sort_order, css_class, is_default) VALUES
-- 任务状态
(101, 1, '草稿', 'DRAFT', 1, 'info', 1),
(102, 1, '审批中', 'APPROVED', 2, 'warning', 0),
(103, 1, '飞行中', 'FLYING', 3, 'primary', 0),
(104, 1, '已完成', 'COMPLETED', 4, 'success', 0),
(105, 1, '异常', 'ABNORMAL', 5, 'danger', 0),
(106, 1, '已取消', 'CANCELLED', 6, '', 0),
-- 任务类型
(201, 2, '测绘任务', 'SURVEY', 1, '', 1),
(202, 2, '地形图', 'MAPPING', 2, '', 0),
(203, 2, '植保喷洒', 'SPRAY', 3, '', 0),
(204, 2, '土壤采样', 'SAMPLE', 4, '', 0),
(205, 2, '巡检监测', 'INSPECT', 5, '', 0),
-- 土壤类型
(301, 3, '砂土', 'SANDY', 1, '', 0),
(302, 3, '壤土', 'LOAM', 2, '', 1),
(303, 3, '黏土', 'CLAY', 3, '', 0),
(304, 3, '粉土', 'SILT', 4, '', 0),
-- 肥力等级
(401, 4, '一级', 'I', 1, 'success', 0),
(402, 4, '二级', 'II', 2, 'primary', 0),
(403, 4, '三级', 'III', 3, 'warning', 1),
(404, 4, '四级', 'IV', 4, 'danger', 0),
-- 风险等级
(501, 5, '低风险', 'LOW', 1, 'success', 0),
(502, 5, '中风险', 'MEDIUM', 2, 'warning', 1),
(503, 5, '高风险', 'HIGH', 3, 'danger', 0),
(504, 5, '极高风险', 'CRITICAL', 4, 'danger', 0),
-- 审批状态
(601, 6, '草稿', 'DRAFT', 1, 'info', 1),
(602, 6, '审批中', 'PROCESSING', 2, 'warning', 0),
(603, 6, '已通过', 'PASSED', 3, 'success', 0),
(604, 6, '已驳回', 'REJECTED', 4, 'danger', 0),
(605, 6, '已退回', 'RETURNED', 5, 'warning', 0),
-- 设备状态
(701, 7, '空闲', 'IDLE', 1, 'success', 1),
(702, 7, '作业中', 'ON_MISSION', 2, 'primary', 0),
(703, 7, '维护中', 'MAINTENANCE', 3, 'warning', 0),
(704, 7, '已退役', 'RETIRED', 4, 'info', 0),
-- 处理状态
(801, 8, '待处理', 'PENDING', 1, 'info', 1),
(802, 8, '处理中', 'PROCESSING', 2, 'primary', 0),
(803, 8, '已完成', 'DONE', 3, 'success', 0),
(804, 8, '已失败', 'FAILED', 4, 'danger', 0),
-- 审批动作
(901, 10, '提交', 'SUBMIT', 1, '', 0),
(902, 10, '通过', 'APPROVE', 2, '', 0),
(903, 10, '驳回', 'REJECT', 3, '', 0),
(904, 10, '退回', 'RETURN', 4, '', 0),
(905, 10, '委托', 'DELEGATE', 5, '', 0),
(906, 10, '转交', 'TRANSFER', 6, '', 0);

-- -----------------------------------------------------------
-- 6.4 设备数据
-- -----------------------------------------------------------
INSERT INTO device (device_id, device_code, device_name, device_type, device_model, manufacturer, serial_no, dept_id, status, battery_level, work_hours, location) VALUES
(1, 'UAV-DJI-M350-001', 'DJI M350RTK #001', 'UAV', 'Matrice 350 RTK', '大疆创新', '2023M350001', 300, 'ON_MISSION', 85, 286.5, '望城采集站'),
(2, 'UAV-DJI-M350-002', 'DJI M350RTK #002', 'UAV', 'Matrice 350 RTK', '大疆创新', '2023M350002', 300, 'IDLE', 100, 198.2, '望城采集站'),
(3, 'UAV-DJI-M300-003', 'DJI M300RTK #003', 'UAV', 'Matrice 300 RTK', '大疆创新', '2022M300003', 301, 'IDLE', 92, 412.8, '岳麓采集站'),
(4, 'RTK-ZED-201', '中海达ZED-RTK #201', 'RTK', 'ZED-F9P', '中海达', '2023ZED201', 300, 'IDLE', 100, 0, '望城采集站'),
(5, 'RTK-UNI-202', 'UFO-RTK #202', 'RTK', 'UFO-M1', 'UFO', '2023UFO202', 301, 'MAINTENANCE', 100, 156.3, '岳麓采集站'),
(6, 'CAM-P1-001', '禅思P1 #001', 'CAMERA', 'Zenmuse P1', '大疆创新', '2023P1001', 300, 'ON_MISSION', NULL, 520.6, '望城采集站'),
(7, 'LIDAR-L1-001', '禅思L1 #001', 'SENSOR', 'Zenmuse L1', '大疆创新', '2023L1001', 300, 'IDLE', NULL, 186.2, '望城采集站'),
(8, 'SENSOR-MS-101', '多光谱 #101', 'SENSOR', 'MS600 Pro', '睿铂科技', '2023MS101', 301, 'IDLE', NULL, 98.5, '岳麓采集站');

-- -----------------------------------------------------------
-- 6.5 飞行任务数据
-- -----------------------------------------------------------
INSERT INTO flight_mission (mission_id, mission_no, mission_name, mission_type, area_name, area_address, latitude, longitude, device_id, operator_id, flight_date, start_time, end_time, duration, coverage, altitude, overlap_fwd, overlap_side, photo_count, lidar_points, soil_samples, gps_accuracy_h, gps_accuracy_v, status) VALUES
(1, 'ZRS-2026-0617-001', '望城区乔口镇土壤调查航拍', 'SURVEY', '望城区乔口镇', '湖南省长沙市望城区乔口镇', 28.4567, 112.8352, 1, 1001, '2026-06-17', '2026-06-17 08:30:00', '2026-06-17 09:12:00', 42, 860.5, 120, 0.80, 0.65, 1247, 286000000, 36, '±1.2cm', '±2.1cm', 'COMPLETED'),
(2, 'ZRS-2026-0616-003', '岳麓区莲花镇地形测绘', 'MAPPING', '岳麓区莲花镇', '湖南省长沙市岳麓区莲花镇', 28.3892, 112.7891, 3, 1003, '2026-06-16', '2026-06-16 07:45:00', '2026-06-16 09:15:00', 90, 1250.8, 100, 0.75, 0.60, 2156, 420000000, 0, '±1.5cm', '±2.5cm', 'COMPLETED'),
(3, 'ZRS-2026-0616-002', '雨花区跳马镇植保作业', 'SPRAY', '雨花区跳马镇', '湖南省长沙市雨花区跳马镇', 28.0125, 112.9236, 1, 1001, '2026-06-16', '2026-06-16 06:00:00', '2026-06-16 11:30:00', 330, 680.2, 30, 0.70, 0.55, 0, 0, 0, '±2.0cm', '±3.0cm', 'FLYING'),
(4, 'ZRS-2026-0615-001', '开福区青竹湖生态监测', 'INSPECT', '开福区青竹湖', '湖南省长沙市开福区青竹湖', 28.2568, 112.8456, 8, 1050, '2026-06-15', '2026-06-15 09:00:00', '2026-06-15 11:30:00', 150, 520.3, 80, 0.80, 0.65, 856, 0, 0, '±1.0cm', '±1.8cm', 'COMPLETED'),
(5, 'ZRS-2026-0614-002', '天心区暮云镇土壤采样', 'SAMPLE', '天心区暮云镇', '湖南省长沙市天心区暮云镇', 27.9568, 112.8236, 1, 1002, '2026-06-14', '2026-06-14 08:00:00', '2026-06-14 12:00:00', 240, 320.5, 50, 0.85, 0.70, 0, 0, 48, '±1.3cm', '±2.2cm', 'ABNORMAL');

-- -----------------------------------------------------------
-- 6.6 GPS航迹点数据
-- -----------------------------------------------------------
INSERT INTO gps_track_point (point_id, mission_id, seq_no, latitude, longitude, altitude, speed, heading, record_time, satellites, fix_type, accuracy_h, accuracy_v) VALUES
(1, 1, 1, 28.456720, 112.835210, 120.0, 5.2, 45, '2026-06-17 08:30:01', 18, 'FIXED', 0.012, 0.021),
(2, 1, 2, 28.456735, 112.835325, 120.2, 5.5, 47, '2026-06-17 08:30:02', 18, 'FIXED', 0.012, 0.021),
(3, 1, 3, 28.456750, 112.835440, 119.8, 5.8, 50, '2026-06-17 08:30:03', 18, 'FIXED', 0.013, 0.022),
(4, 1, 4, 28.456768, 112.835560, 120.1, 5.6, 52, '2026-06-17 08:30:04', 17, 'FIXED', 0.014, 0.023),
(5, 1, 5, 28.456785, 112.835680, 120.0, 5.4, 55, '2026-06-17 08:30:05', 17, 'FIXED', 0.013, 0.021),
(6, 1, 6, 28.456800, 112.835800, 119.9, 5.2, 58, '2026-06-17 08:30:06', 18, 'FIXED', 0.012, 0.020),
(7, 1, 7, 28.456815, 112.835920, 120.3, 5.0, 60, '2026-06-17 08:30:07', 18, 'FIXED', 0.012, 0.020),
(8, 1, 8, 28.456830, 112.836040, 120.1, 4.8, 62, '2026-06-17 08:30:08', 18, 'FIXED', 0.011, 0.019);

-- -----------------------------------------------------------
-- 6.7 土壤采样数据
-- -----------------------------------------------------------
INSERT INTO soil_sample (sample_id, sample_code, mission_id, latitude, longitude, depth, soil_type, ph_value, moisture, ec_value, organic_matter, total_nitrogen, available_p, available_k, cec_value, fertility_grade, collector_id, collect_time) VALUES
(1, 'SP-2026-001', 1, 28.45672, 112.83521, 20.0, 'LOAM', 6.8, 32.5, 245, 24.5, 1.42, 18.6, 126.5, 15.8, 'II', 1001, '2026-06-17 10:30:00'),
(2, 'SP-2026-002', 1, 28.45718, 112.83605, 20.0, 'CLAY', 7.2, 45.2, 312, 28.6, 1.62, 22.3, 158.2, 18.5, 'I', 1001, '2026-06-17 10:45:00'),
(3, 'SP-2026-003', 1, 28.45801, 112.83489, 20.0, 'SANDY', 5.9, 18.3, 178, 14.2, 0.85, 8.5, 68.2, 8.6, 'IV', 1001, '2026-06-17 11:00:00'),
(4, 'SP-2026-004', 1, 28.45765, 112.83542, 20.0, 'LOAM', 6.5, 28.6, 220, 22.8, 1.35, 16.2, 115.6, 14.2, 'II', 1001, '2026-06-17 11:15:00'),
(5, 'SP-2026-005', 1, 28.45822, 112.83618, 20.0, 'CLAY', 7.0, 38.2, 280, 26.5, 1.55, 20.8, 142.3, 17.2, 'I', 1001, '2026-06-17 11:30:00'),
(6, 'SP-2026-006', 1, 28.45688, 112.83475, 20.0, 'SANDY', 6.2, 22.5, 195, 16.8, 0.98, 10.2, 85.6, 10.5, 'III', 1001, '2026-06-17 11:45:00');

-- -----------------------------------------------------------
-- 6.8 数据处理进度
-- -----------------------------------------------------------
INSERT INTO data_processing (process_id, mission_id, process_type, stage_name, stage_order, progress, status, start_time, end_time, duration, quality_score, processed_by) VALUES
(1, 1, 'ORTHO', '数据导入', 1, 100, 'DONE', '2026-06-17 09:30:00', '2026-06-17 09:45:00', 900, NULL, 1050),
(2, 1, 'ORTHO', '质量校验', 2, 100, 'DONE', '2026-06-17 09:45:00', '2026-06-17 10:15:00', 1800, 96.5, 1050),
(3, 1, 'ORTHO', '空三解算', 3, 100, 'DONE', '2026-06-17 10:15:00', '2026-06-17 14:30:00', 15300, 92.8, 1050),
(4, 1, 'ORTHO', '3D重建', 4, 86, 'PROCESSING', '2026-06-17 14:30:00', NULL, NULL, NULL, 1050),
(5, 1, 'ORTHO', '坐标转换', 5, 0, 'PENDING', NULL, NULL, NULL, NULL, 1050),
(6, 2, 'ORTHO', '数据导入', 1, 100, 'DONE', '2026-06-16 09:30:00', '2026-06-16 09:50:00', 1200, NULL, 1050),
(7, 2, 'ORTHO', '质量校验', 2, 100, 'DONE', '2026-06-16 09:50:00', '2026-06-16 10:30:00', 2400, 97.2, 1050),
(8, 2, 'ORTHO', '空三解算', 3, 100, 'DONE', '2026-06-16 10:30:00', '2026-06-16 16:00:00', 19800, 94.5, 1050),
(9, 2, 'ORTHO', '3D重建', 4, 100, 'DONE', '2026-06-16 16:00:00', '2026-06-17 08:00:00', 57600, 91.2, 1050),
(10, 2, 'ORTHO', '坐标转换', 5, 100, 'DONE', '2026-06-17 08:00:00', '2026-06-17 08:30:00', 1800, 95.6, 1050);

-- -----------------------------------------------------------
-- 6.9 面积测量数据
-- -----------------------------------------------------------
INSERT INTO area_measurement (measure_id, measure_no, plot_name, owner_name, mission_id, gps_area, registered_area, area_diff, diff_ratio, status, perimeter, accuracy, measurer_id, measure_time) VALUES
(1, 'AM-2026-001', '张三家承包地', '张三', 1, 8.62, 8.50, 0.12, 1.41, 'NORMAL', 386.5, '±0.1%', 1001, '2026-06-17 14:00:00'),
(2, 'AM-2026-002', '村集体水田', '村集体', 1, 156.38, 156.00, 0.38, 0.24, 'NORMAL', 1825.6, '±0.1%', 1001, '2026-06-17 14:30:00'),
(3, 'AM-2026-003', '李四旱地', '李四', 1, 12.15, 15.00, -2.85, -19.00, 'ABNORMAL', 425.2, '±0.1%', 1001, '2026-06-17 15:00:00'),
(4, 'AM-2026-004', '王五菜地', '王五', 1, 5.82, 5.80, 0.02, 0.34, 'NORMAL', 198.6, '±0.1%', 1001, '2026-06-17 15:30:00'),
(5, 'AM-2026-005', '村小学用地', '村小学', 1, 8.15, 8.00, 0.15, 1.88, 'REVIEW', 365.2, '±0.1%', 1001, '2026-06-17 16:00:00');

-- -----------------------------------------------------------
-- 6.10 天气数据
-- -----------------------------------------------------------
INSERT INTO weather_data (weather_id, record_date, station_code, station_name, temperature, humidity, wind_speed, rainfall, visibility, weather_type, data_source) VALUES
(1, '2026-06-17', 'WCS-QKP', '望城乔口站', 28.5, 65, 3.2, 0.0, '良好', '多云', 'AUTO'),
(2, '2026-06-16', 'WCS-QKP', '望城乔口站', 29.2, 58, 2.8, 0.0, '良好', '晴', 'AUTO'),
(3, '2026-06-15', 'WCS-QKP', '望城乔口站', 26.8, 75, 4.5, 12.5, '一般', '小雨', 'AUTO'),
(4, '2026-06-14', 'WCS-QKP', '望城乔口站', 31.5, 45, 5.2, 0.0, '良好', '晴', 'AUTO'),
(5, '2026-06-13', 'WCS-QKP', '望城乔口站', 30.8, 52, 3.8, 0.0, '良好', '晴', 'AUTO');

-- -----------------------------------------------------------
-- 6.11 降雨量数据
-- -----------------------------------------------------------
INSERT INTO rainfall_record (record_id, station_code, station_name, record_date, record_hour, rainfall, duration, intensity, data_type) VALUES
(1, 'RF-WC-001', '望城站#001', '2026-06-15', 8, 3.2, 45, 'LIGHT', 'AUTO'),
(2, 'RF-WC-001', '望城站#001', '2026-06-15', 9, 5.8, 60, 'MODERATE', 'AUTO'),
(3, 'RF-WC-001', '望城站#001', '2026-06-15', 10, 2.5, 30, 'LIGHT', 'AUTO'),
(4, 'RF-WC-001', '望城站#001', '2026-06-15', 14, 8.5, 90, 'HEAVY', 'AUTO'),
(5, 'RF-WC-002', '岳麓站#001', '2026-06-15', 9, 4.2, 50, 'LIGHT', 'AUTO'),
(6, 'RF-WC-002', '岳麓站#001', '2026-06-15', 10, 6.8, 75, 'MODERATE', 'AUTO');

-- -----------------------------------------------------------
-- 6.12 灾害风险数据
-- -----------------------------------------------------------
INSERT INTO disaster_risk (risk_id, mission_id, latitude, longitude, disaster_type, risk_level, hazard_score, vulnerability, exposure, risk_score, area_affected, description, assessor_id, assess_time) VALUES
(1, 1, 28.4575, 112.8362, 'EROSION', 'LOW', 0.25, 0.30, 0.20, 0.28, 5.2, '轻度水土流失，建议植被恢复', 1020, '2026-06-17 16:00:00'),
(2, 1, 28.4580, 112.8345, 'FLOOD', 'MEDIUM', 0.55, 0.45, 0.35, 0.48, 12.8, '低洼地带，雨季需关注排水', 1020, '2026-06-17 16:30:00'),
(3, 1, 28.4568, 112.8370, 'SUBSIDENCE', 'LOW', 0.20, 0.25, 0.15, 0.22, 3.5, '土层稳定，沉降风险低', 1020, '2026-06-17 17:00:00'),
(4, 2, 28.3895, 112.7895, 'LANDSLIDE', 'MEDIUM', 0.50, 0.55, 0.40, 0.52, 8.6, '坡度较大，雨季需监测', 1020, '2026-06-16 18:00:00');

-- -----------------------------------------------------------
-- 6.13 土壤历史数据（2019-2025）
-- -----------------------------------------------------------
INSERT INTO soil_history (history_id, sample_id, record_year, organic_matter, total_nitrogen, available_p, available_k, ph_value, bulk_density, cec_value, fertility_score, fertility_grade) VALUES
-- 采样点1历史数据
(1, 1, 2019, 18.2, 1.05, 8.5, 85.2, 5.6, 1.35, 12.5, 62.5, 'III'),
(2, 1, 2020, 19.0, 1.12, 9.8, 92.5, 5.8, 1.32, 13.2, 65.8, 'III'),
(3, 1, 2021, 20.5, 1.22, 12.5, 98.6, 6.0, 1.28, 13.8, 68.2, 'II'),
(4, 1, 2022, 21.2, 1.28, 14.2, 105.2, 6.2, 1.25, 14.2, 72.5, 'II'),
(5, 1, 2023, 22.5, 1.32, 15.8, 112.5, 6.4, 1.22, 14.8, 76.8, 'II'),
(6, 1, 2024, 23.5, 1.38, 17.2, 120.2, 6.5, 1.18, 15.2, 80.5, 'II'),
(7, 1, 2025, 24.5, 1.42, 18.6, 126.5, 6.8, 1.15, 15.8, 85.2, 'II'),
-- 采样点2历史数据
(8, 2, 2019, 20.5, 1.18, 10.2, 95.5, 6.2, 1.28, 14.5, 68.5, 'III'),
(9, 2, 2022, 24.2, 1.42, 16.8, 128.5, 6.8, 1.18, 16.8, 78.5, 'II'),
(10, 2, 2025, 28.6, 1.62, 22.3, 158.2, 7.2, 1.08, 18.5, 92.5, 'I');

-- -----------------------------------------------------------
-- 6.14 审批流程定义元数据
-- -----------------------------------------------------------
INSERT INTO zrws_process_definition_meta (meta_id, process_key, process_name, steps_json, sla_hours, enabled) VALUES
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

-- -----------------------------------------------------------
-- 6.15 审批任务Mock数据
-- -----------------------------------------------------------
INSERT INTO zrws_approval_task (task_id, flow_instance_id, biz_type, biz_id, biz_title, applicant_id, applicant_name, applicant_dept, cur_step, cur_step_key, status, priority, sla_deadline) VALUES
(100001, 'INST-001', 'STANDARD', 1, '望城区乔口镇测绘任务标准审批', 1001, '王工', '外业采集部', '校核', 'checker', 'PROCESSING', 1, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(100002, 'INST-002', 'MATERIAL', 3001, 'RTK基准站电池采购申请', 1002, '李工', '装备部', '库存核验', 'warehouse_check', 'PROCESSING', 0, DATE_ADD(NOW(), INTERVAL 4 HOUR)),
(100003, 'INST-003', 'DRONE_FLIGHT', 2, '岳麓区莲花镇无人机航拍任务', 1003, '张工', '外业采集部', '空域申请', 'airspace', 'PROCESSING', 2, DATE_ADD(NOW(), INTERVAL 24 HOUR)),
(100004, 'INST-004', 'EMERGENCY', 4002, '应急地质灾害调查', 1040, '王指挥', '应急部', '指挥员批准', 'commander', 'PROCESSING', 2, DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(100005, 'INST-005', 'STANDARD', 4, '数据处理归档申请', 1050, '李工', '数据分析部', NULL, NULL, 'PASSED', 0, NULL);

-- -----------------------------------------------------------
-- 6.16 审批意见Mock数据
-- -----------------------------------------------------------
INSERT INTO zrws_approval_comment (comment_id, task_id, step_key, step_name, approver_id, approver_name, action, opinion) VALUES
(200001, 100001, 'start', '提交申请', 1001, '王工', 'SUBMIT', '请相关部门审批，任务重要性高'),
(200002, 100002, 'dept_approve', '部门审批', 1010, '刘经理', 'APPROVE', '同意采购，确保及时到位'),
(200003, 100003, 'start', '提交报备', 1003, '张工', 'SUBMIT', '申请明日上午8点至12点飞行'),
(200004, 100004, 'emergency_submit', '应急提交', 1040, '王指挥', 'SUBMIT', '灾害现场紧急，需立即飞行'),
(200005, 100005, 'checker', '校核', 1020, '赵高工', 'APPROVE', '数据质量合格，通过');

-- ============================================================
-- 7. 验证查询
-- ============================================================
SELECT '部门数量' as item, COUNT(*) as count FROM sys_dept WHERE is_deleted = 0
UNION ALL SELECT '用户数量', COUNT(*) FROM sys_user WHERE is_deleted = 0
UNION ALL SELECT '设备数量', COUNT(*) FROM device WHERE is_deleted = 0
UNION ALL SELECT '飞行任务', COUNT(*) FROM flight_mission WHERE is_deleted = 0
UNION ALL SELECT '土壤采样', COUNT(*) FROM soil_sample WHERE is_deleted = 0
UNION ALL SELECT 'GPS航迹点', COUNT(*) FROM gps_track_point
UNION ALL SELECT '数据处理', COUNT(*) FROM data_processing WHERE is_deleted = 0
UNION ALL SELECT '面积测量', COUNT(*) FROM area_measurement WHERE is_deleted = 0
UNION ALL SELECT '天气记录', COUNT(*) FROM weather_data WHERE is_deleted = 0
UNION ALL SELECT '降雨记录', COUNT(*) FROM rainfall_record WHERE is_deleted = 0
UNION ALL SELECT '灾害风险', COUNT(*) FROM disaster_risk WHERE is_deleted = 0
UNION ALL SELECT '土壤历史', COUNT(*) FROM soil_history WHERE is_deleted = 0
UNION ALL SELECT '审批任务', COUNT(*) FROM zrws_approval_task WHERE is_deleted = 0
UNION ALL SELECT '审批意见', COUNT(*) FROM zrws_approval_comment WHERE is_deleted = 0
UNION ALL SELECT '流程定义', COUNT(*) FROM zrws_process_definition_meta;
