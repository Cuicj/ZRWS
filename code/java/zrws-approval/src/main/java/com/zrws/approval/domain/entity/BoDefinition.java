package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BO业务对象定义实体
 * <p>定义业务对象的结构、目标表、校验规则等配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bo_definition")
public class BoDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /** BO定义ID */
    @TableId(type = IdType.AUTO)
    private Long boId;

    /** BO编码 */
    private String boCode;

    /** BO名称 */
    private String boName;

    /** BO类型: EXCEL/CSV/JSON/XML/TEXT/FIXED_WIDTH */
    private String boType;

    /** 目标表名 */
    private String targetTable;

    /** 描述说明 */
    private String description;

    /** 版本号 */
    private String version;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    /** AI解析提示词 */
    private String aiPrompt;

    /** 校验规则JSON */
    private String validationRule;

    /** 数据转换规则JSON */
    private String transformRule;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建者 */
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新者 */
    private Long updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /** 字段配置列表（非数据库字段） */
    @TableField(exist = false)
    private List<BoField> fields;
}