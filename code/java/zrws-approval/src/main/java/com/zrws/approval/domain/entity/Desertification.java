package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_desertification")
public class Desertification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private String recordCode;

    private String region;

    private String regionCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDate monitorDate;

    private String monitorPeriod;

    private BigDecimal vegetationCoverage;

    private String vegetationTrend;

    private BigDecimal bareLandRatio;

    private BigDecimal sandDuneHeightAvg;

    private BigDecimal sandDuneMigrationRate;

    private BigDecimal soilOrganicMatter;

    private BigDecimal soilMoisture;

    private BigDecimal aridityIndex;

    private String climateType;

    private BigDecimal windErosionModulus;

    private String desertificationType;

    private String desertificationGrade;

    private BigDecimal desertificationArea;

    private BigDecimal desertificationRatio;

    private BigDecimal landDegradationIndex;

    private String riskLevel;

    private BigDecimal riskScore;

    private String impactAssessment;

    private String controlMeasures;

    private String dataSource;

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

    public enum MonitorPeriod {
        MONTHLY("月度"),
        QUARTERLY("季度"),
        YEARLY("年度");

        private final String desc;

        MonitorPeriod(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum VegetationTrend {
        INCREASING("增加"),
        STABLE("稳定"),
        DECREASING("减少");

        private final String desc;

        VegetationTrend(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum ClimateType {
        HUMID("湿润区"),
        SEMI_HUMID("半湿润区"),
        SEMI_ARID("半干旱区"),
        ARID("干旱区"),
        HYPER_ARID("极干旱区");

        private final String desc;

        ClimateType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum DesertificationType {
        WIND("风蚀沙漠化"),
        WATER("水蚀沙漠化"),
        SALT("盐渍化沙漠化"),
        FREEZE("冻融沙漠化");

        private final String desc;

        DesertificationType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum DesertificationGrade {
        MILD("轻度"),
        MODERATE("中度"),
        SEVERE("重度"),
        EXTREME("极重度");

        private final String desc;

        DesertificationGrade(String desc) {
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
