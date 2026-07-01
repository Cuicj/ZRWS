package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 角色类型: SYSTEM/CUSTOM */
    private String roleType;

    /** 权限列表（JSON格式） */
    private String permissions;

    /** 数据范围: ALL/DEPT/SELF */
    private String dataScope;

    /** 排序 */
    private Integer sortOrder;

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

    public enum RoleType {
        SYSTEM, CUSTOM
    }

    public enum DataScope {
        ALL, DEPT, SELF
    }

    public enum Status {
        ACTIVE, DISABLED
    }
}
