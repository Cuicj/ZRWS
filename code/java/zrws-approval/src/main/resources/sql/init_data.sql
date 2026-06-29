-- ============================================================
-- 智壤卫士 - 初始化数据 SQL
-- 版本: 3.3.2
-- 说明: 包含所有业务表的初始数据，用于首次部署或数据同步
-- ============================================================

-- -------------------------------------------------------
-- 1. 飞行任务表 zrws_flight_mission
-- -------------------------------------------------------
INSERT INTO zrws_flight_mission (mission_code, mission_name, uav_id, operator, flight_time, duration, coverage_area, flight_altitude, overlap_sidelap, overlap_frontlap, photo_count, data_size, gsd, accuracy_horizontal, accuracy_vertical, status, latitude, longitude, weather, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'ZRS-2026-0617-001' AS mission_code, '望城区乔口镇航测' AS mission_name, 'UAV-DJI-M350-003' AS uav_id, '王工' AS operator, '2026-06-17 08:30:00' AS flight_time, 42 AS duration, 860.0 AS coverage_area, 120.0 AS flight_altitude, 0.8 AS overlap_sidelap, 0.65 AS overlap_frontlap, 1247 AS photo_count, 286000000 AS data_size, 36 AS gsd, '±1.2cm' AS accuracy_horizontal, '±2.1cm' AS accuracy_vertical, 'COMPLETED' AS status, 28.45672 AS latitude, 112.83521 AS longitude, '晴' AS weather, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'ZRS-2026-0616-003', '岳麓区莲花镇航测', 'UAV-DJI-M350-002', '李工', '2026-06-16 09:00:00', 58, 1250.0, 100.0, 0.75, 0.6, 2100, 356000000, 48, '±1.0cm', '±1.8cm', 'COMPLETED', 28.38567, 112.78934, '多云', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'ZRS-2026-0616-002', '雨花区跳马镇航测', 'UAV-DJI-M350-003', '王工', '2026-06-16 14:00:00', 35, 680.0, 80.0, 0.85, 0.7, 980, 198000000, 24, '±1.5cm', '±2.5cm', 'PROCESSING', 28.23456, 113.01234, '晴', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'ZRS-2026-0615-001', '开福区青竹湖航测', 'UAV-DJI-M300-001', '张工', '2026-06-15 07:30:00', 28, 520.0, 110.0, 0.8, 0.65, 760, 156000000, 20, '±1.1cm', '±2.0cm', 'COMPLETED', 28.51234, 112.94567, '晴', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'ZRS-2026-0614-002', '天心区暮云镇航测', 'UAV-DJI-M350-002', '李工', '2026-06-14 10:00:00', 15, 320.0, 90.0, 0.7, 0.55, 420, 86000000, 12, '±2.0cm', '±3.5cm', 'ABNORMAL', 28.34567, 113.12345, '阴', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_flight_mission WHERE mission_code = tmp.mission_code);

-- -------------------------------------------------------
-- 2. 土壤采样表 zrws_soil_sample
-- -------------------------------------------------------
INSERT INTO zrws_soil_sample (sample_code, mission_code, location, latitude, longitude, ph_value, moisture_content, ec_value, soil_type, collector, collect_time, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'SP-001' AS sample_code, 'ZRS-2026-0617-001' AS mission_code, '望城区乔口镇' AS location, 28.45672 AS latitude, 112.83521 AS longitude, 6.8 AS ph_value, 0.32 AS moisture_content, 245 AS ec_value, '壤土' AS soil_type, '王工' AS collector, '2026-06-17 10:00:00' AS collect_time, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'SP-002', 'ZRS-2026-0617-001', '望城区乔口镇', 28.45718, 112.83605, 7.2, 0.45, 312, '黏土', '王工', '2026-06-17 10:30:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SP-003', 'ZRS-2026-0617-001', '望城区乔口镇', 28.45801, 112.83489, 5.9, 0.18, 178, '砂土', '李工', '2026-06-17 11:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SP-004', 'ZRS-2026-0616-003', '岳麓区莲花镇', 28.38567, 112.78934, 6.5, 0.28, 198, '壤土', '李工', '2026-06-16 10:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SP-005', 'ZRS-2026-0615-001', '开福区青竹湖', 28.51234, 112.94567, 7.0, 0.35, 265, '黏土', '张工', '2026-06-15 09:00:00', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_soil_sample WHERE sample_code = tmp.sample_code);

