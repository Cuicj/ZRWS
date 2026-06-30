package com.zrws.approval.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 每日数据生成定时任务
 * 每天凌晨2点自动生成新的测试数据，确保各页面有数据展示
 */
@Slf4j
@Component
public class DailyDataGenerator {

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
    @Autowired
    private ApprovalTaskMapper approvalTaskMapper;
    @Autowired
    private DisasterRiskMapper disasterRiskMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 每日凌晨2点执行数据生成
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyData() {
        log.info("[定时任务] 开始生成每日数据...");
        
        int totalGenerated = 0;
        
        try {
            // 生成今日飞行任务
            int missions = generateFlightMissions();
            totalGenerated += missions;
            
            // 生成今日土壤采样
            int samples = generateSoilSamples();
            totalGenerated += samples;
            
            // 更新设备状态
            updateDeviceStatus();
            
            // 生成今日质量校验记录
            int checks = generateQualityChecks();
            totalGenerated += checks;
            
            // 生成今日公告
            int announcements = generateAnnouncements();
            totalGenerated += announcements;
            
            // 生成今日审批任务
            int approvals = generateApprovalTasks();
            totalGenerated += approvals;
            
            // 生成灾害风险评估
            int risks = generateDisasterRisks();
            totalGenerated += risks;
            
            log.info("[定时任务] 每日数据生成完成，共生成 {} 条数据", totalGenerated);
            
        } catch (Exception e) {
            log.error("[定时任务] 每日数据生成失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每小时更新设备在线状态和仪表盘数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateHourlyData() {
        log.info("[定时任务] 每小时更新设备状态...");
        try {
            updateDeviceStatus();
            log.info("[定时任务] 设备状态更新完成");
        } catch (Exception e) {
            log.error("[定时任务] 设备状态更新失败: {}", e.getMessage());
        }
    }

    private int generateFlightMissions() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        String missionCode = "ZRS-" + dateStr + "-001";
        
        // 检查是否已存在今日任务
        Long count = flightMissionMapper.selectCount(
            new LambdaQueryWrapper<FlightMission>()
                .eq(FlightMission::getMissionCode, missionCode)
        );
        if (count > 0) {
            log.info("[定时任务] 今日飞行任务已存在，跳过生成");
            return 0;
        }
        
        String[] areas = {"望城区乔口镇", "岳麓区莲花镇", "雨花区跳马镇", "开福区青竹湖", "天心区暮云镇"};
        String[] operators = {"王工", "李工", "张工", "赵工"};
        String[] weathers = {"晴", "多云", "阴"};
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String area = areas[random.nextInt(areas.length)];
        String operator = operators[random.nextInt(operators.length)];
        String weather = weathers[random.nextInt(weathers.length)];
        
        FlightMission mission = new FlightMission();
        mission.setMissionCode(missionCode);
        mission.setAreaName(area);
        mission.setDroneId("UAV-DJI-M350-003");
        mission.setDroneModel("DJI Matrice 350 RTK");
        mission.setOperator(operator);
        mission.setFlightTime(LocalDateTime.now().withHour(8).withMinute(30));
        mission.setDuration(random.nextInt(20, 60));
        mission.setCoverage(random.nextDouble() * 1500 + 500);
        mission.setAltitude(random.nextDouble() * 50 + 80);
        mission.setForwardOverlap(0.75 + random.nextDouble() * 0.1);
        mission.setSideOverlap(0.6 + random.nextDouble() * 0.1);
        mission.setPhotoCount(random.nextInt(500, 2500));
        mission.setLidarPoints((long)(random.nextInt(100_000_000, 500_000_000)));
        mission.setSoilSamples(random.nextInt(10, 50));
        mission.setGpsAccuracyH("±1.0cm");
        mission.setGpsAccuracyV("±1.8cm");
        mission.setStatus(FlightMission.Status.PROCESSING.name());
        mission.setCenterLat(28.45 + random.nextDouble() * 0.1);
        mission.setCenterLng(112.8 + random.nextDouble() * 0.2);
        mission.setWeather(weather);
        mission.setIsDeleted(0);
        
        flightMissionMapper.insert(mission);
        log.info("[定时任务] 生成飞行任务: {}", missionCode);
        return 1;
    }

    private int generateSoilSamples() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        
        // 获取今日任务编码
        FlightMission latestMission = flightMissionMapper.selectOne(
            new LambdaQueryWrapper<FlightMission>()
                .orderByDesc(FlightMission::getCreatedTime)
                .last("LIMIT 1")
        );
        
        if (latestMission == null) {
            return 0;
        }
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(3, 8);
        
        for (int i = 1; i <= count; i++) {
            String sampleCode = "SP-" + dateStr + "-" + String.format("%03d", i);
            
            SoilSample sample = new SoilSample();
            sample.setSampleCode(sampleCode);
            sample.setMissionCode(latestMission.getMissionCode());
            sample.setLatitude(latestMission.getCenterLat() + random.nextDouble() * 0.01);
            sample.setLongitude(latestMission.getCenterLng() + random.nextDouble() * 0.01);
            sample.setElevation(random.nextDouble() * 50 + 30);
            sample.setPhValue(random.nextDouble() * 2 + 5.5);
            sample.setMoisture(random.nextDouble() * 0.4);
            sample.setEcValue(random.nextInt(100, 400));
            sample.setSoilType(random.nextInt(3) == 0 ? SoilSample.SoilType.LOAM.name() : 
                              random.nextInt(3) == 1 ? SoilSample.SoilType.CLAY.name() : 
                              SoilSample.SoilType.SAND.name());
            sample.setSoilTexture(sample.getSoilType().equals("LOAM") ? "壤土" : 
                                  sample.getSoilType().equals("CLAY") ? "黏土" : "砂土");
            sample.setOrganicMatter(random.nextDouble() * 3 + 1);
            sample.setNitrogen(random.nextDouble() * 1.5);
            sample.setPhosphorus(random.nextDouble() * 0.8);
            sample.setPotassium(random.nextDouble() * 2);
            sample.setDepth("0-30cm");
            sample.setCollector("王工");
            sample.setCollectTime(LocalDateTime.now().minusHours(random.nextInt(1, 6)));
            sample.setStatus("COMPLETED");
            sample.setIsDeleted(0);
            
            soilSampleMapper.insert(sample);
        }
        
        log.info("[定时任务] 生成 {} 条土壤采样数据", count);
        return count;
    }

