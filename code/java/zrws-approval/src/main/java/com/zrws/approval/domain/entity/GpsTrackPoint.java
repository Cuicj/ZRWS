package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * GPS航迹点实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_gps_track_point")
public class GpsTrackPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long pointId;

    private Long missionId;

    private String missionCode;

    private Integer sequence;

    private Double latitude;

    private Double longitude;

    private Double altitude;

    private Double speed;

    private Double heading;

    private String gpsTime;

    private Integer satellites;

    private String fixType;

    private Double accuracyH;

    private Double accuracyV;

    private String pointType;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum PointType {
        TAKEOFF("起飞"),
        LANDING("降落"),
        SCAN("扫描"),
        SAMPLE("采样"),
        WAYPOINT("航点");

        private final String desc;

        PointType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
