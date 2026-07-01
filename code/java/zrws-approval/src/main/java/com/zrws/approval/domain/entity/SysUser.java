package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 真实姓名 */
    private String realName;

    /** 头像URL */
    private String avatar;

    /** 支付宝用户ID */
    private String alipayUserId;

    /** 微信openid */
    private String wechatOpenid;

    /** 微信unionid */
    private String wechatUnionid;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 状态: ACTIVE/DISABLED */
    private String status;

    /** 当前登录组织ID */
    private Long currentOrgId;

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

    public enum Status {
        ACTIVE, DISABLED
    }
}
