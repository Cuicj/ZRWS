package com.zrws.approval.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zrws.approval.domain.dto.WeatherInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 天气服务 - 根据IP获取地理位置和天气
 * 使用免费API: ip-api.com (IP定位) + open-meteo.com (天气)
 */
@Slf4j
@Service
public class WeatherService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据IP获取地理位置和天气信息
     */
    public WeatherInfo getWeatherByIp(String ip) {
        try {
            // 1. 本地IP兜底
            if (ip == null || ip.isBlank() || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                log.info("本地IP，使用默认天气");
                return getDefaultWeather();
            }

            // 2. IP定位 (ip-api.com, 免费, 每分钟45次)
            String ipUrl = "http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=status,country,regionName,city,lat,lon";
            JsonNode ipResp = restTemplate.getForObject(ipUrl, JsonNode.class);

            if (ipResp == null || !"success".equals(ipResp.path("status").asText())) {
                log.warn("IP定位失败: ip={}", ip);
                return null;
            }

            double lat = ipResp.path("lat").asDouble();
            double lon = ipResp.path("lon").asDouble();
            String city = ipResp.path("city").asText();
            String province = ipResp.path("regionName").asText();

            log.info("IP定位成功: ip={}, province={}, city={}, lat={}, lon={}", ip, province, city, lat, lon);

            // 3. 获取天气 (open-meteo.com, 完全免费, 无需key)
            return getWeatherByCoords(lat, lon, city, province);

        } catch (Exception e) {
            log.warn("获取天气失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据经纬度获取天气
     */
    private WeatherInfo getWeatherByCoords(double lat, double lon, String city, String province) {
        try {
            String weatherUrl = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia/Shanghai",
                    lat, lon);

            JsonNode weatherResp = restTemplate.getForObject(weatherUrl, JsonNode.class);
            if (weatherResp == null || !weatherResp.has("current")) {
                log.warn("天气API返回异常");
                return null;
            }

            JsonNode current = weatherResp.path("current");
            int weatherCode = current.path("weather_code").asInt();

            WeatherInfo info = new WeatherInfo();
            info.setCity(city);
            info.setProvince(province);
            info.setTemperature(current.path("temperature_2m").asDouble());
            info.setHumidity(current.path("relative_humidity_2m").asInt());
            info.setWindSpeed(current.path("wind_speed_10m").asDouble());
            info.setWeatherCode(weatherCode);
            info.setWeather(weatherCodeToText(weatherCode));

            log.info("天气获取成功: city={}, weather={}, temp={}℃", city, info.getWeather(), info.getTemperature());
            return info;

        } catch (Exception e) {
            log.warn("获取天气数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * WMO天气代码转中文描述
     */
    private String weatherCodeToText(int code) {
        return switch (code) {
            case 0 -> "晴";
            case 1 -> "少云";
            case 2 -> "多云";
            case 3 -> "阴";
            case 45, 48 -> "雾";
            case 51, 53, 55 -> "毛毛雨";
            case 56, 57 -> "冻毛毛雨";
            case 61 -> "小雨";
            case 63 -> "中雨";
            case 65 -> "大雨";
            case 66, 67 -> "冻雨";
            case 71 -> "小雪";
            case 73 -> "中雪";
            case 75 -> "大雪";
            case 77 -> "霰";
            case 80 -> "小阵雨";
            case 81 -> "阵雨";
            case 82 -> "强阵雨";
            case 85, 86 -> "阵雪";
            case 95 -> "雷暴";
            case 96, 99 -> "雷暴伴冰雹";
            default -> "未知";
        };
    }

    /**
     * 默认天气（本地IP时使用）
     */
    private WeatherInfo getDefaultWeather() {
        return getWeatherByCoords(39.9042, 116.4074, "北京", "北京");
    }
}
