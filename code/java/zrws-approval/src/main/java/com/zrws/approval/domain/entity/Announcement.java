package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_announcement")
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long announcementId;

    private String title;

    private String content;

    private String summary;

    private Long categoryId;

    private String categoryName;

    /** 封面图片 */
    private String coverImage;

    private String author;

    private String source;

    private String sourceUrl;

    /** 是否置顶 */
    private Integer isTop;

    /** 是否推荐 */
    private Integer isRecommend;

    /** 是否热门 */
    private Integer isHot;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime publishTime;

    private LocalDateTime offlineTime;

    /** 状态: DRAFT/PENDING/APPROVED/REJECTED/PUBLISHED/OFFLINE */
    private String status;

    /** 审核状态 */
    private String auditStatus;

    /** 驳回原因 */
    private String rejectionReason;

    private String keywords;

    private String tags;

    private Integer priority;

    @TableLogic
    private Integer isDeleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 公告状态枚举
     */
    public enum Status {
        DRAFT("草稿"),
        PENDING("待审核"),
        APPROVED("已审核"),
        REJECTED("已驳回"),
        PUBLISHED("已发布"),
        OFFLINE("已下线");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 审核状态枚举
     */
    public enum AuditStatus {
        PENDING("待审核"),
        PASSED("已通过"),
        REJECTED("已驳回");

        private final String desc;

        AuditStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}