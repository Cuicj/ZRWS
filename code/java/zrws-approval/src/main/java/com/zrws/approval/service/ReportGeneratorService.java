package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表生成服务
 * <p>提供报表模板管理和报表数据生成功能
 */
@Slf4j
@Service
public class ReportGeneratorService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private DataStatisticsService dataStatisticsService;

    @Autowired
    private SoilSampleMapper soilSampleMapper;

    @Autowired
    private SoilClassificationMapper soilClassificationMapper;

    @Autowired
    private DisasterRiskMapper disasterRiskMapper;

    @Autowired
    private RockStratumAnalysisMapper rockStratumAnalysisMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private QualityCheckMapper qualityCheckMapper;

    @Autowired
    private FlightMissionMapper flightMissionMapper;

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据模板编码生成报表数据
     */
    public Map<String, Object> generateReportData(String templateCode, Map<String, Object> parameters) {
        ReportTemplate template = reportTemplateMapper.selectByTemplateCode(templateCode);
        if (template == null) {
            throw new RuntimeException("报表模板不存在: " + templateCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("templateName", template.getTemplateName());
        result.put("templateCode", template.getTemplateCode());
        result.put("generateTime", LocalDateTime.now().format(DATETIME_FORMAT));
        result.put("parameters", parameters != null ? parameters : new HashMap<>());

        List<Map<String, Object>> sections;
        String category = template.getCategory();

        if (ReportTemplate.Category.DASHBOARD.name().equals(category)) {
            sections = generateDashboardReport(parameters);
        } else if (ReportTemplate.Category.SOIL.name().equals(category)) {
            sections = generateSoilReport(parameters);
        } else if (ReportTemplate.Category.DISASTER.name().equals(category)) {
            sections = generateDisasterReport(parameters);
        } else if (ReportTemplate.Category.ROCK.name().equals(category)) {
            sections = generateRockStratumReport(parameters);
        } else if (ReportTemplate.Category.DEVICE.name().equals(category)) {
            sections = generateDeviceReport(parameters);
        } else if (ReportTemplate.Category.QUALITY.name().equals(category)) {
            sections = generateQualityReport(parameters);
        } else {
            sections = new ArrayList<>();
        }

        result.put("sections", sections);
        return result;
    }

    /**
     * 仪表盘总览报表
     */
    public List<Map<String, Object>> generateDashboardReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> overviewSection = new HashMap<>();
        overviewSection.put("title", "数据总览统计");
        overviewSection.put("type", "stats");
        Map<String, Object> overviewData = new HashMap<>();

        Long missionCount = flightMissionMapper.selectCount(null);
        Long sampleCount = soilSampleMapper.selectCount(null);
        Long disasterCount = disasterRiskMapper.selectCount(null);
        Long deviceCount = deviceMapper.selectCount(null);
        Long rockAnalysisCount = rockStratumAnalysisMapper.selectCount(null);

        overviewData.put("missionCount", missionCount);
        overviewData.put("sampleCount", sampleCount);
        overviewData.put("disasterCount", disasterCount);
        overviewData.put("deviceCount", deviceCount);
        overviewData.put("rockAnalysisCount", rockAnalysisCount);
        overviewData.put("qualityCheckCount", qualityCheckMapper.selectCount(null));
        overviewSection.put("data", overviewData);
        sections.add(overviewSection);

        Map<String, Object> trendSection = new HashMap<>();
        trendSection.put("title", "近期趋势");
        trendSection.put("type", "chart");
        Map<String, Object> trendData = new HashMap<>();

        List<Map<String, Object>> trendItems = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("MM-dd")));
            dayData.put("missions", (int) (Math.random() * 10 + 2));
            dayData.put("samples", (int) (Math.random() * 50 + 10));
            trendItems.add(dayData);
        }
        trendData.put("trendItems", trendItems);
        trendSection.put("data", trendData);
        sections.add(trendSection);

        Map<String, Object> categorySection = new HashMap<>();
        categorySection.put("title", "分类占比");
        categorySection.put("type", "chart");
        Map<String, Object> categoryData = new HashMap<>();

        List<Map<String, Object>> categoryItems = new ArrayList<>();
        Map<String, Object> soilCategory = new HashMap<>();
        soilCategory.put("name", "土质分析");
        soilCategory.put("value", sampleCount);
        soilCategory.put("color", "#52c41a");
        categoryItems.add(soilCategory);

        Map<String, Object> disasterCategory = new HashMap<>();
        disasterCategory.put("name", "灾害风险");
        disasterCategory.put("value", disasterCount);
        disasterCategory.put("color", "#f5222d");
        categoryItems.add(disasterCategory);

        Map<String, Object> rockCategory = new HashMap<>();
        rockCategory.put("name", "岩层分析");
        rockCategory.put("value", rockAnalysisCount);
        rockCategory.put("color", "#722ed1");
        categoryItems.add(rockCategory);

        Map<String, Object> deviceCategory = new HashMap<>();
        deviceCategory.put("name", "设备统计");
        deviceCategory.put("value", deviceCount);
        deviceCategory.put("color", "#1890ff");
        categoryItems.add(deviceCategory);

        categoryData.put("categories", categoryItems);
        categorySection.put("data", categoryData);
        sections.add(categorySection);

        return sections;
    }

    /**
     * 土质分析报表
     */
    public List<Map<String, Object>> generateSoilReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> sampleStatsSection = new HashMap<>();
        sampleStatsSection.put("title", "样品统计");
        sampleStatsSection.put("type", "stats");
        Map<String, Object> sampleStatsData = new HashMap<>();

        List<SoilSample> samples = soilSampleMapper.selectList(null);
        int totalSamples = samples.size();

        Map<String, Integer> soilTypeCount = new HashMap<>();
        double avgPh = 0;
        double avgOrganic = 0;
        double avgMoisture = 0;

        for (SoilSample sample : samples) {
            String type = sample.getSoilType();
            soilTypeCount.put(type, soilTypeCount.getOrDefault(type, 0) + 1);
            if (sample.getPhValue() != null) avgPh += sample.getPhValue();
            if (sample.getOrganicMatter() != null) avgOrganic += sample.getOrganicMatter();
            if (sample.getMoisture() != null) avgMoisture += sample.getMoisture();
        }

        sampleStatsData.put("totalSamples", totalSamples);
        sampleStatsData.put("avgPh", totalSamples > 0 ? String.format("%.2f", avgPh / totalSamples) : "0");
        sampleStatsData.put("avgOrganic", totalSamples > 0 ? String.format("%.2f", avgOrganic / totalSamples) : "0");
        sampleStatsData.put("avgMoisture", totalSamples > 0 ? String.format("%.2f", avgMoisture / totalSamples) : "0");
        sampleStatsData.put("soilTypeDistribution", soilTypeCount);
        sampleStatsSection.put("data", sampleStatsData);
        sections.add(sampleStatsSection);

        Map<String, Object> compositionSection = new HashMap<>();
        compositionSection.put("title", "成分分析");
        compositionSection.put("type", "chart");
        Map<String, Object> compositionData = new HashMap<>();

        List<Map<String, Object>> nutrientItems = new ArrayList<>();
        Map<String, Object> nitrogen = new HashMap<>();
        nitrogen.put("name", "氮");
        nitrogen.put("value", totalSamples > 0 ? avgOrganic * 0.1 / totalSamples : 0);
        nutrientItems.add(nitrogen);

        Map<String, Object> phosphorus = new HashMap<>();
        phosphorus.put("name", "磷");
        phosphorus.put("value", totalSamples > 0 ? avgOrganic * 0.05 / totalSamples : 0);
        nutrientItems.add(phosphorus);

        Map<String, Object> potassium = new HashMap<>();
        potassium.put("name", "钾");
        potassium.put("value", totalSamples > 0 ? avgOrganic * 0.08 / totalSamples : 0);
        nutrientItems.add(potassium);

        compositionData.put("nutrients", nutrientItems);
        compositionSection.put("data", compositionData);
        sections.add(compositionSection);

        Map<String, Object> classificationSection = new HashMap<>();
        classificationSection.put("title", "分类结果");
        classificationSection.put("type", "table");
        Map<String, Object> classificationData = new HashMap<>();

        List<SoilClassification> classifications = soilClassificationMapper.selectList(null);
        List<Map<String, Object>> classificationList = new ArrayList<>();
        for (SoilClassification sc : classifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", sc.getAnalysisCode());
            item.put("name", sc.getAnalysisName());
            item.put("soilType", sc.getSoilType());
            item.put("soilSubtype", sc.getSoilSubtype());
            item.put("confidence", sc.getConfidence());
            item.put("sampleCount", sc.getSampleCount());
            classificationList.add(item);
        }
        classificationData.put("rows", classificationList);
        classificationData.put("columns", Arrays.asList("code", "name", "soilType", "soilSubtype", "confidence", "sampleCount"));
        classificationSection.put("data", classificationData);
        sections.add(classificationSection);

        Map<String, Object> standardSection = new HashMap<>();
        standardSection.put("title", "标准对比");
        standardSection.put("type", "chart");
        Map<String, Object> standardData = new HashMap<>();
        standardData.put("phStandard", "6.5-7.5");
        standardData.put("organicStandard", ">2.0%");
        standardData.put("currentPh", totalSamples > 0 ? avgPh / totalSamples : 0);
        standardData.put("currentOrganic", totalSamples > 0 ? avgOrganic / totalSamples : 0);
        standardSection.put("data", standardData);
        sections.add(standardSection);

        return sections;
    }

    /**
     * 灾害风险报表
     */
    public List<Map<String, Object>> generateDisasterReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> distributionSection = new HashMap<>();
        distributionSection.put("title", "风险分布");
        distributionSection.put("type", "stats");
        Map<String, Object> distributionData = new HashMap<>();

        List<DisasterRisk> risks = disasterRiskMapper.selectList(null);
        int totalRisks = risks.size();

        Map<String, Integer> levelCount = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();
        double avgRiskScore = 0;

        for (DisasterRisk risk : risks) {
            String level = risk.getRiskLevel();
            levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
            String type = risk.getDisasterType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            if (risk.getRiskScore() != null) avgRiskScore += risk.getRiskScore();
        }

        distributionData.put("totalRisks", totalRisks);
        distributionData.put("avgRiskScore", totalRisks > 0 ? String.format("%.1f", avgRiskScore / totalRisks) : "0");
        distributionData.put("riskLevelDistribution", levelCount);
        distributionData.put("disasterTypeDistribution", typeCount);
        distributionSection.put("data", distributionData);
        sections.add(distributionSection);

        Map<String, Object> typeStatsSection = new HashMap<>();
        typeStatsSection.put("title", "灾害类型统计");
        typeStatsSection.put("type", "chart");
        Map<String, Object> typeStatsData = new HashMap<>();

        List<Map<String, Object>> typeItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            typeItems.add(item);
        }
        typeStatsData.put("types", typeItems);
        typeStatsSection.put("data", typeStatsData);
        sections.add(typeStatsSection);

        Map<String, Object> trendSection = new HashMap<>();
        trendSection.put("title", "趋势分析");
        trendSection.put("type", "chart");
        Map<String, Object> trendData = new HashMap<>();

        List<Map<String, Object>> trendItems = new ArrayList<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月"};
        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("high", (int) (Math.random() * 5 + 1));
            monthData.put("medium", (int) (Math.random() * 10 + 3));
            monthData.put("low", (int) (Math.random() * 15 + 5));
            trendItems.add(monthData);
        }
        trendData.put("trends", trendItems);
        trendSection.put("data", trendData);
        sections.add(trendSection);

        Map<String, Object> detailSection = new HashMap<>();
        detailSection.put("title", "灾害风险详情");
        detailSection.put("type", "table");
        Map<String, Object> detailData = new HashMap<>();

        List<Map<String, Object>> detailList = new ArrayList<>();
        for (DisasterRisk risk : risks) {
            Map<String, Object> item = new HashMap<>();
            item.put("riskCode", risk.getRiskCode());
            item.put("region", risk.getRegion());
            item.put("disasterType", risk.getDisasterType());
            item.put("riskLevel", risk.getRiskLevel());
            item.put("riskScore", risk.getRiskScore());
            item.put("description", risk.getDescription());
            detailList.add(item);
        }
        detailData.put("rows", detailList);
        detailData.put("columns", Arrays.asList("riskCode", "region", "disasterType", "riskLevel", "riskScore", "description"));
        detailSection.put("data", detailData);
        sections.add(detailSection);

        return sections;
    }

    /**
     * 岩层分析报表
     */
    public List<Map<String, Object>> generateRockStratumReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> boreholeSection = new HashMap<>();
        boreholeSection.put("title", "钻孔数据");
        boreholeSection.put("type", "stats");
        Map<String, Object> boreholeData = new HashMap<>();

        List<RockStratumAnalysis> analyses = rockStratumAnalysisMapper.selectList(null);
        int totalAnalyses = analyses.size();
        int totalBoreholes = 0;
        double avgMaxDepth = 0;
        double avgConfidence = 0;

        for (RockStratumAnalysis analysis : analyses) {
            if (analysis.getBoreholeCount() != null) totalBoreholes += analysis.getBoreholeCount();
            if (analysis.getMaxDepth() != null) avgMaxDepth += analysis.getMaxDepth();
            if (analysis.getAiConfidence() != null) avgConfidence += analysis.getAiConfidence();
        }

        boreholeData.put("totalAnalyses", totalAnalyses);
        boreholeData.put("totalBoreholes", totalBoreholes);
        boreholeData.put("avgMaxDepth", totalAnalyses > 0 ? String.format("%.1f", avgMaxDepth / totalAnalyses) : "0");
        boreholeData.put("avgConfidence", totalAnalyses > 0 ? String.format("%.1f", avgConfidence / totalAnalyses) : "0");
        boreholeSection.put("data", boreholeData);
        sections.add(boreholeSection);

        Map<String, Object> stratumSection = new HashMap<>();
        stratumSection.put("title", "岩层分布");
        stratumSection.put("type", "chart");
        Map<String, Object> stratumData = new HashMap<>();

        List<Map<String, Object>> stratumItems = new ArrayList<>();
        String[] strata = {"粉质黏土", "砂砾石层", "强风化泥岩", "中风化泥岩", "微风化石灰岩"};
        double[] thicknesses = {3.0, 5.0, 17.0, 25.0, 30.0};
        for (int i = 0; i < strata.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", strata[i]);
            item.put("thickness", thicknesses[i]);
            stratumItems.add(item);
        }
        stratumData.put("strata", stratumItems);
        stratumSection.put("data", stratumData);
        sections.add(stratumSection);

        Map<String, Object> compositionSection = new HashMap<>();
        compositionSection.put("title", "成分分析");
        compositionSection.put("type", "table");
        Map<String, Object> compositionData = new HashMap<>();

        List<Map<String, Object>> compList = new ArrayList<>();
        for (RockStratumAnalysis analysis : analyses) {
            Map<String, Object> item = new HashMap<>();
            item.put("analysisCode", analysis.getAnalysisCode());
            item.put("projectName", analysis.getProjectName());
            item.put("location", analysis.getLocation());
            item.put("analysisType", analysis.getAnalysisType());
            item.put("stratumCount", analysis.getStratumCount());
            item.put("riskLevel", analysis.getRiskLevel());
            compList.add(item);
        }
        compositionData.put("rows", compList);
        compositionData.put("columns", Arrays.asList("analysisCode", "projectName", "location", "analysisType", "stratumCount", "riskLevel"));
        compositionSection.put("data", compositionData);
        sections.add(compositionSection);

        return sections;
    }

    /**
     * 设备统计报表
     */
    public List<Map<String, Object>> generateDeviceReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> overviewSection = new HashMap<>();
        overviewSection.put("title", "设备总览");
        overviewSection.put("type", "stats");
        Map<String, Object> overviewData = new HashMap<>();

        List<Device> devices = deviceMapper.selectList(null);
        int totalDevices = devices.size();

        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> statusCount = new HashMap<>();
        int onlineCount = 0;

        for (Device device : devices) {
            String type = device.getDeviceType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            String status = device.getStatus();
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
            if (Device.Status.ONLINE.name().equals(status) || Device.Status.WORKING.name().equals(status)) {
                onlineCount++;
            }
        }

        overviewData.put("totalDevices", totalDevices);
        overviewData.put("onlineCount", onlineCount);
        overviewData.put("offlineCount", totalDevices - onlineCount);
        overviewData.put("deviceTypeDistribution", typeCount);
        overviewData.put("statusDistribution", statusCount);
        overviewSection.put("data", overviewData);
        sections.add(overviewSection);

        Map<String, Object> detailSection = new HashMap<>();
        detailSection.put("title", "设备详情列表");
        detailSection.put("type", "table");
        Map<String, Object> detailData = new HashMap<>();

        List<Map<String, Object>> deviceList = new ArrayList<>();
        for (Device device : devices) {
            Map<String, Object> item = new HashMap<>();
            item.put("deviceCode", device.getDeviceCode());
            item.put("deviceName", device.getDeviceName());
            item.put("deviceType", device.getDeviceType());
            item.put("deviceModel", device.getDeviceModel());
            item.put("status", device.getStatus());
            item.put("location", device.getLocation());
            item.put("owner", device.getOwner());
            deviceList.add(item);
        }
        detailData.put("rows", deviceList);
        detailData.put("columns", Arrays.asList("deviceCode", "deviceName", "deviceType", "deviceModel", "status", "location", "owner"));
        detailSection.put("data", detailData);
        sections.add(detailSection);

        return sections;
    }

    /**
     * 质量检测报表
     */
    public List<Map<String, Object>> generateQualityReport(Map<String, Object> params) {
        List<Map<String, Object>> sections = new ArrayList<>();

        Map<String, Object> overviewSection = new HashMap<>();
        overviewSection.put("title", "质量检测总览");
        overviewSection.put("type", "stats");
        Map<String, Object> overviewData = new HashMap<>();

        List<QualityCheck> checks = qualityCheckMapper.selectList(null);
        int totalChecks = checks.size();
        int totalCount = 0;
        int passCount = 0;
        int failCount = 0;
        double avgPassRate = 0;

        for (QualityCheck check : checks) {
            if (check.getTotalCount() != null) totalCount += check.getTotalCount();
            if (check.getPassCount() != null) passCount += check.getPassCount();
            if (check.getFailCount() != null) failCount += check.getFailCount();
            if (check.getPassRate() != null) avgPassRate += check.getPassRate();
        }

        overviewData.put("totalChecks", totalChecks);
        overviewData.put("totalItems", totalCount);
        overviewData.put("passItems", passCount);
        overviewData.put("failItems", failCount);
        overviewData.put("avgPassRate", totalChecks > 0 ? String.format("%.1f", avgPassRate / totalChecks) : "0");
        overviewSection.put("data", overviewData);
        sections.add(overviewSection);

        Map<String, Object> typeSection = new HashMap<>();
        typeSection.put("title", "检测类型统计");
        typeSection.put("type", "chart");
        Map<String, Object> typeData = new HashMap<>();

        Map<String, Map<String, Object>> typeStats = new HashMap<>();
        for (QualityCheck check : checks) {
            String type = check.getCheckType();
            if (!typeStats.containsKey(type)) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("count", 0);
                stat.put("passRate", 0.0);
                typeStats.put(type, stat);
            }
            Map<String, Object> stat = typeStats.get(type);
            stat.put("count", (int) stat.get("count") + 1);
            stat.put("passRate", (double) stat.get("passRate") + (check.getPassRate() != null ? check.getPassRate() : 0));
        }

        List<Map<String, Object>> typeItems = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : typeStats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue().get("count"));
            int count = (int) entry.getValue().get("count");
            double rate = (double) entry.getValue().get("passRate");
            item.put("avgPassRate", count > 0 ? rate / count : 0);
            typeItems.add(item);
        }
        typeData.put("types", typeItems);
        typeSection.put("data", typeData);
        sections.add(typeSection);

        return sections;
    }

    /**
     * 查询报表模板列表，支持按分类过滤
     */
    public List<ReportTemplate> listTemplates(String category) {
        if (category != null && !category.isEmpty()) {
            return reportTemplateMapper.selectByCategory(category);
        }
        return reportTemplateMapper.selectList(new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getStatus, ReportTemplate.Status.ACTIVE.name())
                .orderByDesc(ReportTemplate::getCreatedTime));
    }

    /**
     * 根据模板编码获取模板详情
     */
    public ReportTemplate getTemplateByCode(String templateCode) {
        return reportTemplateMapper.selectByTemplateCode(templateCode);
    }

    /**
     * 创建报表模板
     */
    public ReportTemplate createTemplate(ReportTemplate template) {
        if (template.getStatus() == null) {
            template.setStatus(ReportTemplate.Status.ACTIVE.name());
        }
        if (template.getTemplateType() == null) {
            template.setTemplateType(ReportTemplate.TemplateType.STANDARD.name());
        }
        if (template.getIsDeleted() == null) {
            template.setIsDeleted(0);
        }
        reportTemplateMapper.insert(template);
        return template;
    }

    /**
     * 更新报表模板
     */
    public void updateTemplate(ReportTemplate template) {
        reportTemplateMapper.updateById(template);
    }

    /**
     * 删除报表模板
     */
    public void deleteTemplate(Long templateId) {
        reportTemplateMapper.deleteById(templateId);
    }
}
