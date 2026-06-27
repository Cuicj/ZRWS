package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 土壤采样实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_soil_sample")
public class SoilSample implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long sampleId;

    private String sampleCode;

    private Long missionId;

    private String missionCode;

    private Double latitude;

    private Double longitude;

    private Double elevation;

    private Double phValue;

    private Double moisture;

    private Integer ecValue;

    private String soilType;

    private String soilTexture;

    private Double organicMatter;

    private Double nitrogen;

    private Double phosphorus;

    private Double potassium;

    private String depth;

    private String collector;

    private LocalDateTime collectTime;

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

    public enum SoilType {
        LOAM("壤土"),
        CLAY("黏土"),
        SAND("砂土"),
        SILT("粉土"),
        OTHER("其他");

        private final String desc;

        SoilType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
