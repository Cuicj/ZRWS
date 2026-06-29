package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报表模板 Mapper接口
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {

    /**
     * 根据模板编码查询
     */
    ReportTemplate selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据分类查询模板列表
     */
    List<ReportTemplate> selectByCategory(@Param("category") String category);
}
