package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.VideoTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI视频生成任务 Mapper接口
 */
@Mapper
public interface VideoTaskMapper extends BaseMapper<VideoTask> {

    VideoTask selectByTaskNo(@Param("taskNo") String taskNo);

    VideoTask selectByProviderTaskId(@Param("providerTaskId") String providerTaskId);

    List<VideoTask> selectByOperator(@Param("operatorId") Long operatorId);

    List<VideoTask> selectByStatus(@Param("status") String status);

    List<VideoTask> selectProcessingTasks();

    List<VideoTask> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    List<VideoTask> selectByProvider(@Param("provider") String provider);
}
