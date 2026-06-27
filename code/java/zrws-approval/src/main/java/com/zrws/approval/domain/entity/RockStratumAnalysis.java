package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岩层结构分析任务实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_rock_stratum_analysis")
public class RockStratumAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long analysisId;

    private String analysisCode;

    private Long missionId;

    private String missionCode;

    private String projectName;

    private String location;

    private Double latitude;

    private Double longitude;

    private Double elevation;

    private String analysisType;

    private String dataSource;

    private Integer boreholeCount;

    private Double maxDepth;

    private Integer stratumCount;

    private String stratumData;

    private String lithologyData;

    private String structureData;

    private String faultData;

    private String aiAlgorithm;

    private String aiModelVersion;

    private Double aiConfidence;

    private String aiSummary;

    private String aiDetail;

    private String riskLevel;

    private String suggestion;

    private String analyst;

    private LocalDateTime analysisTime;

    private LocalDateTime reportTime;

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

    public enum AnalysisType {
        COMPREHENSIVE("综合分析"),
        BOREHOLE("钻孔分析"),
        GEOPHYSICAL("物探分析"),
        GPR("地质雷达"),
        SAMPLING("取样分析");

        private final String desc;

        AnalysisType(String desc) {
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
        VERY_HIGH("极高风险");

        private final String desc;

        RiskLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum AiAlgorithm {
        CNN("CNN卷积神经网络"),
        TRANSFORMER("Transformer模型"),
        RANDOM_FOREST("随机森林"),
        SVM("支持向量机"),
        ENSEMBLE("集成学习"),
        DEEP_LEARNING("深度学习");

        private final String desc;

        AiAlgorithm(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