-- -------------------------------------------------------
-- 3. 设备管理表 zrws_device
-- -------------------------------------------------------
INSERT INTO zrws_device (device_code, device_name, device_type, manufacturer, model, serial_number, status, location, last_maintenance, next_maintenance, battery_level, firmware_version, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'UAV-DJI-M350-001' AS device_code, '大疆M350 RTK无人机' AS device_name, 'UAV' AS device_type, '大疆创新' AS manufacturer, 'Matrice 350 RTK' AS model, 'SN-M350-001' AS serial_number, 'ONLINE' AS status, '设备库A-01' AS location, '2026-06-01' AS last_maintenance, '2026-07-01' AS next_maintenance, 95 AS battery_level, 'v3.2.1' AS firmware_version, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'UAV-DJI-M350-002', '大疆M350 RTK无人机', 'UAV', '大疆创新', 'Matrice 350 RTK', 'SN-M350-002', 'ONLINE', '设备库A-02', '2026-06-05', '2026-07-05', 88, 'v3.2.1', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'UAV-DJI-M350-003', '大疆M350 RTK无人机', 'UAV', '大疆创新', 'Matrice 350 RTK', 'SN-M350-003', 'MAINTENANCE', '设备库A-03', '2026-05-20', '2026-06-20', 45, 'v3.2.1', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'UAV-DJI-M300-001', '大疆M300 RTK无人机', 'UAV', '大疆创新', 'Matrice 300 RTK', 'SN-M300-001', 'ONLINE', '设备库B-01', '2026-06-10', '2026-07-10', 92, 'v3.1.5', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'UAV-JF-P30-001', '极飞P30农业无人机', 'UAV', '极飞科技', 'P30 2024款', 'SN-P30-001', 'ONLINE', '设备库C-01', '2026-06-15', '2026-07-15', 100, 'v2.8.0', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'GPR-001', '地质雷达探测仪', 'GPR', '瑞典MALA', 'ProEx', 'SN-GPR-001', 'ONLINE', '设备库D-01', '2026-05-25', '2026-06-25', NULL, 'v4.1.2', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'GNSS-001', 'RTK-GNSS测量仪', 'GNSS', '中海达', 'Hi-Target V2', 'SN-GNSS-001', 'ONLINE', '设备库E-01', '2026-06-08', '2026-07-08', NULL, 'v2.3.0', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_device WHERE device_code = tmp.device_code);

-- -------------------------------------------------------
-- 4. 质量校验表 zrws_quality_check
-- -------------------------------------------------------
INSERT INTO zrws_quality_check (check_code, mission_code, check_type, check_item, check_result, threshold_min, threshold_max, actual_value, status, inspector, check_time, remark, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'QC-2026-0617-001' AS check_code, 'ZRS-2026-0617-001' AS mission_code, '影像质量' AS check_type, '影像清晰度' AS check_item, 'PASS' AS check_result, 0.8 AS threshold_min, 1.0 AS threshold_max, 0.92 AS actual_value, 'COMPLETED' AS status, '李工' AS inspector, '2026-06-17 16:00:00' AS check_time, '影像质量良好' AS remark, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'QC-2026-0617-002', 'ZRS-2026-0617-001', '位置精度', '平面精度', 'PASS', 0.0, 0.05, 0.012 AS actual_value, 'COMPLETED', '李工', '2026-06-17 16:30:00', '符合规范要求', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'QC-2026-0616-001', 'ZRS-2026-0616-003', '影像质量', '航向重叠度', 'PASS', 0.65, 1.0, 0.75 AS actual_value, 'COMPLETED', '王工', '2026-06-16 18:00:00', '符合要求', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'QC-2026-0616-002', 'ZRS-2026-0616-003', '位置精度', '高程精度', 'WARNING', 0.0, 0.1, 0.085 AS actual_value, 'COMPLETED', '王工', '2026-06-16 18:30:00', '略超规范但可接受', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'QC-2026-0615-001', 'ZRS-2026-0615-001', '数据完整性', '照片数量', 'PASS', 700, 900, 760 AS actual_value, 'COMPLETED', '张工', '2026-06-15 17:00:00', '数据完整', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_quality_check WHERE check_code = tmp.check_code);

-- -------------------------------------------------------
-- 5. 公告分类表 zrws_announcement_category
-- -------------------------------------------------------
INSERT INTO zrws_announcement_category (category_name, category_code, description, sort_order, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT '行业政策' AS category_name, 'CATEGORY_001' AS category_code, '土地资源相关政策法规' AS description, 1 AS sort_order, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT '技术动态', 'CATEGORY_002', '测绘地理信息技术发展', 2, 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '项目进展', 'CATEGORY_003', '土地调查项目进展通报', 3, 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '学术交流', 'CATEGORY_004', '学术会议、培训通知', 4, 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '行业报告', 'CATEGORY_005', '土地资源分析报告', 5, 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_announcement_category WHERE category_code = tmp.category_code);

