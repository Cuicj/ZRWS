package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_organization")
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 组织名称 */
    private String orgName;

    /** 组织类型: PERSONAL/ENTERPRISE */
    private String orgType;

    /** 订阅级别: FREE/PRO/ENTERPRISE */
    private String subscriptionLevel;

    /** 订阅到期时间 */
    private LocalDateTime subscriptionExpireTime;

    /** 最大成员数（null表示不限） */
    private Integer maxMembers;

    /** 状态: ACTIVE/DISABLED */
    private String status;

    /** 租户ID */
    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    private Long createdBy;

    private Long updatedBy;

    @TableLogic
    private Integer isDeleted;
}
