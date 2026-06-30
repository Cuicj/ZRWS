package com.zrws.approval.controller;

import com.zrws.approval.service.ExternalDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 外部数据接入 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/external-data")
@CrossOrigin(origins = "*")
public class ExternalDataController {

    @Autowired
    private ExternalDataService externalDataService;

    /**
     * 获取可用数据源列表
     */
    @GetMapping("/sources")
    public ResponseEntity<Map<String, Object>> listSources() {
        try {
            List<Map<String, Object>> sources = new ArrayList<>();

            Map<String, Object> source1 = new LinkedHashMap<>();
            source1.put("code", "soil_lab_api");
            source1.put("name", "土壤检测实验室API");
            source1.put("description", "第三方土壤检测机构数据接口");
            source1.put("status", "available");
            source1.put("lastSync", null);
            sources.add(source1);

            Map<String, Object> source2 = new LinkedHashMap<>();
            source2.put("code", "agri_data_platform");
            source2.put("name", "农业数据平台");
            source2.put("description", "农业农村部土壤监测数据平台");
            source2.put("status", "available");
            source2.put("lastSync", null);
            sources.add(source2);

            Map<String, Object> source3 = new LinkedHashMap<>();
            source3.put("code", "geo_monitor_system");
            source3.put("name", "地质监测系统");
            source3.put("description", "地质环境监测站数据系统");
            source3.put("status", "available");
            source3.put("lastSync", null);
            sources.add(source3);

            Map<String, Object> result = new HashMap<>();
            result.put("list", sources);
            result.put("total", sources.size());
            return success(result);
        } catch (Exception e) {
            log.error("获取数据源列表失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/sources/{sourceCode}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String sourceCode) {
        try {
            boolean connected = externalDataService.testConnection(sourceCode);
            Map<String, Object> result = new HashMap<>();
            result.put("sourceCode", sourceCode);
            result.put("connected", connected);
            result.put("message", connected ? "连接成功" : "连接失败");
            return success(result);
        } catch (Exception e) {
            log.error("测试数据源连接失败: {}", sourceCode, e);
            return error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 拉取外部数据
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchData(@RequestBody FetchRequest request) {
        try {
            ExternalDataService.ExternalDataResult result = externalDataService.fetchSoilData(
                    request.getSource(),
                    request.getParams() != null ? request.getParams() : new HashMap<>()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("source", result.getSource());
            response.put("message", result.getMessage());
            response.put("fetchTime", result.getFetchTime());
            response.put("totalCount", result.getTotalCount());
            response.put("rawData", result.getRawData());
            response.put("mappedData", result.getMappedData());

            return success(response);
        } catch (Exception e) {
            log.error("拉取外部数据失败", e);
            return error("拉取失败: " + e.getMessage());
        }
    }

    /**
     * 获取字段映射关系
     */
    @GetMapping("/field-mapping")
    public ResponseEntity<Map<String, Object>> getFieldMapping() {
        try {
            Map<String, String> mapping = externalDataService.getFieldMapping();
            Map<String, Object> result = new HashMap<>();
            result.put("mapping", mapping);
            result.put("total", mapping.size());
            return success(result);
        } catch (Exception e) {
            log.error("获取字段映射失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 字段映射测试
     */
    @PostMapping("/field-mapping/test")
    public ResponseEntity<Map<String, Object>> testFieldMapping(@RequestBody Map<String, Object> testData) {
        try {
            Map<String, Object> mappedData = externalDataService.autoFieldMapping(testData);
            Map<String, Object> convertedData = externalDataService.convertDataFormat(mappedData);

            Map<String, Object> result = new HashMap<>();
            result.put("original", testData);
            result.put("mapped", mappedData);
            result.put("converted", convertedData);
            return success(result);
        } catch (Exception e) {
            log.error("字段映射测试失败", e);
            return error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 数据格式转换测试
     */
    @PostMapping("/convert/test")
    public ResponseEntity<Map<String, Object>> testConvert(@RequestBody Map<String, Object> testData) {
        try {
            Map<String, Object> convertedData = externalDataService.convertDataFormat(testData);
            Map<String, Object> result = new HashMap<>();
            result.put("original", testData);
            result.put("converted", convertedData);
            return success(result);
        } catch (Exception e) {
            log.error("数据格式转换测试失败", e);
            return error("测试失败: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", message);
        result.put("data", null);
        return ResponseEntity.ok(result);
    }

    @Data
    public static class FetchRequest {
        private String source;
        private Map<String, Object> params;
    }
}
