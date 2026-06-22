package com.zrws.approval.service;

import cn.hutool.core.date.DateUtil;
import com.zrws.approval.domain.entity.DataStatistics;
import com.zrws.approval.domain.entity.DataImportBatch;
import com.zrws.approval.mapper.DataStatisticsMapper;
import com.zrws.approval.mapper.DataImportBatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务
 */
@Slf4j
@Service
public class DataStatisticsService {

    @Autowired
    private DataStatisticsMapper statisticsMapper;

    @Autowired
    private DataImportBatchMapper batchMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取今日统计
     */
    public DataStatistics getTodayStats(String boCode) {
        String today = LocalDate.now().format(DATE_FORMAT);
        return statisticsMapper.selectByDate(today, boCode);
    }

    /**
     * 获取日期范围统计
     */
    public List<DataStatistics> getStatsByDateRange(String startDate, String endDate) {
        return statisticsMapper.selectByDateRange(startDate, endDate);
    }

    /**
     * 获取BO统计
     */
    public List<DataStatistics> getStatsByBoCode(String boCode) {
        return statisticsMapper.selectByBoCode(boCode);
    }

    /**
     * 获取汇总统计
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 按BO分组统计
        List<DataStatistics> boStats = statisticsMapper.selectSummaryByBo();
        summary.put("byBo", boStats);

        // 今日统计
        String today = LocalDate.now().format(DATE_FORMAT);
        List<DataStatistics> todayStats = statisticsMapper.selectByDateRange(today, today);
        summary.put("today", todayStats);

        // 本周统计
        String weekStart = LocalDate.now().minusDays(7).format(DATE_FORMAT);
        List<DataStatistics> weekStats = statisticsMapper.selectByDateRange(weekStart, today);
        summary.put("week", weekStats);

        // 本月统计
        String monthStart = LocalDate.now().withDayOfMonth(1).format(DATE_FORMAT);
        List<DataStatistics> monthStats = statisticsMapper.selectByDateRange(monthStart, today);
        summary.put("month", monthStats);

        return summary;
    }

    /**
     * 获取导入批次统计
     */
    public Map<String, Object> getImportStats() {
        Map<String, Object> stats = new HashMap<>();

        List<DataImportBatch> batches = batchMapper.selectList(null);

        int total = batches.size();
        int success = 0;
        int failed = 0;
        int partial = 0;
        int totalRows = 0;
        int successRows = 0;
        int failedRows = 0;

        for (DataImportBatch batch : batches) {
            switch (batch.getStatus()) {
                case "SUCCESS":
                    success++;
                    break;
                case "FAILED":
                    failed++;
                    break;
                case "PARTIAL":
                    partial++;
                    break;
            }

            if (batch.getTotalRows() != null) totalRows += batch.getTotalRows();
            if (batch.getSuccessRows() != null) successRows += batch.getSuccessRows();
            if (batch.getFailedRows() != null) failedRows += batch.getFailedRows();
        }

        stats.put("total", total);
        stats.put("success", success);
        stats.put("failed", failed);
        stats.put("partial", partial);
        stats.put("totalRows", totalRows);
        stats.put("successRows", successRows);
        stats.put("failedRows", failedRows);
        stats.put("successRate", total > 0 ? (success * 100.0 / total) : 0);
        stats.put("dataSuccessRate", totalRows > 0 ? (successRows * 100.0 / totalRows) : 0);

        return stats;
    }

    /**
     * 记录导入统计
     */
    public void recordImportStats(DataImportBatch batch) {
        String today = LocalDate.now().format(DATE_FORMAT);

        // 查找今日统计记录
        DataStatistics stats = statisticsMapper.selectByDate(today, batch.getBoCode());
        if (stats == null) {
            stats = new DataStatistics();
            stats.setStatsDate(today);
            stats.setBoCode(batch.getBoCode());
            stats.setBoName(getBoName(batch.getBoCode()));
            stats.setPeriodType("DAILY");
        }

        // 更新统计数据
        stats.setTotalCount(stats.getTotalCount() != null ? stats.getTotalCount() + 1 : 1);
        stats.setFileCount(stats.getFileCount() != null ? stats.getFileCount() + 1 : 1);

        if ("SUCCESS".equals(batch.getStatus())) {
            stats.setSuccessCount(stats.getSuccessCount() != null ? stats.getSuccessCount() + 1 : 1);
        } else if ("FAILED".equals(batch.getStatus())) {
            stats.setFailedCount(stats.getFailedCount() != null ? stats.getFailedCount() + 1 : 1);
        } else if ("PARTIAL".equals(batch.getStatus())) {
            stats.setSuccessCount(stats.getSuccessCount() != null ? stats.getSuccessCount() + 1 : 1);
            stats.setFailedCount(stats.getFailedCount() != null ? stats.getFailedCount() + 1 : 1);
        }

        if (batch.getTotalRows() != null) {
            stats.setTotalRecords(stats.getTotalRecords() != null ?
                    stats.getTotalRecords() + batch.getTotalRows() : batch.getTotalRows());
        }
        if (batch.getSuccessRows() != null) {
            stats.setApprovedCount(stats.getApprovedCount() != null ?
                    stats.getApprovedCount() + batch.getSuccessRows() : batch.getSuccessRows());
        }

        // 计算数据质量评分
        if (batch.getTotalRows() != null && batch.getTotalRows() > 0) {
            int quality = (int) ((batch.getSuccessRows() * 100.0) / batch.getTotalRows());
            if (stats.getQualityScore() == null) {
                stats.setQualityScore(quality);
            } else {
                stats.setQualityScore((stats.getQualityScore() + quality) / 2);
            }
        }

        if (stats.getStatsId() != null) {
            statisticsMapper.updateById(stats);
        } else {
            statisticsMapper.insert(stats);
        }
    }

    /**
     * 获取BO名称（简化实现）
     */
    private String getBoName(String boCode) {
        Map<String, String> boNames = new HashMap<>();
        boNames.put("SOIL_SAMPLE", "土壤采样数据");
        boNames.put("WEATHER_DATA", "气象数据");
        boNames.put("PLOT_INFO", "地块信息");
        boNames.put("FLIGHT_MISSION", "飞行任务");
        boNames.put("DISASTER_RISK", "灾害风险");
        return boNames.getOrDefault(boCode, boCode);
    }
}