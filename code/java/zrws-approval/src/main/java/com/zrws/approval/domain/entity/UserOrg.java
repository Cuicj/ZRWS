package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-组织关联实体（多对多）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_user_org")
public class UserOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 组织ID */
    private Long orgId;

    /** 角色ID */
    private Long roleId;

    /** 部门ID */
    private Long deptId;

    /** 加入时间 */
    private LocalDateTime joinTime;

    /** 状态: ACTIVE/PENDING/REJECTED */
    private String status;

    /** 租户ID */
    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer isDeleted;

    public enum Status {
        ACTIVE, PENDING, REJECTED
    }
}
