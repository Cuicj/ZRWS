package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.ApiKey;
import com.zrws.approval.domain.entity.ExternalCompany;
import com.zrws.approval.service.ExternalCompanyService;
import com.zrws.approval.service.OpenApiAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/external-company")
@CrossOrigin(origins = "*")
public class ExternalCompanyController {

    @Autowired
    private ExternalCompanyService externalCompanyService;

    @Autowired
    private OpenApiAuthService openApiAuthService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        try {
            List<ExternalCompany> companies = externalCompanyService.listAll();
            return success("companies", companies);
        } catch (Exception e) {
            log.error("查询外部公司列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody ExternalCompany company) {
        if (company.getCompanyName() == null || company.getCompanyName().isEmpty()) {
            return error("公司名称不能为空");
        }
        try {
            ExternalCompany created = externalCompanyService.create(company);
            return success("company", created);
        } catch (Exception e) {
            log.error("创建外部公司失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody ExternalCompany company) {
        if (company.getCompanyId() == null) {
            return error("公司ID不能为空");
        }
        try {
            externalCompanyService.update(company);
            return success("company", company);
        } catch (Exception e) {
            log.error("更新外部公司失败, companyId={}", company.getCompanyId(), e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{companyId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long companyId) {
        if (companyId == null) {
            return error("公司ID不能为空");
        }
        try {
            externalCompanyService.delete(companyId);
            return success();
        } catch (Exception e) {
            log.error("删除外部公司失败, companyId={}", companyId, e);
            return error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate-api-key")
    public ResponseEntity<Map<String, Object>> generateApiKey(@RequestBody Map<String, Object> request) {
        Long companyId = request.get("companyId") != null ? Long.valueOf(request.get("companyId").toString()) : null;
        String companyName = (String) request.get("companyName");
        String permissions = (String) request.get("permissions");

        if (companyId == null) {
            return error("公司ID不能为空");
        }
        if (companyName == null || companyName.isEmpty()) {
            return error("公司名称不能为空");
        }

        try {
            ApiKey apiKey = openApiAuthService.generateApiKey(companyId, companyName, permissions);
            return success("apiKey", apiKey);
        } catch (Exception e) {
            log.error("生成API Key失败, companyId={}", companyId, e);
            return error("生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/api-keys")
    public ResponseEntity<Map<String, Object>> listApiKeys() {
        try {
            List<ApiKey> apiKeys = openApiAuthService.listApiKeys();
            return success("apiKeys", apiKeys);
        } catch (Exception e) {
            log.error("查询API Key列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/revoke-api-key/{keyId}")
    public ResponseEntity<Map<String, Object>> revokeApiKey(@PathVariable Long keyId) {
        if (keyId == null) {
            return error("Key ID不能为空");
        }
        try {
            openApiAuthService.revokeApiKey(keyId);
            return success();
        } catch (Exception e) {
            log.error("吊销API Key失败, keyId={}", keyId, e);
            return error("吊销失败: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> success() {
        return success(null, null);
    }

    private ResponseEntity<Map<String, Object>> success(String key, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (key != null && data != null) {
            result.put(key, data);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return ResponseEntity.badRequest().body(result);
    }
}
