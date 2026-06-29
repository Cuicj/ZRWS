package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_api_key")
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long keyId;

    private String apiKey;

    private String apiSecret;

    private Long companyId;

    private String companyName;

    private String permissions;

    private Integer rateLimit;

    private Integer requestCount;

    private LocalDateTime lastRequestTime;

    private String status;

    private LocalDateTime expireTime;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    private Long tenantId;

    @TableLogic
    private Integer isDeleted;

    public enum Status {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE"),
        DISABLED("DISABLED");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
