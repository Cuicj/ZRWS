package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    // ============================================================
    // AI分析与行政区域
    // ============================================================

    /**
     * 获取所有行政区域级别
     */
    public List<Map<String, Object>> getAllAdminLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        for (Announcement.AdminLevel level : Announcement.AdminLevel.values()) {
            Map<String, Object> levelMap = new HashMap<>();
            levelMap.put("code", level.name());
            levelMap.put("name", level.getDesc());
            levels.add(levelMap);
        }
        return levels;
    }

    /**
     * 按行政区域级别获取公告
     */
    public List<Announcement> getAnnouncementsByAdminLevel(String adminLevel) {
        return announcementMapper.selectByAdminLevel(adminLevel);
    }

    /**
     * 按行政区域获取公告（支持多级）
     */
    public List<Announcement> getAnnouncementsByRegion(String province, String city, String county, String township) {
        return announcementMapper.selectByRegion(province, city, county, township);
    }

    /**
     * AI分析公告内容
     */
    @Transactional
    public Announcement analyzeWithAI(Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }

        String content = announcement.getContent();
        if (content == null || content.isEmpty()) {
            return announcement;
        }

        // 1. 生成AI摘要
        String aiSummary = generateAISummary(content);
        announcement.setAiSummary(aiSummary);

        // 2. 提取关键词
        String aiKeywords = extractKeywords(content);
        announcement.setAiKeywords(aiKeywords);

        // 3. 生成时间线
        String aiTimeline = generateTimeline(content);
        announcement.setAiTimeline(aiTimeline);

        // 4. 自动识别行政区域
        identifyAdminLevel(announcement);

        // 5. 识别土地类型
        identifyLandType(announcement);

        announcementMapper.updateById(announcement);
        log.info("AI分析完成: 公告ID={}, 行政级别={}", announcementId, announcement.getAdminLevel());

        return announcement;
    }

    /**
     * 批量AI分析
     */
    @Transactional
    public int batchAnalyzeWithAI(List<Long> announcementIds) {
        int count = 0;
        for (Long id : announcementIds) {
            try {
                analyzeWithAI(id);
                count++;
            } catch (Exception e) {
                log.error("AI分析失败: 公告ID={}, 错误={}", id, e.getMessage());
            }
        }
        return count;
    }

    /**
     * 生成AI摘要
     */
    private String generateAISummary(String content) {
        // 简单实现：取前200字作为摘要
        if (content.length() <= 200) {
            return content;
        }
        String summary = content.substring(0, 200);
        // 避免截断句子
        int lastDot = summary.lastIndexOf('。');
        if (lastDot > 150) {
            summary = summary.substring(0, lastDot + 1);
        }
        return summary + "...";
    }

    /**
     * 提取关键词
     */
    private String extractKeywords(String content) {
        // 简单实现：提取土地相关关键词
        Set<String> keywords = new HashSet<>();

        // 土地相关词汇
        String[] landKeywords = {
            "耕地", "农田", "土壤", "土地", "林地", "草地", "水域", "建设用地",
            "宅基地", "承包地", "流转", "征收", "占用", "复垦", "整治",
            "基本农田", "永久基本农田", "耕地保护", "占补平衡", "增减挂钩"
        };

        // 政策相关词汇
        String[] policyKeywords = {
            "通知", "办法", "条例", "规定", "标准", "规划", "方案", "意见",
            "决定", "批复", "函", "公告"
        };

        // 提取土地关键词
        for (String keyword : landKeywords) {
            if (content.contains(keyword)) {
                keywords.add(keyword);
            }
        }

        // 提取政策关键词
        for (String keyword : policyKeywords) {
            if (content.contains(keyword)) {
                keywords.add(keyword);
            }
        }

        return String.join(",", keywords);
    }

    /**
     * 生成时间线
     */
    private String generateTimeline(String content) {
        try {
            List<Map<String, Object>> timeline = new ArrayList<>();

            // 提取日期
            Pattern datePattern = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日?");
            Matcher matcher = datePattern.matcher(content);

            while (matcher.find()) {
                Map<String, Object> event = new HashMap<>();
                String year = matcher.group(1);
                String month = matcher.group(2);
                String day = matcher.group(3);
                String dateStr = String.format("%s-%02d-%02d", year, Integer.parseInt(month), Integer.parseInt(day));

                event.put("date", dateStr);
                event.put("year", year);
                event.put("month", month);
                event.put("day", day);

                // 提取事件描述（简化处理）
                String eventText = matcher.group();
                int start = Math.max(0, matcher.start() - 10);
                int end = Math.min(content.length(), matcher.end() + 50);
                String context = content.substring(start, end).replace("\n", " ").trim();
                event.put("description", context);

                timeline.add(event);
            }

            // 按日期排序
            timeline.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("date")));

            // 返回JSON
            return objectMapper.writeValueAsString(timeline);
        } catch (Exception e) {
            log.error("生成时间线失败: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 识别行政区域级别
     */
    private void identifyAdminLevel(Announcement announcement) {
        String content = announcement.getContent();

        // 识别省级
        if (content.contains("省人民政府") || content.contains("省自然资源厅") ||
            content.contains("省农业农村厅") || content.contains("省级")) {
            announcement.setAdminLevel("PROVINCE");
        }
        // 识别市级
        else if (content.contains("市人民政府") || content.contains("市自然资源局") ||
                 content.contains("市农业农村局") || content.contains("市级")) {
            announcement.setAdminLevel("CITY");
        }
        // 识别县级
        else if (content.contains("县人民政府") || content.contains("县自然资源局") ||
                 content.contains("县农业农村局") || content.contains("县级")) {
            announcement.setAdminLevel("COUNTY");
        }
        // 识别乡级
        else if (content.contains("乡人民政府") || content.contains("镇人民政府") ||
                 content.contains("乡镇") || content.contains("乡级")) {
            announcement.setAdminLevel("TOWNSHIP");
        }
        // 默认县级
        else {
            announcement.setAdminLevel("COUNTY");
        }
    }

    /**
     * 识别土地类型
     */
    private void identifyLandType(Announcement announcement) {
        String content = announcement.getContent();

        if (content.contains("耕地") || content.contains("基本农田")) {
            announcement.setLandType("农田");
        } else if (content.contains("林地")) {
            announcement.setLandType("林地");
        } else if (content.contains("草地")) {
            announcement.setLandType("草地");
        } else if (content.contains("水域") || content.contains("河流") || content.contains("湖泊")) {
            announcement.setLandType("水域");
        } else if (content.contains("建设用地") || content.contains("宅基地")) {
            announcement.setLandType("建设用地");
        } else if (content.contains("未利用地")) {
            announcement.setLandType("未利用地");
        } else {
            announcement.setLandType("综合");
        }
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总数
        long total = announcementMapper.selectCount(null);
        stats.put("total", total);

        // 按行政区域统计
        Map<String, Long> byAdminLevel = new HashMap<>();
        for (Announcement.AdminLevel level : Announcement.AdminLevel.values()) {
            List<Announcement> list = announcementMapper.selectByAdminLevel(level.name());
            byAdminLevel.put(level.name(), (long) list.size());
        }
        stats.put("byAdminLevel", byAdminLevel);

        // 按土地类型统计
        Map<String, Long> byLandType = new HashMap<>();
        List<Announcement> allAnnouncements = announcementMapper.selectPublished();
        byLandType = allAnnouncements.stream()
            .filter(a -> a.getLandType() != null)
            .collect(Collectors.groupingBy(Announcement::getLandType, Collectors.counting()));
        stats.put("byLandType", byLandType);

        // 本月新增
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long thisMonth = allAnnouncements.stream()
            .filter(a -> a.getCreatedTime() != null && a.getCreatedTime().isAfter(startOfMonth))
            .count();
        stats.put("thisMonth", thisMonth);

        return stats;
    }
}