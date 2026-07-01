package com.zrws.approval.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MyBatis-Plus 配置
 * <p>包含多租户拦截器、分页拦截器、乐观锁拦截器。
 * <ul>
 *     <li>TenantLineInnerInterceptor：基于 tenant_id 列自动拼装租户条件</li>
 *     <li>PaginationInnerInterceptor：MySQL 分页</li>
 *     <li>OptimisticLockerInnerInterceptor：基于 version 列的乐观锁</li>
 * </ul>
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 统一数据表（系统级公共数据），存在 tenant_id 字段但不应被租户过滤。
     * <p>注：zrws_sys_role（系统角色表）按租户隔离，不在此列表中；
     * zrws_organization（组织表）按租户隔离，不在此列表中。
     */
    private static final Set<String> IGNORE_TABLES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "zrws_geo_standard",
            "zrws_eco_standard",
            "zrws_climate_warming",
            "zrws_desertification",
            "zrws_sys_config",
            "zrws_sys_menu",
            "zrws_announcement_category"
    )));

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户拦截器
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getTenantId();
                return new LongValue(tenantId == null ? 0L : tenantId);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                if (tableName == null || tableName.isEmpty()) {
                    return true;
                }
                // ACT_ 前缀的表（Flowable 工作流表）不进行租户过滤
                if (tableName.toUpperCase().startsWith("ACT_")) {
                    return true;
                }
                // 统一数据表（系统级公共数据）不进行租户过滤
                return IGNORE_TABLES.contains(tableName);
            }
        }));

        // 2. 分页拦截器（MySQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 3. 乐观锁拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
}
