package com.zrws.approval.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据导入响应DTO
 */
@Data
public class DataImportResponse {

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

    /** 跳过行数 */
    private Integer skippedRows;

    /** 导入耗时(秒) */
    private Double duration;

    /** 错误信息 */
    private String errorMessage;

    /** 字段映射 */
    private List<?> fieldMappings;

    /** 字段映射详情 */
    private List<FieldMapping> fieldMapping;

    /** 数据预览 */
    private List<Map<String, Object>> dataPreview;

    /** AI分析结果 */
    private Object aiAnalysis;

    /** 验证摘要 */
    private Object validationSummary;

    /** 导入详情 */
    private List<ImportDetail> details;

    /** 错误报告 */
    private ErrorReport errorReport;

    /**
     * 导入明细
     */
    @Data
    public static class ImportDetail {
        /** 行号 */
        private Integer rowNumber;
        /** 状态 */
        private String status;
        /** 目标记录ID */
        private Long targetId;
        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 错误报告
     */
    @Data
    public static class ErrorReport {
        /** 总错误数 */
        private Integer totalErrors;
        /** 按类型统计 */
        private Map<String, Integer> errorTypeStats;
        /** 错误行详情 */
        private List<ErrorRow> errorRows;
    }

    /**
     * 字段映射
     */
    @Data
    public static class FieldMapping {
        /** Excel列名 */
        private String excelColumn;
        /** 目标字段名 */
        private String targetField;
        /** 映射类型 */
        private String mappingType;
        /** 置信度 */
        private Double confidence;
    }

    /**
     * 错误行
     */
    @Data
    public static class ErrorRow {
        /** 行号 */
        private Integer rowNumber;
        /** 错误字段 */
        private String fieldName;
        /** 错误值 */
        private String errorValue;
        /** 错误信息 */
        private String errorMessage;
        /** 错误类型 */
        private String errorType;
    }
}