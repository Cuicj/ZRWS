package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.Device;
import com.zrws.approval.domain.entity.FlightMission;
import com.zrws.approval.mapper.DeviceMapper;
import com.zrws.approval.mapper.FlightMissionMapper;
import com.zrws.approval.service.UnifiedDroneSDKService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 无人机管理 REST API
 * 提供无人机设备连接、任务管理、实时监控等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/drone")
@CrossOrigin(origins = "*")
public class DroneController {

    @Autowired
    private UnifiedDroneSDKService droneSDKService;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private FlightMissionMapper missionMapper;

    // ==================== 设备连接管理 ====================

    /**
     * 获取无人机设备列表
     */
    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> listDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getDeviceType, "DRONE");
        wrapper.eq(Device::getIsDeleted, 0);
        
        if (brand != null && !brand.isEmpty()) {
            wrapper.like(Device::getDeviceModel, brand);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Device::getStatus, status);
        }
        
        List<Device> devices = deviceMapper.selectList(wrapper);
        
        // 补充连接状态
        List<Map<String, Object>> deviceList = devices.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("deviceId", d.getDeviceId());
            map.put("deviceCode", d.getDeviceCode());
            map.put("deviceName", d.getDeviceName());
            map.put("deviceModel", d.getDeviceModel());
            map.put("manufacturer", d.getManufacturer());
            map.put("serialNumber", d.getSerialNumber());
            map.put("status", d.getStatus());
            map.put("batteryLevel", d.getBatteryLevel());
            map.put("signalLevel", d.getSignalLevel());
            map.put("firmwareVersion", d.getFirmwareVersion());
            map.put("location", d.getLocation());
            map.put("latitude", d.getLatitude());
            map.put("longitude", d.getLongitude());
            map.put("totalFlights", d.getTotalFlights());
            map.put("totalFlightHours", d.getTotalFlightHours());
            map.put("lastOnline", d.getLastOnline());
            
            // 判断是否已连接
            map.put("isConnected", droneSDKService.getActiveConnections().containsKey(d.getDeviceId().toString()));
            
            return map;
        }).collect(Collectors.toList());
        
        return success(Map.of("list", deviceList, "total", deviceList.size()));
    }

    /**
     * 连接无人机
     */
    @PostMapping("/connect/{deviceId}")
    public ResponseEntity<Map<String, Object>> connectDrone(@PathVariable Long deviceId) {
        try {
            UnifiedDroneSDKService.DroneConnection connection = droneSDKService.connectDrone(deviceId.toString());
            return success(Map.of(
                "connectionId", connection.getConnectionId(),
                "deviceId", connection.getDeviceId(),
                "brand", connection.getBrand().getDesc(),
                "model", connection.getModel(),
                "status", connection.getStatus(),
                "message", "连接成功"
            ));
        } catch (Exception e) {
            log.error("连接无人机失败", e);
            return error("连接失败: " + e.getMessage());
        }
    }

    /**
     * 断开无人机连接
     */
    @PostMapping("/disconnect/{deviceId}")
    public ResponseEntity<Map<String, Object>> disconnectDrone(@PathVariable Long deviceId) {
        try {
            droneSDKService.disconnectDrone(deviceId.toString());
            return success(Map.of("message", "断开连接成功"));
        } catch (Exception e) {
            log.error("断开连接失败", e);
            return error("断开失败: " + e.getMessage());
        }
    }

    /**
     * 获取活跃连接列表
     */
    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getActiveConnections() {
        Map<String, UnifiedDroneSDKService.DroneConnection> connections = droneSDKService.getActiveConnections();
        List<Map<String, Object>> list = connections.values().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("connectionId", c.getConnectionId());
            map.put("deviceId", c.getDeviceId());
            map.put("brand", c.getBrand().getDesc());
            map.put("model", c.getModel());
            map.put("status", c.getStatus());
            map.put("connectTime", c.getConnectTime());
            map.put("firmwareVersion", c.getFirmwareVersion());
            return map;
        }).collect(Collectors.toList());
        
        return success(Map.of("list", list, "total", list.size()));
    }

    // ==================== 任务管理 ====================

    /**
     * 上传航点任务
     */
    @PostMapping("/missions/waypoint")
    public ResponseEntity<Map<String, Object>> uploadWaypointMission(
            @RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waypointData = (List<Map<String, Object>>) request.get("waypoints");
            
            List<UnifiedDroneSDKService.Waypoint> waypoints = waypointData.stream().map(wp -> {
                UnifiedDroneSDKService.Waypoint wp2 = new UnifiedDroneSDKService.Waypoint();
                wp2.setLatitude(((Number) wp.get("latitude")).doubleValue());
                wp2.setLongitude(((Number) wp.get("longitude")).doubleValue());
                wp2.setAltitude(((Number) wp.get("altitude")).doubleValue());
                wp2.setSpeed(((Number) wp.getOrDefault("speed", 5.0)).doubleValue());
                wp2.setHoverTime((Integer) wp.getOrDefault("hoverTime", 0));
                wp2.setShootPhoto((Boolean) wp.getOrDefault("shootPhoto", false));
                wp2.setRecordVideo((Boolean) wp.getOrDefault("recordVideo", false));
                return wp2;
            }).collect(Collectors.toList());
            
            UnifiedDroneSDKService.MissionUploadResult result = 
                droneSDKService.uploadWaypointMission(deviceId, waypoints);
            
            if (result.isSuccess()) {
                return success(Map.of(
                    "missionId", result.getMissionId(),
                    "waypointCount", result.getWaypointCount(),
                    "totalDistance", result.getTotalDistance(),
                    "estimatedDuration", result.getEstimatedDuration(),
                    "message", result.getMessage()
                ));
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("上传航点任务失败", e);
            return error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传区域扫描任务
     */
    @PostMapping("/missions/polygon")
    public ResponseEntity<Map<String, Object>> uploadPolygonMission(
            @RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> polygonData = (List<Map<String, Object>>) request.get("polygon");
            List<UnifiedDroneSDKService.Coordinate> polygon = polygonData.stream().map(p -> 
                new UnifiedDroneSDKService.Coordinate(
                    ((Number) p.get("lat")).doubleValue(),
                    ((Number) p.get("lng")).doubleValue()
                )
            ).collect(Collectors.toList());
            
            UnifiedDroneSDKService.PolygonSurvey survey = new UnifiedDroneSDKService.PolygonSurvey();
            survey.setPolygon(polygon);
            survey.setAltitude(((Number) request.getOrDefault("altitude", 120.0)).doubleValue());
            survey.setSpeed(((Number) request.getOrDefault("speed", 8.0)).doubleValue());
            survey.setForwardOverlap(((Number) request.getOrDefault("forwardOverlap", 0.8)).doubleValue());
            survey.setSideOverlap(((Number) request.getOrDefault("sideOverlap", 0.7)).doubleValue());
            survey.setCapturePhoto(true);
            
            UnifiedDroneSDKService.MissionUploadResult result = 
                droneSDKService.uploadPolygonMission(deviceId, survey);
            
            if (result.isSuccess()) {
                return success(Map.of(
                    "missionId", result.getMissionId(),
                    "totalDistance", result.getTotalDistance(),
                    "estimatedDuration", result.getEstimatedDuration(),
                    "message", result.getMessage()
                ));
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("上传区域扫描任务失败", e);
            return error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 开始执行任务
     */
    @PostMapping("/missions/{deviceId}/start")
    public ResponseEntity<Map<String, Object>> startMission(@PathVariable String deviceId) {
        try {
            droneSDKService.startMission(deviceId);
            return success(Map.of("message", "任务已开始"));
        } catch (Exception e) {
            log.error("启动任务失败", e);
            return error("启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/missions/{deviceId}/stop")
    public ResponseEntity<Map<String, Object>> stopMission(@PathVariable String deviceId) {
        try {
            droneSDKService.stopMission(deviceId);
            return success(Map.of("message", "任务已停止"));
        } catch (Exception e) {
            log.error("停止任务失败", e);
            return error("停止失败: " + e.getMessage());
        }
    }

    /**
     * 返航
     */
    @PostMapping("/missions/{deviceId}/return")
    public ResponseEntity<Map<String, Object>> returnToHome(@PathVariable String deviceId) {
        try {
            droneSDKService.returnToHome(deviceId);
            return success(Map.of("message", "返航指令已发送"));
        } catch (Exception e) {
            log.error("返航失败", e);
            return error("返航失败: " + e.getMessage());
        }
    }

    // ==================== 实时监控 ====================

    /**
     * 获取实时遥测数据
     */
    @GetMapping("/telemetry/{deviceId}")
    public ResponseEntity<Map<String, Object>> getLiveTelemetry(@PathVariable String deviceId) {
        try {
            UnifiedDroneSDKService.FlightTelemetry telemetry = 
                droneSDKService.getLiveTelemetry(deviceId);
            
            if (telemetry == null) {
                return error("设备未连接或无数据");
            }
            
            return success(Map.of(
                "deviceId", telemetry.getDeviceId(),
                "brand", telemetry.getBrand().getDesc(),
                "timestamp", telemetry.getTimestamp(),
                "latitude", telemetry.getLatitude(),
                "longitude", telemetry.getLongitude(),
                "altitude", telemetry.getAltitude(),
                "absAltitude", telemetry.getAbsAltitude(),
                "speed", telemetry.getSpeed(),
                "heading", telemetry.getHeading(),
                "pitch", telemetry.getPitch(),
                "roll", telemetry.getRoll(),
                "batteryPercent", telemetry.getBatteryPercent(),
                "batteryVoltage", telemetry.getBatteryVoltage(),
                "batteryTemp", telemetry.getBatteryTemp(),
                "satelliteCount", telemetry.getSatelliteCount(),
                "gpsAccuracy", telemetry.getGpsAccuracy(),
                "rtkStatus", telemetry.getRtkStatus(),
                "signalStrength", telemetry.getSignalStrength(),
                "storagePercent", telemetry.getStoragePercent(),
                "windSpeed", telemetry.getWindSpeed(),
                "flightMode", telemetry.getFlightMode(),
                "isFlying", telemetry.isFlying()
            ));
        } catch (Exception e) {
            log.error("获取遥测数据失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有活跃设备的遥测数据
     */
    @GetMapping("/telemetry/all")
    public ResponseEntity<Map<String, Object>> getAllTelemetry() {
        Map<String, UnifiedDroneSDKService.FlightTelemetry> telemetryMap = 
            droneSDKService.getLiveTelemetryData();
        
        List<Map<String, Object>> list = telemetryMap.values().stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("deviceId", t.getDeviceId());
            map.put("brand", t.getBrand().getDesc());
            map.put("timestamp", t.getTimestamp());
            map.put("latitude", t.getLatitude());
            map.put("longitude", t.getLongitude());
            map.put("altitude", t.getAltitude());
            map.put("speed", t.getSpeed());
            map.put("batteryPercent", t.getBatteryPercent());
            map.put("isFlying", t.isFlying());
            return map;
        }).collect(Collectors.toList());
        
        return success(Map.of("list", list, "total", list.size()));
    }

    // ==================== 数据获取 ====================

    /**
     * 获取飞行任务列表
     */
    @GetMapping("/missions")
    public ResponseEntity<Map<String, Object>> listMissions(
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<FlightMission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlightMission::getIsDeleted, 0);
        
        if (missionId != null) {
            wrapper.eq(FlightMission::getMissionId, missionId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(FlightMission::getStatus, status);
        }
        
        wrapper.orderByDesc(FlightMission::getFlightTime);
        List<FlightMission> missions = missionMapper.selectList(wrapper);
        
        List<Map<String, Object>> list = missions.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("missionId", m.getMissionId());
            map.put("missionCode", m.getMissionCode());
            map.put("areaName", m.getAreaName());
            map.put("droneId", m.getDroneId());
            map.put("droneModel", m.getDroneModel());
            map.put("operator", m.getOperator());
            map.put("flightTime", m.getFlightTime());
            map.put("duration", m.getDuration());
            map.put("coverage", m.getCoverage());
            map.put("altitude", m.getAltitude());
            map.put("photoCount", m.getPhotoCount());
            map.put("lidarPoints", m.getLidarPoints());
            map.put("soilSamples", m.getSoilSamples());
            map.put("gpsAccuracyH", m.getGpsAccuracyH());
            map.put("gpsAccuracyV", m.getGpsAccuracyV());
            map.put("status", m.getStatus());
            map.put("centerLat", m.getCenterLat());
            map.put("centerLng", m.getCenterLng());
            map.put("weather", m.getWeather());
            return map;
        }).collect(Collectors.toList());
        
        return success(Map.of("list", list, "total", list.size()));
    }

    /**
     * 获取已拍照列表
     */
    @GetMapping("/photos/{deviceId}")
    public ResponseEntity<Map<String, Object>> getPhotos(@PathVariable String deviceId) {
        try {
            List<UnifiedDroneSDKService.CapturedPhoto> photos = 
                droneSDKService.getCapturedPhotos(deviceId);
            
            List<Map<String, Object>> list = photos.stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("photoId", p.getPhotoId());
                map.put("deviceId", p.getDeviceId());
                map.put("captureTime", p.getCaptureTime());
                map.put("latitude", p.getLatitude());
                map.put("longitude", p.getLongitude());
                map.put("altitude", p.getAltitude());
                map.put("heading", p.getHeading());
                map.put("filePath", p.getFilePath());
                map.put("fileSize", p.getFileSize());
                map.put("format", p.getFormat());
                return map;
            }).collect(Collectors.toList());
            
            return success(Map.of("list", list, "total", list.size()));
        } catch (Exception e) {
            log.error("获取照片列表失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取LiDAR数据
     */
    @GetMapping("/lidar/{deviceId}")
    public ResponseEntity<Map<String, Object>> getLidarData(@PathVariable String deviceId) {
        try {
            UnifiedDroneSDKService.LidarData lidar = droneSDKService.getLidarData(deviceId);
            
            if (lidar == null) {
                return success(Map.of("data", null, "message", "无LiDAR数据"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("deviceId", lidar.getDeviceId());
            data.put("startTime", lidar.getStartTime());
            data.put("endTime", lidar.getEndTime());
            data.put("pointCount", lidar.getPointCount());
            data.put("coverage", lidar.getCoverage());
            data.put("avgDensity", lidar.getAvgDensity());
            data.put("filePath", lidar.getFilePath());
            data.put("status", lidar.getStatus());
            
            return success(Map.of("data", data));
        } catch (Exception e) {
            log.error("获取LiDAR数据失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

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
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", message
        ));
    }
}
