# 生态环境监测（气候变暖/沙漠化/水土流失）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增生态环境监测模块，包含气候变暖监测、沙漠化监测，并扩展现有灾害风险模块中的水土流失监测功能，提供完整的生态环境评估与预警能力。

**Architecture:** 采用「部分扩展 + 部分新增」的混合架构：
1. **扩展现有灾害风险模块**：丰富水土流失监测的指标与评估维度
2. **新增生态环境监测模块**：包含气候变暖监测、沙漠化监测两个子功能
3. **统一标准数据体系**：扩展现有地质标准库，新增生态环境相关的国际标准、国家标准、安全阈值等基础数据
4. **数据驱动**：先准备标准数据和模拟业务数据，再开发功能界面

**Tech Stack:** Spring Boot 3.2 + MyBatis-Plus + MySQL / Vue 3 + Vite + Element Plus + ECharts

---

## 第一阶段：数据准备与标准体系建设

### Task 1: 确定标准数据清单

**目标**：明确需要哪些世界标准数据、安全数据、基本标准数据

**数据分类清单：**

#### 1.1 世界气象组织（WMO）气候标准数据
| 数据项 | 说明 | 来源标准 |
|--------|------|----------|
| 全球平均温度基准值 | 1850-1900年 pre-industrial 基准 | WMO IPCC AR6 |
| 温度异常阈值 | ±1.5°C, ±2.0°C 警戒线 | IPCC 巴黎协定 |
| 极端高温事件分级 | 轻度/中度/重度/极端 | WMO ETCCDI |
| 极端低温事件分级 | 轻度/中度/重度/极端 | WMO ETCCDI |
| 降水异常指数 | 强降水、连续干日等 | WMO ETCCDI |
| 海平面上升速率 | 全球平均海平面变化 | WMO GLOSS |

#### 1.2 联合国防治荒漠化公约（UNCCD）沙漠化标准
| 数据项 | 说明 | 来源标准 |
|--------|------|----------|
| 沙漠化程度分级 | 轻度/中度/重度/极重度 | UNCCD LADA |
| 植被覆盖度阈值 | <10%, 10-30%, 30-50%, >50% | FAO 荒漠化评价 |
| 土壤侵蚀模数阈值 | t/km²·a 分级 | SL 190-2007 |
| 土地退化指数 | LDI 计算方法 | UNCCD SDG 15.3 |
| 干旱指数 AI | 湿润/半湿润/半干旱/干旱/极干旱 | UNEP 干旱指数 |

#### 1.3 水土流失国家标准数据
| 数据项 | 说明 | 来源标准 |
|--------|------|----------|
| 土壤侵蚀分类分级标准 | 水力/风力/冻融/重力 | SL 190-2007 |
| 水力侵蚀强度分级 | 微度/轻度/中度/强烈/极强烈/剧烈 | SL 190-2007 |
| 风力侵蚀强度分级 | 微度/轻度/中度/强烈/极强烈/剧烈 | SL 190-2007 |
| 水土流失容许值 | 不同土壤类型的容许流失量 | GB/T 15772-2008 |
| 通用土壤流失方程 USLE | R·K·L·S·C·P 因子 | Wischmeier 通用公式 |

#### 1.4 安全数据与预警阈值
| 数据项 | 说明 |
|--------|------|
| 气候变暖预警等级 | 蓝色/黄色/橙色/红色 四级预警 |
| 沙漠化预警等级 | 蓝色/黄色/橙色/红色 四级预警 |
| 水土流失预警等级 | 蓝色/黄色/橙色/红色 四级预警 |
| 生态安全红线指标 | 生态保护红线划定标准 |
| 环境质量安全阈值 | 各项指标的安全范围 |

#### 1.5 基本标准数据（地理/气候分区）
| 数据项 | 说明 |
|--------|------|
| 中国气候分区 | 热带/亚热带/暖温带/中温带/寒温带/高原气候区 |
| 中国干湿分区 | 湿润区/半湿润区/半干旱区/干旱区 |
| 中国植被分区 | 森林/草原/荒漠等八大植被区 |
| 中国土壤侵蚀类型分区 | 水力侵蚀区/风力侵蚀区/冻融侵蚀区 |
| 全球气候类型 | Köppen 气候分类系统 |

- [x] **Step 1: 确认数据清单完整**
  - 已覆盖气候变暖、沙漠化、水土流失三大主题
  - 包含国际标准、国家标准、安全阈值、基础数据四个层次

