package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 土壤/岩石标准分类实体
 * 包含中国土壤分类、国际WRB分类、岩石分类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_geo_standard")
public class GeoStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long standardId;

    private String standardCode;

    private String standardName;

    private String category;

    private String subcategory;

    private String classificationSystem;

    private String parentMaterial;

    private String chemicalComposition;

    private String mineralComposition;

    private String physicalProperties;

    private String mechanicalProperties;

    private String typicalElements;

    private String formationEnvironment;

    private String distribution;

    private String colorDescription;

    private String textureDescription;

    private String structureDescription;

    private Double hardnessMin;

    private Double hardnessMax;

    private Double densityMin;

    private Double densityMax;

    private Double porosityMin;

    private Double porosityMax;

    private Double permeabilityMin;

    private Double permeabilityMax;

    private Integer sortOrder;

    private String status;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum Category {
        SOIL_CHINA("中国土壤分类"),
        SOIL_WRB("国际WRB土壤分类"),
        ROCK_IGNEOUS("岩浆岩"),
        ROCK_SEDIMENTARY("沉积岩"),
        ROCK_METAMORPHIC("变质岩"),
        MINERAL("矿物"),
        SOIL_TEXTURE("土壤质地"),
        SOIL_STRUCTURE("土壤结构");

        private final String desc;

        Category(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum ClassificationSystem {
        CST("中国土壤分类系统"),
        WRB("世界土壤资源参比基础(WRB)"),
        USDA("美国农业部土壤分类(USDA)"),
        IUGS("国际地科联岩石分类(IUGS)"),
        GB_T_17412("中国岩石分类GB/T 17412");

        private final String desc;

        ClassificationSystem(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
