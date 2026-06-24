package com.zrws.approval.service;

import com.zrws.approval.config.FlowableConfig;
import com.zrws.approval.domain.entity.ApprovalTask;
import com.zrws.approval.dto.SubmitApprovalReq;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalService 单元测试
 * 测试审批核心服务的各种业务方法
 */
@ExtendWith(MockitoExtension.class)
public class ApprovalServiceTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @Mock
    private com.zrws.approval.mapper.ApprovalTaskMapper taskMapper;

    @Mock
    private com.zrws.approval.mapper.ApprovalCommentMapper commentMapper;

    @Mock
    private FlowableConfig flowableConfig;

    @InjectMocks
    private ApprovalService approvalService;

    private SubmitApprovalReq submitReq;

    @BeforeEach
    void setUp() {
        // 初始化提交审批请求
        submitReq = new SubmitApprovalReq();
        submitReq.setBizType("STANDARD");
        submitReq.setBizId(1001L);
        submitReq.setBizTitle("测试审批任务");
        submitReq.setApplicantId(1001L);
        submitReq.setApplicantName("王工");
        submitReq.setApplicantDept("技术部");
        submitReq.setPriority(0);
    }

    // ============================================================
    // 1. submitApproval - 提交审批测试
    // ============================================================

    @Test
    @DisplayName("提交标准审批 - 成功")
    void testSubmitApproval_Standard_Success() {
        // Arrange
        Map<String, Object> mockVars = new HashMap<>();
        mockVars.put("checker", "1001");

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("PROCESS-001");
        when(flowableConfig.buildStandardVariables(anyString(), anyString())).thenReturn(mockVars);
        when(runtimeService.startProcessInstanceByKey(eq("STANDARD"), anyString(), anyMap()))
                .thenReturn(mockInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("校核");
        when(mockTask.getTaskDefinitionKey()).thenReturn("checker");
        // 使用完整的查询模拟
        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-001")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.insert(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        Map<String, Object> result = approvalService.submitApproval(submitReq);

        // Assert
        assertNotNull(result);
        assertEquals("PROCESSING", result.get("status"));
        assertEquals("PROCESS-001", result.get("flowInstanceId"));
        assertEquals("校核", result.get("curStep"));
        assertEquals("checker", result.get("curStepKey"));

        verify(runtimeService, times(1)).startProcessInstanceByKey(eq("STANDARD"), anyString(), anyMap());
        verify(taskMapper, times(1)).insert(any(ApprovalTask.class));
        verify(commentMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("提交审批 - 未知业务类型抛异常")
    void testSubmitApproval_UnknownBizType_ThrowsException() {
        // Arrange
        submitReq.setBizType("UNKNOWN_TYPE");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> approvalService.submitApproval(submitReq)
        );
        assertTrue(exception.getMessage().contains("未知业务类型"));
    }

    @Test
    @DisplayName("提交物资申领审批 - 成功")
    void testSubmitApproval_Material_Success() {
        // Arrange
        submitReq.setBizType("MATERIAL");
        Map<String, Object> mockVars = new HashMap<>();
        mockVars.put("deptManager", "1001");

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("PROCESS-002");
        when(flowableConfig.buildMaterialVariables(anyString())).thenReturn(mockVars);
        when(runtimeService.startProcessInstanceByKey(eq("MATERIAL"), anyString(), anyMap()))
                .thenReturn(mockInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("部门审批");
        when(mockTask.getTaskDefinitionKey()).thenReturn("dept_approve");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-002")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.insert(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        Map<String, Object> result = approvalService.submitApproval(submitReq);

        // Assert
        assertNotNull(result);
        assertEquals("PROCESSING", result.get("status"));
        verify(flowableConfig, times(1)).buildMaterialVariables(anyString());
    }

    @Test
    @DisplayName("提交无人机飞行报备 - 成功")
    void testSubmitApproval_DroneFlight_Success() {
        // Arrange
        submitReq.setBizType("DRONE_FLIGHT");
        Map<String, Object> mockVars = new HashMap<>();
        mockVars.put("airspaceAdmin", "1001");

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("PROCESS-003");
        when(flowableConfig.buildDroneFlightVariables(anyString())).thenReturn(mockVars);
        when(runtimeService.startProcessInstanceByKey(eq("DRONE_FLIGHT"), anyString(), anyMap()))
                .thenReturn(mockInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("空域申请");
        when(mockTask.getTaskDefinitionKey()).thenReturn("airspace");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-003")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.insert(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        Map<String, Object> result = approvalService.submitApproval(submitReq);

        // Assert
        assertNotNull(result);
        assertEquals("空域申请", result.get("curStep"));
        verify(flowableConfig, times(1)).buildDroneFlightVariables(anyString());
    }

    @Test
    @DisplayName("提交应急快速通道 - 成功")
    void testSubmitApproval_Emergency_Success() {
        // Arrange
        submitReq.setBizType("EMERGENCY");
        submitReq.setPriority(2); // 紧急
        Map<String, Object> mockVars = new HashMap<>();
        mockVars.put("applicant", "1001");

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("PROCESS-004");
        when(flowableConfig.buildEmergencyVariables(anyString())).thenReturn(mockVars);
        when(runtimeService.startProcessInstanceByKey(eq("EMERGENCY"), anyString(), anyMap()))
                .thenReturn(mockInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("应急提交");
        when(mockTask.getTaskDefinitionKey()).thenReturn("emergency_submit");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-004")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.insert(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        Map<String, Object> result = approvalService.submitApproval(submitReq);

        // Assert
        assertNotNull(result);
        assertEquals("应急提交", result.get("curStep"));
        verify(flowableConfig, times(1)).buildEmergencyVariables(anyString());
    }

    // ============================================================
    // 2. approve - 审批通过测试
    // ============================================================

    @Test
    @DisplayName("审批通过 - 任务不存在抛异常")
    void testApprove_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> approvalService.approve(999L, 1001L, "王工", "同意")
        );
        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("审批通过 - 状态不是PROCESSING抛异常")
    void testApprove_WrongStatus_ThrowsException() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setStatus("PASSED"); // 已通过状态
        task.setFlowInstanceId("PROCESS-001");
        when(taskMapper.selectById(100001L)).thenReturn(task);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalService.approve(100001L, 1001L, "王工", "同意")
        );
        assertTrue(exception.getMessage().contains("不可审批"));
    }

    @Test
    @DisplayName("审批通过 - 无待办任务抛异常")
    void testApprove_NoPendingTask_ThrowsException() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setStatus("PROCESSING");
        task.setFlowInstanceId("PROCESS-001");
        task.setCurStepKey("checker");
        when(taskMapper.selectById(100001L)).thenReturn(task);

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-001")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("checker")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null); // 无待办任务

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalService.approve(100001L, 1001L, "王工", "同意")
        );
        assertTrue(exception.getMessage().contains("无待办任务"));
    }

    @Test
    @DisplayName("审批通过 - 成功完成审批")
    void testApprove_Success_CompletesApproval() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setStatus("PROCESSING");
        task.setFlowInstanceId("PROCESS-001");
        task.setCurStep("校核");
        task.setCurStepKey("checker");
        when(taskMapper.selectById(100001L)).thenReturn(task);

        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("TASK-001");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-001")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("checker")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        // 模拟下一步无任务（流程结束）
        when(taskQuery.singleResult()).thenReturn(mockTask).thenReturn(null);

        when(taskMapper.updateById(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        boolean result = approvalService.approve(100001L, 1001L, "王工", "数据准确，通过");

        // Assert
        assertTrue(result);
        verify(taskService, times(1)).complete(eq("TASK-001"), anyMap());
        verify(commentMapper, times(1)).insert(any());
        verify(taskMapper, times(1)).updateById(any(ApprovalTask.class));
    }

    // ============================================================
    // 3. reject - 驳回审批测试
    // ============================================================

    @Test
    @DisplayName("驳回审批 - 任务不存在抛异常")
    void testReject_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> approvalService.reject(999L, 1001L, "王工", "材料不全")
        );
    }

    @Test
    @DisplayName("驳回审批 - 成功驳回")
    void testReject_Success() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setFlowInstanceId("PROCESS-001");
        task.setCurStep("校核");
        task.setCurStepKey("checker");
        when(taskMapper.selectById(100001L)).thenReturn(task);
        when(commentMapper.insert(any())).thenReturn(1);
        when(taskMapper.updateById(any(ApprovalTask.class))).thenReturn(1);

        // Act
        boolean result = approvalService.reject(100001L, 1001L, "王工", "材料不全，请补充");

        // Assert
        assertTrue(result);
        verify(runtimeService, times(1)).deleteProcessInstance(eq("PROCESS-001"), anyString());
        verify(taskMapper, times(1)).updateById(argThat(t ->
                "REJECTED".equals(t.getStatus())
        ));
    }

    // ============================================================
    // 4. returnBack - 退回修改测试
    // ============================================================

    @Test
    @DisplayName("退回修改 - 任务不存在抛异常")
    void testReturnBack_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> approvalService.returnBack(999L, 1001L, "王工", "需修改数据")
        );
    }

    @Test
    @DisplayName("退回修改 - 成功退回并重启流程")
    void testReturnBack_Success() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setBizType("STANDARD");
        task.setBizId(1001L);
        task.setBizTitle("测试任务");
        task.setApplicantId(1001L);
        task.setFlowInstanceId("PROCESS-001");
        task.setCurStep("审核");
        task.setCurStepKey("reviewer");
        when(taskMapper.selectById(100001L)).thenReturn(task);
        when(commentMapper.insert(any())).thenReturn(1);

        ProcessInstance newInstance = mock(ProcessInstance.class);
        when(newInstance.getId()).thenReturn("PROCESS-002");
        when(runtimeService.startProcessInstanceByKey(eq("STANDARD"), anyString(), anyMap()))
                .thenReturn(newInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("校核");
        when(mockTask.getTaskDefinitionKey()).thenReturn("checker");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-002")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.updateById(any(ApprovalTask.class))).thenReturn(1);

        // Act
        boolean result = approvalService.returnBack(100001L, 1001L, "张总工", "数据需更正");

        // Assert
        assertTrue(result);
        verify(runtimeService, times(1)).deleteProcessInstance(eq("PROCESS-001"), anyString());
        verify(runtimeService, times(1)).startProcessInstanceByKey(eq("STANDARD"), anyString(), anyMap());
        verify(taskMapper, times(1)).updateById(argThat(t ->
                "PROCESS-002".equals(t.getFlowInstanceId()) &&
                "RETURNED".equals(t.getStatus())
        ));
    }

    // ============================================================
    // 5. getById - 查询任务测试
    // ============================================================

    @Test
    @DisplayName("查询任务详情 - 成功")
    void testGetById_Success() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setBizType("STANDARD");
        task.setBizTitle("测试任务");
        task.setStatus("PROCESSING");
        when(taskMapper.selectById(100001L)).thenReturn(task);

        // Act
        ApprovalTask result = approvalService.getById(100001L);

        // Assert
        assertNotNull(result);
        assertEquals(100001L, result.getTaskId());
        assertEquals("STANDARD", result.getBizType());
        assertEquals("PROCESSING", result.getStatus());
    }

    @Test
    @DisplayName("查询任务详情 - 任务不存在返回null")
    void testGetById_NotFound() {
        // Arrange
        when(taskMapper.selectById(999L)).thenReturn(null);

        // Act
        ApprovalTask result = approvalService.getById(999L);

        // Assert
        assertNull(result);
    }

    // ============================================================
    // 6. 边界条件测试
    // ============================================================

    @Test
    @DisplayName("提交审批 - 意见为空使用默认值")
    void testApprove_OpinionNull_UsesDefault() {
        // Arrange
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(100001L);
        task.setStatus("PROCESSING");
        task.setFlowInstanceId("PROCESS-001");
        task.setCurStep("校核");
        task.setCurStepKey("checker");
        when(taskMapper.selectById(100001L)).thenReturn(task);

        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("TASK-001");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-001")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("checker")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask).thenReturn(null);

        when(taskMapper.updateById(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act - 传入null意见
        boolean result = approvalService.approve(100001L, 1001L, "王工", null);

        // Assert
        assertTrue(result);
        verify(commentMapper, times(1)).insert(argThat(c ->
                "同意".equals(c.getOpinion())
        ));
    }

    @Test
    @DisplayName("提交审批 - 高优先级任务")
    void testSubmitApproval_HighPriority() {
        // Arrange
        submitReq.setPriority(2); // 紧急
        Map<String, Object> mockVars = new HashMap<>();

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("PROCESS-005");
        when(flowableConfig.buildStandardVariables(anyString(), anyString())).thenReturn(mockVars);
        when(runtimeService.startProcessInstanceByKey(eq("STANDARD"), anyString(), anyMap()))
                .thenReturn(mockInstance);

        Task mockTask = mock(Task.class);
        when(mockTask.getName()).thenReturn("校核");
        when(mockTask.getTaskDefinitionKey()).thenReturn("checker");

        org.flowable.engine.TaskQuery taskQuery = mock(org.flowable.engine.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("PROCESS-005")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(taskMapper.insert(any(ApprovalTask.class))).thenReturn(1);
        when(commentMapper.insert(any())).thenReturn(1);

        // Act
        Map<String, Object> result = approvalService.submitApproval(submitReq);

        // Assert
        assertNotNull(result);
        verify(taskMapper, times(1)).insert(argThat(t ->
                t.getPriority() == 2
        ));
    }
}
