package com.zrws.approval.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.config.AlipayConfig;
import com.zrws.approval.config.TenantContext;
import com.zrws.approval.domain.entity.Organization;
import com.zrws.approval.domain.entity.PaymentOrder;
import com.zrws.approval.domain.entity.Subscription;
import com.zrws.approval.mapper.OrganizationMapper;
import com.zrws.approval.mapper.PaymentOrderMapper;
import com.zrws.approval.mapper.SubscriptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付宝支付服务
 * <p>基于 alipay.trade.precreate（订单码支付）实现扫码支付、异步通知处理、订单状态查询
 */
@Slf4j
@Service
public class AlipayService {

    private static final DateTimeFormatter ALIPAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    /**
     * 创建支付订单
     *
     * @param userId   用户ID
     * @param orgId    组织ID
     * @param planCode 订阅计划编码（PRO/ENTERPRISE）
     * @return 支付订单（包含二维码链接）
     */
    public PaymentOrder createPaymentOrder(Long userId, Long orgId, String planCode) {
        // 1. 获取计划信息
        Map<String, Object> planInfo = getPlanInfo(planCode);
        if (planInfo == null) {
            throw new IllegalArgumentException("无效的订阅计划: " + planCode);
        }
        BigDecimal amount = (BigDecimal) planInfo.get("price");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("免费计划无需支付");
        }

        // 2. 查询组织获取tenantId
        Organization org = organizationMapper.selectById(orgId);
        if (org == null) {
            throw new IllegalArgumentException("组织不存在");
        }

        // 3. 生成订单号：ZRWS + 时间戳 + 随机数
        String orderNo = "ZRWS" + System.currentTimeMillis()
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // 4. 创建PaymentOrder记录
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(orderNo);
        order.setOrgId(orgId);
        order.setUserId(userId);
        order.setSubject("智壤卫士" + planInfo.get("name") + "月度订阅");
        order.setTotalAmount(amount);
        order.setPayChannel(PaymentOrder.PayChannel.ALIPAY.name());
        order.setPayStatus(PaymentOrder.PayStatus.PENDING.name());
        order.setBody(planCode); // 存储计划编码，用于异步通知后激活订阅
        order.setTenantId(org.getTenantId());
        paymentOrderMapper.insert(order);

