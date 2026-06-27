package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_device")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String deviceModel;

    private String manufacturer;

    private String serialNumber;

    private String status;

    private Integer batteryLevel;

    private Integer signalLevel;

    private Integer storageLevel;

    private Double temperature;

    private String firmwareVersion;

    private String ipAddress;

    private String location;

    private Double latitude;

    private Double longitude;

    private String owner;

    private String department;

    private LocalDateTime purchaseDate;

    private LocalDateTime lastMaintenance;

    private LocalDateTime lastOnline;

    private Integer totalFlights;

    private Double totalFlightHours;

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

    public enum DeviceType {
        DRONE("无人机"),
        CAMERA("相机"),
        LIDAR("激光雷达"),
        GPS("GPS设备"),
        SOIL_SENSOR("土壤传感器"),
        GPR("地质雷达"),
        COMPUTER("工作站"),
        OTHER("其他");

        private final String desc;

        DeviceType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        ONLINE("在线"),
        OFFLINE("离线"),
        WORKING("作业中"),
        CHARGING("充电中"),
        MAINTENANCE("维护中"),
        FAULT("故障");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
