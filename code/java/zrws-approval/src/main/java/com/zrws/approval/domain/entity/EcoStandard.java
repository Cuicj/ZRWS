package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_eco_standard")
public class EcoStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long standardId;

    private String standardCode;

    private String standardName;

    private String category;

    private String subcategory;

    private String standardSystem;

    private String gradeLevel;

    private BigDecimal thresholdMin;

    private BigDecimal thresholdMax;

    private String unit;

    private String description;

    private String indicatorParams;

    private String referenceStandard;

    private Integer sortOrder;

    private String status;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    private Long tenantId;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum Category {
        CLIMATE_WARMING("气候变暖"),
        DESERTIFICATION("沙漠化"),
        SOIL_EROSION("水土流失"),
        ECO_SAFETY("生态安全"),
        CLIMATE_ZONE("气候分区");

        private final String desc;

        Category(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum GradeLevel {
        LOW("低"),
        MILD("轻度"),
        LIGHT("轻微"),
        MODERATE("中度"),
        MODERATE_HIGH("中高"),
        SEVERE("重度"),
        VERY_SEVERE("极强烈"),
        EXTREME("极高/剧烈"),
        HUMID("湿润"),
        SEMI_HUMID("半湿润"),
        SEMI_ARID("半干旱"),
        ARID("干旱"),
        HYPER_ARID("极干旱"),
        BLUE("蓝色预警"),
        YELLOW("黄色预警"),
        ORANGE("橙色预警"),
        RED("红色预警");

        private final String desc;

        GradeLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
