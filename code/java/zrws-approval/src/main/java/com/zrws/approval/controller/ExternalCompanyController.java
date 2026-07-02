package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.ApiKey;
import com.zrws.approval.domain.entity.ExternalCompany;
import com.zrws.approval.service.ExternalCompanyService;
import com.zrws.approval.service.OpenApiAuthService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<List<ExternalCompany>> list() {
        try {
            List<ExternalCompany> companies = externalCompanyService.listAll();
            return R.ok(companies);
        } catch (Exception e) {
            log.error("查询外部公司列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public R<ExternalCompany> create(@RequestBody ExternalCompany company) {
        if (company.getCompanyName() == null || company.getCompanyName().isEmpty()) {
            return R.fail("公司名称不能为空");
        }
        try {
            ExternalCompany created = externalCompanyService.create(company);
            return R.ok(created);
        } catch (Exception e) {
            log.error("创建外部公司失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public R<ExternalCompany> update(@RequestBody ExternalCompany company) {
        if (company.getCompanyId() == null) {
            return R.fail("公司ID不能为空");
        }
        try {
            externalCompanyService.update(company);
            return R.ok(company);
        } catch (Exception e) {
            log.error("更新外部公司失败, companyId={}", company.getCompanyId(), e);
            return R.fail("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{companyId}")
    public R<String> delete(@PathVariable Long companyId) {
        if (companyId == null) {
            return R.fail("公司ID不能为空");
        }
        try {
            externalCompanyService.delete(companyId);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除外部公司失败, companyId={}", companyId, e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate-api-key")
    public R<ApiKey> generateApiKey(@RequestBody Map<String, Object> request) {
        Long companyId = request.get("companyId") != null ? Long.valueOf(request.get("companyId").toString()) : null;
        String companyName = (String) request.get("companyName");
        String permissions = (String) request.get("permissions");

        if (companyId == null) {
            return R.fail("公司ID不能为空");
        }
        if (companyName == null || companyName.isEmpty()) {
            return R.fail("公司名称不能为空");
        }

        try {
            ApiKey apiKey = openApiAuthService.generateApiKey(companyId, companyName, permissions);
            return R.ok(apiKey);
        } catch (Exception e) {
            log.error("生成API Key失败, companyId={}", companyId, e);
            return R.fail("生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/api-keys")
    public R<List<ApiKey>> listApiKeys() {
        try {
            List<ApiKey> apiKeys = openApiAuthService.listApiKeys();
            return R.ok(apiKeys);
        } catch (Exception e) {
            log.error("查询API Key列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/revoke-api-key/{keyId}")
    public R<String> revokeApiKey(@PathVariable Long keyId) {
        if (keyId == null) {
            return R.fail("Key ID不能为空");
        }
        try {
            openApiAuthService.revokeApiKey(keyId);
            return R.ok("吊销成功");
        } catch (Exception e) {
            log.error("吊销API Key失败, keyId={}", keyId, e);
            return R.fail("吊销失败: " + e.getMessage());
        }
    }
}
