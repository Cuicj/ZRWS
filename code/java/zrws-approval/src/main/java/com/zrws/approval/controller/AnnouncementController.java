package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.Announcement;
import com.zrws.approval.domain.entity.AnnouncementAudit;
import com.zrws.approval.domain.entity.AnnouncementCategory;
import com.zrws.approval.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<Map<String, Object>> getPublishedList(
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
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", list);
        return success(result);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return error("公告不存在");
        }
        // 增加浏览次数
        announcementService.viewAnnouncement(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("announcement", announcement);
        return success(result);
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTop() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getTopAnnouncements());
        return success(result);
    }

    /**
     * 获取推荐公告
     */
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommend() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getRecommendAnnouncements());
        return success(result);
    }

    /**
     * 获取热门公告
     */
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> getHot() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getHotAnnouncements());
        return success(result);
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getLatestAnnouncements(limit));
        return success(result);
    }

    /**
     * 点赞
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long id) {
        announcementService.likeAnnouncement(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "点赞成功");
        return success(result);
    }

    // ============================================================
    // 分类管理
    // ============================================================

    /**
     * 获取所有分类
     */
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getActiveCategories());
        return success(result);
    }

    /**
     * 保存分类
     */
    @PostMapping("/category")
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestBody AnnouncementCategory category) {
        try {
            AnnouncementCategory saved = announcementService.saveCategory(category);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("category", saved);
            return success(result);
        } catch (Exception e) {
            log.error("保存分类失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            announcementService.deleteCategory(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return success(result);
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 公告管理
    // ============================================================

    /**
     * 获取所有公告(管理)
     */
    @GetMapping("/manage/list")
    public ResponseEntity<Map<String, Object>> getManageList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auditStatus) {
        List<Announcement> list;
        if ("PENDING".equals(status)) {
            list = announcementService.getPendingAnnouncements();
        } else {
            list = announcementService.getPublishedAnnouncements();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", list);
        return success(result);
    }

    /**
     * 保存公告
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody Announcement announcement) {
        try {
            Announcement saved = announcementService.saveAnnouncement(announcement);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("announcement", saved);
            return success(result);
        } catch (Exception e) {
            log.error("保存公告失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return success(result);
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    // ============================================================
    // 审核管理
    // ============================================================

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAudit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.submitForAudit(id, operatorId, operatorName);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "提交审核成功");
            return success(result);
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditInfo) {
        try {
            Long auditorId = auditInfo.get("auditorId") != null ?
                    Long.valueOf(auditInfo.get("auditorId").toString()) : 1L;
            String auditorName = (String) auditInfo.getOrDefault("auditorName", "管理员");
            String comment = (String) auditInfo.get("comment");
            announcementService.approve(id, auditorId, auditorName, comment);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "审批通过");
            return success(result);
        } catch (Exception e) {
            log.error("审批失败", e);
            return error("审批失败: " + e.getMessage());
        }
    }

    /**
     * 驳回
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditInfo) {
        try {
            Long auditorId = auditInfo.get("auditorId") != null ?
                    Long.valueOf(auditInfo.get("auditorId").toString()) : 1L;
            String auditorName = (String) auditInfo.getOrDefault("auditorName", "管理员");
            String reason = (String) auditInfo.get("reason");
            announcementService.reject(id, auditorId, auditorName, reason);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已驳回");
            return success(result);
        } catch (Exception e) {
            log.error("驳回失败", e);
            return error("驳回失败: " + e.getMessage());
        }
    }

    /**
     * 发布
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publish(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.publish(id, operatorId, operatorName);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "发布成功");
            return success(result);
        } catch (Exception e) {
            log.error("发布失败", e);
            return error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 下线
     */
    @PostMapping("/{id}/offline")
    public ResponseEntity<Map<String, Object>> offline(
            @PathVariable Long id,
            @RequestBody Map<String, Object> operator) {
        try {
            Long operatorId = operator.get("operatorId") != null ?
                    Long.valueOf(operator.get("operatorId").toString()) : 1L;
            String operatorName = (String) operator.getOrDefault("operatorName", "管理员");
            announcementService.offline(id, operatorId, operatorName);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "下线成功");
            return success(result);
        } catch (Exception e) {
            log.error("下线失败", e);
            return error("下线失败: " + e.getMessage());
        }
    }

    /**
     * 获取审核记录
     */
    @GetMapping("/{id}/audit")
    public ResponseEntity<Map<String, Object>> getAuditRecords(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getAuditRecords(id));
        return success(result);
    }

    // ============================================================
    // AI分析与行政区域
    // ============================================================

    /**
     * 获取所有行政区域级别
     */
    @GetMapping("/admin-levels")
    public ResponseEntity<Map<String, Object>> getAdminLevels() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getAllAdminLevels());
        return success(result);
    }

    /**
     * 按行政区域级别获取公告
     */
    @GetMapping("/by-level")
    public ResponseEntity<Map<String, Object>> getByAdminLevel(@RequestParam String level) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getAnnouncementsByAdminLevel(level));
        return success(result);
    }

    /**
     * 按行政区域获取公告
     */
    @GetMapping("/by-region")
    public ResponseEntity<Map<String, Object>> getByRegion(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String township) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", announcementService.getAnnouncementsByRegion(province, city, county, township));
        return success(result);
    }

    /**
     * AI分析单条公告
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeWithAI(@PathVariable Long id) {
        try {
            Announcement analyzed = announcementService.analyzeWithAI(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "AI分析完成");
            result.put("announcement", analyzed);
            return success(result);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return error("AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 批量AI分析
     */
    @PostMapping("/batch-analyze")
    public ResponseEntity<Map<String, Object>> batchAnalyzeWithAI(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return error("请选择要分析的公告");
            }
            int count = announcementService.batchAnalyzeWithAI(ids);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "成功分析 " + count + " 条公告");
            result.put("count", count);
            return success(result);
        } catch (Exception e) {
            log.error("批量AI分析失败", e);
            return error("批量AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("statistics", announcementService.getStatistics());
        return success(result);
    }

    // ============================================================
    // 私有方法
    // ============================================================

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            result.putAll(dataMap);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return ResponseEntity.badRequest().body(result);
    }
}