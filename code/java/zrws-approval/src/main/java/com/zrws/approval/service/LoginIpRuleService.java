package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrws.approval.domain.entity.LoginIpRule;
import com.zrws.approval.mapper.LoginIpRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 登录IP限制规则服务
 */
@Slf4j
@Service
public class LoginIpRuleService extends ServiceImpl<LoginIpRuleMapper, LoginIpRule> {

    /**
     * 检查IP是否允许登录
     * 规则：白名单优先。存在启用的白名单时，只有白名单中的IP允许登录；
     * 无白名单时，黑名单中的IP禁止登录。
     *
     * @param ip       客户端IP
     * @param tenantId 租户ID
     * @return true-允许登录, false-禁止登录
     */
    public boolean isIpAllowed(String ip, Long tenantId) {
        if (ip == null || ip.isBlank()) {
            return true;
        }

        List<LoginIpRule> rules = list(
                new LambdaQueryWrapper<LoginIpRule>()
                        .eq(LoginIpRule::getTenantId, tenantId)
                        .eq(LoginIpRule::getStatus, LoginIpRule.Status.ENABLED.name())
                        .eq(LoginIpRule::getIsDeleted, 0)
        );

        if (rules.isEmpty()) {
            return true;
        }

        // 分离白名单和黑名单
        List<LoginIpRule> whitelist = rules.stream()
                .filter(r -> LoginIpRule.RuleType.WHITELIST.name().equals(r.getRuleType()))
                .toList();
        List<LoginIpRule> blacklist = rules.stream()
                .filter(r -> LoginIpRule.RuleType.BLACKLIST.name().equals(r.getRuleType()))
                .toList();

        // 白名单优先：存在白名单时，只有白名单中的IP允许登录
        if (!whitelist.isEmpty()) {
            boolean inWhitelist = whitelist.stream()
                    .anyMatch(r -> ipMatches(ip, r.getIpAddress()));
            if (!inWhitelist) {
                log.warn("IP不在白名单中，禁止登录: ip={}, tenantId={}", ip, tenantId);
            }
            return inWhitelist;
        }

        // 无白名单时，检查黑名单
        boolean inBlacklist = blacklist.stream()
                .anyMatch(r -> ipMatches(ip, r.getIpAddress()));
        if (inBlacklist) {
            log.warn("IP在黑名单中，禁止登录: ip={}, tenantId={}", ip, tenantId);
        }
        return !inBlacklist;
    }

    /**
     * 判断IP是否匹配规则（支持精确匹配和CIDR）
     */
    private boolean ipMatches(String ip, String rule) {
        if (rule == null || rule.isBlank()) {
            return false;
        }
        if (rule.contains("/")) {
            return ipInCidr(ip, rule);
        }
        return rule.equals(ip);
    }

    /**
     * 判断IP是否在CIDR范围内
     */
    private boolean ipInCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            byte[] networkBytes = InetAddress.getByName(networkAddress).getAddress();

            // 只支持IPv4
            if (ipBytes.length != 4 || networkBytes.length != 4) {
                return false;
            }

            int mask = 0xFFFFFFFF << (32 - prefixLength);
            int ipInt = bytesToInt(ipBytes);
            int networkInt = bytesToInt(networkBytes);

            return (ipInt & mask) == (networkInt & mask);
        } catch (UnknownHostException | NumberFormatException e) {
            log.warn("CIDR解析失败: cidr={}, error={}", cidr, e.getMessage());
            return false;
        }
    }

    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}
