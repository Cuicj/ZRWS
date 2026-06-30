package com.zrws.approval.config;

import com.zrws.approval.domain.entity.EcoStandard;
import com.zrws.approval.service.EcoStandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(3)
public class EcoDataInitializer implements ApplicationRunner {

    @Autowired
    private EcoStandardService ecoStandardService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            initClimateWarmingStandards();
            initDesertificationStandards();
            initSoilErosionStandards();
            initEcoSafetyStandards();
            initClimateZoneStandards();
            log.info("[生态标准] 数据初始化完成");
        } catch (Exception e) {
            log.error("[生态标准] 数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void initClimateWarmingStandards() {
        List<EcoStandard> standards = Arrays.asList(
                createStandard("CW-TA-01", "显著偏冷", "CLIMATE_WARMING", "温度异常分级", "IPCC_AR6", "EXTREME", null, new BigDecimal("-1.0"), "°C", "温度距平低于-1.0°C，显著偏冷", "IPCC Sixth Assessment Report", 1),
                createStandard("CW-TA-02", "偏冷", "CLIMATE_WARMING", "温度异常分级", "IPCC_AR6", "SEVERE", new BigDecimal("-1.0"), new BigDecimal("-0.5"), "°C", "温度距平-1.0~-0.5°C，偏冷", "IPCC Sixth Assessment Report", 2),
                createStandard("CW-TA-03", "正常", "CLIMATE_WARMING", "温度异常分级", "IPCC_AR6", "MILD", new BigDecimal("-0.5"), new BigDecimal("0.5"), "°C", "温度距平-0.5~0.5°C，正常范围", "IPCC Sixth Assessment Report", 3),
                createStandard("CW-TA-04", "偏暖", "CLIMATE_WARMING", "温度异常分级", "IPCC_AR6", "MODERATE", new BigDecimal("0.5"), new BigDecimal("1.0"), "°C", "温度距平0.5~1.0°C，偏暖", "IPCC Sixth Assessment Report", 4),
                createStandard("CW-TA-05", "显著偏暖", "CLIMATE_WARMING", "温度异常分级", "IPCC_AR6", "EXTREME", new BigDecimal("1.0"), null, "°C", "温度距平高于1.0°C，显著偏暖", "IPCC Sixth Assessment Report", 5),
                createStandard("CW-EH-01", "轻度高温", "CLIMATE_WARMING", "极端高温分级", "WMO_ETCCDI", "MILD", new BigDecimal("35.0"), new BigDecimal("37.0"), "°C", "日最高气温35-37°C，轻度高温", "WMO ETCCDI指标体系", 1),
                createStandard("CW-EH-02", "中度高温", "CLIMATE_WARMING", "极端高温分级", "WMO_ETCCDI", "MODERATE", new BigDecimal("37.0"), new BigDecimal("40.0"), "°C", "日最高气温37-40°C，中度高温", "WMO ETCCDI指标体系", 2),
                createStandard("CW-EH-03", "重度高温", "CLIMATE_WARMING", "极端高温分级", "WMO_ETCCDI", "SEVERE", new BigDecimal("40.0"), new BigDecimal("42.0"), "°C", "日最高气温40-42°C，重度高温", "WMO ETCCDI指标体系", 3),
                createStandard("CW-EH-04", "极端高温", "CLIMATE_WARMING", "极端高温分级", "WMO_ETCCDI", "EXTREME", new BigDecimal("42.0"), null, "°C", "日最高气温>42°C，极端高温", "WMO ETCCDI指标体系", 4),
                createStandard("CW-EL-01", "轻度低温", "CLIMATE_WARMING", "极端低温分级", "WMO_ETCCDI", "MILD", new BigDecimal("-5.0"), new BigDecimal("0.0"), "°C", "日最低气温-5~0°C，轻度低温", "WMO ETCCDI指标体系", 1),
                createStandard("CW-EL-02", "中度低温", "CLIMATE_WARMING", "极端低温分级", "WMO_ETCCDI", "MODERATE", new BigDecimal("-10.0"), new BigDecimal("-5.0"), "°C", "日最低气温-10~-5°C，中度低温", "WMO ETCCDI指标体系", 2),
                createStandard("CW-EL-03", "重度低温", "CLIMATE_WARMING", "极端低温分级", "WMO_ETCCDI", "SEVERE", new BigDecimal("-20.0"), new BigDecimal("-10.0"), "°C", "日最低气温-20~-10°C，重度低温", "WMO ETCCDI指标体系", 3),
                createStandard("CW-EL-04", "极端低温", "CLIMATE_WARMING", "极端低温分级", "WMO_ETCCDI", "EXTREME", null, new BigDecimal("-20.0"), "°C", "日最低气温<-20°C，极端低温", "WMO ETCCDI指标体系", 4),
                createStandard("CW-WT-01", "快速变暖", "CLIMATE_WARMING", "变暖趋势分级", "IPCC", "EXTREME", new BigDecimal("0.4"), null, "°C/10a", "10年升温速率>0.4°C，快速变暖", "IPCC AR6", 1),
                createStandard("CW-WT-02", "中等变暖", "CLIMATE_WARMING", "变暖趋势分级", "IPCC", "SEVERE", new BigDecimal("0.2"), new BigDecimal("0.4"), "°C/10a", "10年升温速率0.2-0.4°C，中等变暖", "IPCC AR6", 2),
                createStandard("CW-WT-03", "缓慢变暖", "CLIMATE_WARMING", "变暖趋势分级", "IPCC", "MODERATE", new BigDecimal("0.1"), new BigDecimal("0.2"), "°C/10a", "10年升温速率0.1-0.2°C，缓慢变暖", "IPCC AR6", 3),
                createStandard("CW-WT-04", "稳定", "CLIMATE_WARMING", "变暖趋势分级", "IPCC", "MILD", new BigDecimal("-0.1"), new BigDecimal("0.1"), "°C/10a", "10年升温速率-0.1~0.1°C，基本稳定", "IPCC AR6", 4),
                createStandard("CW-WT-05", "降温趋势", "CLIMATE_WARMING", "变暖趋势分级", "IPCC", "LOW", null, new BigDecimal("-0.1"), "°C/10a", "10年升温速率<-0.1°C，呈降温趋势", "IPCC AR6", 5),
                createStandard("CW-RL-01", "低风险", "CLIMATE_WARMING", "风险等级", "CUSTOM", "LOW", new BigDecimal("0"), new BigDecimal("25"), "分", "综合评分0-25分，低风险", "系统自定义", 1),
                createStandard("CW-RL-02", "中风险", "CLIMATE_WARMING", "风险等级", "CUSTOM", "MEDIUM", new BigDecimal("26"), new BigDecimal("50"), "分", "综合评分26-50分，中风险", "系统自定义", 2),
                createStandard("CW-RL-03", "高风险", "CLIMATE_WARMING", "风险等级", "CUSTOM", "HIGH", new BigDecimal("51"), new BigDecimal("75"), "分", "综合评分51-75分，高风险", "系统自定义", 3),
                createStandard("CW-RL-04", "极高风险", "CLIMATE_WARMING", "风险等级", "CUSTOM", "EXTREME", new BigDecimal("76"), new BigDecimal("100"), "分", "综合评分76-100分，极高风险", "系统自定义", 4)
        );
        saveIfNotExists(standards);
    }

    private void initDesertificationStandards() {
        List<EcoStandard> standards = Arrays.asList(
                createStandard("DS-DG-01", "轻度沙漠化", "DESERTIFICATION", "沙漠化程度分级", "UNCCD_LADA", "MILD", new BigDecimal("30"), new BigDecimal("50"), "%", "植被覆盖度30-50%，轻度沙漠化", "UNCCD LADA项目", 1),
                createStandard("DS-DG-02", "中度沙漠化", "DESERTIFICATION", "沙漠化程度分级", "UNCCD_LADA", "MODERATE", new BigDecimal("10"), new BigDecimal("30"), "%", "植被覆盖度10-30%，中度沙漠化", "UNCCD LADA项目", 2),
                createStandard("DS-DG-03", "重度沙漠化", "DESERTIFICATION", "沙漠化程度分级", "UNCCD_LADA", "SEVERE", new BigDecimal("5"), new BigDecimal("10"), "%", "植被覆盖度5-10%，重度沙漠化", "UNCCD LADA项目", 3),
                createStandard("DS-DG-04", "极重度沙漠化", "DESERTIFICATION", "沙漠化程度分级", "UNCCD_LADA", "EXTREME", new BigDecimal("0"), new BigDecimal("5"), "%", "植被覆盖度<5%，极重度沙漠化", "UNCCD LADA项目", 4),
                createStandard("DS-VC-01", "高覆盖度", "DESERTIFICATION", "植被覆盖度分级", "FAO", "HIGH", new BigDecimal("70"), new BigDecimal("100"), "%", "植被覆盖度>70%，高覆盖度", "FAO荒漠化评价标准", 1),
                createStandard("DS-VC-02", "中高覆盖度", "DESERTIFICATION", "植被覆盖度分级", "FAO", "MODERATE_HIGH", new BigDecimal("50"), new BigDecimal("70"), "%", "植被覆盖度50-70%，中高覆盖度", "FAO荒漠化评价标准", 2),
                createStandard("DS-VC-03", "中等覆盖度", "DESERTIFICATION", "植被覆盖度分级", "FAO", "MODERATE", new BigDecimal("30"), new BigDecimal("50"), "%", "植被覆盖度30-50%，中等覆盖度", "FAO荒漠化评价标准", 3),
                createStandard("DS-VC-04", "低覆盖度", "DESERTIFICATION", "植被覆盖度分级", "FAO", "LOW", new BigDecimal("10"), new BigDecimal("30"), "%", "植被覆盖度10-30%，低覆盖度", "FAO荒漠化评价标准", 4),
                createStandard("DS-VC-05", "极低覆盖度", "DESERTIFICATION", "植被覆盖度分级", "FAO", "VERY_LOW", new BigDecimal("0"), new BigDecimal("10"), "%", "植被覆盖度<10%，极低覆盖度", "FAO荒漠化评价标准", 5),
                createStandard("DS-AI-01", "湿润区", "DESERTIFICATION", "干旱指数分级", "UNEP", "HUMID", new BigDecimal("1.0"), null, "", "干旱指数AI>1.0，湿润区", "UNEP干旱指数分类", 1),
                createStandard("DS-AI-02", "半湿润区", "DESERTIFICATION", "干旱指数分级", "UNEP", "SEMI_HUMID", new BigDecimal("0.65"), new BigDecimal("1.0"), "", "干旱指数AI 0.65-1.0，半湿润区", "UNEP干旱指数分类", 2),
                createStandard("DS-AI-03", "半干旱区", "DESERTIFICATION", "干旱指数分级", "UNEP", "SEMI_ARID", new BigDecimal("0.3"), new BigDecimal("0.65"), "", "干旱指数AI 0.3-0.65，半干旱区", "UNEP干旱指数分类", 3),
                createStandard("DS-AI-04", "干旱区", "DESERTIFICATION", "干旱指数分级", "UNEP", "ARID", new BigDecimal("0.13"), new BigDecimal("0.3"), "", "干旱指数AI 0.13-0.3，干旱区", "UNEP干旱指数分类", 4),
                createStandard("DS-AI-05", "极干旱区", "DESERTIFICATION", "干旱指数分级", "UNEP", "HYPER_ARID", new BigDecimal("0"), new BigDecimal("0.13"), "", "干旱指数AI<0.13，极干旱区", "UNEP干旱指数分类", 5),
                createStandard("DS-WE-01", "微度风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "MILD", new BigDecimal("0"), new BigDecimal("200"), "t/km²·a", "风蚀模数<200 t/km²·a，微度", "SL 190-2007土壤侵蚀分类分级标准", 1),
                createStandard("DS-WE-02", "轻度风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "LIGHT", new BigDecimal("200"), new BigDecimal("2500"), "t/km²·a", "风蚀模数200-2500 t/km²·a，轻度", "SL 190-2007土壤侵蚀分类分级标准", 2),
                createStandard("DS-WE-03", "中度风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "MODERATE", new BigDecimal("2500"), new BigDecimal("5000"), "t/km²·a", "风蚀模数2500-5000 t/km²·a，中度", "SL 190-2007土壤侵蚀分类分级标准", 3),
                createStandard("DS-WE-04", "强烈风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "SEVERE", new BigDecimal("5000"), new BigDecimal("8000"), "t/km²·a", "风蚀模数5000-8000 t/km²·a，强烈", "SL 190-2007土壤侵蚀分类分级标准", 4),
                createStandard("DS-WE-05", "极强烈风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "VERY_SEVERE", new BigDecimal("8000"), new BigDecimal("15000"), "t/km²·a", "风蚀模数8000-15000 t/km²·a，极强烈", "SL 190-2007土壤侵蚀分类分级标准", 5),
                createStandard("DS-WE-06", "剧烈风蚀", "DESERTIFICATION", "风蚀强度分级", "SL_190_2007", "EXTREME", new BigDecimal("15000"), null, "t/km²·a", "风蚀模数>15000 t/km²·a，剧烈", "SL 190-2007土壤侵蚀分类分级标准", 6),
                createStandard("DS-DT-01", "风蚀沙漠化", "DESERTIFICATION", "沙漠化类型", "GB", null, null, null, "", "", "国家标准", 1),
                createStandard("DS-DT-02", "水蚀沙漠化", "DESERTIFICATION", "沙漠化类型", "GB", null, null, null, "", "", "国家标准", 2),
                createStandard("DS-DT-03", "盐渍化沙漠化", "DESERTIFICATION", "沙漠化类型", "GB", null, null, null, "", "", "国家标准", 3),
                createStandard("DS-DT-04", "冻融沙漠化", "DESERTIFICATION", "沙漠化类型", "GB", null, null, null, "", "", "国家标准", 4),
                createStandard("DS-RL-01", "低风险", "DESERTIFICATION", "风险等级", "CUSTOM", "LOW", new BigDecimal("0"), new BigDecimal("25"), "分", "综合评分0-25分，低风险", "系统自定义", 1),
                createStandard("DS-RL-02", "中风险", "DESERTIFICATION", "风险等级", "CUSTOM", "MEDIUM", new BigDecimal("26"), new BigDecimal("50"), "分", "综合评分26-50分，中风险", "系统自定义", 2),
                createStandard("DS-RL-03", "高风险", "DESERTIFICATION", "风险等级", "CUSTOM", "HIGH", new BigDecimal("51"), new BigDecimal("75"), "分", "综合评分51-75分，高风险", "系统自定义", 3),
                createStandard("DS-RL-04", "极高风险", "DESERTIFICATION", "风险等级", "CUSTOM", "EXTREME", new BigDecimal("76"), new BigDecimal("100"), "分", "综合评分76-100分，极高风险", "系统自定义", 4)
        );
        saveIfNotExists(standards);
    }

    private void initSoilErosionStandards() {
        List<EcoStandard> standards = Arrays.asList(
                createStandard("SE-HE-01", "微度水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "MILD", new BigDecimal("0"), new BigDecimal("200"), "t/km²·a", "侵蚀模数<200 t/km²·a，微度", "SL 190-2007土壤侵蚀分类分级标准", 1),
                createStandard("SE-HE-02", "轻度水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "LIGHT", new BigDecimal("200"), new BigDecimal("2500"), "t/km²·a", "侵蚀模数200-2500 t/km²·a，轻度", "SL 190-2007土壤侵蚀分类分级标准", 2),
                createStandard("SE-HE-03", "中度水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "MODERATE", new BigDecimal("2500"), new BigDecimal("5000"), "t/km²·a", "侵蚀模数2500-5000 t/km²·a，中度", "SL 190-2007土壤侵蚀分类分级标准", 3),
                createStandard("SE-HE-04", "强烈水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "SEVERE", new BigDecimal("5000"), new BigDecimal("8000"), "t/km²·a", "侵蚀模数5000-8000 t/km²·a，强烈", "SL 190-2007土壤侵蚀分类分级标准", 4),
                createStandard("SE-HE-05", "极强烈水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "VERY_SEVERE", new BigDecimal("8000"), new BigDecimal("15000"), "t/km²·a", "侵蚀模数8000-15000 t/km²·a，极强烈", "SL 190-2007土壤侵蚀分类分级标准", 5),
                createStandard("SE-HE-06", "剧烈水力侵蚀", "SOIL_EROSION", "水力侵蚀分级", "SL_190_2007", "EXTREME", new BigDecimal("15000"), null, "t/km²·a", "侵蚀模数>15000 t/km²·a，剧烈", "SL 190-2007土壤侵蚀分类分级标准", 6),
                createStandard("SE-WE-01", "微度风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "MILD", new BigDecimal("0"), new BigDecimal("200"), "t/km²·a", "侵蚀模数<200 t/km²·a，微度", "SL 190-2007土壤侵蚀分类分级标准", 1),
                createStandard("SE-WE-02", "轻度风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "LIGHT", new BigDecimal("200"), new BigDecimal("2500"), "t/km²·a", "侵蚀模数200-2500 t/km²·a，轻度", "SL 190-2007土壤侵蚀分类分级标准", 2),
                createStandard("SE-WE-03", "中度风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "MODERATE", new BigDecimal("2500"), new BigDecimal("5000"), "t/km²·a", "侵蚀模数2500-5000 t/km²·a，中度", "SL 190-2007土壤侵蚀分类分级标准", 3),
                createStandard("SE-WE-04", "强烈风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "SEVERE", new BigDecimal("5000"), new BigDecimal("8000"), "t/km²·a", "侵蚀模数5000-8000 t/km²·a，强烈", "SL 190-2007土壤侵蚀分类分级标准", 4),
                createStandard("SE-WE-05", "极强烈风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "VERY_SEVERE", new BigDecimal("8000"), new BigDecimal("15000"), "t/km²·a", "侵蚀模数8000-15000 t/km²·a，极强烈", "SL 190-2007土壤侵蚀分类分级标准", 5),
                createStandard("SE-WE-06", "剧烈风力侵蚀", "SOIL_EROSION", "风力侵蚀分级", "SL_190_2007", "EXTREME", new BigDecimal("15000"), null, "t/km²·a", "侵蚀模数>15000 t/km²·a，剧烈", "SL 190-2007土壤侵蚀分类分级标准", 6),
                createStandard("SE-ST-01", "黑土容许流失量", "SOIL_EROSION", "土壤容许流失量", "GB_T_15772", null, null, new BigDecimal("200"), "t/km²·a", "东北黑土区容许流失量200 t/km²·a", "GB/T 15772-2008水土保持综合治理规划通则", 1),
                createStandard("SE-ST-02", "黄土容许流失量", "SOIL_EROSION", "土壤容许流失量", "GB_T_15772", null, null, new BigDecimal("1000"), "t/km²·a", "黄土高原容许流失量1000 t/km²·a", "GB/T 15772-2008水土保持综合治理规划通则", 2),
                createStandard("SE-ST-03", "红壤容许流失量", "SOIL_EROSION", "土壤容许流失量", "GB_T_15772", null, null, new BigDecimal("500"), "t/km²·a", "南方红壤区容许流失量500 t/km²·a", "GB/T 15772-2008水土保持综合治理规划通则", 3),
                createStandard("SE-ST-04", "紫色土容许流失量", "SOIL_EROSION", "土壤容许流失量", "GB_T_15772", null, null, new BigDecimal("500"), "t/km²·a", "紫色土区容许流失量500 t/km²·a", "GB/T 15772-2008水土保持综合治理规划通则", 4),
                createStandard("SE-ST-05", "风沙土容许流失量", "SOIL_EROSION", "土壤容许流失量", "GB_T_15772", null, null, new BigDecimal("200"), "t/km²·a", "风沙土区容许流失量200 t/km²·a", "GB/T 15772-2008水土保持综合治理规划通则", 5),
                createStandard("SE-USLE-R", "降雨侵蚀力因子R", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映降雨对土壤的侵蚀能力，与降雨量、降雨强度相关", "Wischmeier通用土壤流失方程", 1),
                createStandard("SE-USLE-K", "土壤可蚀性因子K", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映土壤被侵蚀的难易程度，与土壤质地、有机质等有关", "Wischmeier通用土壤流失方程", 2),
                createStandard("SE-USLE-L", "坡长因子L", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映坡长对侵蚀的影响，坡长越长侵蚀越大", "Wischmeier通用土壤流失方程", 3),
                createStandard("SE-USLE-S", "坡度因子S", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映坡度对侵蚀的影响，坡度越陡侵蚀越大", "Wischmeier通用土壤流失方程", 4),
                createStandard("SE-USLE-C", "植被覆盖因子C", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映植被覆盖对土壤的保护作用，取值0-1", "Wischmeier通用土壤流失方程", 5),
                createStandard("SE-USLE-P", "水土保持措施因子P", "SOIL_EROSION", "USLE因子", "USLE", null, null, null, "", "反映水土保持措施的减蚀作用，取值0-1", "Wischmeier通用土壤流失方程", 6)
        );
        saveIfNotExists(standards);
    }

    private void initEcoSafetyStandards() {
        List<EcoStandard> standards = Arrays.asList(
                createStandard("ES-CW-01", "气候变暖蓝色预警", "ECO_SAFETY", "气候变暖预警", "BLUE", null, new BigDecimal("26"), new BigDecimal("50"), "分", "综合评分26-50分，蓝色预警，需关注", "系统自定义", 1),
                createStandard("ES-CW-02", "气候变暖黄色预警", "ECO_SAFETY", "气候变暖预警", "YELLOW", null, new BigDecimal("51"), new BigDecimal("65"), "分", "综合评分51-65分，黄色预警，需警惕", "系统自定义", 2),
                createStandard("ES-CW-03", "气候变暖橙色预警", "ECO_SAFETY", "气候变暖预警", "ORANGE", null, new BigDecimal("66"), new BigDecimal("80"), "分", "综合评分66-80分，橙色预警，需防范", "系统自定义", 3),
                createStandard("ES-CW-04", "气候变暖红色预警", "ECO_SAFETY", "气候变暖预警", "RED", null, new BigDecimal("81"), new BigDecimal("100"), "分", "综合评分81-100分，红色预警，紧急应对", "系统自定义", 4),
                createStandard("ES-DS-01", "沙漠化蓝色预警", "ECO_SAFETY", "沙漠化预警", "BLUE", null, new BigDecimal("26"), new BigDecimal("50"), "分", "综合评分26-50分，蓝色预警，需关注", "系统自定义", 1),
                createStandard("ES-DS-02", "沙漠化黄色预警", "ECO_SAFETY", "沙漠化预警", "YELLOW", null, new BigDecimal("51"), new BigDecimal("65"), "分", "综合评分51-65分，黄色预警，需警惕", "系统自定义", 2),
                createStandard("ES-DS-03", "沙漠化橙色预警", "ECO_SAFETY", "沙漠化预警", "ORANGE", null, new BigDecimal("66"), new BigDecimal("80"), "分", "综合评分66-80分，橙色预警，需防范", "系统自定义", 3),
                createStandard("ES-DS-04", "沙漠化红色预警", "ECO_SAFETY", "沙漠化预警", "RED", null, new BigDecimal("81"), new BigDecimal("100"), "分", "综合评分81-100分，红色预警，紧急应对", "系统自定义", 4),
                createStandard("ES-SE-01", "水土流失蓝色预警", "ECO_SAFETY", "水土流失预警", "BLUE", null, new BigDecimal("26"), new BigDecimal("50"), "分", "综合评分26-50分，蓝色预警，需关注", "系统自定义", 1),
                createStandard("ES-SE-02", "水土流失黄色预警", "ECO_SAFETY", "水土流失预警", "YELLOW", null, new BigDecimal("51"), new BigDecimal("65"), "分", "综合评分51-65分，黄色预警，需警惕", "系统自定义", 2),
                createStandard("ES-SE-03", "水土流失橙色预警", "ECO_SAFETY", "水土流失预警", "ORANGE", null, new BigDecimal("66"), new BigDecimal("80"), "分", "综合评分66-80分，橙色预警，需防范", "系统自定义", 3),
                createStandard("ES-SE-04", "水土流失红色预警", "ECO_SAFETY", "水土流失预警", "RED", null, new BigDecimal("81"), new BigDecimal("100"), "分", "综合评分81-100分，红色预警，紧急应对", "系统自定义", 4)
        );
        saveIfNotExists(standards);
    }

    private void initClimateZoneStandards() {
        List<EcoStandard> standards = Arrays.asList(
                createStandard("CZ-CL-01", "热带季风气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在海南岛、雷州半岛、台湾南部，全年高温，分旱雨两季", "中国气候区划", 1),
                createStandard("CZ-CL-02", "亚热带季风气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在秦岭-淮河以南，夏季高温多雨，冬季温和少雨", "中国气候区划", 2),
                createStandard("CZ-CL-03", "暖温带季风气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在华北地区，夏季高温多雨，冬季寒冷干燥", "中国气候区划", 3),
                createStandard("CZ-CL-04", "中温带季风气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在东北地区，夏季温暖短促，冬季寒冷漫长", "中国气候区划", 4),
                createStandard("CZ-CL-05", "寒温带季风气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在大兴安岭北部，全年寒冷，冬季漫长", "中国气候区划", 5),
                createStandard("CZ-CL-06", "高原山地气候", "CLIMATE_ZONE", "中国气候分区", null, null, null, null, "", "主要分布在青藏高原，气候垂直差异显著", "中国气候区划", 6),
                createStandard("CZ-HU-01", "湿润区", "CLIMATE_ZONE", "中国干湿分区", null, null, null, null, "", "年降水量>800mm，主要分布在秦岭-淮河以南", "中国干湿区划", 1),
                createStandard("CZ-HU-02", "半湿润区", "CLIMATE_ZONE", "中国干湿分区", null, null, null, null, "", "年降水量400-800mm，主要分布在东北平原、华北平原", "中国干湿区划", 2),
                createStandard("CZ-HU-03", "半干旱区", "CLIMATE_ZONE", "中国干湿分区", null, null, null, null, "", "年降水量200-400mm，主要分布在内蒙古高原、黄土高原", "中国干湿区划", 3),
                createStandard("CZ-HU-04", "干旱区", "CLIMATE_ZONE", "中国干湿分区", null, null, null, null, "", "年降水量<200mm，主要分布在新疆、内蒙古西部", "中国干湿区划", 4),
                createStandard("CZ-VE-01", "寒温带针叶林区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在大兴安岭北部，以兴安落叶松、樟子松为主", "中国植被区划", 1),
                createStandard("CZ-VE-02", "温带针阔混交林区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在小兴安岭、长白山，以红松、水曲柳为主", "中国植被区划", 2),
                createStandard("CZ-VE-03", "暖温带落叶阔叶林区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在华北地区，以杨树、槐树、栎树为主", "中国植被区划", 3),
                createStandard("CZ-VE-04", "亚热带常绿阔叶林区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在长江流域，以樟树、楠竹、茶树为主", "中国植被区划", 4),
                createStandard("CZ-VE-05", "热带季雨林区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在海南岛、西双版纳，以榕树、望天树为主", "中国植被区划", 5),
                createStandard("CZ-VE-06", "温带草原区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在内蒙古高原，以针茅、羊草为主", "中国植被区划", 6),
                createStandard("CZ-VE-07", "温带荒漠区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在新疆、甘肃，以梭梭、红柳、胡杨为主", "中国植被区划", 7),
                createStandard("CZ-VE-08", "青藏高原高山植被区", "CLIMATE_ZONE", "中国植被分区", null, null, null, null, "", "主要分布在青藏高原，以高寒草甸、垫状植物为主", "中国植被区划", 8),
                createStandard("CZ-ER-01", "水力侵蚀为主区", "CLIMATE_ZONE", "土壤侵蚀类型分区", null, null, null, null, "", "主要分布在东部季风区，以降雨侵蚀为主", "中国土壤侵蚀区划", 1),
                createStandard("CZ-ER-02", "风力侵蚀为主区", "CLIMATE_ZONE", "土壤侵蚀类型分区", null, null, null, null, "", "主要分布在西北干旱区，以风蚀沙化为主", "中国土壤侵蚀区划", 2),
                createStandard("CZ-ER-03", "冻融侵蚀为主区", "CLIMATE_ZONE", "土壤侵蚀类型分区", null, null, null, null, "", "主要分布在青藏高原、高山区，以冻融作用为主", "中国土壤侵蚀区划", 3)
        );
        saveIfNotExists(standards);
    }

    private void saveIfNotExists(List<EcoStandard> standards) {
        int addedCount = 0;
        for (EcoStandard standard : standards) {
            if (!ecoStandardService.existsByCode(standard.getStandardCode())) {
                ecoStandardService.add(standard);
                addedCount++;
            }
        }
        if (addedCount > 0) {
            log.info("[生态标准] 新增 {} 条标准数据", addedCount);
        }
    }

    private EcoStandard createStandard(String code, String name, String category, String subcategory,
                                       String system, String gradeLevel, BigDecimal min, BigDecimal max,
                                       String unit, String description, String reference, int sort) {
        EcoStandard standard = new EcoStandard();
        standard.setStandardCode(code);
        standard.setStandardName(name);
        standard.setCategory(category);
        standard.setSubcategory(subcategory);
        standard.setStandardSystem(system);
        standard.setGradeLevel(gradeLevel);
        standard.setThresholdMin(min);
        standard.setThresholdMax(max);
        standard.setUnit(unit);
        standard.setDescription(description);
        standard.setReferenceStandard(reference);
        standard.setSortOrder(sort);
        standard.setStatus("ACTIVE");
        standard.setIsDeleted(0);
        return standard;
    }
}
