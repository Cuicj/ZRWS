package com.zrws.approval.domain.dto;

import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterRequest {

    /** 手机号 */
    private String phone;

    /** 验证码 */
    private String verifyCode;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 组织名称 */
    private String orgName;

    /** 支付宝用户ID（可空） */
    private String alipayUserId;

    /** 微信openid（可空） */
    private String wechatOpenid;
}
