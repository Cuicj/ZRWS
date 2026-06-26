package com.zrws.approval.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 字段元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段类型（如 VARCHAR, BIGINT, DATETIME 等）
     */
    private String type;

    /**
     * 字段长度
     */
    private Integer length;

    /**
     * 是否可空
     */
    private boolean nullable = true;

    /**
     * 是否主键
     */
    private boolean primaryKey = false;

    /**
     * 是否自增
     */
    private boolean autoIncrement = false;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字段注释
     */
    private String comment;
}
