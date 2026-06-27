package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.RockSample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RockSampleMapper extends BaseMapper<RockSample> {

    @Select("SELECT * FROM zrws_rock_sample WHERE analysis_id = #{analysisId} AND is_deleted = 0 ORDER BY depth")
    List<RockSample> selectByAnalysisId(@Param("analysisId") Long analysisId);

    @Select("SELECT * FROM zrws_rock_sample WHERE mission_id = #{missionId} AND is_deleted = 0 ORDER BY created_time DESC")
    List<RockSample> selectByMissionId(@Param("missionId") Long missionId);
}
