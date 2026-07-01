package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.PaymentOrder;
import com.zrws.approval.domain.entity.SysUser;
import com.zrws.approval.mapper.SysUserMapper;
import com.zrws.approval.service.AlipayService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 * <p>所有API路径前会自动加上 context-path: /approval
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 创建支付订单
     *
     * @param planCode 订阅计划编码（PRO/ENTERPRISE）
     * @return 支付订单（包含二维码链接）
     */
    @PostMapping("/create")
    public R<PaymentOrder> create(@RequestParam String planCode) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return R.fail("未登录");
            }
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getCurrentOrgId() == null) {
                return R.fail("用户未关联组织");
            }
            PaymentOrder order = alipayService.createPaymentOrder(
                    userId, user.getCurrentOrgId(), planCode);
            return R.ok(order);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return R.fail("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝异步通知
     * <p>无需认证（已在SecurityConfig放行），支付宝服务器直接调用
     *
     * @param params 支付宝通知参数
     * @return "success" 或 "fail"
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        log.info("收到支付宝异步通知: outTradeNo={}", params.get("out_trade_no"));
        return alipayService.handleNotify(params);
    }

    /**
     * 查询订单状态
     *
     * @param orderNo 商户订单号
     * @return 支付订单
     */
    @GetMapping("/status/{orderNo}")
    public R<PaymentOrder> status(@PathVariable String orderNo) {
        try {
            PaymentOrder order = alipayService.queryOrderStatus(orderNo);
            return R.ok(order);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("查询订单状态失败: orderNo={}", orderNo, e);
            return R.fail("查询订单状态失败");
        }
    }

    /**
     * 获取订阅计划列表
     *
     * @return 计划列表
     */
    @GetMapping("/plans")
    public R<List<Map<String, Object>>> plans() {
        return R.ok(alipayService.getPlanList());
    }

    /**
     * 从SecurityContext获取当前用户ID
     *
     * @return 用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
