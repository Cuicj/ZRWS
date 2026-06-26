package com.zrws.approval.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 初始化脚本解析器
 * 解析 resources/sql/ 下的 SQL 文件，提取 CREATE TABLE / INSERT 语句涉及的表
 */
@Slf4j
public class SqlScriptScanner {

    private static final Pattern CREATE_TABLE_PATTERN =
            Pattern.compile("(?i)CREATE\\s+TABLE(?:\\s+IF\\s+NOT\\s+EXISTS)?\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

    private static final Pattern INSERT_PATTERN =
            Pattern.compile("(?i)INSERT\\s+INTO\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

    private final String sqlInitLocation;

    public SqlScriptScanner(String sqlInitLocation) {
        this.sqlInitLocation = sqlInitLocation;
    }

    public List<TableMetadata> scan() {
        List<TableMetadata> tables = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(sqlInitLocation);

            for (Resource resource : resources) {
                if (!resource.exists()) continue;
                try (InputStream is = resource.getInputStream()) {
                    String content = new String(is.readAllBytes());
                    // 移除 SQL 注释
                    content = stripComments(content);

                    // 提取 CREATE TABLE
                    Matcher createMatcher = CREATE_TABLE_PATTERN.matcher(content);
                    while (createMatcher.find()) {
                        String name = createMatcher.group(1);
                        String key = name.toLowerCase();
                        if (processed.contains(key)) continue;
                        processed.add(key);

                        TableMetadata table = new TableMetadata();
                        table.setTableName(name);
                        table.setSource("SQL_FILE:" + resource.getFilename());
                        tables.add(table);
                    }

                    // 提取 INSERT INTO（用于检测初始数据表）
                    Matcher insertMatcher = INSERT_PATTERN.matcher(content);
                    while (insertMatcher.find()) {
                        String name = insertMatcher.group(1);
                        String key = name.toLowerCase();
                        if (processed.contains(key)) continue;
                        processed.add(key);

                        TableMetadata table = new TableMetadata();
                        table.setTableName(name);
                        table.setSource("SQL_FILE_INSERT:" + resource.getFilename());
                        tables.add(table);
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描 SQL 脚本失败", e);
        }

        return tables;
    }

    private String stripComments(String sql) {
        // 移除 -- 单行注释
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");
        // 移除 /* */ 多行注释
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        return sql;
    }
}
