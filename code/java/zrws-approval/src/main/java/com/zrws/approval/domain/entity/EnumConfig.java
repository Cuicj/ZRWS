package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 枚举值配置实体
 * <p>用于数据校验和转换的枚举值定义
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_enum_config")
public class EnumConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 枚举ID */
    @TableId(type = IdType.AUTO)
    private Long enumId;

    /** 枚举编码 */
    private String enumCode;

    /** 枚举名称 */
    private String enumName;

    /** 枚举项值 */
    private String itemValue;

    /** 枚举项标签 */
    private String itemLabel;

    /** 排序 */
    private Integer itemOrder;

    /** 是否启用 */
    private Integer isActive;

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
}