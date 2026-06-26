package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long menuId;

    private Long parentId;

    private String menuName;

    private String menuPath;

    private String menuIcon;

    private String menuType;

    private String component;

    private String permission;

    private Integer sortOrder;

    private Integer status;

    private String menuGroup;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
