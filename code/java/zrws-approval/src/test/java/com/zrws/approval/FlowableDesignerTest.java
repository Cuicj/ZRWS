package com.zrws.approval;

import com.zrws.approval.domain.entity.BpmnDraft;
import com.zrws.approval.mapper.BpmnDraftMapper;
import com.zrws.approval.service.FlowableDesignerService;
import org.flowable.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程设计器测试类
 * 测试草稿保存、加载、部署等核心功能
 */
@SpringBootTest
public class FlowableDesignerTest {

    @Autowired
    private FlowableDesignerService designerService;

    @Autowired
    private BpmnDraftMapper draftMapper;

    /**
     * 测试草稿保存和加载
     */
    @Test
    public void testSaveAndLoadDraft() {
        // 准备测试数据
        String processKey = "TEST_PROCESS_" + System.currentTimeMillis();
        Map<String, Object> json = new HashMap<>();
        json.put("processKey", processKey);
        json.put("processName", "测试流程");
        json.put("processes", createTestProcesses());

        // 保存草稿
        Map<String, Object> saved = designerService.saveDraft(processKey, json);
        assertNotNull(saved);
        assertEquals(processKey, saved.get("processKey"));
        assertEquals("DRAFT", saved.get("status"));
        assertEquals(1, saved.get("version"));

        // 从数据库加载
        Map<String, Object> loaded = designerService.getDraft(processKey);
        assertNotNull(loaded);
        assertEquals(processKey, loaded.get("processKey"));
        assertEquals("测试流程", loaded.get("processName"));

        // 清理测试数据
        draftMapper.deleteById((Long) saved.get("id"));
    }

    /**
     * 测试草稿更新（版本递增）
     */
    @Test
    public void testDraftVersionIncrement() {
        String processKey = "TEST_VERSION_" + System.currentTimeMillis();
        Map<String, Object> json = new HashMap<>();
        json.put("processKey", processKey);
        json.put("processName", "版本测试");
        json.put("processes", createTestProcesses());

        // 第一次保存
        Map<String, Object> first = designerService.saveDraft(processKey, json);
        assertEquals(1, first.get("version"));

        // 第二次保存（更新）
        Map<String, Object> second = designerService.saveDraft(processKey, json);
        assertEquals(2, second.get("version"));

        // 第三次保存
        Map<String, Object> third = designerService.saveDraft(processKey, json);
        assertEquals(3, third.get("version"));

        // 清理
        draftMapper.deleteById((Long) third.get("id"));
    }

    /**
     * 测试草稿列表查询
     */
    @Test
    public void testListDrafts() {
        List<Map<String, Object>> drafts = designerService.listDrafts();
        assertNotNull(drafts);
        // 应该包含初始化数据
        assertTrue(drafts.size() >= 4); // STANDARD, MATERIAL, DRONE_FLIGHT, EMERGENCY
    }

    /**
     * 测试流程提交审核
     */
    @Test
    public void testSubmitForReview() {
        String processKey = "TEST_REVIEW_" + System.currentTimeMillis();
        Map<String, Object> json = new HashMap<>();
        json.put("processKey", processKey);
        json.put("processName", "审核测试");
        json.put("processes", createTestProcesses());

        // 保存草稿
        Map<String, Object> saved = designerService.saveDraft(processKey, json);

        // 提交审核
        Map<String, Object> submitted = designerService.submitForReview(processKey);
        assertEquals("PENDING_REVIEW", submitted.get("status"));
        assertNotNull(submitted.get("submitTime"));

        // 清理
        draftMapper.deleteById((Long) saved.get("id"));
    }

    /**
     * 测试审核通过/驳回
     */
    @Test
    public void testReview() {
        String processKey = "TEST_APPROVE_" + System.currentTimeMillis();
        Map<String, Object> json = new HashMap<>();
        json.put("processKey", processKey);
        json.put("processName", "审批测试");
        json.put("processes", createTestProcesses());

        // 保存并提交
        Map<String, Object> saved = designerService.saveDraft(processKey, json);
        designerService.submitForReview(processKey);

        // 审核通过
        Map<String, Object> approved = designerService.review(processKey, true, "审核通过");
        assertEquals("APPROVED", approved.get("status"));
        assertEquals("审核通过", approved.get("reviewComment"));
        assertNotNull(approved.get("reviewTime"));

        // 清理
        draftMapper.deleteById((Long) saved.get("id"));
    }

    /**
     * 测试流程发布
     */
    @Test
    public void testPublish() {
        String processKey = "TEST_PUBLISH_" + System.currentTimeMillis();
        Map<String, Object> json = new HashMap<>();
        json.put("processKey", processKey);
        json.put("processName", "发布测试");
        json.put("processes", createTestProcesses());

        // 保存、提交、审核
        Map<String, Object> saved = designerService.saveDraft(processKey, json);
        designerService.submitForReview(processKey);
        designerService.review(processKey, true, "审核通过");

        // 发布
        Map<String, Object> published = designerService.publish(processKey);
        assertEquals("PUBLISHED", published.get("status"));
        assertNotNull(published.get("publishTime"));

        // 清理
        draftMapper.deleteById((Long) saved.get("id"));
    }

