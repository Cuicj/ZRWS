package com.zrws.approval.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据导入请求DTO
 */
@Data
public class DataImportRequest {

    /** 批次号 */
    private String batchNo;

    /** BO编码 */
    private String boCode;

    /** 文件路径 */
    private String filePath;

    /** 文件内容（Base64） */
    private String fileContent;

    /** 导入模式: INSERT/UPDATE/INSERT_UPDATE/REPLACE */
    private String importMode;

    /** 是否使用AI校验 */
    private Boolean useAiValidation;

    /** 是否使用AI智能映射 */
    private Boolean useAiMapping;

    /** 是否自动修正可修复的错误 */
    private Boolean autoFix;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 额外参数 */
    private Map<String, Object> params;
}