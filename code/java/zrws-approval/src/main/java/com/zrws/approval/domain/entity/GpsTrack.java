package com.zrws.approval.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS航迹实体（虚拟概念，基于航迹点聚合）
 */
@Data
public class GpsTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long trackId;

    private Long missionId;

    private String missionCode;

    private String trackName;

    private Integer pointCount;

    private Double totalDistance;

    private Double maxAltitude;

    private Double minAltitude;

    private Double avgSpeed;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private List<GpsTrackPoint> points;
}