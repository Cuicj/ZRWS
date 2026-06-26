package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BO字段配置实体
 * <p>定义BO的字段映射、校验规则、转换规则
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_bo_field")
public class BoField implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字段ID */
    @TableId(type = IdType.AUTO)
    private Long fieldId;

    /** BO定义ID */
    private Long boId;

    /** 字段编码 */
    private String fieldCode;

    /** 字段名称 */
    private String fieldName;

    /** 字段类型: STRING/INT/DECIMAL/DATE/DATETIME/BOOLEAN/ENUM/JSON */
    private String fieldType;

    /** 源文件列名(逗号分隔,AI匹配用) */
    private String sourceNames;

    /** 目标表列名 */
    private String targetColumn;

    /** 默认值 */
    private String defaultValue;

    /** 是否必填 */
    private Integer isRequired;

    /** 是否唯一 */
    private Integer isUnique;

    /** 校验类型: RANGE/LENGTH/REGEX/ENUM/CUSTOM */
    private String validationType;

    /** 校验规则表达式 */
    private String validationRule;

    /** 转换表达式 */
    private String transformExpr;

    /** 枚举值列表(JSON) */
    private String enumValues;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 获取字段类型枚举
     */
    public FieldType getFieldTypeEnum() {
        try {
            return FieldType.valueOf(this.fieldType.toUpperCase());
        } catch (Exception e) {
            return FieldType.STRING;
        }
    }

    /**
     * 字段类型枚举
     */
    public enum FieldType {
        STRING, INT, BIGINT, DECIMAL, DATE, DATETIME, BOOLEAN, ENUM, JSON, TEXT
    }
}