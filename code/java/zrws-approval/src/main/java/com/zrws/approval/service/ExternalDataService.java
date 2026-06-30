package com.zrws.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 外部数据接入服务
 * <p>支持从第三方API拉取土壤数据，AI自动字段映射，数据格式自动转换
 */
@Slf4j
@Service
public class ExternalDataService {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 从第三方API拉取土壤数据（Mock实现）
     */
    public ExternalDataResult fetchSoilData(String source, Map<String, Object> params) {
        ExternalDataResult result = new ExternalDataResult();
        result.setSuccess(false);
        result.setSource(source);
        result.setFetchTime(LocalDateTime.now());

        try {
            List<Map<String, Object>> rawData = mockFetchExternalData(source, params);
            result.setRawData(rawData);
            result.setTotalCount(rawData.size());

            List<Map<String, Object>> mappedData = new ArrayList<>();
            for (Map<String, Object> row : rawData) {
                Map<String, Object> mapped = autoFieldMapping(row);
                Map<String, Object> converted = convertDataFormat(mapped);
                mappedData.add(converted);
            }

            result.setMappedData(mappedData);
            result.setSuccess(true);
            result.setMessage("数据拉取成功");

            log.info("从外部数据源 {} 拉取数据成功，共 {} 条", source, rawData.size());

        } catch (Exception e) {
            log.error("从外部数据源 {} 拉取数据失败", source, e);
            result.setSuccess(false);
            result.setMessage("数据拉取失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * AI自动字段映射
     * <p>将外部数据字段智能匹配到系统内部字段
     */
    public Map<String, Object> autoFieldMapping(Map<String, Object> externalData) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> fieldMapping = buildFieldMapping();

        for (Map.Entry<String, Object> entry : externalData.entrySet()) {
            String externalField = entry.getKey();
            Object value = entry.getValue();

            String internalField = findInternalField(externalField, fieldMapping);
            if (internalField != null) {
                result.put(internalField, value);
            } else {
                result.put(externalField, value);
            }
        }

        return result;
    }

    /**
     * 数据格式自动转换
     * <p>单位转换、编码转换等
     */
    public Map<String, Object> convertDataFormat(Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>(data);

        if (result.containsKey("phValue")) {
            Object ph = result.get("phValue");
            if (ph != null) {
                try {
                    double phVal = Double.parseDouble(ph.toString());
                    if (phVal > 14) {
                        phVal = phVal / 10.0;
                        result.put("phValue", Math.round(phVal * 100.0) / 100.0);
                        result.put("phValue_original", ph);
                        result.put("phValue_converted", true);
                    }
                } catch (Exception e) {
                    log.warn("pH值转换失败: {}", ph);
                }
            }
        }

        if (result.containsKey("organicMatter")) {
            Object om = result.get("organicMatter");
            if (om != null) {
                try {
                    double omVal = Double.parseDouble(om.toString());
                    if (omVal > 100) {
                        omVal = omVal / 10.0;
                        result.put("organicMatter", Math.round(omVal * 100.0) / 100.0);
                        result.put("organicMatter_original", om);
                        result.put("organicMatter_converted", true);
                    }
                } catch (Exception e) {
                    log.warn("有机质转换失败: {}", om);
                }
            }
        }

        if (result.containsKey("moisture")) {
            Object moisture = result.get("moisture");
            if (moisture != null) {
                try {
                    double moistureVal = Double.parseDouble(moisture.toString());
                    if (moistureVal > 0 && moistureVal < 1) {
                        moistureVal = moistureVal * 100;
                        result.put("moisture", Math.round(moistureVal * 100.0) / 100.0);
                        result.put("moisture_original", moisture);
                        result.put("moisture_converted", true);
                    }
                } catch (Exception e) {
                    log.warn("含水率转换失败: {}", moisture);
                }
            }
        }

        if (result.containsKey("collectTime")) {
            Object time = result.get("collectTime");
            if (time != null) {
                String timeStr = time.toString();
                try {
                    LocalDateTime dateTime = parseDateTime(timeStr);
                    result.put("collectTime", dateTime.toString());
                    result.put("collectTime_original", timeStr);
                    result.put("collectTime_converted", true);
                } catch (Exception e) {
                    log.warn("时间格式转换失败: {}", timeStr);
                }
            }
        }

        if (result.containsKey("latitude")) {
            Object lat = result.get("latitude");
            if (lat != null && lat instanceof String) {
                try {
                    String latStr = (String) lat;
                    if (latStr.contains("°") || latStr.contains("度")) {
                        double latVal = convertDegreeToDecimal(latStr);
                        result.put("latitude", Math.round(latVal * 1000000.0) / 1000000.0);
                        result.put("latitude_original", latStr);
                        result.put("latitude_converted", true);
                    }
                } catch (Exception e) {
                    log.warn("纬度转换失败: {}", lat);
                }
            }
        }

        if (result.containsKey("longitude")) {
            Object lon = result.get("longitude");
            if (lon != null && lon instanceof String) {
                try {
                    String lonStr = (String) lon;
                    if (lonStr.contains("°") || lonStr.contains("度")) {
                        double lonVal = convertDegreeToDecimal(lonStr);
                        result.put("longitude", Math.round(lonVal * 1000000.0) / 1000000.0);
                        result.put("longitude_original", lonStr);
                        result.put("longitude_converted", true);
                    }
                } catch (Exception e) {
                    log.warn("经度转换失败: {}", lon);
                }
            }
        }

        return result;
    }

    /**
     * 获取字段映射关系
     */
    public Map<String, String> getFieldMapping() {
        return buildFieldMapping();
    }

    /**
     * 测试第三方数据源连接
     */
    public boolean testConnection(String source) {
        try {
            mockFetchExternalData(source, new HashMap<>());
            return true;
        } catch (Exception e) {
            log.error("测试数据源连接失败: {}", source, e);
            return false;
        }
    }

    private List<Map<String, Object>> mockFetchExternalData(String source, Map<String, Object> params) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        Random random = new Random();
        int count = random.nextInt(20) + 5;

        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new LinkedHashMap<>();

            switch (source.toLowerCase()) {
                case "soil_lab_api":
                    row.put("sample_id", "SL" + String.format("%06d", random.nextInt(100000)));
                    row.put("ph_value", 5.5 + random.nextDouble() * 3);
                    row.put("organic_matter", 1.0 + random.nextDouble() * 5);
                    row.put("water_content", 15.0 + random.nextDouble() * 20);
                    row.put("soil_texture", getRandomTexture(random));
                    row.put("soil_color", getRandomColor(random));
                    row.put("total_n", 0.5 + random.nextDouble() * 2);
                    row.put("total_p", 0.3 + random.nextDouble() * 1);
                    row.put("total_k", 10.0 + random.nextDouble() * 20);
                    row.put("ec", 100 + random.nextInt(500));
                    row.put("lat", 30.0 + random.nextDouble() * 10);
                    row.put("lng", 110.0 + random.nextDouble() * 10);
                    row.put("elevation", 100 + random.nextInt(2000));
                    row.put("sampling_date", LocalDateTime.now().minusDays(random.nextInt(30)).toString());
                    row.put("collector_name", "技术员" + (char)('A' + random.nextInt(10)));
                    row.put("depth", (random.nextInt(10) + 1) * 10 + "cm");
                    break;

                case "agri_data_platform":
                    row.put("sampleCode", "AGRI-" + String.format("%05d", random.nextInt(10000)));
                    row.put("pH", 6.0 + random.nextDouble() * 2);
                    row.put("organicCarbon", 0.5 + random.nextDouble() * 3);
                    row.put("moisture", 0.15 + random.nextDouble() * 0.25);
                    row.put("textureType", getRandomTexture(random));
                    row.put("colorDesc", getRandomColor(random));
                    row.put("N_total", 0.8 + random.nextDouble() * 1.5);
                    row.put("P_total", 0.2 + random.nextDouble() * 0.8);
                    row.put("K_total", 15.0 + random.nextDouble() * 15);
                    row.put("conductivity", 150 + random.nextInt(400));
                    row.put("latitude", 28.0 + random.nextDouble() * 8);
                    row.put("longitude", 112.0 + random.nextDouble() * 8);
                    row.put("altitude", 50 + random.nextInt(1500));
                    row.put("collectDate", LocalDateTime.now().minusDays(random.nextInt(60)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    row.put("operator", "采样员" + (char)('甲' + random.nextInt(10)));
                    row.put("sampleDepth", (random.nextInt(5) + 1) * 20 + "厘米");
                    break;

                case "geo_monitor_system":
                    row.put("sid", "GEO-" + UUID.randomUUID().toString().substring(0, 8));
                    row.put("ph_val", 5.0 + random.nextDouble() * 4);
                    row.put("om_content", 0.5 + random.nextDouble() * 6);
                    row.put("water_rate", 10.0 + random.nextDouble() * 30);
                    row.put("texture", getRandomTexture(random));
                    row.put("soil_color", getRandomColor(random));
                    row.put("nitrogen", 0.3 + random.nextDouble() * 2.5);
                    row.put("phosphorus", 0.1 + random.nextDouble() * 1.2);
                    row.put("potassium", 8.0 + random.nextDouble() * 25);
                    row.put("ec_value", 80 + random.nextInt(600));
                    row.put("lat_deg", "3" + random.nextInt(5) + "°" + random.nextInt(60) + "'" + random.nextInt(60) + "\"N");
                    row.put("lon_deg", "1" + (random.nextInt(3) + 1) + "°" + random.nextInt(60) + "'" + random.nextInt(60) + "\"E");
                    row.put("height", 200 + random.nextInt(3000));
                    row.put("time", LocalDateTime.now().minusHours(random.nextInt(720)).toString());
                    row.put("person", "监测员" + random.nextInt(20));
                    row.put("depth_range", "0-" + (random.nextInt(10) + 5) * 10 + "cm");
                    break;

                default:
                    row.put("sampleCode", "EXT-" + String.format("%06d", random.nextInt(100000)));
                    row.put("phValue", 5.5 + random.nextDouble() * 3.5);
                    row.put("organicMatter", 1.0 + random.nextDouble() * 5);
                    row.put("moisture", 15.0 + random.nextDouble() * 25);
                    row.put("soilTexture", getRandomTexture(random));
                    row.put("color", getRandomColor(random));
                    row.put("nitrogen", 0.5 + random.nextDouble() * 2);
                    row.put("phosphorus", 0.2 + random.nextDouble() * 1);
                    row.put("potassium", 10.0 + random.nextDouble() * 20);
                    row.put("ecValue", 100 + random.nextInt(500));
                    row.put("latitude", 30.0 + random.nextDouble() * 10);
                    row.put("longitude", 110.0 + random.nextDouble() * 10);
                    row.put("elevation", 100 + random.nextInt(2000));
                    row.put("collectTime", LocalDateTime.now().minusDays(random.nextInt(30)).toString());
                    row.put("collector", "外部采集" + random.nextInt(10));
                    row.put("depth", (random.nextInt(10) + 1) * 10 + "cm");
            }

            dataList.add(row);
        }

        return dataList;
    }

    private Map<String, String> buildFieldMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();

        mapping.put("sample_id", "sampleCode");
        mapping.put("sampleCode", "sampleCode");
        mapping.put("sample code", "sampleCode");
        mapping.put("sid", "sampleCode");
        mapping.put("样品编号", "sampleCode");
        mapping.put("样本编号", "sampleCode");

        mapping.put("ph_value", "phValue");
        mapping.put("phValue", "phValue");
        mapping.put("ph_val", "phValue");
        mapping.put("pH", "phValue");
        mapping.put("ph", "phValue");
        mapping.put("PH", "phValue");
        mapping.put("酸碱度", "phValue");
        mapping.put("pH值", "phValue");

        mapping.put("organic_matter", "organicMatter");
        mapping.put("organicMatter", "organicMatter");
        mapping.put("organic_carbon", "organicMatter");
        mapping.put("organicCarbon", "organicMatter");
        mapping.put("om_content", "organicMatter");
        mapping.put("有机质", "organicMatter");
        mapping.put("有机碳", "organicMatter");
        mapping.put("OM", "organicMatter");

        mapping.put("water_content", "moisture");
        mapping.put("waterContent", "moisture");
        mapping.put("moisture", "moisture");
        mapping.put("water_rate", "moisture");
        mapping.put("含水率", "moisture");
        mapping.put("水分", "moisture");
        mapping.put("含水量", "moisture");

        mapping.put("soil_texture", "soilTexture");
        mapping.put("soilTexture", "soilTexture");
        mapping.put("textureType", "soilTexture");
        mapping.put("texture", "soilTexture");
        mapping.put("质地", "soilTexture");
        mapping.put("土壤质地", "soilTexture");

        mapping.put("soil_color", "color");
        mapping.put("soilColor", "color");
        mapping.put("colorDesc", "color");
        mapping.put("color", "color");
        mapping.put("颜色", "color");
        mapping.put("土壤颜色", "color");

        mapping.put("total_n", "nitrogen");
        mapping.put("totalN", "nitrogen");
        mapping.put("N_total", "nitrogen");
        mapping.put("nitrogen", "nitrogen");
        mapping.put("全氮", "nitrogen");
        mapping.put("总氮", "nitrogen");
        mapping.put("N", "nitrogen");

        mapping.put("total_p", "phosphorus");
        mapping.put("totalP", "phosphorus");
        mapping.put("P_total", "phosphorus");
        mapping.put("phosphorus", "phosphorus");
        mapping.put("全磷", "phosphorus");
        mapping.put("总磷", "phosphorus");
        mapping.put("P", "phosphorus");

        mapping.put("total_k", "potassium");
        mapping.put("totalK", "potassium");
        mapping.put("K_total", "potassium");
        mapping.put("potassium", "potassium");
        mapping.put("全钾", "potassium");
        mapping.put("总钾", "potassium");
        mapping.put("K", "potassium");

        mapping.put("ec", "ecValue");
        mapping.put("ecValue", "ecValue");
        mapping.put("ec_value", "ecValue");
        mapping.put("conductivity", "ecValue");
        mapping.put("电导率", "ecValue");
        mapping.put("EC值", "ecValue");

        mapping.put("lat", "latitude");
        mapping.put("latitude", "latitude");
        mapping.put("lat_deg", "latitude");
        mapping.put("纬度", "latitude");

        mapping.put("lng", "longitude");
        mapping.put("lon", "longitude");
        mapping.put("longitude", "longitude");
        mapping.put("lon_deg", "longitude");
        mapping.put("经度", "longitude");

        mapping.put("elevation", "elevation");
        mapping.put("altitude", "elevation");
        mapping.put("height", "elevation");
        mapping.put("海拔", "elevation");
        mapping.put("高程", "elevation");

        mapping.put("sampling_date", "collectTime");
        mapping.put("collectDate", "collectTime");
        mapping.put("collect_time", "collectTime");
        mapping.put("collectTime", "collectTime");
        mapping.put("time", "collectTime");
        mapping.put("采集时间", "collectTime");
        mapping.put("采样时间", "collectTime");

        mapping.put("collector_name", "collector");
        mapping.put("collectorName", "collector");
        mapping.put("operator", "collector");
        mapping.put("person", "collector");
        mapping.put("collector", "collector");
        mapping.put("采集人", "collector");
        mapping.put("采样人", "collector");

        mapping.put("sample_depth", "depth");
        mapping.put("sampleDepth", "depth");
        mapping.put("depth_range", "depth");
        mapping.put("depth", "depth");
        mapping.put("深度", "depth");
        mapping.put("采样深度", "depth");

        return mapping;
    }

    private String findInternalField(String externalField, Map<String, String> fieldMapping) {
        if (fieldMapping.containsKey(externalField)) {
            return fieldMapping.get(externalField);
        }

        String externalLower = externalField.toLowerCase().replaceAll("[\\s_-]", "");
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String keyLower = entry.getKey().toLowerCase().replaceAll("[\\s_-]", "");
            if (keyLower.equals(externalLower) ||
                keyLower.contains(externalLower) ||
                externalLower.contains(keyLower)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                // continue to next format
            }
        }

        return LocalDateTime.now();
    }