---

### Task 2: 数据库表结构设计

**Files:**
- Create: `code/sql/eco_environment_schema.sql`

**2.1 生态环境标准数据表 `zrws_eco_standard`**

通用标准数据表，存储各类生态环境标准数据（复用现有 `zrws_geo_standard` 的设计模式）

```sql
CREATE TABLE IF NOT EXISTS `zrws_eco_standard` (
  `standard_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标准ID',
  `standard_code` VARCHAR(64) NOT NULL COMMENT '标准代码',
  `standard_name` VARCHAR(128) NOT NULL COMMENT '标准名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类: CLIMATE_WARMING-气候变暖, DESERTIFICATION-沙漠化, SOIL_EROSION-水土流失, ECO_SAFETY-生态安全, CLIMATE_ZONE-气候分区',
  `subcategory` VARCHAR(64) DEFAULT NULL COMMENT '子分类',
  `standard_system` VARCHAR(64) DEFAULT NULL COMMENT '标准体系: IPCC/WMO/UNCCD/FAO/GB/SL等',
  `grade_level` VARCHAR(32) DEFAULT NULL COMMENT '等级: 轻度/中度/重度/极重度 等',
  `threshold_min` DECIMAL(12,4) DEFAULT NULL COMMENT '阈值下限',
  `threshold_max` DECIMAL(12,4) DEFAULT NULL COMMENT '阈值上限',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `description` TEXT DEFAULT NULL COMMENT '描述说明',
  `indicator_params` JSON DEFAULT NULL COMMENT '指标参数(JSON)',
  `reference_standard` VARCHAR(255) DEFAULT NULL COMMENT '参考标准文号',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态',
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
```

**2.2 气候变暖监测表 `zrws_climate_warming`**

```sql
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
  `warming_trend` VARCHAR(16) DEFAULT NULL COMMENT '变暖趋势: RAPID_FAST-快速, MODERATE-中等, SLOW-缓慢, STABLE-稳定, COOLING-降温',
  `risk_level` VARCHAR(16) DEFAULT NULL COMMENT '风险等级: LOW-低, MEDIUM-中, HIGH-高, EXTREME-极高',
  `risk_score` DECIMAL(5,1) DEFAULT NULL COMMENT '风险评分(0-100)',
  `impact_assessment` TEXT DEFAULT NULL COMMENT '影响评估',
  `adaptation_measures` TEXT DEFAULT NULL COMMENT '适应措施建议',
  `data_source` VARCHAR(64) DEFAULT NULL COMMENT '数据来源',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态',
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
```

**2.3 沙漠化监测表 `zrws_desertification`**

```sql
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
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态',
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
```

**2.4 扩展水土流失监测（扩展现有灾害风险表）**

在现有 `zrws_disaster_risk` 表基础上，通过 `disasterType = SOIL_EROSION` 区分水土流失类型，新增更详细的水土流失指标字段（通过 `monitoringData` JSON 字段扩展，不新增物理列）。

---

### Task 3: 标准数据初始化 SQL

**Files:**
- Create: `code/java/zrws-approval/src/main/resources/sql/init_eco_standard_data.sql`

**3.1 气候变暖标准数据（20+条）**
- IPCC 温度异常分级（5级）
- WMO 极端高温事件分级（4级）
- WMO 极端低温事件分级（4级）
- 气候变暖趋势分级（5级）
- 气候变暖风险等级阈值（4级）
- 气候变暖预警等级（4级）

**3.2 沙漠化标准数据（25+条）**
- UNCCD 沙漠化程度分级（4级）
- 植被覆盖度分级（5级）
- 干旱指数分级（5级）
- 风蚀强度分级（6级）
- 沙漠化类型分类（4类）
- 沙漠化风险等级阈值（4级）
- 沙漠化预警等级（4级）

**3.3 水土流失标准数据（20+条）**
- SL 190-2007 水力侵蚀强度分级（6级）
- SL 190-2007 风力侵蚀强度分级（6级）
- 土壤容许流失量（6类土壤）
- USLE 因子说明（6因子）
- 水土流失预警等级（4级）

**3.4 基础分区数据（15+条）**
- 中国气候分区（6区）
- 中国干湿分区（4区）
- 中国植被分区（8区）
- 中国土壤侵蚀类型分区（3区）

**总计：约 80-100 条标准数据**

---

## 第二阶段：后端开发

### Task 4: 实体类与 Mapper 开发

**Files:**
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/domain/entity/EcoStandard.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/domain/entity/ClimateWarming.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/domain/entity/Desertification.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/mapper/EcoStandardMapper.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/mapper/ClimateWarmingMapper.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/mapper/DesertificationMapper.java`

