package com.zrws.approval.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
public class DailyDataScheduler {

    @Autowired
    private FlightMissionMapper flightMissionMapper;
    @Autowired
    private SoilSampleMapper soilSampleMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private QualityCheckMapper qualityCheckMapper;
    @Autowired
    private ClimateWarmingMapper climateWarmingMapper;
    @Autowired
    private DesertificationMapper desertificationMapper;
    @Autowired
    private DisasterRiskMapper disasterRiskMapper;
    @Autowired
    private GpsTrackPointMapper gpsTrackPointMapper;
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    @Autowired
    private LandPlotMapper landPlotMapper;
    @Autowired
    private SoilClassificationMapper soilClassificationMapper;
    @Autowired
    private RockStratumAnalysisMapper rockStratumAnalysisMapper;

    private final Random random = new Random();

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyData() {
        log.info("[每日数据] 开始生成每日业务数据...");
        try {
            generateFlightMissions();
            generateSoilSamples();
            generateQualityChecks();
            generateClimateWarmingData();
            generateDisasterRiskData();
            generateGpsTrackData();
            updateDeviceStatus();
            log.info("[每日数据] 每日业务数据生成完成");
        } catch (Exception e) {
            log.error("[每日数据] 每日数据生成失败: {}", e.getMessage(), e);
        }
    }

