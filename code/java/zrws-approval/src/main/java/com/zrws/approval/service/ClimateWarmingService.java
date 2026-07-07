package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.ClimateWarming;
import com.zrws.approval.mapper.ClimateWarmingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ClimateWarmingService {

    @Autowired
    private ClimateWarmingMapper climateWarmingMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Page<ClimateWarming> getPage(int pageNum, int pageSize, String region, String riskLevel, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<ClimateWarming> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClimateWarming::getIsDeleted, 0);
        if (StringUtils.hasText(region)) {
            wrapper.like(ClimateWarming::getRegion, region);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(ClimateWarming::getRiskLevel, riskLevel);
        }
        if (startDate != null) {
            wrapper.ge(ClimateWarming::getMonitorDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(ClimateWarming::getMonitorDate, endDate);
        }
        wrapper.orderByDesc(ClimateWarming::getMonitorDate);
        return climateWarmingMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public ClimateWarming getById(Long id) {
        return climateWarmingMapper.selectById(id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        List<ClimateWarming> latest = climateWarmingMapper.selectList(
                new LambdaQueryWrapper<ClimateWarming>()
                        .eq(ClimateWarming::getIsDeleted, 0)
                        .orderByDesc(ClimateWarming::getMonitorDate)
                        .last("LIMIT 1")
        );

        if (!latest.isEmpty()) {
            ClimateWarming latestRecord = latest.get(0);
            stats.put("avgTempAnomaly", latestRecord.getTemperatureAnomaly());
            stats.put("warmingRate", latestRecord.getWarmingRate10y());
        } else {
            stats.put("avgTempAnomaly", BigDecimal.ZERO);
            stats.put("warmingRate", BigDecimal.ZERO);
        }

        long highTempDays = climateWarmingMapper.selectCount(
                new LambdaQueryWrapper<ClimateWarming>()
                        .eq(ClimateWarming::getIsDeleted, 0)
                        .ge(ClimateWarming::getExtremeHighTempDays, 1)
        );
        stats.put("highTempDays", highTempDays);

        long highRiskCount = climateWarmingMapper.selectCount(
                new LambdaQueryWrapper<ClimateWarming>()
                        .eq(ClimateWarming::getIsDeleted, 0)
                        .in(ClimateWarming::getRiskLevel, Arrays.asList("HIGH", "EXTREME"))
        );
        stats.put("highRiskCount", highRiskCount);

        long totalRecords = climateWarmingMapper.selectCount(
                new LambdaQueryWrapper<ClimateWarming>()
                        .eq(ClimateWarming::getIsDeleted, 0)
        );
        stats.put("totalRecords", totalRecords);

        return stats;
    }

    public Map<String, Object> getTrendData(String region) {
        Map<String, Object> result = new HashMap<>();

        List<ClimateWarming> list;
        if (StringUtils.hasText(region)) {
            list = climateWarmingMapper.selectTrendByRegion(region);
        } else {
            list = climateWarmingMapper.selectList(
                    new LambdaQueryWrapper<ClimateWarming>()
                            .eq(ClimateWarming::getIsDeleted, 0)
                            .ge(ClimateWarming::getMonitorDate, LocalDate.now().minusMonths(12))
                            .orderByAsc(ClimateWarming::getMonitorDate)
            );
        }

        // 按月分组并取平均值
        LinkedHashMap<String, List<ClimateWarming>> byMonth = new LinkedHashMap<>();
        for (ClimateWarming record : list) {
            String monthKey = record.getMonitorDate().getYear() + "-" +
                    String.format("%02d", record.getMonitorDate().getMonthValue());
            byMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(record);
        }

        List<String> months = new ArrayList<>();
        List<BigDecimal> avgTemperatures = new ArrayList<>();
        List<BigDecimal> tempAnomalies = new ArrayList<>();
        List<BigDecimal> precipitations = new ArrayList<>();

        for (Map.Entry<String, List<ClimateWarming>> entry : byMonth.entrySet()) {
            List<ClimateWarming> records = entry.getValue();
            months.add(entry.getKey().split("-")[1] + "月");

            BigDecimal tempSum = BigDecimal.ZERO, anomalySum = BigDecimal.ZERO, precipSum = BigDecimal.ZERO;
            for (ClimateWarming r : records) {
                tempSum = tempSum.add(r.getAvgTemperature() != null ? r.getAvgTemperature() : BigDecimal.ZERO);
                anomalySum = anomalySum.add(r.getTemperatureAnomaly() != null ? r.getTemperatureAnomaly() : BigDecimal.ZERO);
                precipSum = precipSum.add(r.getPrecipitation() != null ? r.getPrecipitation() : BigDecimal.ZERO);
            }
            int count = records.size();
            avgTemperatures.add(tempSum.divide(BigDecimal.valueOf(count), 1, BigDecimal.ROUND_HALF_UP));
            tempAnomalies.add(anomalySum.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP));
            precipitations.add(precipSum.divide(BigDecimal.valueOf(count), 1, BigDecimal.ROUND_HALF_UP));
        }

        result.put("months", months);
        result.put("avgTemperatures", avgTemperatures);
        result.put("tempAnomalies", tempAnomalies);
        result.put("precipitations", precipitations);

        return result;
    }

    public List<Map<String, Object>> getRiskDistribution() {
        return climateWarmingMapper.countByRiskLevel();
    }

    public String generateRecordCode() {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long count = climateWarmingMapper.selectCount(
                new LambdaQueryWrapper<ClimateWarming>()
                        .likeRight(ClimateWarming::getRecordCode, "CW" + dateStr)
        );
        return String.format("CW%s%04d", dateStr, count + 1);
    }

    public void add(ClimateWarming climateWarming) {
        if (!StringUtils.hasText(climateWarming.getRecordCode())) {
            climateWarming.setRecordCode(generateRecordCode());
        }
        climateWarmingMapper.insert(climateWarming);
    }

    public void update(ClimateWarming climateWarming) {
        climateWarmingMapper.updateById(climateWarming);
    }

    public void delete(Long id) {
        climateWarmingMapper.deleteById(id);
    }

    public ClimateWarming assessRisk(Long id) {
        ClimateWarming record = climateWarmingMapper.selectById(id);
        if (record == null) {
            return null;
        }

        BigDecimal score = calculateRiskScore(record);
        record.setRiskScore(score);
        record.setRiskLevel(determineRiskLevel(score));
        record.setAnalysisTime(LocalDateTime.now());
        record.setAnalyst("系统自动评估");

        climateWarmingMapper.updateById(record);
        return record;
    }

    private BigDecimal calculateRiskScore(ClimateWarming record) {
        double score = 0;

        BigDecimal anomaly = record.getTemperatureAnomaly();
        if (anomaly != null) {
            double anomalyScore = Math.min(Math.abs(anomaly.doubleValue()) * 25, 35);
            score += anomalyScore;
        }

        Integer highDays = record.getExtremeHighTempDays();
        if (highDays != null) {
            double highDaysScore = Math.min(highDays.doubleValue() * 1.5, 25);
            score += highDaysScore;
        }

        BigDecimal rate = record.getWarmingRate10y();
        if (rate != null) {
            double rateScore = Math.min(rate.doubleValue() * 50, 20);
            score += rateScore;
        }

        Integer droughtDays = record.getDroughtDays();
        if (droughtDays != null) {
            double droughtScore = Math.min(droughtDays.doubleValue() * 0.5, 10);
            score += droughtScore;
        }

        BigDecimal precipAnomaly = record.getPrecipitationAnomaly();
        if (precipAnomaly != null) {
            double precipScore = Math.min(Math.abs(precipAnomaly.doubleValue()) * 0.2, 10);
            score += precipScore;
        }

        return BigDecimal.valueOf(Math.min(Math.max(score, 0), 100));
    }

    private String determineRiskLevel(BigDecimal score) {
        double s = score.doubleValue();
        if (s <= 25) return "LOW";
        if (s <= 50) return "MEDIUM";
        if (s <= 75) return "HIGH";
        return "EXTREME";
    }
}
