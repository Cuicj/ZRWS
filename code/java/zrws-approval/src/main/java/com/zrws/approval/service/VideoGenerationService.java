package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.config.VideoGenProperties;
import com.zrws.approval.domain.entity.VideoTask;
import com.zrws.approval.dto.VideoGenerateRequest;
import com.zrws.approval.mapper.VideoTaskMapper;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI视频生成服务
 * 封装多家第三方视频生成API，提供统一调用接口
 */
@Slf4j
@Service
public class VideoGenerationService {

    @Autowired
    private VideoTaskMapper videoTaskMapper;

    @Autowired
    private VideoGenProperties videoGenProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, IVideoProvider> providerMap = new ConcurrentHashMap<>();

    /**
     * 注册视频提供商
     */
    public void registerProvider(String providerName, IVideoProvider provider) {
        providerMap.put(providerName.toUpperCase(), provider);
        log.info("视频生成提供商已注册: {}", providerName);
    }

    /**
     * 获取提供商实例
     */
    public IVideoProvider getProvider(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            providerName = videoGenProperties.getDefaultProvider();
        }
        IVideoProvider provider = providerMap.get(providerName.toUpperCase());
        if (provider == null) {
            throw new RuntimeException("不支持的视频生成提供商: " + providerName);
        }
        return provider;
    }

    /**
     * 提交视频生成任务
     */
    public VideoTask submitTask(VideoGenerateRequest request, Long operatorId, String operatorName) {
        if (!videoGenProperties.isEnabled()) {
            throw new RuntimeException("视频生成功能未启用");
        }

        String providerName = request.getProvider() != null ? request.getProvider() : videoGenProperties.getDefaultProvider();
        IVideoProvider provider = getProvider(providerName);

        VideoTask task = new VideoTask();
        task.setTaskNo(generateTaskNo());
        task.setTaskName(request.getTaskName() != null ? request.getTaskName() : "视频生成-" + DateUtil.format(new Date(), "yyyyMMddHHmmss"));
        task.setProvider(providerName.toUpperCase());
        task.setGenerationMode(request.getGenerationMode() != null ? request.getGenerationMode() : VideoTask.GenerationMode.TEXT_TO_VIDEO.name());
        task.setPrompt(request.getPrompt());
        task.setPromptEn(request.getPromptEn());
        task.setNegativePrompt(request.getNegativePrompt());
        task.setReferenceImageUrl(request.getReferenceImageUrl());
        task.setDuration(request.getDuration() != null ? request.getDuration() : 5);
        task.setWidth(request.getWidth() != null ? request.getWidth() : 720);
        task.setHeight(request.getHeight() != null ? request.getHeight() : 1280);
        task.setFps(request.getFps() != null ? request.getFps() : 24);
        task.setModelName(request.getModelName());
        task.setStylePreset(request.getStylePreset());
        task.setCallbackUrl(request.getCallbackUrl());
        task.setStatus(VideoTask.TaskStatus.PENDING.name());
        task.setProgress(0);
        task.setOperatorId(operatorId);
        task.setOperatorName(operatorName);
        task.setRetryCount(0);

        if (request.getExtraParams() != null) {
            try {
                task.setExtraParams(objectMapper.writeValueAsString(request.getExtraParams()));
            } catch (Exception e) {
                log.warn("序列化扩展参数失败", e);
            }
        }

        videoTaskMapper.insert(task);
        log.info("视频生成任务已创建: taskNo={}, provider={}", task.getTaskNo(), providerName);

        processTaskAsync(task.getTaskId());

        return task;
    }

    /**
     * 异步处理视频生成任务
     */
    @Async("videoGenTaskExecutor")
    public void processTaskAsync(Long taskId) {
        try {
            VideoTask task = videoTaskMapper.selectById(taskId);
            if (task == null) {
                log.error("视频任务不存在: taskId={}", taskId);
                return;
            }

            IVideoProvider provider = getProvider(task.getProvider());

            task.setStatus(VideoTask.TaskStatus.PROCESSING.name());
            task.setStartTime(LocalDateTime.now());
            task.setProgress(5);
            videoTaskMapper.updateById(task);

            String providerTaskId = provider.submitGenerationTask(task);
            if (providerTaskId == null || providerTaskId.isBlank()) {
                throw new RuntimeException("提供商返回空任务ID");
            }

            task.setProviderTaskId(providerTaskId);
            task.setProgress(10);
            videoTaskMapper.updateById(task);

            log.info("视频任务已提交到提供商: taskNo={}, providerTaskId={}", task.getTaskNo(), providerTaskId);

            pollTaskStatus(taskId, providerTaskId, provider);

        } catch (Exception e) {
            log.error("视频生成任务处理失败, taskId={}", taskId, e);
            handleTaskFailure(taskId, e.getMessage());
        }
    }

    /**
     * 轮询任务状态
     */
    private void pollTaskStatus(Long taskId, String providerTaskId, IVideoProvider provider) {
        int pollInterval = videoGenProperties.getPollIntervalSeconds() * 1000;
        int timeoutMs = videoGenProperties.getTaskTimeoutMinutes() * 60 * 1000;
        long startTime = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                handleTaskFailure(taskId, "任务超时（" + videoGenProperties.getTaskTimeoutMinutes() + "分钟）");
                return;
            }

            try {
                Thread.sleep(pollInterval);

                VideoTask task = videoTaskMapper.selectById(taskId);
                if (task == null || VideoTask.TaskStatus.CANCELLED.name().equals(task.getStatus())) {
                    log.info("视频任务已取消或不存在: taskId={}", taskId);
                    return;
                }

                ProviderTaskStatus status = provider.queryTaskStatus(providerTaskId, task);

                if (status == null) {
                    continue;
                }

                if (status.getProgress() != null && status.getProgress() > task.getProgress()) {
                    task.setProgress(status.getProgress());
                    videoTaskMapper.updateById(task);
                }

                switch (status.getStatus()) {
                    case SUCCESS:
                        handleTaskSuccess(task, status, provider);
                        return;
                    case FAILED:
                        handleTaskFailure(taskId, status.getErrorMessage() != null ? status.getErrorMessage() : "生成失败");
                        return;
                    case PROCESSING:
                    case PENDING:
                    default:
                        break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleTaskFailure(taskId, "任务被中断");
                return;
            } catch (Exception e) {
                log.warn("查询视频任务状态异常, taskId={}", taskId, e);
            }
        }
    }

    /**
     * 处理任务成功
     */
    private void handleTaskSuccess(VideoTask task, ProviderTaskStatus status, IVideoProvider provider) {
        try {
            String videoUrl = status.getVideoUrl();
            if (videoUrl == null || videoUrl.isBlank()) {
                throw new RuntimeException("提供商返回的视频URL为空");
            }

            task.setVideoUrl(videoUrl);
            task.setCoverUrl(status.getCoverUrl());
            task.setProgress(100);

            String localPath = downloadVideo(videoUrl, task.getTaskNo());
            if (localPath != null) {
                File videoFile = new File(localPath);
                task.setVideoPath(localPath);
                task.setFileName(videoFile.getName());
                task.setFileSize(videoFile.length());
            }

            task.setStatus(VideoTask.TaskStatus.SUCCESS.name());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(null);
            videoTaskMapper.updateById(task);

            log.info("视频生成任务成功: taskNo={}, videoUrl={}", task.getTaskNo(), videoUrl);

            if (task.getCallbackUrl() != null && !task.getCallbackUrl().isBlank()) {
                sendCallback(task);
            }

        } catch (Exception e) {
            log.error("处理视频任务成功结果失败, taskId={}", task.getTaskId(), e);
            handleTaskFailure(task.getTaskId(), "下载视频失败: " + e.getMessage());
        }
    }

    /**
     * 处理任务失败
     */
    private void handleTaskFailure(Long taskId, String errorMessage) {
        try {
            VideoTask task = videoTaskMapper.selectById(taskId);
            if (task == null) return;

            int maxRetry = videoGenProperties.getMaxRetryCount();
            int currentRetry = task.getRetryCount() != null ? task.getRetryCount() : 0;

            if (currentRetry < maxRetry) {
                task.setRetryCount(currentRetry + 1);
                task.setStatus(VideoTask.TaskStatus.PENDING.name());
                task.setErrorMessage("重试中(" + (currentRetry + 1) + "/" + maxRetry + "): " + errorMessage);
                videoTaskMapper.updateById(task);

                log.info("视频任务准备重试: taskNo={}, retry={}/{}", task.getTaskNo(), currentRetry + 1, maxRetry);
                processTaskAsync(taskId);
                return;
            }

            task.setStatus(VideoTask.TaskStatus.FAILED.name());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(errorMessage);
            videoTaskMapper.updateById(task);

            log.warn("视频生成任务失败: taskNo={}, error={}", task.getTaskNo(), errorMessage);

            if (task.getCallbackUrl() != null && !task.getCallbackUrl().isBlank()) {
                sendCallback(task);
            }

        } catch (Exception e) {
            log.error("处理任务失败状态异常, taskId={}", taskId, e);
        }
    }

    /**
     * 下载视频到本地
     */
    private String downloadVideo(String videoUrl, String taskNo) {
        try {
            String storagePath = videoGenProperties.getStoragePath();
            File dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = taskNo + ".mp4";
            String filePath = storagePath + File.separator + fileName;

            URL url = new URL(videoUrl);
            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in);
                 FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }

            log.info("视频下载完成: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.warn("下载视频失败: url={}", videoUrl, e);
            return null;
        }
    }

    /**
     * 发送回调通知
     */
    private void sendCallback(VideoTask task) {
        try {
            restTemplate.postForObject(task.getCallbackUrl(), task, String.class);
            log.info("任务回调已发送: taskNo={}, callbackUrl={}", task.getTaskNo(), task.getCallbackUrl());
        } catch (Exception e) {
            log.warn("发送回调失败: taskNo={}", task.getTaskNo(), e);
        }
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        if (VideoTask.TaskStatus.SUCCESS.name().equals(task.getStatus()) ||
            VideoTask.TaskStatus.FAILED.name().equals(task.getStatus()) ||
            VideoTask.TaskStatus.CANCELLED.name().equals(task.getStatus())) {
            return false;
        }

        task.setStatus(VideoTask.TaskStatus.CANCELLED.name());
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage("用户取消");
        videoTaskMapper.updateById(task);

        if (task.getProviderTaskId() != null) {
            try {
                IVideoProvider provider = getProvider(task.getProvider());
                provider.cancelTask(task.getProviderTaskId());
            } catch (Exception e) {
                log.warn("取消提供商任务失败: providerTaskId={}", task.getProviderTaskId(), e);
            }
        }

        return true;
    }

    /**
     * 获取任务详情
     */
    public VideoTask getTask(Long taskId) {
        return videoTaskMapper.selectById(taskId);
    }

    public VideoTask getTaskByNo(String taskNo) {
        return videoTaskMapper.selectByTaskNo(taskNo);
    }

    /**
     * 获取用户的视频任务列表
     */
    public List<VideoTask> listUserTasks(Long operatorId, int limit) {
        List<VideoTask> tasks = videoTaskMapper.selectByOperator(operatorId);
        tasks.sort((t1, t2) -> {
            if (t1.getCreatedTime() == null && t2.getCreatedTime() == null) return 0;
            if (t1.getCreatedTime() == null) return 1;
            if (t2.getCreatedTime() == null) return -1;
            return t2.getCreatedTime().compareTo(t1.getCreatedTime());
        });
        if (tasks.size() > limit) {
            tasks = tasks.subList(0, limit);
        }
        return tasks;
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "VID" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 视频提供商接口
     */
    public interface IVideoProvider {

        String submitGenerationTask(VideoTask task);

        ProviderTaskStatus queryTaskStatus(String providerTaskId, VideoTask localTask);

        default void cancelTask(String providerTaskId) {
        }
    }

    /**
     * 提供商任务状态
     */
    @Data
    public static class ProviderTaskStatus {
        private TaskStatusEnum status;
        private Integer progress;
        private String videoUrl;
        private String coverUrl;
        private String errorMessage;

        public enum TaskStatusEnum {
            PENDING,
            PROCESSING,
            SUCCESS,
            FAILED
        }
    }
}
