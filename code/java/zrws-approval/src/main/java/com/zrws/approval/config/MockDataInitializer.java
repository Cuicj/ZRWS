package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Mock数据初始化器（已禁用，需设置 zrws.mock.enabled=true 才启用）
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "zrws.mock.enabled", havingValue = "true")
@Order(3)
public class MockDataInitializer implements ApplicationRunner {

    @Autowired
    private FlightMissionMapper flightMissionMapper;
    @Autowired
    private SoilSampleMapper soilSampleMapper;
    @Autowired
    private GpsTrackPointMapper gpsTrackPointMapper;
    @Autowired
    private LandPlotMapper landPlotMapper;
    @Autowired
    private SoilClassificationMapper soilClassificationMapper;
    @Autowired
    private RockStratumAnalysisMapper rockStratumAnalysisMapper;
    @Autowired
    private DisasterRiskMapper disasterRiskMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private QualityCheckMapper qualityCheckMapper;
    @Autowired
    private AnnouncementMapper announcementMapper;
    @Autowired
    private AnnouncementCategoryMapper announcementCategoryMapper;
    @Autowired
    private ApprovalTaskMapper approvalTaskMapper;
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    @Autowired
    private ExternalCompanyMapper externalCompanyMapper;
    @Autowired
    private ApiKeyMapper apiKeyMapper;
    @Autowired
    private SysConfigMapper sysConfigMapper;
    @Autowired
    private ExportTaskMapper exportTaskMapper;
    @Autowired
    private DataStatisticsMapper dataStatisticsMapper;
    @Autowired
    private ClimateWarmingMapper climateWarmingMapper;
    @Autowired
    private DesertificationMapper desertificationMapper;

    @Override
    public void run(ApplicationArguments args) {
        int success = 0;
        int failed = 0;
        String[][] tasks = {
            {"飞行任务", "initFlightMissions"},
            {"土壤采样", "initSoilSamples"},
            {"GPS航迹点", "initGpsTrackPoints"},
            {"地块", "initLandPlots"},
            {"土质分类", "initSoilClassifications"},
            {"外部公司", "initExternalCompanies"},
            {"API密钥", "initApiKeys"},
            {"灾害风险", "initDisasterRisks"},
            {"设备", "initDevices"},
            {"质量校验", "initQualityChecks"},
            {"公告分类", "initAnnouncementCategories"},
            {"公告", "initAnnouncements"},
            {"审批任务", "initApprovalTasks"},
            {"报表模板", "initReportTemplates"},
            {"岩层结构分析", "initRockStratumAnalyses"},
            {"系统配置", "initSysConfigs"},
            {"导出任务", "initExportTasks"},
            {"数据统计", "initDataStatistics"},
            {"气候变暖监测", "initClimateWarmingData"},
            {"沙漠化监测", "initDesertificationData"}
        };
        for (String[] task : tasks) {
            try {
                switch (task[1]) {
                    case "initFlightMissions": initFlightMissions(); break;
                    case "initSoilSamples": initSoilSamples(); break;
                    case "initGpsTrackPoints": initGpsTrackPoints(); break;
                    case "initLandPlots": initLandPlots(); break;
                    case "initSoilClassifications": initSoilClassifications(); break;
                    case "initExternalCompanies": initExternalCompanies(); break;
                    case "initApiKeys": initApiKeys(); break;
                    case "initDisasterRisks": initDisasterRisks(); break;
                    case "initDevices": initDevices(); break;
                    case "initQualityChecks": initQualityChecks(); break;
                    case "initAnnouncementCategories": initAnnouncementCategories(); break;
                    case "initAnnouncements": initAnnouncements(); break;
                    case "initApprovalTasks": initApprovalTasks(); break;
                    case "initReportTemplates": initReportTemplates(); break;
                    case "initRockStratumAnalyses": initRockStratumAnalyses(); break;
                    case "initSysConfigs": initSysConfigs(); break;
                    case "initExportTasks": initExportTasks(); break;
                    case "initDataStatistics": initDataStatistics(); break;
                    case "initClimateWarmingData": initClimateWarmingData(); break;
                    case "initDesertificationData": initDesertificationData(); break;
                }
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("[Mock数据] {}初始化失败: {}", task[0], e.getMessage());
            }
        }
        log.info("[Mock数据] Mock数据初始化完成，成功 {} 项，失败 {} 项", success, failed);
    }