**设计要点：**
- 复用现有 `DisasterRisk` 实体的设计模式
- 使用 MyBatis-Plus 注解
- 枚举类定义在实体类内部

---

### Task 5: Service 层开发

**Files:**
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/service/EcoStandardService.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/service/ClimateWarmingService.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/service/DesertificationService.java`
- Modify: `code/java/zrws-approval/src/main/java/com/zrws/approval/service/DisasterRiskService.java`

**功能清单：**
- EcoStandardService: 标准数据 CRUD、按分类查询、按等级查询
- ClimateWarmingService: 监测数据 CRUD、统计分析、趋势分析、风险评估
- DesertificationService: 监测数据 CRUD、统计分析、趋势分析、风险评估
- DisasterRiskService: 扩展水土流失相关的查询和分析方法

---

### Task 6: Controller 层开发

**Files:**
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/controller/EcoStandardController.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/controller/ClimateWarmingController.java`
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/controller/DesertificationController.java`
- Modify: `code/java/zrws-approval/src/main/java/com/zrws/approval/controller/DisasterRiskController.java`

**API 清单：**
- GET `/api/v1/eco-standard/list` - 标准数据列表
- GET `/api/v1/eco-standard/{category}` - 按分类查询
- GET `/api/v1/climate-warming/list` - 气候变暖监测列表
- GET `/api/v1/climate-warming/stats` - 统计数据
- GET `/api/v1/climate-warming/trend` - 趋势数据
- GET `/api/v1/desertification/list` - 沙漠化监测列表
- GET `/api/v1/desertification/stats` - 统计数据
- GET `/api/v1/desertification/trend` - 趋势数据

---

### Task 7: 数据初始化器

**Files:**
- Create: `code/java/zrws-approval/src/main/java/com/zrws/approval/config/EcoDataInitializer.java`
- Modify: `code/java/zrws-approval/src/main/java/com/zrws/approval/config/MockDataInitializer.java`

**功能：**
- EcoDataInitializer: 启动时加载生态环境标准数据（幂等性，重复启动不重复插入）
- MockDataInitializer 扩展：新增气候变暖、沙漠化、水土流失的模拟业务数据生成

---

### Task 8: 定时任务扩展

**Files:**
- Modify: `code/java/zrws-approval/src/main/java/com/zrws/approval/scheduler/DailyDataScheduler.java`

**新增任务：**
- 每日生成气候变暖监测数据
- 每日生成沙漠化监测数据
- 每周生成水土流失评估数据

---

## 第三阶段：前端开发

### Task 9: 前端 API 层

**Files:**
- Create: `code/html/src/api/climateWarming.js`
- Create: `code/html/src/api/desertification.js`
- Create: `code/html/src/api/ecoStandard.js`
- Modify: `code/html/src/api/disasterRisk.js`

---

### Task 10: 路由与菜单配置

**Files:**
- Modify: `code/html/src/router/index.js`
- Modify: `code/java/zrws-approval/src/main/java/com/zrws/approval/config/MenuDataInitializer.java`
- Modify: `code/java/zrws-approval/src/main/resources/sql/sys_menu_init.sql`

**新增菜单：**
- 「生态环境」菜单组
  - 气候变暖监测
  - 沙漠化监测
  - 水土流失监测（从灾害风险中移至此处，或保留两处入口）
  - 生态标准库

---

### Task 11: 气候变暖监测页面

**Files:**
- Create: `code/html/src/views/pages/ClimateWarming.vue`

**页面功能：**
1. 顶部统计卡片：
   - 平均气温距平
   - 高温日数
   - 变暖速率
   - 风险区域数

2. 趋势图表（ECharts）：
   - 温度变化趋势折线图
   - 降水变化柱状图
   - 极端事件统计图

3. 监测数据列表：
   - 区域、日期、平均气温、距平、风险等级
   - 支持按时间、区域筛选

4. 详情面板：
   - 温度详情
   - 降水详情
   - 极端事件
   - 影响评估
   - 适应措施建议

---

### Task 12: 沙漠化监测页面

**Files:**
- Create: `code/html/src/views/pages/Desertification.vue`

**页面功能：**
1. 顶部统计卡片：
   - 沙漠化面积
   - 植被覆盖度
   - 沙漠化占比
   - 风险区域数

2. 趋势图表（ECharts）：
   - 植被覆盖度变化趋势
   - 沙漠化程度分布饼图
   - 土地退化指数雷达图

3. 监测数据列表：
   - 区域、日期、植被覆盖度、沙漠化程度、风险等级
   - 支持按时间、区域、程度筛选

4. 详情面板：
   - 植被情况
   - 土壤情况
   - 气候条件
   - 影响评估
   - 防治措施建议

---

### Task 13: 水土流失监测页面（增强版）

**Files:**
- Modify: `code/html/src/views/pages/DisasterRisk.vue`

**增强功能：**
1. 新增水土流失专用 Tab
2. 水力/风力侵蚀强度分布图
3. 土壤流失量趋势图
4. USLE 各因子贡献分析
5. 水土流失治理建议

---

### Task 14: 生态标准库页面

**Files:**
- Create: `code/html/src/views/pages/EcoStandard.vue`

**页面功能：**
1. 分类标签页：气候变暖 / 沙漠化 / 水土流失 / 生态安全 / 气候分区
2. 标准数据表格
3. 详情查看
4. 支持搜索和筛选

---

## 第四阶段：测试与部署

### Task 15: 数据验证与测试

- [ ] 验证标准数据完整性（80+条）
- [ ] 验证模拟数据生成正常
- [ ] 验证后端 API 接口正常
- [ ] 验证前端页面展示正常
- [ ] 验证菜单权限配置正确

### Task 16: 部署上线

- [ ] 执行数据库初始化脚本
- [ ] 后端编译部署
- [ ] 前端构建部署
- [ ] 生产环境验证

---

## 文件结构总览

### 后端新增/修改文件（17个）
```
code/java/zrws-approval/src/main/java/com/zrws/approval/
├── domain/entity/
│   ├── EcoStandard.java          [新增]
│   ├── ClimateWarming.java       [新增]
│   └── Desertification.java      [新增]
├── mapper/
│   ├── EcoStandardMapper.java    [新增]
│   ├── ClimateWarmingMapper.java [新增]
│   └── DesertificationMapper.java[新增]
├── service/
│   ├── EcoStandardService.java   [新增]
│   ├── ClimateWarmingService.java[新增]
│   ├── DesertificationService.java [新增]
│   └── DisasterRiskService.java  [修改]
├── controller/
│   ├── EcoStandardController.java [新增]
│   ├── ClimateWarmingController.java [新增]
│   ├── DesertificationController.java [新增]
│   └── DisasterRiskController.java [修改]
├── config/
│   ├── EcoDataInitializer.java   [新增]
│   ├── MenuDataInitializer.java  [修改]
│   └── MockDataInitializer.java  [修改]
└── scheduler/
    └── DailyDataScheduler.java   [修改]
