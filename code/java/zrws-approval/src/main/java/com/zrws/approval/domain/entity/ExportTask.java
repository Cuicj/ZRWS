package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据导出任务实体
 * <p>记录每次数据导出的基本信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_export_task")
public class ExportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 任务名称 */
    private String taskName;

    /** BO编码 */
    private String boCode;

    /** 导出类型 */
    private String exportType;

    /** 文件格式: EXCEL/PDF/CSV */
    private String fileFormat;

    /** 过滤条件JSON */
    private String filterConditions;

    /** 导出字段列表JSON */
    private String fieldList;

    /** 文件存储路径 */
    private String filePath;

    /** 文件名 */
    private String fileName;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 总行数 */
    private Integer totalRows;

    /** 状态: PENDING/PROCESSING/SUCCESS/FAILED */
    private String status;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 错误信息 */
    private String errorMessage;

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
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }
}
