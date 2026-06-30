package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.Desertification;
import com.zrws.approval.mapper.DesertificationMapper;
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
public class DesertificationService {

    @Autowired
    private DesertificationMapper desertificationMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Page<Desertification> getPage(int pageNum, int pageSize, String region, String grade, String riskLevel, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Desertification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Desertification::getIsDeleted, 0);
        if (StringUtils.hasText(region)) {
            wrapper.like(Desertification::getRegion, region);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(Desertification::getDesertificationGrade, grade);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(Desertification::getRiskLevel, riskLevel);
        }
        if (startDate != null) {
            wrapper.ge(Desertification::getMonitorDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Desertification::getMonitorDate, endDate);
        }
        wrapper.orderByDesc(Desertification::getMonitorDate);
        return desertificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Desertification getById(Long id) {
        return desertificationMapper.selectById(id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Desertification> latest = desertificationMapper.selectList(
                new LambdaQueryWrapper<Desertification>()
                        .eq(Desertification::getIsDeleted, 0)
                        .orderByDesc(Desertification::getMonitorDate)
                        .last("LIMIT 1")
        );

        BigDecimal totalArea = BigDecimal.ZERO;
        BigDecimal avgVegetation = BigDecimal.ZERO;
        BigDecimal desertRatio = BigDecimal.ZERO;

        if (!latest.isEmpty()) {
            Desertification latestRecord = latest.get(0);
            totalArea = latestRecord.getDesertificationArea() != null ? latestRecord.getDesertificationArea() : BigDecimal.ZERO;
            avgVegetation = latestRecord.getVegetationCoverage() != null ? latestRecord.getVegetationCoverage() : BigDecimal.ZERO;
            desertRatio = latestRecord.getDesertificationRatio() != null ? latestRecord.getDesertificationRatio() : BigDecimal.ZERO;
        }

        stats.put("totalArea", totalArea);
        stats.put("avgVegetationCoverage", avgVegetation);
        stats.put("desertificationRatio", desertRatio);

        long highRiskCount = desertificationMapper.selectCount(
                new LambdaQueryWrapper<Desertification>()
                        .eq(Desertification::getIsDeleted, 0)
                        .in(Desertification::getRiskLevel, Arrays.asList("HIGH", "EXTREME"))
        );
        stats.put("highRiskCount", highRiskCount);

        long totalRecords = desertificationMapper.selectCount(
                new LambdaQueryWrapper<Desertification>()
                        .eq(Desertification::getIsDeleted, 0)
        );
        stats.put("totalRecords", totalRecords);

        return stats;
    }

    public Map<String, Object> getTrendData(String region) {
        Map<String, Object> result = new HashMap<>();

        List<Desertification> list;
        if (StringUtils.hasText(region)) {
            list = desertificationMapper.selectTrendByRegion(region);
        } else {
            list = desertificationMapper.selectList(
                    new LambdaQueryWrapper<Desertification>()
                            .eq(Desertification::getIsDeleted, 0)
                            .orderByDesc(Desertification::getMonitorDate)
                            .last("LIMIT 12")
            );
        }
        Collections.reverse(list);

        List<String> months = new ArrayList<>();
        List<BigDecimal> vegetationCoverages = new ArrayList<>();
        List<BigDecimal> bareLandRatios = new ArrayList<>();
        List<BigDecimal> desertificationAreas = new ArrayList<>();

        for (Desertification record : list) {
            months.add(record.getMonitorDate().getMonthValue() + "月");
            vegetationCoverages.add(record.getVegetationCoverage());
            bareLandRatios.add(record.getBareLandRatio());
            desertificationAreas.add(record.getDesertificationArea());
        }

        result.put("months", months);
        result.put("vegetationCoverages", vegetationCoverages);
        result.put("bareLandRatios", bareLandRatios);
        result.put("desertificationAreas", desertificationAreas);

        return result;
    }

    public List<Map<String, Object>> getGradeDistribution() {
        return desertificationMapper.countByGrade();
    }

    public List<Map<String, Object>> getRiskDistribution() {
        return desertificationMapper.countByRiskLevel();
    }

    public String generateRecordCode() {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long count = desertificationMapper.selectCount(
                new LambdaQueryWrapper<Desertification>()
                        .likeRight(Desertification::getRecordCode, "DS" + dateStr)
        );
        return String.format("DS%s%04d", dateStr, count + 1);
    }

    public void add(Desertification desertification) {
        if (!StringUtils.hasText(desertification.getRecordCode())) {
            desertification.setRecordCode(generateRecordCode());
        }
        desertificationMapper.insert(desertification);
    }

    public void update(Desertification desertification) {
        desertificationMapper.updateById(desertification);
    }

    public void delete(Long id) {
        desertificationMapper.deleteById(id);
    }

    public Desertification assessRisk(Long id) {
        Desertification record = desertificationMapper.selectById(id);
        if (record == null) {
            return null;
        }

        BigDecimal score = calculateRiskScore(record);
        record.setRiskScore(score);
        record.setRiskLevel(determineRiskLevel(score));
        record.setAnalysisTime(LocalDateTime.now());
        record.setAnalyst("系统自动评估");

        desertificationMapper.updateById(record);
        return record;
    }

    private BigDecimal calculateRiskScore(Desertification record) {
        double score = 0;

        BigDecimal vegetation = record.getVegetationCoverage();
        if (vegetation != null) {
            double vegScore = Math.min((100 - vegetation.doubleValue()) * 0.4, 30);
            score += vegScore;
        }

        String grade = record.getDesertificationGrade();
        if (grade != null) {
            Map<String, Double> gradeScores = new HashMap<>();
            gradeScores.put("MILD", 10.0);
            gradeScores.put("MODERATE", 25.0);
            gradeScores.put("SEVERE", 40.0);
            gradeScores.put("EXTREME", 50.0);
            score += gradeScores.getOrDefault(grade, 0.0);
        }

        BigDecimal organicMatter = record.getSoilOrganicMatter();
        if (organicMatter != null) {
            double organicScore = Math.min(Math.max(5 - organicMatter.doubleValue(), 0) * 4, 15);
            score += organicScore;
        }

        BigDecimal aridity = record.getAridityIndex();
        if (aridity != null) {
            double aridityScore = Math.min(Math.max(0.65 - aridity.doubleValue(), 0) * 30, 15);
            score += aridityScore;
        }

        BigDecimal windErosion = record.getWindErosionModulus();
        if (windErosion != null) {
            double windScore = Math.min(windErosion.doubleValue() / 300.0, 15);
            score += windScore;
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
