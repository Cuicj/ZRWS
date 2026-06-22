package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.BoDefinition;
import com.zrws.approval.domain.entity.BoField;
import com.zrws.approval.domain.entity.DataImportBatch;
import com.zrws.approval.domain.entity.ApprovalFlowConfig;
import com.zrws.approval.dto.DataAnalysisRequest;
import com.zrws.approval.dto.DataAnalysisResponse;
import com.zrws.approval.dto.DataImportRequest;
import com.zrws.approval.dto.DataImportResponse;
import com.zrws.approval.mapper.BoDefinitionMapper;
import com.zrws.approval.mapper.BoFieldMapper;
import com.zrws.approval.mapper.DataImportBatchMapper;
import com.zrws.approval.service.AIAnalyzerService;
import com.zrws.approval.service.DataImportService;
import com.zrws.approval.service.ApprovalFlowConfigService;
import com.zrws.approval.service.DataStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析器 REST API
 * <p>提供数据导入、分析、校验的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dataanalyzer")
@CrossOrigin(origins = "*")
public class DataAnalyzerController {

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private AIAnalyzerService aiAnalyzerService;

    @Autowired
    private BoDefinitionMapper boDefinitionMapper;

    @Autowired
    private BoFieldMapper boFieldMapper;

    @Autowired
    private DataImportBatchMapper batchMapper;

    @Autowired
    private ApprovalFlowConfigService approvalFlowConfigService;

    @Autowired
    private DataStatisticsService dataStatisticsService;

    // ============================================================
    // 1. BO定义管理
    // ============================================================

    /**
     * 获取BO定义列表
     * GET /api/v1/dataanalyzer/bo
     */
    @GetMapping("/bo")
    public ResponseEntity<Map<String, Object>> listBoDefinitions(
            @RequestParam(required = false) String boType) {

        List<BoDefinition> definitions;
        if (boType != null && !boType.isEmpty()) {
            definitions = boDefinitionMapper.selectByBoType(boType);
        } else {
            definitions = boDefinitionMapper.selectActiveList();
        }

        return success(Map.of(
                "success", true,
                "list", definitions
        ));
    }

    /**
     * 获取BO定义详情
     * GET /api/v1/dataanalyzer/bo/{boCode}
     */
    @GetMapping("/bo/{boCode}")
    public ResponseEntity<Map<String, Object>> getBoDefinition(@PathVariable String boCode) {
        BoDefinition bo = boDefinitionMapper.selectByBoCode(boCode);
        if (bo == null) {
            return error("BO定义不存在: " + boCode);
        }

        // 获取字段配置
        List<BoField> fields = boFieldMapper.selectByBoId(bo.getBoId());
        bo.setFields(fields);

        return success(Map.of(
                "success", true,
                "bo", bo
        ));
    }

    /**
     * 获取BO字段配置
     * GET /api/v1/dataanalyzer/bo/{boCode}/fields
     */
    @GetMapping("/bo/{boCode}/fields")
    public ResponseEntity<Map<String, Object>> getBoFields(@PathVariable String boCode) {
        BoDefinition bo = boDefinitionMapper.selectByBoCode(boCode);
        if (bo == null) {
            return error("BO定义不存在: " + boCode);
        }

        List<BoField> fields = boFieldMapper.selectByBoId(bo.getBoId());

        return success(Map.of(
                "success", true,
                "fields", fields
        ));
    }

    // ============================================================
    // 2. 数据分析（预览）
    // ============================================================

