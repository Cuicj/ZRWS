package com.zrws.approval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.DisasterRisk;
import com.zrws.approval.domain.entity.GeoStandard;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import com.zrws.approval.mapper.DisasterRiskMapper;
import com.zrws.approval.mapper.GeoStandardMapper;
import com.zrws.approval.mapper.RockStratumAnalysisMapper;
import com.zrws.approval.mapper.SoilSampleMapper;
import com.zrws.approval.service.OpenApiAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/openapi/v1")
@CrossOrigin(origins = "*")
public class OpenApiGatewayController {

    @Autowired
    private OpenApiAuthService authService;

    @Autowired
    private GeoStandardMapper geoStandardMapper;

    @Autowired
    private DisasterRiskMapper disasterRiskMapper;

    @Autowired
    private RockStratumAnalysisMapper rockStratumAnalysisMapper;

    @Autowired
    private SoilSampleMapper soilSampleMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/geo-standards")
    public ResponseEntity<Map<String, Object>> getGeoStandards(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestParam(required = false) String category) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/geo-standards", null);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        try {
            List<GeoStandard> standards;
            if (category != null && !category.isEmpty()) {
                standards = geoStandardMapper.selectByCategory(category);
            } else {
                standards = geoStandardMapper.selectList(null);
            }
            return success("standards", standards);
        } catch (Exception e) {
            log.error("查询地质标准数据失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/geo-standards/{id}")
    public ResponseEntity<Map<String, Object>> getGeoStandardById(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @PathVariable Long id) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/geo-standards/" + id, null);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return error("标准不存在");
            }
            return success("standard", standard);
        } catch (Exception e) {
            log.error("查询标准详情失败, id={}", id, e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/soil-analysis/query")
    public ResponseEntity<Map<String, Object>> querySoilAnalysis(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody(required = false) Map<String, Object> request) {

        String bodyStr = null;
        try {
            bodyStr = request != null ? objectMapper.writeValueAsString(request) : "";
        } catch (Exception e) {
            bodyStr = "";
        }

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "POST", "/openapi/v1/soil-analysis/query", bodyStr);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> sample = new HashMap<>();
            sample.put("sampleId", 1L);
            sample.put("sampleCode", "SOIL-2024-001");
            sample.put("location", "北京市朝阳区");
            sample.put("soilType", "壤土");
            sample.put("phValue", 7.2);
            sample.put("organicMatter", 2.5);
            sample.put("status", "ACTIVE");
            result.add(sample);

            Map<String, Object> data = new HashMap<>();
            data.put("total", result.size());
            data.put("records", result);
            return success(data);
        } catch (Exception e) {
            log.error("土质分析数据查询失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/disaster-risk/query")
    public ResponseEntity<Map<String, Object>> queryDisasterRisk(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestParam(required = false) String region) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/disaster-risk/query", null);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        try {
            List<DisasterRisk> risks = disasterRiskMapper.selectList(null);
            if (region != null && !region.isEmpty()) {
                List<DisasterRisk> filtered = new ArrayList<>();
                for (DisasterRisk risk : risks) {
                    if (region.equals(risk.getRegion())) {
                        filtered.add(risk);
                    }
                }
                risks = filtered;
            }
            return success("risks", risks);
        } catch (Exception e) {
            log.error("灾害风险数据查询失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/rock-stratum/query")
    public ResponseEntity<Map<String, Object>> queryRockStratum(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody(required = false) Map<String, Object> request) {

        String bodyStr = null;
        try {
            bodyStr = request != null ? objectMapper.writeValueAsString(request) : "";
        } catch (Exception e) {
            bodyStr = "";
        }

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "POST", "/openapi/v1/rock-stratum/query", bodyStr);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        try {
            List<RockStratumAnalysis> analyses = rockStratumAnalysisMapper.selectAll();
            return success("analyses", analyses);
        } catch (Exception e) {
            log.error("岩层分析数据查询失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/api-doc")
    public ResponseEntity<Map<String, Object>> getApiDoc(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/api-doc", null);
        if (!authValid) {
            return error("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return error("请求频率超过限制");
        }

        List<Map<String, Object>> apis = new ArrayList<>();

        Map<String, Object> api1 = new HashMap<>();
        api1.put("method", "GET");
        api1.put("path", "/geo-standards");
        api1.put("description", "地质标准数据查询");
        api1.put("params", "category(可选): 分类");
        apis.add(api1);

        Map<String, Object> api2 = new HashMap<>();
        api2.put("method", "GET");
        api2.put("path", "/geo-standards/{id}");
        api2.put("description", "标准详情");
        api2.put("params", "id: 标准ID");
        apis.add(api2);

        Map<String, Object> api3 = new HashMap<>();
        api3.put("method", "POST");
        api3.put("path", "/soil-analysis/query");
        api3.put("description", "土质分析数据查询");
        api3.put("params", "请求体: 查询条件");
        apis.add(api3);

        Map<String, Object> api4 = new HashMap<>();
        api4.put("method", "GET");
        api4.put("path", "/disaster-risk/query");
        api4.put("description", "灾害风险数据查询");
        api4.put("params", "region(可选): 区域");
        apis.add(api4);

        Map<String, Object> api5 = new HashMap<>();
        api5.put("method", "POST");
        api5.put("path", "/rock-stratum/query");
        api5.put("description", "岩层分析数据查询");
        api5.put("params", "请求体: 查询条件");
        apis.add(api5);

        return success("apis", apis);
    }

    private ResponseEntity<Map<String, Object>> success(String key, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put(key, data);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> success(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data != null) {
            result.putAll(data);
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
