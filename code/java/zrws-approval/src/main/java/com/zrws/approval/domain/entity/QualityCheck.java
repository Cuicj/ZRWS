package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 质量校验实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_quality_check")
public class QualityCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long checkId;

    private String checkCode;

    private Long batchId;

    private Long missionId;

    private String missionCode;

    private String checkType;

    private String checkItem;

    private Integer totalCount;

    private Integer passCount;

    private Integer failCount;

    private Double passRate;

    private String status;

    private String failDetails;

    private String checker;

    private LocalDateTime checkTime;

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

    public enum CheckType {
        PHOTO_QUALITY("照片质量"),
        GPS_COMPLETENESS("GPS完整性"),
        LIDAR_DENSITY("LiDAR密度"),
        OVERLAP_RATE("重叠率"),
        COORDINATE_ACCURACY("坐标精度"),
        DATA_INTEGRITY("数据完整性");

        private final String desc;

        CheckType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        PENDING("待校验"),
        PROCESSING("校验中"),
        PASSED("通过"),
        FAILED("未通过"),
        PARTIAL("部分通过");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
