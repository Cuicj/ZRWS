package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zrws.approval.config.TenantContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充 createdTime, updatedTime, tenantId
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);

        // 自动填充tenantId（如果字段存在且为空）
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null && tenantId > 0) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }
}
