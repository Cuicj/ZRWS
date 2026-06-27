package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务主表 实体
 */
@Data
@TableName("zrws_approval_task")
public class ApprovalTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long taskId;

    /** Flowable 流程实例ID */
    private String flowInstanceId;

    /** 业务类型: STANDARD / MATERIAL / DRONE_FLIGHT / EMERGENCY */
    private String bizType;

    /** 关联业务ID */
    private Long bizId;

    /** 业务标题 */
    private String bizTitle;

    private Long applicantId;
    private String applicantName;
    private String applicantDept;

    /** 当前步骤名称 */
    private String curStep;
    private String curStepKey;

    /** DRAFT/PROCESSING/PASSED/REJECTED/RETURNED/ARCHIVED */
    private String status;

    private Integer priority;

    /** SLA 截止时间 */
    private LocalDateTime slaDeadline;

    /** 业务数据JSON */
    private String bizData;

    private Integer isDeleted;
    private Long tenantId;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
}
