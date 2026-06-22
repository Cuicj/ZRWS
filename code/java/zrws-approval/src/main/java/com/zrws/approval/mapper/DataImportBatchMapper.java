package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.DataImportBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入批次 Mapper接口
 */
@Mapper
public interface DataImportBatchMapper extends BaseMapper<DataImportBatch> {

    /**
     * 根据批次号查询
     */
    DataImportBatch selectByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 根据BO编码查询批次列表
     */
    List<DataImportBatch> selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询处理中的批次
     */
    List<DataImportBatch> selectProcessing();

    /**
     * 查询操作人的导入记录
     */
    List<DataImportBatch> selectByOperator(@Param("operatorId") Long operatorId);

    /**
     * 统计某时间段的导入记录
     */
    List<DataImportBatch> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}