    private double convertDegreeToDecimal(String degreeStr) {
        degreeStr = degreeStr.replaceAll("[°度]", "°")
                .replaceAll("[′']", "'")
                .replaceAll("[″\"]", "\"");

        double degrees = 0;
        double minutes = 0;
        double seconds = 0;
        String direction = "";

        try {
            String[] parts = degreeStr.split("[°'\"]");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                degrees = Double.parseDouble(parts[0].trim());
            }
            if (parts.length > 1 && !parts[1].isEmpty()) {
                minutes = Double.parseDouble(parts[1].trim());
            }
            if (parts.length > 2 && !parts[2].isEmpty()) {
                seconds = Double.parseDouble(parts[2].trim());
            }
            if (degreeStr.contains("S") || degreeStr.contains("W") ||
                degreeStr.contains("南") || degreeStr.contains("西")) {
                direction = "negative";
            }
        } catch (Exception e) {
            log.warn("度分秒转换失败: {}", degreeStr);
        }

        double decimal = degrees + minutes / 60 + seconds / 3600;
        if ("negative".equals(direction)) {
            decimal = -decimal;
        }

        return decimal;
    }

    private String getRandomTexture(Random random) {
        String[] textures = {"壤土", "粘土", "砂土", "粉土", "砂壤土", "粘壤土", "粉壤土"};
        return textures[random.nextInt(textures.length)];
    }

    private String getRandomColor(Random random) {
        String[] colors = {"棕色", "黄褐色", "暗棕色", "红棕色", "黄色", "灰棕色", "黑色"};
        return colors[random.nextInt(colors.length)];
    }

    @Data
    public static class ExternalDataResult {
        private Boolean success;
        private String source;
        private String message;
        private LocalDateTime fetchTime;
        private Integer totalCount;
        private List<Map<String, Object>> rawData;
        private List<Map<String, Object>> mappedData;
    }
}
