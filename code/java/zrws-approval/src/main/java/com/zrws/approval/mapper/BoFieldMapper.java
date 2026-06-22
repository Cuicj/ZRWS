package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.BoField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * BO字段配置 Mapper接口
 */
@Mapper
public interface BoFieldMapper extends BaseMapper<BoField> {

    /**
     * 根据BO ID查询字段列表
     */
    List<BoField> selectByBoId(@Param("boId") Long boId);

    /**
     * 根据BO编码查询字段列表
     */
    List<BoField> selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询必填字段
     */
    List<BoField> selectRequiredFields(@Param("boId") Long boId);

    /**
     * 查询唯一性字段
     */
    List<BoField> selectUniqueFields(@Param("boId") Long boId);
}