package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Mock数据初始化器
 * 启动服务时检测到没有数据就自动插入
 */
@Slf4j
@Component
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

    @Override
    public void run(ApplicationArguments args) {
        try {
            initFlightMissions();
            initSoilSamples();
            initGpsTrackPoints();
            initLandPlots();
            initSoilClassifications();
            initRockStratumAnalyses();
            initDisasterRisks();
            initDevices();
            initQualityChecks();
            initAnnouncementCategories();
            initAnnouncements();
            initApprovalTasks();
            log.info("[Mock数据] Mock数据初始化完成");
        } catch (Exception e) {
            log.warn("[Mock数据] Mock数据初始化失败（可能表不存在）: {}", e.getMessage());
        }
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
            createAnnouncementCategory("LAND_REHAB", "土地整治", "land", "#52c41a", "土地整治项目相关公告", 2),
            createAnnouncementCategory("DISASTER", "灾害预警", "disaster", "#f5222d", "地质灾害预警信息", 3),
            createAnnouncementCategory("TECH", "技术动态", "tech", "#722ed1", "土地资源监测技术动态", 4),
            createAnnouncementCategory("PROJECT", "项目公示", "project", "#fa8c16", "项目招标及结果公示", 5)
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
            createAnnouncement("湖南省土地整治规划（2026-2030年）征求意见稿",
                "为贯彻落实国家关于土地资源保护与利用的总体要求，结合我省实际，省自然资源厅组织编制了《湖南省土地整治规划（2026-2030年）》（征求意见稿），现向社会公开征求意见。",
                "湖南省自然资源厅发布土地整治规划征求意见稿，规划期为2026-2030年，涉及全省14个市州，重点推进高标准农田建设、农村建设用地整理等六大工程。",
                1L, "政策法规", "省自然资源厅", "湖南省自然资源厅官网",
                "湖南省", "长沙市", Announcement.AdminLevel.PROVINCE.name(),
                "土地整治,规划,政策", "政策,规划", 1, 1, 1,
                "AI分析显示该规划覆盖全省范围，重点关注高标准农田建设和生态修复，预计将对全省土地利用格局产生深远影响。",
                "[{\"time\":\"2026-01-15\",\"event\":\"规划编制启动\"},{\"time\":\"2026-03-20\",\"event\":\"初稿完成\"},{\"time\":\"2026-06-25\",\"event\":\"公开征求意见\"},{\"time\":\"2026-08-01\",\"event\":\"预计正式发布\"}]",
                "土地整治,高标准农田,生态修复,规划",
                LocalDateTime.of(2026, 6, 25, 9, 0), 28.2282, 112.9388),
            createAnnouncement("长沙市2026年第二批土地整治项目招标公告",
                "长沙市自然资源和规划局决定对长沙市2026年第二批土地整治项目进行公开招标，项目涉及望城区、长沙县、浏阳市共8个乡镇，总投资约2.5亿元。",
                "长沙市发布2026年第二批土地整治项目招标公告，涉及3个区县8个乡镇，总投资2.5亿元，主要建设内容包括高标准农田建设、灌溉设施改造等。",
                5L, "项目公示", "市自然资源和规划局", "长沙市公共资源交易中心",
                "湖南省", "长沙市", Announcement.AdminLevel.CITY.name(),
                "招标,土地整治,项目", "招标,项目", 0, 1, 0,
                "AI分析：本批次项目集中在长沙东部和北部地区，有望显著提升区域农业生产条件和土地利用效率。",
                "[{\"time\":\"2026-06-20\",\"event\":\"招标公告发布\"},{\"time\":\"2026-07-10\",\"event\":\"投标截止\"},{\"time\":\"2026-07-15\",\"event\":\"开标\"},{\"time\":\"2026-07-20\",\"event\":\"中标公示\"}]",
                "招标,土地整治,高标准农田",
                LocalDateTime.of(2026, 6, 20, 10, 30), 28.2282, 112.9388),
            createAnnouncement("望城区地质灾害气象风险预警",
                "根据气象部门预报，未来24小时望城区部分区域将出现强降雨天气，发生地质灾害的风险较高。请相关部门和群众做好防范准备。",
                "望城区发布地质灾害气象风险预警，预警级别为黄色，受影响区域包括乔口镇、铜官街道等5个乡镇街道，提醒当地居民注意防范滑坡、泥石流等地质灾害。",
                3L, "灾害预警", "区应急管理局", "望城区气象局",
                "湖南省", "长沙市", "望城区", Announcement.AdminLevel.COUNTY.name(),
                "地质灾害,预警,降雨", "预警,灾害", 1, 0, 1,
                "AI分析显示强降雨主要集中在西北部山区，该区域地形坡度较大，土层较厚，滑坡风险较高。建议重点监测坡脚和沟口区域。",
                "[{\"time\":\"2026-06-27 08:00\",\"event\":\"预警发布\"},{\"time\":\"2026-06-27 14:00\",\"event\":\"预计强降雨开始\"},{\"time\":\"2026-06-28 02:00\",\"event\":\"预计降雨最强时段\"},{\"time\":\"2026-06-28 20:00\",\"event\":\"预计预警解除\"}]",
                "地质灾害,滑坡,降雨,预警",
                LocalDateTime.of(2026, 6, 27, 8, 0), 28.4567, 112.8352),
            createAnnouncement("无人机激光雷达在土地监测中的应用进展",
                "随着无人机技术和激光雷达技术的快速发展，土地资源监测手段正在发生革命性变化。本文综述了近年来无人机LiDAR技术在土地调查、灾害监测等领域的应用进展。",
                "技术动态文章综述无人机激光雷达技术在土地监测中的应用进展，涵盖地形测绘、土地利用调查、地质灾害监测等多个应用场景，技术精度和效率显著提升。",
                4L, "技术动态", "技术部", "自然资源学报",
                "湖南省", "长沙市", Announcement.AdminLevel.PROVINCE.name(),
                "无人机,激光雷达,土地监测", "技术,无人机", 0, 1, 0,
                "AI分析：无人机LiDAR技术已成为土地监测的重要手段，点云密度可达200点/平方米以上，高程精度优于5cm，工作效率是传统方法的10-20倍。",
                "[{\"time\":\"2020\",\"event\":\"技术起步阶段\"},{\"time\":\"2023\",\"event\":\"规模化应用\"},{\"time\":\"2025\",\"event\":\"AI智能分析普及\"},{\"time\":\"2026\",\"event\":\"实时监测成为可能\"}]",
                "无人机,LiDAR,激光雷达,土地监测,遥感",
                LocalDateTime.of(2026, 6, 15, 14, 0), 28.2282, 112.9388),
            createAnnouncement("乔口镇盘龙岭村土地整治项目公示",
                "根据望城区人民政府批复，乔口镇盘龙岭村土地整治项目已完成立项，现将项目基本情况予以公示，公示期为7天。项目建设规模1200亩，总投资480万元。",
                "乔口镇盘龙岭村土地整治项目公示，建设规模1200亩，总投资480万元，主要建设内容包括田块整治、灌溉排水、田间道路等，预计新增耕地面积50亩。",
                2L, "土地整治", "乔口镇政府", "望城区政府官网",
                "湖南省", "长沙市", "望城区", "乔口镇", Announcement.AdminLevel.TOWNSHIP.name(),
                "土地整治,盘龙岭村,公示", "土地整治,项目", 0, 0, 0,
                "AI分析：该项目实施后预计可新增耕地约50亩，耕地质量平均提升0.5个等级，农业生产条件将得到显著改善。",
                "[{\"time\":\"2026-06-10\",\"event\":\"项目立项\"},{\"time\":\"2026-06-25\",\"event\":\"项目公示\"},{\"time\":\"2026-07-05\",\"event\":\"预计开工\"},{\"time\":\"2026-12-31\",\"event\":\"预计竣工\"}]",
                "土地整治,盘龙岭村,高标准农田",
                LocalDateTime.of(2026, 6, 25, 16, 0), 28.4567, 112.8352),
            createAnnouncement("湖南省自然资源厅关于加强耕地保护的实施意见",
                "为深入贯彻党中央、国务院关于耕地保护的决策部署，坚决遏制耕地'非农化'、防止'非粮化'，结合我省实际，现就加强耕地保护工作提出如下实施意见。",
                "湖南省自然资源厅发布加强耕地保护实施意见，提出严格耕地用途管制、加强耕地占补平衡、强化耕地执法监督等八项具体措施，严守耕地保护红线。",
                1L, "政策法规", "省自然资源厅", "湖南省人民政府办公厅",
                "湖南省", "长沙市", Announcement.AdminLevel.PROVINCE.name(),
                "耕地保护,政策,实施意见", "政策,耕地", 1, 1, 0,
                "AI分析：该政策从多维度强化耕地保护，预计将有效遏制耕地减少趋势，对保障粮食安全具有重要意义。政策执行重点在城乡结合部和主要交通沿线。",
                "[{\"time\":\"2026-05-20\",\"event\":\"政策起草\"},{\"time\":\"2026-06-10\",\"event\":\"省政府审议通过\"},{\"time\":\"2026-06-28\",\"event\":\"正式发布实施\"}]",
                "耕地保护,非农化,非粮化,粮食安全",
                LocalDateTime.of(2026, 6, 28, 9, 0), 28.2282, 112.9388),
            createAnnouncement("岳麓区2026年地质灾害排查工作启动",
                "岳麓区自然资源局决定在全区范围内开展2026年度地质灾害隐患排查工作，全面掌握全区地质灾害隐患点分布及变化情况，保障人民群众生命财产安全。",
                "岳麓区启动2026年地质灾害排查工作，排查范围覆盖全区所有乡镇街道，重点排查山区、矿区、交通沿线等区域，采用无人机遥感+地面核查相结合的方式。",
                3L, "灾害预警", "区自然资源局", "岳麓区政府官网",
                "湖南省", "长沙市", "岳麓区", Announcement.AdminLevel.COUNTY.name(),
                "地质灾害,排查,岳麓区", "灾害,排查", 0, 0, 0,
                "AI分析显示岳麓区西部山区地质灾害风险较高，本次排查预计将新发现隐患点15-20处，需重点关注滑坡和崩塌类型。",
                "[{\"time\":\"2026-06-20\",\"event\":\"工作部署\"},{\"time\":\"2026-06-25\",\"event\":\"排查启动\"},{\"time\":\"2026-07-15\",\"event\":\"外业完成\"},{\"time\":\"2026-07-31\",\"event\":\"报告提交\"}]",
                "地质灾害,隐患排查,岳麓区",
                LocalDateTime.of(2026, 6, 20, 11, 0), 28.3857, 112.7893),
            createAnnouncement("土地资源智能监测技术研讨会在长沙召开",
                "由湖南省自然资源学会主办的'2026土地资源智能监测技术研讨会'于6月22日在长沙召开，来自全国各地的300余名专家学者参会，共同探讨土地监测新技术、新方法。",
                "土地资源智能监测技术研讨会在长沙召开，会议围绕AI+遥感、大数据分析、实时监测等热点话题展开交流，展示了多项最新技术成果。",
                4L, "技术动态", "学会秘书处", "湖南省自然资源学会",
                "湖南省", "长沙市", Announcement.AdminLevel.CITY.name(),
                "研讨会,技术,智能监测", "技术,会议", 0, 1, 0,
                "AI分析：本次会议反映了土地监测技术向智能化、实时化方向快速发展的趋势，AI技术已深度融入土地调查、监测、评价等各个环节。",
                "[{\"time\":\"2026-06-22 09:00\",\"event\":\"开幕式\"},{\"time\":\"2026-06-22 14:00\",\"event\":\"主题报告\"},{\"time\":\"2026-06-23 09:00\",\"event\":\"分论坛\"},{\"time\":\"2026-06-23 16:00\",\"event\":\"闭幕式\"}]",
                "智能监测,AI,遥感,大数据,研讨会",
                LocalDateTime.of(2026, 6, 22, 9, 0), 28.2282, 112.9388)
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
}
