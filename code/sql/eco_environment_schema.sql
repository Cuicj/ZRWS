-- =====================================================
-- 生态环境监测模块建表脚本
-- 版本：v1.0
-- 日期：2026-07-01
-- 说明：包含生态标准、气候变暖、沙漠化三张表
-- =====================================================

-- -----------------------------------------------------
-- 1. 生态环境标准数据表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `zrws_eco_standard` (
  `standard_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标准ID',
  `standard_code` VARCHAR(64) NOT NULL COMMENT '标准代码',
  `standard_name` VARCHAR(128) NOT NULL COMMENT '标准名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类: CLIMATE_WARMING-气候变暖, DESERTIFICATION-沙漠化, SOIL_EROSION-水土流失, ECO_SAFETY-生态安全, CLIMATE_ZONE-气候分区',
  `subcategory` VARCHAR(64) DEFAULT NULL COMMENT '子分类',
  `standard_system` VARCHAR(64) DEFAULT NULL COMMENT '标准体系: IPCC/WMO/UNCCD/FAO/GB/SL等',
  `grade_level` VARCHAR(32) DEFAULT NULL COMMENT '等级: MILD-轻度, MODERATE-中度, SEVERE-重度, EXTREME-极重度等',
  `threshold_min` DECIMAL(12,4) DEFAULT NULL COMMENT '阈值下限',
  `threshold_max` DECIMAL(12,4) DEFAULT NULL COMMENT '阈值上限',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `description` TEXT DEFAULT NULL COMMENT '描述说明',
  `indicator_params` JSON DEFAULT NULL COMMENT '指标参数(JSON)',
  `reference_standard` VARCHAR(255) DEFAULT NULL COMMENT '参考标准文号',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用, INACTIVE-停用',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`standard_id`),
  UNIQUE KEY `uk_standard_code` (`standard_code`),
  KEY `idx_category` (`category`),
  KEY `idx_grade_level` (`grade_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生态环境标准数据表';

-- -----------------------------------------------------
-- 2. 气候变暖监测表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `zrws_climate_warming` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `record_code` VARCHAR(64) NOT NULL COMMENT '记录编号',
  `region` VARCHAR(128) DEFAULT NULL COMMENT '区域名称',
  `region_code` VARCHAR(32) DEFAULT NULL COMMENT '区域编码',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `monitor_date` DATE NOT NULL COMMENT '监测日期',
  `avg_temperature` DECIMAL(6,2) DEFAULT NULL COMMENT '平均气温(°C)',
  `max_temperature` DECIMAL(6,2) DEFAULT NULL COMMENT '最高气温(°C)',
  `min_temperature` DECIMAL(6,2) DEFAULT NULL COMMENT '最低气温(°C)',
  `temperature_anomaly` DECIMAL(5,2) DEFAULT NULL COMMENT '温度距平(°C)',
  `precipitation` DECIMAL(8,1) DEFAULT NULL COMMENT '降水量(mm)',
  `precipitation_anomaly` DECIMAL(6,1) DEFAULT NULL COMMENT '降水距平百分率(%)',
  `extreme_high_temp_days` INT DEFAULT 0 COMMENT '高温日数(>35°C)',
  `extreme_low_temp_days` INT DEFAULT 0 COMMENT '低温日数(<-5°C)',
  `drought_days` INT DEFAULT 0 COMMENT '连续干旱日数',
  `heat_wave_events` INT DEFAULT 0 COMMENT '热浪事件次数',
  `warming_rate_10y` DECIMAL(5,3) DEFAULT NULL COMMENT '10年升温速率(°C/10a)',
  `warming_trend` VARCHAR(16) DEFAULT NULL COMMENT '变暖趋势: RAPID-快速, MODERATE-中等, SLOW-缓慢, STABLE-稳定, COOLING-降温',
  `risk_level` VARCHAR(16) DEFAULT NULL COMMENT '风险等级: LOW-低, MEDIUM-中, HIGH-高, EXTREME-极高',
  `risk_score` DECIMAL(5,1) DEFAULT NULL COMMENT '风险评分(0-100)',
  `impact_assessment` TEXT DEFAULT NULL COMMENT '影响评估',
  `adaptation_measures` TEXT DEFAULT NULL COMMENT '适应措施建议',
  `data_source` VARCHAR(64) DEFAULT NULL COMMENT '数据来源',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布, ARCHIVED-已归档',
  `analyst` VARCHAR(64) DEFAULT NULL COMMENT '分析人员',
  `analysis_time` DATETIME DEFAULT NULL COMMENT '分析时间',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_record_code` (`record_code`),
  KEY `idx_monitor_date` (`monitor_date`),
  KEY `idx_region` (`region_code`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='气候变暖监测表';

-- -----------------------------------------------------
-- 3. 沙漠化监测表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `zrws_desertification` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `record_code` VARCHAR(64) NOT NULL COMMENT '记录编号',
  `region` VARCHAR(128) DEFAULT NULL COMMENT '区域名称',
  `region_code` VARCHAR(32) DEFAULT NULL COMMENT '区域编码',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `monitor_date` DATE NOT NULL COMMENT '监测日期',
  `monitor_period` VARCHAR(32) DEFAULT NULL COMMENT '监测周期: MONTHLY-月, QUARTERLY-季, YEARLY-年',
  `vegetation_coverage` DECIMAL(5,2) DEFAULT NULL COMMENT '植被覆盖度(%)',
  `vegetation_trend` VARCHAR(16) DEFAULT NULL COMMENT '植被变化趋势: INCREASING-增加, STABLE-稳定, DECREASING-减少',
  `bare_land_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '裸地面积占比(%)',
  `sand_dune_height_avg` DECIMAL(6,1) DEFAULT NULL COMMENT '沙丘平均高度(m)',
  `sand_dune_migration_rate` DECIMAL(6,1) DEFAULT NULL COMMENT '沙丘移动速率(m/a)',
  `soil_organic_matter` DECIMAL(5,2) DEFAULT NULL COMMENT '土壤有机质含量(%)',
  `soil_moisture` DECIMAL(5,2) DEFAULT NULL COMMENT '土壤含水量(%)',
  `aridity_index` DECIMAL(6,3) DEFAULT NULL COMMENT '干旱指数AI',
  `climate_type` VARCHAR(32) DEFAULT NULL COMMENT '气候类型: HUMID-湿润, SEMI_HUMID-半湿润, SEMI_ARID-半干旱, ARID-干旱, HYPER_ARID-极干旱',
  `wind_erosion_modulus` DECIMAL(10,1) DEFAULT NULL COMMENT '风蚀模数(t/km²·a)',
  `desertification_type` VARCHAR(32) DEFAULT NULL COMMENT '沙漠化类型: WIND-风蚀, WATER-水蚀, SALT-盐渍化, FREEZE-冻融',
  `desertification_grade` VARCHAR(32) DEFAULT NULL COMMENT '沙漠化程度: MILD-轻度, MODERATE-中度, SEVERE-重度, EXTREME-极重度',
  `desertification_area` DECIMAL(10,2) DEFAULT NULL COMMENT '沙漠化面积(km²)',
  `desertification_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '沙漠化占比(%)',
  `land_degradation_index` DECIMAL(5,2) DEFAULT NULL COMMENT '土地退化指数LDI',
  `risk_level` VARCHAR(16) DEFAULT NULL COMMENT '风险等级: LOW-低, MEDIUM-中, HIGH-高, EXTREME-极高',
  `risk_score` DECIMAL(5,1) DEFAULT NULL COMMENT '风险评分(0-100)',
  `impact_assessment` TEXT DEFAULT NULL COMMENT '影响评估',
  `control_measures` TEXT DEFAULT NULL COMMENT '防治措施建议',
  `data_source` VARCHAR(64) DEFAULT NULL COMMENT '数据来源',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布, ARCHIVED-已归档',
  `analyst` VARCHAR(64) DEFAULT NULL COMMENT '分析人员',
  `analysis_time` DATETIME DEFAULT NULL COMMENT '分析时间',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_record_code` (`record_code`),
  KEY `idx_monitor_date` (`monitor_date`),
  KEY `idx_region` (`region_code`),
  KEY `idx_desertification_grade` (`desertification_grade`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沙漠化监测表';
