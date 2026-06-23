package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告 Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 获取已发布的公告
     */
    List<Announcement> selectPublished();

    /**
     * 获取分类下的公告
     */
    List<Announcement> selectByCategory(@Param("categoryId") Long categoryId);

    /**
     * 获取置顶公告
     */
    List<Announcement> selectTop();

    /**
     * 获取推荐公告
     */
    List<Announcement> selectRecommend();

    /**
     * 获取热门公告
     */
    List<Announcement> selectHot();

    /**
     * 获取待审核公告
     */
    List<Announcement> selectPending();

    /**
     * 获取今日公告
     */
    List<Announcement> selectToday();

    /**
     * 关键词搜索
     */
    List<Announcement> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取最新公告
     */
    List<Announcement> selectLatest(@Param("limit") int limit);

    /**
     * 增加浏览次数
     */
    int incrementViewCount(@Param("announcementId") Long announcementId);

    /**
     * 增加点赞次数
     */
    int incrementLikeCount(@Param("announcementId") Long announcementId);
}