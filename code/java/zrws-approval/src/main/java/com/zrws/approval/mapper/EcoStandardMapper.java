package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.EcoStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EcoStandardMapper extends BaseMapper<EcoStandard> {

    @Select("SELECT * FROM zrws_eco_standard WHERE category = #{category} AND status = 'ACTIVE' AND is_deleted = 0 ORDER BY sort_order")
    List<EcoStandard> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM zrws_eco_standard WHERE category = #{category} AND grade_level = #{gradeLevel} AND status = 'ACTIVE' AND is_deleted = 0 ORDER BY sort_order")
    List<EcoStandard> selectByCategoryAndGrade(@Param("category") String category, @Param("gradeLevel") String gradeLevel);

    @Select("SELECT * FROM zrws_eco_standard WHERE standard_system = #{system} AND status = 'ACTIVE' AND is_deleted = 0 ORDER BY sort_order")
    List<EcoStandard> selectBySystem(@Param("system") String system);

    @Select("SELECT * FROM zrws_eco_standard WHERE standard_name LIKE CONCAT('%', #{keyword}, '%') AND status = 'ACTIVE' AND is_deleted = 0 LIMIT 20")
    List<EcoStandard> searchByName(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM zrws_eco_standard WHERE standard_code = #{code} AND is_deleted = 0")
    int countByCode(@Param("code") String code);
}
