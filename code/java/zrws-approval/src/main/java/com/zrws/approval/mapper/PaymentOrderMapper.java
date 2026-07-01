package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 根据订单号查询（忽略租户过滤），用于支付宝异步通知等无租户上下文的场景
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM zrws_payment_order WHERE order_no = #{orderNo} AND is_deleted = 0 LIMIT 1")
    PaymentOrder selectByOrderNoIgnoreTenant(@Param("orderNo") String orderNo);
}
