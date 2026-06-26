package com.zrws.approval.schema;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实体类扫描器
 * 扫描带 @TableName 注解的实体类，提取表名和字段信息
 */
@Slf4j
public class EntityClassScanner {

    private final String basePackage;
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([a-z])([A-Z])");

    public EntityClassScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 扫描所有带 @TableName 注解的实体类
     */
    public List<TableMetadata> scan() {
        List<TableMetadata> tables = new ArrayList<>();

        try {
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(TableName.class));

            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition def : candidates) {
                String className = def.getBeanClassName();
                if (className == null) continue;
                try {
                    Class<?> clazz = Class.forName(className);
                    TableMetadata table = parseEntity(clazz);
                    if (table != null) {
                        tables.add(table);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("加载实体类失败: {}", className);
                }
            }
        } catch (Exception e) {
            log.error("扫描实体类失败", e);
        }

        return tables;
    }

    private TableMetadata parseEntity(Class<?> clazz) {
        TableName tableNameAnno = clazz.getAnnotation(TableName.class);
        if (tableNameAnno == null) return null;

        TableMetadata table = new TableMetadata();
        table.setTableName(tableNameAnno.value());
        table.setSource("ENTITY");

        List<Field> allFields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (int i = fields.length - 1; i >= 0; i--) {
                allFields.add(0, fields[i]);
            }
            current = current.getSuperclass();
        }

        for (Field field : allFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            if (field.isSynthetic()) continue;

            ColumnMetadata col = parseField(field);
            if (col != null) {
                table.addColumn(col);
            }
        }

        log.debug("扫描实体: {} -> {} ({} 字段)", clazz.getSimpleName(), table.getTableName(), table.getColumns().size());
        return table;
    }

    private ColumnMetadata parseField(Field field) {
        ColumnMetadata col = new ColumnMetadata();
        col.setName(camelToSnake(field.getName()));

        TableField tableField = field.getAnnotation(TableField.class);
        TableId tableId = field.getAnnotation(TableId.class);

        if (tableField != null && !tableField.exist()) {
            return null;
        }

        if (tableField != null && !tableField.value().isEmpty()) {
            col.setName(tableField.value());
        }

        if (tableId != null) {
            col.setPrimaryKey(true);
            IdType idType = tableId.type();
            col.setAutoIncrement(idType == IdType.AUTO);
            if (tableId.value() != null && !tableId.value().isEmpty()) {
                col.setName(tableId.value());
            }
        }

        Class<?> javaType = field.getType();
        col.setType(mapJavaTypeToSql(javaType));
        col.setLength(getDefaultLength(javaType));
        col.setNullable(!col.isPrimaryKey());

        return col;
    }

    private String camelToSnake(String camel) {
        if (camel == null || camel.isEmpty()) return camel;
        Matcher matcher = CAMEL_CASE_PATTERN.matcher(camel);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "_" + matcher.group(2).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString().toLowerCase();
    }

    private String mapJavaTypeToSql(Class<?> javaType) {
        if (javaType == Long.class || javaType == long.class) return "BIGINT";
        if (javaType == Integer.class || javaType == int.class) return "INT";
        if (javaType == Short.class || javaType == short.class) return "SMALLINT";
        if (javaType == Byte.class || javaType == byte.class) return "TINYINT";
        if (javaType == Double.class || javaType == double.class) return "DOUBLE";
        if (javaType == Float.class || javaType == float.class) return "FLOAT";
        if (javaType == Boolean.class || javaType == boolean.class) return "TINYINT(1)";
        if (javaType == java.math.BigDecimal.class) return "DECIMAL(18,2)";
        if (javaType == java.util.Date.class || javaType == java.time.LocalDateTime.class
                || javaType == java.time.LocalDate.class) return "DATETIME";
        if (javaType == java.time.LocalTime.class) return "TIME";
        if (javaType == byte[].class) return "LONGTEXT";
        return "VARCHAR";
    }

    private Integer getDefaultLength(Class<?> javaType) {
        if (javaType == String.class) return 255;
        if (javaType == java.math.BigDecimal.class) return 18;
        return null;
    }
}
