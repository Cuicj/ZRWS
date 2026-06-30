package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ClimateWarming;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ClimateWarmingMapper extends BaseMapper<ClimateWarming> {

    @Select("SELECT risk_level as riskLevel, COUNT(*) as count FROM zrws_climate_warming WHERE is_deleted = 0 GROUP BY risk_level")
    List<Map<String, Object>> countByRiskLevel();

    @Select("SELECT * FROM zrws_climate_warming WHERE region = #{region} AND is_deleted = 0 ORDER BY monitor_date DESC LIMIT 12")
    List<ClimateWarming> selectTrendByRegion(@Param("region") String region);
}
