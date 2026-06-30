-- =====================================================
-- 生态环境标准数据初始化脚本
-- 版本：v1.0
-- 日期：2026-07-01
-- 说明：气候变暖、沙漠化、水土流失、生态安全、气候分区标准数据
-- 总计：104条
-- =====================================================

-- =====================================================
-- 一、气候变暖标准数据（22条）
-- =====================================================

-- 1.1 温度异常分级（5条）- IPCC AR6
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('CW-TA-01', '显著偏冷', 'CLIMATE_WARMING', '温度异常分级', 'IPCC_AR6', 'EXTREME', NULL, -1.0, '°C', '温度距平低于-1.0°C，显著偏冷', 'IPCC Sixth Assessment Report', 1),
('CW-TA-02', '偏冷', 'CLIMATE_WARMING', '温度异常分级', 'IPCC_AR6', 'SEVERE', -1.0, -0.5, '°C', '温度距平-1.0~-0.5°C，偏冷', 'IPCC Sixth Assessment Report', 2),
('CW-TA-03', '正常', 'CLIMATE_WARMING', '温度异常分级', 'IPCC_AR6', 'MILD', -0.5, 0.5, '°C', '温度距平-0.5~0.5°C，正常范围', 'IPCC Sixth Assessment Report', 3),
('CW-TA-04', '偏暖', 'CLIMATE_WARMING', '温度异常分级', 'IPCC_AR6', 'MODERATE', 0.5, 1.0, '°C', '温度距平0.5~1.0°C，偏暖', 'IPCC Sixth Assessment Report', 4),
('CW-TA-05', '显著偏暖', 'CLIMATE_WARMING', '温度异常分级', 'IPCC_AR6', 'EXTREME', 1.0, NULL, '°C', '温度距平高于1.0°C，显著偏暖', 'IPCC Sixth Assessment Report', 5);

-- 1.2 极端高温事件分级（4条）- WMO ETCCDI
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('CW-EH-01', '轻度高温', 'CLIMATE_WARMING', '极端高温分级', 'WMO_ETCCDI', 'MILD', 35.0, 37.0, '°C', '日最高气温35-37°C，轻度高温', 'WMO ETCCDI指标体系', 1),
('CW-EH-02', '中度高温', 'CLIMATE_WARMING', '极端高温分级', 'WMO_ETCCDI', 'MODERATE', 37.0, 40.0, '°C', '日最高气温37-40°C，中度高温', 'WMO ETCCDI指标体系', 2),
('CW-EH-03', '重度高温', 'CLIMATE_WARMING', '极端高温分级', 'WMO_ETCCDI', 'SEVERE', 40.0, 42.0, '°C', '日最高气温40-42°C，重度高温', 'WMO ETCCDI指标体系', 3),
('CW-EH-04', '极端高温', 'CLIMATE_WARMING', '极端高温分级', 'WMO_ETCCDI', 'EXTREME', 42.0, NULL, '°C', '日最高气温>42°C，极端高温', 'WMO ETCCDI指标体系', 4);

-- 1.3 极端低温事件分级（4条）- WMO ETCCDI
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('CW-EL-01', '轻度低温', 'CLIMATE_WARMING', '极端低温分级', 'WMO_ETCCDI', 'MILD', -5.0, 0.0, '°C', '日最低气温-5~0°C，轻度低温', 'WMO ETCCDI指标体系', 1),
('CW-EL-02', '中度低温', 'CLIMATE_WARMING', '极端低温分级', 'WMO_ETCCDI', 'MODERATE', -10.0, -5.0, '°C', '日最低气温-10~-5°C，中度低温', 'WMO ETCCDI指标体系', 2),
('CW-EL-03', '重度低温', 'CLIMATE_WARMING', '极端低温分级', 'WMO_ETCCDI', 'SEVERE', -20.0, -10.0, '°C', '日最低气温-20~-10°C，重度低温', 'WMO ETCCDI指标体系', 3),
('CW-EL-04', '极端低温', 'CLIMATE_WARMING', '极端低温分级', 'WMO_ETCCDI', 'EXTREME', NULL, -20.0, '°C', '日最低气温<-20°C，极端低温', 'WMO ETCCDI指标体系', 4);

