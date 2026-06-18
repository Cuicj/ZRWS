package com.zrws.approval.service;

import com.zrws.approval.config.FlowableConfig;
import com.zrws.approval.domain.entity.ApprovalComment;
import com.zrws.approval.domain.entity.ApprovalTask;
import com.zrws.approval.dto.SubmitApprovalReq;
import com.zrws.approval.mapper.ApprovalCommentMapper;
import com.zrws.approval.mapper.ApprovalTaskMapper;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批核心服务
 * <p>提供流程提交/审批/驳回/退回/待办/已办/历史记录等核心接口
 * <p>与 Flowable 引擎紧密集成，业务数据与流程数据双向同步
 */
@Service
public class ApprovalService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ApprovalTaskMapper taskMapper;

    @Autowired
    private ApprovalCommentMapper commentMapper;

    @Autowired
    private FlowableConfig flowableConfig;

    // ============================================================
    // 1. 提交审批 - 启动一个新的 BPMN 流程
    // ============================================================
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitApproval(SubmitApprovalReq req) {
        // 1) 根据业务类型选择流程变量构建器
        Map<String, Object> variables;
        switch (req.getBizType()) {
            case "STANDARD":
                variables = flowableConfig.buildStandardVariables(String.valueOf(req.getApplicantId()), req.getBizTitle());
                break;
            case "MATERIAL":
                variables = flowableConfig.buildMaterialVariables(String.valueOf(req.getApplicantId()));
                break;
            case "DRONE_FLIGHT":
                variables = flowableConfig.buildDroneFlightVariables(String.valueOf(req.getApplicantId()));
                break;
            case "EMERGENCY":
                variables = flowableConfig.buildEmergencyVariables(String.valueOf(req.getApplicantId()));
                break;
            default:
                throw new IllegalArgumentException("未知业务类型: " + req.getBizType());
        }
        variables.put("bizTitle", req.getBizTitle());
        variables.put("bizType", req.getBizType());
        variables.put("applicantName", req.getApplicantName());

        // 2) 启动 Flowable 流程实例
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                req.getBizType(),
                String.valueOf(req.getBizId() == null ? 0 : req.getBizId()),
                variables
        );

        // 3) 保存业务审批记录
        ApprovalTask approval = new ApprovalTask();
        approval.setFlowInstanceId(instance.getId());
        approval.setBizType(req.getBizType());
        approval.setBizId(req.getBizId());
        approval.setBizTitle(req.getBizTitle());
        approval.setApplicantId(req.getApplicantId());
        approval.setApplicantName(req.getApplicantName());
        approval.setApplicantDept(req.getApplicantDept());
        approval.setStatus("PROCESSING");
        approval.setPriority(req.getPriority());
        approval.setBizData(req.getBizData());
        // 查找当前任务的首个 UserTask
        Task firstTask = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        if (firstTask != null) {
            approval.setCurStep(firstTask.getName());
            approval.setCurStepKey(firstTask.getTaskDefinitionKey());
        }
        taskMapper.insert(approval);

        // 4) 记录提交意见
        ApprovalComment comment = new ApprovalComment();
        comment.setTaskId(approval.getTaskId());
        comment.setFlowInstanceId(instance.getId());
        comment.setStepKey("start");
        comment.setStepName("提交");
        comment.setApproverId(req.getApplicantId());
        comment.setApproverName(req.getApplicantName());
        comment.setAction("SUBMIT");
        comment.setOpinion("发起审批 - " + req.getBizTitle());
        commentMapper.insert(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", approval.getTaskId());
        result.put("flowInstanceId", instance.getId());
        result.put("curStep", approval.getCurStep());
        result.put("curStepKey", approval.getCurStepKey());
        result.put("status", "PROCESSING");
        return result;
    }

    // ============================================================
    // 2. 通过审批 - 完成当前 Flowable Task
    // ============================================================
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long taskId, Long approverId, String approverName, String opinion) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null) throw new IllegalArgumentException("审批任务不存在: " + taskId);
        if (!"PROCESSING".equals(task.getStatus())) throw new IllegalStateException("当前状态不可审批: " + task.getStatus());

        Task flowableTask = taskService.createTaskQuery()
                .processInstanceId(task.getFlowInstanceId())
                .taskDefinitionKey(task.getCurStepKey())
                .singleResult();
        if (flowableTask == null) throw new IllegalStateException("当前无待办任务，请刷新");

        // 完成 Flowable 任务
        Map<String, Object> vars = new HashMap<>();
        vars.put("approveTime", LocalDateTime.now().toString());
        taskService.complete(flowableTask.getId(), vars);

        // 保存审批意见
        ApprovalComment comment = new ApprovalComment();
        comment.setTaskId(taskId);
        comment.setFlowInstanceId(task.getFlowInstanceId());
        comment.setStepKey(task.getCurStepKey());
        comment.setStepName(task.getCurStep());
        comment.setApproverId(approverId);
        comment.setApproverName(approverName);
        comment.setAction("APPROVE");
        comment.setOpinion(opinion == null ? "同意" : opinion);
        commentMapper.insert(comment);

        // 查找下一步任务（或流程已结束）
        Task nextTask = taskService.createTaskQuery().processInstanceId(task.getFlowInstanceId()).singleResult();
        if (nextTask == null) {
            // 流程完成
            task.setCurStep(null);
            task.setCurStepKey(null);
            task.setStatus("PASSED");
        } else {
            task.setCurStep(nextTask.getName());
            task.setCurStepKey(nextTask.getTaskDefinitionKey());
        }
        task.setUpdatedTime(LocalDateTime.now());
        taskMapper.updateById(task);

        return true;
    }

    // ============================================================
    // 3. 驳回审批 - 删除流程实例并标记状态为 REJECTED
    // ============================================================
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long taskId, Long approverId, String approverName, String reason) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null) throw new IllegalArgumentException("审批任务不存在: " + taskId);

        // 保存驳回意见
        ApprovalComment comment = new ApprovalComment();
        comment.setTaskId(taskId);
        comment.setFlowInstanceId(task.getFlowInstanceId());
        comment.setStepKey(task.getCurStepKey());
        comment.setStepName(task.getCurStep());
        comment.setApproverId(approverId);
        comment.setApproverName(approverName);
        comment.setAction("REJECT");
        comment.setOpinion(reason == null ? "驳回" : reason);
        commentMapper.insert(comment);

        // 删除 Flowable 流程实例（终止）
        runtimeService.deleteProcessInstance(task.getFlowInstanceId(), "审批驳回: " + reason);

        // 标记业务状态
        task.setStatus("REJECTED");
        task.setUpdatedTime(LocalDateTime.now());
        taskMapper.updateById(task);

        return true;
    }

    // ============================================================
    // 4. 退回修改 - 将流程跳回起始节点 (businessKey + message)
    // ============================================================
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBack(Long taskId, Long approverId, String approverName, String reason) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null) throw new IllegalArgumentException("审批任务不存在: " + taskId);

        // 保存退回意见
        ApprovalComment comment = new ApprovalComment();
        comment.setTaskId(taskId);
        comment.setFlowInstanceId(task.getFlowInstanceId());
        comment.setStepKey(task.getCurStepKey());
        comment.setStepName(task.getCurStep());
        comment.setApproverId(approverId);
        comment.setApproverName(approverName);
        comment.setAction("RETURN");
        comment.setOpinion(reason == null ? "退回修改" : reason);
        commentMapper.insert(comment);

        // 终止当前流程并重新启动一个新的同类型流程（简化版退回机制）
        runtimeService.deleteProcessInstance(task.getFlowInstanceId(), "退回修改");

        Map<String, Object> variables = new HashMap<>();
        variables.put("applicant", String.valueOf(task.getApplicantId()));
        variables.put("bizTitle", task.getBizTitle());

        // 重新启动同类型流程
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                task.getBizType(),
                String.valueOf(task.getBizId() == null ? 0 : task.getBizId()),
                variables
        );
        task.setFlowInstanceId(instance.getId());
        task.setStatus("RETURNED");
        Task firstTask = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        if (firstTask != null) {
            task.setCurStep(firstTask.getName());
            task.setCurStepKey(firstTask.getTaskDefinitionKey());
        }
        task.setUpdatedTime(LocalDateTime.now());
        taskMapper.updateById(task);

        return true;
    }

    // ============================================================
    // 5. 我的待办
    // ============================================================
    public List<ApprovalTask> todoList(String assignee) {
        return taskMapper.selectTodoList(assignee);
    }

    // ============================================================
    // 6. 我发起的审批
    // ============================================================
    public List<ApprovalTask> myApplied(Long applicantId) {
        return taskMapper.selectMyApplied(applicantId);
    }

    // ============================================================
    // 7. 审批历史
    // ============================================================
    public List<ApprovalComment> history(Long taskId) {
        return commentMapper.selectByTaskId(taskId);
    }

    // ============================================================
    // 8. 我的已办 - 从 Flowable 历史服务中查询
    // ============================================================
    public List<HistoricTaskInstance> myDone(String assignee) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(0, 50);
    }

    // ============================================================
    // 9. 查询单个任务详情
    // ============================================================
    public ApprovalTask getById(Long taskId) {
        return taskMapper.selectById(taskId);
    }
}
