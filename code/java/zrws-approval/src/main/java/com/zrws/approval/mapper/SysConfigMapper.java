package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 全局配置 Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    /**
     * 根据配置组查询
     */
    List<SysConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 根据配置键查询
     */
    SysConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 获取所有启用的配置
     */
    List<SysConfig> selectActiveConfigs();

    /**
     * 更新配置值
     */
    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}