-- -------------------------------------------------------
-- 6. 公告表 zrws_announcement
-- -------------------------------------------------------
INSERT INTO zrws_announcement (title, content, category_id, author, source, views, is_top, is_published, publish_time, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT '自然资源部发布2026年度土地变更调查通知' AS title, '根据自然资源部工作部署，2026年度全国土地变更调查工作将于3月份全面启动...' AS content, 1 AS category_id, '管理员' AS author, '自然资源部', 1256 AS views, 1 AS is_top, 1 AS is_published, '2026-06-20 09:00:00' AS publish_time, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT '无人机遥感技术在土地调查中的应用研讨会', '由中国测绘学会主办的无人机遥感技术应用研讨会将于7月在长沙召开...', 4, '学会秘书处', '中国测绘学会', 892, 0, 1, '2026-06-18 14:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '湖南省2026年耕地质量监测报告发布', '湖南省自然资源厅发布2026年上半年耕地质量监测报告...' FROM DUAL
    UNION ALL SELECT 'AI技术赋能土壤分类识别研究取得突破', '近日，由多家科研机构联合开展的AI土壤分类识别研究取得重要进展...', 2, '研发部', '内部', 1567, 0, 1, '2026-06-15 10:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '望城区乔口镇航测项目顺利完成', '望城区乔口镇三维地形航测项目于6月17日顺利完成数据采集...', 3, '项目组', '内部', 678, 0, 1, '2026-06-17 18:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '《土地管理法实施条例》最新修订解读', '国务院近日发布《土地管理法实施条例》修订版，主要涉及...', 1, '政策法规部', '政策法规', 2341, 1, 1, '2026-06-10 08:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '2026年第二季度土地市场监测分析报告', '全国主要城市2026年第二季度土地市场监测分析报告已发布...', 5, '分析中心', '分析中心', 789, 0, 1, '2026-06-25 09:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT '极飞P30农业无人机在土地测量中的应用', '极飞P30农业无人机在湖南省多个土地测量项目中得到应用...', 2, '技术部', '内部', 456, 0, 1, '2026-06-12 15:00:00', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_announcement WHERE title = tmp.title AND tenant_id = tmp.tenant_id);

