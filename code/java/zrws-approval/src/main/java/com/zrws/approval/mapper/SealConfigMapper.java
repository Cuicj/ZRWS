package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.SealConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 电子签章配置 Mapper接口
 */
@Mapper
public interface SealConfigMapper extends BaseMapper<SealConfig> {

    /**
     * 查询所有启用的签章
     */
    @Select("SELECT * FROM zrws_seal_config WHERE status = 'ACTIVE' AND is_deleted = 0")
    List<SealConfig> selectActiveList();

    /**
     * 根据名称查询
     */
    @Select("SELECT * FROM zrws_seal_config WHERE seal_name = #{sealName} AND is_deleted = 0 LIMIT 1")
    SealConfig selectByName(@Param("sealName") String sealName);
}
