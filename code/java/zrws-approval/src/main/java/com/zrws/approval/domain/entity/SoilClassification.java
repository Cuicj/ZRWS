package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 土质分类分析实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_soil_classification")
public class SoilClassification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long analysisId;

    private String analysisCode;

    private Long missionId;

    private String missionCode;

    private String analysisName;

    private Integer sampleCount;

    private String soilType;

    private String soilSubtype;

    private Double confidence;

    private String description;

    private Double phValue;

    private Double organicMatter;

    private Double moisture;

    private Double nitrogen;

    private Double phosphorus;

    private Double potassium;

    private String texture;

    private String structure;

    private String color;

    private Double depth;

    private String parentMaterial;

    private String vegetation;

    private String aiAnalysis;

    private String aiSuggestion;

    private String status;

    private String analyst;

    private LocalDateTime analysisTime;

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

    public enum SoilType {
        PADDY_SOIL("水稻土"),
        GARDEN_SOIL("菜园土"),
        YELLOW_BROWN_EARTH("黄棕壤"),
        RED_SOIL("红壤"),
        LATOSOLIC_RED_EARTH("砖红壤性红壤"),
        LIMESTONE_SOIL("石灰土"),
        PURPLE_SOIL("紫色土"),
        ALLUVIAL_SOIL("冲积土");

        private final String desc;

        SoilType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
