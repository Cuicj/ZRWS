package com.zrws.approval.service.impl;

import com.zrws.approval.config.VideoGenProperties;
import com.zrws.approval.domain.entity.VideoTask;
import com.zrws.approval.service.VideoGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

/**
 * Mock 视频生成提供商
 * 用于开发测试，模拟视频生成流程，不实际调用API
 */
@Slf4j
@Component
public class MockVideoProvider implements VideoGenerationService.IVideoProvider {

    private static final String PROVIDER_NAME = "MOCK";

    @Autowired
    private VideoGenerationService videoGenerationService;

    @Autowired
    private VideoGenProperties videoGenProperties;

    @PostConstruct
    public void init() {
        Map<String, VideoGenProperties.ProviderConfig> providers = videoGenProperties.getProviders();
        if (providers != null && providers.containsKey(PROVIDER_NAME)) {
            videoGenerationService.registerProvider(PROVIDER_NAME, this);
            log.info("Mock视频生成提供商初始化完成（仅用于测试）");
        }
    }

    @Override
    public String submitGenerationTask(VideoTask task) {
        String mockTaskId = "mock-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Mock视频任务提交成功: taskId={}, prompt={}", mockTaskId, task.getPrompt());
        return mockTaskId;
    }

    @Override
    public VideoGenerationService.ProviderTaskStatus queryTaskStatus(String providerTaskId, VideoTask localTask) {
        VideoGenerationService.ProviderTaskStatus status = new VideoGenerationService.ProviderTaskStatus();

        int currentProgress = localTask.getProgress() != null ? localTask.getProgress() : 0;

        if (currentProgress < 30) {
            status.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.PENDING);
            status.setProgress(20);
        } else if (currentProgress < 90) {
            status.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.PROCESSING);
            status.setProgress(Math.min(95, currentProgress + 15));
        } else {
            status.setStatus(VideoGenerationService.ProviderTaskStatus.TaskStatusEnum.SUCCESS);
            status.setProgress(100);
            status.setVideoUrl("https://www.w3schools.com/html/mov_bbb.mp4");
            status.setCoverUrl("");
        }

        log.debug("Mock任务状态查询: taskId={}, status={}, progress={}",
                providerTaskId, status.getStatus(), status.getProgress());

        return status;
    }

    @Override
    public void cancelTask(String providerTaskId) {
        log.info("Mock任务已取消: taskId={}", providerTaskId);
    }
}
