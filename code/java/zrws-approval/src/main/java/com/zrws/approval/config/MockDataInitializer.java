package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Mock数据初始化器
 * 启动服务时检测到没有数据就自动插入
 */
@Slf4j
@Component
public class MockDataInitializer {

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

    @PostConstruct
    public void init() {
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
            log.info("Mock数据初始化完成");
        } catch (Exception e) {
            log.warn("Mock数据初始化失败（可能表不存在）: {}", e.getMessage());
        }
    }

    private void initFlightMissions() {
        Long count = flightMissionMapper.selectCount(new LambdaQueryWrapper<FlightMission>());
        if (count != null && count > 0) {
            log.info("飞行任务数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化飞行任务Mock数据...");
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
        log.info("飞行任务Mock数据初始化完成，共 {} 条", missions.length);
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
            log.info("土壤采样数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化土壤采样Mock数据...");
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
        log.info("土壤采样Mock数据初始化完成，共 {} 条", samples.length);
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
            log.info("GPS航迹点数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化GPS航迹点Mock数据...");
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
        log.info("GPS航迹点Mock数据初始化完成，共 {} 条", points.length);
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
            log.info("地块数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化地块Mock数据...");
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
        log.info("地块Mock数据初始化完成，共 {} 条", plots.length);
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
            log.info("土质分类数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化土质分类Mock数据...");
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
        log.info("土质分类Mock数据初始化完成，共 {} 条", classifications.length);
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
            log.info("岩层结构分析数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化岩层结构分析Mock数据...");
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
        log.info("岩层结构分析Mock数据初始化完成，共 {} 条", analyses.length);
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
            log.info("灾害风险数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化灾害风险Mock数据...");
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
        log.info("灾害风险Mock数据初始化完成，共 {} 条", risks.length);
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
            log.info("设备数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化设备Mock数据...");
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
        log.info("设备Mock数据初始化完成，共 {} 条", devices.length);
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
            log.info("质量校验数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化质量校验Mock数据...");
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
        log.info("质量校验Mock数据初始化完成，共 {} 条", checks.length);
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
}
