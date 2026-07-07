package com.zrws.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI视频生成任务实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("zrws_video_task")
public class VideoTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 任务名称 */
    private String taskName;

    /** 视频生成提供商: KUAISHOU_KELING / BYTE_JIMENG / TENCENT_HUNYUAN / CUSTOM */
    private String provider;

    /** 生成模式: TEXT_TO_VIDEO / IMAGE_TO_VIDEO */
    private String generationMode;

    /** 提示词（中文） */
    private String prompt;

    /** 英文提示词（部分API需要） */
    private String promptEn;

    /** 负面提示词 */
    private String negativePrompt;

    /** 参考图片URL（图生视频模式） */
    private String referenceImageUrl;

    /** 视频时长（秒） */
    private Integer duration;

    /** 视频宽度 */
    private Integer width;

    /** 视频高度 */
    private Integer height;

    /** 帧率 */
    private Integer fps;

    /** 模型名称 */
    private String modelName;

    /** 风格预设 */
    private String stylePreset;

    /** 提供商侧任务ID */
    private String providerTaskId;

    /** 生成的视频URL */
    private String videoUrl;

    /** 视频本地存储路径 */
    private String videoPath;

    /** 视频文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 封面图URL */
    private String coverUrl;

    /** 状态: PENDING / PROCESSING / SUCCESS / FAILED / CANCELLED */
    private String status;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 开始生成时间 */
    private LocalDateTime startTime;

    /** 完成时间 */
    private LocalDateTime endTime;

    /** 错误信息 */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount;

    /** 扩展参数JSON */
    private String extraParams;

    /** 回调URL（生成完成后通知） */
    private String callbackUrl;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    private Long tenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    public enum TaskStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    public enum Provider {
        KUAISHOU_KELING,
        BYTE_JIMENG,
        TENCENT_HUNYUAN,
        CUSTOM
    }

    public enum GenerationMode {
        TEXT_TO_VIDEO,
        IMAGE_TO_VIDEO
    }
}
