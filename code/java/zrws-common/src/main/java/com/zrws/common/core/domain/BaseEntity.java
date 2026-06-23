package com.zrws.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类继承此类
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建者 */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新者 */
    private Long updatedBy;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 备注 */
    private String remark;
}