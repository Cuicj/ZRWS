package com.zrws.approval.schema;

import com.zrws.approval.config.SchemaSyncProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据库 Schema 自动同步服务
 *
 * 核心功能：
 * 1. 扫描实体类、Mapper XML、SQL 脚本，收集期望的表结构
 * 2. 对比数据库真实表结构，找出差异
 * 3. 自动同步缺失的表和字段
 */
@Slf4j
@Service
public class SchemaSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchemaSyncProperties properties;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUser;

    /**
     * 同步结果
     */
    @Data
    public static class SyncResult {
        public int scannedEntityCount = 0;
        public int scannedMapperCount = 0;
        public int scannedSqlCount = 0;
        public int existingTableCount = 0;
        public int createdTableCount = 0;
        public int addedColumnCount = 0;
        public List<String> createdTables = new ArrayList<>();
        public List<String> addedColumns = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
        public long costMillis = 0;
    }

    /**
     * 执行完整的 Schema 同步
     */
    public SyncResult sync() {
        long start = System.currentTimeMillis();
        SyncResult result = new SyncResult();

        if (!properties.isEnabled()) {
            log.info("[DBA] Schema 自动同步已禁用 (zrws.dba.enabled=false)");
            result.warnings.add("Schema 同步已禁用");
            return result;
        }

        log.info("========================================");
        log.info("  [DBA] 开始数据库 Schema 自动校验");
        log.info("  数据库: {}", datasourceUrl);
        log.info("  用户: {}", datasourceUser);
        log.info("========================================");

        try {
            // 1. 收集所有期望的表
            Map<String, TableMetadata> expectedTables = collectExpectedTables(result);
            log.info("[DBA] 期望表总数: {} 张", expectedTables.size());
            if (properties.isVerbose()) {
                expectedTables.values().forEach(t ->
                        log.info("  · {} [来源: {}, 字段: {}]", t.getTableName(), t.getSource(), t.getColumns().size()));
            }

            // 2. 过滤掉需要排除的表
            int beforeFilter = expectedTables.size();
            expectedTables = filterExcludedTables(expectedTables);
            if (expectedTables.size() < beforeFilter) {
                log.info("[DBA] 已排除 {} 张框架表 (匹配前缀: {})",
                        beforeFilter - expectedTables.size(), Arrays.toString(properties.getExcludeTablePrefixes()));
            }

            // 3. 获取数据库现有表
            Map<String, Set<String>> existingTables = fetchExistingTables();
            result.existingTableCount = existingTables.size();
            log.info("[DBA] 数据库现有表总数: {} 张", existingTables.size());

            // 4. 比对并同步
            syncTables(expectedTables, existingTables, result);

            // 5. 输出汇总
            result.costMillis = System.currentTimeMillis() - start;
            printSummary(result);

        } catch (Exception e) {
            log.error("[DBA] Schema 同步过程发生异常", e);
            result.errors.add("同步异常: " + e.getMessage());
            if (properties.isFailOnSyncError()) {
                throw new RuntimeException("Schema 同步失败，应用启动中止", e);
            }
        }

        return result;
    }

    /**
     * 收集所有期望的表（实体类 + Mapper XML + SQL 脚本）
     */
    private Map<String, TableMetadata> collectExpectedTables(SyncResult result) {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();

        // 1. 扫描实体类
        EntityClassScanner entityScanner = new EntityClassScanner(properties.getEntityPackage());
        List<TableMetadata> entityTables = entityScanner.scan();
        result.scannedEntityCount = entityTables.size();
        for (TableMetadata t : entityTables) {
            tables.putIfAbsent(t.getTableName().toLowerCase(), t);
        }
        log.info("[DBA] 实体类扫描: {} 张表", entityTables.size());

        // 2. 扫描 Mapper XML
        MapperXmlScanner mapperScanner = new MapperXmlScanner(properties.getMapperLocations());
        List<TableMetadata> mapperTables = mapperScanner.scan();
        result.scannedMapperCount = mapperTables.size();
        for (TableMetadata t : mapperTables) {
            // Mapper XML 通常不提供字段定义，使用表名占位
            tables.putIfAbsent(t.getTableName().toLowerCase(), t);
        }
        log.info("[DBA] Mapper XML 扫描: {} 个表引用", mapperTables.size());

        // 3. 扫描 SQL 脚本
        SqlScriptScanner sqlScanner = new SqlScriptScanner(properties.getSqlInitLocation());
        List<TableMetadata> sqlTables = sqlScanner.scan();
        result.scannedSqlCount = sqlTables.size();
        for (TableMetadata t : sqlTables) {
            tables.putIfAbsent(t.getTableName().toLowerCase(), t);
        }
        log.info("[DBA] SQL 脚本扫描: {} 个表引用", sqlTables.size());

        return tables;
    }

    /**
     * 过滤掉需要排除的表
     */
    private Map<String, TableMetadata> filterExcludedTables(Map<String, TableMetadata> tables) {
        Map<String, TableMetadata> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, TableMetadata> entry : tables.entrySet()) {
            String name = entry.getKey();
            boolean excluded = false;
            for (String prefix : properties.getExcludeTablePrefixes()) {
                if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                filtered.put(name, entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * 获取数据库现有表及字段
     */
    private Map<String, Set<String>> fetchExistingTables() {
        Map<String, Set<String>> tables = new LinkedHashMap<>();
        try {
            // 一次性查询所有表和字段（避免 N+1 查询）
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME, COLUMN_NAME " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "ORDER BY TABLE_NAME, ORDINAL_POSITION"
            );
            for (Map<String, Object> row : rows) {
                String tableName = ((String) row.get("TABLE_NAME")).toLowerCase();
                String columnName = (String) row.get("COLUMN_NAME");
                tables.computeIfAbsent(tableName, k -> new HashSet<>()).add(columnName.toLowerCase());
            }
        } catch (Exception e) {
            log.error("[DBA] 获取数据库表结构失败", e);
            throw e;
        }
        return tables;
    }

    /**
     * 同步表结构
     */
    private void syncTables(Map<String, TableMetadata> expectedTables,
                            Map<String, Set<String>> existingTables,
                            SyncResult result) {
        for (Map.Entry<String, TableMetadata> entry : expectedTables.entrySet()) {
            String tableName = entry.getKey();
            TableMetadata table = entry.getValue();

            if (existingTables.containsKey(tableName)) {
                // 表已存在，比对字段
                syncColumns(table, existingTables.get(tableName), result);
            } else {
                // 表不存在，创建表
                createTable(table, result);
            }
        }
    }

    /**
     * 创建表（仅在实体类有完整字段定义时）
     */
    private void createTable(TableMetadata table, SyncResult result) {
        if (table.getColumns().isEmpty()) {
            String msg = "跳过表创建（无字段定义）: " + table.getTableName() + " [" + table.getSource() + "]";
            log.warn("[DBA] {}", msg);
            result.warnings.add(msg);
            return;
        }

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `").append(table.getTableName()).append("` (\n");

            List<String> colDefs = new ArrayList<>();
            List<String> primaryKeys = new ArrayList<>();

            for (ColumnMetadata col : table.getColumns().values()) {
                StringBuilder colDef = new StringBuilder();
                colDef.append("  `").append(col.getName()).append("` ");
                colDef.append(col.getType());
                String upperType = col.getType().toUpperCase();
                if (upperType.startsWith("VARCHAR") && !col.getType().contains("(") && col.getLength() != null) {
                    colDef.append("(").append(col.getLength()).append(")");
                }
                if (upperType.startsWith("DECIMAL") && !col.getType().contains("(")) {
                    colDef.append("(18,2)");
                }
                if (!col.isNullable()) {
                    colDef.append(" NOT NULL");
                }
                if (col.isAutoIncrement()) {
                    colDef.append(" AUTO_INCREMENT");
                }
                if (col.getDefaultValue() != null) {
                    colDef.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
                }
                if (col.getComment() != null && !col.getComment().isEmpty()) {
                    colDef.append(" COMMENT '").append(col.getComment().replace("'", "''")).append("'");
                }
                colDefs.add(colDef.toString());

                if (col.isPrimaryKey()) {
                    primaryKeys.add("`" + col.getName() + "`");
                }
            }

            if (!primaryKeys.isEmpty()) {
                colDefs.add("  PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
            }

            sql.append(String.join(",\n", colDefs));
            sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            if (table.getComment() != null) {
                sql.append(" COMMENT='").append(table.getComment().replace("'", "''")).append("'");
            }

            if (properties.isVerbose()) {
                log.debug("[DBA] 创建表 SQL:\n{}", sql);
            }

            jdbcTemplate.execute(sql.toString());
            result.createdTableCount++;
            result.createdTables.add(table.getTableName());
            log.info("[DBA] ✅ 创建表: {} (字段: {})", table.getTableName(), table.getColumns().size());
        } catch (Exception e) {
            String msg = "创建表失败: " + table.getTableName() + " - " + e.getMessage();
            log.error("[DBA] ❌ {}", msg);
            log.error("[DBA] 表 {} 字段列表: {}", table.getTableName(), 
                    table.getColumns().keySet());
            result.errors.add(msg);
        }
    }

    /**
     * 同步字段
     */
    private void syncColumns(TableMetadata table, Set<String> existingColumns, SyncResult result) {
        if (table.getColumns().isEmpty()) {
            return;
        }

        for (ColumnMetadata col : table.getColumns().values()) {
            if (existingColumns.contains(col.getName().toLowerCase())) {
                continue;
            }

            // 字段缺失，添加
            try {
                StringBuilder sql = new StringBuilder();
                sql.append("ALTER TABLE `").append(table.getTableName()).append("` ");
                sql.append("ADD COLUMN `").append(col.getName()).append("` ");
                sql.append(col.getType());
                String upperType = col.getType().toUpperCase();
                if (upperType.startsWith("VARCHAR") && !col.getType().contains("(") && col.getLength() != null) {
                    sql.append("(").append(col.getLength()).append(")");
                }
                if (upperType.startsWith("DECIMAL") && !col.getType().contains("(")) {
                    sql.append("(18,2)");
                }
                if (!col.isNullable()) {
                    sql.append(" NOT NULL");
                }
                if (col.getDefaultValue() != null) {
                    sql.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
                }
                if (col.getComment() != null && !col.getComment().isEmpty()) {
                    sql.append(" COMMENT '").append(col.getComment().replace("'", "''")).append("'");
                }

                jdbcTemplate.execute(sql.toString());
                result.addedColumnCount++;
                result.addedColumns.add(table.getTableName() + "." + col.getName());
                log.info("[DBA] ➕ 添加字段: {}.{}", table.getTableName(), col.getName());
            } catch (Exception e) {
                String msg = "添加字段失败: " + table.getTableName() + "." + col.getName() + " - " + e.getMessage();
                log.error("[DBA] ❌ {}", msg);
                result.errors.add(msg);
            }
        }
    }

    /**
     * 打印汇总信息
     */
    private void printSummary(SyncResult result) {
        log.info("========================================");
        log.info("  [DBA] Schema 同步完成");
        log.info("========================================");
        log.info("  · 扫描实体类:  {} 张表", result.scannedEntityCount);
        log.info("  · 扫描Mapper:  {} 个引用", result.scannedMapperCount);
        log.info("  · 扫描SQL脚本: {} 个引用", result.scannedSqlCount);
        log.info("  · 现有表:      {} 张", result.existingTableCount);
        log.info("  · 新建表:      {} 张", result.createdTableCount);
        log.info("  · 新增字段:    {} 个", result.addedColumnCount);
        log.info("  · 耗时:        {} ms", result.costMillis);
        log.info("  · 警告:        {} 条", result.warnings.size());
        log.info("  · 错误:        {} 条", result.errors.size());

        if (!result.createdTables.isEmpty()) {
            log.info("  · 新建表清单: {}", String.join(", ", result.createdTables));
        }
        if (!result.addedColumns.isEmpty()) {
            log.info("  · 新增字段清单: {}", String.join(", ", result.addedColumns));
        }
        if (!result.warnings.isEmpty()) {
            log.warn("  ⚠ 警告列表:");
            result.warnings.forEach(w -> log.warn("    - {}", w));
        }
        if (!result.errors.isEmpty()) {
            log.error("  ❌ 错误列表:");
            result.errors.forEach(e -> log.error("    - {}", e));
        }
        log.info("========================================");
    }
}
