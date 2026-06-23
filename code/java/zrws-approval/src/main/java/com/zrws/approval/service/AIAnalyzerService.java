package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.BoDefinition;
import com.zrws.approval.domain.entity.BoField;
import com.zrws.approval.dto.DataAnalysisRequest;
import com.zrws.approval.dto.DataAnalysisResponse;
import com.zrws.approval.mapper.BoDefinitionMapper;
import com.zrws.approval.mapper.BoFieldMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI数据分析服务
 * <p>使用OpenAI API进行数据智能分析、字段映射、校验建议
 */
@Slf4j
@Service
public class AIAnalyzerService {

    @Autowired
    private BoDefinitionMapper boDefinitionMapper;

    @Autowired
    private BoFieldMapper boFieldMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${spring.ai.analyzer.temperature:0.3}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.model:gpt-3.5-turbo}")
    private String chatModel;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 智能字段映射
     */
    public List<DataAnalysisResponse.FieldMapping> intelligentFieldMapping(
            List<String> sourceHeaders,
            String boCode) {

        BoDefinition bo = boDefinitionMapper.selectByBoCode(boCode);
        if (bo == null) {
            log.error("BO定义不存在: {}", boCode);
            return Collections.emptyList();
        }

        List<BoField> targetFields = boFieldMapper.selectByBoId(bo.getBoId());
        String prompt = buildFieldMappingPrompt(sourceHeaders, targetFields, bo);

        try {
            String response = callOpenAI(prompt);
            return parseFieldMappingResponse(response, sourceHeaders);
        } catch (Exception e) {
            log.error("AI字段映射失败: {}", e.getMessage());
            return fallbackFieldMapping(sourceHeaders, targetFields);
        }
    }

    /**
     * 数据质量分析
     */
    public DataAnalysisResponse.AiAnalysisResult analyzeDataQuality(
            List<Map<String, Object>> dataRows,
            List<BoField> fieldConfigs) {

        String prompt = buildDataQualityPrompt(dataRows, fieldConfigs);

        try {
            String response = callOpenAI(prompt);
            return parseAiAnalysisResult(response);
        } catch (Exception e) {
            log.error("AI数据分析失败: {}", e.getMessage());
            return createBasicAnalysis(dataRows, fieldConfigs);
        }
    }

    /**
     * 智能数据校验
     */
    public Map<String, Object> smartValidate(
            Map<String, Object> rowData,
            List<BoField> fieldConfigs,
            String boCode) {

        String prompt = buildSmartValidationPrompt(rowData, fieldConfigs, boCode);

        try {
            String response = callOpenAI(prompt);
            return parseValidationResult(response);
        } catch (Exception e) {
            log.error("AI智能校验失败: {}", e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    /**
     * 生成数据分析报告
     */
    public String generateAnalysisReport(DataAnalysisRequest request, DataAnalysisResponse response) {
        String prompt = String.format(
            "请为以下数据分析生成一份详细报告：\n\n" +
            "数据概览：\n" +
            "- BO类型：%s\n" +
            "- 总行数：%d\n" +
            "- 成功行数：%d\n" +
            "- 失败行数：%d\n\n" +
            "字段映射：%d 个字段已映射\n\n" +
            "校验结果：\n" +
            "- 通过：%d\n" +
            "- 失败：%d\n" +
            "- 警告：%d\n\n" +
            "请以Markdown格式输出，包含：\n" +
            "1. 数据质量评分（0-100）\n" +
            "2. 主要问题列表\n" +
            "3. 数据分布统计\n" +
            "4. 改进建议",
                request.getBoCode(),
                response.getTotalRows() != null ? response.getTotalRows() : 0,
                response.getSuccessRows(),
                response.getFailedRows(),
                response.getFieldMappings() != null ? response.getFieldMappings().size() : 0,
                response.getValidationSummary() != null ? response.getValidationSummary().getPassedValidations() : 0,
                response.getValidationSummary() != null ? response.getValidationSummary().getFailedValidations() : 0,
                response.getValidationSummary() != null ? response.getValidationSummary().getWarningValidations() : 0
        );

        try {
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("生成分析报告失败: {}", e.getMessage());
            return "报告生成失败: " + e.getMessage();
        }
    }

    // ==================== 私有方法 ====================

    private String buildFieldMappingPrompt(List<String> headers, List<BoField> targetFields, BoDefinition bo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是数据匹配专家。请根据以下信息，将源数据的列名匹配到目标BO字段。\n\n");
        prompt.append("## 源数据列名：\n");
        for (int i = 0; i < headers.size(); i++) {
            prompt.append(String.format("%d. %s\n", i + 1, headers.get(i)));
        }
        prompt.append("\n## 目标BO定义：\n");
        prompt.append(String.format("- BO名称：%s\n", bo.getBoName()));
        prompt.append(String.format("- 目标表：%s\n", bo.getTargetTable()));
        if (bo.getAiPrompt() != null && !bo.getAiPrompt().isEmpty()) {
            prompt.append(String.format("- 业务说明：%s\n", bo.getAiPrompt()));
        }
        prompt.append("\n## 目标字段配置：\n");
        for (BoField field : targetFields) {
            prompt.append(String.format("- %s (%s): %s | 可能列名: %s\n",
                    field.getFieldName(),
                    field.getFieldType(),
                    field.getFieldCode(),
                    field.getSourceNames()));
        }
        prompt.append("\n## 输出要求：\n");
        prompt.append("请以JSON格式输出字段映射结果，格式如下：\n");
        prompt.append("[\n  {\"sourceField\": \"源列名\", \"targetField\": \"目标字段编码\", \"confidence\": 0.95, \"description\": \"匹配说明\"},\n  ...\n]\n");
        prompt.append("如果某个源列无法匹配，返回null即可。\n");
        prompt.append("只输出JSON，不要其他内容。");
        return prompt.toString();
    }

    private String buildDataQualityPrompt(List<Map<String, Object>> dataRows, List<BoField> fieldConfigs) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是数据分析专家。请分析以下数据的质量并识别问题。\n\n");

        int sampleSize = Math.min(dataRows.size(), 50);
        List<Map<String, Object>> sampleData = dataRows.subList(0, sampleSize);

        prompt.append(String.format("## 数据样本（共 %d 行，展示前 %d 行）：\n", dataRows.size(), sampleSize));
        try {
            prompt.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleData));
        } catch (Exception e) {
            log.error("序列化样本数据失败", e);
        }

        prompt.append("\n## 字段配置：\n");
        for (BoField field : fieldConfigs) {
            String validation = field.getValidationRule() != null ? field.getValidationRule() : "无";
            prompt.append(String.format("- %s: 类型=%s, 必填=%s, 校验规则=%s\n",
                    field.getFieldName(), field.getFieldType(),
                    field.getIsRequired() == 1 ? "是" : "否",
                    validation));
        }

        prompt.append("\n## 输出要求：\n");
        prompt.append("请以JSON格式输出分析结果：\n");
        prompt.append("{\n  \"qualityScore\": 85,\n  \"dataDistribution\": {\"字段名\": {\"正常\": 100, \"异常\": 5}},\n  \"anomalies\": [{\"rowNumber\": 5, \"fieldName\": \"xxx\", \"value\": \"xxx\", \"anomalyType\": \"范围超限\", \"description\": \"...\"}],\n  \"suggestions\": [\"建议1\", \"建议2\"],\n  \"problems\": [{\"type\": \"数据类型错误\", \"description\": \"...\", \"affectedRows\": 10, \"severity\": \"MEDIUM\"}]\n}\n");
        prompt.append("只输出JSON。");
        return prompt.toString();
    }

    private String buildSmartValidationPrompt(Map<String, Object> rowData, List<BoField> fieldConfigs, String boCode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请校验以下数据行的有效性：\n\n");
        try {
            prompt.append("## 数据行：\n");
            prompt.append(objectMapper.writeValueAsString(rowData));
        } catch (Exception e) {
            prompt.append(rowData.toString());
        }
        prompt.append("\n## 字段配置：\n");
        for (BoField field : fieldConfigs) {
            prompt.append(String.format("- %s: %s\n", field.getFieldCode(), field.getFieldType()));
        }
        prompt.append("\n## 输出要求：\n");
        prompt.append("JSON格式校验结果：\n");
        prompt.append("{\n  \"isValid\": true,\n  \"errors\": [{\"field\": \"xxx\", \"value\": \"xxx\", \"message\": \"错误信息\"}],\n  \"warnings\": [{\"field\": \"xxx\", \"value\": \"xxx\", \"message\": \"警告信息\"}],\n  \"suggestions\": [\"修复建议\"]\n}\n");
        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API Key未配置");
        }

        String url = baseUrl + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", chatModel);
        requestBody.put("temperature", temperature);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Collections.singletonMap("role", "user"));
        messages.get(0).put("content", prompt);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        String responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new RuntimeException("OpenAI API返回为空");
        }

        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI API返回choices为空");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private List<DataAnalysisResponse.FieldMapping> parseFieldMappingResponse(String response, List<String> headers) {
        List<DataAnalysisResponse.FieldMapping> mappings = new ArrayList<>();
        try {
            String jsonStr = extractJson(response);
            if (jsonStr != null && !jsonStr.isEmpty()) {
                List<Map> parsedMappings = objectMapper.readValue(jsonStr, List.class);
                for (Map item : parsedMappings) {
                    if (item == null) continue;
                    DataAnalysisResponse.FieldMapping mapping = new DataAnalysisResponse.FieldMapping();
                    mapping.setSourceField((String) item.get("sourceField"));
                    mapping.setTargetField((String) item.get("targetField"));
                    Object confidence = item.get("confidence");
                    if (confidence instanceof Number) {
                        mapping.setConfidence(((Number) confidence).doubleValue());
                    }
                    mapping.setDescription((String) item.get("description"));
                    mappings.add(mapping);
                }
            }
        } catch (Exception e) {
            log.error("解析字段映射响应失败: {}", e.getMessage());
        }
        if (mappings.isEmpty()) {
            mappings = fallbackFieldMapping(headers, headers);
        }
        return mappings;
    }

    private List<DataAnalysisResponse.FieldMapping> fallbackFieldMapping(List<String> headers, List<String> possibleTargets) {
        return fallbackFieldMappingByKeyword(headers, possibleTargets);
    }

    private List<DataAnalysisResponse.FieldMapping> fallbackFieldMappingByKeyword(List<String> headers, List<String> possibleTargets) {
        List<DataAnalysisResponse.FieldMapping> mappings = new ArrayList<>();
        for (String header : headers) {
            DataAnalysisResponse.FieldMapping mapping = new DataAnalysisResponse.FieldMapping();
            mapping.setSourceField(header);
            mapping.setConfidence(0.5);
            mapping.setDescription("基于关键词匹配");

            String headerLower = header.toLowerCase().replaceAll("[\\s_-]", "");
            for (String target : possibleTargets) {
                String targetLower = target.toLowerCase().replaceAll("[\\s_-]", "");
                if (headerLower.contains(targetLower) || targetLower.contains(headerLower)) {
                    mapping.setTargetField(target);
                    mapping.setConfidence(0.7);
                    mapping.setDescription("关键词模糊匹配");
                    break;
                }
            }
            if (mapping.getTargetField() == null) {
                mapping.setTargetField(header);
            }
            mappings.add(mapping);
        }
        return mappings;
    }

    private DataAnalysisResponse.AiAnalysisResult parseAiAnalysisResult(String response) {
        DataAnalysisResponse.AiAnalysisResult result = new DataAnalysisResponse.AiAnalysisResult();
        try {
            String jsonStr = extractJson(response);
            if (jsonStr != null) {
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);
                result.setQualityScore(getIntValue(parsed.get("qualityScore"), 0));

                if (parsed.get("dataDistribution") instanceof Map) {
                    result.setDataDistribution((Map<String, Object>) parsed.get("dataDistribution"));
                }
                if (parsed.get("anomalies") instanceof List) {
                    List<Map> anomalies = (List<Map>) parsed.get("anomalies");
                    result.setAnomalies(anomalies.stream().map(a -> {
                        DataAnalysisResponse.AnomalyItem item = new DataAnalysisResponse.AnomalyItem();
                        item.setRowNumber(getIntValue(a.get("rowNumber"), 0));
                        item.setFieldName((String) a.get("fieldName"));
                        item.setValue(String.valueOf(a.get("value")));
                        item.setAnomalyType((String) a.get("anomalyType"));
                        item.setDescription((String) a.get("description"));
                        return item;
                    }).toList());
                }
                if (parsed.get("suggestions") instanceof List) {
                    result.setSuggestions((List<String>) parsed.get("suggestions"));
                }
                if (parsed.get("problems") instanceof List) {
                    List<Map> problems = (List<Map>) parsed.get("problems");
                    result.setProblems(problems.stream().map(p -> {
                        DataAnalysisResponse.ProblemItem item = new DataAnalysisResponse.ProblemItem();
                        item.setType((String) p.get("type"));
                        item.setDescription((String) p.get("description"));
                        item.setAffectedRows(getIntValue(p.get("affectedRows"), 0));
                        item.setSeverity((String) p.get("severity"));
                        return item;
                    }).toList());
                }
            }
        } catch (Exception e) {
            log.error("解析AI分析结果失败: {}", e.getMessage());
            result.setQualityScore(0);
            result.setSuggestions(Collections.singletonList("分析失败，请检查数据格式"));
        }
        return result;
    }

    private Map<String, Object> parseValidationResult(String response) {
        try {
            String jsonStr = extractJson(response);
            if (jsonStr != null) {
                return objectMapper.readValue(jsonStr, Map.class);
            }
        } catch (Exception e) {
            log.error("解析校验结果失败: {}", e.getMessage());
        }
        return Collections.singletonMap("error", "解析失败");
    }

    private DataAnalysisResponse.AiAnalysisResult createBasicAnalysis(List<Map<String, Object>> dataRows, List<BoField> fieldConfigs) {
        DataAnalysisResponse.AiAnalysisResult result = new DataAnalysisResponse.AiAnalysisResult();
        result.setQualityScore(70);
        result.setDataDistribution(new HashMap<>());
        result.setAnomalies(new ArrayList<>());
        result.setSuggestions(Arrays.asList(
                "建议检查数据格式是否符合预期",
                "部分字段可能需要额外校验"
        ));
        result.setProblems(new ArrayList<>());
        return result;
    }

    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        int startIdx = response.indexOf("```json");
        if (startIdx >= 0) {
            startIdx += 7;
        } else {
            startIdx = response.indexOf("```");
            if (startIdx >= 0) {
                startIdx += 3;
            } else {
                startIdx = 0;
            }
        }
        int endIdx = response.lastIndexOf("```");
        if (endIdx > startIdx) {
            return response.substring(startIdx, endIdx).trim();
        }
        int jsonStart = response.indexOf('[');
        if (jsonStart < 0) {
            jsonStart = response.indexOf('{');
        }
        if (jsonStart >= 0) {
            return response.substring(jsonStart).trim();
        }
        return response.trim();
    }

    private Integer getIntValue(Object value, Integer defaultVal) {
        if (value == null) return defaultVal;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
