package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.GpsTrack;
import com.zrws.approval.domain.entity.GpsTrackPoint;
import com.zrws.approval.service.GpsTrackService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/gps-track")
@CrossOrigin(origins = "*")
public class GpsTrackController {

    @Autowired
    private GpsTrackService gpsTrackService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(required = false) Long missionId) {
        try {
            List<GpsTrack> tracks = gpsTrackService.getTrackList(missionId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", tracks);
            result.put("total", tracks.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询航迹列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<GpsTrack> getById(@PathVariable Long id) {
        try {
            GpsTrack track = gpsTrackService.getTrackById(id);
            if (track == null) {
                return R.fail("航迹不存在");
            }
            return R.ok(track);
        } catch (Exception e) {
            log.error("查询航迹详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/points/{trackId}")
    public R<Map<String, Object>> getPoints(@PathVariable Long trackId) {
        try {
            List<GpsTrackPoint> points = gpsTrackService.getPointsByTrackId(trackId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", points);
            result.put("total", points.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询航迹点列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public R<GpsTrack> create(@RequestBody GpsTrack track) {
        try {
            Long trackId = gpsTrackService.createTrack(track);
            track.setTrackId(trackId);
            return R.ok("创建成功", track);
        } catch (Exception e) {
            log.error("创建航迹失败", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            gpsTrackService.deleteTrack(id);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除航迹失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