-- 1.4 变暖趋势分级（5条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('CW-WT-01', '快速变暖', 'CLIMATE_WARMING', '变暖趋势分级', 'IPCC', 'EXTREME', 0.4, NULL, '°C/10a', '10年升温速率>0.4°C，快速变暖', 'IPCC AR6', 1),
('CW-WT-02', '中等变暖', 'CLIMATE_WARMING', '变暖趋势分级', 'IPCC', 'SEVERE', 0.2, 0.4, '°C/10a', '10年升温速率0.2-0.4°C，中等变暖', 'IPCC AR6', 2),
('CW-WT-03', '缓慢变暖', 'CLIMATE_WARMING', '变暖趋势分级', 'IPCC', 'MODERATE', 0.1, 0.2, '°C/10a', '10年升温速率0.1-0.2°C，缓慢变暖', 'IPCC AR6', 3),
('CW-WT-04', '稳定', 'CLIMATE_WARMING', '变暖趋势分级', 'IPCC', 'MILD', -0.1, 0.1, '°C/10a', '10年升温速率-0.1~0.1°C，基本稳定', 'IPCC AR6', 4),
('CW-WT-05', '降温趋势', 'CLIMATE_WARMING', '变暖趋势分级', 'IPCC', 'LOW', NULL, -0.1, '°C/10a', '10年升温速率<-0.1°C，呈降温趋势', 'IPCC AR6', 5);

-- 1.5 气候变暖风险等级阈值（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `sort_order`) VALUES
('CW-RL-01', '低风险', 'CLIMATE_WARMING', '风险等级', 'CUSTOM', 'LOW', 0, 25, '分', '综合评分0-25分，低风险', 1),
('CW-RL-02', '中风险', 'CLIMATE_WARMING', '风险等级', 'CUSTOM', 'MEDIUM', 26, 50, '分', '综合评分26-50分，中风险', 2),
('CW-RL-03', '高风险', 'CLIMATE_WARMING', '风险等级', 'CUSTOM', 'HIGH', 51, 75, '分', '综合评分51-75分，高风险', 3),
('CW-RL-04', '极高风险', 'CLIMATE_WARMING', '风险等级', 'CUSTOM', 'EXTREME', 76, 100, '分', '综合评分76-100分，极高风险', 4);

-- =====================================================
-- 二、沙漠化标准数据（26条）
-- =====================================================

-- 2.1 沙漠化程度分级（4条）- UNCCD LADA
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('DS-DG-01', '轻度沙漠化', 'DESERTIFICATION', '沙漠化程度分级', 'UNCCD_LADA', 'MILD', 30, 50, '%', '植被覆盖度30-50%，轻度沙漠化', 'UNCCD LADA项目', 1),
('DS-DG-02', '中度沙漠化', 'DESERTIFICATION', '沙漠化程度分级', 'UNCCD_LADA', 'MODERATE', 10, 30, '%', '植被覆盖度10-30%，中度沙漠化', 'UNCCD LADA项目', 2),
('DS-DG-03', '重度沙漠化', 'DESERTIFICATION', '沙漠化程度分级', 'UNCCD_LADA', 'SEVERE', 5, 10, '%', '植被覆盖度5-10%，重度沙漠化', 'UNCCD LADA项目', 3),
('DS-DG-04', '极重度沙漠化', 'DESERTIFICATION', '沙漠化程度分级', 'UNCCD_LADA', 'EXTREME', 0, 5, '%', '植被覆盖度<5%，极重度沙漠化', 'UNCCD LADA项目', 4);

-- 2.2 植被覆盖度分级（5条）- FAO
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('DS-VC-01', '高覆盖度', 'DESERTIFICATION', '植被覆盖度分级', 'FAO', 'HIGH', 70, 100, '%', '植被覆盖度>70%，高覆盖度', 'FAO荒漠化评价标准', 1),
('DS-VC-02', '中高覆盖度', 'DESERTIFICATION', '植被覆盖度分级', 'FAO', 'MODERATE_HIGH', 50, 70, '%', '植被覆盖度50-70%，中高覆盖度', 'FAO荒漠化评价标准', 2),
('DS-VC-03', '中等覆盖度', 'DESERTIFICATION', '植被覆盖度分级', 'FAO', 'MODERATE', 30, 50, '%', '植被覆盖度30-50%，中等覆盖度', 'FAO荒漠化评价标准', 3),
('DS-VC-04', '低覆盖度', 'DESERTIFICATION', '植被覆盖度分级', 'FAO', 'LOW', 10, 30, '%', '植被覆盖度10-30%，低覆盖度', 'FAO荒漠化评价标准', 4),
('DS-VC-05', '极低覆盖度', 'DESERTIFICATION', '植被覆盖度分级', 'FAO', 'VERY_LOW', 0, 10, '%', '植被覆盖度<10%，极低覆盖度', 'FAO荒漠化评价标准', 5);

