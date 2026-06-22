package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.DataImportDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据导入明细 Mapper接口
 */
@Mapper
public interface DataImportDetailMapper extends BaseMapper<DataImportDetail> {

    /**
     * 根据批次ID查询明细列表
     */
    List<DataImportDetail> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据批次ID查询失败记录
     */
    List<DataImportDetail> selectFailedByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据批次ID统计各状态数量
     */
    Long countByStatus(@Param("batchId") Long batchId, @Param("status") String status);
}