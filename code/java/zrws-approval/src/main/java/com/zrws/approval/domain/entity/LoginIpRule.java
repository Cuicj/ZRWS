package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录IP限制规则实体
 */
@Data
@TableName("zrws_login_ip_rule")
public class LoginIpRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** IP地址（支持精确IP如 192.168.1.1 或 CIDR如 192.168.1.0/24） */
    private String ipAddress;

    /** 规则类型: BLACKLIST(禁止), WHITELIST(允许) */
    private String ruleType;

    /** 备注 */
    private String remark;

    /** 状态: ENABLED(启用), DISABLED(禁用) */
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

    public enum RuleType {
        BLACKLIST, WHITELIST
    }

    public enum Status {
        ENABLED, DISABLED
    }
}