-- 2.3 干旱指数分级（5条）- UNEP
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('DS-AI-01', '湿润区', 'DESERTIFICATION', '干旱指数分级', 'UNEP', 'HUMID', 1.0, NULL, '', '干旱指数AI>1.0，湿润区', 'UNEP干旱指数分类', 1),
('DS-AI-02', '半湿润区', 'DESERTIFICATION', '干旱指数分级', 'UNEP', 'SEMI_HUMID', 0.65, 1.0, '', '干旱指数AI 0.65-1.0，半湿润区', 'UNEP干旱指数分类', 2),
('DS-AI-03', '半干旱区', 'DESERTIFICATION', '干旱指数分级', 'UNEP', 'SEMI_ARID', 0.3, 0.65, '', '干旱指数AI 0.3-0.65，半干旱区', 'UNEP干旱指数分类', 3),
('DS-AI-04', '干旱区', 'DESERTIFICATION', '干旱指数分级', 'UNEP', 'ARID', 0.13, 0.3, '', '干旱指数AI 0.13-0.3，干旱区', 'UNEP干旱指数分类', 4),
('DS-AI-05', '极干旱区', 'DESERTIFICATION', '干旱指数分级', 'UNEP', 'HYPER_ARID', 0, 0.13, '', '干旱指数AI<0.13，极干旱区', 'UNEP干旱指数分类', 5);

