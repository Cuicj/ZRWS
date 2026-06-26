package com.zrws.approval.schema;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * DBA 管理 API
 * 提供手动触发 Schema 同步的接口
 */
@RestController
@RequestMapping("/api/v1/dba")
@RequiredArgsConstructor
public class DbaAdminController {

    private final SchemaSyncService schemaSyncService;

    /**
     * 手动触发 Schema 同步
     */
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        SchemaSyncService.SyncResult result = schemaSyncService.sync();
        return toResponse(result);
    }

    /**
     * 获取最近一次同步状态（占位，可扩展为持久化）
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "ok");
        Map<String, Object> data = new HashMap<>();
        data.put("service", "zrws-approval");
        data.put("feature", "schema-auto-sync");
        data.put("description", "数据库 Schema 自动校验与同步");
        resp.put("data", data);
        return resp;
    }

    private Map<String, Object> toResponse(SchemaSyncService.SyncResult r) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", r.errors.isEmpty() ? 0 : -1);
        resp.put("msg", r.errors.isEmpty() ? "同步完成" : "同步存在错误");
        Map<String, Object> data = new HashMap<>();
        data.put("scannedEntityCount", r.scannedEntityCount);
        data.put("scannedMapperCount", r.scannedMapperCount);
        data.put("scannedSqlCount", r.scannedSqlCount);
        data.put("existingTableCount", r.existingTableCount);
        data.put("createdTableCount", r.createdTableCount);
        data.put("addedColumnCount", r.addedColumnCount);
        data.put("createdTables", r.createdTables);
        data.put("addedColumns", r.addedColumns);
        data.put("warnings", r.warnings);
        data.put("errors", r.errors);
        data.put("costMillis", r.costMillis);
        resp.put("data", data);
        return resp;
    }
}
