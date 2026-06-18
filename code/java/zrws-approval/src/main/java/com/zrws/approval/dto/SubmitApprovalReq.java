package com.zrws.approval.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 提交审批请求 DTO
 */
@Data
public class SubmitApprovalReq {

    /** 业务类型：STANDARD / MATERIAL / DRONE_FLIGHT / EMERGENCY */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** 关联业务ID */
    private Long bizId;

    /** 业务标题 */
    @NotBlank(message = "业务标题不能为空")
    private String bizTitle;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    @NotBlank(message = "申请人姓名不能为空")
    private String applicantName;

    /** 申请部门 */
    private String applicantDept;

    /** 0-普通 1-高 2-紧急 */
    private Integer priority = 0;

    /** 业务数据 JSON（可选） */
    private String bizData;
}
