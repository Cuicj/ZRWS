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

    private final Random random = new Random();

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyData() {
        log.info("[每日数据] 开始生成每日业务数据...");
        try {
            generateFlightMissions();
            generateSoilSamples();
            generateQualityChecks();
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
            mission.setAltitude(100 + random.nextInt(100));
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
}
