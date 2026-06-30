package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.Desertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DesertificationMapper extends BaseMapper<Desertification> {

    @Select("SELECT desertification_grade as grade, COUNT(*) as count FROM zrws_desertification WHERE is_deleted = 0 GROUP BY desertification_grade")
    List<Map<String, Object>> countByGrade();

    @Select("SELECT risk_level as riskLevel, COUNT(*) as count FROM zrws_desertification WHERE is_deleted = 0 GROUP BY risk_level")
    List<Map<String, Object>> countByRiskLevel();

    @Select("SELECT * FROM zrws_desertification WHERE region = #{region} AND is_deleted = 0 ORDER BY monitor_date DESC LIMIT 12")
    List<Desertification> selectTrendByRegion(@Param("region") String region);
}
