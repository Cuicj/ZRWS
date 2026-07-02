package com.zrws.approval.controller;

import com.zrws.approval.service.FlowableDeployService;
import com.zrws.common.core.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public R<Map<String, Object>> deployFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.fail("请选择要上传的BPMN文件");
        }
        Map<String, Object> result = deployService.deployFromFile(file);
        return R.ok(result);
    }

    /**
     * 批量部署classpath下所有BPMN文件
     * POST /api/v1/flowable/deploy/batch
     */
    @PostMapping("/deploy/batch")
    public R<Map<String, Object>> deployBatch() {
        List<Map<String, Object>> results = deployService.deployAllFromClasspath();
        return R.ok(Map.of("total", results.size(), "deployments", results));
    }

    /**
     * 动态创建流程定义
     * POST /api/v1/flowable/deploy/create
     */
    @PostMapping("/deploy/create")
    public R<Map<String, Object>> createProcess(@RequestBody Map<String, Object> body) {
        String processKey = (String) body.get("processKey");
        String processName = (String) body.get("processName");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> steps = (List<Map<String, String>>) body.get("steps");

        if (processKey == null || processName == null || steps == null) {
            return R.fail("缺少必要参数");
        }

        Map<String, Object> result = deployService.createStandardProcess(processKey, processName, steps);
        return R.ok(result);
    }

    // ============================================================
    // 2. 查询流程定义
    // ============================================================

    /**
     * 查询所有流程定义
     * GET /api/v1/flowable/definitions
     */
    @GetMapping("/definitions")
    public R<Map<String, Object>> listDefinitions() {
        List<Map<String, Object>> list = deployService.listProcessDefinitions();
        return R.ok(Map.of("total", list.size(), "list", list));
    }

    /**
     * 查询单个流程定义
     * GET /api/v1/flowable/definitions/{processDefinitionId}
     */
    @GetMapping("/definitions/{processDefinitionId}")
    public R<Map<String, Object>> getDefinition(@PathVariable String processDefinitionId) {
        Map<String, Object> definition = deployService.getProcessDefinition(processDefinitionId);
        if (definition == null) {
            return R.fail("流程定义不存在");
        }
        return R.ok(definition);
    }

    /**
     * 获取流程定义的BPMN XML
     * GET /api/v1/flowable/definitions/{processDefinitionId}/xml
     */
    @GetMapping("/definitions/{processDefinitionId}/xml")
    public R<Map<String, Object>> getDefinitionXML(@PathVariable String processDefinitionId) {
        String xml = deployService.getProcessDefinitionXML(processDefinitionId);
        if (xml == null) {
            return R.fail("流程定义不存在");
        }
        return R.ok(Map.of("xml", xml));
    }

    /**
     * 获取流程版本列表
     * GET /api/v1/flowable/definitions/{processKey}/versions
     */
    @GetMapping("/definitions/{processKey}/versions")
    public R<Map<String, Object>> getVersions(@PathVariable String processKey) {
        List<Map<String, Object>> versions = deployService.getProcessVersions(processKey);
        return R.ok(Map.of("processKey", processKey, "total", versions.size(), "versions", versions));
    }

    // ============================================================
    // 3. 删除/更新流程
    // ============================================================

    /**
     * 删除流程部署
     * DELETE /api/v1/flowable/deployments/{deploymentId}
     */
    @DeleteMapping("/deployments/{deploymentId}")
    public R<String> deleteDeployment(@PathVariable String deploymentId) {
        deployService.deleteDeployment(deploymentId);
        return R.ok("删除成功");
    }

    /**
     * 根据流程key删除所有版本
     * DELETE /api/v1/flowable/deployments/key/{processKey}
     */
    @DeleteMapping("/deployments/key/{processKey}")
    public R<String> deleteByKey(@PathVariable String processKey) {
        deployService.deleteDeploymentByKey(processKey);
        return R.ok("删除成功");
    }

    /**
     * 挂起流程定义
     * POST /api/v1/flowable/definitions/{processDefinitionId}/suspend
     */
    @PostMapping("/definitions/{processDefinitionId}/suspend")
    public R<String> suspendDefinition(@PathVariable String processDefinitionId) {
        deployService.suspendProcessDefinition(processDefinitionId);
        return R.ok("已挂起");
    }

    /**
     * 激活流程定义
     * POST /api/v1/flowable/definitions/{processDefinitionId}/activate
     */
    @PostMapping("/definitions/{processDefinitionId}/activate")
    public R<String> activateDefinition(@PathVariable String processDefinitionId) {
        deployService.activateProcessDefinition(processDefinitionId);
        return R.ok("已激活");
    }

    // ============================================================
    // 4. 统计信息
    // ============================================================

    /**
     * 获取流程引擎统计信息
     * GET /api/v1/flowable/stats
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = deployService.getDeploymentStats();
        return R.ok(stats);
    }
}
