package com.zrws.approval.domain.dto;

import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponse {

    /** JWT令牌 */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 组织ID */
    private Long orgId;

    /** 组织名称 */
    private String orgName;

    /** 租户ID */
    private Long tenantId;

    /** 订阅等级 */
    private String subscriptionLevel;

    /** 头像 */
    private String avatar;
}
