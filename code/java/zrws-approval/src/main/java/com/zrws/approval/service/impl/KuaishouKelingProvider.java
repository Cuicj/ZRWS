package com.zrws.approval.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.config.VideoGenProperties;
import com.zrws.approval.domain.entity.VideoTask;
import com.zrws.approval.service.VideoGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 快手可灵 AI 视频生成提供商实现
 * 文档参考: https://klingai.com/docs
 */
@Slf4j
@Component
public class KuaishouKelingProvider implements VideoGenerationService.IVideoProvider {

    private static final String PROVIDER_NAME = "KUAISHOU_KELING";

    @Autowired
    private VideoGenerationService videoGenerationService;

    @Autowired
    private VideoGenProperties videoGenProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private VideoGenProperties.ProviderConfig config;

    @PostConstruct
    public void init() {
        Map<String, VideoGenProperties.ProviderConfig> providers = videoGenProperties.getProviders();
        if (providers != null && providers.containsKey(PROVIDER_NAME)) {
            this.config = providers.get(PROVIDER_NAME);
            videoGenerationService.registerProvider(PROVIDER_NAME, this);
            log.info("快手可灵视频生成提供商初始化完成");
        } else {
            log.warn("快手可灵视频生成配置未找到，跳过初始化");
        }
    }

    @Override
    public String submitGenerationTask(VideoTask task) {
        if (config == null || config.getApiKey() == null || config.getApiKey().startsWith("your-")) {
            throw new RuntimeException("快手可灵API配置无效，请在配置文件中设置正确的 api-key");
        }

        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.klingai.com";
            String url = baseUrl + "/v1/videos/text2video";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", task.getPrompt());

            if (task.getPromptEn() != null && !task.getPromptEn().isBlank()) {
                requestBody.put("prompt_en", task.getPromptEn());
            }
            if (task.getNegativePrompt() != null && !task.getNegativePrompt().isBlank()) {
                requestBody.put("negative_prompt", task.getNegativePrompt());
            }

            requestBody.put("duration", task.getDuration() != null ? task.getDuration() : 5);

            if (task.getModelName() != null && !task.getModelName().isBlank()) {
                requestBody.put("model", task.getModelName());
            } else if (config.getModelName() != null && !config.getModelName().isBlank()) {
                requestBody.put("model", config.getModelName());
            }

            Map<String, Object> size = new HashMap<>();
            size.put("width", task.getWidth() != null ? task.getWidth() : 720);
            size.put("height", task.getHeight() != null ? task.getHeight() : 1280);
            requestBody.put("size", size);

            if (task.getStylePreset() != null && !task.getStylePreset().isBlank()) {
                requestBody.put("style_preset", task.getStylePreset());
            }

            if (task.getExtraParams() != null && !task.getExtraParams().isBlank()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extra = objectMapper.readValue(task.getExtraParams(), Map.class);
                    requestBody.putAll(extra);
                } catch (Exception e) {
                    log.warn("解析扩展参数失败，忽略: {}", e.getMessage());
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.ACCEPTED) {
                throw new RuntimeException("API请求失败: HTTP " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            String taskId = data.path("task_id").asText();

            if (taskId == null || taskId.isBlank()) {
                throw new RuntimeException("API返回数据格式异常: " + response.getBody());
            }

            log.info("快手可灵任务提交成功: taskId={}", taskId);
            return taskId;

        } catch (Exception e) {
            log.error("提交快手可灵视频生成任务失败", e);
            throw new RuntimeException("提交视频生成任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public VideoGenerationService.ProviderTaskStatus queryTaskStatus(String providerTaskId, VideoTask localTask) {
        if (config == null) {
            return null;
        }

        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.klingai.com";
            String url = baseUrl + "/v1/videos/text2video/" + providerTaskId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("查询任务状态失败: HTTP {}", response.getStatusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            String status = data.path("status").asText();

            VideoGenerationService.ProviderTaskStatus result = new VideoGenerationService.ProviderTaskStatus();

            switch (status) {
                case "submitted":
                case "queued":
                    result.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.PENDING);
                    result.setProgress(5);
                    break;
                case "processing":
                case "running":
                    result.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.PROCESSING);
                    int progress = data.path("progress").asInt(20);
                    result.setProgress(Math.max(10, Math.min(95, progress)));
                    break;
                case "succeeded":
                case "success":
                    result.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.SUCCESS);
                    result.setProgress(100);
                    JsonNode videos = data.path("video_urls");
                    if (videos != null && videos.isArray() && videos.size() > 0) {
                        result.setVideoUrl(videos.get(0).asText());
                    } else {
                        result.setVideoUrl(data.path("video_url").asText());
                    }
                    result.setCoverUrl(data.path("cover_url").asText());
                    break;
                case "failed":
                case "error":
                    result.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.FAILED);
                    result.setErrorMessage(data.path("error_message").asText("生成失败"));
                    break;
                default:
                    result.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.PROCESSING);
                    result.setProgress(50);
                    break;
            }

            return result;

        } catch (Exception e) {
            log.warn("查询快手可灵任务状态异常: taskId={}", providerTaskId, e);
            return null;
        }
    }

    @Override
    public void cancelTask(String providerTaskId) {
        if (config == null) return;

        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.klingai.com";
            String url = baseUrl + "/v1/videos/text2video/" + providerTaskId + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("快手可灵任务已取消: taskId={}", providerTaskId);
        } catch (Exception e) {
            log.warn("取消快手可灵任务失败: taskId={}", providerTaskId, e);
        }
    }
}
