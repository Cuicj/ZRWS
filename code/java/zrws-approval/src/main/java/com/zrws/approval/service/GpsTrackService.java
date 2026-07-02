package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.GpsTrack;
import com.zrws.approval.domain.entity.GpsTrackPoint;
import com.zrws.approval.mapper.GpsTrackPointMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GpsTrackService {

    @Autowired
    private GpsTrackPointMapper gpsTrackPointMapper;

    public List<GpsTrack> getTrackList(Long missionId) {
        LambdaQueryWrapper<GpsTrackPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GpsTrackPoint::getIsDeleted, 0);
        if (missionId != null) {
            wrapper.eq(GpsTrackPoint::getMissionId, missionId);
        }
        wrapper.orderByAsc(GpsTrackPoint::getSequence);

        List<GpsTrackPoint> allPoints = gpsTrackPointMapper.selectList(wrapper);

        Map<Long, List<GpsTrackPoint>> pointsByMission = allPoints.stream()
                .collect(Collectors.groupingBy(GpsTrackPoint::getMissionId));

        List<GpsTrack> tracks = new ArrayList<>();
        for (Map.Entry<Long, List<GpsTrackPoint>> entry : pointsByMission.entrySet()) {
            GpsTrack track = createTrackFromPoints(entry.getKey(), entry.getValue());
            tracks.add(track);
        }

        tracks.sort((t1, t2) -> {
            if (t1.getStartTime() == null) return 1;
            if (t2.getStartTime() == null) return -1;
            return t2.getStartTime().compareTo(t1.getStartTime());
        });

        return tracks;
    }

    public GpsTrack getTrackById(Long trackId) {
        LambdaQueryWrapper<GpsTrackPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GpsTrackPoint::getIsDeleted, 0);
        wrapper.eq(GpsTrackPoint::getMissionId, trackId);
        wrapper.orderByAsc(GpsTrackPoint::getSequence);

        List<GpsTrackPoint> points = gpsTrackPointMapper.selectList(wrapper);

        if (points.isEmpty()) {
            return null;
        }

        return createTrackFromPoints(trackId, points);
    }

    public List<GpsTrackPoint> getPointsByTrackId(Long trackId) {
        LambdaQueryWrapper<GpsTrackPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GpsTrackPoint::getIsDeleted, 0);
        wrapper.eq(GpsTrackPoint::getMissionId, trackId);
        wrapper.orderByAsc(GpsTrackPoint::getSequence);
        return gpsTrackPointMapper.selectList(wrapper);
    }

    private GpsTrack createTrackFromPoints(Long missionId, List<GpsTrackPoint> points) {
        GpsTrack track = new GpsTrack();
        track.setTrackId(missionId);
        track.setMissionId(missionId);
        track.setPointCount(points.size());

        if (!points.isEmpty()) {
            track.setMissionCode(points.get(0).getMissionCode());
            track.setTrackName("航迹 - 任务" + missionId);
            track.setPoints(points);

            Double maxAlt = points.stream()
                    .map(GpsTrackPoint::getAltitude)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(0.0);
            track.setMaxAltitude(maxAlt);

            Double minAlt = points.stream()
                    .map(GpsTrackPoint::getAltitude)
                    .filter(Objects::nonNull)
                    .min(Double::compareTo)
                    .orElse(0.0);
            track.setMinAltitude(minAlt);

            Double avgSpeed = points.stream()
                    .map(GpsTrackPoint::getSpeed)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            track.setAvgSpeed(avgSpeed);

            LocalDateTime start = points.stream()
                    .map(p -> parseGpsTime(p.getGpsTime()))
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            track.setStartTime(start);

            LocalDateTime end = points.stream()
                    .map(p -> parseGpsTime(p.getGpsTime()))
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            track.setEndTime(end);

            track.setTotalDistance(calculateTotalDistance(points));
            track.setStatus("ACTIVE");
        }

        return track;
    }

    private LocalDateTime parseGpsTime(String gpsTime) {
        if (!StringUtils.hasText(gpsTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(gpsTime);
        } catch (Exception e) {
            log.warn("解析GPS时间失败: {}", gpsTime);
            return null;
        }
    }

    private Double calculateTotalDistance(List<GpsTrackPoint> points) {
        double totalDistance = 0.0;
        for (int i = 1; i < points.size(); i++) {
            GpsTrackPoint prev = points.get(i - 1);
            GpsTrackPoint curr = points.get(i);
            if (prev.getLatitude() != null && prev.getLongitude() != null &&
                curr.getLatitude() != null && curr.getLongitude() != null) {
                totalDistance += calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
                );
            }
        }
        return totalDistance;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public Long createTrack(GpsTrack track) {
        if (track.getPoints() != null && !track.getPoints().isEmpty()) {
            int sequence = 1;
            for (GpsTrackPoint point : track.getPoints()) {
                point.setMissionId(track.getMissionId());
                point.setSequence(sequence++);
                point.setIsDeleted(0);
                gpsTrackPointMapper.insert(point);
            }
        }
        return track.getMissionId();
    }

    public void deleteTrack(Long trackId) {
        LambdaQueryWrapper<GpsTrackPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GpsTrackPoint::getMissionId, trackId);
        gpsTrackPointMapper.delete(wrapper);
    }
}