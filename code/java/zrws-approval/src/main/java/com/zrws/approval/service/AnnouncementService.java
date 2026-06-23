package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.Announcement;
import com.zrws.approval.domain.entity.AnnouncementAudit;
import com.zrws.approval.domain.entity.AnnouncementCategory;
import com.zrws.approval.mapper.AnnouncementAuditMapper;
import com.zrws.approval.mapper.AnnouncementCategoryMapper;
import com.zrws.approval.mapper.AnnouncementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务
 */
@Slf4j
@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private AnnouncementCategoryMapper categoryMapper;

    @Autowired
    private AnnouncementAuditMapper auditMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================
    // 分类管理
    // ============================================================

    public List<AnnouncementCategory> getAllCategories() {
        return categoryMapper.selectBySort();
    }

    public List<AnnouncementCategory> getActiveCategories() {
        return categoryMapper.selectActive();
    }

    public AnnouncementCategory getCategoryById(Long categoryId) {
        return categoryMapper.selectById(categoryId);
    }

    @Transactional
    public AnnouncementCategory saveCategory(AnnouncementCategory category) {
        if (category.getCategoryId() != null) {
            categoryMapper.updateById(category);
        } else {
            categoryMapper.insert(category);
        }
        return category;
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryMapper.deleteById(categoryId);
    }

    // ============================================================
    // 公告管理
    // ============================================================

    public List<Announcement> getPublishedAnnouncements() {
        return announcementMapper.selectPublished();
    }

    public List<Announcement> getAnnouncementsByCategory(Long categoryId) {
        return announcementMapper.selectByCategory(categoryId);
    }

    public List<Announcement> getTopAnnouncements() {
        return announcementMapper.selectTop();
    }

    public List<Announcement> getRecommendAnnouncements() {
        return announcementMapper.selectRecommend();
    }

    public List<Announcement> getHotAnnouncements() {
        return announcementMapper.selectHot();
    }

    public List<Announcement> getPendingAnnouncements() {
        return announcementMapper.selectPending();
    }

    public List<Announcement> getLatestAnnouncements(int limit) {
        return announcementMapper.selectLatest(limit);
    }

    public List<Announcement> getTodayAnnouncements() {
        return announcementMapper.selectToday();
    }

    public List<Announcement> searchAnnouncements(String keyword) {
        return announcementMapper.searchByKeyword(keyword);
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Transactional
    public Announcement saveAnnouncement(Announcement announcement) {
        if (announcement.getAnnouncementId() != null) {
            announcementMapper.updateById(announcement);
        } else {
            announcement.setStatus("DRAFT");
            announcement.setAuditStatus("PENDING");
            announcement.setViewCount(0);
            announcement.setLikeCount(0);
            announcement.setCommentCount(0);
            announcementMapper.insert(announcement);
        }
        return announcement;
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        announcementMapper.deleteById(announcementId);
    }

    @Transactional
    public void viewAnnouncement(Long announcementId) {
        announcementMapper.incrementViewCount(announcementId);
    }

    @Transactional
    public void likeAnnouncement(Long announcementId) {
        announcementMapper.incrementLikeCount(announcementId);
    }

    // ============================================================
    // 审核管理
    // ============================================================

    /**
     * 提交审核
     */
    @Transactional
    public void submitForAudit(Long announcementId, Long operatorId, String operatorName) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcement.setStatus("PENDING");
        announcement.setAuditStatus("PENDING");
        announcementMapper.updateById(announcement);

        // 记录审核
        AnnouncementAudit audit = new AnnouncementAudit();
        audit.setAnnouncementId(announcementId);
        audit.setAuditType("SUBMIT");
        audit.setAuditResult("PENDING");
        audit.setComment("提交审核");
        audit.setAuditorId(operatorId);
        audit.setAuditorName(operatorName);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approve(Long announcementId, Long auditorId, String auditorName, String comment) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcement.setStatus("APPROVED");
        announcement.setAuditStatus("PASSED");
        announcementMapper.updateById(announcement);

        // 记录审核
        AnnouncementAudit audit = new AnnouncementAudit();
        audit.setAnnouncementId(announcementId);
        audit.setAuditType("APPROVE");
        audit.setAuditResult("PASSED");
        audit.setComment(comment != null ? comment : "审批通过");
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    /**
     * 驳回
     */
    @Transactional
    public void reject(Long announcementId, Long auditorId, String auditorName, String reason) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcement.setStatus("REJECTED");
        announcement.setAuditStatus("REJECTED");
        announcement.setRejectionReason(reason);
        announcementMapper.updateById(announcement);

        // 记录审核
        AnnouncementAudit audit = new AnnouncementAudit();
        audit.setAnnouncementId(announcementId);
        audit.setAuditType("REJECT");
        audit.setAuditResult("REJECTED");
        audit.setRejectReason(reason);
        audit.setComment("驳回: " + reason);
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    /**
     * 发布
     */
    @Transactional
    public void publish(Long announcementId, Long operatorId, String operatorName) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcement.setStatus("PUBLISHED");
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // 记录审核
        AnnouncementAudit audit = new AnnouncementAudit();
        audit.setAnnouncementId(announcementId);
        audit.setAuditType("PUBLISH");
        audit.setAuditResult("PUBLISHED");
        audit.setComment("发布上线");
        audit.setAuditorId(operatorId);
        audit.setAuditorName(operatorName);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    /**
     * 下线
     */
    @Transactional
    public void offline(Long announcementId, Long operatorId, String operatorName) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        announcement.setStatus("OFFLINE");
        announcement.setOfflineTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // 记录审核
        AnnouncementAudit audit = new AnnouncementAudit();
        audit.setAnnouncementId(announcementId);
        audit.setAuditType("OFFLINE");
        audit.setAuditResult("OFFLINE");
        audit.setComment("下线");
        audit.setAuditorId(operatorId);
        audit.setAuditorName(operatorName);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
    }

    /**
     * 获取审核记录
     */
    public List<AnnouncementAudit> getAuditRecords(Long announcementId) {
        return auditMapper.selectByAnnouncementId(announcementId);
    }
}