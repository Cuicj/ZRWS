package com.zrws.approval.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final Random random = new Random();

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyData() {
        log.info("[每日数据] 开始生成每日业务数据...");
        try {
            generateFlightMissions();
            generateSoilSamples();
            generateQualityChecks();
            generateClimateWarmingData();
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
}
