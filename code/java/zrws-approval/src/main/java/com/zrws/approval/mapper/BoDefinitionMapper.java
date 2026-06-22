package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.BoDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * BO定义 Mapper接口
 */
@Mapper
public interface BoDefinitionMapper extends BaseMapper<BoDefinition> {

    /**
     * 根据BO编码查询
     */
    BoDefinition selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询启用的BO定义列表
     */
    List<BoDefinition> selectActiveList();

    /**
     * 根据BO类型查询
     */
    List<BoDefinition> selectByBoType(@Param("boType") String boType);
}