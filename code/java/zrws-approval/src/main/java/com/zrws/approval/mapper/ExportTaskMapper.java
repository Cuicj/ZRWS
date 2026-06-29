package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ExportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导出任务 Mapper接口
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {

    /**
     * 根据任务编号查询
     */
    ExportTask selectByTaskNo(@Param("taskNo") String taskNo);

    /**
     * 根据BO编码查询任务列表
     */
    List<ExportTask> selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询处理中的任务
     */
    List<ExportTask> selectProcessing();

    /**
     * 查询操作人的导出记录
     */
    List<ExportTask> selectByOperator(@Param("operatorId") Long operatorId);

    /**
     * 统计某时间段的导出记录
     */
    List<ExportTask> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
}