    /**
     * 分析数据文件（预览）
     * POST /api/v1/dataanalyzer/analyze
     * Body: {"boCode": "SOIL_SAMPLE", "filePath": "/path/to/file.xlsx", "useAiEnhance": true}
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeData(@RequestBody DataAnalysisRequest request) {
        if (request.getBoCode() == null || request.getBoCode().isEmpty()) {
            return error("请提供BO编码");
        }

        if (request.getFilePath() == null && request.getFileContent() == null) {
            return error("请提供文件路径或文件内容");
        }

        try {
            log.info("开始数据分析: boCode={}, filePath={}", request.getBoCode(), request.getFilePath());

            DataAnalysisResponse response = dataImportService.analyzeData(request);

            if (response.getSuccess()) {
                return success(response);
            } else {
                return error(response.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("数据分析失败", e);
            return error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件并分析
     * POST /api/v1/dataanalyzer/upload-analyze
     * FormData: file, boCode, useAiEnhance
     */
    @PostMapping("/upload-analyze")
    public ResponseEntity<Map<String, Object>> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("boCode") String boCode,
            @RequestParam(value = "useAiEnhance", defaultValue = "true") Boolean useAiEnhance) {

        if (file.isEmpty()) {
            return error("请上传文件");
        }

        try {
            // 保存文件
            String filePath = saveUploadFile(file);

            // 构建请求
            DataAnalysisRequest request = new DataAnalysisRequest();
            request.setBoCode(boCode);
            request.setFilePath(filePath);
            request.setUseAiEnhance(useAiEnhance);

            // 分析数据
            DataAnalysisResponse response = dataImportService.analyzeData(request);

            if (response.getSuccess()) {
                return success(response);
            } else {
                return error(response.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("上传分析失败", e);
            return error("分析失败: " + e.getMessage());
        }
    }

    /**
     * AI智能字段映射
     * POST /api/v1/dataanalyzer/mapping
     * Body: {"boCode": "SOIL_SAMPLE", "headers": ["样本编号", "pH值", "纬度", "经度"]}
     */
    @PostMapping("/mapping")
    public ResponseEntity<Map<String, Object>> intelligentMapping(@RequestBody Map<String, Object> request) {
        String boCode = (String) request.get("boCode");
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) request.get("headers");

        if (boCode == null || headers == null) {
            return error("请提供BO编码和字段列表");
        }

        try {
            var mappings = aiAnalyzerService.intelligentFieldMapping(headers, boCode);

            return success(Map.of(
                    "success", true,
                    "mappings", mappings
            ));

        } catch (Exception e) {
            log.error("字段映射失败", e);
            return error("映射失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 3. 数据导入
    // ============================================================

    /**
     * 导入数据
     * POST /api/v1/dataanalyzer/import
     * Body: {"boCode": "SOIL_SAMPLE", "filePath": "/path/to/file.xlsx", "importMode": "INSERT_UPDATE"}
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestBody DataImportRequest request) {
        if (request.getBoCode() == null || request.getBoCode().isEmpty()) {
            return error("请提供BO编码");
        }

        if (request.getFilePath() == null && request.getFileContent() == null) {
            return error("请提供文件路径或文件内容");
        }

        try {
            log.info("开始数据导入: boCode={}, filePath={}", request.getBoCode(), request.getFilePath());

            DataImportResponse response = dataImportService.importData(request);

            if (response.getSuccess()) {
                return success(response);
            } else {
                return error("导入失败");
            }

        } catch (Exception e) {
            log.error("数据导入失败", e);
            return error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 上传并导入
     * POST /api/v1/dataanalyzer/upload-import
     * FormData: file, boCode, importMode, useAiMapping, autoFix
     */
    @PostMapping("/upload-import")
    public ResponseEntity<Map<String, Object>> uploadAndImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("boCode") String boCode,
            @RequestParam(value = "importMode", defaultValue = "INSERT_UPDATE") String importMode,
            @RequestParam(value = "useAiMapping", defaultValue = "true") Boolean useAiMapping,
            @RequestParam(value = "autoFix", defaultValue = "false") Boolean autoFix) {

        if (file.isEmpty()) {
            return error("请上传文件");
        }

        try {
            // 保存文件
            String filePath = saveUploadFile(file);

            // 构建请求
            DataImportRequest request = new DataImportRequest();
            request.setBoCode(boCode);
            request.setFilePath(filePath);
            request.setImportMode(importMode);
            request.setUseAiMapping(useAiMapping);
            request.setAutoFix(autoFix);

            // 导入数据
            DataImportResponse response = dataImportService.importData(request);

            if (response.getSuccess()) {
                return success(response);
            } else {
                return error("导入失败");
            }

        } catch (Exception e) {
            log.error("上传导入失败", e);
            return error("导入失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 4. 导入批次管理
    // ============================================================

    /**
     * 获取导入批次列表
     * GET /api/v1/dataanalyzer/batches
     */
    @GetMapping("/batches")
    public ResponseEntity<Map<String, Object>> listBatches(
            @RequestParam(required = false) String boCode,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String keyword) {

        List<DataImportBatch> batches;

        if (boCode != null && !boCode.isEmpty()) {
            batches = batchMapper.selectByBoCode(boCode);
        } else if (operatorId != null) {
            batches = batchMapper.selectByOperator(operatorId);
        } else {
            batches = batchMapper.selectList(null);
        }

        // 关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            batches = batches.stream()
                    .filter(b -> b.getBatchNo().toLowerCase().contains(keyword.toLowerCase()) ||
                                (b.getFileName() != null && b.getFileName().toLowerCase().contains(keyword.toLowerCase())))
                    .toList();
        }

        return success(Map.of(
                "success", true,
                "batches", batches
        ));
    }

    /**
     * 获取批次详情
     * GET /api/v1/dataanalyzer/batches/{batchNo}
     */
    @GetMapping("/batches/{batchNo}")
    public ResponseEntity<Map<String, Object>> getBatchDetail(@PathVariable String batchNo) {
        DataImportBatch batch = batchMapper.selectByBatchNo(batchNo);
        if (batch == null) {
            return error("批次不存在: " + batchNo);
        }

        return success(Map.of(
                "success", true,
                "batch", batch
        ));
    }

    // ============================================================
    // 5. AI分析增强
    // ============================================================

    /**
     * 生成AI分析报告
     * POST /api/v1/dataanalyzer/ai-report
     */
    @PostMapping("/ai-report")
    public ResponseEntity<Map<String, Object>> generateAIReport(@RequestBody DataAnalysisRequest request) {
        if (request.getBoCode() == null) {
            return error("请提供BO编码");
        }

        try {
            DataAnalysisResponse analysisResponse = dataImportService.analyzeData(request);

            if (!analysisResponse.getSuccess()) {
                return error(analysisResponse.getErrorMessage());
            }

            String report = aiAnalyzerService.generateAnalysisReport(request, analysisResponse);

            return success(Map.of(
                    "success", true,
                    "report", report,
                    "analysis", analysisResponse
            ));

        } catch (Exception e) {
            log.error("生成AI报告失败", e);
            return error("报告生成失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 6. 审批流配置管理
    // ============================================================

    /**
     * 获取所有审批流配置
     * GET /api/v1/dataanalyzer/approval-configs
     */
    @GetMapping("/approval-configs")
    public ResponseEntity<Map<String, Object>> listApprovalConfigs() {
        List<ApprovalFlowConfig> configs = approvalFlowConfigService.getAllConfigs();
        return success(Map.of(
                "success", true,
                "configs", configs
        ));
    }

    /**
     * 根据BO编码获取审批流配置
     * GET /api/v1/dataanalyzer/approval-configs/bo/{boCode}
     */
    @GetMapping("/approval-configs/bo/{boCode}")
    public ResponseEntity<Map<String, Object>> getApprovalConfigsByBo(@PathVariable String boCode) {
        List<ApprovalFlowConfig> configs = approvalFlowConfigService.getConfigsByBoCode(boCode);
        return success(Map.of(
                "success", true,
                "configs", configs
        ));
    }

    /**
     * 获取审批流配置详情
     * GET /api/v1/dataanalyzer/approval-config/{configId}
     */
    @GetMapping("/approval-config/{configId}")
    public ResponseEntity<Map<String, Object>> getApprovalConfig(@PathVariable Long configId) {
        ApprovalFlowConfig config = approvalFlowConfigService.getConfig(null, null);
        // 需要在Service中添加根据ID查询的方法
        return success(Map.of(
                "success", true,
                "config", config
        ));
    }

    /**
     * 保存审批流配置
     * POST /api/v1/dataanalyzer/approval-config
     */
    @PostMapping("/approval-config")
    public ResponseEntity<Map<String, Object>> saveApprovalConfig(@RequestBody ApprovalFlowConfig config) {
        try {
            ApprovalFlowConfig saved = approvalFlowConfigService.saveConfig(config);
            return success(Map.of(
                    "success", true,
                    "config", saved
            ));
        } catch (Exception e) {
            log.error("保存审批配置失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 更新审批流配置
     * PUT /api/v1/dataanalyzer/approval-config
     */
    @PutMapping("/approval-config")
    public ResponseEntity<Map<String, Object>> updateApprovalConfig(@RequestBody ApprovalFlowConfig config) {
        try {
            approvalFlowConfigService.updateConfig(config.getConfigId(), config);
            return success(Map.of(
                    "success", true,
                    "message", "更新成功"
            ));
        } catch (Exception e) {
            log.error("更新审批配置失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除审批流配置
     * DELETE /api/v1/dataanalyzer/approval-config/{configId}
     */
    @DeleteMapping("/approval-config/{configId}")
    public ResponseEntity<Map<String, Object>> deleteApprovalConfig(@PathVariable Long configId) {
        try {
            approvalFlowConfigService.deleteConfig(configId);
            return success(Map.of(
                    "success", true,
                    "message", "删除成功"
            ));
        } catch (Exception e) {
            log.error("删除审批配置失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换审批流启用状态
     * PATCH /api/v1/dataanalyzer/approval-config/{configId}/toggle
     */
    @PatchMapping("/approval-config/{configId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleApprovalConfig(@PathVariable Long configId,
                                                                   @RequestBody Map<String, Integer> body) {
        try {
            ApprovalFlowConfig config = new ApprovalFlowConfig();
            config.setConfigId(configId);
            config.setEnableApproval(body.get("enableApproval"));
            approvalFlowConfigService.updateConfig(configId, config);
            return success(Map.of(
                    "success", true,
                    "message", "操作成功"
            ));
        } catch (Exception e) {
            log.error("切换审批配置状态失败", e);
            return error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查操作是否需要审批
     * GET /api/v1/dataanalyzer/approval-config/check
     */
    @GetMapping("/approval-config/check")
    public ResponseEntity<Map<String, Object>> checkNeedApproval(
            @RequestParam String boCode,
            @RequestParam String operationType) {
        boolean needApproval = approvalFlowConfigService.needApproval(boCode, operationType);
        String processKey = approvalFlowConfigService.getApprovalProcessKey(boCode, operationType);

        return success(Map.of(
                "success", true,
                "needApproval", needApproval,
                "processKey", processKey
        ));
    }

    // ============================================================
    // 7. 数据统计
    // ============================================================

    /**
     * 获取统计汇总
     * GET /api/v1/dataanalyzer/stats/summary
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getStatsSummary() {
        try {
            Map<String, Object> importStats = dataStatisticsService.getImportStats();
            Map<String, Object> summary = dataStatisticsService.getSummary();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", importStats.get("total"));
            result.put("success", importStats.get("success"));
            result.put("failed", importStats.get("failed"));
            result.put("partial", importStats.get("partial"));
            result.put("totalRows", importStats.get("totalRows"));
            result.put("successRows", importStats.get("successRows"));
            result.put("failedRows", importStats.get("failedRows"));
            result.put("successRate", importStats.get("successRate"));
            result.put("dataSuccessRate", importStats.get("dataSuccessRate"));

            // 按BO统计
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> byBo = (List<Map<String, Object>>) summary.get("byBo");
            result.put("byBo", byBo);

            // 按日期统计（最近7天）
            result.put("byDate", summary.get("week"));

            return success(result);
        } catch (Exception e) {
            log.error("获取统计汇总失败", e);
            return error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日统计
     * GET /api/v1/dataanalyzer/stats/today
     */
    @GetMapping("/stats/today")
    public ResponseEntity<Map<String, Object>> getTodayStats(@RequestParam(required = false) String boCode) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("today", dataStatisticsService.getTodayStats(boCode));
            return success(result);
        } catch (Exception e) {
            log.error("获取今日统计失败", e);
            return error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取日期范围统计
     * GET /api/v1/dataanalyzer/stats/date-range
     */
    @GetMapping("/stats/date-range")
    public ResponseEntity<Map<String, Object>> getStatsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            return success(Map.of(
                    "success", true,
                    "stats", dataStatisticsService.getStatsByDateRange(startDate, endDate)
            ));
        } catch (Exception e) {
            log.error("获取日期范围统计失败", e);
            return error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取BO统计
     * GET /api/v1/dataanalyzer/stats/bo/{boCode}
     */
    @GetMapping("/stats/bo/{boCode}")
    public ResponseEntity<Map<String, Object>> getStatsByBoCode(@PathVariable String boCode) {
        try {
            return success(Map.of(
                    "success", true,
                    "stats", dataStatisticsService.getStatsByBoCode(boCode)
            ));
        } catch (Exception e) {
            log.error("获取BO统计失败", e);
            return error("获取统计失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 私有方法
    // ============================================================

    private String saveUploadFile(MultipartFile file) throws Exception {
        String uploadDir = System.getProperty("java.io.tmpdir") + "/dataimport/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.io.File destFile = new java.io.File(uploadDir + filename);
        file.transferTo(destFile);

        return destFile.getAbsolutePath();
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<?, ?>) data);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", message
        ));
    }
}