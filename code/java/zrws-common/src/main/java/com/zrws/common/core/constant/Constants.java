package com.zrws.common.core.constant;

/**
 * 通用常量
 */
public class Constants {

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码
     */
    public static final int FAIL = 500;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 系统内置用户ID
     */
    public static final Long SYS_USER_ID = 1L;

    /**
     * 系统管理员角色ID
     */
    public static final Long ADMIN_ROLE_ID = 1L;

    /**
     * 正常状态
     */
    public static final String STATUS_NORMAL = "0";

    /**
     * 禁用状态
     */
    public static final String STATUS_DISABLE = "1";

    /**
     * 删除标记
     */
    public static final String DEL_FLAG = "0";

    /**
     * 删除
     */
    public static final String DEL_FLAG_DELETED = "1";

    /**
     * 审批状态 - 待审批
     */
    public static final String APPROVAL_STATUS_PENDING = "PENDING";

    /**
     * 审批状态 - 已通过
     */
    public static final String APPROVAL_STATUS_APPROVED = "APPROVED";

    /**
     * 审批状态 - 已驳回
     */
    public static final String APPROVAL_STATUS_REJECTED = "REJECTED";

    /**
     * 审批状态 - 已发布
     */
    public static final String APPROVAL_STATUS_PUBLISHED = "PUBLISHED";
}