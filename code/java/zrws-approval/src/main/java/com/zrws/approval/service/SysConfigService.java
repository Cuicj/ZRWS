package com.zrws.approval.service;

import com.zrws.approval.domain.entity.SysConfig;
import com.zrws.approval.mapper.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局配置服务
 */
@Slf4j
@Service
public class SysConfigService {

    @Autowired
    private SysConfigMapper configMapper;

    /**
     * 获取所有配置
     */
    public List<SysConfig> getAllConfigs() {
        return configMapper.selectActiveConfigs();
    }

    /**
     * 获取配置组
     */
    public List<SysConfig> getByGroup(String group) {
        return configMapper.selectByGroup(group);
    }

    /**
     * 获取配置值
     */
    public String getValue(String key) {
        SysConfig config = configMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置值(带默认值)
     */
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔配置
     */
    public Boolean getBooleanValue(String key) {
        return "true".equalsIgnoreCase(getValue(key));
    }

    /**
     * 获取整数配置
     */
    public Integer getIntValue(String key) {
        String value = getValue(key);
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取全局设置
     */
    public Map<String, Object> getGlobalSettings() {
        Map<String, Object> settings = new HashMap<>();
        List<SysConfig> configs = configMapper.selectByGroup("GLOBAL");
        for (SysConfig config : configs) {
            switch (config.getConfigType()) {
                case "INT":
                    settings.put(config.getConfigKey(), config.getIntValue());
                    break;
                case "BOOLEAN":
                    settings.put(config.getConfigKey(), config.getBooleanValue());
                    break;
                default:
                    settings.put(config.getConfigKey(), config.getConfigValue());
            }
        }
        return settings;
    }

    /**
     * 更新配置
     */
    public boolean updateConfig(String key, String value) {
        return configMapper.updateValue(key, value) > 0;
    }

    /**
     * 保存配置
     */
    public void saveConfig(SysConfig config) {
        if (config.getConfigId() != null) {
            configMapper.updateById(config);
        } else {
            configMapper.insert(config);
        }
    }

    /**
     * 获取禁用词列表
     */
    public List<String> getDisableWords() {
        String words = getValue("DISABLE_WORDS", "");
        if (words.isEmpty()) {
            return List.of();
        }
        return List.of(words.split(","));
    }

    /**
     * 检查是否包含禁用词
     */
    public boolean containsDisableWord(String content) {
        List<String> words = getDisableWords();
        for (String word : words) {
            if (content.contains(word.trim())) {
                return true;
            }
        }
        return false;
    }
}