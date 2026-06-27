package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地块实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_land_plot")
public class LandPlot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long plotId;

    private String plotCode;

    private String plotName;

    private String owner;

    private String landType;

    private String landUse;

    private Double gpsArea;

    private Double registeredArea;

    private Double areaDiff;

    private String status;

    private String region;

    private String province;

    private String city;

    private String county;

    private String township;

    private String village;

    private Double centerLat;

    private Double centerLng;

    private String boundaryGeoJson;

    private String soilType;

    private Double fertilityLevel;

    private String irrigationType;

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
        NORMAL("正常"),
        ABNORMAL("异常"),
        REVIEW("待复核");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
