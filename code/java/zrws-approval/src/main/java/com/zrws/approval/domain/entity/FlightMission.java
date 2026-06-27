package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 飞行任务实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_flight_mission")
public class FlightMission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long missionId;

    private String missionCode;

    private String areaName;

    private String droneId;

    private String droneModel;

    private String operator;

    private LocalDateTime flightTime;

    private Integer duration;

    private Double coverage;

    private Double altitude;

    private Double forwardOverlap;

    private Double sideOverlap;

    private Integer photoCount;

    private Long lidarPoints;

    private Integer soilSamples;

    private String gpsAccuracyH;

    private String gpsAccuracyV;

    private String status;

    private Double centerLat;

    private Double centerLng;

    private String weather;

    private String remark;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum Status {
        PENDING("待执行"),
        PROCESSING("进行中"),
        COMPLETED("已完成"),
        ABNORMAL("异常");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