    private void generateFlightMissions() {
        long count = flightMissionMapper.selectCount(
            new LambdaQueryWrapper<FlightMission>()
                .ge(FlightMission::getFlightTime, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有飞行任务，跳过生成");
            return;
        }

        String[] areas = {"望城区乔口镇", "岳麓区莲花镇", "长沙县黄花镇", "宁乡市花明楼镇", "浏阳市大瑶镇"};
        String[] pilots = {"王工", "李工", "张工", "刘工", "陈工"};
        String[] droneModels = {"DJI Matrice 350 RTK", "DJI Matrice 300 RTK", "DJI Mavic 3E"};
        int missionCount = 2 + random.nextInt(3);

        for (int i = 0; i < missionCount; i++) {
            FlightMission mission = new FlightMission();
            mission.setMissionCode("FM-" + LocalDateTime.now().getDayOfYear() + "-" + String.format("%03d", i + 1));
            mission.setAreaName(areas[random.nextInt(areas.length)]);
            mission.setDroneId("DRONE-00" + (1 + random.nextInt(3)));
            mission.setDroneModel(droneModels[random.nextInt(droneModels.length)]);
            mission.setOperator(pilots[random.nextInt(pilots.length)]);
            mission.setDuration(30 + random.nextInt(60));
            mission.setCoverage(5.0 + random.nextDouble() * 15.0);
            mission.setAltitude(100.0 + random.nextInt(100));
            mission.setForwardOverlap(70 + random.nextDouble() * 10);
            mission.setSideOverlap(60 + random.nextDouble() * 10);
            mission.setPhotoCount(100 + random.nextInt(400));
            mission.setLidarPoints(1000000L + random.nextInt(5000000));
            mission.setSoilSamples(random.nextInt(10));
            mission.setStatus(i == 0 ? "COMPLETED" : (i == 1 ? "IN_PROGRESS" : "PENDING"));
            mission.setFlightTime(LocalDateTime.now().minusHours(4 + i * 2));
            mission.setCenterLat(28.3 + random.nextDouble() * 0.3);
            mission.setCenterLng(112.7 + random.nextDouble() * 0.3);
            mission.setWeather("晴");
            mission.setIsDeleted(0);
            flightMissionMapper.insert(mission);
        }
        log.info("[每日数据] 生成飞行任务 {} 条", missionCount);
    }

    private void generateSoilSamples() {
        long count = soilSampleMapper.selectCount(
            new LambdaQueryWrapper<SoilSample>()
                .ge(SoilSample::getCollectTime, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有土壤采样数据，跳过生成");
            return;
        }

        String[] soilTypes = {"LOAM", "CLAY", "SAND"};
        String[] soilTextures = {"壤质", "黏质", "砂质"};
        String[] collectors = {"王工", "李工", "张工"};
        int sampleCount = 3 + random.nextInt(4);

        for (int i = 0; i < sampleCount; i++) {
            int typeIdx = random.nextInt(soilTypes.length);
            SoilSample sample = new SoilSample();
            sample.setSampleCode("SS-" + LocalDateTime.now().getDayOfYear() + "-" + String.format("%03d", i + 1));
            sample.setMissionCode("ZRS-2026-" + LocalDateTime.now().getDayOfYear() + "-001");
            sample.setLatitude(28.3 + random.nextDouble() * 0.3);
            sample.setLongitude(112.7 + random.nextDouble() * 0.3);
            sample.setElevation(30 + random.nextDouble() * 50);
            sample.setPhValue(5.5 + random.nextDouble() * 2);
            sample.setMoisture(15 + random.nextDouble() * 25);
            sample.setEcValue(100 + random.nextInt(300));
            sample.setSoilType(soilTypes[typeIdx]);
            sample.setSoilTexture(soilTextures[typeIdx]);
            sample.setOrganicMatter(1.0 + random.nextDouble() * 3.0);
            sample.setNitrogen(0.5 + random.nextDouble() * 2.0);
            sample.setPhosphorus(0.2 + random.nextDouble() * 1.0);
            sample.setPotassium(0.5 + random.nextDouble() * 1.5);
            sample.setDepth("0-" + (20 + random.nextInt(20)) + "cm");
            sample.setCollector(collectors[random.nextInt(collectors.length)]);
            sample.setCollectTime(LocalDateTime.now().minusHours(6 + i));
            sample.setStatus("ANALYZED");
            sample.setIsDeleted(0);
            soilSampleMapper.insert(sample);
        }
        log.info("[每日数据] 生成土壤采样数据 {} 条", sampleCount);
    }

    private void generateQualityChecks() {
        long count = qualityCheckMapper.selectCount(
            new LambdaQueryWrapper<QualityCheck>()
                .ge(QualityCheck::getCheckTime, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有质量校验数据，跳过生成");
            return;
        }

        String[] checkTypes = {"PHOTO_QUALITY", "DATA_INTEGRITY", "COORDINATE_ACCURACY"};
        String[] checkItems = {"照片质量检测", "数据完整性检测", "坐标精度校验"};
        String[] checkers = {"王工", "李工", "张工"};
        int checkCount = 1 + random.nextInt(2);

        for (int i = 0; i < checkCount; i++) {
            QualityCheck check = new QualityCheck();
            check.setCheckCode("QC-" + LocalDateTime.now().getDayOfYear() + "-00" + (i + 1));
            check.setCheckType(checkTypes[i % checkTypes.length]);
            check.setCheckItem(checkItems[i % checkItems.length]);
            check.setTotalCount(500 + random.nextInt(1000));
            check.setPassCount(480 + random.nextInt(190));
            check.setFailCount(10 + random.nextInt(30));
            check.setPassRate(94.0 + random.nextDouble() * 5.0);
            check.setStatus(random.nextInt(10) > 2 ? "PASSED" : "FAILED");
            check.setChecker(checkers[random.nextInt(checkers.length)]);
            check.setCheckTime(LocalDateTime.now().minusHours(2 + i));
            check.setIsDeleted(0);
            qualityCheckMapper.insert(check);
        }
        log.info("[每日数据] 生成质量校验数据 {} 条", checkCount);
    }

    private void updateDeviceStatus() {
        try {
            java.util.List<Device> devices = deviceMapper.selectList(null);
            for (Device device : devices) {
                if ("DRONE".equals(device.getDeviceType())) {
                    int newBattery = 60 + random.nextInt(40);
                    int newSignal = 80 + random.nextInt(20);
                    device.setBatteryLevel(newBattery);
                    device.setSignalLevel(newSignal);
                    device.setLastOnline(LocalDateTime.now());
                    deviceMapper.updateById(device);
                }
            }
            log.info("[每日数据] 更新设备状态完成，共 {} 台设备", devices.size());
        } catch (Exception e) {
            log.warn("[每日数据] 更新设备状态失败: {}", e.getMessage());
        }
    }

    private void generateClimateWarmingData() {
        long count = climateWarmingMapper.selectCount(
            new LambdaQueryWrapper<ClimateWarming>()
                .ge(ClimateWarming::getMonitorDate, LocalDateTime.now().toLocalDate())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有气候变暖数据，跳过生成");
            return;
        }

        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};

        for (int i = 0; i < regions.length; i++) {
            double baseTemp = 20 + random.nextDouble() * 8;
            double tempAnomaly = 0.3 + random.nextDouble() * 0.8;
            double precip = 80 + random.nextDouble() * 150;
            double precipAnomaly = -20 + random.nextDouble() * 50;
            int highTempDays = 3 + random.nextInt(15);
            int lowTempDays = random.nextInt(5);
            int droughtDays = 5 + random.nextInt(25);
            int heatWaves = random.nextInt(3);
            double warmingRate = 0.15 + random.nextDouble() * 0.35;
            double riskScore = 20 + random.nextDouble() * 60;
            String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
            String trend = warmingRate > 0.4 ? "RAPID" : (warmingRate > 0.2 ? "MODERATE" : (warmingRate > 0.1 ? "SLOW" : "STABLE"));

            ClimateWarming cw = new ClimateWarming();
            cw.setRecordCode("CW-" + regionCodes[i] + "-" + LocalDateTime.now().getYear() + String.format("%03d", LocalDateTime.now().getDayOfYear()));
            cw.setRegion(regions[i]);
            cw.setRegionCode(regionCodes[i]);
            cw.setLatitude(java.math.BigDecimal.valueOf(lats[i]));
            cw.setLongitude(java.math.BigDecimal.valueOf(lngs[i]));
            cw.setMonitorDate(LocalDateTime.now().toLocalDate());
            cw.setAvgTemperature(java.math.BigDecimal.valueOf(baseTemp).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setMaxTemperature(java.math.BigDecimal.valueOf(baseTemp + 8 + random.nextDouble() * 5).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setMinTemperature(java.math.BigDecimal.valueOf(baseTemp - 6 - random.nextDouble() * 4).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setTemperatureAnomaly(java.math.BigDecimal.valueOf(tempAnomaly).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setPrecipitation(java.math.BigDecimal.valueOf(precip).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setPrecipitationAnomaly(java.math.BigDecimal.valueOf(precipAnomaly).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setExtremeHighTempDays(highTempDays);
            cw.setExtremeLowTempDays(lowTempDays);
            cw.setDroughtDays(droughtDays);
            cw.setHeatWaveEvents(heatWaves);
            cw.setWarmingRate10y(java.math.BigDecimal.valueOf(warmingRate).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setWarmingTrend(trend);
            cw.setRiskLevel(riskLevel);
            cw.setRiskScore(java.math.BigDecimal.valueOf(riskScore).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            cw.setImpactAssessment("气温偏高" + String.format("%.1f", tempAnomaly) + "°C，对农业生产、生态系统有一定影响");
            cw.setAdaptationMeasures("加强节水灌溉、调整种植结构、完善高温预警机制");
            cw.setDataSource("气象站观测+卫星遥感");
            cw.setStatus("COMPLETED");
            cw.setAnalyst("系统自动分析");
            cw.setAnalysisTime(LocalDateTime.now());
            cw.setIsDeleted(0);
            climateWarmingMapper.insert(cw);
        }
        log.info("[每日数据] 生成气候变暖监测数据 {} 条", regions.length);
    }

    @Scheduled(cron = "0 0 3 1 * ?")
    public void generateMonthlyData() {
        log.info("[每月数据] 开始生成月度业务数据...");
        try {
            generateDesertificationData();
            log.info("[每月数据] 月度业务数据生成完成");
        } catch (Exception e) {
            log.error("[每月数据] 月度数据生成失败: {}", e.getMessage(), e);
        }
    }

    private void generateDesertificationData() {
        String currentMonth = LocalDateTime.now().getYear() + "-" + String.format("%02d", LocalDateTime.now().getMonthValue());
        long count = desertificationMapper.selectCount(
            new LambdaQueryWrapper<Desertification>()
                .apply("DATE_FORMAT(monitor_date, '%Y-%m') = {0}", currentMonth)
        );
        if (count > 0) {
            log.info("[每月数据] 本月已有沙漠化数据，跳过生成");
            return;
        }

        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};
        String[] types = {"WATER", "WIND", "WATER", "WATER", "WIND", "WIND", "WIND", "WATER", "WATER", "WIND", "WATER", "WATER", "WATER", "WIND"};

        for (int i = 0; i < regions.length; i++) {
            double vegCov = 35 + random.nextDouble() * 55;
            double bareLand = 5 + random.nextDouble() * 30;
            double sandHeight = 0.5 + random.nextDouble() * 3;
            double migrationRate = 0.5 + random.nextDouble() * 5;
            double soilOM = 0.5 + random.nextDouble() * 2.5;
            double soilMoisture = 10 + random.nextDouble() * 25;
            double aridity = 0.3 + random.nextDouble() * 0.9;
            double windErosion = 100 + random.nextDouble() * 3000;
            double desertArea = 10 + random.nextDouble() * 200;
            double desertRatio = 2 + random.nextDouble() * 25;
            double ldi = 0.2 + random.nextDouble() * 0.6;
            double riskScore = 15 + random.nextDouble() * 55;
            String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
            String grade = vegCov > 50 ? "MILD" : (vegCov > 30 ? "MODERATE" : (vegCov > 10 ? "SEVERE" : "EXTREME"));
            String climateType = aridity > 1.0 ? "HUMID" : (aridity > 0.65 ? "SEMI_HUMID" : (aridity > 0.3 ? "SEMI_ARID" : (aridity > 0.13 ? "ARID" : "HYPER_ARID")));
            String vegTrend = random.nextDouble() > 0.6 ? "INCREASING" : (random.nextDouble() > 0.3 ? "STABLE" : "DECREASING");

            Desertification ds = new Desertification();
            ds.setRecordCode("DS-" + regionCodes[i] + "-" + LocalDateTime.now().getYear() + String.format("%02d", LocalDateTime.now().getMonthValue()));
            ds.setRegion(regions[i]);
            ds.setRegionCode(regionCodes[i]);
            ds.setLatitude(java.math.BigDecimal.valueOf(lats[i]));
            ds.setLongitude(java.math.BigDecimal.valueOf(lngs[i]));
            ds.setMonitorDate(LocalDateTime.now().toLocalDate().withDayOfMonth(1));
            ds.setMonitorPeriod("MONTHLY");
            ds.setVegetationCoverage(java.math.BigDecimal.valueOf(vegCov).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setVegetationTrend(vegTrend);
            ds.setBareLandRatio(java.math.BigDecimal.valueOf(bareLand).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setSandDuneHeightAvg(java.math.BigDecimal.valueOf(sandHeight).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setSandDuneMigrationRate(java.math.BigDecimal.valueOf(migrationRate).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setSoilOrganicMatter(java.math.BigDecimal.valueOf(soilOM).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setSoilMoisture(java.math.BigDecimal.valueOf(soilMoisture).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setAridityIndex(java.math.BigDecimal.valueOf(aridity).setScale(3, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setClimateType(climateType);
            ds.setWindErosionModulus(java.math.BigDecimal.valueOf(windErosion).setScale(0, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setDesertificationType(types[i]);
            ds.setDesertificationGrade(grade);
            ds.setDesertificationArea(java.math.BigDecimal.valueOf(desertArea).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setDesertificationRatio(java.math.BigDecimal.valueOf(desertRatio).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setLandDegradationIndex(java.math.BigDecimal.valueOf(ldi).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setRiskLevel(riskLevel);
            ds.setRiskScore(java.math.BigDecimal.valueOf(riskScore).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
            ds.setImpactAssessment("土地退化指数" + String.format("%.2f", ldi) + "，生态系统服务功能有所下降");
            ds.setControlMeasures("实施退耕还林还草、设置沙障、推广节水农业技术");
            ds.setDataSource("卫星遥感+地面调查");
            ds.setStatus("COMPLETED");
            ds.setAnalyst("系统自动分析");
            ds.setAnalysisTime(LocalDateTime.now());
            ds.setIsDeleted(0);
            desertificationMapper.insert(ds);
        }
        log.info("[每月数据] 生成沙漠化监测数据 {} 条", regions.length);
    }

    // ==================== 启动时回填历史数据 ====================

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        backfillEcoHistoricalData();
    }

    private void backfillEcoHistoricalData() {
        log.info("[数据回填] 开始检查并回填历史数据...");
        try {
            backfillClimateWarmingHistory();
            backfillDesertificationHistory();
            backfillDisasterRiskHistory();
            backfillGpsTrackHistory();
            initReportTemplateData();
            backfillLandPlotData();
            backfillSoilClassificationData();
            backfillRockStratumAnalysisData();
            log.info("[数据回填] 历史数据回填完成");
        } catch (Exception e) {
            log.error("[数据回填] 历史数据回填失败: {}", e.getMessage(), e);
        }
    }

    private void backfillClimateWarmingHistory() {
        long totalRecords = climateWarmingMapper.selectCount(
            new LambdaQueryWrapper<ClimateWarming>().eq(ClimateWarming::getIsDeleted, 0)
        );
        if (totalRecords >= 168) {
            log.info("[数据回填] 气候变暖历史数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成气候变暖历史数据（过去12个月）...");
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};

        LocalDate today = LocalDate.now();
        int totalGenerated = 0;

        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            LocalDate monitorDate = today.minusMonths(monthOffset).withDayOfMonth(15);
            String monthStr = monitorDate.getYear() + "-" + String.format("%02d", monitorDate.getMonthValue());

            long monthCount = climateWarmingMapper.selectCount(
                new LambdaQueryWrapper<ClimateWarming>()
                    .apply("DATE_FORMAT(monitor_date, '%Y-%m') = {0}", monthStr)
            );
            if (monthCount > 0) {
                continue;
            }

            for (int i = 0; i < regions.length; i++) {
                double baseTemp = 15 + random.nextDouble() * 15 + (monthOffset >= 6 ? -3 : 3);
                double tempAnomaly = 0.2 + random.nextDouble() * 1.0;
                double precip = 50 + random.nextDouble() * 200;
                int highTempDays = 2 + random.nextInt(18);
                int droughtDays = 3 + random.nextInt(25);
                int heatWaves = random.nextInt(4);
                double warmingRate = 0.15 + random.nextDouble() * 0.35;
                double riskScore = 15 + random.nextDouble() * 65;
                String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
                String trend = warmingRate > 0.4 ? "RAPID" : (warmingRate > 0.2 ? "MODERATE" : (warmingRate > 0.1 ? "SLOW" : "STABLE"));

                ClimateWarming cw = new ClimateWarming();
                cw.setRecordCode("CW-" + regionCodes[i] + "-" + monitorDate.getYear() + String.format("%03d", monitorDate.getDayOfYear()));
                cw.setRegion(regions[i]);
                cw.setRegionCode(regionCodes[i]);
                cw.setLatitude(java.math.BigDecimal.valueOf(lats[i]));
                cw.setLongitude(java.math.BigDecimal.valueOf(lngs[i]));
                cw.setMonitorDate(monitorDate);
                cw.setAvgTemperature(java.math.BigDecimal.valueOf(baseTemp).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setMaxTemperature(java.math.BigDecimal.valueOf(baseTemp + 8 + random.nextDouble() * 5).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setMinTemperature(java.math.BigDecimal.valueOf(baseTemp - 6 - random.nextDouble() * 4).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setTemperatureAnomaly(java.math.BigDecimal.valueOf(tempAnomaly).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setPrecipitation(java.math.BigDecimal.valueOf(precip).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setPrecipitationAnomaly(java.math.BigDecimal.valueOf(-20 + random.nextDouble() * 60).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setExtremeHighTempDays(highTempDays);
                cw.setExtremeLowTempDays(random.nextInt(5));
                cw.setDroughtDays(droughtDays);
                cw.setHeatWaveEvents(heatWaves);
                cw.setWarmingRate10y(java.math.BigDecimal.valueOf(warmingRate).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setWarmingTrend(trend);
                cw.setRiskLevel(riskLevel);
                cw.setRiskScore(java.math.BigDecimal.valueOf(riskScore).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                cw.setImpactAssessment("气温偏高" + String.format("%.1f", tempAnomaly) + "°C，对农业生产、生态系统有一定影响");
                cw.setAdaptationMeasures("加强节水灌溉、调整种植结构、完善高温预警机制");
                cw.setDataSource("气象站观测+卫星遥感");
                cw.setStatus("COMPLETED");
                cw.setAnalyst("系统自动分析");
                cw.setAnalysisTime(LocalDateTime.now());
                cw.setIsDeleted(0);
                climateWarmingMapper.insert(cw);
                totalGenerated++;
            }
        }
        log.info("[数据回填] 气候变暖历史数据生成完成，共 {} 条", totalGenerated);
    }

    private void backfillDesertificationHistory() {
        long totalRecords = desertificationMapper.selectCount(
            new LambdaQueryWrapper<Desertification>().eq(Desertification::getIsDeleted, 0)
        );
        if (totalRecords >= 168) {
            log.info("[数据回填] 沙漠化历史数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成沙漠化历史数据（过去12个月）...");
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
        String[] regionCodes = {"NN", "LZ", "GL", "WZ", "BH", "FCG", "QZ", "GG", "YL", "BS", "HZ", "HC", "LB", "CZ"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11, 22.63, 23.90, 24.41, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60, 110.15, 106.62, 111.55, 108.06, 109.23, 107.37};
        String[] types = {"WATER", "WIND", "WATER", "WATER", "WIND", "WIND", "WIND", "WATER", "WATER", "WIND", "WATER", "WATER", "WATER", "WIND"};

        LocalDate today = LocalDate.now();
        int totalGenerated = 0;

        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            LocalDate monitorDate = today.minusMonths(monthOffset).withDayOfMonth(1);
            String monthStr = monitorDate.getYear() + "-" + String.format("%02d", monitorDate.getMonthValue());

            long monthCount = desertificationMapper.selectCount(
                new LambdaQueryWrapper<Desertification>()
                    .apply("DATE_FORMAT(monitor_date, '%Y-%m') = {0}", monthStr)
            );
            if (monthCount > 0) {
                continue;
            }

            for (int i = 0; i < regions.length; i++) {
                double vegCov = 35 + random.nextDouble() * 55;
                double aridity = 0.3 + random.nextDouble() * 0.9;
                double windErosion = 100 + random.nextDouble() * 3000;
                double ldi = 0.2 + random.nextDouble() * 0.6;
                double riskScore = 15 + random.nextDouble() * 55;
                String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
                String grade = vegCov > 50 ? "MILD" : (vegCov > 30 ? "MODERATE" : (vegCov > 10 ? "SEVERE" : "EXTREME"));
                String climateType = aridity > 1.0 ? "HUMID" : (aridity > 0.65 ? "SEMI_HUMID" : (aridity > 0.3 ? "SEMI_ARID" : (aridity > 0.13 ? "ARID" : "HYPER_ARID")));
                String vegTrend = random.nextDouble() > 0.6 ? "INCREASING" : (random.nextDouble() > 0.3 ? "STABLE" : "DECREASING");

                Desertification ds = new Desertification();
                ds.setRecordCode("DS-" + regionCodes[i] + "-" + monitorDate.getYear() + String.format("%02d", monitorDate.getMonthValue()));
                ds.setRegion(regions[i]);
                ds.setRegionCode(regionCodes[i]);
                ds.setLatitude(java.math.BigDecimal.valueOf(lats[i]));
                ds.setLongitude(java.math.BigDecimal.valueOf(lngs[i]));
                ds.setMonitorDate(monitorDate);
                ds.setMonitorPeriod("MONTHLY");
                ds.setVegetationCoverage(java.math.BigDecimal.valueOf(vegCov).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setVegetationTrend(vegTrend);
                ds.setBareLandRatio(java.math.BigDecimal.valueOf(5 + random.nextDouble() * 30).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setSandDuneHeightAvg(java.math.BigDecimal.valueOf(0.5 + random.nextDouble() * 3).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setSandDuneMigrationRate(java.math.BigDecimal.valueOf(0.5 + random.nextDouble() * 5).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setSoilOrganicMatter(java.math.BigDecimal.valueOf(0.5 + random.nextDouble() * 2.5).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setSoilMoisture(java.math.BigDecimal.valueOf(10 + random.nextDouble() * 25).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setAridityIndex(java.math.BigDecimal.valueOf(aridity).setScale(3, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setClimateType(climateType);
                ds.setWindErosionModulus(java.math.BigDecimal.valueOf(windErosion).setScale(0, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setDesertificationType(types[i]);
                ds.setDesertificationGrade(grade);
                ds.setDesertificationArea(java.math.BigDecimal.valueOf(10 + random.nextDouble() * 200).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setDesertificationRatio(java.math.BigDecimal.valueOf(2 + random.nextDouble() * 25).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setLandDegradationIndex(java.math.BigDecimal.valueOf(ldi).setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setRiskLevel(riskLevel);
                ds.setRiskScore(java.math.BigDecimal.valueOf(riskScore).setScale(1, java.math.BigDecimal.ROUND_HALF_UP));
                ds.setImpactAssessment("土地退化指数" + String.format("%.2f", ldi) + "，生态系统服务功能有所下降");
                ds.setControlMeasures("实施退耕还林还草、设置沙障、推广节水农业技术");
                ds.setDataSource("卫星遥感+地面调查");
                ds.setStatus("COMPLETED");
                ds.setAnalyst("系统自动分析");
                ds.setAnalysisTime(LocalDateTime.now());
                ds.setIsDeleted(0);
                desertificationMapper.insert(ds);
                totalGenerated++;
            }
        }
        log.info("[数据回填] 沙漠化历史数据生成完成，共 {} 条", totalGenerated);
    }

    // ==================== 灾害风险（含水土流失）数据生成 ====================

    private void generateDisasterRiskData() {
        long count = disasterRiskMapper.selectCount(
            new LambdaQueryWrapper<DisasterRisk>()
                .ge(DisasterRisk::getAssessmentTime, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有灾害风险数据，跳过生成");
            return;
        }

        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60};
        String[] disasterTypes = {"SOIL_EROSION", "SOIL_EROSION", "LANDSLIDE", "DEBRIS_FLOW", "SOIL_EROSION", "FLOOD", "SOIL_EROSION", "GROUND_SUBSIDENCE"};
        String[] erosionTypes = {"WATER", "WATER", null, null, "WIND", null, "WATER", null};
        String[] soilTypes = {"红壤", "石灰土", "黄壤", "紫色土", "砖红壤", "水稻土", "红壤", "冲积土"};
        int recordCount = regions.length;

        for (int i = 0; i < recordCount; i++) {
            double riskScore = 15 + random.nextDouble() * 70;
            String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
            String disasterType = disasterTypes[i];
            boolean isSoilErosion = "SOIL_EROSION".equals(disasterType);
            String erosionType = isSoilErosion ? erosionTypes[i] : null;
            double erosionModulus = isSoilErosion ? (200 + random.nextDouble() * 8000) : 0;
            String erosionGrade = erosionModulus < 200 ? "MILD" : (erosionModulus < 2500 ? "LIGHT" : (erosionModulus < 5000 ? "MODERATE" : (erosionModulus < 8000 ? "SEVERE" : "EXTREME")));
            double vegetationCoverage = isSoilErosion ? (20 + random.nextDouble() * 60) : 0;
            double slope = isSoilErosion ? (3 + random.nextDouble() * 30) : 0;
            double tolerableLoss = isSoilErosion ? 500 : 0;

            DisasterRisk dr = new DisasterRisk();
            dr.setRiskCode("DR-" + LocalDate.now().getDayOfYear() + "-" + String.format("%03d", i + 1));
            dr.setRegion(regions[i]);
            dr.setLatitude(lats[i]);
            dr.setLongitude(lngs[i]);
            dr.setDisasterType(disasterType);
            dr.setRiskLevel(riskLevel);
            dr.setRiskScore(riskScore);
            dr.setDescription(isSoilErosion ? "水土流失监测评估" : disasterType + "风险评估");
            dr.setInfluencingFactors(isSoilErosion ? 
                ("侵蚀类型:" + erosionType + ",植被覆盖度:" + String.format("%.1f", vegetationCoverage) + "%,坡度:" + String.format("%.1f", slope) + "°,土壤类型:" + soilTypes[i]) :
                "降雨、地质条件、人类活动");
            dr.setMonitoringData(isSoilErosion ?
                ("{\"erosionType\":\"" + erosionType + "\",\"erosionModulus\":" + String.format("%.0f", erosionModulus) + ",\"erosionGrade\":\"" + erosionGrade + "\",\"vegetationCoverage\":" + String.format("%.1f", vegetationCoverage) + ",\"slope\":" + String.format("%.1f", slope) + ",\"soilType\":\"" + soilTypes[i] + "\",\"tolerableLoss\":" + String.format("%.0f", tolerableLoss) + "}") :
                "{\"rainfall\":" + (50 + random.nextInt(200)) + ",\"displacement\":" + String.format("%.2f", random.nextDouble() * 5) + "}");
            dr.setHistoricalRecords("近5年发生" + random.nextInt(5) + "次类似灾害");
            dr.setAiAnalysis(isSoilErosion ? 
                "基于USLE模型分析，侵蚀模数" + String.format("%.0f", erosionModulus) + " t/km²·a，属于" + erosionGrade + "级别侵蚀" :
                "基于多源数据融合分析，存在" + riskLevel + "风险");
            dr.setAiSuggestion(isSoilErosion ? 
                "建议采取水土保持措施：梯田改造、植被恢复、沟道治理" : 
                "建议加强监测预警，必要时进行工程治理");
            dr.setAiConfidence(0.75 + random.nextDouble() * 0.2);
            dr.setStatus("COMPLETED");
            dr.setAnalyst("系统自动分析");
            dr.setAssessmentTime(LocalDateTime.now());
            dr.setIsDeleted(0);
            disasterRiskMapper.insert(dr);
        }
        log.info("[每日数据] 生成灾害风险数据 {} 条（含水土流失）", recordCount);
    }

    private void backfillDisasterRiskHistory() {
        long totalRecords = disasterRiskMapper.selectCount(
            new LambdaQueryWrapper<DisasterRisk>().eq(DisasterRisk::getIsDeleted, 0)
        );
        if (totalRecords >= 240) {
            log.info("[数据回填] 灾害风险历史数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成灾害风险历史数据（过去30天）...");
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 21.69, 21.97, 23.11};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 108.35, 108.63, 109.60};
        String[] disasterTypes = {"SOIL_EROSION", "SOIL_EROSION", "LANDSLIDE", "DEBRIS_FLOW", "SOIL_EROSION", "FLOOD", "SOIL_EROSION", "GROUND_SUBSIDENCE"};
        String[] erosionTypes = {"WATER", "WATER", null, null, "WIND", null, "WATER", null};
        String[] soilTypes = {"红壤", "石灰土", "黄壤", "紫色土", "砖红壤", "水稻土", "红壤", "冲积土"};

        LocalDate today = LocalDate.now();
        int totalGenerated = 0;

        for (int dayOffset = 29; dayOffset >= 0; dayOffset--) {
            LocalDate monitorDate = today.minusDays(dayOffset);

            for (int i = 0; i < regions.length; i++) {
                double riskScore = 15 + random.nextDouble() * 70;
                String riskLevel = riskScore < 25 ? "LOW" : (riskScore < 50 ? "MEDIUM" : (riskScore < 75 ? "HIGH" : "EXTREME"));
                String disasterType = disasterTypes[i];
                boolean isSoilErosion = "SOIL_EROSION".equals(disasterType);
                String erosionType = isSoilErosion ? erosionTypes[i] : null;
                double erosionModulus = isSoilErosion ? (200 + random.nextDouble() * 8000) : 0;
                String erosionGrade = erosionModulus < 200 ? "MILD" : (erosionModulus < 2500 ? "LIGHT" : (erosionModulus < 5000 ? "MODERATE" : (erosionModulus < 8000 ? "SEVERE" : "EXTREME")));
                double vegetationCoverage = isSoilErosion ? (20 + random.nextDouble() * 60) : 0;
                double slope = isSoilErosion ? (3 + random.nextDouble() * 30) : 0;
                double tolerableLoss = isSoilErosion ? 500 : 0;

                DisasterRisk dr = new DisasterRisk();
                dr.setRiskCode("DR-" + monitorDate.getDayOfYear() + "-" + String.format("%03d", i + 1));
                dr.setRegion(regions[i]);
                dr.setLatitude(lats[i]);
                dr.setLongitude(lngs[i]);
                dr.setDisasterType(disasterType);
                dr.setRiskLevel(riskLevel);
                dr.setRiskScore(riskScore);
                dr.setDescription(isSoilErosion ? "水土流失监测评估" : disasterType + "风险评估");
                dr.setInfluencingFactors(isSoilErosion ?
                    ("侵蚀类型:" + erosionType + ",植被覆盖度:" + String.format("%.1f", vegetationCoverage) + "%,坡度:" + String.format("%.1f", slope) + "°,土壤类型:" + soilTypes[i]) :
                    "降雨、地质条件、人类活动");
                dr.setMonitoringData(isSoilErosion ?
                    ("{\"erosionType\":\"" + erosionType + "\",\"erosionModulus\":" + String.format("%.0f", erosionModulus) + ",\"erosionGrade\":\"" + erosionGrade + "\",\"vegetationCoverage\":" + String.format("%.1f", vegetationCoverage) + ",\"slope\":" + String.format("%.1f", slope) + ",\"soilType\":\"" + soilTypes[i] + "\",\"tolerableLoss\":" + String.format("%.0f", tolerableLoss) + "}") :
                    "{\"rainfall\":" + (50 + random.nextInt(200)) + ",\"displacement\":" + String.format("%.2f", random.nextDouble() * 5) + "}");
                dr.setHistoricalRecords("近5年发生" + random.nextInt(5) + "次类似灾害");
                dr.setAiAnalysis(isSoilErosion ?
                    "基于USLE模型分析，侵蚀模数" + String.format("%.0f", erosionModulus) + " t/km²·a，属于" + erosionGrade + "级别侵蚀" :
                    "基于多源数据融合分析，存在" + riskLevel + "风险");
                dr.setAiSuggestion(isSoilErosion ?
                    "建议采取水土保持措施：梯田改造、植被恢复、沟道治理" :
                    "建议加强监测预警，必要时进行工程治理");
                dr.setAiConfidence(0.75 + random.nextDouble() * 0.2);
                dr.setStatus("COMPLETED");
                dr.setAnalyst("系统自动分析");
                dr.setAssessmentTime(monitorDate.atTime(10, 0));
                dr.setIsDeleted(0);
                disasterRiskMapper.insert(dr);
                totalGenerated++;
            }
        }
        log.info("[数据回填] 灾害风险历史数据生成完成，共 {} 条", totalGenerated);
    }

    // ==================== GPS航迹数据生成 ====================

    private void generateGpsTrackData() {
        long count = gpsTrackPointMapper.selectCount(
            new LambdaQueryWrapper<GpsTrackPoint>()
                .ge(GpsTrackPoint::getGpsTime, LocalDate.now().toString())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有GPS航迹点数据，跳过生成");
            return;
        }

        // 每日生成2条航迹，每条航迹30-50个点
        String[] areaNames = {"南宁武鸣区", "柳州柳江县", "桂林临桂区", "梧州苍梧县", "北海合浦县"};
        int trackCount = 2;
        int totalPoints = 0;

        for (int t = 0; t < trackCount; t++) {
            long missionId = 9000L + LocalDate.now().getDayOfYear() * 10L + t;
            String missionCode = "GT-" + LocalDate.now().getYear() + "-" + String.format("%03d", LocalDate.now().getDayOfYear()) + "-" + (t + 1);
            String areaName = areaNames[random.nextInt(areaNames.length)];

            double centerLat = 22.5 + random.nextDouble() * 3.0;
            double centerLng = 107.5 + random.nextDouble() * 4.0;
            int pointCount = 30 + random.nextInt(21);
            LocalDateTime startTime = LocalDateTime.now().minusHours(3 - t);

            for (int i = 0; i < pointCount; i++) {
                GpsTrackPoint point = new GpsTrackPoint();
                point.setMissionId(missionId);
                point.setMissionCode(missionCode);
                point.setSequence(i + 1);
                // 沿航迹行进，纬度经度渐变
                point.setLatitude(centerLat + (random.nextDouble() - 0.5) * 0.05 + i * 0.001);
                point.setLongitude(centerLng + (random.nextDouble() - 0.5) * 0.05 + i * 0.001);
                point.setAltitude(80 + random.nextDouble() * 60);
                point.setSpeed(8 + random.nextDouble() * 12);
                point.setHeading(random.nextDouble() * 360);
                point.setGpsTime(startTime.plusSeconds(i * 5L).toString());
                point.setSatellites(8 + random.nextInt(6));
                point.setFixType(random.nextBoolean() ? "RTK_FIX" : "RTK_FLOAT");
                point.setAccuracyH(0.02 + random.nextDouble() * 0.2);
                point.setAccuracyV(0.05 + random.nextDouble() * 0.3);
                point.setPointType(i == 0 ? "TAKEOFF" : (i == pointCount - 1 ? "LANDING" : (i % 10 == 0 ? "SAMPLE" : "SCAN")));
                point.setIsDeleted(0);
                gpsTrackPointMapper.insert(point);
                totalPoints++;
            }
        }
        log.info("[每日数据] 生成GPS航迹 {} 条，航迹点 {} 个", trackCount, totalPoints);
    }

    private void backfillGpsTrackHistory() {
        long totalRecords = gpsTrackPointMapper.selectCount(
            new LambdaQueryWrapper<GpsTrackPoint>().eq(GpsTrackPoint::getIsDeleted, 0)
        );
        if (totalRecords >= 200) {
            log.info("[数据回填] GPS航迹历史数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成GPS航迹历史数据（过去30天）...");
        String[] areaNames = {"南宁武鸣区", "柳州柳江县", "桂林临桂区", "梧州苍梧县", "北海合浦县", "防城港东兴市", "钦州灵山县", "贵港桂平市"};
        LocalDate today = LocalDate.now();
        int totalGenerated = 0;

        for (int dayOffset = 29; dayOffset >= 1; dayOffset--) {
            LocalDate monitorDate = today.minusDays(dayOffset);
            String dateStr = monitorDate.toString();

            long dayCount = gpsTrackPointMapper.selectCount(
                new LambdaQueryWrapper<GpsTrackPoint>().likeRight(GpsTrackPoint::getGpsTime, dateStr)
            );
            if (dayCount > 0) {
                continue;
            }

            int trackCount = 1 + random.nextInt(2);
            for (int t = 0; t < trackCount; t++) {
                long missionId = 8000L + dayOffset * 10L + t;
                String missionCode = "GT-" + monitorDate.getYear() + "-" + String.format("%03d", monitorDate.getDayOfYear()) + "-" + (t + 1);

                double centerLat = 22.5 + random.nextDouble() * 3.0;
                double centerLng = 107.5 + random.nextDouble() * 4.0;
                int pointCount = 25 + random.nextInt(26);
                LocalDateTime startTime = monitorDate.atTime(9 + t * 2, 0).plusMinutes(random.nextInt(30));

                for (int i = 0; i < pointCount; i++) {
                    GpsTrackPoint point = new GpsTrackPoint();
                    point.setMissionId(missionId);
                    point.setMissionCode(missionCode);
                    point.setSequence(i + 1);
                    point.setLatitude(centerLat + (random.nextDouble() - 0.5) * 0.05 + i * 0.001);
                    point.setLongitude(centerLng + (random.nextDouble() - 0.5) * 0.05 + i * 0.001);
                    point.setAltitude(80 + random.nextDouble() * 60);
                    point.setSpeed(8 + random.nextDouble() * 12);
                    point.setHeading(random.nextDouble() * 360);
                    point.setGpsTime(startTime.plusSeconds(i * 5L).toString());
                    point.setSatellites(8 + random.nextInt(6));
                    point.setFixType(random.nextBoolean() ? "RTK_FIX" : "RTK_FLOAT");
                    point.setAccuracyH(0.02 + random.nextDouble() * 0.2);
                    point.setAccuracyV(0.05 + random.nextDouble() * 0.3);
                    point.setPointType(i == 0 ? "TAKEOFF" : (i == pointCount - 1 ? "LANDING" : (i % 10 == 0 ? "SAMPLE" : "SCAN")));
                    point.setIsDeleted(0);
                    gpsTrackPointMapper.insert(point);
                    totalGenerated++;
                }
            }
        }
        log.info("[数据回填] GPS航迹历史数据生成完成，共 {} 个航迹点", totalGenerated);
    }

    // ==================== 报告模板初始化 ====================

    private void initReportTemplateData() {
        long totalRecords = reportTemplateMapper.selectCount(
            new LambdaQueryWrapper<ReportTemplate>().eq(ReportTemplate::getIsDeleted, 0)
        );
        if (totalRecords > 0) {
            log.info("[数据回填] 报告模板数据已存在（{}条），跳过初始化", totalRecords);
            return;
        }

        log.info("[数据回填] 开始初始化报告模板数据...");
        String[][] templates = {
            {"RT-DASHBOARD-01", "综合监控仪表盘报告", "STANDARD", "DASHBOARD", "汇总飞行任务、土壤采样、设备状态等核心监控指标的仪表盘报表", "PDF"},
            {"RT-SOIL-01", "土壤采样分析报告", "STANDARD", "SOIL", "土壤样本理化性质分析报告，包含pH值、有机质、氮磷钾含量等指标", "PDF"},
            {"RT-SOIL-02", "土壤肥力评估报告", "CUSTOM", "SOIL", "基于多期采样数据的土壤肥力综合评估报告", "EXCEL"},
            {"RT-DISASTER-01", "灾害风险评估报告", "STANDARD", "DISASTER", "水土流失、滑坡、泥石流等灾害风险综合评估报告", "PDF"},
            {"RT-ROCK-01", "岩层结构分析报告", "STANDARD", "ROCK", "钻孔取样、物探数据综合分析形成的岩层结构报告", "WORD"},
            {"RT-DEVICE-01", "设备运行统计报告", "STANDARD", "DEVICE", "无人机等设备运行状态、电池、信号统计报表", "EXCEL"},
            {"RT-QUALITY-01", "质量检测报告", "STANDARD", "QUALITY", "照片质量、数据完整性、坐标精度等质量校验统计报告", "PDF"},
            {"RT-CLIMATE-01", "气候变化监测报告", "CUSTOM", "DASHBOARD", "气候变暖、沙漠化等生态环境监测数据综合报告", "PDF"}
        };

        int generated = 0;
        for (String[] tpl : templates) {
            ReportTemplate rt = new ReportTemplate();
            rt.setTemplateCode(tpl[0]);
            rt.setTemplateName(tpl[1]);
            rt.setTemplateType(tpl[2]);
            rt.setCategory(tpl[3]);
            rt.setDescription(tpl[4]);
            rt.setTemplateContent("{\"sections\":[{\"title\":\"概述\",\"type\":\"summary\"},{\"title\":\"数据明细\",\"type\":\"table\"},{\"title\":\"图表分析\",\"type\":\"chart\"},{\"title\":\"结论建议\",\"type\":\"conclusion\"}]}");
            rt.setDataSource("{\"primaryTable\":\"zrws_" + tpl[3].toLowerCase() + "\",\"dateRange\":\"LAST_30_DAYS\"}");
            rt.setParameters("[{\"name\":\"startDate\",\"type\":\"date\",\"required\":true},{\"name\":\"endDate\",\"type\":\"date\",\"required\":true},{\"name\":\"region\",\"type\":\"string\",\"required\":false}]");
            rt.setOutputFormat(tpl[5]);
            rt.setStatus("ACTIVE");
            rt.setIsDeleted(0);
            reportTemplateMapper.insert(rt);
            generated++;
        }
        log.info("[数据回填] 报告模板初始化完成，共 {} 个模板", generated);
    }

    // ==================== 地块数据回填 ====================

    private void backfillLandPlotData() {
        long totalRecords = landPlotMapper.selectCount(
            new LambdaQueryWrapper<LandPlot>().eq(LandPlot::getIsDeleted, 0)
        );
        if (totalRecords >= 20) {
            log.info("[数据回填] 地块数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成地块数据...");
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "北海市", "玉林市", "百色市", "河池市"};
        String[] counties = {"武鸣区", "柳江县", "临桂区", "苍梧县", "合浦县", "玉州区", "右江区", "金城江区"};
        String[] townships = {"城厢镇", "拉堡镇", "临桂镇", "龙圩镇", "廉州镇", "茂林镇", "百城街道", "六甲镇"};
        String[] owners = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] landTypes = {"耕地", "园地", "林地", "建设用地"};
        String[] landUses = {"水稻种植", "果园", "经济林", "蔬菜大棚", "住宅用地"};
        String[] soilTypes = {"红壤", "水稻土", "石灰土", "黄壤", "紫色土"};
        String[] irrigationTypes = {"灌溉", "旱作", "滴灌", "漫灌"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 22.63, 23.90, 24.70};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 110.15, 106.62, 108.06};

        int generated = 0;
        for (int i = 0; i < 24; i++) {
            int idx = i % regions.length;
            double gpsArea = 5.0 + random.nextDouble() * 95.0;
            double registeredArea = gpsArea * (0.95 + random.nextDouble() * 0.1);

            LandPlot plot = new LandPlot();
            plot.setPlotCode("LP-" + String.format("%04d", i + 1));
            plot.setPlotName(regions[idx] + counties[idx] + townships[idx] + "地块" + (i / 8 + 1));
            plot.setOwner(owners[random.nextInt(owners.length)]);
            plot.setLandType(landTypes[i % landTypes.length]);
            plot.setLandUse(landUses[i % landUses.length]);
            plot.setGpsArea(Math.round(gpsArea * 100.0) / 100.0);
            plot.setRegisteredArea(Math.round(registeredArea * 100.0) / 100.0);
            plot.setAreaDiff(Math.round((gpsArea - registeredArea) * 100.0) / 100.0);
            plot.setStatus(random.nextInt(10) > 1 ? "NORMAL" : "REVIEW");
            plot.setRegion(regions[idx]);
            plot.setProvince("广西壮族自治区");
            plot.setCity(regions[idx]);
            plot.setCounty(counties[idx]);
            plot.setTownship(townships[idx]);
            plot.setVillage(townships[idx] + "村");
            plot.setCenterLat(lats[idx] + (random.nextDouble() - 0.5) * 0.1);
            plot.setCenterLng(lngs[idx] + (random.nextDouble() - 0.5) * 0.1);
            plot.setBoundaryGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[" + (lngs[idx] - 0.01) + "," + (lats[idx] - 0.01) + "],[" + (lngs[idx] + 0.01) + "," + (lats[idx] - 0.01) + "],[" + (lngs[idx] + 0.01) + "," + (lats[idx] + 0.01) + "],[" + (lngs[idx] - 0.01) + "," + (lats[idx] + 0.01) + "],[" + (lngs[idx] - 0.01) + "," + (lats[idx] - 0.01) + "]]]}");
            plot.setSoilType(soilTypes[i % soilTypes.length]);
            plot.setFertilityLevel((double) (2 + random.nextInt(3)));
            plot.setIrrigationType(irrigationTypes[i % irrigationTypes.length]);
            plot.setRemark("自动化测绘登记地块");
            plot.setIsDeleted(0);
            landPlotMapper.insert(plot);
            generated++;
        }
        log.info("[数据回填] 地块数据生成完成，共 {} 条", generated);
    }

    // ==================== 土质分类数据回填 ====================

    private void backfillSoilClassificationData() {
        long totalRecords = soilClassificationMapper.selectCount(
            new LambdaQueryWrapper<SoilClassification>().eq(SoilClassification::getIsDeleted, 0)
        );
        if (totalRecords >= 15) {
            log.info("[数据回填] 土质分类数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成土质分类数据...");
        String[] soilTypes = {"PADDY_SOIL", "RED_SOIL", "LIMESTONE_SOIL", "YELLOW_BROWN_EARTH", "PURPLE_SOIL", "ALLUVIAL_SOIL"};
        String[] soilSubtypes = {"潴育型水稻土", "典型红壤", "棕色石灰土", "黄棕壤", "酸性紫色土", "冲积土"};
        String[] textures = {"黏质", "壤质", "砂质", "粉质"};
        String[] structures = {"团粒结构", "块状结构", "柱状结构", "片状结构"};
        String[] colors = {"棕黄色", "红棕色", "灰棕色", "黄棕色", "紫红色"};
        String[] parentMaterials = {"花岗岩风化物", "石灰岩风化物", "砂页岩风化物", "第四纪红土", "河流冲积物"};
        String[] vegetations = {"水稻", "玉米", "甘蔗", "果园", "桉树", "蔬菜"};
        String[] regions = {"南宁市", "柳州市", "桂林市", "梧州市", "玉林市", "百色市"};

        int generated = 0;
        for (int i = 0; i < 18; i++) {
            int typeIdx = i % soilTypes.length;
            double ph = 4.5 + random.nextDouble() * 4.0;
            double om = 0.8 + random.nextDouble() * 4.0;
            double moisture = 12 + random.nextDouble() * 28;
            double nitrogen = 0.5 + random.nextDouble() * 2.5;
            double phosphorus = 0.2 + random.nextDouble() * 1.5;
            double potassium = 0.5 + random.nextDouble() * 2.0;

            SoilClassification sc = new SoilClassification();
            sc.setAnalysisCode("SC-" + String.format("%04d", i + 1));
            sc.setMissionId(7000L + i);
            sc.setMissionCode("ZRS-2026-" + String.format("%03d", 100 + i));
            sc.setAnalysisName(regions[i % regions.length] + "土壤分类分析" + (i + 1));
            sc.setSampleCount(3 + random.nextInt(8));
            sc.setSoilType(soilTypes[typeIdx]);
            sc.setSoilSubtype(soilSubtypes[typeIdx]);
            sc.setConfidence(0.78 + random.nextDouble() * 0.2);
            sc.setDescription("基于多源遥感与采样数据的土壤类型分类分析");
            sc.setPhValue(Math.round(ph * 100.0) / 100.0);
            sc.setOrganicMatter(Math.round(om * 100.0) / 100.0);
            sc.setMoisture(Math.round(moisture * 100.0) / 100.0);
            sc.setNitrogen(Math.round(nitrogen * 100.0) / 100.0);
            sc.setPhosphorus(Math.round(phosphorus * 100.0) / 100.0);
            sc.setPotassium(Math.round(potassium * 100.0) / 100.0);
            sc.setTexture(textures[i % textures.length]);
            sc.setStructure(structures[i % structures.length]);
            sc.setColor(colors[i % colors.length]);
            sc.setDepth(20.0 + random.nextDouble() * 60.0);
            sc.setParentMaterial(parentMaterials[i % parentMaterials.length]);
            sc.setVegetation(vegetations[i % vegetations.length]);
            sc.setAiAnalysis("基于CNN模型识别，土壤类型为" + soilSubtypes[typeIdx] + "，置信度" + String.format("%.1f%%", (0.78 + random.nextDouble() * 0.2) * 100));
            sc.setAiSuggestion("建议针对" + soilSubtypes[typeIdx] + "特性调整施肥方案，pH值" + String.format("%.1f", ph) + (ph < 5.5 ? "偏酸，建议施用石灰改良" : "适宜"));
            sc.setStatus("COMPLETED");
            sc.setAnalyst("系统自动分析");
            sc.setAnalysisTime(LocalDateTime.now().minusDays(i));
            sc.setRemark("AI辅助土壤分类");
            sc.setIsDeleted(0);
            soilClassificationMapper.insert(sc);
            generated++;
        }
        log.info("[数据回填] 土质分类数据生成完成，共 {} 条", generated);
    }

    // ==================== 岩层分析数据回填 ====================

    private void backfillRockStratumAnalysisData() {
        long totalRecords = rockStratumAnalysisMapper.selectCount(
            new LambdaQueryWrapper<RockStratumAnalysis>().eq(RockStratumAnalysis::getIsDeleted, 0)
        );
        if (totalRecords >= 12) {
            log.info("[数据回填] 岩层分析数据已存在（{}条），跳过回填", totalRecords);
            return;
        }

        log.info("[数据回填] 开始生成岩层分析数据...");
        String[] projectNames = {"南宁地铁5号线地质勘察", "柳州跨江大桥基础工程", "桂林旅游快速通道隧道", "梧州港区地基处理", "北海海上风电基础", "玉林水利枢纽工程", "百色矿山边坡稳定", "河池地下硐室工程", "来宾高层建筑地基", "崇左边境公路边坡"};
        String[] locations = {"南宁市西乡塘区", "柳州市城中区", "桂林市象山区", "梧州市万秀区", "北海市银海区", "玉林市玉州区", "百色市右江区", "河池市金城江区", "来宾市兴宾区", "崇左市江州区"};
        String[] analysisTypes = {"COMPREHENSIVE", "BOREHOLE", "GEOPHYSICAL", "GPR", "SAMPLING"};
        String[] aiAlgorithms = {"CNN", "TRANSFORMER", "RANDOM_FOREST", "ENSEMBLE", "DEEP_LEARNING"};
        String[] riskLevels = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"};
        double[] lats = {22.82, 24.33, 25.27, 23.48, 21.48, 22.63, 23.90, 24.70, 23.74, 22.37};
        double[] lngs = {108.37, 109.42, 110.29, 111.34, 109.12, 110.15, 106.62, 108.06, 109.23, 107.37};

        int generated = 0;
        for (int i = 0; i < 15; i++) {
            int boreholeCount = 5 + random.nextInt(20);
            double maxDepth = 15 + random.nextDouble() * 85;
            int stratumCount = 3 + random.nextInt(8);
            double aiConfidence = 0.78 + random.nextDouble() * 0.2;
            String riskLevel = riskLevels[random.nextInt(riskLevels.length)];

            RockStratumAnalysis ra = new RockStratumAnalysis();
            ra.setAnalysisCode("RSA-" + String.format("%04d", i + 1));
            ra.setMissionId(6000L + i);
            ra.setMissionCode("ZRS-2026-" + String.format("%03d", 200 + i));
            ra.setProjectName(projectNames[i % projectNames.length]);
            ra.setLocation(locations[i % locations.length]);
            ra.setLatitude(lats[i % lats.length]);
            ra.setLongitude(lngs[i % lngs.length]);
            ra.setElevation(30 + random.nextDouble() * 200);
            ra.setAnalysisType(analysisTypes[i % analysisTypes.length]);
            ra.setDataSource("钻孔取样+地质雷达+物探");
            ra.setBoreholeCount(boreholeCount);
            ra.setMaxDepth(Math.round(maxDepth * 100.0) / 100.0);
            ra.setStratumCount(stratumCount);
            ra.setStratumData("[{\"layer\":1,\"name\":\"素填土\",\"thickness\":\"2.5m\"},{\"layer\":2,\"name\":\"粉质黏土\",\"thickness\":\"5.8m\"},{\"layer\":3,\"name\":\"强风化泥岩\",\"thickness\":\"8.2m\"},{\"layer\":4,\"name\":\"中风化灰岩\",\"thickness\":\"15.6m\"}]");
            ra.setLithologyData("[{\"name\":\"黏土\",\"color\":\"黄褐色\",\"state\":\"可塑\"},{\"name\":\"泥岩\",\"color\":\"灰黑色\",\"state\":\"强风化\"},{\"name\":\"灰岩\",\"color\":\"灰白色\",\"state\":\"中风化\"}]");
            ra.setStructureData("{\"dip\":\"N30°E\",\"dipAngle\":\"35°\",\"jointCount\":4}");
            ra.setFaultData("{\"hasFault\":false,\"fractureZone\":\"无\"}");
            ra.setAiAlgorithm(aiAlgorithms[i % aiAlgorithms.length]);
            ra.setAiModelVersion("v2.1.0");
            ra.setAiConfidence(aiConfidence);
            ra.setAiSummary("基于" + boreholeCount + "个钻孔及物探数据，识别出" + stratumCount + "个岩层，整体稳定性" + (riskLevel.equals("LOW") ? "良好" : riskLevel.equals("MEDIUM") ? "一般" : "较差"));
            ra.setAiDetail("综合分析显示，场地主要地层为填土、黏土、泥岩、灰岩。基岩埋深" + String.format("%.1f", maxDepth * 0.6) + "m，承载力特征值约" + (200 + random.nextInt(400)) + "kPa。建议采用" + (maxDepth > 50 ? "桩基础" : "扩展基础") + "。");
            ra.setRiskLevel(riskLevel);
            ra.setSuggestion(riskLevel.equals("LOW") ? "场地稳定，可按常规设计施工" : riskLevel.equals("MEDIUM") ? "需加强支护，注意地下水影响" : "存在较高风险，建议进行专项支护设计并加强监测");
            ra.setAnalyst("系统自动分析");
            ra.setAnalysisTime(LocalDateTime.now().minusDays(i));
            ra.setReportTime(LocalDateTime.now().minusDays(i).plusHours(2));
            ra.setStatus("COMPLETED");
            ra.setRemark("AI辅助岩层结构分析");
            ra.setIsDeleted(0);
            rockStratumAnalysisMapper.insert(ra);
            generated++;
        }
        log.info("[数据回填] 岩层分析数据生成完成，共 {} 条", generated);
    }
}
