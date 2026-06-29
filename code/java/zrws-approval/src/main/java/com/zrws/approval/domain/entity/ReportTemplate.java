package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报表模板实体
 * <p>定义报表的模板结构、数据源和参数配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_report_template")
public class ReportTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long templateId;

    private String templateCode;

    private String templateName;

    private String templateType;

    private String category;

    private String description;

    private String templateContent;

    private String dataSource;

    private String parameters;

    private String outputFormat;

    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    private Long tenantId;

    @TableLogic
    private Integer isDeleted;

    public enum TemplateType {
        STANDARD("标准模板"),
        CUSTOM("自定义模板");

        private final String desc;

        TemplateType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Category {
        DASHBOARD("仪表盘"),
        SOIL("土质分析"),
        DISASTER("灾害风险"),
        ROCK("岩层分析"),
        DEVICE("设备统计"),
        QUALITY("质量检测");

        private final String desc;

        Category(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum OutputFormat {
        PDF("PDF"),
        EXCEL("Excel"),
        WORD("Word");

        private final String desc;

        OutputFormat(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        ACTIVE("启用"),
        INACTIVE("禁用");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
