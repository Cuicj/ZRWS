package com.zrws.approval.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.BoDefinition;
import com.zrws.approval.domain.entity.BoField;
import com.zrws.approval.domain.entity.ExportTask;
import com.zrws.approval.mapper.BoDefinitionMapper;
import com.zrws.approval.mapper.BoFieldMapper;
import com.zrws.approval.mapper.ExportTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导出服务
 * <p>负责数据查询、Excel/PDF导出
 */
@Slf4j
@Service
public class DataExportService {

    @Autowired
    private BoDefinitionMapper boDefinitionMapper;

    @Autowired
    private BoFieldMapper boFieldMapper;

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建导出任务
     */
    public ExportTask createExportTask(String boCode, String fileFormat,
                                       String filterConditions, String fieldList) {
        ExportTask task = new ExportTask();
        task.setTaskNo(generateTaskNo());
        task.setTaskName("数据导出-" + boCode);
        task.setBoCode(boCode);
        task.setExportType("FULL");
        task.setFileFormat(fileFormat);
        task.setFilterConditions(filterConditions);
        task.setFieldList(fieldList);
        task.setStatus(ExportTask.TaskStatus.PENDING.name());
        task.setTotalRows(0);

        exportTaskMapper.insert(task);
        return task;
    }

    /**
     * 查询导出数据
     */
    public List<Map<String, Object>> queryExportData(String boCode, String filterConditions,
                                                     List<String> fieldList) {
        BoDefinition bo = boDefinitionMapper.selectByBoCode(boCode);
        if (bo == null) {
            throw new RuntimeException("BO定义不存在: " + boCode);
        }

        List<BoField> fieldConfigs = boFieldMapper.selectByBoId(bo.getBoId());
        if (fieldConfigs.isEmpty()) {
            throw new RuntimeException("BO字段配置不存在: " + boCode);
        }

        Map<String, BoField> fieldMap = new HashMap<>();
        for (BoField field : fieldConfigs) {
            fieldMap.put(field.getFieldCode(), field);
        }

        List<String> selectColumns = new ArrayList<>();
        if (fieldList != null && !fieldList.isEmpty()) {
            for (String fieldCode : fieldList) {
                BoField field = fieldMap.get(fieldCode);
                if (field != null && field.getStatus() == 1) {
                    selectColumns.add(field.getTargetColumn() + " AS " + field.getFieldCode());
                }
            }
        } else {
            for (BoField field : fieldConfigs) {
                if (field.getStatus() == 1) {
                    selectColumns.add(field.getTargetColumn() + " AS " + field.getFieldCode());
                }
            }
        }

        if (selectColumns.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectColumns));
        sql.append(" FROM ").append(bo.getTargetTable());

        List<Object> params = new ArrayList<>();
        if (filterConditions != null && !filterConditions.trim().isEmpty()) {
            try {
                Map<String, Object> filters = objectMapper.readValue(filterConditions,
                        new TypeReference<Map<String, Object>>() {});
                if (!filters.isEmpty()) {
                    List<String> whereClauses = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : filters.entrySet()) {
                        BoField field = fieldMap.get(entry.getKey());
                        if (field != null) {
                            whereClauses.add(field.getTargetColumn() + " = ?");
                            params.add(entry.getValue());
                        }
                    }
                    if (!whereClauses.isEmpty()) {
                        sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
                    }
                }
            } catch (Exception e) {
                log.warn("解析过滤条件失败，忽略过滤: {}", e.getMessage());
            }
        }

        sql.append(" ORDER BY 1 DESC LIMIT 10000");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    /**
     * 导出为Excel，返回文件路径
     */
    public String exportToExcel(String boCode, List<Map<String, Object>> data) {
        BoDefinition bo = boDefinitionMapper.selectByBoCode(boCode);
        if (bo == null) {
            throw new RuntimeException("BO定义不存在: " + boCode);
        }

        List<BoField> fieldConfigs = boFieldMapper.selectByBoId(bo.getBoId());
        Map<String, BoField> fieldMap = new LinkedHashMap<>();
        for (BoField field : fieldConfigs) {
            if (field.getStatus() == 1) {
                fieldMap.put(field.getFieldCode(), field);
            }
        }

        List<List<String>> head = new ArrayList<>();
        List<String> fieldCodes = new ArrayList<>();

        if (!data.isEmpty()) {
            Set<String> dataKeys = data.get(0).keySet();
            for (String key : dataKeys) {
                BoField field = fieldMap.get(key);
                String headName = field != null ? field.getFieldName() : key;
                List<String> headColumn = new ArrayList<>();
                headColumn.add(headName);
                head.add(headColumn);
                fieldCodes.add(key);
            }
        } else {
            for (Map.Entry<String, BoField> entry : fieldMap.entrySet()) {
                List<String> headColumn = new ArrayList<>();
                headColumn.add(entry.getValue().getFieldName());
                head.add(headColumn);
                fieldCodes.add(entry.getKey());
            }
        }

        List<List<Object>> dataRows = new ArrayList<>();
        for (Map<String, Object> row : data) {
            List<Object> dataRow = new ArrayList<>();
            for (String fieldCode : fieldCodes) {
                Object value = row.get(fieldCode);
                dataRow.add(value != null ? value : "");
            }
            dataRows.add(dataRow);
        }

        String exportDir = System.getProperty("java.io.tmpdir") + "/export/";
        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = bo.getBoName() + "_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        String filePath = exportDir + fileName;

        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);

        HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(
                headWriteCellStyle, contentWriteCellStyle);

        EasyExcel.write(filePath)
                .head(head)
                .registerWriteHandler(styleStrategy)
                .sheet(bo.getBoName())
                .doWrite(dataRows);

        log.info("Excel导出完成: boCode={}, 行数={}, 文件={}", boCode, data.size(), filePath);
        return filePath;
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "EXP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", new Random().nextInt(10000));
    }
}
