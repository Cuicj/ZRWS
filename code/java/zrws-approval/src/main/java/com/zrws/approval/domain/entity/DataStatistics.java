package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据统计实体
 * <p>记录数据导入、审批等操作的统计信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_data_statistics")
public class DataStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计ID */
    @TableId(type = IdType.AUTO)
    private Long statsId;

    /** 统计日期 */
    private String statsDate;

    /** BO编码 */
    private String boCode;

    /** BO名称 */
    private String boName;

    /** 操作类型 */
    private String operationType;

    /** 总数量 */
    private Integer totalCount;

    /** 成功数量 */
    private Integer successCount;

    /** 失败数量 */
    private Integer failedCount;

    /** 审批通过数量 */
    private Integer approvedCount;

    /** 审批驳回数量 */
    private Integer rejectedCount;

    /** 待审批数量 */
    private Integer pendingCount;

    /** 导入文件数 */
    private Integer fileCount;

    /** 总记录数 */
    private Integer totalRecords;

    /** 数据质量评分 */
    private Integer qualityScore;

    /** 平均处理时间(秒) */
    private Double avgProcessTime;

    /** 统计周期: DAILY(日)/WEEKLY(周)/MONTHLY(月) */
    private String periodType;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 统计周期枚举
     */
    public enum PeriodType {
        DAILY("日统计"),
        WEEKLY("周统计"),
        MONTHLY("月统计");

        private final String desc;

        PeriodType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}