    /**
     * 测试BPMN XML与JSON互转
     */
    @Test
    public void testXmlJsonConversion() {
        // 准备测试BPMN XML
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">" +
                "<bpmn:process id=\"Process_Test\" name=\"测试流程\" isExecutable=\"true\">" +
                "<bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>" +
                "<bpmn:userTask id=\"UserTask_1\" name=\"审批\"/>" +
                "<bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>" +
                "<bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"UserTask_1\"/>" +
                "<bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"UserTask_1\" targetRef=\"EndEvent_1\"/>" +
                "</bpmn:process>" +
                "</bpmn:definitions>";

        // XML转JSON
        Map<String, Object> json = designerService.bpmnXmlToJson(testXml);
        assertNotNull(json);
        assertNotNull(json.get("processes"));
        assertEquals(1, ((List<?>) json.get("processes")).size());

        // JSON转XML
        String convertedXml = designerService.jsonToBpmnXml(json);
        assertNotNull(convertedXml);
        assertTrue(convertedXml.contains("Process_Test"));
        assertTrue(convertedXml.contains("UserTask_1"));
    }

    /**
     * 测试流程验证 - 有效流程
     */
    @Test
    public void testValidateValidProcess() {
        Map<String, Object> json = new HashMap<>();
        json.put("processes", createTestProcesses());

        Map<String, Object> result = designerService.validateBpmn(json);
        assertTrue((Boolean) result.get("valid"));
        assertEquals(0, ((List<?>) result.get("errors")).size());
    }

    /**
     * 测试流程验证 - 缺少开始事件
     */
    @Test
    public void testValidateMissingStartEvent() {
        Map<String, Object> json = new HashMap<>();
        List<Map<String, Object>> processes = new ArrayList<>();

        Map<String, Object> process = new HashMap<>();
        process.put("id", "Process_NoStart");
        process.put("name", "无开始流程");

        List<Map<String, Object>> nodes = new ArrayList<>();
        // 添加结束事件但没有开始事件
        Map<String, Object> endEvent = new HashMap<>();
        endEvent.put("id", "EndEvent_1");
        endEvent.put("type", "endEvent");
        nodes.add(endEvent);

        process.put("nodes", nodes);
        process.put("edges", new ArrayList<>());
        processes.add(process);
        json.put("processes", processes);

        Map<String, Object> result = designerService.validateBpmn(json);
        assertFalse((Boolean) result.get("valid"));
        assertTrue(((List<?>) result.get("errors")).size() > 0);
    }

    /**
     * 测试获取流程模板
     */
    @Test
    public void testGetTemplates() {
        List<Map<String, Object>> templates = designerService.getTemplates();
        assertNotNull(templates);
        assertEquals(4, templates.size());

        // 验证标准审批模板
        Map<String, Object> standard = templates.stream()
                .filter(t -> "STANDARD".equals(t.get("key")))
                .findFirst()
                .orElse(null);
        assertNotNull(standard);
        assertEquals("标准审批", standard.get("name"));
    }

    /**
     * 测试从模板创建流程
     */
    @Test
    public void testCreateFromTemplate() {
        Map<String, Object> result = designerService.createFromTemplate("STANDARD");
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("STANDARD", result.get("processKey"));
    }

    /**
     * 创建测试用的流程定义
     */
    private List<Map<String, Object>> createTestProcesses() {
        List<Map<String, Object>> processes = new ArrayList<>();

        Map<String, Object> process = new HashMap<>();
        process.put("id", "Process_Test_" + System.currentTimeMillis());
        process.put("name", "测试流程");

        List<Map<String, Object>> nodes = new ArrayList<>();

        // 开始事件
        Map<String, Object> start = new HashMap<>();
        start.put("id", "StartEvent_1");
        start.put("type", "startEvent");
        start.put("name", "开始");
        nodes.add(start);

        // 用户任务
        Map<String, Object> task = new HashMap<>();
        task.put("id", "UserTask_1");
        task.put("type", "userTask");
        task.put("name", "审批任务");
        task.put("assignee", "${assignee}");
        nodes.add(task);

        // 结束事件
        Map<String, Object> end = new HashMap<>();
        end.put("id", "EndEvent_1");
        end.put("type", "endEvent");
        end.put("name", "结束");
        nodes.add(end);

        // 连线
        List<Map<String, Object>> edges = new ArrayList<>();

        Map<String, Object> edge1 = new HashMap<>();
        edge1.put("id", "Flow_1");
        edge1.put("sourceRef", "StartEvent_1");
        edge1.put("targetRef", "UserTask_1");
        edges.add(edge1);

        Map<String, Object> edge2 = new HashMap<>();
        edge2.put("id", "Flow_2");
        edge2.put("sourceRef", "UserTask_1");
        edge2.put("targetRef", "EndEvent_1");
        edges.add(edge2);

        process.put("nodes", nodes);
        process.put("edges", edges);
        processes.add(process);

        return processes;
    }
}
