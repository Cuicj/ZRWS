package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_external_company")
public class ExternalCompany implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long companyId;

    private String companyName;

    private String companyCode;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String apiConfig;

    private String callbackUrl;

    private String dataScope;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    private Long tenantId;

    @TableLogic
    private Integer isDeleted;

    public enum Status {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DataScope {
        ALL("ALL"),
        TENANT("TENANT"),
        SELF("SELF");

        private final String value;

        DataScope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