    private void initFlightMissions() {
        Long count = flightMissionMapper.selectCount(new LambdaQueryWrapper<FlightMission>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 飞行任务数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化飞行任务Mock数据...");
        FlightMission[] missions = {
            createMission("ZRS-2026-0617-001", "望城区乔口镇", "UAV-DJI-M350-003", "王工",
                LocalDateTime.of(2026, 6, 17, 8, 30), 42, 860.0, 120.0, 0.8, 0.65,
                1247, 286000000L, 36, "±1.2cm", "±2.1cm",
                FlightMission.Status.COMPLETED.name(), 28.45672, 112.83521, "晴"),
            createMission("ZRS-2026-0616-003", "岳麓区莲花镇", "UAV-DJI-M350-002", "李工",
                LocalDateTime.of(2026, 6, 16, 9, 0), 58, 1250.0, 100.0, 0.75, 0.6,
                2100, 356000000L, 48, "±1.0cm", "±1.8cm",
                FlightMission.Status.COMPLETED.name(), 28.38567, 112.78934, "多云"),
            createMission("ZRS-2026-0616-002", "雨花区跳马镇", "UAV-DJI-M350-003", "王工",
                LocalDateTime.of(2026, 6, 16, 14, 0), 35, 680.0, 80.0, 0.85, 0.7,
                980, 198000000L, 24, "±1.5cm", "±2.5cm",
                FlightMission.Status.PROCESSING.name(), 28.23456, 113.01234, "晴"),
            createMission("ZRS-2026-0615-001", "开福区青竹湖", "UAV-DJI-M300-001", "张工",
                LocalDateTime.of(2026, 6, 15, 7, 30), 28, 520.0, 110.0, 0.8, 0.65,
                760, 156000000L, 20, "±1.1cm", "±2.0cm",
                FlightMission.Status.COMPLETED.name(), 28.51234, 112.94567, "晴"),
            createMission("ZRS-2026-0614-002", "天心区暮云镇", "UAV-DJI-M350-002", "李工",
                LocalDateTime.of(2026, 6, 14, 10, 0), 15, 320.0, 90.0, 0.7, 0.55,
                420, 86000000L, 12, "±2.0cm", "±3.5cm",
                FlightMission.Status.ABNORMAL.name(), 28.34567, 113.12345, "阴")
        };
        for (FlightMission mission : missions) {
            flightMissionMapper.insert(mission);
        }
        log.info("[Mock数据] 飞行任务Mock数据初始化完成，共 {} 条", missions.length);
    }

    private FlightMission createMission(String code, String area, String droneId, String operator,
                                        LocalDateTime flightTime, int duration, double coverage, double altitude,
                                        double forwardOverlap, double sideOverlap, int photoCount, long lidarPoints,
                                        int soilSamples, String accH, String accV, String status,
                                        double lat, double lng, String weather) {
        FlightMission m = new FlightMission();
        m.setMissionCode(code);
        m.setAreaName(area);
        m.setDroneId(droneId);
        m.setDroneModel("DJI Matrice 350 RTK");
        m.setOperator(operator);
        m.setFlightTime(flightTime);
        m.setDuration(duration);
        m.setCoverage(coverage);
        m.setAltitude(altitude);
        m.setForwardOverlap(forwardOverlap);
        m.setSideOverlap(sideOverlap);
        m.setPhotoCount(photoCount);
        m.setLidarPoints(lidarPoints);
        m.setSoilSamples(soilSamples);
        m.setGpsAccuracyH(accH);
        m.setGpsAccuracyV(accV);
        m.setStatus(status);
        m.setCenterLat(lat);
        m.setCenterLng(lng);
        m.setWeather(weather);
        m.setIsDeleted(0);
        return m;
    }

    private void initSoilSamples() {
        Long count = soilSampleMapper.selectCount(new LambdaQueryWrapper<SoilSample>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 土壤采样数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化土壤采样Mock数据...");
        SoilSample[] samples = {
            createSample("SP-001", "ZRS-2026-0617-001", 28.45672, 112.83521, 35.2,
                6.8, 0.32, 245, SoilSample.SoilType.LOAM.name(), "壤土", 3.2, 1.5, 0.8, 2.1,
                "0-30cm", "王工", LocalDateTime.of(2026, 6, 17, 9, 0)),
            createSample("SP-002", "ZRS-2026-0617-001", 28.45718, 112.83605, 32.8,
                7.2, 0.45, 312, SoilSample.SoilType.CLAY.name(), "黏土", 2.8, 1.2, 0.6, 1.8,
                "0-40cm", "王工", LocalDateTime.of(2026, 6, 17, 9, 30)),
            createSample("SP-003", "ZRS-2026-0617-001", 28.45801, 112.83489, 38.5,
                5.9, 0.18, 178, SoilSample.SoilType.SAND.name(), "砂土", 1.5, 0.8, 0.4, 1.2,
                "0-25cm", "王工", LocalDateTime.of(2026, 6, 17, 10, 0)),
            createSample("SP-004", "ZRS-2026-0617-001", 28.45765, 112.83542, 36.1,
                6.5, 0.28, 220, SoilSample.SoilType.LOAM.name(), "壤土", 3.0, 1.4, 0.7, 2.0,
                "0-35cm", "李工", LocalDateTime.of(2026, 6, 17, 10, 30)),
            createSample("SP-005", "ZRS-2026-0617-001", 28.45822, 112.83618, 33.7,
                7.0, 0.38, 280, SoilSample.SoilType.CLAY.name(), "黏土", 2.5, 1.1, 0.5, 1.6,
                "0-30cm", "李工", LocalDateTime.of(2026, 6, 17, 11, 0)),
            createSample("SP-006", "ZRS-2026-0617-001", 28.45688, 112.83475, 37.3,
                6.2, 0.22, 195, SoilSample.SoilType.SAND.name(), "砂土", 1.8, 0.9, 0.5, 1.4,
                "0-20cm", "李工", LocalDateTime.of(2026, 6, 17, 11, 30))
        };
        for (SoilSample sample : samples) {
            soilSampleMapper.insert(sample);
        }
        log.info("[Mock数据] 土壤采样Mock数据初始化完成，共 {} 条", samples.length);
    }

    private SoilSample createSample(String code, String missionCode, double lat, double lng, double elev,
                                    double ph, double moisture, int ec, String soilType, String texture,
                                    double organic, double nitrogen, double phosphorus, double potassium,
                                    String depth, String collector, LocalDateTime collectTime) {
        SoilSample s = new SoilSample();
        s.setSampleCode(code);
        s.setMissionCode(missionCode);
        s.setLatitude(lat);
        s.setLongitude(lng);
        s.setElevation(elev);
        s.setPhValue(ph);
        s.setMoisture(moisture);
        s.setEcValue(ec);
        s.setSoilType(soilType);
        s.setSoilTexture(texture);
        s.setOrganicMatter(organic);
        s.setNitrogen(nitrogen);
        s.setPhosphorus(phosphorus);
        s.setPotassium(potassium);
        s.setDepth(depth);
        s.setCollector(collector);
        s.setCollectTime(collectTime);
        s.setStatus("COMPLETED");
        s.setIsDeleted(0);
        return s;
    }

    private void initGpsTrackPoints() {
        Long count = gpsTrackPointMapper.selectCount(new LambdaQueryWrapper<GpsTrackPoint>());
        if (count != null && count > 0) {
            log.info("[Mock数据] GPS航迹点数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化GPS航迹点Mock数据...");
        String missionCode = "ZRS-2026-0617-001";
        GpsTrackPoint[] points = {
            createTrackPoint(1, missionCode, 28.456720, 112.835210, 120.0, 8.5, 0.0, "08:30:01", 18, "FIXED", 0.012, 0.021, GpsTrackPoint.PointType.TAKEOFF.name()),
            createTrackPoint(2, missionCode, 28.456735, 112.835325, 120.2, 10.2, 45.3, "08:30:02", 19, "FIXED", 0.011, 0.020, GpsTrackPoint.PointType.SCAN.name()),
            createTrackPoint(3, missionCode, 28.456750, 112.835440, 119.8, 12.1, 90.5, "08:30:03", 18, "FIXED", 0.012, 0.021, GpsTrackPoint.PointType.SCAN.name()),
            createTrackPoint(4, missionCode, 28.456768, 112.835560, 120.1, 11.8, 135.2, "08:30:04", 17, "FIXED", 0.013, 0.022, GpsTrackPoint.PointType.SCAN.name()),
            createTrackPoint(5, missionCode, 28.456785, 112.835680, 120.0, 10.5, 180.0, "08:30:05", 18, "FIXED", 0.012, 0.021, GpsTrackPoint.PointType.SCAN.name()),
            createTrackPoint(6, missionCode, 28.457500, 112.836800, 120.0, 9.8, 45.0, "08:31:15", 19, "FIXED", 0.010, 0.018, GpsTrackPoint.PointType.WAYPOINT.name()),
            createTrackPoint(7, missionCode, 28.458200, 112.837500, 120.0, 10.0, 60.0, "08:32:30", 18, "FIXED", 0.011, 0.020, GpsTrackPoint.PointType.WAYPOINT.name()),
            createTrackPoint(8, missionCode, 28.458800, 112.836200, 120.0, 9.5, 120.0, "08:33:45", 17, "FIXED", 0.012, 0.021, GpsTrackPoint.PointType.WAYPOINT.name()),
            createTrackPoint(9, missionCode, 28.458500, 112.835000, 120.0, 8.0, 180.0, "08:34:20", 18, "FIXED", 0.011, 0.020, GpsTrackPoint.PointType.SAMPLE.name()),
            createTrackPoint(10, missionCode, 28.457200, 112.835000, 10.0, 2.5, 270.0, "08:42:15", 19, "FIXED", 0.010, 0.018, GpsTrackPoint.PointType.LANDING.name())
        };
        for (GpsTrackPoint point : points) {
            gpsTrackPointMapper.insert(point);
        }
        log.info("[Mock数据] GPS航迹点Mock数据初始化完成，共 {} 条", points.length);
    }

    private GpsTrackPoint createTrackPoint(int seq, String missionCode, double lat, double lng, double alt,
                                           double speed, double heading, String gpsTime, int satellites,
                                           String fixType, double accH, double accV, String pointType) {
        GpsTrackPoint p = new GpsTrackPoint();
        p.setSequence(seq);
        p.setMissionCode(missionCode);
        p.setLatitude(lat);
        p.setLongitude(lng);
        p.setAltitude(alt);
        p.setSpeed(speed);
        p.setHeading(heading);
        p.setGpsTime(gpsTime);
        p.setSatellites(satellites);
        p.setFixType(fixType);
        p.setAccuracyH(accH);
        p.setAccuracyV(accV);
        p.setPointType(pointType);
        p.setIsDeleted(0);
        return p;
    }

    private void initLandPlots() {
        Long count = landPlotMapper.selectCount(new LambdaQueryWrapper<LandPlot>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 地块数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化地块Mock数据...");
        LandPlot[] plots = {
            createPlot("P-001", "张三家承包地", "张三", "耕地", "旱地", 8.62, 8.5, 0.12,
                LandPlot.Status.NORMAL.name(), "望城区乔口镇", "湖南省", "长沙市", "望城区", "乔口镇", "盘龙岭村",
                28.45672, 112.83521, "壤土", 3.2, "灌溉"),
            createPlot("P-002", "村集体水田", "村集体", "耕地", "水田", 156.38, 156.0, 0.38,
                LandPlot.Status.NORMAL.name(), "望城区乔口镇", "湖南省", "长沙市", "望城区", "乔口镇", "盘龙岭村",
                28.45718, 112.83605, "水稻土", 4.5, "灌溉"),
            createPlot("P-003", "李四旱地", "李四", "耕地", "旱地", 12.15, 15.0, -2.85,
                LandPlot.Status.ABNORMAL.name(), "望城区乔口镇", "湖南省", "长沙市", "望城区", "乔口镇", "盘龙岭村",
                28.45801, 112.83489, "红壤", 2.1, "靠天"),
            createPlot("P-004", "王五菜地", "王五", "耕地", "菜地", 5.82, 5.8, 0.02,
                LandPlot.Status.NORMAL.name(), "望城区乔口镇", "湖南省", "长沙市", "望城区", "乔口镇", "盘龙岭村",
                28.45765, 112.83542, "菜园土", 3.8, "灌溉"),
            createPlot("P-005", "村小学用地", "村小学", "建设用地", "科教用地", 8.15, 8.0, 0.15,
                LandPlot.Status.REVIEW.name(), "望城区乔口镇", "湖南省", "长沙市", "望城区", "乔口镇", "盘龙岭村",
                28.45822, 112.83618, "", 0.0, "")
        };
        for (LandPlot plot : plots) {
            landPlotMapper.insert(plot);
        }
        log.info("[Mock数据] 地块Mock数据初始化完成，共 {} 条", plots.length);
    }

    private LandPlot createPlot(String code, String name, String owner, String landType, String landUse,
                                double gpsArea, double regArea, double diff, String status,
                                String region, String province, String city, String county, String township, String village,
                                double lat, double lng, String soilType, double fertility, String irrigation) {
        LandPlot p = new LandPlot();
        p.setPlotCode(code);
        p.setPlotName(name);
        p.setOwner(owner);
        p.setLandType(landType);
        p.setLandUse(landUse);
        p.setGpsArea(gpsArea);
        p.setRegisteredArea(regArea);
        p.setAreaDiff(diff);
        p.setStatus(status);
        p.setRegion(region);
        p.setProvince(province);
        p.setCity(city);
        p.setCounty(county);
        p.setTownship(township);
        p.setVillage(village);
        p.setCenterLat(lat);
        p.setCenterLng(lng);
        p.setSoilType(soilType);
        p.setFertilityLevel(fertility);
        p.setIrrigationType(irrigation);
        p.setIsDeleted(0);
        return p;
    }

    private void initSoilClassifications() {
        Long count = soilClassificationMapper.selectCount(new LambdaQueryWrapper<SoilClassification>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 土质分类数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化土质分类Mock数据...");
        SoilClassification[] classifications = {
            createSoilClassification("SC-2026-0617-001", "ZRS-2026-0617-001", "乔口镇土壤分类", 36,
                SoilClassification.SoilType.PADDY_SOIL.name(), "潴育型水稻土", 96.5,
                "土质疏松，富含有机质，适合水稻种植", 6.5, 3.2, 34.5, 1.8, 0.9, 2.5,
                "壤土", "团粒结构", "灰棕色", 30.0, "第四纪红色黏土", "水稻",
                "AI基于光谱+理化数据综合分析，置信度96.5%", "建议加强有机肥施用，维持土壤肥力",
                "COMPLETED", "李工", LocalDateTime.of(2026, 6, 17, 12, 0)),
            createSoilClassification("SC-2026-0617-002", "ZRS-2026-0617-001", "乔口镇菜地分类", 12,
                SoilClassification.SoilType.GARDEN_SOIL.name(), "菜园土", 93.2,
                "经过长期耕作改良，肥力较高", 6.8, 2.8, 31.2, 2.1, 1.1, 2.8,
                "壤土", "团粒结构", "暗棕色", 25.0, "人工熟化", "蔬菜",
                "AI基于图像+理化数据分析，置信度93.2%", "建议合理轮作，防止土传病害",
                "COMPLETED", "王工", LocalDateTime.of(2026, 6, 17, 14, 0)),
            createSoilClassification("SC-2026-0616-001", "ZRS-2026-0616-003", "莲花镇土壤分类", 48,
                SoilClassification.SoilType.YELLOW_BROWN_EARTH.name(), "黄棕壤", 91.8,
                "中等肥力，适合旱作和果树种植", 6.4, 2.5, 28.9, 1.5, 0.7, 2.0,
                "黏壤土", "块状结构", "黄棕色", 40.0, "花岗岩风化物", "茶树/果树",
                "AI综合分析，置信度91.8%", "建议增施有机肥，改良土壤结构",
                "COMPLETED", "张工", LocalDateTime.of(2026, 6, 16, 16, 0))
        };
        for (SoilClassification sc : classifications) {
            soilClassificationMapper.insert(sc);
        }
        log.info("[Mock数据] 土质分类Mock数据初始化完成，共 {} 条", classifications.length);
    }

    private SoilClassification createSoilClassification(String code, String missionCode, String name, int sampleCount,
                                                         String soilType, String subtype, double confidence,
                                                         String description, double ph, double organic, double moisture,
                                                         double nitrogen, double phosphorus, double potassium,
                                                         String texture, String structure, String color, double depth,
                                                         String parentMaterial, String vegetation,
                                                         String aiAnalysis, String aiSuggestion,
                                                         String status, String analyst, LocalDateTime analysisTime) {
        SoilClassification sc = new SoilClassification();
        sc.setAnalysisCode(code);
        sc.setMissionCode(missionCode);
        sc.setAnalysisName(name);
        sc.setSampleCount(sampleCount);
        sc.setSoilType(soilType);
        sc.setSoilSubtype(subtype);
        sc.setConfidence(confidence);
        sc.setDescription(description);
        sc.setPhValue(ph);
        sc.setOrganicMatter(organic);
        sc.setMoisture(moisture);
        sc.setNitrogen(nitrogen);
        sc.setPhosphorus(phosphorus);
        sc.setPotassium(potassium);
        sc.setTexture(texture);
        sc.setStructure(structure);
        sc.setColor(color);
        sc.setDepth(depth);
        sc.setParentMaterial(parentMaterial);
        sc.setVegetation(vegetation);
        sc.setAiAnalysis(aiAnalysis);
        sc.setAiSuggestion(aiSuggestion);
        sc.setStatus(status);
        sc.setAnalyst(analyst);
        sc.setAnalysisTime(analysisTime);
        sc.setIsDeleted(0);
        return sc;
    }

    private void initRockStratumAnalyses() {
        Long count = rockStratumAnalysisMapper.selectCount(new LambdaQueryWrapper<RockStratumAnalysis>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 岩层结构分析数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化岩层结构分析Mock数据...");
        RockStratumAnalysis[] analyses = {
            createRockAnalysis("RSA-2026-0617-001", "ZRS-2026-0617-001", "乔口镇综合地质勘察",
                "望城区乔口镇盘龙岭村", 28.45672, 112.83521, 35.2,
                RockStratumAnalysis.AnalysisType.COMPREHENSIVE.name(), "钻孔+地质雷达", 6, 80.0, 5,
                "[{\"depth\":\"0-3m\",\"lithology\":\"粉质黏土\",\"thickness\":3.0},{\"depth\":\"3-8m\",\"lithology\":\"砂砾石层\",\"thickness\":5.0},{\"depth\":\"8-25m\",\"lithology\":\"强风化泥岩\",\"thickness\":17.0},{\"depth\":\"25-50m\",\"lithology\":\"中风化泥岩\",\"thickness\":25.0},{\"depth\":\"50-80m\",\"lithology\":\"微风化石灰岩\",\"thickness\":30.0}]",
                "[" +
                    "{\"name\":\"粉质黏土层\",\"depth\":\"0-3m\",\"type\":\"Q4al+pl\",\"strength\":\"120kPa\",\"permeability\":\"低渗透\"}," +
                    "{\"name\":\"砂砾石层\",\"depth\":\"3-8m\",\"type\":\"Q2al\",\"strength\":\"280kPa\",\"permeability\":\"中渗透\"}," +
                    "{\"name\":\"强风化泥岩\",\"depth\":\"8-25m\",\"type\":\"E2s\",\"strength\":\"350kPa\",\"permeability\":\"低渗透\"}," +
                    "{\"name\":\"中风化泥岩\",\"depth\":\"25-50m\",\"type\":\"E2s\",\"strength\":\"12MPa\",\"permeability\":\"微渗透\"}," +
                    "{\"name\":\"微风化石灰岩\",\"depth\":\"50-80m\",\"type\":\"C1d\",\"strength\":\"60MPa\",\"permeability\":\"微渗透\"}" +
                    "]",
                "{\"structure\":\"单斜构造\",\"dip\":\"15°\",\"dipDirection\":\"135°\",\"fractures\":2}",
                "[]",
                RockStratumAnalysis.AiAlgorithm.DEEP_LEARNING.name(), "v2.1.0", 94.8,
                "AI分析结果显示：该区域岩层结构较为稳定，从上到下依次为第四系覆盖层、强风化泥岩、中风化泥岩、微风化石灰岩。岩层倾角约15°，整体为单斜构造。50米以下为硬质石灰岩，承载力较高。",
                "{" +
                    "\"boreholeCount\":6," +
                    "\"avgRQD\":78.5," +
                    "\"rockQuality\":\"良好\"," +
                    "\"bearingCapacity\":\"60MPa\"," +
                    "\"seismicLevel\":\"VIII度\"" +
                    "}",
                "建议在石灰岩地层进行基础施工时注意岩溶发育情况，施工前应进行详细勘察",
                RockStratumAnalysis.RiskLevel.LOW.name(),
                "COMPLETED", "张总工", LocalDateTime.of(2026, 6, 17, 16, 0), LocalDateTime.of(2026, 6, 17, 18, 0)),
            createRockAnalysis("RSA-2026-0616-001", "ZRS-2026-0616-003", "莲花镇地质雷达探测",
                "岳麓区莲花镇", 28.38567, 112.78934, 48.5,
                RockStratumAnalysis.AnalysisType.GPR.name(), "地质雷达", 0, 30.0, 4,
                "[{\"depth\":\"0-2m\",\"lithology\":\"耕植土\",\"thickness\":2.0},{\"depth\":\"2-10m\",\"lithology\":\"粉质黏土\",\"thickness\":8.0},{\"depth\":\"10-20m\",\"lithology\":\"强风化砂岩\",\"thickness\":10.0},{\"depth\":\"20-30m\",\"lithology\":\"中风化砂岩\",\"thickness\":10.0}]",
                "[{\"name\":\"耕植土层\",\"depth\":\"0-2m\"},{\"name\":\"粉质黏土层\",\"depth\":\"2-10m\"},{\"name\":\"强风化砂岩层\",\"depth\":\"10-20m\"},{\"name\":\"中风化砂岩层\",\"depth\":\"20-30m\"}]",
                "{\"structure\":\"层状结构\",\"dip\":\"10°\",\"dipDirection\":\"210°\"}",
                "[{\"name\":\"F1断层\",\"type\":\"正断层\",\"strike\":\"NE30°\",\"dip\":\"60°\"}]",
                RockStratumAnalysis.AiAlgorithm.CNN.name(), "v1.8.0", 87.3,
                "地质雷达探测结果显示：该区域存在一条小型正断层（F1），位于地表下15-20米处，断距约2米。岩层整体为砂岩地层，风化程度随深度增加而降低。建议对断层附近区域进行重点监测。",
                "{\"gprFrequency\":500,\"penetrationDepth\":30,\"resolution\":\"0.5m\"}",
                "建议对F1断层附近区域进行加密监测，防止不均匀沉降",
                RockStratumAnalysis.RiskLevel.MEDIUM.name(),
                "COMPLETED", "李工", LocalDateTime.of(2026, 6, 16, 14, 0), LocalDateTime.of(2026, 6, 16, 16, 30))
        };
        for (RockStratumAnalysis ra : analyses) {
            rockStratumAnalysisMapper.insert(ra);
        }
        log.info("[Mock数据] 岩层结构分析Mock数据初始化完成，共 {} 条", analyses.length);
    }

    private RockStratumAnalysis createRockAnalysis(String code, String missionCode, String projectName,
                                                    String location, double lat, double lng, double elev,
                                                    String analysisType, String dataSource, int boreholeCount,
                                                    double maxDepth, int stratumCount,
                                                    String stratumData, String lithologyData,
                                                    String structureData, String faultData,
                                                    String aiAlgorithm, String aiModelVersion, double aiConfidence,
                                                    String aiSummary, String aiDetail,
                                                    String suggestion, String riskLevel,
                                                    String status, String analyst,
                                                    LocalDateTime analysisTime, LocalDateTime reportTime) {
        RockStratumAnalysis ra = new RockStratumAnalysis();
        ra.setAnalysisCode(code);
        ra.setMissionCode(missionCode);
        ra.setProjectName(projectName);
        ra.setLocation(location);
        ra.setLatitude(lat);
        ra.setLongitude(lng);
        ra.setElevation(elev);
        ra.setAnalysisType(analysisType);
        ra.setDataSource(dataSource);
        ra.setBoreholeCount(boreholeCount);
        ra.setMaxDepth(maxDepth);
        ra.setStratumCount(stratumCount);
        ra.setStratumData(stratumData);
        ra.setLithologyData(lithologyData);
        ra.setStructureData(structureData);
        ra.setFaultData(faultData);
        ra.setAiAlgorithm(aiAlgorithm);
        ra.setAiModelVersion(aiModelVersion);
        ra.setAiConfidence(aiConfidence);
        ra.setAiSummary(aiSummary);
        ra.setAiDetail(aiDetail);
        ra.setSuggestion(suggestion);
        ra.setRiskLevel(riskLevel);
        ra.setStatus(status);
        ra.setAnalyst(analyst);
        ra.setAnalysisTime(analysisTime);
        ra.setReportTime(reportTime);
        ra.setIsDeleted(0);
        return ra;
    }

    private void initDisasterRisks() {
        Long count = disasterRiskMapper.selectCount(new LambdaQueryWrapper<DisasterRisk>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 灾害风险数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化灾害风险Mock数据...");
        DisasterRisk[] risks = {
            createDisasterRisk("DR-2026-0617-001", "ZRS-2026-0617-001", "望城区乔口镇",
                28.45672, 112.83521, DisasterRisk.DisasterType.LANDSLIDE.name(),
                DisasterRisk.RiskLevel.LOW.name(), 15.5, "地形平缓，风险较低",
                "{\"slope\":\"5°\",\"elevationDiff\":\"20m\",\"soilType\":\"黏性土\",\"vegetation\":\"茂密\"}",
                "{\"rainfall\":\"1200mm/year\",\"monitoringPoints\":3}",
                "[{\"date\":\"2018-06\",\"type\":\"小型滑坡\",\"damage\":\"轻微\"}]",
                "AI综合分析：该区域地形坡度较缓（约5°），植被覆盖率高，历史上仅发生过小型滑坡。整体风险较低。",
                "建议在雨季加强巡查，重点关注坡脚区域",
                92.3, "COMPLETED", "王工", LocalDateTime.of(2026, 6, 17, 10, 0)),
            createDisasterRisk("DR-2026-0617-002", "ZRS-2026-0617-001", "望城区乔口镇",
                28.45718, 112.83605, DisasterRisk.DisasterType.DEBRIS_FLOW.name(),
                DisasterRisk.RiskLevel.MEDIUM.name(), 42.8, "沟道存在，需关注雨季",
                "{\"drainageArea\":\"2.5km²\",\"channelSlope\":\"15°\",\"sedimentSupply\":\"中等\"}",
                "{\"rainfall\":\"1200mm/year\",\"warningLevel\":\"黄色预警\"}",
                "[{\"date\":\"2017-07\",\"type\":\"泥石流\",\"damage\":\"中度\"}]",
                "AI分析：该区域存在天然沟道，流域面积约2.5平方公里，沟道坡度较大。雨季强降雨时可能引发泥石流，需重点关注。",
                "建议建立雨量监测站，制定应急预案，雨季加强巡查",
                88.5, "COMPLETED", "李工", LocalDateTime.of(2026, 6, 17, 11, 0)),
            createDisasterRisk("DR-2026-0617-003", "ZRS-2026-0617-001", "望城区乔口镇",
                28.45801, 112.83489, DisasterRisk.DisasterType.GROUND_SUBSIDENCE.name(),
                DisasterRisk.RiskLevel.LOW.name(), 18.2, "土层稳定",
                "{\"subsidenceRate\":\"5mm/year\",\"soilType\":\"粉质黏土\",\"groundwater\":\"稳定\"}",
                "{\"monitoringPoints\":5,\"referenceStation\":\"CS01\"}",
                "[]",
                "AI分析：该区域土层较为稳定，地下水位变化小，沉降速率约5mm/年，属于正常固结沉降范围。地面沉降风险较低。",
                "建议定期监测，关注地下水开采情况",
                94.1, "COMPLETED", "张工", LocalDateTime.of(2026, 6, 17, 12, 0)),
            createDisasterRisk("DR-2026-0617-004", "ZRS-2026-0617-001", "望城区乔口镇",
                28.45765, 112.83542, DisasterRisk.DisasterType.FLOOD.name(),
                DisasterRisk.RiskLevel.MEDIUM.name(), 55.6, "排水系统一般",
                "{\"elevation\":\"32m\",\"drainageCapacity\":\"5年一遇\",\"riverDistance\":\"800m\"}",
                "{\"pumpingStations\":2,\"drainagePipes\":\"3.5km\"}",
                "[{\"date\":\"2017-06\",\"type\":\"内涝\",\"duration\":\"24h\"}]",
                "AI分析：该区域海拔较低（约32米），排水系统标准为5年一遇，遇强降雨可能发生内涝。距离河流约800米，需关注河流水位。",
                "建议升级排水系统，加强汛期应急排涝能力",
                89.7, "COMPLETED", "王工", LocalDateTime.of(2026, 6, 17, 13, 0))
        };
        for (DisasterRisk risk : risks) {
            disasterRiskMapper.insert(risk);
        }
        log.info("[Mock数据] 灾害风险Mock数据初始化完成，共 {} 条", risks.length);
    }

    private DisasterRisk createDisasterRisk(String code, String missionCode, String region,
                                             double lat, double lng, String disasterType,
                                             String riskLevel, double riskScore, String description,
                                             String factors, String monitoring, String history,
                                             String aiAnalysis, String aiSuggestion,
                                             double aiConfidence, String status,
                                             String analyst, LocalDateTime assessmentTime) {
        DisasterRisk dr = new DisasterRisk();
        dr.setRiskCode(code);
        dr.setMissionCode(missionCode);
        dr.setRegion(region);
        dr.setLatitude(lat);
        dr.setLongitude(lng);
        dr.setDisasterType(disasterType);
        dr.setRiskLevel(riskLevel);
        dr.setRiskScore(riskScore);
        dr.setDescription(description);
        dr.setInfluencingFactors(factors);
        dr.setMonitoringData(monitoring);
        dr.setHistoricalRecords(history);
        dr.setAiAnalysis(aiAnalysis);
        dr.setAiSuggestion(aiSuggestion);
        dr.setAiConfidence(aiConfidence);
        dr.setStatus(status);
        dr.setAnalyst(analyst);
        dr.setAssessmentTime(assessmentTime);
        dr.setIsDeleted(0);
        return dr;
    }

    private void initDevices() {
        Long count = deviceMapper.selectCount(new LambdaQueryWrapper<Device>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 设备数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化设备Mock数据...");
        Device[] devices = {
            createDevice("DEV-001", "M350-003", Device.DeviceType.DRONE.name(), "DJI Matrice 350 RTK", "大疆创新",
                "SN-M350-202603-003", Device.Status.ONLINE.name(), 78, 95, 45, 28.5,
                "v03.01.0000", "192.168.1.101", "望城区乔口镇", 28.45672, 112.83521,
                "王工", "外业采集部", LocalDateTime.of(2026, 3, 15, 0, 0),
                LocalDateTime.of(2026, 6, 10, 0, 0), LocalDateTime.of(2026, 6, 17, 12, 0),
                128, 156.5),
            createDevice("DEV-002", "M350-002", Device.DeviceType.DRONE.name(), "DJI Matrice 350 RTK", "大疆创新",
                "SN-M350-202602-002", Device.Status.WORKING.name(), 65, 92, 38, 30.2,
                "v03.01.0000", "192.168.1.102", "岳麓区莲花镇", 28.38567, 112.78934,
                "李工", "外业采集部", LocalDateTime.of(2026, 2, 20, 0, 0),
                LocalDateTime.of(2026, 5, 28, 0, 0), LocalDateTime.of(2026, 6, 16, 16, 0),
                156, 189.2),
            createDevice("DEV-003", "M300-001", Device.DeviceType.DRONE.name(), "DJI Matrice 300 RTK", "大疆创新",
                "SN-M300-202508-001", Device.Status.CHARGING.name(), 95, 0, 62, 25.8,
                "v02.02.0100", "192.168.1.103", "单位库房", 28.40000, 112.90000,
                "张工", "外业采集部", LocalDateTime.of(2025, 8, 10, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0), LocalDateTime.of(2026, 6, 15, 18, 0),
                285, 342.8),
            createDevice("DEV-004", "LiDAR-001", Device.DeviceType.LIDAR.name(), "RIEGL VZ-2000", "RIEGL",
                "SN-LIDAR-202511-001", Device.Status.ONLINE.name(), 0, 98, 55, 24.0,
                "v2.5.0", "192.168.1.110", "外业采集部", 28.45672, 112.83521,
                "张工", "技术部", LocalDateTime.of(2025, 11, 20, 0, 0),
                LocalDateTime.of(2026, 6, 5, 0, 0), LocalDateTime.of(2026, 6, 17, 10, 0),
                48, 78.5),
            createDevice("DEV-005", "GPR-001", Device.DeviceType.GPR.name(), "SIR-4000", "GSSI",
                "SN-GPR-202601-001", Device.Status.MAINTENANCE.name(), 0, 0, 0, 22.5,
                "v6.0", "", "技术部实验室", 28.40000, 112.90000,
                "李工", "技术部", LocalDateTime.of(2026, 1, 15, 0, 0),
                LocalDateTime.of(2026, 6, 15, 0, 0), LocalDateTime.of(2026, 6, 14, 17, 0),
                23, 32.1),
            createDevice("DEV-006", "WS-001", Device.DeviceType.COMPUTER.name(), "图形工作站", "戴尔",
                "SN-WS-202604-001", Device.Status.ONLINE.name(), 0, 100, 72, 35.0,
                "Windows 10 Pro", "192.168.1.200", "数据处理中心", 28.40000, 112.90000,
                "张工", "数据分析部", LocalDateTime.of(2026, 4, 10, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0), LocalDateTime.of(2026, 6, 17, 18, 0),
                0, 0.0)
        };
        for (Device device : devices) {
            deviceMapper.insert(device);
        }
        log.info("[Mock数据] 设备Mock数据初始化完成，共 {} 条", devices.length);
    }

    private Device createDevice(String code, String name, String type, String model, String manufacturer,
                                 String serialNumber, String status, int battery, int signal, int storage,
                                 double temperature, String firmware, String ip, String location,
                                 double lat, double lng, String owner, String department,
                                 LocalDateTime purchaseDate, LocalDateTime lastMaintenance, LocalDateTime lastOnline,
                                 int totalFlights, double totalFlightHours) {
        Device d = new Device();
        d.setDeviceCode(code);
        d.setDeviceName(name);
        d.setDeviceType(type);
        d.setDeviceModel(model);
        d.setManufacturer(manufacturer);
        d.setSerialNumber(serialNumber);
        d.setStatus(status);
        d.setBatteryLevel(battery);
        d.setSignalLevel(signal);
        d.setStorageLevel(storage);
        d.setTemperature(temperature);
        d.setFirmwareVersion(firmware);
        d.setIpAddress(ip);
        d.setLocation(location);
        d.setLatitude(lat);
        d.setLongitude(lng);
        d.setOwner(owner);
        d.setDepartment(department);
        d.setPurchaseDate(purchaseDate);
        d.setLastMaintenance(lastMaintenance);
        d.setLastOnline(lastOnline);
        d.setTotalFlights(totalFlights);
        d.setTotalFlightHours(totalFlightHours);
        d.setIsDeleted(0);
        return d;
    }

    private void initQualityChecks() {
        Long count = qualityCheckMapper.selectCount(new LambdaQueryWrapper<QualityCheck>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 质量校验数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化质量校验Mock数据...");
        QualityCheck[] checks = {
            createQualityCheck("QC-2026-0617-001", 1L, "ZRS-2026-0617-001",
                QualityCheck.CheckType.PHOTO_QUALITY.name(), "照片质量检测",
                1247, 1198, 49, 96.1, QualityCheck.Status.PASSED.name(),
                "49张照片因曝光问题不合格，已剔除",
                "王工", LocalDateTime.of(2026, 6, 17, 13, 0)),
            createQualityCheck("QC-2026-0617-002", 1L, "ZRS-2026-0617-001",
                QualityCheck.CheckType.GPS_COMPLETENESS.name(), "GPS完整性检查",
                1247, 1247, 0, 100.0, QualityCheck.Status.PASSED.name(),
                "",
                "李工", LocalDateTime.of(2026, 6, 17, 13, 30)),
            createQualityCheck("QC-2026-0617-003", 1L, "ZRS-2026-0617-001",
                QualityCheck.CheckType.LIDAR_DENSITY.name(), "LiDAR点云密度检测",
                286, 278, 8, 97.2, QualityCheck.Status.PASSED.name(),
                "8条航带点云密度略低，满足精度要求",
                "张工", LocalDateTime.of(2026, 6, 17, 14, 0)),
            createQualityCheck("QC-2026-0617-004", 1L, "ZRS-2026-0617-001",
                QualityCheck.CheckType.OVERLAP_RATE.name(), "重叠率检测",
                0, 0, 0, 78.3, QualityCheck.Status.PARTIAL.name(),
                "部分区域旁向重叠率不足60%，需关注",
                "张工", LocalDateTime.of(2026, 6, 17, 14, 30)),
            createQualityCheck("QC-2026-0616-001", 2L, "ZRS-2026-0616-003",
                QualityCheck.CheckType.PHOTO_QUALITY.name(), "照片质量检测",
                2100, 2058, 42, 98.0, QualityCheck.Status.PASSED.name(),
                "",
                "李工", LocalDateTime.of(2026, 6, 16, 18, 0))
        };
        for (QualityCheck check : checks) {
            qualityCheckMapper.insert(check);
        }
        log.info("[Mock数据] 质量校验Mock数据初始化完成，共 {} 条", checks.length);
    }

    private QualityCheck createQualityCheck(String code, Long batchId, String missionCode,
                                             String checkType, String checkItem,
                                             int total, int pass, int fail, double passRate,
                                             String status, String failDetails,
                                             String checker, LocalDateTime checkTime) {
        QualityCheck qc = new QualityCheck();
        qc.setCheckCode(code);
        qc.setBatchId(batchId);
        qc.setMissionCode(missionCode);
        qc.setCheckType(checkType);
        qc.setCheckItem(checkItem);
        qc.setTotalCount(total);
        qc.setPassCount(pass);
        qc.setFailCount(fail);
        qc.setPassRate(passRate);
        qc.setStatus(status);
        qc.setFailDetails(failDetails);
        qc.setChecker(checker);
        qc.setCheckTime(checkTime);
        qc.setIsDeleted(0);
        return qc;
    }

    private void initAnnouncementCategories() {
        Long count = announcementCategoryMapper.selectCount(new LambdaQueryWrapper<AnnouncementCategory>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 公告分类数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化公告分类Mock数据...");
        AnnouncementCategory[] categories = {
            createAnnouncementCategory("POLICY", "政策法规", "policy", "#1890ff", "国家及地方土地相关政策法规", 1),
            createAnnouncementCategory("NOTICE", "通知公告", "notice", "#fa8c16", "各类通知公告信息", 2),
            createAnnouncementCategory("LAND_NEWS", "土地新闻", "land_news", "#52c41a", "土地资源相关新闻动态", 3),
            createAnnouncementCategory("INDUSTRY", "行业动态", "industry", "#722ed1", "土地行业发展动态", 4),
            createAnnouncementCategory("TECH_GUIDE", "技术指南", "tech_guide", "#13c2c2", "土地监测与整治技术指南", 5),
            createAnnouncementCategory("MARKET", "市场信息", "market", "#eb2f96", "土地市场交易信息", 6)
        };
        for (AnnouncementCategory category : categories) {
            announcementCategoryMapper.insert(category);
        }
        log.info("[Mock数据] 公告分类Mock数据初始化完成，共 {} 条", categories.length);
    }

    private AnnouncementCategory createAnnouncementCategory(String code, String name, String icon, String color,
                                                             String description, int sortOrder) {
        AnnouncementCategory ac = new AnnouncementCategory();
        ac.setCategoryCode(code);
        ac.setCategoryName(name);
        ac.setIcon(icon);
        ac.setColor(color);
        ac.setDescription(description);
        ac.setSortOrder(sortOrder);
        ac.setStatus(1);
        ac.setIsDeleted(0);
        return ac;
    }

    private void initAnnouncements() {
        Long count = announcementMapper.selectCount(new LambdaQueryWrapper<Announcement>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 公告数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化公告Mock数据...");
        Announcement[] announcements = {
            createAnnouncement("湖南省人民政府关于加强耕地保护和改进占补平衡的实施意见",
                "为深入贯彻落实党中央、国务院关于加强耕地保护和改进占补平衡的决策部署，坚守土地公有制性质不改变、耕地红线不突破、农民利益不受损三条底线，确保我省耕地数量不减少、质量有提高、生态有改善，现结合我省实际，提出如下实施意见。一、严格控制耕地减少，强化土地利用总体规划管控，从严核定新增建设用地规模，从严控制建设占用耕地特别是优质耕地。二、大力推进耕地质量提升，统筹整合各类农田建设资金，集中力量建设高标准农田，提升耕地质量等级。三、加强耕地保护监督，强化耕地保护目标责任考核，将耕地保护目标纳入各级政府绩效考核体系，严格考核问责。",
                "湖南省发布加强耕地保护和改进占补平衡实施意见，从严格控制耕地减少、大力推进耕地质量提升、加强耕地保护监督三个方面提出具体措施，确保全省耕地数量不减少、质量有提高。",
                1L, "政策法规", "省自然资源厅", "湖南省人民政府官网",
                "湖南省", "长沙市", "岳麓区", Announcement.AdminLevel.PROVINCE.name(),
                "耕地保护,占补平衡,高标准农田", "政策法规,耕地保护",
                1, 1, 1,
                "AI分析显示该规划覆盖全省范围，重点关注高标准农田建设和生态修复，预计将对全省土地利用格局产生深远影响。",
                "[{\"time\":\"2026-01-15\",\"event\":\"规划编制启动\"},{\"time\":\"2026-03-20\",\"event\":\"初稿完成\"},{\"time\":\"2026-06-25\",\"event\":\"公开征求意见\"},{\"time\":\"2026-08-01\",\"event\":\"预计正式发布\"}]",
                "土地整治,高标准农田,生态修复,规划",
                LocalDateTime.of(2026, 6, 15, 9, 0), 28.2282, 112.9388),
            createAnnouncement("长沙市永久基本农田特殊保护实施细则",
                "为切实加强永久基本农田特殊保护，保障国家粮食安全，根据《中华人民共和国土地管理法》《基本农田保护条例》等法律法规，结合我市实际，制定本实施细则。一、划定范围：永久基本农田划定以乡（镇）为单位进行，落实到地块，纳入国家永久基本农田数据库严格管理。二、保护措施：严格占用审批，严禁改变用途，严禁闲置荒芜，加强质量建设。三、监督管理：建立保护台账，加强动态巡查，严格考核问责。",
                "长沙市出台永久基本农田特殊保护实施细则，明确划定范围、保护措施和监督管理要求，实行最严格的永久基本农田保护制度。",
                1L, "政策法规", "市自然资源和规划局", "长沙市人民政府官网",
                "湖南省", "长沙市", "雨花区", Announcement.AdminLevel.CITY.name(),
                "永久基本农田,特殊保护,耕地红线", "政策法规,基本农田",
                1, 1, 0,
                "AI分析：该细则对永久基本农田实行最严格的保护，涉及全市280万亩永久基本农田，对保障粮食安全具有重要意义。",
                "[{\"time\":\"2026-05-10\",\"event\":\"细则起草\"},{\"time\":\"2026-06-05\",\"event\":\"征求意见\"},{\"time\":\"2026-06-18\",\"event\":\"正式发布\"}]",
                "永久基本农田,特殊保护,粮食安全",
                LocalDateTime.of(2026, 6, 18, 10, 30), 28.2282, 112.9388),
            createAnnouncement("关于开展长沙市2026年度土地整治项目申报工作的通知",
                "各区县（市）自然资源局、农业农村局：为深入贯彻落实国家、省关于土地整治工作的决策部署，统筹推进我市土地整治工作，根据年度工作安排，现就开展2026年度土地整治项目申报工作有关事项通知如下：一、申报范围：高标准农田建设项目、城乡建设用地增减挂钩项目、土地复垦项目、宜耕后备资源开发项目。二、申报条件：项目选址符合相关规划，土地权属清晰无争议，地方政府和群众积极性高。三、请于2026年7月31日前将申报材料报送至市自然资源和规划局土地整治科。",
                "长沙市启动2026年度土地整治项目申报工作，涵盖高标准农田建设、城乡建设用地增减挂钩、土地复垦、宜耕后备资源开发四大类，申报截止日期为2026年7月31日。",
                2L, "通知公告", "市自然资源和规划局", "长沙市自然资源和规划局官网",
                "湖南省", "长沙市", "芙蓉区", Announcement.AdminLevel.CITY.name(),
                "土地整治,项目申报,高标准农田", "通知公告,土地整治",
                1, 0, 1,
                "AI分析：本批次项目重点支持长沙周边区县，预计可新增耕地面积约5000亩，有效提升耕地质量。",
                "[{\"time\":\"2026-06-20\",\"event\":\"通知发布\"},{\"time\":\"2026-07-31\",\"event\":\"申报截止\"},{\"time\":\"2026-08-15\",\"event\":\"专家评审\"},{\"time\":\"2026-08-31\",\"event\":\"立项批复\"}]",
                "土地整治,项目申报,高标准农田",
                LocalDateTime.of(2026, 6, 20, 14, 0), 28.2282, 112.9388),
            createAnnouncement("望城区完成全域土壤采样检测工作",
                "近日，望城区自然资源局联合农业农村局完成了全域土壤采样检测工作，全面掌握了全区土壤质量状况，为耕地保护、农业生产和土地整治提供了科学依据。本次土壤采样检测工作历时3个月，覆盖全区14个乡镇（街道），共采集土壤样品2860份，检测项目包括pH值、有机质、氮、磷、钾、重金属等30余项指标。检测结果显示，望城区耕地土壤整体质量良好，有机质含量平均为28.5g/kg，pH值平均为6.2，处于适宜水平。",
                "望城区完成全域土壤采样检测，采集样品2860份，检测30余项指标，结果显示土壤整体质量良好，部分区域需加强改良治理。",
                3L, "土地新闻", "望城区自然资源局", "长沙晚报",
                "湖南省", "长沙市", "望城区", Announcement.AdminLevel.COUNTY.name(),
                "土壤检测,土壤质量,耕地保护", "土地新闻,土壤检测",
                0, 1, 1,
                "AI分析：望城区土壤整体质量良好，但部分区域存在酸化和养分不均衡问题，建议加强改良治理，重点推广测土配方施肥技术。",
                "[{\"time\":\"2026-03-15\",\"event\":\"工作启动\"},{\"time\":\"2026-04-20\",\"event\":\"野外采样\"},{\"time\":\"2026-05-30\",\"event\":\"实验室分析\"},{\"time\":\"2026-06-12\",\"event\":\"成果发布\"}]",
                "土壤检测,土壤质量,耕地保护",
                LocalDateTime.of(2026, 6, 12, 8, 30), 28.4567, 112.8352),
            createAnnouncement("长沙县探索农村土地流转新模式成效显著",
                "近年来，长沙县积极探索农村土地流转新模式，通过建立土地流转服务平台、培育新型农业经营主体、完善流转机制等措施，有效促进了土地规模化、集约化经营，取得了显著成效。截至目前，长沙县农村土地流转面积已达85.6万亩，占耕地总面积的68.5%，涉及农户12.8万户。土地流转后，粮食产量平均提高15%，农民人均增收3200元。",
                "长沙县农村土地流转面积达85.6万亩，占耕地68.5%，通过建立三级服务体系、培育新型经营主体、规范流转行为等措施，实现粮食增产、农民增收。",
                3L, "土地新闻", "长沙县农业农村局", "湖南日报",
                "湖南省", "长沙市", "长沙县", Announcement.AdminLevel.COUNTY.name(),
                "土地流转,规模化经营,乡村振兴", "土地新闻,土地流转",
                0, 1, 1,
                "AI分析：长沙县土地流转模式成效显著，为全省提供了可复制可推广的经验。下一步建议加强土地流转风险防控，完善社会保障体系。",
                "[{\"time\":\"2023-01\",\"event\":\"试点启动\"},{\"time\":\"2024-06\",\"event\":\"全面推广\"},{\"time\":\"2025-12\",\"event\":\"体系建成\"},{\"time\":\"2026-06\",\"event\":\"成效显现\"}]",
                "土地流转,规模化经营,乡村振兴",
                LocalDateTime.of(2026, 6, 8, 16, 20), 28.2282, 112.9388),
            createAnnouncement("湖南省城乡建设用地增减挂钩工作推进会议在长沙召开",
                "6月15日，湖南省城乡建设用地增减挂钩工作推进会议在长沙召开。会议总结了近年来全省增减挂钩工作成效，分析了当前面临的形势和问题，部署了下一阶段重点工作任务。会议指出，近年来我省城乡建设用地增减挂钩工作取得显著成效：累计完成增减挂钩指标交易12.3万亩，交易金额达246亿元；支持脱贫攻坚和乡村振兴作用明显；耕地保护得到加强；城乡用地结构不断优化。",
                "湖南省城乡建设用地增减挂钩工作推进会在长沙召开，总结近年成效，部署下一阶段工作。累计完成指标交易12.3万亩，交易金额246亿元。",
                4L, "行业动态", "省自然资源厅", "湖南省自然资源厅官网",
                "湖南省", "长沙市", "天心区", Announcement.AdminLevel.PROVINCE.name(),
                "增减挂钩,城乡建设用地,耕地保护", "行业动态,增减挂钩",
                0, 1, 1,
                "AI分析：增减挂钩政策在优化城乡用地结构、支持乡村振兴方面发挥了重要作用。建议进一步规范项目管理，提高复垦耕地质量。",
                "[{\"time\":\"2026-06-15 09:00\",\"event\":\"开幕式\"},{\"time\":\"2026-06-15 10:30\",\"event\":\"工作报告\"},{\"time\":\"2026-06-15 14:00\",\"event\":\"经验交流\"},{\"time\":\"2026-06-15 16:30\",\"event\":\"总结讲话\"}]",
                "增减挂钩,城乡建设用地,耕地保护",
                LocalDateTime.of(2026, 6, 16, 9, 30), 28.2282, 112.9388),
            createAnnouncement("长沙市出台乡村振兴战略规划土地政策支持措施",
                "为深入贯彻实施乡村振兴战略，充分发挥自然资源部门职能作用，近日，长沙市自然资源和规划局出台《关于强化乡村振兴战略规划土地政策支持的若干措施》，从规划引领、用地保障、土地整治、改革创新等四个方面提出20条具体支持措施。主要包括：完善镇村规划体系，优先保障乡村产业发展用地，大力推进高标准农田建设，深化农村宅基地制度改革，推进集体经营性建设用地入市等。",
                "长沙市出台20条土地政策支持乡村振兴，涵盖规划引领、用地保障、土地整治、改革创新四大方面，为乡村振兴提供有力土地要素保障。",
                4L, "行业动态", "市自然资源和规划局", "红网",
                "湖南省", "长沙市", "开福区", Announcement.AdminLevel.CITY.name(),
                "乡村振兴,土地政策,农村改革", "行业动态,乡村振兴",
                0, 1, 0,
                "AI分析：该政策措施全面系统，从规划、用地、整治、改革四个维度支持乡村振兴，预计将有效激发农村发展活力。",
                "[{\"time\":\"2026-05-20\",\"event\":\"政策起草\"},{\"time\":\"2026-06-05\",\"event\":\"征求意见\"},{\"time\":\"2026-06-10\",\"event\":\"正式出台\"}]",
                "乡村振兴,土地政策,农村改革",
                LocalDateTime.of(2026, 6, 10, 11, 0), 28.2282, 112.9388),
            createAnnouncement("土壤样品采集与检测技术指南",
                "为规范土壤样品采集与检测工作，提高土壤检测数据质量，特制定本技术指南。一、土壤样品采集：采样准备包括工具准备、资料收集、布点设计；采样方法包括划分采样单元、确定采样深度、多点混合采样、样品编号登记。二、土壤样品处理：风干、磨碎过筛、分样保存。三、土壤检测项目及方法：理化性质检测包括pH值、有机质、全氮、有效磷、速效钾、机械组成等；重金属检测包括镉、铅、铬、砷、汞等。四、质量控制：采样质量控制、实验室质量控制、数据审核。",
                "土壤采样检测技术指南，涵盖采样准备、采样方法、样品处理、检测项目方法及质量控制等内容，为土壤检测工作提供技术规范。",
                5L, "技术指南", "技术部", "技术手册",
                "湖南省", "长沙市", "岳麓区", "岳麓街道", Announcement.AdminLevel.CITY.name(),
                "土壤检测,采样技术,分析方法", "技术指南,土壤检测",
                0, 0, 1,
                "AI分析：该技术指南内容全面，实用性强，可有效指导土壤采样检测工作，确保检测数据准确可靠。",
                "[{\"time\":\"2026-05-10\",\"event\":\"指南编写\"},{\"time\":\"2026-06-01\",\"event\":\"专家评审\"},{\"time\":\"2026-06-05\",\"event\":\"发布实施\"}]",
                "土壤检测,采样技术,分析方法",
                LocalDateTime.of(2026, 6, 5, 13, 40), 28.2282, 112.9388),
            createAnnouncement("高标准农田建设技术规范",
                "为规范高标准农田建设，提高建设质量和效益，根据国家和省有关规定，结合我市实际，制定本技术规范。一、建设目标：通过土地整治、农田水利、田间道路、农田防护与生态环境保持、农田输配电等工程建设，实现'田成方、土成型、渠相通、路相连、旱能灌、涝能排、渍能降、土壤肥、无污染、产量高'的目标。二、建设标准：土地平整工程要求田块规整、田面平整、表土保护；农田水利工程要求灌排系统配套、节水灌溉；田间道路工程要求道路畅通、晴雨通车。",
                "高标准农田建设技术规范，明确建设目标、建设标准和建设程序，涵盖土地平整、农田水利、田间道路、农田防护四大工程。",
                5L, "技术指南", "技术部", "技术手册",
                "湖南省", "长沙市", "望城区", "乔口镇", Announcement.AdminLevel.CITY.name(),
                "高标准农田,土地整治,建设规范", "技术指南,土地整治",
                0, 0, 1,
                "AI分析：该技术规范结合长沙实际，具有较强的针对性和可操作性，对指导全市高标准农田建设具有重要意义。",
                "[{\"time\":\"2026-04-15\",\"event\":\"规范编制\"},{\"time\":\"2026-05-20\",\"event\":\"征求意见\"},{\"time\":\"2026-06-03\",\"event\":\"发布实施\"}]",
                "高标准农田,土地整治,建设规范",
                LocalDateTime.of(2026, 6, 3, 10, 15), 28.4567, 112.8352),
            createAnnouncement("2026年上半年长沙市土地市场运行情况分析",
                "2026年上半年，长沙市土地市场总体运行平稳，供应结构持续优化，市场预期总体稳定。上半年，全市供应国有建设用地285宗，总面积1268公顷，同比增长8.5%；成交262宗，总面积1185公顷，同比增长6.2%；成交总价款528亿元，同比下降2.3%。主要特点：工业用地成交活跃，住宅用地溢价率回落，商业用地需求平稳，节约集约用地水平提升。",
                "2026年上半年长沙土地市场总体平稳，供应1268公顷同比增8.5%，成交1185公顷、价款528亿元，工业用地成交活跃，住宅溢价率回落。",
                6L, "市场信息", "市自然资源和规划局", "长沙市土地市场月报",
                "湖南省", "长沙市", "芙蓉区", Announcement.AdminLevel.CITY.name(),
                "土地市场,土地供应,土地成交", "市场信息,土地市场",
                0, 0, 1,
                "AI分析：上半年长沙土地市场运行总体平稳，供应结构持续优化，工业用地增长较快，住宅市场趋于理性。预计下半年将继续保持平稳态势。",
                "[{\"time\":\"2026-01-15\",\"event\":\"1月市场分析\"},{\"time\":\"2026-04-15\",\"event\":\"一季度分析\"},{\"time\":\"2026-06-25\",\"event\":\"上半年分析\"}]",
                "土地市场,土地供应,土地成交",
                LocalDateTime.of(2026, 6, 25, 15, 30), 28.2282, 112.9388),
            createAnnouncement("关于进一步规范耕地占补平衡管理工作的通知",
                "为严格落实耕地占补平衡制度，加强补充耕地项目管理，确保补充耕地数量相等、质量相当、生态适宜，现就进一步规范耕地占补平衡管理工作通知如下：一、严格补充耕地项目立项管理，科学选址，规范立项程序，加强可研论证。二、严格补充耕地项目实施管理，严格设计标准，加强施工管理，强化质量监理。三、严格补充耕地项目验收管理，规范验收程序，严格验收标准，加强耕地质量评定。四、严格补充耕地指标管理，实行指标核销制，规范指标交易，加强指标监管。",
                "湖南省进一步规范耕地占补平衡管理，严格立项、实施、验收、指标管理和后期管护，确保补充耕地数量相等、质量相当。",
                1L, "政策法规", "省自然资源厅", "湖南省自然资源厅官网",
                "湖南省", "长沙市", "雨花区", Announcement.AdminLevel.PROVINCE.name(),
                "占补平衡,补充耕地,耕地保护", "政策法规,占补平衡",
                0, 0, 0,
                "AI分析：该通知从立项、实施、验收、指标、管护五个环节强化耕地占补平衡管理，对确保耕地数量质量双提升具有重要作用。",
                "[{\"time\":\"2026-06-22\",\"event\":\"通知发布\"},{\"time\":\"2026-07-01\",\"event\":\"施行日期\"}]",
                "占补平衡,补充耕地,耕地保护",
                LocalDateTime.of(2026, 6, 22, 9, 45), 28.2282, 112.9388),
            createAnnouncement("浏阳市2026年第一批城乡建设用地增减挂钩项目立项公示",
                "根据《湖南省城乡建设用地增减挂钩试点管理办法》等有关规定，浏阳市自然资源局对2026年第一批城乡建设用地增减挂钩项目进行了审查，拟同意立项。项目区总面积128.56公顷，涉及拆旧地块86个，拟复垦为农用地118.35公顷，其中耕地92.45公顷。涉及大瑶镇、澄潭江镇、中和镇三个乡镇。项目总投资估算6580万元。公示时间：2026年6月28日至2026年7月4日。",
                "浏阳市2026年第一批增减挂钩项目拟立项公示，涉及3个乡镇，总面积128.56公顷，拟复垦耕地92.45公顷，总投资6580万元。",
                2L, "通知公告", "浏阳市自然资源局", "浏阳市人民政府官网",
                "湖南省", "长沙市", "浏阳市", "大瑶镇", Announcement.AdminLevel.COUNTY.name(),
                "增减挂钩,项目公示,土地复垦", "通知公告,增减挂钩",
                0, 0, 0,
                "AI分析：该项目实施后可有效增加耕地面积，优化城乡用地结构，支持乡村振兴。项目选址合理，复垦潜力较大。",
                "[{\"time\":\"2026-06-28\",\"event\":\"公示发布\"},{\"time\":\"2026-07-04\",\"event\":\"公示截止\"},{\"time\":\"2026-07-15\",\"event\":\"预计立项\"}]",
                "增减挂钩,项目公示,土地复垦",
                LocalDateTime.of(2026, 6, 28, 10, 0), 28.2282, 112.9388),
            createAnnouncement("宁乡市巷子口镇推进全域土地综合整治助力乡村振兴",
                "宁乡市巷子口镇以全域土地综合整治试点为抓手，统筹推进农用地整理、建设用地整理和乡村生态保护修复，有效优化了土地利用结构，提升了耕地质量，改善了农村人居环境，为乡村振兴注入了强劲动力。主要成效：新增耕地面积1850亩，耕地质量平均提升1个等级；农村建设用地减少1200亩；农民人均增收2800元；引进农业企业5家，壮大了村集体经济。",
                "宁乡市巷子口镇推进全域土地综合整治，建成高标准农田8500亩，新增耕地1850亩，带动农民人均增收2800元，助力乡村振兴成效显著。",
                3L, "土地新闻", "宁乡市自然资源局", "长沙晚报",
                "湖南省", "长沙市", "宁乡市", "巷子口镇", Announcement.AdminLevel.COUNTY.name(),
                "全域土地综合整治,乡村振兴,宁乡", "土地新闻,乡村振兴",
                0, 0, 0,
                "AI分析：巷子口镇全域土地综合整治模式成效显著，实现了耕地增加、用地集约、农民增收、生态改善的多赢局面，值得总结推广。",
                "[{\"time\":\"2024-03\",\"event\":\"试点启动\"},{\"time\":\"2025-06\",\"event\":\"工程实施\"},{\"time\":\"2026-06\",\"event\":\"成效显现\"}]",
                "全域土地综合整治,乡村振兴,宁乡",
                LocalDateTime.of(2026, 6, 19, 9, 20), 28.2282, 112.9388),
            createAnnouncement("长沙市智慧自然资源综合监管平台上线运行",
                "近日，长沙市智慧自然资源综合监管平台正式上线运行。该平台融合卫星遥感、无人机航测、物联网、大数据、人工智能等先进技术，构建了'天空地一体化'的自然资源监测监管体系，实现了对自然资源的全面感知、实时监测、智能分析和精准管控。主要功能包括：国土空间基础信息平台、卫星遥感监测系统、无人机巡查系统、物联网监测系统、执法监察系统、耕地保护监管系统等。",
                "长沙市智慧自然资源综合监管平台上线，融合卫星遥感、无人机、物联网、AI等技术，构建'天空地一体化'监测体系，监测执法效能大幅提升。",
                4L, "行业动态", "市自然资源和规划局", "智慧长沙",
                "湖南省", "长沙市", "岳麓区", Announcement.AdminLevel.CITY.name(),
                "智慧国土,自然资源,监管平台", "行业动态,智慧国土",
                0, 0, 0,
                "AI分析：该平台建成后将大幅提升自然资源监测监管能力，违法用地发现时间从3个月缩短到1周以内，发现准确率提高到95%以上。",
                "[{\"time\":\"2025-03\",\"event\":\"项目启动\"},{\"time\":\"2025-12\",\"event\":\"系统开发\"},{\"time\":\"2026-05\",\"event\":\"试运行\"},{\"time\":\"2026-06-14\",\"event\":\"正式上线\"}]",
                "智慧国土,自然资源,监管平台",
                LocalDateTime.of(2026, 6, 14, 14, 30), 28.2282, 112.9388),
            createAnnouncement("长沙农村产权交易市场运行情况良好",
                "自长沙农村产权交易中心成立以来，我市农村产权交易市场运行平稳，交易规模持续扩大，交易品种不断丰富，为盘活农村资源资产、促进农民增收发挥了重要作用。截至2026年6月，全市累计完成农村产权交易项目3268宗，交易金额达58.6亿元。交易品种涵盖土地经营权流转、林权交易、养殖水面经营权、农村集体经营性资产等12大类。",
                "长沙农村产权交易市场运行良好，累计交易3268宗、金额58.6亿元，涵盖12大类交易品种，市场功能逐步完善。",
                6L, "市场信息", "市农业农村局", "长沙农村产权交易中心",
                "湖南省", "长沙市", "天心区", Announcement.AdminLevel.CITY.name(),
                "农村产权交易,土地流转,农村改革", "市场信息,产权交易",
                0, 0, 0,
                "AI分析：农村产权交易市场的建立和完善，对盘活农村资源资产、增加农民财产性收入、促进农村经济发展具有重要意义。",
                "[{\"time\":\"2023-06\",\"event\":\"中心成立\"},{\"time\":\"2024-12\",\"event\":\"体系建成\"},{\"time\":\"2026-06\",\"event\":\"成效显著\"}]",
                "农村产权交易,土地流转,农村改革",
                LocalDateTime.of(2026, 6, 26, 11, 20), 28.2282, 112.9388),
            createAnnouncement("无人机在土地资源监测中的应用技术指南",
                "无人机技术凭借其机动灵活、高分辨率、成本低等优势，已成为土地资源监测的重要技术手段。本指南介绍无人机在土地资源监测中的应用方法和技术要求。主要内容包括：无人机系统组成（飞行平台、任务载荷、地面站、数据处理系统）；主要应用场景（土地利用动态监测、违法用地监测、耕地保护监测、土地整治监测、地质灾害监测、土地调查）；作业技术流程（前期准备、外业飞行、内业处理）；技术质量要求（影像分辨率、平面精度、高程精度、重叠度）。",
                "无人机土地资源监测技术指南，介绍系统组成、应用场景、作业流程和质量要求，为无人机土地监测工作提供技术参考。",
                5L, "技术指南", "技术部", "技术手册",
                "湖南省", "长沙市", "开福区", Announcement.AdminLevel.CITY.name(),
                "无人机,土地监测,遥感技术", "技术指南,无人机",
                0, 0, 0,
                "AI分析：无人机技术在土地监测中的应用越来越广泛，该指南对规范无人机作业、提高监测质量具有重要指导意义。",
                "[{\"time\":\"2026-05-20\",\"event\":\"指南编写\"},{\"time\":\"2026-06-05\",\"event\":\"专家评审\"},{\"time\":\"2026-06-11\",\"event\":\"发布实施\"}]",
                "无人机,土地监测,遥感技术",
                LocalDateTime.of(2026, 6, 11, 16, 0), 28.2282, 112.9388),
            createAnnouncement("关于开展2026年度全市土壤污染状况详查的通知",
                "各区县（市）人民政府，市直有关部门：为深入贯彻《中华人民共和国土壤污染防治法》，全面掌握我市土壤环境质量状况，科学有序推进土壤污染防治工作，根据国家和省统一部署，决定开展2026年度全市土壤污染状况详查工作。调查范围包括农用地土壤污染状况详查和重点行业企业用地土壤污染状况调查。时间安排：准备阶段（2026年7月-8月）、调查阶段（2026年9月-2027年3月）、总结阶段（2027年4月-6月）。",
                "长沙市启动2026年度土壤污染状况详查，涵盖农用地和重点行业企业用地，查明土壤污染状况，为土壤污染防治提供科学依据。",
                2L, "通知公告", "市生态环境局", "长沙市生态环境局官网",
                "湖南省", "长沙市", "雨花区", Announcement.AdminLevel.CITY.name(),
                "土壤污染,土壤调查,环境保护", "通知公告,土壤污染",
                0, 0, 0,
                "AI分析：本次土壤污染状况详查将全面摸清全市土壤环境质量家底，为土壤污染防治和土壤环境管理提供科学依据。",
                "[{\"time\":\"2026-06-27\",\"event\":\"通知发布\"},{\"time\":\"2026-07\",\"event\":\"准备阶段\"},{\"time\":\"2026-09\",\"event\":\"调查阶段\"}]",
                "土壤污染,土壤调查,环境保护",
                LocalDateTime.of(2026, 6, 27, 10, 30), 28.2282, 112.9388)
        };
        for (Announcement announcement : announcements) {
            announcementMapper.insert(announcement);
        }
        log.info("[Mock数据] 公告Mock数据初始化完成，共 {} 条", announcements.length);
    }

    private Announcement createAnnouncement(String title, String content, String summary,
                                             Long categoryId, String categoryName,
                                             String author, String source,
                                             String province, String city, String adminLevel,
                                             String keywords, String tags,
                                             int isTop, int isRecommend, int isHot,
                                             String aiSummary, String aiTimeline, String aiKeywords,
                                             LocalDateTime publishTime, double lat, double lng) {
        return createAnnouncement(title, content, summary, categoryId, categoryName, author, source,
            province, city, null, null, adminLevel, keywords, tags, isTop, isRecommend, isHot,
            aiSummary, aiTimeline, aiKeywords, publishTime, lat, lng);
    }

    private Announcement createAnnouncement(String title, String content, String summary,
                                             Long categoryId, String categoryName,
                                             String author, String source,
                                             String province, String city, String county, String adminLevel,
                                             String keywords, String tags,
                                             int isTop, int isRecommend, int isHot,
                                             String aiSummary, String aiTimeline, String aiKeywords,
                                             LocalDateTime publishTime, double lat, double lng) {
        return createAnnouncement(title, content, summary, categoryId, categoryName, author, source,
            province, city, county, null, adminLevel, keywords, tags, isTop, isRecommend, isHot,
            aiSummary, aiTimeline, aiKeywords, publishTime, lat, lng);
    }

    private Announcement createAnnouncement(String title, String content, String summary,
                                             Long categoryId, String categoryName,
                                             String author, String source,
                                             String province, String city, String county, String township,
                                             String adminLevel,
                                             String keywords, String tags,
                                             int isTop, int isRecommend, int isHot,
                                             String aiSummary, String aiTimeline, String aiKeywords,
                                             LocalDateTime publishTime, double lat, double lng) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setSummary(summary);
        a.setCategoryId(categoryId);
        a.setCategoryName(categoryName);
        a.setAuthor(author);
        a.setSource(source);
        a.setProvince(province);
        a.setCity(city);
        a.setCounty(county);
        a.setTownship(township);
        a.setAdminLevel(adminLevel);
        a.setKeywords(keywords);
        a.setTags(tags);
        a.setIsTop(isTop);
        a.setIsRecommend(isRecommend);
        a.setIsHot(isHot);
        a.setAiSummary(aiSummary);
        a.setAiTimeline(aiTimeline);
        a.setAiKeywords(aiKeywords);
        a.setLatitude(lat);
        a.setLongitude(lng);
        a.setStatus(Announcement.Status.PUBLISHED.name());
        a.setAuditStatus(Announcement.AuditStatus.PASSED.name());
        a.setPublishTime(publishTime);
        a.setViewCount((int)(Math.random() * 1000 + 100));
        a.setLikeCount((int)(Math.random() * 100 + 10));
        a.setCommentCount((int)(Math.random() * 50 + 5));
        a.setPriority(isTop == 1 ? 1 : 0);
        a.setIsDeleted(0);
        return a;
    }

    private void initApprovalTasks() {
        Long count = approvalTaskMapper.selectCount(new LambdaQueryWrapper<ApprovalTask>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 审批任务数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化审批任务Mock数据...");
        ApprovalTask[] tasks = {
            createApprovalTask("STANDARD", "望城区乔口镇盘龙岭村土地整治项目立项审批",
                "张三", "土地整治科", "科室负责人审核", "dept_leader",
                "PROCESSING", 2,
                "{\"projectName\":\"盘龙岭村土地整治项目\",\"projectArea\":1200,\"investment\":480,\"location\":\"望城区乔口镇盘龙岭村\"}",
                LocalDateTime.of(2026, 6, 28, 17, 0),
                LocalDateTime.of(2026, 6, 25, 9, 0)),
            createApprovalTask("MATERIAL", "无人机LiDAR设备采购申请",
                "李四", "技术装备科", "财务审核", "finance_review",
                "PROCESSING", 1,
                "{\"item\":\"DJI Matrice 350 RTK + LiDAR套装\",\"quantity\":2,\"amount\":680000,\"supplier\":\"某某科技有限公司\"}",
                LocalDateTime.of(2026, 6, 30, 17, 0),
                LocalDateTime.of(2026, 6, 26, 14, 0)),
            createApprovalTask("DRONE_FLIGHT", "岳麓区莲花镇无人机飞行任务审批",
                "王五", "外业采集部", "空域管制审批", "airspace_approval",
                "PASSED", 2,
                "{\"missionCode\":\"ZRS-2026-0630-001\",\"location\":\"岳麓区莲花镇\",\"flightTime\":\"2026-06-30\",\"altitude\":120,\"duration\":60}",
                LocalDateTime.of(2026, 6, 29, 12, 0),
                LocalDateTime.of(2026, 6, 24, 10, 0)),
            createApprovalTask("EMERGENCY", "望城区乔口镇地质灾害应急调查任务",
                "赵六", "应急监测科", "—", "—",
                "PASSED", 3,
                "{\"event\":\"乔口镇滑坡隐患\",\"level\":\"二级\",\"location\":\"望城区乔口镇盘龙岭村\",\"team\":\"应急调查一组\"}",
                LocalDateTime.of(2026, 6, 27, 14, 0),
                LocalDateTime.of(2026, 6, 27, 8, 30)),
            createApprovalTask("STANDARD", "长沙县土地质量地球化学调查项目立项",
                "钱七", "调查评价科", "分管领导审批", "leader_review",
                "REJECTED", 1,
                "{\"projectName\":\"长沙县土地质量地球化学调查\",\"surveyArea\":500,\"budget\":1200000,\"period\":\"2026.07-2026.12\"}",
                LocalDateTime.of(2026, 6, 28, 17, 0),
                LocalDateTime.of(2026, 6, 20, 15, 0)),
            createApprovalTask("DRONE_FLIGHT", "开福区青竹湖无人机航摄任务",
                "孙八", "外业采集部", "部门负责人审批", "dept_leader",
                "PROCESSING", 1,
                "{\"missionCode\":\"ZRS-2026-0702-001\",\"location\":\"开福区青竹湖\",\"flightTime\":\"2026-07-02\",\"altitude\":150,\"duration\":45}",
                LocalDateTime.of(2026, 7, 1, 17, 0),
                LocalDateTime.of(2026, 6, 27, 11, 0))
        };
        for (ApprovalTask task : tasks) {
            approvalTaskMapper.insert(task);
        }
        log.info("[Mock数据] 审批任务Mock数据初始化完成，共 {} 条", tasks.length);
    }

    private ApprovalTask createApprovalTask(String bizType, String bizTitle,
                                             String applicantName, String applicantDept,
                                             String curStep, String curStepKey,
                                             String status, int priority,
                                             String bizData,
                                             LocalDateTime slaDeadline, LocalDateTime createdTime) {
        ApprovalTask t = new ApprovalTask();
        t.setBizType(bizType);
        t.setBizTitle(bizTitle);
        t.setApplicantId((long)(Math.random() * 100 + 1));
        t.setApplicantName(applicantName);
        t.setApplicantDept(applicantDept);
        t.setCurStep(curStep);
        t.setCurStepKey(curStepKey);
        t.setStatus(status);
        t.setPriority(priority);
        t.setBizData(bizData);
        t.setSlaDeadline(slaDeadline);
        t.setCreatedTime(createdTime);
        t.setUpdatedTime(createdTime);
        t.setIsDeleted(0);
        return t;
    }

    private void initReportTemplates() {
        Long count = reportTemplateMapper.selectCount(new LambdaQueryWrapper<ReportTemplate>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 报表模板数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化报表模板Mock数据...");
        ReportTemplate[] templates = {
            createReportTemplate("DASHBOARD_OVERVIEW", "仪表盘总览",
                ReportTemplate.TemplateType.STANDARD.name(),
                ReportTemplate.Category.DASHBOARD.name(),
                "系统整体数据概览仪表盘，包含数据总览、趋势分析、分类占比等",
                "PDF",
                "[{\"name\":\"数据总览\",\"type\":\"stats\"},{\"name\":\"近期趋势\",\"type\":\"chart\"},{\"name\":\"分类占比\",\"type\":\"chart\"}]",
                "[{\"key\":\"timeRange\",\"label\":\"时间范围\",\"type\":\"select\",\"options\":[\"今日\",\"本周\",\"本月\"]}]"),
            createReportTemplate("SOIL_ANALYSIS", "土质分析报表",
                ReportTemplate.TemplateType.STANDARD.name(),
                ReportTemplate.Category.SOIL.name(),
                "土壤采样数据分析报表，包含样品统计、成分分析、分类结果、标准对比",
                "PDF",
                "[{\"name\":\"样品统计\",\"type\":\"stats\"},{\"name\":\"成分分析\",\"type\":\"chart\"},{\"name\":\"分类结果\",\"type\":\"table\"},{\"name\":\"标准对比\",\"type\":\"chart\"}]",
                "[{\"key\":\"missionCode\",\"label\":\"任务编码\",\"type\":\"input\"},{\"key\":\"soilType\",\"label\":\"土壤类型\",\"type\":\"select\",\"options\":[\"壤土\",\"黏土\",\"砂土\"]}]"),
            createReportTemplate("DISASTER_RISK", "灾害风险报表",
                ReportTemplate.TemplateType.STANDARD.name(),
                ReportTemplate.Category.DISASTER.name(),
                "地质灾害风险分析报表，包含风险分布、灾害类型统计、趋势分析",
                "PDF",
                "[{\"name\":\"风险分布\",\"type\":\"stats\"},{\"name\":\"灾害类型统计\",\"type\":\"chart\"},{\"name\":\"趋势分析\",\"type\":\"chart\"},{\"name\":\"灾害风险详情\",\"type\":\"table\"}]",
                "[{\"key\":\"region\",\"label\":\"区域\",\"type\":\"input\"},{\"key\":\"riskLevel\",\"label\":\"风险等级\",\"type\":\"select\",\"options\":[\"高\",\"中\",\"低\"]}]"),
            createReportTemplate("ROCK_STRATUM", "岩层分析报表",
                ReportTemplate.TemplateType.STANDARD.name(),
                ReportTemplate.Category.ROCK.name(),
                "岩层结构分析报表，包含钻孔数据、岩层分布、成分分析",
                "EXCEL",
                "[{\"name\":\"钻孔数据\",\"type\":\"stats\"},{\"name\":\"岩层分布\",\"type\":\"chart\"},{\"name\":\"成分分析\",\"type\":\"table\"}]",
                "[{\"key\":\"projectName\",\"label\":\"项目名称\",\"type\":\"input\"},{\"key\":\"analysisType\",\"label\":\"分析类型\",\"type\":\"select\",\"options\":[\"综合勘察\",\"地质雷达\"]}]"),
            createReportTemplate("DEVICE_STATS", "设备统计报表",
                ReportTemplate.TemplateType.STANDARD.name(),
                ReportTemplate.Category.DEVICE.name(),
                "设备管理统计报表，包含设备总览、设备详情列表",
                "EXCEL",
                "[{\"name\":\"设备总览\",\"type\":\"stats\"},{\"name\":\"设备详情列表\",\"type\":\"table\"}]",
                "[{\"key\":\"deviceType\",\"label\":\"设备类型\",\"type\":\"select\",\"options\":[\"无人机\",\"激光雷达\",\"地质雷达\"]},{\"key\":\"status\",\"label\":\"设备状态\",\"type\":\"select\",\"options\":[\"在线\",\"离线\",\"工作中\"]}]")
        };
        for (ReportTemplate template : templates) {
            reportTemplateMapper.insert(template);
        }
        log.info("[Mock数据] 报表模板Mock数据初始化完成，共 {} 条", templates.length);
    }

    private ReportTemplate createReportTemplate(String code, String name, String type, String category,
                                                String description, String outputFormat,
                                                String templateContent, String parameters) {
        ReportTemplate t = new ReportTemplate();
        t.setTemplateCode(code);
        t.setTemplateName(name);
        t.setTemplateType(type);
        t.setCategory(category);
        t.setDescription(description);
        t.setTemplateContent(templateContent);
        t.setDataSource("{}");
        t.setParameters(parameters);
        t.setOutputFormat(outputFormat);
        t.setStatus(ReportTemplate.Status.ACTIVE.name());
        t.setIsDeleted(0);
        return t;
    }

    private void initExternalCompanies() {
        Long count = externalCompanyMapper.selectCount(new LambdaQueryWrapper<ExternalCompany>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 外部公司数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化外部公司数据...");
        ExternalCompany[] companies = {
            createExternalCompany("湖南省地质调查院", "HUNAN-GEO", "张工", "13800138001", 
                "zhang@hunan-geo.gov.cn", "https://api.hunan-geo.gov.cn/callback", 
                "soil-analysis,disaster-risk,geo-standards", "ACTIVE"),
            createExternalCompany("中化地质测绘院", "ZH-GEO", "李工", "13900139002",
                "li@zh-geo.com", "https://api.zh-geo.com/webhook",
                "soil-analysis,rock-stratum,land-plot", "ACTIVE"),
            createExternalCompany("长沙智土科技有限公司", "ZHITU-TECH", "王工", "13700137003",
                "wang@zhitu-tech.com", "https://api.zhitu-tech.com/callback",
                "all", "ACTIVE")
        };
        for (ExternalCompany company : companies) {
            externalCompanyMapper.insert(company);
        }
        log.info("[Mock数据] 外部公司数据初始化完成，共 {} 条", companies.length);
    }

    private ExternalCompany createExternalCompany(String name, String code, String contact, String phone,
                                                  String email, String callbackUrl, String dataScope, String status) {
        ExternalCompany c = new ExternalCompany();
        c.setCompanyName(name);
        c.setCompanyCode(code);
        c.setContactPerson(contact);
        c.setContactPhone(phone);
        c.setContactEmail(email);
        c.setCallbackUrl(callbackUrl);
        c.setDataScope(dataScope);
        c.setStatus(status);
        c.setApiConfig("{}");
        c.setIsDeleted(0);
        return c;
    }

    private void initApiKeys() {
        Long count = apiKeyMapper.selectCount(new LambdaQueryWrapper<ApiKey>());
        if (count != null && count > 0) {
            log.info("[Mock数据] API Key数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化API Key数据...");
        ApiKey[] keys = {
            createApiKey(1L, "湖南省地质调查院", "ZRWS_HN_GEO_20260601", 
                "ZRWS_SEC_HN_GEO_SECRET_2026", "soil-analysis:read,disaster-risk:read,geo-standards:read", 1000),
            createApiKey(2L, "中化地质测绘院", "ZRWS_ZHGEO_20260615",
                "ZRWS_SEC_ZHGEO_SECRET_2026", "soil-analysis:read,rock-stratum:read,land-plot:read", 500),
            createApiKey(3L, "长沙智土科技有限公司", "ZRWS_ZTTECH_20260620",
                "ZRWS_SEC_ZTTECH_SECRET_2026", "all", 2000)
        };
        for (ApiKey key : keys) {
            apiKeyMapper.insert(key);
        }
        log.info("[Mock数据] API Key数据初始化完成，共 {} 条", keys.length);
    }

    private ApiKey createApiKey(Long companyId, String companyName, String apiKey, String apiSecret,
                                String permissions, int rateLimit) {
        ApiKey k = new ApiKey();
        k.setCompanyId(companyId);
        k.setCompanyName(companyName);
        k.setApiKey(apiKey);
        k.setApiSecret(apiSecret);
        k.setPermissions(permissions);
        k.setRateLimit(rateLimit);
        k.setRequestCount(0);
        k.setStatus("ACTIVE");
        k.setExpireTime(LocalDateTime.now().plusYears(1));
        k.setIsDeleted(0);
        return k;
    }

    private void initSysConfigs() {
        Long count = sysConfigMapper.selectCount(new LambdaQueryWrapper<SysConfig>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 系统配置数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化系统配置Mock数据...");
        SysConfig[] configs = {
            createSysConfig("site_name", "智壤卫士", "STRING", "站点名称", "general", "系统显示的名称", 1),
            createSysConfig("default_theme", "light", "STRING", "默认主题", "display", "新用户默认使用的主题", 2),
            createSysConfig("enable_register", "false", "BOOLEAN", "开放注册", "security", "是否允许用户自行注册", 3),
            createSysConfig("session_timeout", "30", "INT", "会话超时（分钟）", "security", "用户登录超时时间", 4),
            createSysConfig("password_min_length", "8", "INT", "密码最小长度", "security", "用户密码最小字符数", 5),
            createSysConfig("password_strong", "true", "BOOLEAN", "强密码要求", "security", "要求包含大小写字母、数字、特殊字符", 6),
            createSysConfig("login_max_attempts", "5", "INT", "最大登录尝试", "security", "连续失败次数后锁定账户", 7),
            createSysConfig("enable_captcha", "false", "BOOLEAN", "启用验证码", "security", "登录时显示图形验证码", 8),
            createSysConfig("announcement_enabled", "true", "BOOLEAN", "启用公告", "content", "是否显示系统公告栏", 9),
            createSysConfig("file_max_size", "50", "INT", "文件上传大小（MB）", "content", "单文件最大上传大小", 10),
            createSysConfig("enable_audit", "true", "BOOLEAN", "内容审核", "content", "发布内容是否需要审核", 11),
            createSysConfig("sidebar_width", "220", "INT", "侧边栏宽度（px）", "display", "左侧菜单宽度", 12),
            createSysConfig("animation_enabled", "true", "BOOLEAN", "启用动画", "display", "是否启用页面切换动画", 13),
            createSysConfig("show_breadcrumb", "true", "BOOLEAN", "显示面包屑", "display", "是否显示页面顶部面包屑导航", 14),
            createSysConfig("font_size", "medium", "STRING", "字体大小", "display", "全局默认字体大小", 15)
        };
        for (SysConfig config : configs) {
            sysConfigMapper.insert(config);
        }
        log.info("[Mock数据] 系统配置Mock数据初始化完成，共 {} 条", configs.length);
    }

    private SysConfig createSysConfig(String key, String value, String type, String name,
                                       String group, String description, int sort) {
        SysConfig c = new SysConfig();
        c.setConfigKey(key);
        c.setConfigValue(value);
        c.setConfigType(type);
        c.setConfigName(name);
        c.setConfigGroup(group);
        c.setDescription(description);
        c.setSortOrder(sort);
        c.setStatus(1);
        c.setIsSystem(1);
        c.setIsDeleted(0);
        return c;
    }

    private void initExportTasks() {
        Long count = exportTaskMapper.selectCount(new LambdaQueryWrapper<ExportTask>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 导出任务数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化导出任务Mock数据...");
        ExportTask[] tasks = {
            createExportTask("EXP-20260617-001", "乔口镇土壤采样数据导出", "SOIL_SAMPLE", "DATA_EXPORT", "EXCEL",
                "[{\"field\":\"sampleCode\",\"op\":\"LIKE\",\"value\":\"SP-\"}]",
                "[\"sampleCode\",\"missionCode\",\"phValue\",\"organicMatter\",\"soilType\",\"collectTime\"]",
                "/exports/soil_sample_20260617.xlsx", "土壤采样数据_20260617.xlsx",
                256000L, 128, "SUCCESS", "王工",
                LocalDateTime.of(2026, 6, 17, 14, 0), LocalDateTime.of(2026, 6, 17, 14, 5)),
            createExportTask("EXP-20260616-001", "莲花镇飞行任务报告", "FLIGHT_MISSION", "DATA_EXPORT", "PDF",
                "[]", "[\"missionCode\",\"areaName\",\"droneId\",\"flightTime\",\"duration\",\"coverage\"]",
                "/exports/flight_mission_20260616.pdf", "飞行任务报告_20260616.pdf",
                1890000L, 56, "SUCCESS", "李工",
                LocalDateTime.of(2026, 6, 16, 16, 0), LocalDateTime.of(2026, 6, 16, 16, 12)),
            createExportTask("EXP-20260615-001", "灾害风险评估报表", "DISASTER_RISK", "DATA_EXPORT", "EXCEL",
                "[{\"field\":\"riskLevel\",\"op\":\"EQ\",\"value\":\"MEDIUM\"}]",
                "[\"riskCode\",\"region\",\"disasterType\",\"riskLevel\",\"riskScore\",\"assessmentTime\"]",
                "/exports/disaster_risk_20260615.xlsx", "灾害风险评估_20260615.xlsx",
                89000L, 32, "SUCCESS", "张工",
                LocalDateTime.of(2026, 6, 15, 10, 0), LocalDateTime.of(2026, 6, 15, 10, 2)),
            createExportTask("EXP-20260618-001", "设备台账导出", "DEVICE", "DATA_EXPORT", "EXCEL",
                "[]", "[\"deviceCode\",\"deviceName\",\"deviceType\",\"deviceModel\",\"status\",\"owner\"]",
                null, null, null, 0, "PROCESSING", "系统",
                LocalDateTime.of(2026, 6, 18, 9, 0), null),
            createExportTask("EXP-20260614-001", "审批数据导出", "APPROVAL_TASK", "DATA_EXPORT", "CSV",
                "[{\"field\":\"status\",\"op\":\"EQ\",\"value\":\"PROCESSING\"}]",
                "[\"bizType\",\"bizTitle\",\"applicantName\",\"curStep\",\"status\",\"createdTime\"]",
                "/exports/approval_20260614.csv", "审批数据_20260614.csv",
                45000L, 24, "FAILED", "系统",
                LocalDateTime.of(2026, 6, 14, 11, 0), LocalDateTime.of(2026, 6, 14, 11, 1)),
            createExportTask("EXP-20260613-001", "岩层分析数据", "ROCK_STRATUM", "DATA_EXPORT", "EXCEL",
                "[]", "[\"analysisCode\",\"projectName\",\"location\",\"analysisType\",\"stratumCount\",\"riskLevel\"]",
                "/exports/rock_stratum_20260613.xlsx", "岩层分析数据_20260613.xlsx",
                156000L, 18, "SUCCESS", "李工",
                LocalDateTime.of(2026, 6, 13, 15, 0), LocalDateTime.of(2026, 6, 13, 15, 8))
        };
        for (ExportTask task : tasks) {
            exportTaskMapper.insert(task);
        }
        log.info("[Mock数据] 导出任务Mock数据初始化完成，共 {} 条", tasks.length);
    }

    private ExportTask createExportTask(String taskNo, String taskName, String boCode, String exportType,
                                         String fileFormat, String filterConditions, String fieldList,
                                         String filePath, String fileName, Long fileSize, Integer totalRows,
                                         String status, String operatorName,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        ExportTask t = new ExportTask();
        t.setTaskNo(taskNo);
        t.setTaskName(taskName);
        t.setBoCode(boCode);
        t.setExportType(exportType);
        t.setFileFormat(fileFormat);
        t.setFilterConditions(filterConditions);
        t.setFieldList(fieldList);
        t.setFilePath(filePath);
        t.setFileName(fileName);
        t.setFileSize(fileSize);
        t.setTotalRows(totalRows);
        t.setStatus(status);
        t.setOperatorName(operatorName);
        t.setStartTime(startTime);
        t.setEndTime(endTime);
        t.setIsDeleted(0);
        return t;
    }

    private void initDataStatistics() {
        Long count = dataStatisticsMapper.selectCount(new LambdaQueryWrapper<DataStatistics>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 数据统计数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化数据统计Mock数据...");
        LocalDate today = LocalDate.now();
        DataStatistics[] stats = {
            createDataStats(today.minusDays(0).toString(), "SOIL_SAMPLE", "土壤采样", "IMPORT", 156, 148, 8, 0, 0, 0, 3, 156, 92, 85.5, "DAILY"),
            createDataStats(today.minusDays(1).toString(), "SOIL_SAMPLE", "土壤采样", "IMPORT", 203, 195, 8, 0, 0, 0, 4, 203, 94, 72.3, "DAILY"),
            createDataStats(today.minusDays(2).toString(), "SOIL_SAMPLE", "土壤采样", "IMPORT", 178, 172, 6, 0, 0, 0, 3, 178, 96, 68.7, "DAILY"),
            createDataStats(today.minusDays(0).toString(), "FLIGHT_MISSION", "飞行任务", "CREATE", 5, 5, 0, 3, 1, 1, 0, 5, 100, 125.0, "DAILY"),
            createDataStats(today.minusDays(1).toString(), "FLIGHT_MISSION", "飞行任务", "CREATE", 8, 8, 0, 6, 1, 1, 0, 8, 100, 98.5, "DAILY"),
            createDataStats(today.minusDays(0).toString(), "APPROVAL_TASK", "审批任务", "SUBMIT", 12, 0, 0, 8, 3, 1, 0, 12, 67, 45.2, "DAILY"),
            createDataStats(today.minusDays(1).toString(), "APPROVAL_TASK", "审批任务", "SUBMIT", 18, 0, 0, 12, 4, 2, 0, 18, 67, 52.8, "DAILY"),
            createDataStats(today.minusDays(0).toString(), "DISASTER_RISK", "灾害风险", "ASSESSMENT", 4, 4, 0, 0, 0, 0, 0, 4, 100, 38.6, "DAILY"),
            createDataStats(today.minusDays(7).toString(), "SOIL_SAMPLE", "土壤采样", "IMPORT", 1256, 1210, 46, 0, 0, 0, 25, 1256, 96, 92.5, "WEEKLY"),
            createDataStats(today.minusDays(30).toString(), "SOIL_SAMPLE", "土壤采样", "IMPORT", 5280, 5120, 160, 0, 0, 0, 98, 5280, 97, 88.3, "MONTHLY"),
            createDataStats(today.minusDays(7).toString(), "FLIGHT_MISSION", "飞行任务", "CREATE", 42, 42, 0, 35, 5, 2, 0, 42, 100, 105.6, "WEEKLY"),
            createDataStats(today.minusDays(30).toString(), "FLIGHT_MISSION", "飞行任务", "CREATE", 168, 168, 0, 142, 18, 8, 0, 168, 100, 112.4, "MONTHLY"),
            createDataStats(today.minusDays(7).toString(), "APPROVAL_TASK", "审批任务", "SUBMIT", 96, 0, 0, 72, 18, 6, 0, 96, 75, 68.9, "WEEKLY"),
            createDataStats(today.minusDays(30).toString(), "APPROVAL_TASK", "审批任务", "SUBMIT", 385, 0, 0, 298, 62, 25, 0, 385, 77, 75.2, "MONTHLY"),
            createDataStats(today.minusDays(7).toString(), "DEVICE", "设备管理", "MAINTENANCE", 6, 6, 0, 0, 0, 0, 0, 6, 100, 24.5, "WEEKLY"),
            createDataStats(today.minusDays(7).toString(), "QUALITY_CHECK", "质量校验", "CHECK", 128, 122, 6, 0, 0, 0, 0, 128, 95, 42.8, "WEEKLY")
        };
        for (DataStatistics stat : stats) {
            dataStatisticsMapper.insert(stat);
        }
        log.info("[Mock数据] 数据统计Mock数据初始化完成，共 {} 条", stats.length);
    }

    private DataStatistics createDataStats(String date, String boCode, String boName, String opType,
                                            int total, int success, int failed,
                                            int approved, int rejected, int pending,
                                            int fileCount, int totalRecords, int qualityScore,
                                            double avgProcessTime, String period) {
        DataStatistics s = new DataStatistics();
        s.setStatsDate(date);
        s.setBoCode(boCode);
        s.setBoName(boName);
        s.setOperationType(opType);
        s.setTotalCount(total);
        s.setSuccessCount(success);
        s.setFailedCount(failed);
        s.setApprovedCount(approved);
        s.setRejectedCount(rejected);
        s.setPendingCount(pending);
        s.setFileCount(fileCount);
        s.setTotalRecords(totalRecords);
        s.setQualityScore(qualityScore);
        s.setAvgProcessTime(avgProcessTime);
        s.setPeriodType(period);
        s.setIsDeleted(0);
        return s;
    }

    private void initClimateWarmingData() {
        Long count = climateWarmingMapper.selectCount(new LambdaQueryWrapper<ClimateWarming>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 气候变暖监测数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化气候变暖监测Mock数据...");
        LocalDate today = LocalDate.now();
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};

        for (int i = 0; i < regions.length; i++) {
            double baseTemp = 20 + Math.random() * 8;
            double tempAnomaly = 0.3 + Math.random() * 0.8;
            double precip = 80 + Math.random() * 150;
            double precipAnomaly = -20 + Math.random() * 50;
            int highTempDays = (int)(3 + Math.random() * 15);
            int lowTempDays = (int)(Math.random() * 5);
            int droughtDays = (int)(5 + Math.random() * 25);
            int heatWaves = (int)(Math.random() * 3);
            double warmingRate = 0.15 + Math.random() * 0.35;
            double riskScore = 20 + Math.random() * 60;
            String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
            String trend = warmingRate > 0.4 ? "RAPID" : (warmingRate > 0.2 ? "MODERATE" : (warmingRate > 0.1 ? "SLOW" : "STABLE"));

            ClimateWarming cw = new ClimateWarming();
            cw.setRecordCode("CW-" + regionCodes[i] + "-" + today.toString().replace("-", ""));
            cw.setRegion(regions[i]);
            cw.setRegionCode(regionCodes[i]);
            cw.setLatitude(BigDecimal.valueOf(lats[i]));
            cw.setLongitude(BigDecimal.valueOf(lngs[i]));
            cw.setMonitorDate(today);
            cw.setAvgTemperature(BigDecimal.valueOf(baseTemp).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setMaxTemperature(BigDecimal.valueOf(baseTemp + 8 + Math.random() * 5).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setMinTemperature(BigDecimal.valueOf(baseTemp - 6 - Math.random() * 4).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setTemperatureAnomaly(BigDecimal.valueOf(tempAnomaly).setScale(2, BigDecimal.ROUND_HALF_UP));
            cw.setPrecipitation(BigDecimal.valueOf(precip).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setPrecipitationAnomaly(BigDecimal.valueOf(precipAnomaly).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setExtremeHighTempDays(highTempDays);
            cw.setExtremeLowTempDays(lowTempDays);
            cw.setDroughtDays(droughtDays);
            cw.setHeatWaveEvents(heatWaves);
            cw.setWarmingRate10y(BigDecimal.valueOf(warmingRate).setScale(2, BigDecimal.ROUND_HALF_UP));
            cw.setWarmingTrend(trend);
            cw.setRiskLevel(riskLevel);
            cw.setRiskScore(BigDecimal.valueOf(riskScore).setScale(1, BigDecimal.ROUND_HALF_UP));
            cw.setImpactAssessment("气温偏高" + String.format("%.1f", tempAnomaly) + "°C，对农业生产、生态系统有一定影响");
            cw.setAdaptationMeasures("加强节水灌溉、调整种植结构、完善高温预警机制");
            cw.setDataSource("气象站观测+卫星遥感");
            cw.setStatus("COMPLETED");
            cw.setAnalyst("系统自动分析");
            cw.setAnalysisTime(LocalDateTime.now());
            cw.setIsDeleted(0);
            climateWarmingMapper.insert(cw);
        }
        log.info("[Mock数据] 气候变暖监测Mock数据初始化完成，共 {} 条", regions.length);
    }

    private void initDesertificationData() {
        Long count = desertificationMapper.selectCount(new LambdaQueryWrapper<Desertification>());
        if (count != null && count > 0) {
            log.info("[Mock数据] 沙漠化监测数据已存在，跳过初始化");
            return;
        }
        log.info("[Mock数据] 开始初始化沙漠化监测Mock数据...");
        LocalDate today = LocalDate.now();
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};
        String[] types = {"WATER", "WIND", "WATER", "WATER", "WIND", "WIND", "WIND", "WATER", "WATER", "WIND", "WATER", "WATER", "WATER", "WIND"};

        for (int i = 0; i < regions.length; i++) {
            double vegCov = 35 + Math.random() * 55;
            double bareLand = 5 + Math.random() * 30;
            double sandHeight = 0.5 + Math.random() * 3;
            double migrationRate = 0.5 + Math.random() * 5;
            double soilOM = 0.5 + Math.random() * 2.5;
            double soilMoisture = 10 + Math.random() * 25;
            double aridity = 0.3 + Math.random() * 0.9;
            double windErosion = 100 + Math.random() * 3000;
            double desertArea = 10 + Math.random() * 200;
            double desertRatio = 2 + Math.random() * 25;
            double ldi = 0.2 + Math.random() * 0.6;
            double riskScore = 15 + Math.random() * 55;
            String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
            String grade = vegCov > 50 ? "MILD" : (vegCov > 30 ? "MODERATE" : (vegCov > 10 ? "SEVERE" : "EXTREME"));
            String climateType = aridity > 1.0 ? "HUMID" : (aridity > 0.65 ? "SEMI_HUMID" : (aridity > 0.3 ? "SEMI_ARID" : (aridity > 0.13 ? "ARID" : "HYPER_ARID")));
            String vegTrend = Math.random() > 0.6 ? "INCREASING" : (Math.random() > 0.3 ? "STABLE" : "DECREASING");

            Desertification ds = new Desertification();
            ds.setRecordCode("DS-" + regionCodes[i] + "-" + today.toString().replace("-", ""));
            ds.setRegion(regions[i]);
            ds.setRegionCode(regionCodes[i]);
            ds.setLatitude(BigDecimal.valueOf(lats[i]));
            ds.setLongitude(BigDecimal.valueOf(lngs[i]));
            ds.setMonitorDate(today);
            ds.setMonitorPeriod("MONTHLY");
            ds.setVegetationCoverage(BigDecimal.valueOf(vegCov).setScale(1, BigDecimal.ROUND_HALF_UP));
            ds.setVegetationTrend(vegTrend);
            ds.setBareLandRatio(BigDecimal.valueOf(bareLand).setScale(1, BigDecimal.ROUND_HALF_UP));
            ds.setSandDuneHeightAvg(BigDecimal.valueOf(sandHeight).setScale(2, BigDecimal.ROUND_HALF_UP));
            ds.setSandDuneMigrationRate(BigDecimal.valueOf(migrationRate).setScale(2, BigDecimal.ROUND_HALF_UP));
            ds.setSoilOrganicMatter(BigDecimal.valueOf(soilOM).setScale(2, BigDecimal.ROUND_HALF_UP));
            ds.setSoilMoisture(BigDecimal.valueOf(soilMoisture).setScale(1, BigDecimal.ROUND_HALF_UP));
            ds.setAridityIndex(BigDecimal.valueOf(aridity).setScale(3, BigDecimal.ROUND_HALF_UP));
            ds.setClimateType(climateType);
            ds.setWindErosionModulus(BigDecimal.valueOf(windErosion).setScale(0, BigDecimal.ROUND_HALF_UP));
            ds.setDesertificationType(types[i]);
            ds.setDesertificationGrade(grade);
            ds.setDesertificationArea(BigDecimal.valueOf(desertArea).setScale(2, BigDecimal.ROUND_HALF_UP));
            ds.setDesertificationRatio(BigDecimal.valueOf(desertRatio).setScale(1, BigDecimal.ROUND_HALF_UP));
            ds.setLandDegradationIndex(BigDecimal.valueOf(ldi).setScale(2, BigDecimal.ROUND_HALF_UP));
            ds.setRiskLevel(riskLevel);
            ds.setRiskScore(BigDecimal.valueOf(riskScore).setScale(1, BigDecimal.ROUND_HALF_UP));
            ds.setImpactAssessment("土地退化指数" + String.format("%.2f", ldi) + "，生态系统服务功能有所下降");
            ds.setControlMeasures("实施退耕还林还草、设置沙障、推广节水农业技术");
            ds.setDataSource("卫星遥感+地面调查");
            ds.setStatus("COMPLETED");
            ds.setAnalyst("系统自动分析");
            ds.setAnalysisTime(LocalDateTime.now());
            ds.setIsDeleted(0);
            desertificationMapper.insert(ds);
        }
        log.info("[Mock数据] 沙漠化监测Mock数据初始化完成，共 {} 条", regions.length);
    }
}
