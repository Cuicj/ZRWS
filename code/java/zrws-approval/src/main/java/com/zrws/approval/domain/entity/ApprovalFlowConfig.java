package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批流配置实体
 * <p>配置哪些BO操作需要触发审批流
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("approval_flow_config")
public class ApprovalFlowConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /** BO编码 */
    private String boCode;

    /** BO名称 */
    private String boName;

    /** 操作类型: INSERT/UPDATE/DELETE/IMPORT */
    private String operationType;

    /** 操作类型描述 */
    private String operationDesc;

    /** 是否启用审批流 */
    private Integer enableApproval;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程Key */
    private String processKey;

    /** 流程名称 */
    private String processName;

    /** 审批级别: SINGLE(单级)/MULTI(多级)/CUSTOM(自定义) */
    private String approvalLevel;

    /** 审批人配置(JSON) */
    private String approverConfig;

    /** 条件表达式(当满足条件时触发审批) */
    private String conditionExpr;

    /** 优先级 */
    private Integer priority;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    /** 描述说明 */
    private String description;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建者 */
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新者 */
    private Long updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        INSERT("新增"),
        UPDATE("修改"),
        DELETE("删除"),
        IMPORT("导入");

        private final String desc;

        OperationType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 审批级别枚举
     */
    public enum ApprovalLevel {
        SINGLE("单级审批"),
        MULTI("多级审批"),
        CUSTOM("自定义审批");

        private final String desc;

        ApprovalLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}