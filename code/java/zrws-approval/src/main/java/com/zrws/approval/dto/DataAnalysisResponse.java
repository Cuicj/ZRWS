package com.zrws.approval.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据分析响应DTO
 */
@Data
public class DataAnalysisResponse {

    /** 是否成功 */
    private Boolean success;

    /** 批次号 */
    private String batchNo;

    /** 批次ID */
    private Long batchId;

    /** 总行数 */
    private Integer totalRows;

    /** 成功行数 */
    private Integer successRows;

    /** 失败行数 */
    private Integer failedRows;

    /** 字段映射结果 */
    private List<FieldMapping> fieldMappings;

    /** 校验结果汇总 */
    private ValidationSummary validationSummary;

    /** AI分析结果 */
    private AiAnalysisResult aiAnalysis;

    /** 错误信息（如果有） */
    private String errorMessage;

    /** 数据预览（前10行） */
    private List<Map<String, Object>> dataPreview;

    /**
     * 字段映射信息
     */
    @Data
    public static class FieldMapping {
        /** 源字段名 */
        private String sourceField;
        /** 目标字段名 */
        private String targetField;
        /** 映射置信度 */
        private Double confidence;
        /** 映射说明 */
        private String description;
    }

    /**
     * 校验结果汇总
     */
    @Data
    public static class ValidationSummary {
        /** 总校验项数 */
        private Integer totalValidations;
        /** 通过数 */
        private Integer passedValidations;
        /** 失败数 */
        private Integer failedValidations;
        /** 警告数 */
        private Integer warningValidations;
        /** 主要错误类型统计 */
        private Map<String, Integer> errorTypeStats;
    }

    /**
     * AI分析结果
     */
    @Data
    public static class AiAnalysisResult {
        /** 数据质量评分(0-100) */
        private Integer qualityScore;
        /** 数据分布分析 */
        private Map<String, Object> dataDistribution;
        /** 异常值列表 */
        private List<AnomalyItem> anomalies;
        /** 建议列表 */
        private List<String> suggestions;
        /** 问题汇总 */
        private List<ProblemItem> problems;
    }

    /**
     * 异常项
     */
    @Data
    public static class AnomalyItem {
        /** 行号 */
        private Integer rowNumber;
        /** 字段名 */
        private String fieldName;
        /** 异常值 */
        private String value;
        /** 异常类型 */
        private String anomalyType;
        /** 描述 */
        private String description;
    }

    /**
     * 问题项
     */
    @Data
    public static class ProblemItem {
        /** 问题类型 */
        private String type;
        /** 问题描述 */
        private String description;
        /** 影响行数 */
        private Integer affectedRows;
        /** 严重程度: LOW/MEDIUM/HIGH */
        private String severity;
    }
}