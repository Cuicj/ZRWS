package com.zrws.approval.controller;

import com.zrws.approval.config.VideoGenProperties;
import com.zrws.approval.domain.entity.VideoTask;
import com.zrws.approval.dto.VideoGenerateRequest;
import com.zrws.approval.service.VideoGenerationService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/video-gen")
@CrossOrigin(origins = "*")
public class VideoGenerationController {

    @Autowired
    private VideoGenerationService videoGenerationService;

    @Autowired
    private VideoGenProperties videoGenProperties;

    /**
     * 获取视频生成配置和支持的提供商
     */
    @GetMapping("/config")
    public R<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", videoGenProperties.isEnabled());
        config.put("defaultProvider", videoGenProperties.getDefaultProvider());
        config.put("maxConcurrentTasks", videoGenProperties.getMaxConcurrentTasks());
        config.put("providers", videoGenProperties.getProviders().keySet());
        return R.ok(config);
    }

    /**
     * 提交视频生成任务
     */
    @PostMapping("/generate")
    public R<VideoTask> generateVideo(@RequestBody VideoGenerateRequest request) {
        if (!videoGenProperties.isEnabled()) {
            return R.fail("视频生成功能未启用");
        }

        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            return R.fail("请输入提示词");
        }

        try {
            Long operatorId = getCurrentUserId();
            String operatorName = getCurrentUsername();

            VideoTask task = videoGenerationService.submitTask(request, operatorId, operatorName);
            return R.ok(task);
        } catch (Exception e) {
            log.error("提交视频生成任务失败", e);
            return R.fail("提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    public R<VideoTask> getTask(@PathVariable Long taskId) {
        try {
            VideoTask task = videoGenerationService.getTask(taskId);
            if (task == null) {
                return R.fail("任务不存在");
            }
            return R.ok(task);
        } catch (Exception e) {
            log.error("获取任务详情失败, taskId={}", taskId, e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务编号获取任务详情
     */
    @GetMapping("/task/no/{taskNo}")
    public R<VideoTask> getTaskByNo(@PathVariable String taskNo) {
        try {
            VideoTask task = videoGenerationService.getTaskByNo(taskNo);
            if (task == null) {
                return R.fail("任务不存在");
            }
            return R.ok(task);
        } catch (Exception e) {
            log.error("获取任务详情失败, taskNo={}", taskNo, e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的视频任务列表
     */
    @GetMapping("/tasks")
    public R<List<VideoTask>> listTasks(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            Long operatorId = getCurrentUserId();
            List<VideoTask> tasks = videoGenerationService.listUserTasks(operatorId, limit);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/task/{taskId}/cancel")
    public R<String> cancelTask(@PathVariable Long taskId) {
        try {
            boolean success = videoGenerationService.cancelTask(taskId);
            if (success) {
                return R.ok("取消成功");
            }
            return R.fail("取消失败：任务不存在或已完成");
        } catch (Exception e) {
            log.error("取消任务失败, taskId={}", taskId, e);
            return R.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 下载生成的视频文件
     */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable Long taskId) {
        VideoTask task = videoGenerationService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        if (!VideoTask.TaskStatus.SUCCESS.name().equals(task.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        String filePath = task.getVideoPath();
        if (filePath == null || filePath.isBlank()) {
            if (task.getVideoUrl() != null && !task.getVideoUrl().isBlank()) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, task.getVideoUrl())
                        .build();
            }
            return ResponseEntity.notFound().build();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            if (task.getVideoUrl() != null && !task.getVideoUrl().isBlank()) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, task.getVideoUrl())
                        .build();
            }
            return ResponseEntity.notFound().build();
        }

        try {
            String fileName = URLEncoder.encode(
                    task.getFileName() != null ? task.getFileName() : "video.mp4",
                    StandardCharsets.UTF_8.name()
            ).replaceAll("\\+", "%20");

            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            log.error("下载视频失败, taskId={}", taskId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 流式播放视频
     */
    @GetMapping("/play/{taskId}")
    public ResponseEntity<Resource> playVideo(@PathVariable Long taskId) {
        VideoTask task = videoGenerationService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        if (!VideoTask.TaskStatus.SUCCESS.name().equals(task.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        if (task.getVideoUrl() != null && !task.getVideoUrl().isBlank()) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, task.getVideoUrl())
                    .build();
        }

        String filePath = task.getVideoPath();
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return 1L;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "system";
    }
}
