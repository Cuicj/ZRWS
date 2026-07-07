package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.LoginIpRule;
import com.zrws.approval.service.LoginIpRuleService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录IP限制规则管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/login-ip-rule")
@CrossOrigin(origins = "*")
public class LoginIpRuleController {

    @Autowired
    private LoginIpRuleService loginIpRuleService;

    /**
     * 分页列表
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<LoginIpRule> wrapper = new LambdaQueryWrapper<LoginIpRule>()
                .eq(LoginIpRule::getIsDeleted, 0)
                .orderByDesc(LoginIpRule::getCreatedTime);
        if (ruleType != null && !ruleType.isBlank()) {
            wrapper.eq(LoginIpRule::getRuleType, ruleType);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(LoginIpRule::getStatus, status);
        }
        Page<LoginIpRule> result = loginIpRuleService.page(new Page<>(page, size), wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", result.getCurrent());
        map.put("size", result.getSize());
        return R.ok(map);
    }

    /**
     * 所有启用的规则（用于前端展示当前生效规则）
     */
    @GetMapping("/active")
    public R<List<LoginIpRule>> activeRules() {
        List<LoginIpRule> list = loginIpRuleService.list(
                new LambdaQueryWrapper<LoginIpRule>()
                        .eq(LoginIpRule::getStatus, LoginIpRule.Status.ENABLED.name())
                        .eq(LoginIpRule::getIsDeleted, 0)
                        .orderByDesc(LoginIpRule::getCreatedTime)
        );
        return R.ok(list);
    }

    /**
     * 新增规则
     */
    @PostMapping
    public R<Long> create(@RequestBody LoginIpRule rule) {
        loginIpRuleService.save(rule);
        return R.ok(rule.getId());
    }

    /**
     * 修改规则
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody LoginIpRule rule) {
        rule.setId(id);
        loginIpRuleService.updateById(rule);
        return R.ok();
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        loginIpRuleService.removeById(id);
        return R.ok();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        loginIpRuleService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 切换状态
     */
    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam String status) {
        LoginIpRule rule = new LoginIpRule();
        rule.setId(id);
        rule.setStatus(status);
        loginIpRuleService.updateById(rule);
        return R.ok();
    }
}
