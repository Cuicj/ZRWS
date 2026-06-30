package com.zrws.approval.scheduler;

import com.zrws.approval.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheTask {

    @Autowired
    private RedisService redisService;

    private static final String ANNOUNCEMENT_CACHE_PREFIX = "announcement:";

    @Scheduled(cron = "0 0 3 * * ?")
    public void clearAnnouncementCache() {
        log.info("[定时任务] 开始清理公告缓存...");
        try {
            Long count = redisService.deleteByPrefix(ANNOUNCEMENT_CACHE_PREFIX);
            log.info("[定时任务] 公告缓存清理完成，共清理 {} 个key", count);
        } catch (Exception e) {
            log.error("[定时任务] 公告缓存清理失败: {}", e.getMessage(), e);
        }
    }
}
