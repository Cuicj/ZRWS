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
@TableName("zrws_climate_warming")
public class ClimateWarming implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private String recordCode;

    private String region;

    private String regionCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDate monitorDate;

    private BigDecimal avgTemperature;

    private BigDecimal maxTemperature;

    private BigDecimal minTemperature;

    private BigDecimal temperatureAnomaly;

    private BigDecimal precipitation;

    private BigDecimal precipitationAnomaly;

    private Integer extremeHighTempDays;

    private Integer extremeLowTempDays;

    private Integer droughtDays;

    private Integer heatWaveEvents;

    private BigDecimal warmingRate10y;

    private String warmingTrend;

    private String riskLevel;

    private BigDecimal riskScore;

    private String impactAssessment;

    private String adaptationMeasures;

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

    public enum WarmingTrend {
        RAPID("快速变暖"),
        MODERATE("中等变暖"),
        SLOW("缓慢变暖"),
        STABLE("稳定"),
        COOLING("降温趋势");

        private final String desc;

        WarmingTrend(String desc) {
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
