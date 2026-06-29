package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 电子签章配置实体
 * <p>记录电子签章的基本信息和证书配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_seal_config")
public class SealConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 签章ID */
    @TableId(type = IdType.AUTO)
    private Long sealId;

    /** 签章名称 */
    private String sealName;

    /** 签章类型: COMPANY/PERSONAL/OFFICIAL */
    private String sealType;

    /** 签章图片base64或路径 */
    private String sealImage;

    /** 证书路径 */
    private String certificatePath;

    /** 证书密码 */
    private String certificatePassword;

    /** 签发机构 */
    private String issuer;

    /** 持有人 */
    private String holder;

    /** 状态: ACTIVE/INACTIVE */
    private String status;

    /** 创建人ID */
    private Long createUserId;

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
     * 签章类型枚举
     */
    public enum SealType {
        COMPANY,
        PERSONAL,
        OFFICIAL
    }

    /**
     * 状态枚举
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