```

### 前端新增/修改文件（8个）
```
code/html/src/
├── api/
│   ├── climateWarming.js         [新增]
│   ├── desertification.js        [新增]
│   ├── ecoStandard.js            [新增]
│   └── disasterRisk.js           [修改]
├── views/pages/
│   ├── ClimateWarming.vue        [新增]
│   ├── Desertification.vue       [新增]
│   ├── EcoStandard.vue           [新增]
│   └── DisasterRisk.vue          [修改]
└── router/
    └── index.js                  [修改]
```

### SQL 脚本（2个）
```
code/sql/
└── eco_environment_schema.sql    [新增]
code/java/zrws-approval/src/main/resources/sql/
└── init_eco_standard_data.sql    [新增]
```

---

## 实施优先级

| 优先级 | 任务 | 预计工作量 | 依赖 |
|--------|------|-----------|------|
| P0 | Task 1-3: 数据准备与表设计 | 2h | 无 |
| P0 | Task 4-6: 后端核心功能 | 4h | Task 3 |
| P1 | Task 7-8: 数据初始化与定时任务 | 2h | Task 6 |
| P1 | Task 9-10: 前端基础（API+路由+菜单） | 2h | Task 6 |
| P1 | Task 11-12: 气候变暖+沙漠化页面 | 4h | Task 10 |
| P2 | Task 13-14: 水土流失增强+生态标准库 | 3h | Task 11 |
| P2 | Task 15-16: 测试部署 | 2h | Task 14 |
| **总计** | | **~19h** | |