    private void updateDeviceStatus() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 获取所有设备
        var devices = deviceMapper.selectList(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getIsDeleted, 0)
        );
        
        for (Device device : devices) {
            if (device.getDeviceType().equals(Device.DeviceType.DRONE.name())) {
                // 无人机：随机更新电池、信号、存储
                int battery = random.nextInt(20, 100);
                int signal = random.nextInt(50, 100);
                int storage = random.nextInt(10, 80);
                
                device.setBatteryLevel(battery);
                device.setSignalLevel(signal);
                device.setStorageLevel(storage);
                device.setTemperature(random.nextDouble() * 15 + 25);
                device.setLastOnline(LocalDateTime.now());
                
                // 根据电池决定状态
                if (battery < 20) {
                    device.setStatus(Device.Status.CHARGING.name());
                } else if (battery > 80) {
                    device.setStatus(Device.Status.ONLINE.name());
                }
            } else {
                // 其他设备：更新在线状态
                device.setLastOnline(LocalDateTime.now());
                device.setStatus(Device.Status.ONLINE.name());
            }
            
            deviceMapper.updateById(device);
        }
        
        log.info("[定时任务] 更新 {} 台设备状态", devices.size());
    }

    private int generateQualityChecks() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        
        FlightMission latestMission = flightMissionMapper.selectOne(
            new LambdaQueryWrapper<FlightMission>()
                .orderByDesc(FlightMission::getCreatedTime)
                .last("LIMIT 1")
        );
        
        if (latestMission == null) {
            return 0;
        }
        
        int count = random.nextInt(1, 4);
        String[] checkTypes = {
            QualityCheck.CheckType.PHOTO_QUALITY.name(),
            QualityCheck.CheckType.GPS_COMPLETENESS.name(),
            QualityCheck.CheckType.LIDAR_DENSITY.name()
        };
        String[] checkItems = {"照片质量检测", "GPS完整性检查", "LiDAR点云密度检测"};
        
        for (int i = 1; i <= count; i++) {
            String checkCode = "QC-" + dateStr + "-" + String.format("%03d", i);
            int typeIndex = random.nextInt(checkTypes.length);
            
            QualityCheck check = new QualityCheck();
            check.setCheckCode(checkCode);
            check.setBatchId(1L);
            check.setMissionCode(latestMission.getMissionCode());
            check.setCheckType(checkTypes[typeIndex]);
            check.setCheckItem(checkItems[typeIndex]);
            check.setTotalCount(random.nextInt(500, 2000));
            int pass = (int)(check.getTotalCount() * (0.9 + random.nextDouble() * 0.08));
            check.setPassCount(pass);
            check.setFailCount(check.getTotalCount() - pass);
            check.setPassRate((double)pass / check.getTotalCount() * 100);
            check.setStatus(pass / check.getTotalCount() > 0.95 ? 
                           QualityCheck.Status.PASSED.name() : QualityCheck.Status.PARTIAL.name());
            check.setChecker("张工");
            check.setCheckTime(LocalDateTime.now());
            check.setIsDeleted(0);
            
            qualityCheckMapper.insert(check);
        }
        
        log.info("[定时任务] 生成 {} 条质量校验数据", count);
        return count;
    }

    private int generateAnnouncements() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 每天最多生成1条公告
        String[] titles = {
            "土地资源监测日报 - " + LocalDate.now().toString(),
            "本周土地整治工作进展通报",
            "地质灾害风险研判简报"
        };
        String[] categories = {"政策法规", "土地整治", "灾害预警"};
        Long[] categoryIds = {1L, 2L, 3L};
        
        int index = random.nextInt(titles.length);
        
        Announcement announcement = new Announcement();
        announcement.setTitle(titles[index]);
        announcement.setContent("本日报由系统自动生成，汇总今日土地资源监测数据及工作进展。");
        announcement.setSummary("系统自动生成的每日工作简报");
        announcement.setCategoryId(categoryIds[index]);
        announcement.setCategoryName(categories[index]);
        announcement.setAuthor("系统");
        announcement.setSource("智壤卫士系统");
        announcement.setProvince("湖南省");
        announcement.setCity("长沙市");
        announcement.setAdminLevel(Announcement.AdminLevel.PROVINCE.name());
        announcement.setKeywords("日报,监测,土地");
        announcement.setTags("日报");
        announcement.setIsTop(0);
        announcement.setIsRecommend(0);
        announcement.setIsHot(0);
        announcement.setAiSummary("AI分析：本日数据正常，无异常情况。");
        announcement.setAiTimeline("[]");
        announcement.setAiKeywords("日报");
        announcement.setStatus(Announcement.Status.PUBLISHED.name());
        announcement.setAuditStatus(Announcement.AuditStatus.PASSED.name());
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setViewCount(random.nextInt(50, 200));
        announcement.setLikeCount(random.nextInt(5, 30));
        announcement.setCommentCount(random.nextInt(0, 10));
        announcement.setPriority(0);
        announcement.setIsDeleted(0);
        
        announcementMapper.insert(announcement);
        log.info("[定时任务] 生成公告: {}", titles[index]);
        return 1;
    }

    private int generateApprovalTasks() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        
        // 检查今日是否已有审批任务
        Long count = approvalTaskMapper.selectCount(
            new LambdaQueryWrapper<ApprovalTask>()
                .ge(ApprovalTask::getCreatedTime, today.atStartOfDay())
        );
        
        if (count > 3) {
            log.info("[定时任务] 今日审批任务足够，跳过生成");
            return 0;
        }
        
        String[] bizTypes = {"STANDARD", "DRONE_FLIGHT", "MATERIAL"};
        String[] titles = {
            "土地整治项目立项审批",
            "无人机飞行任务审批",
            "设备采购申请审批"
        };
        String[] steps = {"科室负责人审核", "部门负责人审批", "财务审核"};
        String[] stepKeys = {"dept_leader", "dept_head", "finance_review"};
        String[] names = {"张三", "李四", "王五"};
        String[] depts = {"土地整治科", "外业采集部", "技术装备科"};
        
        int index = random.nextInt(titles.length);
        String taskCode = "AP-" + dateStr + "-" + String.format("%03d", random.nextInt(100));
        
        ApprovalTask task = new ApprovalTask();
        task.setBizType(bizTypes[index]);
        task.setBizTitle(titles[index] + " - " + dateStr);
        task.setApplicantId((long)random.nextInt(1, 100));
        task.setApplicantName(names[random.nextInt(names.length)]);
        task.setApplicantDept(depts[random.nextInt(depts.length)]);
        task.setCurStep(steps[index]);
        task.setCurStepKey(stepKeys[index]);
        task.setStatus(ApprovalTask.Status.PROCESSING.name());
        task.setPriority(random.nextInt(1, 3));
        task.setBizData("{}");
        task.setSlaDeadline(LocalDateTime.now().plusDays(2));
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        task.setIsDeleted(0);
        
        approvalTaskMapper.insert(task);
        log.info("[定时任务] 生成审批任务: {}", task.getBizTitle());
        return 1;
    }

    private int generateDisasterRisks() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        
        // 检查今日是否已有灾害风险数据
        Long count = disasterRiskMapper.selectCount(
            new LambdaQueryWrapper<DisasterRisk>()
                .ge(DisasterRisk::getCreatedTime, today.atStartOfDay())
        );
        
        if (count > 2) {
            log.info("[定时任务] 今日灾害风险数据足够，跳过生成");
            return 0;
        }
        
        FlightMission latestMission = flightMissionMapper.selectOne(
            new LambdaQueryWrapper<FlightMission>()
                .orderByDesc(FlightMission::getCreatedTime)
                .last("LIMIT 1")
        );
        
        if (latestMission == null) {
            return 0;
        }
        
        String riskCode = "DR-" + dateStr + "-001";
        String[] disasterTypes = {
            DisasterRisk.DisasterType.LANDSLIDE.name(),
            DisasterRisk.DisasterType.FLOOD.name()
        };
        String[] riskLevels = {
            DisasterRisk.RiskLevel.LOW.name(),
            DisasterRisk.RiskLevel.MEDIUM.name()
        };
        
        DisasterRisk risk = new DisasterRisk();
        risk.setRiskCode(riskCode);
        risk.setMissionCode(latestMission.getMissionCode());
        risk.setRegion(latestMission.getAreaName());
        risk.setLatitude(latestMission.getCenterLat());
        risk.setLongitude(latestMission.getCenterLng());
        risk.setDisasterType(disasterTypes[random.nextInt(disasterTypes.length)]);
        risk.setRiskLevel(riskLevels[random.nextInt(riskLevels.length)]);
        risk.setRiskScore(random.nextDouble() * 50 + 10);
        risk.setDescription("系统自动风险评估");
        risk.setInfluencingFactors("{}");
        risk.setMonitoringData("{}");
        risk.setHistoricalRecords("[]");
        risk.setAiAnalysis("AI分析：该区域风险等级较低，建议定期巡查。");
        risk.setAiSuggestion("建议加强日常监测");
        risk.setAiConfidence(85.0 + random.nextDouble() * 10);
        risk.setStatus("COMPLETED");
        risk.setAnalyst("系统");
        risk.setAssessmentTime(LocalDateTime.now());
        risk.setIsDeleted(0);
        
        disasterRiskMapper.insert(risk);
        log.info("[定时任务] 生成灾害风险: {}", riskCode);
        return 1;
    }
}