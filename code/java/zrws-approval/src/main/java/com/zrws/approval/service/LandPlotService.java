package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.LandPlot;
import com.zrws.approval.mapper.LandPlotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LandPlotService {

    @Autowired
    private LandPlotMapper landPlotMapper;

    public Page<LandPlot> getPage(int pageNum, int pageSize, String region, String landType, String status) {
        LambdaQueryWrapper<LandPlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LandPlot::getIsDeleted, 0);
        if (StringUtils.hasText(region)) {
            wrapper.like(LandPlot::getRegion, region);
        }
        if (StringUtils.hasText(landType)) {
            wrapper.eq(LandPlot::getLandType, landType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(LandPlot::getStatus, status);
        }
        wrapper.orderByDesc(LandPlot::getCreatedTime);
        return landPlotMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public LandPlot getById(Long id) {
        return landPlotMapper.selectById(id);
    }

    public Map<String, Object> getAreaStats() {
        Map<String, Object> stats = new HashMap<>();

        List<LandPlot> allPlots = landPlotMapper.selectList(
                new LambdaQueryWrapper<LandPlot>()
                        .eq(LandPlot::getIsDeleted, 0)
        );

        long totalPlots = allPlots.size();
        stats.put("totalPlots", totalPlots);

        double totalGpsArea = allPlots.stream()
                .map(LandPlot::getGpsArea)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        stats.put("totalGpsArea", totalGpsArea);

        double totalRegisteredArea = allPlots.stream()
                .map(LandPlot::getRegisteredArea)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        stats.put("totalRegisteredArea", totalRegisteredArea);

        double avgAreaDiff = allPlots.stream()
                .map(LandPlot::getAreaDiff)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        stats.put("avgAreaDiff", avgAreaDiff);

        long normalCount = allPlots.stream()
                .filter(p -> "NORMAL".equals(p.getStatus()))
                .count();
        stats.put("normalCount", normalCount);

        long abnormalCount = allPlots.stream()
                .filter(p -> "ABNORMAL".equals(p.getStatus()))
                .count();
        stats.put("abnormalCount", abnormalCount);

        long reviewCount = allPlots.stream()
                .filter(p -> "REVIEW".equals(p.getStatus()))
                .count();
        stats.put("reviewCount", reviewCount);

        Map<String, Double> areaByType = new HashMap<>();
        allPlots.stream()
                .filter(p -> StringUtils.hasText(p.getLandType()))
                .collect(Collectors.groupingBy(LandPlot::getLandType))
                .forEach((type, plots) -> {
                    double typeArea = plots.stream()
                            .map(LandPlot::getGpsArea)
                            .filter(a -> a != null)
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    areaByType.put(type, typeArea);
                });
        stats.put("areaByType", areaByType);

        Map<String, Long> countByRegion = new HashMap<>();
        allPlots.stream()
                .filter(p -> StringUtils.hasText(p.getRegion()))
                .collect(Collectors.groupingBy(LandPlot::getRegion))
                .forEach((region, plots) -> {
                    countByRegion.put(region, Long.valueOf(plots.size()));
                });
        stats.put("countByRegion", countByRegion);

        return stats;
    }

    public void add(LandPlot landPlot) {
        if (landPlot.getGpsArea() != null && landPlot.getRegisteredArea() != null) {
            landPlot.setAreaDiff(landPlot.getGpsArea() - landPlot.getRegisteredArea());
            double diffRatio = Math.abs(landPlot.getAreaDiff()) / landPlot.getRegisteredArea() * 100;
            if (diffRatio > 10) {
                landPlot.setStatus("ABNORMAL");
            } else if (diffRatio > 5) {
                landPlot.setStatus("REVIEW");
            } else {
                landPlot.setStatus("NORMAL");
            }
        }
        landPlotMapper.insert(landPlot);
    }

    public void update(LandPlot landPlot) {
        if (landPlot.getGpsArea() != null && landPlot.getRegisteredArea() != null) {
            landPlot.setAreaDiff(landPlot.getGpsArea() - landPlot.getRegisteredArea());
            double diffRatio = Math.abs(landPlot.getAreaDiff()) / landPlot.getRegisteredArea() * 100;
            if (diffRatio > 10) {
                landPlot.setStatus("ABNORMAL");
            } else if (diffRatio > 5) {
                landPlot.setStatus("REVIEW");
            } else {
                landPlot.setStatus("NORMAL");
            }
        }
        landPlotMapper.updateById(landPlot);
    }

    public void delete(Long id) {
        landPlotMapper.deleteById(id);
    }
}