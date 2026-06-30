package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程草稿实体
 * 持久化流程设计器的草稿数据
 */
@Data
@TableName("zrws_bpmn_draft")
public class BpmnDraft {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流程唯一标识（Key）
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程描述
     */
    private String description;

    /**
     * BPMN XML内容
     */
    private String xml;

    /**
     * JSON格式的流程定义（前端使用）
     */
    private String jsonDef;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：DRAFT-草稿，PENDING_REVIEW-待审核，APPROVED-已通过，REJECTED-已驳回，PUBLISHED-已发布，DEPLOYED-已部署
     */
    private String status;

    /**
     * 提交审核时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 部署ID
     */
    private String deploymentId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 更新人ID
     */
    private Long updaterId;

    /**
     * 更新人名称
     */
    private String updaterName;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 状态枚举
     */
    public static class Status {
        public static final String DRAFT = "DRAFT";
        public static final String PENDING_REVIEW = "PENDING_REVIEW";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
        public static final String PUBLISHED = "PUBLISHED";
        public static final String DEPLOYED = "DEPLOYED";
    }
}
