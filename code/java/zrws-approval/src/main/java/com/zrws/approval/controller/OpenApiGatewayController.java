package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.DisasterRisk;
import com.zrws.approval.domain.entity.GeoStandard;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.mapper.DisasterRiskMapper;
import com.zrws.approval.mapper.GeoStandardMapper;
import com.zrws.approval.mapper.RockStratumAnalysisMapper;
import com.zrws.approval.mapper.SoilSampleMapper;
import com.zrws.approval.service.OpenApiAuthService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/soil-standards")
    public ResponseEntity<Map<String, Object>> getSoilStandards(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/soil-standards", null);
        if (!authValid) {
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            LambdaQueryWrapper<GeoStandard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GeoStandard::getStatus, "ACTIVE");
            wrapper.eq(GeoStandard::getIsDeleted, 0);
            wrapper.in(GeoStandard::getCategory,
                    GeoStandard.Category.SOIL_CHINA.name(),
                    GeoStandard.Category.SOIL_WRB.name());

            if (category != null && !category.isEmpty()) {
                wrapper.eq(GeoStandard::getCategory, category);
            }

            wrapper.orderByAsc(GeoStandard::getSortOrder);

            Page<GeoStandard> pageParam = new Page<>(page, size);
            Page<GeoStandard> resultPage = geoStandardMapper.selectPage(pageParam, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("page", page);
            data.put("size", size);
            data.put("pages", resultPage.getPages());

            return buildSuccessResponse(data);
        } catch (Exception e) {
            log.error("查询土壤标准列表失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/soil-standards/{id}")
    public ResponseEntity<Map<String, Object>> getSoilStandardById(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @PathVariable Long id) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/soil-standards/" + id, null);
        if (!authValid) {
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return buildErrorResponse("标准不存在");
            }
            return buildSuccessResponse(standard);
        } catch (Exception e) {
            log.error("查询土壤标准详情失败, id={}", id, e);
            return buildErrorResponse("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/soil-analysis/classify")
    public ResponseEntity<Map<String, Object>> soilAnalysisClassify(
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
                "POST", "/openapi/v1/soil-analysis/classify", bodyStr);
        if (!authValid) {
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            Map<String, Object> normalizedParams = normalizeSoilParams(request);

            Double ph = getDoubleValue(normalizedParams, "ph");
            Double organicMatter = getDoubleValue(normalizedParams, "organicMatter");
            String texture = getStringValue(normalizedParams, "texture");
            String color = getStringValue(normalizedParams, "color");
            Double moisture = getDoubleValue(normalizedParams, "moisture");

            List<GeoStandard> standards = geoStandardMapper.selectList(
                    new LambdaQueryWrapper<GeoStandard>()
                            .eq(GeoStandard::getStatus, "ACTIVE")
                            .eq(GeoStandard::getIsDeleted, 0)
                            .in(GeoStandard::getCategory,
                                    GeoStandard.Category.SOIL_CHINA.name(),
                                    GeoStandard.Category.SOIL_WRB.name()));

            List<Map<String, Object>> results = new ArrayList<>();
            for (GeoStandard standard : standards) {
                Map<String, Object> result = calculateSoilSimilarity(ph, organicMatter, texture, color, moisture, standard);
                results.add(result);
            }

            results.sort((a, b) -> Double.compare(
                    ((Number) b.get("confidence")).doubleValue(),
                    ((Number) a.get("confidence")).doubleValue()));

            List<Map<String, Object>> topResults = results.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("matches", topResults);
            data.put("total", topResults.size());
            data.put("inputParams", normalizedParams);

            return buildSuccessResponse(data);
        } catch (Exception e) {
            log.error("AI土壤分类失败", e);
            return buildErrorResponse("分类失败: " + e.getMessage());
        }
    }

    @GetMapping("/soil-samples")
    public ResponseEntity<Map<String, Object>> getSoilSamples(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String soilType,
            @RequestParam(required = false) String status) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/soil-samples", null);
        if (!authValid) {
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            LambdaQueryWrapper<SoilSample> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SoilSample::getIsDeleted, 0);

            if (soilType != null && !soilType.isEmpty()) {
                wrapper.eq(SoilSample::getSoilType, soilType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(SoilSample::getStatus, status);
            }

            wrapper.orderByDesc(SoilSample::getCreatedTime);

            Page<SoilSample> pageParam = new Page<>(page, size);
            Page<SoilSample> resultPage = soilSampleMapper.selectPage(pageParam, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("page", page);
            data.put("size", size);
            data.put("pages", resultPage.getPages());

            return buildSuccessResponse(data);
        } catch (Exception e) {
            log.error("查询土壤采样数据失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/geo-standards")
    public ResponseEntity<Map<String, Object>> getGeoStandards(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestParam(required = false) String category) {

        boolean authValid = authService.validateSignature(apiKey, timestamp, signature,
                "GET", "/openapi/v1/geo-standards", null);
        if (!authValid) {
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            List<GeoStandard> standards;
            if (category != null && !category.isEmpty()) {
                standards = geoStandardMapper.selectByCategory(category);
            } else {
                standards = geoStandardMapper.selectList(null);
            }
            return buildSuccessResponse(Collections.singletonMap("standards", standards));
        } catch (Exception e) {
            log.error("查询地质标准数据失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
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
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return buildErrorResponse("标准不存在");
            }
            return buildSuccessResponse(Collections.singletonMap("standard", standard));
        } catch (Exception e) {
            log.error("查询标准详情失败, id={}", id, e);
            return buildErrorResponse("查询失败: " + e.getMessage());
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
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
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
            return buildSuccessResponse(data);
        } catch (Exception e) {
            log.error("土质分析数据查询失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
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
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
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
            return buildSuccessResponse(Collections.singletonMap("risks", risks));
        } catch (Exception e) {
            log.error("灾害风险数据查询失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
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
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        try {
            List<RockStratumAnalysis> analyses = rockStratumAnalysisMapper.selectAll();
            return buildSuccessResponse(Collections.singletonMap("analyses", analyses));
        } catch (Exception e) {
            log.error("岩层分析数据查询失败", e);
            return buildErrorResponse("查询失败: " + e.getMessage());
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
            return buildErrorResponse("认证失败：无效的API Key或签名");
        }

        if (!authService.checkRateLimit(apiKey)) {
            return buildErrorResponse("请求频率超过限制");
        }

        List<Map<String, Object>> apis = new ArrayList<>();

        Map<String, Object> api1 = new HashMap<>();
        api1.put("method", "GET");
        api1.put("path", "/soil-standards");
        api1.put("description", "土壤标准列表查询（分页）");
        api1.put("params", "category(可选): 分类; page: 页码; size: 每页数量");
        apis.add(api1);

        Map<String, Object> api2 = new HashMap<>();
        api2.put("method", "GET");
        api2.put("path", "/soil-standards/{id}");
        api2.put("description", "土壤标准详情");
        api2.put("params", "id: 标准ID");
        apis.add(api2);

        Map<String, Object> api3 = new HashMap<>();
        api3.put("method", "POST");
        api3.put("path", "/soil-analysis/classify");
        api3.put("description", "AI土壤分类（智能字段适配）");
        api3.put("params", "请求体: 土壤参数（ph/organicMatter/texture/color等）");
        apis.add(api3);

        Map<String, Object> api4 = new HashMap<>();
        api4.put("method", "GET");
        api4.put("path", "/soil-samples");
        api4.put("description", "土壤采样数据查询（分页）");
        api4.put("params", "soilType(可选): 土壤类型; status(可选): 状态; page: 页码; size: 每页数量");
        apis.add(api4);

        Map<String, Object> api5 = new HashMap<>();
        api5.put("method", "GET");
        api5.put("path", "/geo-standards");
        api5.put("description", "地质标准数据查询");
        api5.put("params", "category(可选): 分类");
        apis.add(api5);

        Map<String, Object> api6 = new HashMap<>();
        api6.put("method", "GET");
        api6.put("path", "/geo-standards/{id}");
        api6.put("description", "标准详情");
        api6.put("params", "id: 标准ID");
        apis.add(api6);

        Map<String, Object> api7 = new HashMap<>();
        api7.put("method", "POST");
        api7.put("path", "/soil-analysis/query");
        api7.put("description", "土质分析数据查询");
        api7.put("params", "请求体: 查询条件");
        apis.add(api7);

        Map<String, Object> api8 = new HashMap<>();
        api8.put("method", "GET");
        api8.put("path", "/disaster-risk/query");
        api8.put("description", "灾害风险数据查询");
        api8.put("params", "region(可选): 区域");
        apis.add(api8);

        Map<String, Object> api9 = new HashMap<>();
        api9.put("method", "POST");
        api9.put("path", "/rock-stratum/query");
        api9.put("description", "岩层分析数据查询");
        api9.put("params", "请求体: 查询条件");
        apis.add(api9);

        return buildSuccessResponse(Collections.singletonMap("apis", apis));
    }

    private Map<String, Object> normalizeSoilParams(Map<String, Object> request) {
        if (request == null) {
            return new HashMap<>();
        }

        Map<String, Object> normalized = new HashMap<>();

        Map<String, String[]> fieldAliases = new HashMap<>();
        fieldAliases.put("ph", new String[]{"ph", "pH", "PH", "phValue", "ph_value", "ph_val", "酸碱度", "pH值", "ph值"});
        fieldAliases.put("organicMatter", new String[]{"organicMatter", "organic_matter", "organicMatterContent", "organic_matter_content", "有机质", "有机碳", "OM", "om"});
        fieldAliases.put("moisture", new String[]{"moisture", "moistureContent", "moisture_content", "waterContent", "water_content", "含水率", "水分", "含水量", "湿度"});
        fieldAliases.put("texture", new String[]{"texture", "soilTexture", "soil_texture", "质地", "土壤质地", "机械组成"});
        fieldAliases.put("color", new String[]{"color", "soilColor", "soil_color", "颜色", "土壤颜色"});
        fieldAliases.put("nitrogen", new String[]{"nitrogen", "totalNitrogen", "total_nitrogen", "全氮", "总氮", "N"});
        fieldAliases.put("phosphorus", new String[]{"phosphorus", "totalPhosphorus", "total_phosphorus", "全磷", "总磷", "P"});
        fieldAliases.put("potassium", new String[]{"potassium", "totalPotassium", "total_potassium", "全钾", "总钾", "K"});

        for (Map.Entry<String, String[]> entry : fieldAliases.entrySet()) {
            String targetField = entry.getKey();
            String[] aliases = entry.getValue();

            for (String alias : aliases) {
                if (request.containsKey(alias)) {
                    normalized.put(targetField, request.get(alias));
                    break;
                }
            }
        }

        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            String keyLower = key.toLowerCase().replaceAll("[\\s_-]", "");
            boolean matched = false;

            for (Map.Entry<String, String[]> fieldEntry : fieldAliases.entrySet()) {
                for (String alias : fieldEntry.getValue()) {
                    String aliasLower = alias.toLowerCase().replaceAll("[\\s_-]", "");
                    if (keyLower.equals(aliasLower) || keyLower.contains(aliasLower) || aliasLower.contains(keyLower)) {
                        if (!normalized.containsKey(fieldEntry.getKey())) {
                            normalized.put(fieldEntry.getKey(), entry.getValue());
                            matched = true;
                            break;
                        }
                    }
                }
                if (matched) break;
            }
        }

        return normalized;
    }

    private Map<String, Object> calculateSoilSimilarity(Double ph, Double organicMatter,
                                                         String texture, String color,
                                                         Double moisture, GeoStandard standard) {
        Map<String, Object> result = new HashMap<>();
        result.put("standardId", standard.getStandardId());
        result.put("standardName", standard.getStandardName());
        result.put("standardCode", standard.getStandardCode());
        result.put("category", standard.getCategory());

        Map<String, Object> matchDetails = new HashMap<>();
        double totalScore = 0.0;
        double weightSum = 0.0;

        if (ph != null) {
            double phScore = calculatePhScore(ph, standard);
            matchDetails.put("phScore", Math.round(phScore * 100.0) / 100.0);
            matchDetails.put("phMatch", phScore >= 70);
            totalScore += phScore * 0.30;
            weightSum += 0.30;
        }

        if (organicMatter != null) {
            double omScore = calculateOrganicMatterScore(organicMatter, standard);
            matchDetails.put("organicMatterScore", Math.round(omScore * 100.0) / 100.0);
            matchDetails.put("organicMatterMatch", omScore >= 70);
            totalScore += omScore * 0.25;
            weightSum += 0.25;
        }

        if (texture != null && !texture.isEmpty()) {
            double textureScore = calculateTextureScore(texture, standard);
            matchDetails.put("textureScore", Math.round(textureScore * 100.0) / 100.0);
            matchDetails.put("textureMatch", textureScore >= 60);
            totalScore += textureScore * 0.25;
            weightSum += 0.25;
        }

        if (color != null && !color.isEmpty()) {
            double colorScore = calculateColorScore(color, standard);
            matchDetails.put("colorScore", Math.round(colorScore * 100.0) / 100.0);
            matchDetails.put("colorMatch", colorScore >= 80);
            totalScore += colorScore * 0.20;
            weightSum += 0.20;
        }

        if (moisture != null) {
            double moistureScore = Math.max(0, 100 - Math.abs(moisture - 25) * 2);
            matchDetails.put("moistureScore", Math.round(moistureScore * 100.0) / 100.0);
        }

        double confidence = weightSum > 0 ? (totalScore / weightSum) : 0.0;
        result.put("confidence", Math.round(confidence * 100.0) / 100.0);
        result.put("matchDetails", matchDetails);

        return result;
    }

    private double calculatePhScore(Double phValue, GeoStandard standard) {
        String chemical = standard.getChemicalComposition();
        if (chemical == null || chemical.isEmpty()) {
            return 60.0;
        }

        double targetPh = 7.0;
        if (chemical.contains("pH")) {
            try {
                String phStr = chemical.replaceAll(".*pH[：: ]*([\\d.]+).*", "$1");
                if (!phStr.equals(chemical)) {
                    targetPh = Double.parseDouble(phStr);
                }
            } catch (Exception e) {
                targetPh = 7.0;
            }
        }

        double diff = Math.abs(phValue - targetPh);
        if (diff <= 0.5) {
            return 90 + (0.5 - diff) * 20;
        } else if (diff <= 1.0) {
            return 70 + (1.0 - diff) * 40;
        } else if (diff <= 2.0) {
            return 50 + (2.0 - diff) * 20;
        } else {
            return Math.max(0, 50 - (diff - 2.0) * 10);
        }
    }

    private double calculateOrganicMatterScore(Double omValue, GeoStandard standard) {
        String chemical = standard.getChemicalComposition();
        if (chemical == null || chemical.isEmpty()) {
            return 60.0;
        }

        double targetOm = 2.5;
        if (chemical.contains("有机质") || chemical.contains("有机碳")) {
            try {
                String omStr = chemical.replaceAll(".*有机质[：: ]*([\\d.]+).*", "$1");
                if (!omStr.equals(chemical)) {
                    targetOm = Double.parseDouble(omStr);
                }
            } catch (Exception e) {
                targetOm = 2.5;
            }
        }

        if (targetOm <= 0) return 60.0;
        double diffPercent = Math.abs(omValue - targetOm) / targetOm;
        if (diffPercent <= 0.1) {
            return 90 + (0.1 - diffPercent) * 100;
        } else if (diffPercent <= 0.3) {
            return 70 + (0.3 - diffPercent) * 100;
        } else if (diffPercent <= 0.5) {
            return 50 + (0.5 - diffPercent) * 100;
        } else {
            return Math.max(0, 50 - (diffPercent - 0.5) * 50);
        }
    }

    private double calculateTextureScore(String texture, GeoStandard standard) {
        String textureDesc = standard.getTextureDescription();
        if (textureDesc == null || textureDesc.isEmpty()) {
            return 50.0;
        }

        String textureLower = texture.toLowerCase();
        String descLower = textureDesc.toLowerCase();

        if (descLower.contains(textureLower) || textureLower.contains(descLower)) {
            return 100.0;
        }

        Map<String, List<String>> similarGroups = new HashMap<>();
        similarGroups.put("壤土", Arrays.asList("粉壤土", "砂壤土", "粘壤土", "壤质"));
        similarGroups.put("粘土", Arrays.asList("粘壤土", "重粘土", "壤质粘土"));
        similarGroups.put("砂土", Arrays.asList("砂壤土", "壤质砂土", "粗砂土"));
        similarGroups.put("粉土", Arrays.asList("粉壤土", "壤质粉土", "粉质"));

        for (Map.Entry<String, List<String>> entry : similarGroups.entrySet()) {
            if (descLower.contains(entry.getKey())) {
                for (String similar : entry.getValue()) {
                    if (textureLower.contains(similar) || similar.contains(textureLower)) {
                        return 60.0;
                    }
                }
            }
        }

        return 30.0;
    }

    private double calculateColorScore(String color, GeoStandard standard) {
        String colorDesc = standard.getColorDescription();
        if (colorDesc == null || colorDesc.isEmpty()) {
            return 50.0;
        }

        String colorLower = color.toLowerCase();
        String descLower = colorDesc.toLowerCase();

        if (descLower.contains(colorLower) || colorLower.contains(descLower)) {
            return 100.0;
        }

        String[] colorFamilies = {"棕", "红", "黄", "黑", "灰", "白"};
        for (String family : colorFamilies) {
            if (descLower.contains(family) && colorLower.contains(family)) {
                return 70.0;
            }
        }

        return 30.0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        return value.toString();
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.badRequest().body(result);
    }
}
