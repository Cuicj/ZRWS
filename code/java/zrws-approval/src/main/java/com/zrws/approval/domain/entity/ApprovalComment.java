package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 审批意见表 实体 */
@Data
@TableName("zrws_approval_comment")
public class ApprovalComment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long commentId;

    private Long taskId;
    private String flowInstanceId;
    private String stepKey;
    private String stepName;

    private Long approverId;
    private String approverName;

    /** APPROVE / REJECT / RETURN / SUBMIT */
    private String action;
    private String opinion;

    private Integer isDeleted;
    private Long tenantId;
    private LocalDateTime createdTime;
}
