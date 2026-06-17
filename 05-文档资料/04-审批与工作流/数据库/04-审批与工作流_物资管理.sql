-- ============================================================
-- 智壤卫士 - 物资管理模块 DDL（MySQL 8.0）
-- 模块：04-审批与工作流 扩展
-- 创建日期：2026-06-17
-- 说明：基于 Snowflake 雪花 ID 主键，使用 t_ 前缀
-- ============================================================

-- ------------------------------------------------------------
-- 1. 物资台账表 (t_asset)
-- 描述：对无人机、仪器、车辆等资产全生命周期管理
-- ------------------------------------------------------------
CREATE TABLE `t_asset` (
  `asset_id` BIGINT NOT NULL COMMENT '资产主键（Snowflake雪花ID）',
  `asset_code` VARCHAR(32) NOT NULL COMMENT '资产编号（唯一标识，如 UAV-2026-001）',
  `asset_name` VARCHAR(100) NOT NULL COMMENT '资产名称',
  `category` VARCHAR(10) NOT NULL COMMENT '物资类别：UAV-无人机, INS-测量仪器, VEH-外业车辆, ACC-配件耗材, COM-计算设备, SAF-安全装备',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '型号规格',
  `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '生产厂商',
  `serial_no` VARCHAR(100) DEFAULT NULL COMMENT '出厂序列号',
  `purchase_date` DATE DEFAULT NULL COMMENT '购置日期',
  `warranty_expire` DATE DEFAULT NULL COMMENT '保修到期日期',
  `initial_value` DECIMAL(12,2) DEFAULT NULL COMMENT '初始价值（元）',
  `status` VARCHAR(20) NOT NULL DEFAULT '在库' COMMENT '物资状态：在库/外出中/维修中/待领取/待维护/已报废',
  `current_location` VARCHAR(200) DEFAULT NULL COMMENT '当前位置/保管地点',
  `department_id` BIGINT DEFAULT NULL COMMENT '所属部门ID',
  `responsible_person_id` BIGINT DEFAULT NULL COMMENT '责任人ID',
  `qr_code` VARCHAR(200) DEFAULT NULL COMMENT '二维码/条码信息',
  `photo_url` VARCHAR(500) DEFAULT NULL COMMENT '实物照片URL',
  `remarks` TEXT DEFAULT NULL COMMENT '备注信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_department` (`department_id`),
  KEY `idx_responsible` (`responsible_person_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资台账表';

-- ------------------------------------------------------------
-- 2. 物资申领/外出报备/报废申请表 (t_asset_apply)
-- 描述：统一管理物资领用、外出报备、报废申请
-- ------------------------------------------------------------
CREATE TABLE `t_asset_apply` (
  `apply_id` BIGINT NOT NULL COMMENT '申请主键（Snowflake雪花ID）',
  `apply_no` VARCHAR(32) NOT NULL COMMENT '申请单号（唯一编号）',
  `apply_type` ENUM('领用','外出报备','报废') NOT NULL COMMENT '申请类型：领用/外出报备/报废',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `department_id` BIGINT DEFAULT NULL COMMENT '申请部门ID',
  `asset_ids` JSON NOT NULL COMMENT '物资ID列表（JSON数组，如 [1001, 1002, 1003]）',
  `purpose` VARCHAR(500) DEFAULT NULL COMMENT '申请用途/任务说明',
  `destination` VARCHAR(200) DEFAULT NULL COMMENT '目的地（外出报备时必填）',
  `start_time` DATETIME DEFAULT NULL COMMENT '预计开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '预计结束时间',
  `urgent_level` TINYINT NOT NULL DEFAULT 0 COMMENT '紧急程度：0-普通, 1-紧急, 2-特急',
  `vehicle_plate` VARCHAR(20) DEFAULT NULL COMMENT '车牌号（车辆申请时必填）',
  `flight_area` VARCHAR(200) DEFAULT NULL COMMENT '飞行区域（无人机申请时必填）',
  `expected_return_time` DATETIME DEFAULT NULL COMMENT '预计归还时间',
  `actual_return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `status` VARCHAR(20) NOT NULL DEFAULT '待审批' COMMENT '申请状态：待审批/已批准/已驳回/执行中/超期中/已完成/已取消',
  `workflow_instance_id` VARCHAR(64) DEFAULT NULL COMMENT '关联审批流实例ID',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  `reject_category` VARCHAR(50) DEFAULT NULL COMMENT '驳回原因分类：安全风险/库存不足/资质不符/预算超限/其他',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
  PRIMARY KEY (`apply_id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`apply_type`),
  KEY `idx_workflow` (`workflow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资申领/外出报备/报废申请表';

-- ------------------------------------------------------------
-- 3. 维护保养记录表 (t_asset_maintenance)
-- 描述：记录各项物资的维护、保养、校准、维修
-- ------------------------------------------------------------
CREATE TABLE `t_asset_maintenance` (
  `maintenance_id` BIGINT NOT NULL COMMENT '维护记录主键（Snowflake雪花ID）',
  `asset_id` BIGINT NOT NULL COMMENT '关联资产ID（外键 t_asset.asset_id）',
  `plan_date` DATE NOT NULL COMMENT '计划维护日期',
  `actual_date` DATE DEFAULT NULL COMMENT '实际执行日期',
  `maintenance_type` ENUM('日常检查','定期保养','故障维修','校准检定') NOT NULL COMMENT '维护类型：日常检查/定期保养/故障维修/校准检定',
  `maintenance_content` TEXT DEFAULT NULL COMMENT '维护内容详情',
  `cost` DECIMAL(10,2) DEFAULT NULL COMMENT '维护费用（元）',
  `service_provider` VARCHAR(200) DEFAULT NULL COMMENT '服务提供商',
  `next_maintenance_date` DATE DEFAULT NULL COMMENT '下次维护日期（用于到期提醒）',
  `attachment_urls` JSON DEFAULT NULL COMMENT '附件URL列表（JSON数组）',
  `checker_id` BIGINT DEFAULT NULL COMMENT '检查人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
  PRIMARY KEY (`maintenance_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_plan_date` (`plan_date`),
  KEY `idx_next_date` (`next_maintenance_date`),
  KEY `idx_type` (`maintenance_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护保养记录表';

-- ------------------------------------------------------------
-- 4. 盘点记录表 (t_asset_inventory)
-- 描述：物资资产定期盘点记录
-- ------------------------------------------------------------
CREATE TABLE `t_asset_inventory` (
  `inventory_id` BIGINT NOT NULL COMMENT '盘点记录主键（Snowflake雪花ID）',
  `inventory_date` DATE NOT NULL COMMENT '盘点日期',
  `inventory_type` ENUM('全面盘点','抽盘') NOT NULL COMMENT '盘点类型：全面盘点/抽盘',
  `plan_id` VARCHAR(32) DEFAULT NULL COMMENT '盘点计划编号',
  `checker_id` BIGINT NOT NULL COMMENT '盘点人ID',
  `asset_id` BIGINT NOT NULL COMMENT '关联资产ID（外键 t_asset.asset_id）',
  `book_quantity` INT DEFAULT NULL COMMENT '账面数量（理论数量）',
  `actual_quantity` INT DEFAULT NULL COMMENT '实际盘点数量',
  `result` ENUM('正常','盘盈','盘亏','待报废') DEFAULT NULL COMMENT '盘点结果：正常/盘盈/盘亏/待报废',
  `diff_reason` VARCHAR(500) DEFAULT NULL COMMENT '差异原因说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
  PRIMARY KEY (`inventory_id`),
  KEY `idx_inventory_date` (`inventory_date`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点记录表';

-- ------------------------------------------------------------
-- 5. 使用记录表 (t_asset_usage_log)
-- 描述：物资领用/出库→归还 全链路使用记录
-- ------------------------------------------------------------
CREATE TABLE `t_asset_usage_log` (
  `log_id` BIGINT NOT NULL COMMENT '使用记录主键（Snowflake雪花ID）',
  `asset_id` BIGINT NOT NULL COMMENT '关联资产ID（外键 t_asset.asset_id）',
  `apply_id` BIGINT DEFAULT NULL COMMENT '关联申请ID（外键 t_asset_apply.apply_id）',
  `user_id` BIGINT NOT NULL COMMENT '使用人ID',
  `check_out_time` DATETIME NOT NULL COMMENT '出库/领用时间',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '归还/入库时间',
  `pre_check_status` JSON DEFAULT NULL COMMENT '出库检查详情（JSON：设备外观/功能测试/配件齐全）',
  `post_check_status` JSON DEFAULT NULL COMMENT '归还检查详情（JSON：设备完好度/异常描述）',
  `flight_hours_added` DECIMAL(6,1) DEFAULT NULL COMMENT '本次新增飞行时长（小时，无人机专用）',
  `battery_cycles_added` INT DEFAULT NULL COMMENT '本次新增电池循环次数（无人机专用）',
  `mileage_added` DECIMAL(8,1) DEFAULT NULL COMMENT '本次新增里程（公里，车辆专用）',
  `damage_found` TINYINT NOT NULL DEFAULT 0 COMMENT '损坏情况：0-正常, 1-轻微损坏, 2-严重损坏',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
  PRIMARY KEY (`log_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_check_out` (`check_out_time`),
  KEY `idx_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用记录表';
