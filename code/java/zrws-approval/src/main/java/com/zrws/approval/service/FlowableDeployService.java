package com.zrws.approval.service;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Flowable 流程部署服务
 * <p>提供流程定义的部署、更新、删除、查询等完整生命周期管理
 */
@Service
public class FlowableDeployService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    // ============================================================
    // 1. 部署流程定义（从BPMN文件）
    // ============================================================

    /**
     * 部署单个BPMN文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deployBPMN(String processKey, String processName, InputStream inputStream) {
        // 先检查是否已存在同名流程，存在则删除旧版本
        deleteDeploymentByKey(processKey);

        Deployment deployment = repositoryService.createDeployment()
                .name(processName)
                .key(processKey)
                .addInputStream(processKey + ".bpmn20.xml", inputStream)
                .deploy();

        ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        Map<String, Object> result = new HashMap<>();
        result.put("deploymentId", deployment.getId());
        result.put("deploymentName", deployment.getName());
        result.put("processDefinitionId", processDef.getId());
        result.put("processKey", processDef.getKey());
        result.put("processName", processDef.getName());
        result.put("version", processDef.getVersion());
        result.put("deployTime", deployment.getDeploymentTime());
        return result;
    }

    /**
     * 通过MultipartFile部署BPMN
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deployFromFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String processKey = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String processName = processKey.replace("_", " ");

        return deployBPMN(processKey, processName, file.getInputStream());
    }

    /**
     * 批量部署资源目录下的所有BPMN文件
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> deployAllFromClasspath() {
        List<Map<String, Object>> results = new ArrayList<>();

        // 清理旧部署
        List<Deployment> oldDeployments = repositoryService.createDeploymentQuery().list();
        for (Deployment d : oldDeployments) {
            repositoryService.deleteDeployment(d.getId(), true);
        }

        // 重新部署所有BPMN文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/STANDARD.bpmn20.xml")
                .addClasspathResource("processes/MATERIAL.bpmn20.xml")
                .addClasspathResource("processes/DRONE_FLIGHT.bpmn20.xml")
                .addClasspathResource("processes/EMERGENCY.bpmn20.xml")
                .name("智壤卫士审批流程包")
                .deploy();

        // 获取所有部署的流程定义
        List<ProcessDefinition> processDefs = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .list();

        for (ProcessDefinition def : processDefs) {
            Map<String, Object> result = new HashMap<>();
            result.put("deploymentId", deployment.getId());
            result.put("processDefinitionId", def.getId());
            result.put("processKey", def.getKey());
            result.put("processName", def.getName());
            result.put("version", def.getVersion());
            results.add(result);
        }
        return results;
    }

    // ============================================================
    // 2. 动态创建流程定义（通过API）
    // ============================================================

    /**
     * 动态创建标准审批流程定义
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createStandardProcess(String processKey, String processName,
                                                     List<Map<String, String>> steps) {
        StringBuilder bpmnXml = new StringBuilder();
        bpmnXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        bpmnXml.append("<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n");
        bpmnXml.append("             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        bpmnXml.append("             xmlns:flowable=\"http://flowable.org/bpmn\"\n");
        bpmnXml.append("             targetNamespace=\"http://zrws.com/process/").append(processKey.toLowerCase()).append("\">\n");
        bpmnXml.append("    <process id=\"").append(processKey).append("\" name=\"").append(processName).append("\" isExecutable=\"true\">\n");

        // Start Event
        bpmnXml.append("        <startEvent id=\"start\"/>\n");

        String prevNode = "start";
        for (int i = 0; i < steps.size(); i++) {
            Map<String, String> step = steps.get(i);
            String stepId = step.get("key");
            String stepName = step.get("name");
            String assignee = step.get("assignee");
            String slaHours = step.get("slaHours");

            // UserTask
            bpmnXml.append("        <userTask id=\"").append(stepId).append("\" name=\"").append(stepName).append("\" ");
            bpmnXml.append("flowable:assignee=\"${").append(assignee).append("}\"");
            if (slaHours != null && !slaHours.isEmpty()) {
                bpmnXml.append(" flowable:dueDate=\"${dueDate").append(stepId.substring(0, 1).toUpperCase()).append(stepId.substring(1)).append("}\"");
            }
            bpmnXml.append(">\n");
            bpmnXml.append("            <extensionElements>\n");
            bpmnXml.append("                <flowable:formProperty id=\"opinion\" name=\"审批意见\" type=\"string\" required=\"true\"/>\n");
            bpmnXml.append("            </extensionElements>\n");
            bpmnXml.append("        </userTask>\n");

            // SequenceFlow
            bpmnXml.append("        <sequenceFlow sourceRef=\"").append(prevNode).append("\" targetRef=\"").append(stepId).append("\"/>\n");
            prevNode = stepId;
        }

        // End Event
        bpmnXml.append("        <endEvent id=\"end\"/>\n");
        bpmnXml.append("        <sequenceFlow sourceRef=\"").append(prevNode).append("\" targetRef=\"end\"/>\n");

        bpmnXml.append("    </process>\n");
        bpmnXml.append("</definitions>");

        // 部署生成的BPMN
        InputStream inputStream = new java.io.ByteArrayInputStream(bpmnXml.toString().getBytes());
        return deployBPMN(processKey, processName, inputStream);
    }

    // ============================================================
    // 3. 查询流程定义
    // ============================================================

    /**
     * 查询所有流程定义
     */
    public List<Map<String, Object>> listProcessDefinitions() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .list();

        for (ProcessDefinition def : defs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", def.getId());
            item.put("key", def.getKey());
            item.put("name", def.getName());
            item.put("version", def.getVersion());
            item.put("deploymentId", def.getDeploymentId());
            item.put("resourceName", def.getResourceName());
            item.put("hasStartFormKey", def.hasStartFormKey());
            item.put("isSuspended", def.isSuspended());
            item.put("tenantId", def.getTenantId());
            result.add(item);
        }
        return result;
    }

    /**
     * 查询单个流程定义
     */
    public Map<String, Object> getProcessDefinition(String processDefinitionId) {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (def == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", def.getId());
        result.put("key", def.getKey());
        result.put("name", def.getName());
        result.put("version", def.getVersion());
        result.put("deploymentId", def.getDeploymentId());
        result.put("resourceName", def.getResourceName());
        result.put("diagramResourceName", def.getDiagramResourceName());
        result.put("hasStartFormKey", def.hasStartFormKey());
        result.put("isSuspended", def.isSuspended());
        return result;
    }

    /**
     * 获取流程定义的BPMN XML内容
     */
    public String getProcessDefinitionXML(String processDefinitionId) {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        if (def == null) return null;

        try (InputStream is = repositoryService.getResourceAsStream(def.getDeploymentId(), def.getResourceName())) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("读取BPMN文件失败", e);
        }
    }

    // ============================================================
    // 4. 删除/更新流程部署
    // ============================================================

    /**
     * 删除流程部署（级联删除相关流程实例和任务）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    /**
     * 根据流程key删除所有版本
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeploymentByKey(String processKey) {
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .list();

        Set<String> deploymentIds = new HashSet<>();
        for (ProcessDefinition def : defs) {
            deploymentIds.add(def.getDeploymentId());
        }

        for (String deploymentId : deploymentIds) {
            // 先终止所有运行中的流程实例
            runtimeService.createProcessInstanceQuery()
                    .deploymentId(deploymentId)
                    .list().forEach(instance -> {
                runtimeService.deleteProcessInstance(instance.getId(), "流程已更新");
            });
            repositoryService.deleteDeployment(deploymentId, true);
        }
    }

    /**
     * 挂起流程定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    /**
     * 激活流程定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    // ============================================================
    // 5. 流程版本管理
    // ============================================================

    /**
     * 获取流程的所有版本
     */
    public List<Map<String, Object>> getProcessVersions(String processKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion().desc()
                .list();

        for (ProcessDefinition def : defs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", def.getId());
            item.put("version", def.getVersion());
            item.put("deploymentId", def.getDeploymentId());
            item.put("isSuspended", def.isSuspended());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取流程定义的最新版本
     */
    public ProcessDefinition getLatestVersion(String processKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
    }

    // ============================================================
    // 6. 流程部署统计
    // ============================================================

    /**
     * 获取流程部署统计信息
     */
    public Map<String, Object> getDeploymentStats() {
        Map<String, Object> stats = new HashMap<>();

        // 流程定义数量
        long processCount = repositoryService.createProcessDefinitionQuery().count();
        stats.put("processDefinitionCount", processCount);

        // 部署数量
        long deploymentCount = repositoryService.createDeploymentQuery().count();
        stats.put("deploymentCount", deploymentCount);

        // 运行中流程实例数量
        long runningInstanceCount = runtimeService.createProcessInstanceQuery().count();
        stats.put("runningInstanceCount", runningInstanceCount);

        // 待办任务数量
        long taskCount = taskService.createTaskQuery().count();
        stats.put("taskCount", taskCount);

        // 挂起的流程定义数量
        long suspendedCount = repositoryService.createProcessDefinitionQuery().suspended().count();
        stats.put("suspendedProcessCount", suspendedCount);

        return stats;
    }
}
