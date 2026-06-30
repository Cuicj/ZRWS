package com.zrws.approval.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.*;
import com.zrws.approval.dto.DataAnalysisRequest;
import com.zrws.approval.dto.DataAnalysisResponse;
import com.zrws.approval.dto.DataImportRequest;
import com.zrws.approval.dto.DataImportResponse;
import com.zrws.approval.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导入服务
 * <p>负责数据解析、校验、入库
 */
@Slf4j
@Service
public class DataImportService {

    @Autowired
    private BoDefinitionMapper boDefinitionMapper;

    @Autowired
    private BoFieldMapper boFieldMapper;

    @Autowired
    private DataImportBatchMapper batchMapper;

    @Autowired
    private DataImportDetailMapper detailMapper;

    @Autowired
    private AIAnalyzerService aiAnalyzerService;

    @Autowired
    private DataValidator dataValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.dataimport.upload-path:uploads/dataimport}")
    private String uploadPath;

    /**
     * 分析数据（预览）
     */
    public DataAnalysisResponse analyzeData(DataAnalysisRequest request) {
        DataAnalysisResponse response = new DataAnalysisResponse();
        response.setSuccess(false);

        try {
            BoDefinition bo = boDefinitionMapper.selectByBoCode(request.getBoCode());
            if (bo == null) {
                response.setErrorMessage("BO定义不存在: " + request.getBoCode());
                return response;
            }

            List<Map<String, Object>> dataRows = parseDataFile(request);
            if (dataRows.isEmpty()) {
                response.setErrorMessage("数据文件为空或解析失败");
                return response;
            }

            response.setTotalRows(dataRows.size());

            List<BoField> fieldConfigs = boFieldMapper.selectByBoId(bo.getBoId());
            List<String> headers = new ArrayList<>(dataRows.get(0).keySet());
            List<DataAnalysisResponse.FieldMapping> mappings = aiAnalyzerService.intelligentFieldMapping(headers, request.getBoCode());
            response.setFieldMappings(mappings);

            response.setDataPreview(dataRows.subList(0, Math.min(10, dataRows.size())));

            if (Boolean.TRUE.equals(request.getUseAiEnhance())) {
                DataAnalysisResponse.AiAnalysisResult aiResult = aiAnalyzerService.analyzeDataQuality(dataRows, fieldConfigs);
                response.setAiAnalysis(aiResult);
            }

            DataAnalysisResponse.ValidationSummary summary = performBasicValidation(dataRows, fieldConfigs);
            response.setValidationSummary(summary);

            DataImportBatch batch = createBatch(request, dataRows.size());
            response.setBatchNo(batch.getBatchNo());
            response.setBatchId(batch.getBatchId());

            response.setSuccess(true);
            response.setSuccessRows(0);

        } catch (Exception e) {
            log.error("数据分析失败", e);
            response.setErrorMessage("分析失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 导入数据
     */
    @Transactional(rollbackFor = Exception.class)
    public DataImportResponse importData(DataImportRequest request) {
        DataImportResponse response = new DataImportResponse();
        response.setSuccess(false);

        long startTime = System.currentTimeMillis();

        try {
            BoDefinition bo = boDefinitionMapper.selectByBoCode(request.getBoCode());
            if (bo == null) {
                response.setSuccess(false);
                return response;
            }

            List<Map<String, Object>> dataRows = parseImportFile(request);
            if (dataRows.isEmpty()) {
                return response;
            }

            response.setTotalRows(dataRows.size());

            List<BoField> fieldConfigs = boFieldMapper.selectByBoId(bo.getBoId());

            DataImportBatch batch = createImportBatch(request, dataRows.size());
            response.setBatchId(batch.getBatchId());
            response.setBatchNo(batch.getBatchNo());

            batch.setStatus("PROCESSING");
            batch.setStartTime(LocalDateTime.now());
            batchMapper.updateById(batch);

            Map<String, String> fieldMapping = new HashMap<>();
            if (Boolean.TRUE.equals(request.getUseAiMapping())) {
                List<String> headers = new ArrayList<>(dataRows.get(0).keySet());
                List<DataAnalysisResponse.FieldMapping> mappings =
                        aiAnalyzerService.intelligentFieldMapping(headers, request.getBoCode());

                for (DataAnalysisResponse.FieldMapping mapping : mappings) {
                    if (mapping.getTargetField() != null) {
                        fieldMapping.put(mapping.getSourceField(), mapping.getTargetField());
                    }
                }
            }

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            List<DataImportResponse.ImportDetail> details = new ArrayList<>();
            List<DataImportResponse.ErrorRow> errorRows = new ArrayList<>();

            for (int i = 0; i < dataRows.size(); i++) {
                Map<String, Object> row = dataRows.get(i);
                int rowNumber = i + 2;

                DataImportResponse.ImportDetail detail = new DataImportResponse.ImportDetail();
                detail.setRowNumber(rowNumber);

                try {
                    Map<String, Object> mappedRow = applyFieldMapping(row, fieldMapping);
                    DataValidator.ValidationResult validationResult = dataValidator.validateRow(mappedRow, fieldConfigs);

                    DataImportDetail importDetail = new DataImportDetail();
                    importDetail.setBatchId(batch.getBatchId());
                    importDetail.setRowNumber(rowNumber);
                    importDetail.setSourceData(objectMapper.writeValueAsString(row));
                    importDetail.setFieldMapping(objectMapper.writeValueAsString(fieldMapping));
                    importDetail.setValidationResult(objectMapper.writeValueAsString(validationResult));

                    if (!validationResult.isValid()) {
                        detail.setStatus("FAILED");
                        detail.setErrorMessage(validationResult.getErrors().get(0).getMessage());
                        importDetail.setStatus("FAILED");
                        importDetail.setErrorMessage(detail.getErrorMessage());
                        failedCount++;
                        errorRows.add(new DataImportResponse.ErrorRow() {{
                            setRowNumber(rowNumber);
                            setFieldName(validationResult.getErrors().get(0).getField());
                            setErrorValue(String.valueOf(validationResult.getErrors().get(0).getValue()));
                            setErrorMessage(validationResult.getErrors().get(0).getMessage());
                            setErrorType("VALIDATION");
                        }});
                    } else {
                        Long targetId = insertToTargetTable(bo.getTargetTable(), mappedRow, fieldConfigs, request.getImportMode());
                        if (targetId != null) {
                            detail.setStatus("SUCCESS");
                            detail.setTargetId(targetId);
                            importDetail.setStatus("SUCCESS");
                            importDetail.setTargetId(targetId);
                            successCount++;
                        } else {
                            detail.setStatus("SKIPPED");
                            importDetail.setStatus("SKIPPED");
                            skippedCount++;
                        }
                    }

                    detailMapper.insert(importDetail);
                    details.add(detail);

                } catch (Exception e) {
                    log.error("处理第{}行数据失败: {}", rowNumber, e.getMessage());
                    detail.setStatus("FAILED");
                    detail.setErrorMessage(e.getMessage());
                    failedCount++;
                    details.add(detail);
                }
            }

            batch.setSuccessRows(successCount);
            batch.setFailedRows(failedCount);
            batch.setSkippedRows(skippedCount);
            batch.setStatus(failedCount == 0 ? "SUCCESS" : (successCount == 0 ? "FAILED" : "PARTIAL"));
            batch.setEndTime(LocalDateTime.now());
            batch.setAiAnalysis(objectMapper.writeValueAsString(response.getDetails()));
            batchMapper.updateById(batch);

            response.setSuccess(true);
            response.setSuccessRows(successCount);
            response.setFailedRows(failedCount);
            response.setSkippedRows(skippedCount);
            response.setDuration((System.currentTimeMillis() - startTime) / 1000.0);
            response.setDetails(details);

            if (!errorRows.isEmpty()) {
                DataImportResponse.ErrorReport errorReport = new DataImportResponse.ErrorReport();
                errorReport.setTotalErrors(errorRows.size());
                errorReport.setErrorRows(errorRows);
                response.setErrorReport(errorReport);
            }

        } catch (Exception e) {
            log.error("数据导入失败", e);
            response.setSuccess(false);
        }

        return response;
    }

    /**
     * 土壤专用数据导入
     * <p>自动识别土壤检测字段并映射
     */
    public DataImportResponse importSoilData(DataImportRequest request) {
        DataImportResponse response = new DataImportResponse();
        response.setSuccess(false);

        long startTime = System.currentTimeMillis();

        try {
            List<Map<String, Object>> dataRows = parseImportFile(request);
            if (dataRows.isEmpty()) {
                response.setErrorMessage("数据文件为空或解析失败");
                return response;
            }

            response.setTotalRows(dataRows.size());

            Map<String, String> soilFieldMapping = buildSoilFieldMapping(dataRows.get(0).keySet());
            
            List<DataImportResponse.FieldMapping> fieldMappingList = new ArrayList<>();
            for (Map.Entry<String, String> entry : soilFieldMapping.entrySet()) {
                DataImportResponse.FieldMapping fm = new DataImportResponse.FieldMapping();
                fm.setExcelColumn(entry.getKey());
                fm.setTargetField(entry.getValue());
                fm.setMappingType("AUTO");
                fm.setConfidence(0.8);
                fieldMappingList.add(fm);
            }

            DataImportBatch batch = createSoilImportBatch(request, dataRows.size());
            response.setBatchId(batch.getBatchId());
            response.setBatchNo(batch.getBatchNo());

            batch.setStatus("PROCESSING");
            batch.setStartTime(LocalDateTime.now());
            batchMapper.updateById(batch);

            int successCount = 0;
            int failedCount = 0;
            List<DataImportResponse.ImportDetail> details = new ArrayList<>();

            for (int i = 0; i < dataRows.size(); i++) {
                Map<String, Object> row = dataRows.get(i);
                int rowNumber = i + 2;

                DataImportResponse.ImportDetail detail = new DataImportResponse.ImportDetail();
                detail.setRowNumber(rowNumber);

                try {
                    Map<String, Object> mappedRow = applySoilFieldMapping(row, soilFieldMapping);
                    validateSoilData(mappedRow);

                    detail.setStatus("SUCCESS");
                    successCount++;
                } catch (Exception e) {
                    detail.setStatus("FAILED");
                    detail.setErrorMessage(e.getMessage());
                    failedCount++;
                }

                details.add(detail);
            }

            batch.setSuccessRows(successCount);
            batch.setFailedRows(failedCount);
            batch.setSkippedRows(0);
            batch.setStatus(failedCount == 0 ? "SUCCESS" : (successCount == 0 ? "FAILED" : "PARTIAL"));
            batch.setEndTime(LocalDateTime.now());
            batchMapper.updateById(batch);

            response.setSuccess(true);
            response.setSuccessRows(successCount);
            response.setFailedRows(failedCount);
            response.setSkippedRows(0);
            response.setDuration((System.currentTimeMillis() - startTime) / 1000.0);
            response.setDetails(details);
            response.setFieldMapping(fieldMappingList);

        } catch (Exception e) {
            log.error("土壤数据导入失败", e);
            response.setSuccess(false);
            response.setErrorMessage("导入失败: " + e.getMessage());
        }

        return response;
    }

    private List<Map<String, Object>> parseDataFile(DataAnalysisRequest request) throws Exception {
        String filePath = request.getFilePath();
        String fileContent = request.getFileContent();

        if (filePath != null && !filePath.isEmpty()) {
            return parseExcelFile(new File(filePath));
        } else if (fileContent != null && !fileContent.isEmpty()) {
            byte[] bytes = Base64.getDecoder().decode(fileContent);
            File tempFile = File.createTempFile("import_", ".xlsx");
            FileUtil.writeBytes(bytes, tempFile);
            try {
                return parseExcelFile(tempFile);
            } finally {
                FileUtil.del(tempFile);
            }
        }

        return Collections.emptyList();
    }

    private List<Map<String, Object>> parseImportFile(DataImportRequest request) throws Exception {
        String filePath = request.getFilePath();
        String fileContent = request.getFileContent();

        if (filePath != null && !filePath.isEmpty()) {
            return parseExcelFile(new File(filePath));
        } else if (fileContent != null && !fileContent.isEmpty()) {
            byte[] bytes = Base64.getDecoder().decode(fileContent);
            File tempFile = File.createTempFile("import_", ".xlsx");
            FileUtil.writeBytes(bytes, tempFile);
            try {
                return parseExcelFile(tempFile);
            } finally {
                FileUtil.del(tempFile);
            }
        }

        return Collections.emptyList();
    }

    private List<Map<String, Object>> parseExcelFile(File file) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (InputStream is = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return rows;
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = getCellValueAsString(cell);
                headers.add(header != null ? header.trim() : "column_" + i);
            }

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                boolean hasData = false;

                for (int colNum = 0; colNum < headers.size(); colNum++) {
                    Cell cell = row.getCell(colNum);
                    Object value = getCellValue(cell);
                    String header = headers.get(colNum);

                    rowData.put(header, value);
                    if (value != null && !String.valueOf(value).trim().isEmpty()) {
                        hasData = true;
                    }
                }

                if (hasData) {
                    rows.add(rowData);
                }
            }
        }

        return rows;
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    return (long) numValue;
                }
                return numValue;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? String.valueOf(value) : null;
    }

    private Map<String, Object> applyFieldMapping(Map<String, Object> sourceData, Map<String, String> mapping) {
        if (mapping.isEmpty()) {
            return sourceData;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
            String targetField = mapping.get(entry.getKey());
            if (targetField != null) {
                result.put(targetField, entry.getValue());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private Map<String, String> buildSoilFieldMapping(Set<String> headers) {
        Map<String, String> mapping = new HashMap<>();

        Map<String, List<String>> fieldAliases = new HashMap<>();
        fieldAliases.put("sampleCode", Arrays.asList("样品编号", "样本编号", "sampleCode", "sample_code", "样品号", "编号"));
        fieldAliases.put("phValue", Arrays.asList("pH值", "ph", "pH", "phValue", "ph_value", "酸碱度", "PH值"));
        fieldAliases.put("organicMatter", Arrays.asList("有机质", "有机碳", "organicMatter", "organic_matter", "OM", "有机质含量"));
        fieldAliases.put("moisture", Arrays.asList("含水率", "水分", "moisture", "含水量", "湿度", "waterContent"));
        fieldAliases.put("soilTexture", Arrays.asList("质地", "土壤质地", "texture", "soilTexture", "soil_texture", "机械组成"));
        fieldAliases.put("soilType", Arrays.asList("土壤类型", "土类", "soilType", "soil_type", "类型"));
        fieldAliases.put("nitrogen", Arrays.asList("全氮", "氮", "nitrogen", "总氮", "N", "全N"));
        fieldAliases.put("phosphorus", Arrays.asList("全磷", "磷", "phosphorus", "总磷", "P", "全P"));
        fieldAliases.put("potassium", Arrays.asList("全钾", "钾", "potassium", "总钾", "K", "全K"));
        fieldAliases.put("ecValue", Arrays.asList("电导率", "EC值", "ecValue", "ec_value", "EC", "电导"));
        fieldAliases.put("latitude", Arrays.asList("纬度", "latitude", "lat", "北纬"));
        fieldAliases.put("longitude", Arrays.asList("经度", "longitude", "lon", "lng", "东经"));
        fieldAliases.put("elevation", Arrays.asList("海拔", "高程", "elevation", "altitude"));
        fieldAliases.put("depth", Arrays.asList("深度", "土层深度", "depth", "采样深度"));
        fieldAliases.put("collector", Arrays.asList("采集人", "采样人", "collector", "采样员"));
        fieldAliases.put("collectTime", Arrays.asList("采集时间", "采样时间", "collectTime", "collect_time", "时间"));
        fieldAliases.put("remark", Arrays.asList("备注", "说明", "remark", "comment", "note"));

        for (String header : headers) {
            String headerLower = header.toLowerCase().trim();
            String matchedField = null;

            for (Map.Entry<String, List<String>> entry : fieldAliases.entrySet()) {
                for (String alias : entry.getValue()) {
                    if (headerLower.equals(alias.toLowerCase()) ||
                        headerLower.contains(alias.toLowerCase()) ||
                        alias.toLowerCase().contains(headerLower)) {
                        matchedField = entry.getKey();
                        break;
                    }
                }
                if (matchedField != null) break;
            }

            if (matchedField != null) {
                mapping.put(header, matchedField);
            } else {
                mapping.put(header, header);
            }
        }

        return mapping;
    }

    private Map<String, Object> applySoilFieldMapping(Map<String, Object> sourceData, Map<String, String> mapping) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
            String targetField = mapping.get(entry.getKey());
            if (targetField != null) {
                result.put(targetField, convertSoilValue(targetField, entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private Object convertSoilValue(String field, Object value) {
        if (value == null) return null;
        String strValue = String.valueOf(value).trim();
        if (strValue.isEmpty()) return null;

        try {
            switch (field) {
                case "phValue":
                case "organicMatter":
                case "moisture":
                case "nitrogen":
                case "phosphorus":
                case "potassium":
                case "latitude":
                case "longitude":
                case "elevation":
                    return Double.parseDouble(strValue.replaceAll("[^\\d.]", ""));
                case "ecValue":
                    return Integer.parseInt(strValue.replaceAll("[^\\d]", ""));
                default:
                    return strValue;
            }
        } catch (Exception e) {
            return strValue;
        }
    }

    private void validateSoilData(Map<String, Object> data) {
        List<String> errors = new ArrayList<>();

        Object ph = data.get("phValue");
        if (ph != null) {
            double phVal = ((Number) ph).doubleValue();
            if (phVal < 0 || phVal > 14) {
                errors.add("pH值范围异常: " + phVal);
            }
        }

        Object om = data.get("organicMatter");
        if (om != null) {
            double omVal = ((Number) om).doubleValue();
            if (omVal < 0 || omVal > 100) {
                errors.add("有机质含量异常: " + omVal + "%");
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join("; ", errors));
        }
    }

    private DataImportBatch createSoilImportBatch(DataImportRequest request, int rowCount) {
        DataImportBatch batch = new DataImportBatch();
        batch.setBatchNo(generateBatchNo());
        batch.setBoCode("SOIL_SAMPLE");
        batch.setFileName(request.getFilePath());
        batch.setFilePath(request.getFilePath());
        batch.setTotalRows(rowCount);
        batch.setImportMode("INSERT");
        batch.setStatus("PENDING");
        batch.setOperatorId(request.getOperatorId());
        batch.setOperatorName(request.getOperatorName());
        batchMapper.insert(batch);
        return batch;
    }

    private DataAnalysisResponse.ValidationSummary performBasicValidation(
            List<Map<String, Object>> dataRows, List<BoField> fieldConfigs) {

        DataAnalysisResponse.ValidationSummary summary = new DataAnalysisResponse.ValidationSummary();
        summary.setTotalValidations(0);
        summary.setPassedValidations(0);
        summary.setFailedValidations(0);
        summary.setWarningValidations(0);
        summary.setErrorTypeStats(new HashMap<>());

        int totalChecks = 0;
        int passedChecks = 0;
        int failedChecks = 0;
        int warningChecks = 0;
        Map<String, Integer> errorStats = new HashMap<>();

        for (Map<String, Object> row : dataRows) {
            DataValidator.ValidationResult result = dataValidator.validateRow(row, fieldConfigs);

            totalChecks++;
            if (result.isValid()) {
                passedChecks++;
            } else {
                failedChecks++;
                for (var error : result.getErrors()) {
                    String type = error.getMessage().split(":")[0];
                    errorStats.put(type, errorStats.getOrDefault(type, 0) + 1);
                }
            }
            warningChecks += result.getWarnings().size();
        }

        summary.setTotalValidations(totalChecks);
        summary.setPassedValidations(passedChecks);
        summary.setFailedValidations(failedChecks);
        summary.setWarningValidations(warningChecks);
        summary.setErrorTypeStats(errorStats);

        return summary;
    }

    private DataImportBatch createBatch(DataAnalysisRequest request, int rowCount) {
        DataImportBatch batch = new DataImportBatch();
        batch.setBatchNo(generateBatchNo());
        batch.setBoId(request.getBoCode() != null ?
                boDefinitionMapper.selectByBoCode(request.getBoCode()).getBoId() : null);
        batch.setBoCode(request.getBoCode());
        batch.setFileName(request.getFilePath());
        batch.setTotalRows(rowCount);
        batch.setStatus("PENDING");
        batch.setOperatorId(request.getOperatorId());
        batch.setOperatorName(request.getOperatorName());

        batchMapper.insert(batch);
        return batch;
    }

    private DataImportBatch createImportBatch(DataImportRequest request, int rowCount) {
        DataImportBatch batch = new DataImportBatch();
        batch.setBatchNo(request.getBatchNo() != null ? request.getBatchNo() : generateBatchNo());
        batch.setBoId(boDefinitionMapper.selectByBoCode(request.getBoCode()).getBoId());
        batch.setBoCode(request.getBoCode());
        batch.setFileName(request.getFilePath());
        batch.setFilePath(request.getFilePath());
        batch.setTotalRows(rowCount);
        batch.setImportMode(request.getImportMode() != null ? request.getImportMode() : "INSERT_UPDATE");
        batch.setStatus("PENDING");
        batch.setOperatorId(request.getOperatorId());
        batch.setOperatorName(request.getOperatorName());

        batchMapper.insert(batch);
        return batch;
    }

    private Long insertToTargetTable(String tableName, Map<String, Object> data,
                                     List<BoField> fieldConfigs, String importMode) {
        try {
            List<String> columns = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (BoField field : fieldConfigs) {
                if (field.getStatus() != 1) continue;

                Object value = data.get(field.getFieldCode());
                columns.add(field.getTargetColumn());
                placeholders.add("?");

                value = convertValue(value, field.getFieldType());
                values.add(value);
            }

            columns.add("created_time");
            placeholders.add("NOW()");
            columns.add("updated_time");
            placeholders.add("NOW()");

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName, String.join(", ", columns), String.join(", ", placeholders));

            jdbcTemplate.update(sql, values.toArray());

            return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        } catch (Exception e) {
            log.error("插入数据失败: table={}, error={}", tableName, e.getMessage());
            throw e;
        }
    }

    private Object convertValue(Object value, String fieldType) {
        if (value == null) {
            return null;
        }

        String strValue = String.valueOf(value).trim();
        if (strValue.isEmpty()) {
            return null;
        }

        try {
            switch (fieldType.toUpperCase()) {
                case "INT":
                case "BIGINT":
                    return Long.parseLong(strValue);
                case "DECIMAL":
                case "DOUBLE":
                case "FLOAT":
                    return new BigDecimal(strValue);
                case "BOOLEAN":
                    return "true".equalsIgnoreCase(strValue) || "1".equals(strValue) ||
                           "是".equals(strValue) || "yes".equalsIgnoreCase(strValue);
                case "DATE":
                case "DATETIME":
                    return cn.hutool.core.date.DateUtil.parse(strValue);
                default:
                    return strValue;
            }
        } catch (Exception e) {
            log.warn("值转换失败: {} -> {}, 使用原值", strValue, fieldType);
            return strValue;
        }
    }

    private String generateBatchNo() {
        return "IMP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", new Random().nextInt(10000));
    }
}
