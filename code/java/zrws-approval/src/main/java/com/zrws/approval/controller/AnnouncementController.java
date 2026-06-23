package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.Announcement;
import com.zrws.approval.domain.entity.AnnouncementAudit;
import com.zrws.approval.domain.entity.AnnouncementCategory;
import com.zrws.approval.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        return success(Map.of("success", true, "list", list));
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
        return success(Map.of("success", true, "announcement", announcement));
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTop() {
        return success(Map.of(
                "success", true,
                "list", announcementService.getTopAnnouncements()
        ));
    }

    /**
     * 获取推荐公告
     */
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommend() {
        return success(Map.of(
                "success", true,
                "list", announcementService.getRecommendAnnouncements()
        ));
    }

    /**
     * 获取热门公告
     */
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> getHot() {
        return success(Map.of(
                "success", true,
                "list", announcementService.getHotAnnouncements()
        ));
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        return success(Map.of(
                "success", true,
                "list", announcementService.getLatestAnnouncements(limit)
        ));
    }

    /**
     * 点赞
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long id) {
        announcementService.likeAnnouncement(id);
        return success(Map.of("success", true, "message", "点赞成功"));
    }

    // ============================================================
    // 分类管理
    // ============================================================

    /**
     * 获取所有分类
     */
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getCategories() {
        return success(Map.of(
                "success", true,
                "list", announcementService.getActiveCategories()
        ));
    }

    /**
     * 保存分类
     */
    @PostMapping("/category")
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestBody AnnouncementCategory category) {
        try {
            AnnouncementCategory saved = announcementService.saveCategory(category);
            return success(Map.of("success", true, "category", saved));
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
            return success(Map.of("success", true, "message", "删除成功"));
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
        return success(Map.of("success", true, "list", list));
    }

    /**
     * 保存公告
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody Announcement announcement) {
        try {
            Announcement saved = announcementService.saveAnnouncement(announcement);
            return success(Map.of("success", true, "announcement", saved));
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
            return success(Map.of("success", true, "message", "删除成功"));
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
            return success(Map.of("success", true, "message", "提交审核成功"));
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
            return success(Map.of("success", true, "message", "审批通过"));
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
            return success(Map.of("success", true, "message", "已驳回"));
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
            return success(Map.of("success", true, "message", "发布成功"));
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
            return success(Map.of("success", true, "message", "下线成功"));
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
        return success(Map.of(
                "success", true,
                "list", announcementService.getAuditRecords(id)
        ));
    }

    // ============================================================
    // 私有方法
    // ============================================================

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<?, ?>) data);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", message
        ));
    }
}