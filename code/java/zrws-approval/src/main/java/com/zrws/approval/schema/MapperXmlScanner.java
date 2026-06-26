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
 * Mapper XML 扫描器
 * 解析 MyBatis Mapper XML 文件，提取表名
 */
@Slf4j
public class MapperXmlScanner {

    // 支持带反引号的表名: `table_name` 或 table_name
    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(?i)(?:FROM|INTO|UPDATE|JOIN)\\s+`?([a-zA-Z_][a-zA-Z0-9_]*)`?");

    private final String mapperLocations;

    public MapperXmlScanner(String mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    /**
     * 扫描所有 Mapper XML 文件，提取涉及的表
     */
    public List<TableMetadata> scan() {
        List<TableMetadata> tables = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(mapperLocations);

            for (Resource resource : resources) {
                if (!resource.exists()) continue;
                try (InputStream is = resource.getInputStream()) {
                    String content = new String(is.readAllBytes());
                    Set<String> tableNames = extractTableNames(content);
                    for (String name : tableNames) {
                        String key = name.toLowerCase();
                        if (processed.contains(key)) continue;
                        processed.add(key);

                        TableMetadata table = new TableMetadata();
                        table.setTableName(name);
                        table.setSource("MAPPER_XML:" + resource.getFilename());
                        tables.add(table);
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描 Mapper XML 失败", e);
        }

        return tables;
    }

    private Set<String> extractTableNames(String xml) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = TABLE_PATTERN.matcher(xml);
        while (matcher.find()) {
            String name = matcher.group(1);
            // 过滤 MyBatis 参数变量和太短的
            if (name.contains("$") || name.contains("#{") || name.length() < 2) continue;
            tables.add(name);
        }
        return tables;
    }
}
