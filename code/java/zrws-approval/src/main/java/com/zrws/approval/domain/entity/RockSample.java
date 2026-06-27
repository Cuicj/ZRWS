package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岩矿样品成分分析实体
 * 用于存储取样法分析的样品成分数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_rock_sample")
public class RockSample implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long sampleId;

    private String sampleCode;

    private Long analysisId;

    private String analysisCode;

    private Long missionId;

    private String missionCode;

    private String sampleName;

    private String sampleType;

    private String location;

    private Double latitude;

    private Double longitude;

    private Double elevation;

    private Double depth;

    private String depthUnit;

    private Double weight;

    private String collector;

    private LocalDateTime collectTime;

    private String lithologyEstimate;

    private String color;

    private String texture;

    private String structure;

    private String weatheringDegree;

    private String chemicalData;

    private String mineralData;

    private String traceElements;

    private String physicalData;

    private String analysisMethod;

    private String analysisInstrument;

    private String analyst;

    private LocalDateTime analysisTime;

    private String aiIdentification;

    private Double aiConfidence;

    private String aiMatchedStandards;

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

    public enum SampleType {
        ROCK("岩石样品"),
        SOIL("土壤样品"),
        ORE("矿石样品"),
        WATER("水样"),
        GAS("气样");

        private final String desc;

        SampleType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum WeatheringDegree {
        FRESH("新鲜"),
        SLIGHTLY_WEATHERED("微风化"),
        MODERATELY_WEATHERED("中等风化"),
        HIGHLY_WEATHERED("强风化"),
        COMPLETELY_WEATHERED("全风化"),
        RESIDUAL_SOIL("残积土");

        private final String desc;

        WeatheringDegree(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum AnalysisMethod {
        XRF("X射线荧光光谱(XRF)"),
        ICP("等离子体发射光谱(ICP)"),
        XRD("X射线衍射(XRD)"),
        AAS("原子吸收光谱(AAS)"),
        ICP_MS("电感耦合等离子体质谱(ICP-MS)"),
        CHEMICAL("化学分析"),
        SPECTROSCOPY("光谱分析"),
        MICROSCOPE("显微镜鉴定"),
        COMPREHENSIVE("综合分析");

        private final String desc;

        AnalysisMethod(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
