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
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.mapper.BoDefinitionMapper;
import com.zrws.approval.mapper.BoFieldMapper;
import com.zrws.approval.mapper.ExportTaskMapper;
import com.zrws.approval.mapper.SoilSampleMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private SoilSampleMapper soilSampleMapper;

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
     * 土壤专用报告导出
     * <p>带统计图表的Excel导出
     */
    public String exportSoilReport(List<SoilSample> samples, String reportTitle) {
        String exportDir = System.getProperty("java.io.tmpdir") + "/export/";
        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "土壤检测报告_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        String filePath = exportDir + fileName;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createSoilDataSheet(workbook, samples);
            createSoilStatisticsSheet(workbook, samples);
            createSoilChartsSheet(workbook, samples);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            log.info("土壤报告导出完成: 样本数={}, 文件={}", samples.size(), filePath);
            return filePath;

        } catch (Exception e) {
            log.error("土壤报告导出失败", e);
            throw new RuntimeException("土壤报告导出失败: " + e.getMessage(), e);
        }
    }

    private void createSoilDataSheet(XSSFWorkbook workbook, List<SoilSample> samples) {
        XSSFSheet sheet = workbook.createSheet("样本数据");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        String[] headers = {"样品编号", "采样位置", "纬度", "经度", "海拔(m)",
                "pH值", "有机质(%)", "含水率(%)", "土壤质地", "土壤类型",
                "全氮(g/kg)", "全磷(g/kg)", "全钾(g/kg)", "电导率(μS/cm)",
                "采样深度", "采集人", "采集时间"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        int rowNum = 1;
        for (SoilSample sample : samples) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;

            createCell(row, colNum++, sample.getSampleCode(), dataStyle);
            createCell(row, colNum++, "", dataStyle);
            createCell(row, colNum++, sample.getLatitude(), dataStyle);
            createCell(row, colNum++, sample.getLongitude(), dataStyle);
            createCell(row, colNum++, sample.getElevation(), dataStyle);
            createCell(row, colNum++, sample.getPhValue(), dataStyle);
            createCell(row, colNum++, sample.getOrganicMatter(), dataStyle);
            createCell(row, colNum++, sample.getMoisture(), dataStyle);
            createCell(row, colNum++, sample.getSoilTexture(), dataStyle);
            createCell(row, colNum++, sample.getSoilType(), dataStyle);
            createCell(row, colNum++, sample.getNitrogen(), dataStyle);
            createCell(row, colNum++, sample.getPhosphorus(), dataStyle);
            createCell(row, colNum++, sample.getPotassium(), dataStyle);
            createCell(row, colNum++, sample.getEcValue(), dataStyle);
            createCell(row, colNum++, sample.getDepth(), dataStyle);
            createCell(row, colNum++, sample.getCollector(), dataStyle);
            createCell(row, colNum++, sample.getCollectTime() != null ? sample.getCollectTime().toString() : "", dataStyle);
        }
    }

    private void createSoilStatisticsSheet(XSSFWorkbook workbook, List<SoilSample> samples) {
        XSSFSheet sheet = workbook.createSheet("统计分析");

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("土壤检测数据统计分析");
        titleCell.setCellStyle(titleStyle);

        Row summaryRow = sheet.createRow(2);
        summaryRow.createCell(0).setCellValue("样本总数");
        summaryRow.createCell(1).setCellValue(samples.size());

        String[] statHeaders = {"指标", "最小值", "最大值", "平均值", "标准差", "中位数"};
        Row statHeaderRow = sheet.createRow(4);
        for (int i = 0; i < statHeaders.length; i++) {
            Cell cell = statHeaderRow.createCell(i);
            cell.setCellValue(statHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        String[] indicators = {"pH值", "有机质(%)", "含水率(%)", "全氮(g/kg)", "全磷(g/kg)", "全钾(g/kg)"};
        String[] fields = {"phValue", "organicMatter", "moisture", "nitrogen", "phosphorus", "potassium"};

        for (int i = 0; i < indicators.length; i++) {
            Row row = sheet.createRow(5 + i);
            List<Double> values = extractValues(samples, fields[i]);

            createCell(row, 0, indicators[i], dataStyle);
            createCell(row, 1, getMinValue(values), dataStyle);
            createCell(row, 2, getMaxValue(values), dataStyle);
            createCell(row, 3, getAvgValue(values), dataStyle);
            createCell(row, 4, getStdDev(values), dataStyle);
            createCell(row, 5, getMedianValue(values), dataStyle);
        }

        int textureStartRow = 13;
        Row textureTitleRow = sheet.createRow(textureStartRow);
        textureTitleRow.createCell(0).setCellValue("土壤质地分布");
        textureTitleRow.getCell(0).setCellStyle(titleStyle);

        Row textureHeaderRow = sheet.createRow(textureStartRow + 1);
        createCell(textureHeaderRow, 0, "质地类型", headerStyle);
        createCell(textureHeaderRow, 1, "样本数量", headerStyle);
        createCell(textureHeaderRow, 2, "占比(%)", headerStyle);

        Map<String, Long> textureCount = samples.stream()
                .filter(s -> s.getSoilTexture() != null && !s.getSoilTexture().isEmpty())
                .collect(Collectors.groupingBy(SoilSample::getSoilTexture, Collectors.counting()));

        int textureRow = textureStartRow + 2;
        long total = textureCount.values().stream().mapToLong(Long::longValue).sum();
        for (Map.Entry<String, Long> entry : textureCount.entrySet()) {
            Row row = sheet.createRow(textureRow++);
            createCell(row, 0, entry.getKey(), dataStyle);
            createCell(row, 1, entry.getValue().intValue(), dataStyle);
            createCell(row, 2, Math.round(entry.getValue() * 100.0 / total * 100.0) / 100.0, dataStyle);
        }
    }

    private void createSoilChartsSheet(XSSFWorkbook workbook, List<SoilSample> samples) {
        XSSFSheet sheet = workbook.createSheet("图表分析");

        int dataStartRow = 0;
        Row chartTitleRow = sheet.createRow(dataStartRow);
        chartTitleRow.createCell(0).setCellValue("土壤各指标对比图表");

        int phDataStart = dataStartRow + 2;
        Row phHeader = sheet.createRow(phDataStart);
        phHeader.createCell(0).setCellValue("样品编号");
        phHeader.createCell(1).setCellValue("pH值");
        phHeader.createCell(2).setCellValue("有机质(%)");

        for (int i = 0; i < Math.min(samples.size(), 20); i++) {
            SoilSample sample = samples.get(i);
            Row row = sheet.createRow(phDataStart + 1 + i);
            row.createCell(0).setCellValue(sample.getSampleCode() != null ? sample.getSampleCode() : "样本" + (i + 1));
            row.createCell(1).setCellValue(sample.getPhValue() != null ? sample.getPhValue() : 0);
            row.createCell(2).setCellValue(sample.getOrganicMatter() != null ? sample.getOrganicMatter() : 0);
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 1, 15, 15);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("土壤pH值与有机质对比");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("样品");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("数值");

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(phDataStart + 1, phDataStart + Math.min(samples.size(), 20), 0, 0));

        XDDFNumericalDataSource<Double> phValues = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(phDataStart + 1, phDataStart + Math.min(samples.size(), 20), 1, 1));

        XDDFNumericalDataSource<Double> omValues = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(phDataStart + 1, phDataStart + Math.min(samples.size(), 20), 2, 2));

        XDDFLineChartData lineData = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        XDDFLineChartData.Series phSeries = (XDDFLineChartData.Series) lineData.addSeries(categories, phValues);
        phSeries.setTitle("pH值", null);
        phSeries.setSmooth(true);

        XDDFLineChartData.Series omSeries = (XDDFLineChartData.Series) lineData.addSeries(categories, omValues);
        omSeries.setTitle("有机质(%)", null);
        omSeries.setSmooth(true);

        chart.plot(lineData);

        int textureDataStart = 25;
        Row textureChartHeader = sheet.createRow(textureDataStart);
        textureChartHeader.createCell(0).setCellValue("质地类型");
        textureChartHeader.createCell(1).setCellValue("数量");

        Map<String, Long> textureCount = samples.stream()
                .filter(s -> s.getSoilTexture() != null && !s.getSoilTexture().isEmpty())
                .collect(Collectors.groupingBy(SoilSample::getSoilTexture, Collectors.counting()));

        int textureRow = textureDataStart + 1;
        for (Map.Entry<String, Long> entry : textureCount.entrySet()) {
            Row row = sheet.createRow(textureRow++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }

        XSSFDrawing pieDrawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor pieAnchor = pieDrawing.createAnchor(0, 0, 0, 0, 5, 20, 15, 35);

        XSSFChart pieChart = pieDrawing.createChart(pieAnchor);
        pieChart.setTitleText("土壤质地分布");
        pieChart.setTitleOverlay(false);

        XDDFChartLegend pieLegend = pieChart.getOrAddLegend();
        pieLegend.setPosition(LegendPosition.RIGHT);

        XDDFCategoryDataSource pieCategories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(textureDataStart + 1, textureRow - 1, 0, 0));

        XDDFNumericalDataSource<Double> pieValues = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(textureDataStart + 1, textureRow - 1, 1, 1));

        XDDFPieChartData pieData = (XDDFPieChartData) pieChart.createData(ChartTypes.PIE, null, null);
        pieData.setVaryColors(true);
        pieData.addSeries(pieCategories, pieValues);

        pieChart.plot(pieData);
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private List<Double> extractValues(List<SoilSample> samples, String field) {
        List<Double> values = new ArrayList<>();
        for (SoilSample sample : samples) {
            Double value = null;
            switch (field) {
                case "phValue":
                    value = sample.getPhValue();
                    break;
                case "organicMatter":
                    value = sample.getOrganicMatter();
                    break;
                case "moisture":
                    value = sample.getMoisture();
                    break;
                case "nitrogen":
                    value = sample.getNitrogen();
                    break;
                case "phosphorus":
                    value = sample.getPhosphorus();
                    break;
                case "potassium":
                    value = sample.getPotassium();
                    break;
            }
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private Double getMinValue(List<Double> values) {
        if (values.isEmpty()) return null;
        return values.stream().min(Double::compareTo).orElse(0.0);
    }

    private Double getMaxValue(List<Double> values) {
        if (values.isEmpty()) return null;
        return values.stream().max(Double::compareTo).orElse(0.0);
    }

    private Double getAvgValue(List<Double> values) {
        if (values.isEmpty()) return null;
        return Math.round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 100.0) / 100.0;
    }

    private Double getStdDev(List<Double> values) {
        if (values.size() < 2) return null;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
        return Math.round(Math.sqrt(variance) * 100.0) / 100.0;
    }

    private Double getMedianValue(List<Double> values) {
        if (values.isEmpty()) return null;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return Math.round((sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0 * 100.0) / 100.0;
        } else {
            return Math.round(sorted.get(size / 2) * 100.0) / 100.0;
        }
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "EXP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", new Random().nextInt(10000));
    }
}
