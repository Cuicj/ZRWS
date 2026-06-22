package com.zrws.approval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.ApprovalComment;
import com.zrws.approval.domain.entity.ApprovalTask;
import com.zrws.approval.dto.SubmitApprovalReq;
import com.zrws.approval.service.ApprovalService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ApprovalController 单元测试
 * 测试审批REST API接口
 */
@ExtendWith(MockitoExtension.class)
public class ApprovalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApprovalController approvalController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(approvalController).build();
        objectMapper = new ObjectMapper();
    }

    // ============================================================
    // 1. 提交审批接口测试
    // ============================================================

    @Test
    @DisplayName("POST /api/v1/submit - 提交标准审批成功")
    void testSubmit_Success() throws Exception {
        // Arrange
        SubmitApprovalReq req = new SubmitApprovalReq();
        req.setBizType("STANDARD");
        req.setBizId(1001L);
        req.setBizTitle("测试审批任务");
        req.setApplicantId(1001L);
        req.setApplicantName("王工");
        req.setApplicantDept("技术部");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("taskId", 100001L);
        mockResult.put("flowInstanceId", "PROCESS-001");
        mockResult.put("curStep", "校核");
        mockResult.put("curStepKey", "checker");
        mockResult.put("status", "PROCESSING");

        when(approvalService.submitApproval(any(SubmitApprovalReq.class))).thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.taskId").value(100001))
                .andExpect(jsonPath("$.data.flowInstanceId").value("PROCESS-001"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        verify(approvalService, times(1)).submitApproval(any(SubmitApprovalReq.class));
    }

    @Test
    @DisplayName("POST /api/v1/submit - 提交应急审批成功（高优先级）")
    void testSubmit_Emergency_Success() throws Exception {
        // Arrange
        SubmitApprovalReq req = new SubmitApprovalReq();
        req.setBizType("EMERGENCY");
        req.setBizId(4002L);
        req.setBizTitle("应急地质灾害调查");
        req.setApplicantId(1040L);
        req.setApplicantName("王指挥");
        req.setApplicantDept("应急部");
        req.setPriority(2);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("taskId", 100004L);
        mockResult.put("flowInstanceId", "PROCESS-004");
        mockResult.put("curStep", "应急提交");
        mockResult.put("status", "PROCESSING");

        when(approvalService.submitApproval(any(SubmitApprovalReq.class))).thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.curStep").value("应急提交"));
    }

    @Test
    @DisplayName("POST /api/v1/submit - 提交物资申领成功")
    void testSubmit_Material_Success() throws Exception {
        // Arrange
        SubmitApprovalReq req = new SubmitApprovalReq();
        req.setBizType("MATERIAL");
        req.setBizId(3001L);
        req.setBizTitle("RTK基准站电池采购申请");
        req.setApplicantId(1002L);
        req.setApplicantName("李工");
        req.setApplicantDept("装备部");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("taskId", 100002L);
        mockResult.put("flowInstanceId", "PROCESS-002");
        mockResult.put("curStep", "部门审批");
        mockResult.put("status", "PROCESSING");

        when(approvalService.submitApproval(any(SubmitApprovalReq.class))).thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.curStep").value("部门审批"));
    }

    // ============================================================
    // 2. 审批通过接口测试
    // ============================================================

    @Test
    @DisplayName("POST /api/v1/{taskId}/approve - 审批通过成功")
    void testApprove_Success() throws Exception {
        // Arrange
        when(approvalService.approve(eq(100001L), eq(1020L), eq("赵高工"), eq("数据准确")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/100001/approve")
                        .param("approverId", "1020")
                        .param("approverName", "赵高工")
                        .param("opinion", "数据准确"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("审批通过"));

        verify(approvalService, times(1)).approve(100001L, 1020L, "赵高工", "数据准确");
    }

    @Test
    @DisplayName("POST /api/v1/{taskId}/approve - 使用默认意见")
    void testApprove_DefaultOpinion() throws Exception {
        // Arrange
        when(approvalService.approve(eq(100001L), eq(1020L), eq("赵高工"), eq("同意")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/100001/approve")
                        .param("approverId", "1020")
                        .param("approverName", "赵高工"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(approvalService, times(1)).approve(100001L, 1020L, "赵高工", "同意");
    }

    // ============================================================
    // 3. 审批驳回接口测试
    // ============================================================

    @Test
    @DisplayName("POST /api/v1/{taskId}/reject - 审批驳回成功")
    void testReject_Success() throws Exception {
        // Arrange
        when(approvalService.reject(eq(100001L), eq(1020L), eq("赵高工"), eq("材料不全")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/100001/reject")
                        .param("approverId", "1020")
                        .param("approverName", "赵高工")
                        .param("reason", "材料不全"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("已驳回"));

        verify(approvalService, times(1)).reject(100001L, 1020L, "赵高工", "材料不全");
    }

    @Test
    @DisplayName("POST /api/v1/{taskId}/reject - 使用默认驳回原因")
    void testReject_DefaultReason() throws Exception {
        // Arrange
        when(approvalService.reject(eq(100001L), eq(1020L), eq("赵高工"), eq("驳回")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/100001/reject")
                        .param("approverId", "1020")
                        .param("approverName", "赵高工"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("已驳回"));
    }

    // ============================================================
    // 4. 退回修改接口测试
    // ============================================================

    @Test
    @DisplayName("POST /api/v1/{taskId}/return - 退回修改成功")
    void testReturn_Success() throws Exception {
        // Arrange
        when(approvalService.returnBack(eq(100001L), eq(1020L), eq("张总工"), eq("数据需更正")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/100001/return")
                        .param("approverId", "1020")
                        .param("approverName", "张总工")
                        .param("reason", "数据需更正"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("已退回"));

        verify(approvalService, times(1)).returnBack(100001L, 1020L, "张总工", "数据需更正");
    }

    // ============================================================
    // 5. 我的待办接口测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/todo - 获取待办列表成功")
    void testTodo_Success() throws Exception {
        // Arrange
        List<ApprovalTask> mockList = new ArrayList<>();
        ApprovalTask task1 = new ApprovalTask();
        task1.setTaskId(100001L);
        task1.setBizType("STANDARD");
        task1.setBizTitle("望城区乔口镇测绘任务");
        task1.setStatus("PROCESSING");
        task1.setCurStep("校核");
        mockList.add(task1);

        ApprovalTask task2 = new ApprovalTask();
        task2.setTaskId(100002L);
        task2.setBizType("MATERIAL");
        task2.setBizTitle("RTK电池采购");
        task2.setStatus("PROCESSING");
        task2.setCurStep("部门审批");
        mockList.add(task2);

        when(approvalService.todoList("1020")).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/todo")
                        .param("assignee", "1020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].taskId").value(100001))
                .andExpect(jsonPath("$.data.list[0].bizType").value("STANDARD"))
                .andExpect(jsonPath("$.data.list[1].taskId").value(100002));
    }

    @Test
    @DisplayName("GET /api/v1/todo - 待办列表为空")
    void testTodo_Empty() throws Exception {
        // Arrange
        when(approvalService.todoList("1020")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/todo")
                        .param("assignee", "1020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }

    // ============================================================
    // 6. 我发起的审批接口测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/my-applied - 获取我发起的审批成功")
    void testMyApplied_Success() throws Exception {
        // Arrange
        List<ApprovalTask> mockList = new ArrayList<>();
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setBizType("STANDARD");
        task.setBizTitle("望城区乔口镇测绘任务");
        task.setApplicantId(1001L);
        task.setApplicantName("王工");
        task.setStatus("PROCESSING");
        mockList.add(task);

        when(approvalService.myApplied(1001L)).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/my-applied")
                        .param("applicantId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].applicantName").value("王工"));
    }

    // ============================================================
    // 7. 我的已办接口测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/done - 获取已办列表成功")
    void testDone_Success() throws Exception {
        // Arrange
        List<HistoricTaskInstance> mockList = new ArrayList<>();
        HistoricTaskInstance histTask = mock(HistoricTaskInstance.class);
        when(histTask.getId()).thenReturn("HIST-001");
        when(histTask.getName()).thenReturn("校核");
        when(histTask.getTaskDefinitionKey()).thenReturn("checker");
        mockList.add(histTask);

        when(approvalService.myDone("1020")).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/done")
                        .param("assignee", "1020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ============================================================
    // 8. 审批历史接口测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/{taskId}/history - 获取审批历史成功")
    void testHistory_Success() throws Exception {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setBizType("STANDARD");
        task.setBizTitle("测试任务");
        task.setStatus("PROCESSING");

        List<ApprovalComment> comments = new ArrayList<>();
        ApprovalComment comment1 = new ApprovalComment();
        comment1.setCommentId(200001L);
        comment1.setStepName("提交");
        comment1.setApproverName("王工");
        comment1.setAction("SUBMIT");
        comment1.setOpinion("发起审批");
        comments.add(comment1);

        ApprovalComment comment2 = new ApprovalComment();
        comment2.setCommentId(200002L);
        comment2.setStepName("校核");
        comment2.setApproverName("李工");
        comment2.setAction("APPROVE");
        comment2.setOpinion("数据准确");
        comments.add(comment2);

        when(approvalService.getById(100001L)).thenReturn(task);
        when(approvalService.history(100001L)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/100001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.task.taskId").value(100001))
                .andExpect(jsonPath("$.data.task.bizType").value("STANDARD"))
                .andExpect(jsonPath("$.data.history.length()").value(2))
                .andExpect(jsonPath("$.data.history[0].action").value("SUBMIT"))
                .andExpect(jsonPath("$.data.history[1].action").value("APPROVE"));
    }

    // ============================================================
    // 9. 任务详情接口测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/task/{taskId} - 获取任务详情成功")
    void testGetTask_Success() throws Exception {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setBizType("STANDARD");
        task.setBizTitle("望城区乔口镇测绘任务");
        task.setApplicantId(1001L);
        task.setApplicantName("王工");
        task.setApplicantDept("技术部");
        task.setCurStep("校核");
        task.setCurStepKey("checker");
        task.setStatus("PROCESSING");
        task.setPriority(1);

        when(approvalService.getById(100001L)).thenReturn(task);

        // Act & Assert
        mockMvc.perform(get("/api/v1/task/100001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId").value(100001))
                .andExpect(jsonPath("$.data.bizType").value("STANDARD"))
                .andExpect(jsonPath("$.data.applicantName").value("王工"))
                .andExpect(jsonPath("$.data.curStep").value("校核"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.priority").value(1));
    }

    // ============================================================
    // 10. 异常场景测试
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/task/{taskId} - 任务不存在")
    void testGetTask_NotFound() throws Exception {
        // Arrange
        when(approvalService.getById(999L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/v1/task/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/submit - 业务类型为空")
    void testSubmit_BizTypeRequired() throws Exception {
        // Arrange
        SubmitApprovalReq req = new SubmitApprovalReq();
        req.setBizTitle("测试任务");
        req.setApplicantId(1001L);
        req.setApplicantName("王工");
        // bizType 未设置

        // Act & Assert
        mockMvc.perform(post("/api/v1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
