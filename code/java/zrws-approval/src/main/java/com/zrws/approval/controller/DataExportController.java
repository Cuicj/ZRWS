package com.zrws.approval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.ExportTask;
import com.zrws.approval.mapper.ExportTaskMapper;
import com.zrws.approval.service.DataExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/export")
@CrossOrigin(origins = "*")
public class DataExportController {

    @Autowired
    private DataExportService dataExportService;

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createExportTask(@RequestBody Map<String, Object> request) {
        String boCode = (String) request.get("boCode");
        String fileFormat = (String) request.get("fileFormat");

        if (boCode == null || boCode.isEmpty()) {
            return error("请提供BO编码");
        }
        if (fileFormat == null || fileFormat.isEmpty()) {
            return error("请提供文件格式");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> filters = (Map<String, Object>) request.get("filters");
            @SuppressWarnings("unchecked")
            List<String> fields = (List<String>) request.get("fields");

            String filterConditions = filters != null ? objectMapper.writeValueAsString(filters) : null;
            String fieldList = fields != null ? objectMapper.writeValueAsString(fields) : null;

            ExportTask task = dataExportService.createExportTask(boCode, fileFormat, filterConditions, fieldList);

            return success(Collections.singletonMap("task", task));
        } catch (Exception e) {
            log.error("创建导出任务失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/execute/{taskId}")
    public ResponseEntity<Map<String, Object>> executeExportTask(@PathVariable Long taskId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) {
            return error("任务不存在: " + taskId);
        }

        try {
            task.setStatus(ExportTask.TaskStatus.PROCESSING.name());
            task.setStartTime(LocalDateTime.now());
            exportTaskMapper.updateById(task);

            @SuppressWarnings("unchecked")
            List<String> fieldList = task.getFieldList() != null
                    ? objectMapper.readValue(task.getFieldList(), List.class)
                    : null;

            List<Map<String, Object>> data = dataExportService.queryExportData(
                    task.getBoCode(), task.getFilterConditions(), fieldList);

            String filePath = dataExportService.exportToExcel(task.getBoCode(), data);

            File file = new File(filePath);
            task.setFilePath(filePath);
            task.setFileName(file.getName());
            task.setFileSize(file.length());
            task.setTotalRows(data.size());
            task.setStatus(ExportTask.TaskStatus.SUCCESS.name());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(null);
            exportTaskMapper.updateById(task);

            return success(Collections.singletonMap("task", task));
        } catch (Exception e) {
            log.error("执行导出任务失败, taskId={}", taskId, e);
            task.setStatus(ExportTask.TaskStatus.FAILED.name());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            exportTaskMapper.updateById(task);
            return error("导出失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadExportFile(@PathVariable Long taskId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        if (!ExportTask.TaskStatus.SUCCESS.name().equals(task.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(task.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String fileName = URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败, taskId={}", taskId, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> listExportTasks(
            @RequestParam(required = false) String boCode) {

        List<ExportTask> tasks;

        if (boCode != null && !boCode.isEmpty()) {
            tasks = exportTaskMapper.selectByBoCode(boCode);
        } else {
            tasks = exportTaskMapper.selectList(null);
        }

        tasks.sort((t1, t2) -> {
            if (t1.getCreatedTime() == null && t2.getCreatedTime() == null) return 0;
            if (t1.getCreatedTime() == null) return 1;
            if (t2.getCreatedTime() == null) return -1;
            return t2.getCreatedTime().compareTo(t1.getCreatedTime());
        });

        if (tasks.size() > 50) {
            tasks = tasks.subList(0, 50);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tasks", tasks);
        return success(result);
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            result.putAll(dataMap);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return ResponseEntity.badRequest().body(result);
    }
}
