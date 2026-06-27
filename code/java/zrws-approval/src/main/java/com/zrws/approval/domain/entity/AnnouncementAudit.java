package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告审核记录实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_announcement_audit")
public class AnnouncementAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long auditId;

    private Long announcementId;

    /** 审核类型: SUBMIT/APPROVE/REJECT/PUBLISH/OFFLINE */
    private String auditType;

    private String auditResult;

    private String comment;

    private String rejectReason;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    @TableLogic
    private Integer isDeleted;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 审核类型枚举
     */
    public enum AuditType {
        SUBMIT("提交"),
        APPROVE("审批通过"),
        REJECT("驳回"),
        PUBLISH("发布"),
        OFFLINE("下线");

        private final String desc;

        AuditType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}