package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.AnnouncementCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公告分类 Mapper
 */
@Mapper
public interface AnnouncementCategoryMapper extends BaseMapper<AnnouncementCategory> {

    /**
     * 获取启用的分类
     */
    List<AnnouncementCategory> selectActive();

    /**
     * 按排序查询
     */
    List<AnnouncementCategory> selectBySort();
}