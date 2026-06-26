package com.zrws.approval.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表结构元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 字段映射（字段名 -> 字段定义）
     */
    private Map<String, ColumnMetadata> columns = new LinkedHashMap<>();

    /**
     * 主键字段
     */
    private String primaryKey;

    /**
     * 表注释
     */
    private String comment;

    /**
     * 来源: ENTITY / MAPPER_XML / SQL_FILE
     */
    private String source;

    public void addColumn(ColumnMetadata col) {
        columns.put(col.getName().toLowerCase(), col);
        if (col.isPrimaryKey()) {
            this.primaryKey = col.getName();
        }
    }
}
