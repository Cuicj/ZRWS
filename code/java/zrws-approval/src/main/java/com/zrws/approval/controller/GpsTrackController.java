package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.GpsTrack;
import com.zrws.approval.domain.entity.GpsTrackPoint;
import com.zrws.approval.service.GpsTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) Long missionId) {
        try {
            List<GpsTrack> tracks = gpsTrackService.getTrackList(missionId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", tracks);
            result.put("total", tracks.size());
            return success(result);
        } catch (Exception e) {
            log.error("查询航迹列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            GpsTrack track = gpsTrackService.getTrackById(id);
            if (track == null) {
                return error("航迹不存在");
            }
            return success(Collections.singletonMap("data", track));
        } catch (Exception e) {
            log.error("查询航迹详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/points/{trackId}")
    public ResponseEntity<Map<String, Object>> getPoints(@PathVariable Long trackId) {
        try {
            List<GpsTrackPoint> points = gpsTrackService.getPointsByTrackId(trackId);
            Map<String, Object> result = new HashMap<>();
            result.put("list", points);
            result.put("total", points.size());
            return success(result);
        } catch (Exception e) {
            log.error("查询航迹点列表失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody GpsTrack track) {
        try {
            Long trackId = gpsTrackService.createTrack(track);
            track.setTrackId(trackId);
            Map<String, Object> result = new HashMap<>();
            result.put("data", track);
            result.put("message", "创建成功");
            return success(result);
        } catch (Exception e) {
            log.error("创建航迹失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            gpsTrackService.deleteTrack(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除航迹失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<String, Object>) data);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("msg", message);
        return ResponseEntity.badRequest().body(result);
    }
}