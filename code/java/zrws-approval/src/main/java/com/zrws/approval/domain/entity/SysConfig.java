package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 全局配置实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long configId;

    private String configKey;

    private String configValue;

    /** 类型: STRING/INT/BOOLEAN/JSON */
    private String configType;

    private String configName;

    private String configGroup;

    private String description;

    private Integer sortOrder;

    private Integer status;

    /** 系统内置 */
    private Integer isSystem;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 获取布尔值
     */
    public Boolean getBooleanValue() {
        return "true".equalsIgnoreCase(configValue);
    }

    /**
     * 获取整数值
     */
    public Integer getIntValue() {
        try {
            return Integer.parseInt(configValue);
        } catch (Exception e) {
            return 0;
        }
    }
}