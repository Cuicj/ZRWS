package com.zrws.approval.dto;

import lombok.Data;

import java.util.Map;

/**
 * 视频生成请求 DTO
 */
@Data
public class VideoGenerateRequest {

    private String taskName;

    private String provider;

    private String generationMode;

    private String prompt;

    private String promptEn;

    private String negativePrompt;

    private String referenceImageUrl;

    private Integer duration;

    private Integer width;

    private Integer height;

    private Integer fps;

    private String modelName;

    private String stylePreset;

    private String callbackUrl;

    private Map<String, Object> extraParams;
}
