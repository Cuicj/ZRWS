package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.GeoStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GeoStandardMapper extends BaseMapper<GeoStandard> {

    @Select("SELECT * FROM zrws_geo_standard WHERE category = #{category} AND status = 'ACTIVE' ORDER BY sort_order")
    List<GeoStandard> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM zrws_geo_standard WHERE classification_system = #{system} AND status = 'ACTIVE' ORDER BY sort_order")
    List<GeoStandard> selectBySystem(@Param("system") String system);

    @Select("SELECT * FROM zrws_geo_standard WHERE standard_name LIKE CONCAT('%', #{keyword}, '%') AND status = 'ACTIVE' LIMIT 20")
    List<GeoStandard> searchByName(@Param("keyword") String keyword);
}
