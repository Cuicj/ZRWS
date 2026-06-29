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
    private AnnouncementMapper announcementMapper;

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
        int missionCount = 2 + random.nextInt(3);

        for (int i = 0; i < missionCount; i++) {
            FlightMission mission = new FlightMission();
            mission.setMissionName("日常巡检-" + (i + 1));
            mission.setMissionType("ROUTINE");
            mission.setDeviceCode("M350-00" + (1 + random.nextInt(3)));
            mission.setPilot(pilots[random.nextInt(pilots.length)]);
            mission.setFlightArea(areas[random.nextInt(areas.length)]);
            mission.setPlannedDuration(30 + random.nextInt(60));
            mission.setActualDuration(25 + random.nextInt(50));
            mission.setPlannedDistance(5 + random.nextInt(15));
            mission.setActualDistance(4 + random.nextInt(14));
            mission.setPhotoCount(100 + random.nextInt(400));
            mission.setVideoCount(random.nextInt(5));
            mission.setStatus(i == 0 ? "COMPLETED" : (i == 1 ? "IN_PROGRESS" : "PENDING"));
            mission.setFlightTime(LocalDateTime.now().minusHours(4 + i * 2));
            mission.setCenterLat(28.3 + random.nextDouble() * 0.3);
            mission.setCenterLng(112.7 + random.nextDouble() * 0.3);
            mission.setIsDeleted(0);
            flightMissionMapper.insert(mission);
        }
        log.info("[每日数据] 生成飞行任务 {} 条", missionCount);
    }

    private void generateSoilSamples() {
        long count = soilSampleMapper.selectCount(
            new LambdaQueryWrapper<SoilSample>()
                .ge(SoilSample::getSamplingTime, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (count > 0) {
            log.info("[每日数据] 今日已有土壤采样数据，跳过生成");
            return;
        }

        String[] soilTypes = {"LOAM", "CLAY", "SAND"};
        String[] soilTypeNames = {"壤土", "黏土", "砂土"};
        String[] analysts = {"王工", "李工", "张工"};
        int sampleCount = 3 + random.nextInt(4);

        for (int i = 0; i < sampleCount; i++) {
            int typeIdx = random.nextInt(soilTypes.length);
            SoilSample sample = new SoilSample();
            sample.setSampleCode("SS-" + LocalDateTime.now().getDayOfYear() + "-" + String.format("%03d", i + 1));
            sample.setMissionCode("ZRS-2026-" + LocalDateTime.now().getDayOfYear() + "-001");
            sample.setLatitude(28.3 + random.nextDouble() * 0.3);
            sample.setLongitude(112.7 + random.nextDouble() * 0.3);
            sample.setDepth(20 + random.nextInt(30));
            sample.setPhValue(5.5 + random.nextDouble() * 2);
            sample.setOrganicMatter(0.1 + random.nextDouble() * 0.4);
            sample.setMoisture(150 + random.nextInt(200));
            sample.setSoilType(soilTypes[typeIdx]);
            sample.setSoilTypeName(soilTypeNames[typeIdx]);
            sample.setNitrogen(1.0 + random.nextDouble() * 2.5);
            sample.setPhosphorus(0.5 + random.nextDouble() * 1.5);
            sample.setPotassium(0.3 + random.nextDouble() * 1.0);
            sample.setPhValue(6.0 + random.nextDouble() * 1.5);
            sample.setSamplingDepth("0-" + (20 + random.nextInt(20)) + "cm");
            sample.setAnalyst(analysts[random.nextInt(analysts.length)]);
            sample.setSamplingTime(LocalDateTime.now().minusHours(6 + i));
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

        String[] checkTypes = {"PHOTO_QUALITY", "DATA_INTEGRITY", "ACCURACY"};
        String[] checkTypeNames = {"照片质量检测", "数据完整性检测", "精度校验"};
        int checkCount = 1 + random.nextInt(2);

        for (int i = 0; i < checkCount; i++) {
            QualityCheck check = new QualityCheck();
            check.setBatchNo("QC-" + LocalDateTime.now().getDayOfYear() + "-00" + (i + 1));
            check.setCheckType(checkTypes[i]);
            check.setCheckName(checkTypeNames[i]);
            check.setTotalCount(500 + random.nextInt(1000));
            check.setPassedCount(480 + random.nextInt(490));
            check.setFailedCount(10 + random.nextInt(30));
            check.setPassRate(94 + random.nextDouble() * 5);
            check.setStatus(random.nextInt(10) > 2 ? "PASSED" : "FAILED");
            check.setCheckTime(LocalDateTime.now().minusHours(2 + i));
            check.setIsDeleted(0);
            qualityCheckMapper.insert(check);
        }
        log.info("[每日数据] 生成质量校验数据 {} 条", checkCount);
    }

    private void updateDeviceStatus() {
        try {
            Device[] devices = deviceMapper.selectList(null).toArray(new Device[0]);
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
            log.info("[每日数据] 更新设备状态完成，共 {} 台设备", devices.length);
        } catch (Exception e) {
            log.warn("[每日数据] 更新设备状态失败: {}", e.getMessage());
        }
    }
}
