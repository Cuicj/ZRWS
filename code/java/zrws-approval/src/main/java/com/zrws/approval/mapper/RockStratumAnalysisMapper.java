package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RockStratumAnalysisMapper extends BaseMapper<RockStratumAnalysis> {

    @Select("SELECT * FROM zrws_rock_stratum_analysis WHERE is_deleted = 0 ORDER BY created_time DESC")
    List<RockStratumAnalysis> selectAll();

    @Select("SELECT * FROM zrws_rock_stratum_analysis WHERE mission_id = #{missionId} AND is_deleted = 0 ORDER BY created_time DESC")
    List<RockStratumAnalysis> selectByMissionId(@Param("missionId") Long missionId);
}