-- 2.4 风蚀强度分级（6条）- SL 190-2007
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('DS-WE-01', '微度风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'MILD', 0, 200, 't/km²·a', '风蚀模数<200 t/km²·a，微度', 'SL 190-2007土壤侵蚀分类分级标准', 1),
('DS-WE-02', '轻度风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'LIGHT', 200, 2500, 't/km²·a', '风蚀模数200-2500 t/km²·a，轻度', 'SL 190-2007土壤侵蚀分类分级标准', 2),
('DS-WE-03', '中度风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'MODERATE', 2500, 5000, 't/km²·a', '风蚀模数2500-5000 t/km²·a，中度', 'SL 190-2007土壤侵蚀分类分级标准', 3),
('DS-WE-04', '强烈风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'SEVERE', 5000, 8000, 't/km²·a', '风蚀模数5000-8000 t/km²·a，强烈', 'SL 190-2007土壤侵蚀分类分级标准', 4),
('DS-WE-05', '极强烈风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'VERY_SEVERE', 8000, 15000, 't/km²·a', '风蚀模数8000-15000 t/km²·a，极强烈', 'SL 190-2007土壤侵蚀分类分级标准', 5),
('DS-WE-06', '剧烈风蚀', 'DESERTIFICATION', '风蚀强度分级', 'SL_190_2007', 'EXTREME', 15000, NULL, 't/km²·a', '风蚀模数>15000 t/km²·a，剧烈', 'SL 190-2007土壤侵蚀分类分级标准', 6);

-- 2.5 沙漠化类型（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `sort_order`) VALUES
('DS-DT-01', '风蚀沙漠化', 'DESERTIFICATION', '沙漠化类型', 'GB', 1),
('DS-DT-02', '水蚀沙漠化', 'DESERTIFICATION', '沙漠化类型', 'GB', 2),
('DS-DT-03', '盐渍化沙漠化', 'DESERTIFICATION', '沙漠化类型', 'GB', 3),
('DS-DT-04', '冻融沙漠化', 'DESERTIFICATION', '沙漠化类型', 'GB', 4);

-- 2.6 沙漠化风险等级阈值（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `sort_order`) VALUES
('DS-RL-01', '低风险', 'DESERTIFICATION', '风险等级', 'CUSTOM', 'LOW', 0, 25, '分', '综合评分0-25分，低风险', 1),
('DS-RL-02', '中风险', 'DESERTIFICATION', '风险等级', 'CUSTOM', 'MEDIUM', 26, 50, '分', '综合评分26-50分，中风险', 2),
('DS-RL-03', '高风险', 'DESERTIFICATION', '风险等级', 'CUSTOM', 'HIGH', 51, 75, '分', '综合评分51-75分，高风险', 3),
('DS-RL-04', '极高风险', 'DESERTIFICATION', '风险等级', 'CUSTOM', 'EXTREME', 76, 100, '分', '综合评分76-100分，极高风险', 4);

-- =====================================================
-- 三、水土流失标准数据（23条）
-- =====================================================

-- 3.1 水力侵蚀强度分级（6条）- SL 190-2007
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('SE-HE-01', '微度水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'MILD', 0, 200, 't/km²·a', '侵蚀模数<200 t/km²·a，微度', 'SL 190-2007土壤侵蚀分类分级标准', 1),
('SE-HE-02', '轻度水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'LIGHT', 200, 2500, 't/km²·a', '侵蚀模数200-2500 t/km²·a，轻度', 'SL 190-2007土壤侵蚀分类分级标准', 2),
('SE-HE-03', '中度水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'MODERATE', 2500, 5000, 't/km²·a', '侵蚀模数2500-5000 t/km²·a，中度', 'SL 190-2007土壤侵蚀分类分级标准', 3),
('SE-HE-04', '强烈水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'SEVERE', 5000, 8000, 't/km²·a', '侵蚀模数5000-8000 t/km²·a，强烈', 'SL 190-2007土壤侵蚀分类分级标准', 4),
('SE-HE-05', '极强烈水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'VERY_SEVERE', 8000, 15000, 't/km²·a', '侵蚀模数8000-15000 t/km²·a，极强烈', 'SL 190-2007土壤侵蚀分类分级标准', 5),
('SE-HE-06', '剧烈水力侵蚀', 'SOIL_EROSION', '水力侵蚀分级', 'SL_190_2007', 'EXTREME', 15000, NULL, 't/km²·a', '侵蚀模数>15000 t/km²·a，剧烈', 'SL 190-2007土壤侵蚀分类分级标准', 6);

-- 3.2 风力侵蚀强度分级（6条）- SL 190-2007（与沙漠化中风蚀相同，独立编码）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('SE-WE-01', '微度风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'MILD', 0, 200, 't/km²·a', '侵蚀模数<200 t/km²·a，微度', 'SL 190-2007土壤侵蚀分类分级标准', 1),
('SE-WE-02', '轻度风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'LIGHT', 200, 2500, 't/km²·a', '侵蚀模数200-2500 t/km²·a，轻度', 'SL 190-2007土壤侵蚀分类分级标准', 2),
('SE-WE-03', '中度风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'MODERATE', 2500, 5000, 't/km²·a', '侵蚀模数2500-5000 t/km²·a，中度', 'SL 190-2007土壤侵蚀分类分级标准', 3),
('SE-WE-04', '强烈风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'SEVERE', 5000, 8000, 't/km²·a', '侵蚀模数5000-8000 t/km²·a，强烈', 'SL 190-2007土壤侵蚀分类分级标准', 4),
('SE-WE-05', '极强烈风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'VERY_SEVERE', 8000, 15000, 't/km²·a', '侵蚀模数8000-15000 t/km²·a，极强烈', 'SL 190-2007土壤侵蚀分类分级标准', 5),
('SE-WE-06', '剧烈风力侵蚀', 'SOIL_EROSION', '风力侵蚀分级', 'SL_190_2007', 'EXTREME', 15000, NULL, 't/km²·a', '侵蚀模数>15000 t/km²·a，剧烈', 'SL 190-2007土壤侵蚀分类分级标准', 6);

-- 3.3 土壤容许流失量（5条）- GB/T 15772-2008
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `threshold_min`, `threshold_max`, `unit`, `description`, `reference_standard`, `sort_order`) VALUES
('SE-ST-01', '黑土容许流失量', 'SOIL_EROSION', '土壤容许流失量', 'GB_T_15772', NULL, 200, 't/km²·a', '东北黑土区容许流失量200 t/km²·a', 'GB/T 15772-2008水土保持综合治理规划通则', 1),
('SE-ST-02', '黄土容许流失量', 'SOIL_EROSION', '土壤容许流失量', 'GB_T_15772', NULL, 1000, 't/km²·a', '黄土高原容许流失量1000 t/km²·a', 'GB/T 15772-2008水土保持综合治理规划通则', 2),
('SE-ST-03', '红壤容许流失量', 'SOIL_EROSION', '土壤容许流失量', 'GB_T_15772', NULL, 500, 't/km²·a', '南方红壤区容许流失量500 t/km²·a', 'GB/T 15772-2008水土保持综合治理规划通则', 3),
('SE-ST-04', '紫色土容许流失量', 'SOIL_EROSION', '土壤容许流失量', 'GB_T_15772', NULL, 500, 't/km²·a', '紫色土区容许流失量500 t/km²·a', 'GB/T 15772-2008水土保持综合治理规划通则', 4),
('SE-ST-05', '风沙土容许流失量', 'SOIL_EROSION', '土壤容许流失量', 'GB_T_15772', NULL, 200, 't/km²·a', '风沙土区容许流失量200 t/km²·a', 'GB/T 15772-2008水土保持综合治理规划通则', 5);

-- 3.4 USLE因子说明（6条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `standard_system`, `description`, `reference_standard`, `sort_order`) VALUES
('SE-USLE-R', '降雨侵蚀力因子R', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映降雨对土壤的侵蚀能力，与降雨量、降雨强度相关', 'Wischmeier通用土壤流失方程', 1),
('SE-USLE-K', '土壤可蚀性因子K', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映土壤被侵蚀的难易程度，与土壤质地、有机质等有关', 'Wischmeier通用土壤流失方程', 2),
('SE-USLE-L', '坡长因子L', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映坡长对侵蚀的影响，坡长越长侵蚀越大', 'Wischmeier通用土壤流失方程', 3),
('SE-USLE-S', '坡度因子S', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映坡度对侵蚀的影响，坡度越陡侵蚀越大', 'Wischmeier通用土壤流失方程', 4),
('SE-USLE-C', '植被覆盖因子C', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映植被覆盖对土壤的保护作用，取值0-1', 'Wischmeier通用土壤流失方程', 5),
('SE-USLE-P', '水土保持措施因子P', 'SOIL_EROSION', 'USLE因子', 'USLE', '反映水土保持措施的减蚀作用，取值0-1', 'Wischmeier通用土壤流失方程', 6);

-- =====================================================
-- 四、生态安全与预警标准（12条）
-- =====================================================

-- 4.1 气候变暖预警等级（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `sort_order`) VALUES
('ES-CW-01', '气候变暖蓝色预警', 'ECO_SAFETY', '气候变暖预警', 'BLUE', 26, 50, '分', '综合评分26-50分，蓝色预警，需关注', 1),
('ES-CW-02', '气候变暖黄色预警', 'ECO_SAFETY', '气候变暖预警', 'YELLOW', 51, 65, '分', '综合评分51-65分，黄色预警，需警惕', 2),
('ES-CW-03', '气候变暖橙色预警', 'ECO_SAFETY', '气候变暖预警', 'ORANGE', 66, 80, '分', '综合评分66-80分，橙色预警，需防范', 3),
('ES-CW-04', '气候变暖红色预警', 'ECO_SAFETY', '气候变暖预警', 'RED', 81, 100, '分', '综合评分81-100分，红色预警，紧急应对', 4);

-- 4.2 沙漠化预警等级（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `sort_order`) VALUES
('ES-DS-01', '沙漠化蓝色预警', 'ECO_SAFETY', '沙漠化预警', 'BLUE', 26, 50, '分', '综合评分26-50分，蓝色预警，需关注', 1),
('ES-DS-02', '沙漠化黄色预警', 'ECO_SAFETY', '沙漠化预警', 'YELLOW', 51, 65, '分', '综合评分51-65分，黄色预警，需警惕', 2),
('ES-DS-03', '沙漠化橙色预警', 'ECO_SAFETY', '沙漠化预警', 'ORANGE', 66, 80, '分', '综合评分66-80分，橙色预警，需防范', 3),
('ES-DS-04', '沙漠化红色预警', 'ECO_SAFETY', '沙漠化预警', 'RED', 81, 100, '分', '综合评分81-100分，红色预警，紧急应对', 4);

-- 4.3 水土流失预警等级（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `grade_level`, `threshold_min`, `threshold_max`, `unit`, `description`, `sort_order`) VALUES
('ES-SE-01', '水土流失蓝色预警', 'ECO_SAFETY', '水土流失预警', 'BLUE', 26, 50, '分', '综合评分26-50分，蓝色预警，需关注', 1),
('ES-SE-02', '水土流失黄色预警', 'ECO_SAFETY', '水土流失预警', 'YELLOW', 51, 65, '分', '综合评分51-65分，黄色预警，需警惕', 2),
('ES-SE-03', '水土流失橙色预警', 'ECO_SAFETY', '水土流失预警', 'ORANGE', 66, 80, '分', '综合评分66-80分，橙色预警，需防范', 3),
('ES-SE-04', '水土流失红色预警', 'ECO_SAFETY', '水土流失预警', 'RED', 81, 100, '分', '综合评分81-100分，红色预警，紧急应对', 4);

-- =====================================================
-- 五、基础分区数据（21条）
-- =====================================================

-- 5.1 中国气候分区（6条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `description`, `sort_order`) VALUES
('CZ-CL-01', '热带季风气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在海南岛、雷州半岛、台湾南部，全年高温，分旱雨两季', 1),
('CZ-CL-02', '亚热带季风气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在秦岭-淮河以南，夏季高温多雨，冬季温和少雨', 2),
('CZ-CL-03', '暖温带季风气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在华北地区，夏季高温多雨，冬季寒冷干燥', 3),
('CZ-CL-04', '中温带季风气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在东北地区，夏季温暖短促，冬季寒冷漫长', 4),
('CZ-CL-05', '寒温带季风气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在大兴安岭北部，全年寒冷，冬季漫长', 5),
('CZ-CL-06', '高原山地气候', 'CLIMATE_ZONE', '中国气候分区', '主要分布在青藏高原，气候垂直差异显著', 6);

-- 5.2 中国干湿分区（4条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `description`, `sort_order`) VALUES
('CZ-HU-01', '湿润区', 'CLIMATE_ZONE', '中国干湿分区', '年降水量>800mm，主要分布在秦岭-淮河以南', 1),
('CZ-HU-02', '半湿润区', 'CLIMATE_ZONE', '中国干湿分区', '年降水量400-800mm，主要分布在东北平原、华北平原', 2),
('CZ-HU-03', '半干旱区', 'CLIMATE_ZONE', '中国干湿分区', '年降水量200-400mm，主要分布在内蒙古高原、黄土高原', 3),
('CZ-HU-04', '干旱区', 'CLIMATE_ZONE', '中国干湿分区', '年降水量<200mm，主要分布在新疆、内蒙古西部', 4);

-- 5.3 中国植被分区（8条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `description`, `sort_order`) VALUES
('CZ-VE-01', '寒温带针叶林区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在大兴安岭北部，以兴安落叶松、樟子松为主', 1),
('CZ-VE-02', '温带针阔混交林区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在小兴安岭、长白山，以红松、水曲柳为主', 2),
('CZ-VE-03', '暖温带落叶阔叶林区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在华北地区，以杨树、槐树、栎树为主', 3),
('CZ-VE-04', '亚热带常绿阔叶林区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在长江流域，以樟树、楠竹、茶树为主', 4),
('CZ-VE-05', '热带季雨林区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在海南岛、西双版纳，以榕树、望天树为主', 5),
('CZ-VE-06', '温带草原区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在内蒙古高原，以针茅、羊草为主', 6),
('CZ-VE-07', '温带荒漠区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在新疆、甘肃，以梭梭、红柳、胡杨为主', 7),
('CZ-VE-08', '青藏高原高山植被区', 'CLIMATE_ZONE', '中国植被分区', '主要分布在青藏高原，以高寒草甸、垫状植物为主', 8);

-- 5.4 土壤侵蚀类型分区（3条）
INSERT INTO `zrws_eco_standard` (`standard_code`, `standard_name`, `category`, `subcategory`, `description`, `sort_order`) VALUES
('CZ-ER-01', '水力侵蚀为主区', 'CLIMATE_ZONE', '土壤侵蚀类型分区', '主要分布在东部季风区，以降雨侵蚀为主', 1),
('CZ-ER-02', '风力侵蚀为主区', 'CLIMATE_ZONE', '土壤侵蚀类型分区', '主要分布在西北干旱区，以风蚀沙化为主', 2),
('CZ-ER-03', '冻融侵蚀为主区', 'CLIMATE_ZONE', '土壤侵蚀类型分区', '主要分布在青藏高原、高山区，以冻融作用为主', 3);

-- =====================================================
-- 数据统计：气候变暖22 + 沙漠化26 + 水土流失23 + 生态安全12 + 气候分区21 = 104条
-- =====================================================
