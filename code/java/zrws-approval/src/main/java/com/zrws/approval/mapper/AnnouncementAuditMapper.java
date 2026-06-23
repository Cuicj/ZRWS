package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.AnnouncementAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告审核记录 Mapper
 */
@Mapper
public interface AnnouncementAuditMapper extends BaseMapper<AnnouncementAudit> {

    /**
     * 获取公告的审核记录
     */
    List<AnnouncementAudit> selectByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 获取审核人的审核记录
     */
    List<AnnouncementAudit> selectByAuditorId(@Param("auditorId") Long auditorId);
}