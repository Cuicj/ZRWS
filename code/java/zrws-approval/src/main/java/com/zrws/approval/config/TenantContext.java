package com.zrws.approval.config;

/**
 * 租户上下文（ThreadLocal）
 * <p>用于在请求中传递 tenantId，配合多租户拦截器实现自动租户过滤。
 */
public class TenantContext {

    private static ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        return currentTenant.get();
    }

    /**
     * 清除当前线程的租户ID，防止内存泄漏
     */
    public static void clear() {
        currentTenant.remove();
    }
}
