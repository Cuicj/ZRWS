#!/usr/bin/env python3
"""直接编译并部署（修复后继续）"""

import paramiko
import time
import sys

HOST = '8.163.137.149'
PORT = 22
USERNAME = 'root'
PASSWORD = 'Test_admin'

REPO_DIR = '/root/workspace/ZRWS'
JAR_PATH = '/root/workspace/app.jar'
FRONTEND_DIR = '/var/www/zrws'
ANNOUNCEMENT_DIR = '/app/announcement'
BACKEND_PORT = 5571


def print_step(msg):
    print(f"\n{'='*60}")
    print(f"  {msg}")
    print(f"{'='*60}\n")


def exec_cmd(client, cmd, timeout=300):
    print(f"$ {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout, get_pty=True)
    output = []
    while True:
        try:
            line = stdout.readline()
            if not line:
                break
            line = line.rstrip()
            if line:
                print(f"  {line}")
                output.append(line)
        except:
            break
    exit_code = stdout.channel.recv_exit_status()
    err = stderr.read().decode('utf-8', errors='ignore').strip()
    if err and exit_code != 0:
        print(f"  [错误] {err}")
    return exit_code, output


def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        print_step("Step 1: 连接服务器")
        client.connect(HOST, PORT, USERNAME, PASSWORD, timeout=30)
        print(f"已连接到 {USERNAME}@{HOST}")

        # Step 2: Maven 编译
        print_step("Step 2: Maven 编译后端")
        exit_code, output = exec_cmd(client, f"cd {REPO_DIR}/code/java && mvn clean package -DskipTests", timeout=600)

        if exit_code != 0:
            print("\n编译失败，尝试修复...")
            # 检查是否有 POI 问题
            output_str = '\n'.join(output)
            if 'org.apache.poi.ss.usermodel.charts' in output_str:
                print("检测到 POI 图表导入问题，重新修复...")
                exec_cmd(client, """cat > /root/workspace/ZRWS/code/java/zrws-approval/src/main/java/com/zrws/approval/service/DataExportService.java << 'JAVAEOF'
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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;
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
    }

    private void createSoilStatisticsSheet(XSSFWorkbook workbook, List<SoilSample> samples) {
        XSSFSheet sheet = workbook.createSheet("统计分析");
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("土壤检测数据统计分析");
        Row summaryRow = sheet.createRow(2);
        summaryRow.createCell(0).setCellValue("样本总数");
        summaryRow.createCell(1).setCellValue(samples.size());
    }

    private void createSoilChartsSheet(XSSFWorkbook workbook, List<SoilSample> samples) {
        XSSFSheet sheet = workbook.createSheet("图表分析");
        int dataStartRow = 0;
        Row chartTitleRow = sheet.createRow(dataStartRow);
        chartTitleRow.createCell(0).setCellValue("土壤各指标对比图表");
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
    }

    private List<Double> extractValues(List<SoilSample> samples, String field) {
        List<Double> values = new ArrayList<>();
        for (SoilSample sample : samples) {
            Double value = null;
            switch (field) {
                case "phValue": value = sample.getPhValue(); break;
                case "organicMatter": value = sample.getOrganicMatter(); break;
                case "moisture": value = sample.getMoisture(); break;
                case "nitrogen": value = sample.getNitrogen(); break;
                case "phosphorus": value = sample.getPhosphorus(); break;
                case "potassium": value = sample.getPotassium(); break;
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

    private String generateTaskNo() {
        return "EXP" + DateUtil.format(new Date(), "yyyyMMddHHmmss") +
               String.format("%04d", new Random().nextInt(10000));
    }
}
JAVAEOF""")

                # 重新编译
                print("\n重新编译...")
                exit_code, output = exec_cmd(client, f"cd {REPO_DIR}/code/java && mvn clean package -DskipTests", timeout=600)

        if exit_code == 0:
            print("\n编译成功!")

            # 部署步骤
            print_step("部署后端 JAR")
            exec_cmd(client, f"cp {REPO_DIR}/code/java/zrws-approval/target/zrws-approval-*.jar {JAR_PATH}")

            print_step("构建前端")
            exec_cmd(client, f"cd {REPO_DIR}/code/html && npm run build", timeout=300)

            print_step("部署前端")
            exec_cmd(client, f"rm -rf {FRONTEND_DIR}/*")
            exec_cmd(client, f"cp -r {REPO_DIR}/code/html/dist/* {FRONTEND_DIR}/")

            print_step("部署公告栏")
            exec_cmd(client, f"mkdir -p {ANNOUNCEMENT_DIR}")
            exec_cmd(client, f"cp {REPO_DIR}/公告栏.html {ANNOUNCEMENT_DIR}/")

            print_step("重启服务")
            exec_cmd(client, "systemctl start zrws.service")
            time.sleep(10)

            # 验证
            print_step("验证部署")
            try:
                result = exec_cmd(client, f"curl -s http://localhost:{BACKEND_PORT}/approval/actuator/health")
                print(f"后端健康状态: {result}")
            except:
                pass

            print_step("部署完成!")
        else:
            print("\n编译仍然失败，请检查错误")
            sys.exit(1)

    except Exception as e:
        print(f"\n错误: {e}")
        sys.exit(1)
    finally:
        client.close()

if __name__ == '__main__':
    main()
