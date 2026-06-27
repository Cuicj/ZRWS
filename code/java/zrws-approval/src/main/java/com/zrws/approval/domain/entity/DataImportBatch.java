package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据导入批次实体
 * <p>记录每次数据导入的基本信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_data_import_batch")
public class DataImportBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批次ID */
    @TableId(type = IdType.AUTO)
    private Long batchId;

    /** 批次号 */
    private String batchNo;

    /** BO定义ID */
    private Long boId;

    /** BO编码 */
    private String boCode;

    /** 原始文件名 */
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 文件MD5哈希 */
    private String fileHash;

    /** 总行数 */
    private Integer totalRows;

    /** 成功行数 */
    private Integer successRows;

    /** 失败行数 */
    private Integer failedRows;

    /** 跳过行数 */
    private Integer skippedRows;

    /** 导入模式: INSERT/UPDATE/INSERT_UPDATE/REPLACE */
    private String importMode;

    /** 状态: PENDING/PROCESSING/SUCCESS/FAILED/PARTIAL */
    private String status;

    /** 错误日志JSON */
    private String errorLog;

    /** AI分析结果JSON */
    private String aiAnalysis;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private Long tenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 计算进度百分比
     */
    public int getProgressPercent() {
        if (totalRows == null || totalRows == 0) {
            return 0;
        }
        int processed = (successRows != null ? successRows : 0) +
                       (failedRows != null ? failedRows : 0) +
                       (skippedRows != null ? skippedRows : 0);
        return (int) ((processed * 100.0) / totalRows);
    }

    /**
     * 批次状态枚举
     */
    public enum BatchStatus {
        PENDING,      // 待处理
        PROCESSING,   // 处理中
        SUCCESS,      // 全部成功
        FAILED,       // 全部失败
        PARTIAL      // 部分成功
    }
}