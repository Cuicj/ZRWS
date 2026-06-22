package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.EnumConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 枚举配置 Mapper接口
 */
@Mapper
public interface EnumConfigMapper extends BaseMapper<EnumConfig> {

    /**
     * 根据枚举编码查询所有启用的枚举项
     */
    List<EnumConfig> selectActiveByCode(@Param("enumCode") String enumCode);

    /**
     * 根据枚举编码查询所有枚举项
     */
    List<EnumConfig> selectByCode(@Param("enumCode") String enumCode);

    /**
     * 获取所有枚举编码
     */
    List<String> selectAllCodes();
}