package com.zrws.approval.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zrws.approval.domain.entity.ApprovalTask;
import com.zrws.approval.mapper.ApprovalTaskMapper;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 超时升级调度器
 * <p>定时检查所有 PROCESSING 状态的审批任务，若超过 sla_deadline 未处理则自动升级
 * <p>Cron: 每 5 分钟检查一次
 */
@Component
public class SlaEscalationScheduler {

    @Autowired
    private ApprovalTaskMapper taskMapper;

    @Autowired
    private TaskService taskService;

    /**
     * 每 5 分钟检查一次超时任务
     * Cron: 0 0/5 * * * ? (每5分钟)
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkEscalation() {
        List<ApprovalTask> tasks = taskMapper.selectList(new QueryWrapper<ApprovalTask>()
                .eq("status", "PROCESSING")
                .isNotNull("sla_deadline")
                .lt("sla_deadline", LocalDateTime.now())
        );

        if (tasks == null || tasks.isEmpty()) return;

        for (ApprovalTask task : tasks) {
            // 1. 在 Flowable 中查找当前任务
            Task flowableTask = taskService.createTaskQuery()
                    .processInstanceId(task.getFlowInstanceId())
                    .singleResult();

            if (flowableTask == null) continue;

            // 2. 自动升级为"由更高权限角色处理" - 简化：将当前任务的 assignee 指向上级角色
            Long currentAssignee;
            try {
                currentAssignee = Long.valueOf(flowableTask.getAssignee());
            } catch (Exception e) {
                continue;
            }
            Long escalationAssignee = currentAssignee + 1;
            taskService.setAssignee(flowableTask.getId(), String.valueOf(escalationAssignee));

            // 3. 延长 SLA 时间 (再给 4 小时)
            task.setSlaDeadline(LocalDateTime.now().plusHours(4));
            task.setUpdatedTime(LocalDateTime.now());
            taskMapper.updateById(task);

            System.out.println("[SLA] 审批任务 " + task.getTaskId() + " 超时，已从 " + currentAssignee
                    + " 升级到 " + escalationAssignee + "，新 SLA: " + task.getSlaDeadline());
        }
    }
}
