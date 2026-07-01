package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订阅实体 - SaaS订阅管理
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_subscription")
public class Subscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 组织ID */
    private Long orgId;

    /** 订阅计划编码: FREE/PRO/ENTERPRISE */
    private String planCode;

    /** 订阅计划名称 */
    private String planName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 金额（元） */
    private java.math.BigDecimal amount;

    /** 状态: ACTIVE/EXPIRED/CANCELLED */
    private String status;

    /** 是否自动续费 */
    private Boolean autoRenew;

    /** 关联支付订单ID */
    private Long paymentOrderId;

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

    public enum PlanCode {
        FREE, PRO, ENTERPRISE
    }

    public enum Status {
        ACTIVE, EXPIRED, CANCELLED
    }
}
