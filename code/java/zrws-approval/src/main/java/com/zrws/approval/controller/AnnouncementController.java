package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.Announcement;
import com.zrws.approval.domain.entity.AnnouncementAudit;
import com.zrws.approval.domain.entity.AnnouncementCategory;
import com.zrws.approval.service.AnnouncementService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告栏 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/announcement")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // ============================================================
    // 公告查看(公开接口)
    // ============================================================

    /**
     * 获取已发布的公告列表
     */
    @GetMapping("/list")
    public R<List<Announcement>> getPublishedList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        List<Announcement> list;
        if (keyword != null && !keyword.isEmpty()) {
            list = announcementService.searchAnnouncements(keyword);
        } else if (categoryId != null) {
            list = announcementService.getAnnouncementsByCategory(categoryId);
        } else {
            list = announcementService.getPublishedAnnouncements();
        }
        return R.ok(list);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public R<Announcement> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return R.fail("公告不存在");
        }
        announcementService.viewAnnouncement(id);
        return R.ok(announcement);
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public R<List<Announcement>> getTop() {
        return R.ok(announcementService.getTopAnnouncements());
    }

    /**
     * 获取推荐公告
     */
    @GetMapping("/recommend")
    public R<List<Announcement>> getRecommend() {
        return R.ok(announcementService.getRecommendAnnouncements());
    }

    /**
     * 获取热门公告
     */
    @GetMapping("/hot")
    public R<List<Announcement>> getHot() {
        return R.ok(announcementService.getHotAnnouncements());
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public R<List<Announcement>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(announcementService.getLatestAnnouncements(limit));
    }

    /**
     * 点赞
     */
    @PostMapping("/{id}/like")
    public R<String> like(@PathVariable Long id) {
        announcementService.likeAnnouncement(id);
        return R.ok("点赞成功");
    }

    // ============================================================
    // 分类管理
    // ============================================================

    /**
     * 获取所有分类
     */
    @GetMapping("/category")
    public R<List<AnnouncementCategory>> getCategories() {
        return R.ok(announcementService.getActiveCategories());
    }

    /**
     * 保存分类
     */
    @PostMapping("/category")
    public R<AnnouncementCategory> saveCategory(@RequestBody AnnouncementCategory category) {
        try {
            AnnouncementCategory saved = announcementService.saveCategory(category);
            return R.ok("保存成功", saved);
        } catch (Exception e) {
            log.error("保存分类失败", e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/{id}")
    public R<String> deleteCategory(@PathVariable Long id) {
        try {
            announcementService.deleteCategory(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 公告管理
    // ============================================================

    /**
     * 获取所有公告(管理)
     */
    @GetMapping("/manage/list")
    public R<List<Announcement>> getManageList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auditStatus) {
        List<Announcement> list;
        if ("PENDING".equals(status)) {
            list = announcementService.getPendingAnnouncements();
        } else {
            list = announcementService.getPublishedAnnouncements();
        }
        return R.ok(list);
    }

    /**
     * 保存公告
     */
    @PostMapping
    public R<Announcement> save(@RequestBody Announcement announcement) {
        try {
            Announcement saved = announcementService.saveAnnouncement(announcement);
            return R.ok("保存成功", saved);
        } catch (Exception e) {
            log.error("保存公告失败", e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 审核管理
    // ============================================================

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit")
    public R<String> submitAudit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.submitForAudit(id, operatorId, operatorName);
            return R.ok("提交审核成功");
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return R.fail("提交失败: " + e.getMessage());
        }
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public R<String> approve(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditInfo) {
        try {
            Long auditorId = auditInfo.get("auditorId") != null ?
                    Long.valueOf(auditInfo.get("auditorId").toString()) : 1L;
            String auditorName = (String) auditInfo.getOrDefault("auditorName", "管理员");
            String comment = (String) auditInfo.get("comment");
            announcementService.approve(id, auditorId, auditorName, comment);
            return R.ok("审批通过");
        } catch (Exception e) {
            log.error("审批失败", e);
            return R.fail("审批失败: " + e.getMessage());
        }
    }

    /**
     * 驳回
     */
    @PostMapping("/{id}/reject")
    public R<String> reject(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditInfo) {
        try {
            Long auditorId = auditInfo.get("auditorId") != null ?
                    Long.valueOf(auditInfo.get("auditorId").toString()) : 1L;
            String auditorName = (String) auditInfo.getOrDefault("auditorName", "管理员");
            String reason = (String) auditInfo.get("reason");
            announcementService.reject(id, auditorId, auditorName, reason);
            return R.ok("已驳回");
        } catch (Exception e) {
            log.error("驳回失败", e);
            return R.fail("驳回失败: " + e.getMessage());
        }
    }

    /**
     * 发布
     */
    @PostMapping("/{id}/publish")
    public R<String> publish(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.publish(id, operatorId, operatorName);
            return R.ok("发布成功");
        } catch (Exception e) {
            log.error("发布失败", e);
            return R.fail("发布失败: " + e.getMessage());
        }
    }

    /**
     * 下线
     */
    @PostMapping("/{id}/offline")
    public R<String> offline(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.offline(id, operatorId, operatorName);
            return R.ok("下线成功");
        } catch (Exception e) {
            log.error("下线失败", e);
            return R.fail("下线失败: " + e.getMessage());
        }
    }

    /**
     * 获取审核记录
     */
    @GetMapping("/{id}/audit")
    public R<List<AnnouncementAudit>> getAuditRecords(@PathVariable Long id) {
        return R.ok(announcementService.getAuditRecords(id));
    }

    // ============================================================
    // AI分析与行政区域
    // ============================================================

    /**
     * 获取所有行政区域级别
     */
    @GetMapping("/admin-levels")
    public R<List<Map<String, Object>>> getAdminLevels() {
        return R.ok(announcementService.getAllAdminLevels());
    }

    /**
     * 按行政区域级别获取公告
     */
    @GetMapping("/by-level")
    public R<List<Announcement>> getByAdminLevel(@RequestParam String level) {
        return R.ok(announcementService.getAnnouncementsByAdminLevel(level));
    }

    /**
     * 按行政区域获取公告
     */
    @GetMapping("/by-region")
    public R<List<Announcement>> getByRegion(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String township) {
        return R.ok(announcementService.getAnnouncementsByRegion(province, city, county, township));
    }

    /**
     * AI分析单条公告
     */
    @PostMapping("/{id}/analyze")
    public R<Announcement> analyzeWithAI(@PathVariable Long id) {
        try {
            Announcement analyzed = announcementService.analyzeWithAI(id);
            return R.ok("AI分析完成", analyzed);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return R.fail("AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 批量AI分析
     */
    @PostMapping("/batch-analyze")
    public R<Map<String, Object>> batchAnalyzeWithAI(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return R.fail("请选择要分析的公告");
            }
            int count = announcementService.batchAnalyzeWithAI(ids);
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            return R.ok("成功分析 " + count + " 条公告", data);
        } catch (Exception e) {
            log.error("批量AI分析失败", e);
            return R.fail("批量AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getStatistics() {
        return R.ok(announcementService.getStatistics());
    }
}