-- -------------------------------------------------------
-- 7. 土质分类表 zrws_soil_classification
-- -------------------------------------------------------
INSERT INTO zrws_soil_classification (classification_code, soil_name, classification_system, parent_category, ph_range, moisture_range, ec_range, color_description, texture, structure, formation, distribution, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'SC-001' AS classification_code, '水稻土' AS soil_name, 'CST' AS classification_system, '人为土' AS parent_category, '5.5-7.0' AS ph_range, '30-50' AS moisture_range, '200-500' AS ec_range, '灰棕色-蓝灰色' AS color_description, '壤质-黏质' AS texture, '团粒-棱柱状' AS structure, '人工水耕熟化' AS formation, '秦岭-淮河以南广大地区' AS distribution, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'SC-002', '红壤', 'CST', '铁铝土', '4.5-5.5', '25-45', '100-300', '红色-棕红色', '黏重', '块状-核状', '脱硅富铝化', '长江以南红壤丘陵区', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-003', '黄棕壤', 'CST', '淋溶土', '5.0-6.5', '28-48', '150-400', '黄棕色-棕黄色', '黏壤质', '棱柱状-块状', '弱富铝化黏化', '长江中下游北亚热带', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-004', '黄壤', 'CST', '铁铝土', '4.5-5.5', '30-50', '100-350', '黄色-蜡黄色', '黏重', '核状-块状', '富铝化黄化', '云贵高原、华南山地', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-005', '紫色土', 'CST', '初育土', '6.5-8.5', '25-45', '200-600', '紫色-紫棕色', '壤质-黏壤质', '粒状-小块状', '紫色岩风化', '四川盆地、云贵川', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_soil_classification WHERE classification_code = tmp.classification_code);

-- -------------------------------------------------------
-- 8. 灾害风险表 zrws_disaster_risk
-- -------------------------------------------------------
INSERT INTO zrws_disaster_risk (risk_code, location, latitude, longitude, disaster_type, risk_level, trigger_factor, affected_area, population_at_risk, potential_loss, monitoring_status, last_inspection, analyst, analysis_time, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'RISK-001' AS risk_code, '望城区乔口镇区域' AS location, 28.45672 AS latitude, 112.83521 AS longitude, '滑坡' AS disaster_type, 'LOW' AS risk_level, '降雨、地形坡度' AS trigger_factor, 5.2 AS affected_area, 120 AS population_at_risk, 500.0 AS potential_loss, '正常' AS monitoring_status, '2026-06-15' AS last_inspection, '张总工' AS analyst, '2026-06-15 10:00:00' AS analysis_time, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'RISK-002', '岳麓区莲花镇区域', 28.38921, 112.76543, '泥石流', 'MEDIUM', '暴雨、植被破坏', 12.8, 350, 1500.0, '重点监测', '2026-06-14', '李工', '2026-06-14 14:00:00', 'T001', 0, NOW(), NOW()
    UNION ALL SELECT 'RISK-003', '雨花区跳马镇区域', 28.23456, 113.01234, '地面沉降', 'LOW', '地下水抽取', 8.5, 80, 300.0, '正常', '2026-06-13', '王工', '2026-06-13 09:00:00', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_disaster_risk WHERE risk_code = tmp.risk_code);

-- -------------------------------------------------------
-- 9. 地块表 zrws_land_plot
-- -------------------------------------------------------
INSERT INTO zrws_land_plot (plot_code, plot_name, plot_type, area, perimeter, land_use_type, soil_type, ownership, location, boundary_coords, tenant_id, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'PLOT-001' AS plot_code, '望城区乔口镇示范田' AS plot_name, '示范农田' AS plot_type, 860.5 AS area, 185.2 AS perimeter, '基本农田' AS land_use_type, '水稻土' AS soil_type, '村集体' AS ownership, '望城区乔口镇盘龙岭村' AS location, 'POLYGON((112.835 28.457,112.837 28.457,112.837 28.458,112.835 28.458,112.835 28.457))' AS boundary_coords, 'T001' AS tenant_id, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'PLOT-002', '岳麓区莲花镇试验区', '试验农田', 1250.0, 156.8, '一般农田', '红壤', '农户承包', '岳麓区莲花镇', 'POLYGON((112.785 28.385,112.790 28.385,112.790 28.390,112.785 28.390,112.785 28.385))', 'T001', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_land_plot WHERE plot_code = tmp.plot_code);

-- -------------------------------------------------------
-- 10. 地质标准表 zrws_geo_standard (土壤部分)
-- -------------------------------------------------------
INSERT INTO zrws_geo_standard (standard_code, standard_name, category, subcategory, classification_system, parent_material, chemical_composition, mineral_composition, physical_properties, formation_environment, distribution, color_description, texture_description, structure_description, hardness_min, hardness_max, density_min, density_max, porosity_min, porosity_max, permeability_min, permeability_max, sort_order, status, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'SC-001' AS standard_code, '水稻土' AS standard_name, 'SOIL_CHINA' AS category, '人为土' AS subcategory, 'CST' AS classification_system, '水稻土纲' AS parent_material, '{"SiO2":58.5,"Al2O3":18.2,"Fe2O3":6.8,"CaO":1.2,"MgO":1.5,"K2O":2.3,"Na2O":0.8,"有机质":3.5,"pH":6.5}' AS chemical_composition, '{"黏土矿物":45,"石英":25,"长石":15,"云母":8,"铁氧化物":5,"其他":2}' AS mineral_composition, '{"质地":"壤土-黏壤土","结构":"团粒结构","容重":1.25,"孔隙度":52}' AS physical_properties, '人工水耕熟化' AS formation_environment, '秦岭-淮河以南广大地区' AS distribution, '灰棕色-蓝灰色' AS color_description, '壤质-黏质' AS texture_description, '团粒-棱柱状' AS structure_description, 2.0 AS hardness_min, 4.0 AS hardness_max, 1.1 AS density_min, 1.4 AS density_max, 45.0 AS porosity_min, 58.0 AS porosity_max, 1e-7 AS permeability_min, 1e-5 AS permeability_max, 1 AS sort_order, 'ACTIVE' AS status, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'SC-002', '红壤', 'SOIL_CHINA', '铁铝土', 'CST', '红壤纲', '{"SiO2":52.3,"Al2O3":22.5,"Fe2O3":10.8,"CaO":0.3,"MgO":0.5,"K2O":1.8,"Na2O":0.3,"有机质":1.5,"pH":4.8}', '{"高岭石":40,"石英":25,"赤铁矿":15,"三水铝石":8,"伊利石":7,"其他":5}', '{"质地":"黏壤土-黏土","结构":"块状结构","容重":1.35,"孔隙度":48}', '脱硅富铝化', '长江以南红壤丘陵区', '红色-棕红色', '黏重', '块状-核状', 3.0, 5.0, 1.2, 1.5, 40.0, 52.0, 1e-8, 1e-6, 2, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-003', '黄棕壤', 'SOIL_CHINA', '淋溶土', 'CST', '黄棕壤纲', '{"SiO2":55.8,"Al2O3":20.3,"Fe2O3":8.2,"CaO":0.8,"MgO":1.2,"K2O":2.1,"Na2O":0.6,"有机质":2.5,"pH":5.8}', '{"伊利石":35,"高岭石":20,"石英":25,"绿泥石":8,"蛭石":7,"其他":5}', '{"质地":"黏壤土","结构":"棱柱状结构","容重":1.3,"孔隙度":50}', '弱富铝化黏化', '长江中下游北亚热带', '黄棕色-棕黄色', '黏壤质', '棱柱状-块状', 2.5, 4.5, 1.2, 1.45, 42.0, 55.0, 1e-7, 1e-5, 3, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-004', '黄壤', 'SOIL_CHINA', '铁铝土', 'CST', '黄壤纲', '{"SiO2":50.2,"Al2O3":24.5,"Fe2O3":9.2,"CaO":0.2,"MgO":0.4,"K2O":1.5,"Na2O":0.2,"有机质":2.0,"pH":4.8}', '{"蛭石":30,"高岭石":25,"石英":20,"针铁矿":12,"伊利石":8,"其他":5}', '{"质地":"黏壤土","结构":"核状结构","容重":1.32,"孔隙度":49}', '富铝化黄化', '云贵高原、华南山地', '黄色-蜡黄色', '黏重', '核状-块状', 2.0, 4.0, 1.15, 1.45, 42.0, 54.0, 1e-7, 1e-6, 4, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-005', '紫色土', 'SOIL_CHINA', '初育土', 'CST', '紫色土纲', '{"SiO2":58.2,"Al2O3":15.6,"Fe2O3":5.8,"CaO":3.5,"MgO":2.2,"K2O":2.8,"Na2O":1.0,"有机质":1.2,"pH":7.5}', '{"长石":30,"石英":25,"云母":15,"方解石":12,"蒙脱石":8,"其他":10}', '{"质地":"壤土-黏壤土","结构":"粒状结构","容重":1.28,"孔隙度":51}', '紫色岩风化', '四川盆地、云贵川', '紫色-紫棕色', '壤质-黏壤质', '粒状-小块状', 2.0, 4.5, 1.2, 1.4, 45.0, 55.0, 1e-6, 1e-4, 5, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-006', '石灰土', 'SOIL_CHINA', '初育土', 'CST', '石灰土纲', '{"SiO2":48.5,"Al2O3":16.8,"Fe2O3":6.5,"CaO":8.5,"MgO":2.5,"K2O":1.8,"Na2O":0.5,"有机质":2.2,"pH":7.8}', '{"方解石":35,"石英":20,"蒙脱石":15,"伊利石":10,"高岭石":8,"其他":12}', '{"质地":"黏壤土","结构":"粒状结构","容重":1.25,"孔隙度":52}', '碳酸盐岩风化', '贵州、广西、云南', '棕色-红棕色', '黏壤质', '粒状-团粒状', 2.5, 4.5, 1.15, 1.35, 48.0, 58.0, 1e-6, 1e-4, 6, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-007', '潮土', 'SOIL_CHINA', '半水成土', 'CST', '潮土纲', '{"SiO2":60.2,"Al2O3":15.8,"Fe2O3":5.5,"CaO":2.5,"MgO":1.8,"K2O":2.2,"Na2O":1.2,"有机质":1.8,"pH":7.2}', '{"石英":30,"长石":20,"蒙脱石":15,"伊利石":12,"方解石":8,"其他":15}', '{"质地":"砂壤土-壤土","结构":"层状结构","容重":1.3,"孔隙度":50}', '河流冲积', '黄淮海平原、长江中下游', '灰黄色-棕色', '砂壤-壤质', '层状-碎块状', 3.0, 5.0, 1.2, 1.45, 45.0, 55.0, 1e-5, 1e-3, 7, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'SC-008', '菜园土', 'SOIL_CHINA', '人为土', 'CST', '肥熟土纲', '{"SiO2":56.8,"Al2O3":17.5,"Fe2O3":6.2,"CaO":2.0,"MgO":1.6,"K2O":2.5,"Na2O":0.9,"有机质":3.8,"pH":6.8}', '{"腐殖质":20,"黏土矿物":35,"石英":20,"长石":10,"磷灰石":5,"其他":10}', '{"质地":"壤土","结构":"团粒结构","容重":1.15,"孔隙度":56}', '人工旱耕熟化', '城郊区、蔬菜基地', '暗棕-黑褐色', '壤质', '团粒状', 2.0, 3.5, 1.05, 1.3, 50.0, 60.0, 1e-5, 1e-3, 8, 'ACTIVE', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_geo_standard WHERE standard_code = tmp.standard_code);

-- -------------------------------------------------------
-- 11. 地质标准表 zrws_geo_standard (岩石部分)
-- -------------------------------------------------------
INSERT INTO zrws_geo_standard (standard_code, standard_name, category, classification_system, chemical_composition, mineral_composition, physical_properties, mechanical_properties, typical_elements, formation_environment, distribution, color_description, texture_description, structure_description, hardness_min, hardness_max, density_min, density_max, porosity_min, porosity_max, permeability_min, permeability_max, sort_order, status, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'ROCK-IG-001' AS standard_code, '花岗岩' AS standard_name, 'ROCK_IGNEOUS' AS category, 'GB_T_17412' AS classification_system, '{"SiO2":72.0,"Al2O3":14.0,"Fe2O3":2.5,"CaO":1.5,"MgO":0.8,"K2O":4.5,"Na2O":3.5}' AS chemical_composition, '{"石英":30,"正长石":40,"斜长石":15,"黑云母":8,"角闪石":5,"其他":2}' AS mineral_composition, '{"结构":"全晶质粗粒结构","构造":"块状构造","产状":"深成岩"}' AS physical_properties, '{"抗压强度":100,"抗拉强度":5,"弹性模量":50,"泊松比":0.25}' AS mechanical_properties, 'Si、K' AS typical_elements, '酸性岩浆结晶分异' AS formation_environment, '中国东南部、华南' AS distribution, '肉红色-灰白色' AS color_description, '粗粒结构' AS texture_description, '块状构造' AS structure_description, 6.0 AS hardness_min, 6.5 AS hardness_max, 2.60 AS density_min, 2.75 AS density_max, 0.5 AS porosity_min, 1.5 AS porosity_max, 1e-9 AS permeability_min, 1e-7 AS permeability_max, 1 AS sort_order, 'ACTIVE' AS status, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'ROCK-IG-002', '玄武岩', 'ROCK_IGNEOUS', 'GB_T_17412', '{"SiO2":48.0,"Al2O3":16.0,"Fe2O3":12.0,"CaO":10.0,"MgO":8.0,"K2O":1.0,"Na2O":3.0}', '{"斜长石":55,"辉石":25,"橄榄石":10,"磁铁矿":5,"玻璃质":5}', '{"结构":"隐晶质斑状结构","构造":"气孔杏仁构造","产状":"喷出岩"}', '{"抗压强度":150,"抗拉强度":8,"弹性模量":70,"泊松比":0.28}', 'Fe、Mg、Ca', '基性岩浆喷出冷凝', '中国西南部、东北', '灰黑色-深灰色', '致密隐晶', '块状-气孔', 5.0, 6.0, 2.80, 3.10, 0.5, 2.0, 1e-10, 1e-8, 2, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-IG-003', '闪长岩', 'ROCK_IGNEOUS', 'GB_T_17412', '{"SiO2":58.0,"Al2O3":17.0,"Fe2O3":7.5,"CaO":6.5,"MgO":3.5,"K2O":2.5,"Na2O":4.5}', '{"斜长石":50,"角闪石":25,"黑云母":10,"石英":5,"辉石":5,"其他":5}', '{"结构":"半自形粒状结构","构造":"块状构造","产状":"浅成岩"}', '{"抗压强度":120,"抗拉强度":6,"弹性模量":60,"泊松比":0.26}', 'Al、Ca、Fe', '中性岩浆侵入', '华北、长江中下游', '灰色-深灰色', '中粒结构', '块状构造', 5.5, 6.0, 2.70, 2.90, 0.5, 1.5, 1e-9, 1e-7, 3, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-SE-001', '石灰岩', 'ROCK_SEDIMENTARY', 'GB_T_17412', '{"CaO":55.0,"MgO":2.0,"SiO2":8.0,"Al2O3":2.5,"Fe2O3":1.0,"烧失量":41.0}', '{"方解石":85,"白云石":8,"石英":5,"黏土矿物":2}', '{"结构":"微晶结构","构造":"层理构造","分类":"化学沉积岩"}', '{"抗压强度":80,"抗拉强度":4,"弹性模量":45,"泊松比":0.24}', 'Ca、Mg', '海相/湖相化学沉积', '分布广泛', '灰色-深灰色', '微晶-鲕粒', '层理-块状', 3.0, 4.0, 2.50, 2.80, 1.0, 5.0, 1e-8, 1e-5, 1, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-SE-002', '砂岩', 'ROCK_SEDIMENTARY', 'GB_T_17412', '{"SiO2":78.0,"Al2O3":8.0,"Fe2O3":2.5,"CaO":3.0,"MgO":1.0,"K2O":2.0,"Na2O":1.5}', '{"石英":65,"长石":15,"岩屑":10,"胶结物":10}', '{"结构":"砂状结构","构造":"层理构造","分类":"陆源碎屑岩"}', '{"抗压强度":60,"抗拉强度":3,"弹性模量":30,"泊松比":0.20}', 'Si', '机械搬运沉积', '分布广泛', '灰黄色-灰白色', '砂粒结构', '层理构造', 2.0, 3.5, 2.20, 2.65, 5.0, 25.0, 1e-5, 1e-2, 2, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-SE-003', '页岩', 'ROCK_SEDIMENTARY', 'GB_T_17412', '{"SiO2":58.0,"Al2O3":16.0,"Fe2O3":5.5,"CaO":3.0,"MgO":2.5,"K2O":3.5,"Na2O":1.0,"有机质":1.5}', '{"黏土矿物":55,"石英":20,"长石":10,"有机质":8,"其他":7}', '{"结构":"泥质结构","构造":"页理构造","分类":"黏土岩"}', '{"抗压强度":40,"抗拉强度":2,"弹性模量":20,"泊松比":0.18}', 'Al、K', '细粒悬浮物沉积', '分布广泛', '黑色-灰绿色', '泥质结构', '页理构造', 2.5, 3.5, 2.40, 2.70, 5.0, 20.0, 1e-9, 1e-7, 3, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-ME-001', '片麻岩', 'ROCK_METAMORPHIC', 'GB_T_17412', '{"SiO2":68.0,"Al2O3":15.5,"Fe2O3":5.0,"CaO":2.5,"MgO":2.0,"K2O":3.5,"Na2O":3.5}', '{"石英":30,"长石":40,"黑云母":12,"角闪石":10,"其他":8}', '{"结构":"鳞片粒状变晶结构","构造":"片麻状构造","变质程度":"区域中高温"}', '{"抗压强度":120,"抗拉强度":6,"弹性模量":55,"泊松比":0.25}', 'Si、Al', '区域变质作用', '秦岭-大别山、华北', '灰白色-浅灰色', '粒状鳞片', '片麻状构造', 5.5, 6.5, 2.60, 2.80, 0.5, 1.5, 1e-9, 1e-7, 1, 'ACTIVE', 0, NOW(), NOW()
    UNION ALL SELECT 'ROCK-ME-002', '大理岩', 'ROCK_METAMORPHIC', 'GB_T_17412', '{"CaO":53.0,"MgO":3.0,"SiO2":3.5,"Al2O3":1.0,"Fe2O3":0.5,"烧失量":42.0}', '{"方解石":75,"白云石":20,"石英":3,"其他":2}', '{"结构":"粒状变晶结构","构造":"块状/条带构造","变质程度":"接触/区域"}', '{"抗压强度":90,"抗拉强度":4.5,"弹性模量":50,"泊松比":0.24}', 'Ca', '碳酸盐岩变质', '云南、四川、湖北', '白色-杂色', '粒状变晶', '块状-条带', 3.0, 4.0, 2.60, 2.85, 0.5, 2.0, 1e-9, 1e-7, 2, 'ACTIVE', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_geo_standard WHERE standard_code = tmp.standard_code);

-- -------------------------------------------------------
-- 12. 岩层分析表 zrws_rock_stratum_analysis
-- -------------------------------------------------------
INSERT INTO zrws_rock_stratum_analysis (analysis_code, mission_id, mission_code, project_name, location, latitude, longitude, analysis_type, data_source, borehole_count, max_depth, stratum_count, stratum_data, lithology_data, structure_data, ai_algorithm, ai_confidence, ai_summary, ai_detail, risk_level, suggestion, analyst, analysis_time, status, is_deleted, gmt_create, gmt_modified)
SELECT * FROM (
    SELECT 'RSA-2026-0617-001' AS analysis_code, 1 AS mission_id, 'ZRS-2026-0617-001' AS mission_code, '乔口镇综合地质勘察' AS project_name, '望城区乔口镇盘龙岭村' AS location, 28.45672 AS latitude, 112.83521 AS longitude, 'COMPREHENSIVE' AS analysis_type, '钻孔+地质雷达' AS data_source, 6 AS borehole_count, 80.0 AS max_depth, 5 AS stratum_count, '[{"depth":"0-3m","lithology":"粉质黏土","thickness":3.0},{"depth":"3-8m","lithology":"砂砾石层","thickness":5.0},{"depth":"8-25m","lithology":"强风化泥岩","thickness":17.0},{"depth":"25-50m","lithology":"中风化泥岩","thickness":25.0},{"depth":"50-80m","lithology":"微风化石灰岩","thickness":30.0}]' AS stratum_data, '[{"name":"粉质黏土层","depth":"0-3m","type":"Q4al+pl","strength":"120kPa","permeability":"低渗透","thickness":3.0},{"name":"砂砾石层","depth":"3-8m","type":"Q2al","strength":"280kPa","permeability":"中渗透","thickness":5.0},{"name":"强风化泥岩","depth":"8-25m","type":"E2s","strength":"350kPa","permeability":"低渗透","thickness":17.0},{"name":"中风化泥岩","depth":"25-50m","type":"E2s","strength":"12MPa","permeability":"微渗透","thickness":25.0},{"name":"微风化石灰岩","depth":"50-80m","type":"C1d","strength":"60MPa","permeability":"微渗透","thickness":30.0}]' AS lithology_data, '{"structure":"单斜构造","dip":"15°","dipDirection":"135°","fractures":2}' AS structure_data, 'DEEP_LEARNING' AS ai_algorithm, 94.8 AS ai_confidence, 'AI分析结果显示：该区域岩层结构较为稳定，从上到下依次为第四系覆盖层、强风化泥岩、中风化泥岩、微风化石灰岩。岩层倾角约15°，整体为单斜构造。50米以下为硬质石灰岩，承载力较高。取样法分析与物探结果吻合度94.2%。' AS ai_summary, '{"boreholeCount":6,"avgRQD":"78.5%","rockQuality":"良好","bearingCapacity":"60MPa","seismicLevel":"VIII度","sampleCount":5,"matchRate":"94.2%"}' AS ai_detail, 'LOW' AS risk_level, '建议在石灰岩地层进行基础施工时注意岩溶发育情况，施工前应进行详细勘察' AS suggestion, '张总工' AS analyst, '2026-06-17 16:00:00' AS analysis_time, 'COMPLETED' AS status, 0 AS is_deleted, NOW() AS gmt_create, NOW() AS gmt_modified
    UNION ALL SELECT 'RSA-2026-0616-001', 2, 'ZRS-2026-0616-001', '莲花镇地质雷达探测', '岳麓区莲花镇', 28.38921, 112.76543, 'GEOPHYSICAL', '地质雷达', 0, 30.0, 4, '[{"depth":"0-2m","lithology":"耕植土","thickness":2.0},{"depth":"2-10m","lithology":"粉质黏土","thickness":8.0},{"depth":"10-20m","lithology":"强风化砂岩","thickness":10.0},{"depth":"20-30m","lithology":"中风化砂岩","thickness":10.0}]', '[{"name":"耕植土层","depth":"0-2m","thickness":2.0},{"name":"粉质黏土层","depth":"2-10m","thickness":8.0},{"name":"强风化砂岩层","depth":"10-20m","thickness":10.0},{"name":"中风化砂岩层","depth":"20-30m","thickness":10.0}]', '{"structure":"层状结构","dip":"10°","dipDirection":"210°"}', 'CNN', 87.3, '地质雷达探测结果显示：该区域存在一条小型正断层（F1），位于地表下15-20米处，断距约2米。岩层整体为砂岩地层，风化程度随深度增加而降低。建议对断层附近区域进行重点监测。', '{"gprFrequency":"500MHz","penetrationDepth":"30m","resolution":"0.5m"}', 'MEDIUM', '建议对F1断层附近区域进行加密监测，防止不均匀沉降', '李工', '2026-06-16 14:00:00', 'COMPLETED', 0, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM zrws_rock_stratum_analysis WHERE analysis_code = tmp.analysis_code);

-- -------------------------------------------------------
-- 完成提示
-- -------------------------------------------------------
-- SELECT '数据初始化完成!' AS message;
