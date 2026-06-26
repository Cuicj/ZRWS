package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告分类实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_announcement_category")
public class AnnouncementCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private String categoryCode;

    private String categoryName;

    private String icon;

    private String color;

    private String description;

    private Integer sortOrder;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}