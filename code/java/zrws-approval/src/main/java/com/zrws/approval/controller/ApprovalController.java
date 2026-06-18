package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.ApprovalComment;
import com.zrws.approval.domain.entity.ApprovalTask;
import com.zrws.approval.dto.SubmitApprovalReq;
import com.zrws.approval.service.ApprovalService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批 REST API 控制器
 * <p>Base path: /approval/api/v1
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    /**
     * 1. 发起审批
     * POST /api/v1/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody @Validated SubmitApprovalReq req) {
        Map<String, Object> result = approvalService.submitApproval(req);
        return success(result);
    }

    /**
     * 2. 审批通过
     * POST /api/v1/{taskId}/approve
     */
    @PostMapping("/{taskId}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long taskId,
                                                        @RequestParam Long approverId,
                                                        @RequestParam String approverName,
                                                        @RequestParam(required = false, defaultValue = "同意") String opinion) {
        approvalService.approve(taskId, approverId, approverName, opinion);
        return successMsg("审批通过");
    }

    /**
     * 3. 审批驳回
     * POST /api/v1/{taskId}/reject
     */
    @PostMapping("/{taskId}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long taskId,
                                                       @RequestParam Long approverId,
                                                       @RequestParam String approverName,
                                                       @RequestParam(required = false, defaultValue = "驳回") String reason) {
        approvalService.reject(taskId, approverId, approverName, reason);
        return successMsg("已驳回");
    }

    /**
     * 4. 退回修改
     * POST /api/v1/{taskId}/return
     */
    @PostMapping("/{taskId}/return")
    public ResponseEntity<Map<String, Object>> returnBack(@PathVariable Long taskId,
                                                          @RequestParam Long approverId,
                                                          @RequestParam String approverName,
                                                          @RequestParam(required = false, defaultValue = "退回修改") String reason) {
        approvalService.returnBack(taskId, approverId, approverName, reason);
        return successMsg("已退回");
    }

    /**
     * 5. 我的待办
     * GET /api/v1/todo?assignee=1020
     */
    @GetMapping("/todo")
    public ResponseEntity<Map<String, Object>> todo(@RequestParam String assignee) {
        List<ApprovalTask> list = approvalService.todoList(assignee);
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("list", list);
        return success(result);
    }

    /**
     * 6. 我发起的审批
     * GET /api/v1/my-applied?applicantId=1001
     */
    @GetMapping("/my-applied")
    public ResponseEntity<Map<String, Object>> myApplied(@RequestParam Long applicantId) {
        List<ApprovalTask> list = approvalService.myApplied(applicantId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("list", list);
        return success(result);
    }

    /**
     * 7. 我的已办（Flowable 历史记录）
     * GET /api/v1/done?assignee=1020
     */
    @GetMapping("/done")
    public ResponseEntity<Map<String, Object>> myDone(@RequestParam String assignee) {
        List<HistoricTaskInstance> list = approvalService.myDone(assignee);
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("list", list);
        return success(result);
    }

    /**
     * 8. 审批历史/流程
     * GET /api/v1/{taskId}/history
     */
    @GetMapping("/{taskId}/history")
    public ResponseEntity<Map<String, Object>> history(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        ApprovalTask task = approvalService.getById(taskId);
        List<ApprovalComment> history = approvalService.history(taskId);
        result.put("task", task);
        result.put("history", history);
        return success(result);
    }

    /**
     * 9. 任务详情
     * GET /api/v1/task/{taskId}
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getTask(@PathVariable Long taskId) {
        return success(approvalService.getById(taskId));
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
}
