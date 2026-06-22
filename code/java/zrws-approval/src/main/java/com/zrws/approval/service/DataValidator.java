package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.BoField;
import com.zrws.approval.domain.entity.EnumConfig;
import com.zrws.approval.mapper.EnumConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据校验服务
 * <p>根据BO字段配置，对导入数据进行校验
 */
@Slf4j
@Service
public class DataValidator {

    @Autowired
    private EnumConfigMapper enumConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /** 日期格式列表 */
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );

    /**
     * 校验单行数据
     */
    public ValidationResult validateRow(Map<String, Object> rowData, List<BoField> fieldConfigs) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        for (BoField field : fieldConfigs) {
            if (field.getStatus() != 1) continue; // 跳过禁用的字段

            Object value = rowData.get(field.getFieldCode());
            String valueStr = value != null ? String.valueOf(value).trim() : null;

            // 必填校验
            if (field.getIsRequired() == 1 && (valueStr == null || valueStr.isEmpty())) {
                result.addError(field.getFieldCode(), valueStr, "必填字段不能为空");
                continue;
            }

            // 如果字段为空且有默认值，使用默认值
            if ((valueStr == null || valueStr.isEmpty()) && field.getDefaultValue() != null) {
                rowData.put(field.getFieldCode(), field.getDefaultValue());
                valueStr = field.getDefaultValue();
            }

            // 如果仍然为空，跳过后续校验
            if (valueStr == null || valueStr.isEmpty()) {
                continue;
            }

            // 类型校验
            ValidationResult.FieldValidation fieldResult = validateField(valueStr, field);
            if (!fieldResult.isValid()) {
                result.addError(field.getFieldCode(), valueStr, fieldResult.getMessage());
            } else if (fieldResult.isWarning()) {
                result.addWarning(field.getFieldCode(), valueStr, fieldResult.getMessage());
            }

            // 特殊校验规则
            if (fieldResult.isValid() && field.getValidationRule() != null && !field.getValidationRule().isEmpty()) {
                ValidationResult.FieldValidation ruleResult = validateByRule(valueStr, field);
                if (!ruleResult.isValid()) {
                    result.addError(field.getFieldCode(), valueStr, ruleResult.getMessage());
                } else if (ruleResult.isWarning()) {
                    result.addWarning(field.getFieldCode(), valueStr, ruleResult.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * 校验单字段值
     */
    private ValidationResult.FieldValidation validateField(String value, BoField field) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        try {
            switch (field.getFieldType().toUpperCase()) {
                case "INT":
                case "BIGINT":
                    if (!isInteger(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的整数: " + value);
                    }
                    break;

                case "DECIMAL":
                case "DOUBLE":
                case "FLOAT":
                    if (!isNumeric(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的数字: " + value);
                    }
                    break;

                case "DATE":
                    if (!isValidDate(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的日期格式: " + value);
                    }
                    break;

                case "DATETIME":
                    if (!isValidDateTime(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的日期时间格式: " + value);
                    }
                    break;

                case "BOOLEAN":
                    if (!isBoolean(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的布尔值(应为true/false/是/否/1/0): " + value);
                    }
                    break;

                case "ENUM":
                    // 枚举校验在规则中处理
                    break;

                case "JSON":
                    if (!isValidJson(value)) {
                        result.setValid(false);
                        result.setMessage("不是有效的JSON格式: " + value);
                    }
                    break;

                case "STRING":
                case "TEXT":
                default:
                    // 字符串不做类型校验
                    break;
            }
        } catch (Exception e) {
            log.error("字段校验异常: field={}, value={}, error={}", field.getFieldCode(), value, e.getMessage());
            result.setValid(false);
            result.setMessage("校验过程发生错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据规则校验
     */
    private ValidationResult.FieldValidation validateByRule(String value, BoField field) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        String validationType = field.getValidationType();
        String rule = field.getValidationRule();

        if (validationType == null || rule == null) {
            return result;
        }

        switch (validationType.toUpperCase()) {
            case "RANGE":
                result = validateRange(value, rule, field.getFieldType());
                break;

            case "LENGTH":
                result = validateLength(value, rule);
                break;

            case "REGEX":
                result = validateRegex(value, rule);
                break;

            case "ENUM":
                result = validateEnum(value, field.getEnumValues(), field.getEnumCode());
                break;

            case "CUSTOM":
                result = validateCustom(value, rule);
                break;

            default:
                log.warn("未知的校验类型: {}", validationType);
        }

        return result;
    }

    /**
     * 范围校验
     * 规则格式: "min~max" 如 "0~100"
     */
    private ValidationResult.FieldValidation validateRange(String value, String rule, String fieldType) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        try {
            String[] parts = rule.split("~");
            if (parts.length != 2) {
                log.warn("无效的范围规则: {}", rule);
                return result;
            }

            String minStr = parts[0].trim();
            String maxStr = parts[1].trim();

            boolean hasMin = !minStr.isEmpty();
            boolean hasMax = !maxStr.isEmpty();

            // 根据字段类型进行校验
            if ("DECIMAL".equalsIgnoreCase(fieldType) || "DOUBLE".equalsIgnoreCase(fieldType) ||
                "FLOAT".equalsIgnoreCase(fieldType)) {

                BigDecimal numValue = new BigDecimal(value);
                BigDecimal min = hasMin ? new BigDecimal(minStr) : null;
                BigDecimal max = hasMax ? new BigDecimal(maxStr) : null;

                if (hasMin && numValue.compareTo(min) < 0) {
                    result.setValid(false);
                    result.setMessage(String.format("数值不能小于 %s (当前: %s)", minStr, value));
                } else if (hasMax && numValue.compareTo(max) > 0) {
                    result.setValid(false);
                    result.setMessage(String.format("数值不能大于 %s (当前: %s)", maxStr, value));
                }

            } else if ("INT".equalsIgnoreCase(fieldType) || "BIGINT".equalsIgnoreCase(fieldType)) {

                long numValue = Long.parseLong(value);
                Long min = hasMin ? Long.parseLong(minStr) : null;
                Long max = hasMax ? Long.parseLong(maxStr) : null;

                if (hasMin && numValue < min) {
                    result.setValid(false);
                    result.setMessage(String.format("数值不能小于 %s (当前: %s)", minStr, value));
                } else if (hasMax && numValue > max) {
                    result.setValid(false);
                    result.setMessage(String.format("数值不能大于 %s (当前: %s)", maxStr, value));
                }
            }
        } catch (NumberFormatException e) {
            result.setValid(false);
            result.setMessage("数值格式错误: " + value);
        }

        return result;
    }

    /**
     * 长度校验
     * 规则格式: "min~max" 或 "exact" 如 "0~100" 或 "11"
     */
    private ValidationResult.FieldValidation validateLength(String value, String rule) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        int length = value.length();

        if (rule.contains("~")) {
            String[] parts = rule.split("~");
            int min = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0].trim());
            int max = parts[1].isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(parts[1].trim());

            if (length < min) {
                result.setValid(false);
                result.setMessage(String.format("长度不能小于 %d (当前: %d)", min, length));
            } else if (length > max) {
                result.setValid(false);
                result.setMessage(String.format("长度不能大于 %d (当前: %d)", max, length));
            }
        } else {
            int exact = Integer.parseInt(rule.trim());
            if (length != exact) {
                result.setValid(false);
                result.setMessage(String.format("长度必须为 %d (当前: %d)", exact, length));
            }
        }

        return result;
    }

    /**
     * 正则表达式校验
     */
    private ValidationResult.FieldValidation validateRegex(String value, String pattern) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        try {
            if (!Pattern.matches(pattern, value)) {
                result.setValid(false);
                result.setMessage("格式不符合要求");
            }
        } catch (Exception e) {
            log.error("正则校验异常: pattern={}, error={}", pattern, e.getMessage());
            result.setValid(false);
            result.setMessage("校验规则配置错误");
        }

        return result;
    }

    /**
     * 枚举值校验
     */
    private ValidationResult.FieldValidation validateEnum(String value, String enumValues, String enumCode) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        Set<String> validValues = new HashSet<>();

        // 从配置的枚举值获取
        if (enumValues != null && !enumValues.isEmpty()) {
            try {
                List<String> values = objectMapper.readValue(enumValues, List.class);
                validValues.addAll(values);
            } catch (Exception e) {
                // 配置格式错误，使用原值作为逗号分隔
                String[] parts = enumValues.split(",");
                for (String part : parts) {
                    validValues.add(part.trim());
                }
            }
        }

        // 从数据库枚举配置获取
        if (enumCode != null && !enumCode.isEmpty()) {
            List<EnumConfig> configs = enumConfigMapper.selectActiveByCode(enumCode);
            validValues.addAll(configs.stream()
                    .map(EnumConfig::getItemValue)
                    .collect(Collectors.toSet()));
        }

        // 忽略大小写比较
        boolean found = validValues.stream()
                .anyMatch(v -> v.equalsIgnoreCase(value));

        if (!found && !validValues.isEmpty()) {
            result.setValid(false);
            result.setMessage(String.format("值必须在枚举范围内: %s", validValues));
        }

        return result;
    }

    /**
     * 自定义校验（预留）
     */
    private ValidationResult.FieldValidation validateCustom(String value, String rule) {
        ValidationResult.FieldValidation result = new ValidationResult.FieldValidation();
        result.setValid(true);

        // TODO: 实现自定义校验逻辑
        // 可以通过SpEL表达式、脚本语言等方式实现

        return result;
    }

    // ==================== 辅助方法 ====================

    private boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNumeric(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate.parse(value, formatter);
                return true;
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }
        return false;
    }

    private boolean isValidDateTime(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime.parse(value, formatter);
                return true;
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }
        return false;
    }

    private boolean isBoolean(String value) {
        String lower = value.toLowerCase();
        return "true".equals(lower) || "false".equals(lower) ||
               "1".equals(lower) || "0".equals(lower) ||
               "是".equals(lower) || "否".equals(lower) ||
               "yes".equals(lower) || "no".equals(lower);
    }

    private boolean isValidJson(String value) {
        try {
            objectMapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验结果封装
     */
    @lombok.Data
    public static class ValidationResult {
        private boolean valid;
        private List<FieldValidation> errors;
        private List<FieldValidation> warnings;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void addError(String field, String value, String message) {
            this.errors.add(new FieldValidation(field, value, message, false, false));
            this.valid = false;
        }

        public void addWarning(String field, String value, String message) {
            this.warnings.add(new FieldValidation(field, value, message, false, true));
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class FieldValidation {
            private String field;
            private String value;
            private String message;
            private boolean valid;
            private boolean warning;

            public FieldValidation() {
                this.valid = true;
                this.warning = false;
            }

            public boolean isValid() {
                return valid;
            }

            public boolean isWarning() {
                return warning;
            }
        }
    }
}