        // 5. 调用 alipay.trade.precreate 生成二维码
        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(orderNo);
            model.setTotalAmount(amount.toPlainString());
            model.setSubject(order.getSubject());
            model.setProductCode("QR_CODE_OFFLINE");
            request.setBizModel(model);
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                order.setQrCode(response.getQrCode());
                paymentOrderMapper.updateById(order);
            } else {
                log.error("支付宝预下单失败: code={}, msg={}, subCode={}, subMsg={}",
                        response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());
                throw new RuntimeException("支付宝预下单失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝预下单接口异常: orderNo={}", orderNo, e);
            throw new RuntimeException("调用支付宝接口失败: " + e.getMessage());
        }

        return order;
    }

    /**
     * 处理支付宝异步通知
     *
     * @param params 支付宝通知参数
     * @return "success" 或 "fail"
     */
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(Map<String, String> params) {
        // 1. 验签（必须先验签，确保通知来自支付宝）
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSignType());
            if (!signVerified) {
                log.warn("支付宝异步通知验签失败");
                return "fail";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝异步通知验签异常", e);
            return "fail";
        }

        // 2. 校验交易状态
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.info("非交易成功状态，忽略: tradeStatus={}", tradeStatus);
            return "success";
        }

        // 3. 校验订单信息
        String outTradeNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        String tradeNo = params.get("trade_no");

        // 异步通知无租户上下文，使用忽略租户过滤的方法查询
        PaymentOrder order = paymentOrderMapper.selectByOrderNoIgnoreTenant(outTradeNo);
        if (order == null) {
            log.warn("订单不存在: outTradeNo={}", outTradeNo);
            return "fail";
        }

        // 幂等性检查：已支付的订单直接返回success
        if (PaymentOrder.PayStatus.PAID.name().equals(order.getPayStatus())) {
            log.info("订单已支付，跳过: outTradeNo={}", outTradeNo);
            return "success";
        }

        // 金额校验
        if (new BigDecimal(totalAmount).compareTo(order.getTotalAmount()) != 0) {
            log.warn("订单金额不匹配: outTradeNo={}, expected={}, actual={}",
                    outTradeNo, order.getTotalAmount(), totalAmount);
            return "fail";
        }

        // 4. 设置租户上下文（异步通知无JWT，需手动设置以支持多租户过滤）
        TenantContext.setTenantId(order.getTenantId());
        try {
            // 5. 更新PaymentOrder
            String gmtPayment = params.get("gmt_payment");
            order.setPayStatus(PaymentOrder.PayStatus.PAID.name());
            order.setTradeNo(tradeNo);
            order.setPayTime(parseAlipayTime(gmtPayment));
            order.setNotifyTime(LocalDateTime.now());
            paymentOrderMapper.updateById(order);

            // 6. 激活订阅
            activateSubscription(order);

            log.info("支付成功，订阅已激活: outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);
            return "success";
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderNo 商户订单号
     * @return 支付订单
     */
    public PaymentOrder queryOrderStatus(String orderNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderNo);
        }

        // 如果订单还是PENDING，主动查询支付宝（作为异步通知的兜底）
        if (PaymentOrder.PayStatus.PENDING.name().equals(order.getPayStatus())) {
            try {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(orderNo);
                request.setBizModel(model);
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                    order.setPayStatus(PaymentOrder.PayStatus.PAID.name());
                    order.setTradeNo(response.getTradeNo());
                    order.setPayTime(LocalDateTime.now());
                    order.setNotifyTime(LocalDateTime.now());
                    paymentOrderMapper.updateById(order);
                    activateSubscription(order);
                    log.info("主动查询发现订单已支付: orderNo={}", orderNo);
                }
            } catch (AlipayApiException e) {
                log.error("查询支付宝订单状态异常: orderNo={}", orderNo, e);
            }
        }

        return order;
    }

    /**
     * 获取订阅计划列表
     *
     * @return 计划列表
     */
    public List<Map<String, Object>> getPlanList() {
        List<Map<String, Object>> plans = new ArrayList<>();
        for (String code : new String[]{"FREE", "PRO", "ENTERPRISE"}) {
            Map<String, Object> info = getPlanInfo(code);
            if (info != null) {
                plans.add(info);
            }
        }
        return plans;
    }

    /**
     * 获取计划信息
     *
     * @param planCode 计划编码
     * @return 计划信息Map，无效编码返回null
     */
    private Map<String, Object> getPlanInfo(String planCode) {
        if (planCode == null) {
            return null;
        }
        Map<String, Object> info = new HashMap<>();
        switch (planCode) {
            case "FREE":
                info.put("code", "FREE");
                info.put("name", "免费版");
                info.put("price", new BigDecimal("0"));
                info.put("maxMembers", 3);
                info.put("description", "基础功能");
                info.put("features", List.of("基础数据管理", "3人团队"));
                break;
            case "PRO":
                info.put("code", "PRO");
                info.put("name", "专业版");
                info.put("price", new BigDecimal("299"));
                info.put("maxMembers", 10);
                info.put("description", "全功能");
                info.put("features", List.of("全功能", "10人团队", "数据导出", "AI分析"));
                break;
            case "ENTERPRISE":
                info.put("code", "ENTERPRISE");
                info.put("name", "企业版");
                info.put("price", new BigDecimal("999"));
                info.put("maxMembers", null); // 不限人数
                info.put("description", "全功能+OpenAPI");
                info.put("features", List.of("全功能", "不限人数", "OpenAPI", "专属客服"));
                break;
            default:
                return null;
        }
        return info;
    }

    /**
     * 激活订阅：创建Subscription记录，更新Organization的订阅级别和到期时间
     *
     * @param order 已支付的订单
     */
    private void activateSubscription(PaymentOrder order) {
        String planCode = order.getBody();
        Map<String, Object> planInfo = getPlanInfo(planCode);
        if (planInfo == null) {
            log.error("无法激活订阅，无效的计划编码: {}, orderNo={}", planCode, order.getOrderNo());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMonths(1);

        // 创建Subscription记录
        Subscription subscription = new Subscription();
        subscription.setOrgId(order.getOrgId());
        subscription.setPlanCode(planCode);
        subscription.setPlanName((String) planInfo.get("name"));
        subscription.setStartTime(now);
        subscription.setEndTime(endTime);
        subscription.setAmount(order.getTotalAmount());
        subscription.setStatus(Subscription.Status.ACTIVE.name());
        subscription.setAutoRenew(false);
        subscription.setPaymentOrderId(order.getId());
        subscription.setTenantId(order.getTenantId());
        subscriptionMapper.insert(subscription);

        // 更新Organization的订阅级别和到期时间
        Organization org = organizationMapper.selectById(order.getOrgId());
        if (org != null) {
            org.setSubscriptionLevel(planCode);
            org.setSubscriptionExpireTime(endTime);
            organizationMapper.updateById(org);
        }
    }

    /**
     * 解析支付宝时间格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param timeStr 时间字符串
     * @return LocalDateTime，解析失败返回当前时间
     */
    private LocalDateTime parseAlipayTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(timeStr, ALIPAY_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("解析支付宝时间失败: {}, 使用当前时间", timeStr);
            return LocalDateTime.now();
        }
    }
}
