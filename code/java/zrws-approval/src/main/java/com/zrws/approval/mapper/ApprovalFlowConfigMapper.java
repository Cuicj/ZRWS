package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ApprovalFlowConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批流配置 Mapper接口
 */
@Mapper
public interface ApprovalFlowConfigMapper extends BaseMapper<ApprovalFlowConfig> {

    /**
     * 根据BO编码查询配置列表
     */
    List<ApprovalFlowConfig> selectByBoCode(@Param("boCode") String boCode);

    /**
     * 查询启用的配置
     */
    List<ApprovalFlowConfig> selectActiveConfigs();

    /**
     * 根据BO编码和操作类型查询配置
     */
    ApprovalFlowConfig selectByBoAndOperation(@Param("boCode") String boCode, @Param("operationType") String operationType);
}