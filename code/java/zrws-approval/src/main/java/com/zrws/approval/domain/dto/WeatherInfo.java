package com.zrws.approval.domain.dto;

import lombok.Data;

/**
 * 天气信息 DTO
 */
@Data
public class WeatherInfo {

    /** 城市 */
    private String city;

    /** 省份 */
    private String province;

    /** 天气描述 */
    private String weather;

    /** 温度(℃) */
    private Double temperature;

    /** 湿度(%) */
    private Integer humidity;

    /** 风速(km/h) */
    private Double windSpeed;

    /** 天气图标代码(WMO标准) */
    private Integer weatherCode;
}
