package com.zrws.approval.service;

import com.zrws.approval.domain.entity.ApprovalFlowConfig;
import com.zrws.approval.mapper.ApprovalFlowConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审批流配置服务
 */
@Slf4j
@Service
public class ApprovalFlowConfigService {

    @Autowired
    private ApprovalFlowConfigMapper configMapper;

    /**
     * 获取所有审批流配置
     */
    public List<ApprovalFlowConfig> getAllConfigs() {
        return configMapper.selectActiveConfigs();
    }

    /**
     * 根据BO编码获取配置
     */
    public List<ApprovalFlowConfig> getConfigsByBoCode(String boCode) {
        return configMapper.selectByBoCode(boCode);
    }

    /**
     * 获取BO操作的审批配置
     */
    public ApprovalFlowConfig getConfig(String boCode, String operationType) {
        return configMapper.selectByBoAndOperation(boCode, operationType);
    }

    /**
     * 检查操作是否需要审批
     */
    public boolean needApproval(String boCode, String operationType) {
        ApprovalFlowConfig config = getConfig(boCode, operationType);
        return config != null && config.getEnableApproval() == 1 && config.getStatus() == 1;
    }

    /**
     * 获取审批流程Key
     */
    public String getApprovalProcessKey(String boCode, String operationType) {
        ApprovalFlowConfig config = getConfig(boCode, operationType);
        return config != null ? config.getProcessKey() : null;
    }

    /**
     * 保存配置
     */
    public ApprovalFlowConfig saveConfig(ApprovalFlowConfig config) {
        if (config.getConfigId() != null) {
            configMapper.updateById(config);
        } else {
            configMapper.insert(config);
        }
        return config;
    }

    /**
     * 更新配置
     */
    public void updateConfig(Long configId, ApprovalFlowConfig config) {
        config.setConfigId(configId);
        configMapper.updateById(config);
    }

    /**
     * 删除配置
     */
    public void deleteConfig(Long configId) {
        configMapper.deleteById(configId);
    }

    /**
     * 批量保存配置
     */
    public void saveConfigs(List<ApprovalFlowConfig> configs) {
        for (ApprovalFlowConfig config : configs) {
            saveConfig(config);
        }
    }
}