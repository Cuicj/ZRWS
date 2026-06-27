package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 灾害风险评估实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_disaster_risk")
public class DisasterRisk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long riskId;

    private String riskCode;

    private Long missionId;

    private String missionCode;

    private String region;

    private Double latitude;

    private Double longitude;

    private String disasterType;

    private String riskLevel;

    private Double riskScore;

    private String description;

    private String influencingFactors;

    private String monitoringData;

    private String historicalRecords;

    private String aiAnalysis;

    private String aiSuggestion;

    private Double aiConfidence;

    private String status;

    private String analyst;

    private LocalDateTime assessmentTime;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum DisasterType {
        LANDSLIDE("滑坡"),
        DEBRIS_FLOW("泥石流"),
        GROUND_SUBSIDENCE("地面沉降"),
        FLOOD("洪涝"),
        SOIL_EROSION("水土流失"),
        COLLAPSE("崩塌"),
        EARTHQUAKE("地震");

        private final String desc;

        DisasterType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险"),
        EXTREME("极高风险");

        private final String desc;

        RiskLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
