package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableDeployService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable 流程部署 REST API 控制器
 * <p>提供流程定义的部署、查询、删除、版本管理等接口
 */
@RestController
@RequestMapping("/api/v1/flowable")
@CrossOrigin(origins = "*")
public class FlowableDeployController {

    @Autowired
    private FlowableDeployService deployService;

    // ============================================================
    // 1. 部署管理
    // ============================================================

    /**
     * 部署BPMN文件
     * POST /api/v1/flowable/deploy/file
     */
    @PostMapping("/deploy/file")
    public ResponseEntity<Map<String, Object>> deployFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return error("请选择要上传的BPMN文件");
        }
        Map<String, Object> result = deployService.deployFromFile(file);
        return success(result);
    }

    /**
     * 批量部署classpath下所有BPMN文件
     * POST /api/v1/flowable/deploy/batch
     */
    @PostMapping("/deploy/batch")
    public ResponseEntity<Map<String, Object>> deployBatch() {
        List<Map<String, Object>> results = deployService.deployAllFromClasspath();
        Map<String, Object> result = new HashMap<>();
        result.put("total", results.size());
        result.put("deployments", results);
        return success(result);
    }

    /**
     * 动态创建流程定义
     * POST /api/v1/flowable/deploy/create
     */
    @PostMapping("/deploy/create")
    public ResponseEntity<Map<String, Object>> createProcess(@RequestBody Map<String, Object> body) {
        String processKey = (String) body.get("processKey");
        String processName = (String) body.get("processName");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> steps = (List<Map<String, String>>) body.get("steps");

        if (processKey == null || processName == null || steps == null) {
            return error("缺少必要参数");
        }

        Map<String, Object> result = deployService.createStandardProcess(processKey, processName, steps);
        return success(result);
    }

    // ============================================================
    // 2. 查询流程定义
    // ============================================================

    /**
     * 查询所有流程定义
     * GET /api/v1/flowable/definitions
     */
    @GetMapping("/definitions")
    public ResponseEntity<Map<String, Object>> listDefinitions() {
        List<Map<String, Object>> list = deployService.listProcessDefinitions();
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("list", list);
        return success(result);
    }

    /**
     * 查询单个流程定义
     * GET /api/v1/flowable/definitions/{processDefinitionId}
     */
    @GetMapping("/definitions/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> getDefinition(@PathVariable String processDefinitionId) {
        Map<String, Object> definition = deployService.getProcessDefinition(processDefinitionId);
        if (definition == null) {
            return error("流程定义不存在");
        }
        return success(definition);
    }

    /**
     * 获取流程定义的BPMN XML
     * GET /api/v1/flowable/definitions/{processDefinitionId}/xml
     */
    @GetMapping("/definitions/{processDefinitionId}/xml")
    public ResponseEntity<Map<String, Object>> getDefinitionXML(@PathVariable String processDefinitionId) {
        String xml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (xml == null) {
            return error("流程定义不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("xml", xml);
        return success(result);
    }

    /**
     * 获取流程版本列表
     * GET /api/v1/flowable/definitions/{processKey}/versions
     */
    @GetMapping("/definitions/{processKey}/versions")
    public ResponseEntity<Map<String, Object>> getVersions(@PathVariable String processKey) {
        List<Map<String, Object>> versions = deployService.getProcessVersions(processKey);
        Map<String, Object> result = new HashMap<>();
        result.put("processKey", processKey);
        result.put("total", versions.size());
        result.put("versions", versions);
        return success(result);
    }

    // ============================================================
    // 3. 删除/更新流程
    // ============================================================

    /**
     * 删除流程部署
     * DELETE /api/v1/flowable/deployments/{deploymentId}
     */
    @DeleteMapping("/deployments/{deploymentId}")
    public ResponseEntity<Map<String, Object>> deleteDeployment(@PathVariable String deploymentId) {
        deployService.deleteDeployment(deploymentId);
        return successMsg("删除成功");
    }

    /**
     * 根据流程key删除所有版本
     * DELETE /api/v1/flowable/deployments/key/{processKey}
     */
    @DeleteMapping("/deployments/key/{processKey}")
    public ResponseEntity<Map<String, Object>> deleteByKey(@PathVariable String processKey) {
        deployService.deleteDeploymentByKey(processKey);
        return successMsg("删除成功");
    }

    /**
     * 挂起流程定义
     * POST /api/v1/flowable/definitions/{processDefinitionId}/suspend
     */
    @PostMapping("/definitions/{processDefinitionId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendDefinition(@PathVariable String processDefinitionId) {
        deployService.suspendProcessDefinition(processDefinitionId);
        return successMsg("已挂起");
    }

    /**
     * 激活流程定义
     * POST /api/v1/flowable/definitions/{processDefinitionId}/activate
     */
    @PostMapping("/definitions/{processDefinitionId}/activate")
    public ResponseEntity<Map<String, Object>> activateDefinition(@PathVariable String processDefinitionId) {
        deployService.activateProcessDefinition(processDefinitionId);
        return successMsg("已激活");
    }

    // ============================================================
    // 4. 统计信息
    // ============================================================

    /**
     * 获取流程引擎统计信息
     * GET /api/v1/flowable/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = deployService.getDeploymentStats();
        return success(stats);
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("msg", "success");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> successMsg(String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("msg", msg);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", -1);
        body.put("msg", msg);
        return ResponseEntity.ok(body);
    }
}
