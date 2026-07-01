package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_payment_order")
public class PaymentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户订单号（唯一） */
    private String orderNo;

    /** 组织ID */
    private Long orgId;

    /** 用户ID */
    private Long userId;

    /** 订单标题 */
    private String subject;

    /** 订单金额（元） */
    private java.math.BigDecimal totalAmount;

    /** 支付渠道: ALIPAY/WECHAT */
    private String payChannel;

    /** 第三方交易号 */
    private String tradeNo;

    /** 支付状态: PENDING/PAID/FAILED/REFUNDED */
    private String payStatus;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 二维码链接 */
    private String qrCode;

    /** 异步通知时间 */
    private LocalDateTime notifyTime;

    /** 订单附加信息 */
    private String body;

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

    public enum PayChannel {
        ALIPAY, WECHAT
    }

    public enum PayStatus {
        PENDING, PAID, FAILED, REFUNDED
    }
}
