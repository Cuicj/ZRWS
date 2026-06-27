package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据导入明细实体
 * <p>记录每行数据的导入状态和校验结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_data_import_detail")
public class DataImportDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @TableId(type = IdType.AUTO)
    private Long detailId;

    /** 批次ID */
    private Long batchId;

    /** 源文件行号 */
    private Integer rowNumber;

    /** 源数据JSON */
    private String sourceData;

    /** 目标数据JSON(转换后) */
    private String targetData;

    /** 字段映射JSON */
    private String fieldMapping;

    /** 校验结果JSON */
    private String validationResult;

    /** AI建议JSON */
    private String aiSuggestion;

    /** 状态: PENDING/VALID/PROCESSING/SUCCESS/WARNING/SKIPPED/FAILED */
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 目标表主键ID */
    private Long targetId;

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
     * 明细状态枚举
     */
    public enum DetailStatus {
        PENDING,     // 待处理
        VALID,       // 已校验通过
        PROCESSING,  // 处理中
        SUCCESS,     // 成功
        WARNING,     // 警告(有建议但已处理)
        SKIPPED,     // 跳过
        FAILED       // 失败
    }
}