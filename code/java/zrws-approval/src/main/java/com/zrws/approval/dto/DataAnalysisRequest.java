package com.zrws.approval.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据分析请求DTO
 */
@Data
public class DataAnalysisRequest {

    /** BO编码 */
    private String boCode;

    /** 文件路径 */
    private String filePath;

    /** 文件内容（Base64或直接文本） */
    private String fileContent;

    /** 数据类型 */
    private String dataType;

    /** 分析模式: QUICK/FULL/AI_ANALYSIS */
    private String analysisMode;

    /** 自定义参数 */
    private Map<String, Object> params;

    /** 是否使用AI增强分析 */
    private Boolean useAiEnhance;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;
}