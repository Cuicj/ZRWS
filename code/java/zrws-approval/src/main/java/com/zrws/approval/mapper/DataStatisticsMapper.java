package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.DataStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据统计 Mapper接口
 */
@Mapper
public interface DataStatisticsMapper extends BaseMapper<DataStatistics> {

    /**
     * 根据日期查询统计
     */
    DataStatistics selectByDate(@Param("statsDate") String statsDate, @Param("boCode") String boCode);

    /**
     * 查询日期范围统计
     */
    List<DataStatistics> selectByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 根据BO编码查询统计
     */
    List<DataStatistics> selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询周期统计
     */
    List<DataStatistics> selectByPeriodType(@Param("periodType") String periodType);

    /**
     * 汇总统计(按BO分组)
     */
    List<DataStatistics> selectSummaryByBo();

    /**
     * 汇总统计(按日期分组)
     */
    List<DataStatistics> selectSummaryByDate(@Param("periodType") String periodType);
}