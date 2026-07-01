package com.zrws.approval.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置
 * <p>对应 application.yml 中 alipay 前缀的配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 应用APPID */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 网关地址 */
    private String serverUrl = "https://openapi.alipay.com/gateway.do";

    /** 编码字符集 */
    private String charset = "UTF-8";

    /** 签名类型 */
    private String signType = "RSA2";

    /** 数据格式 */
    private String format = "JSON";

    /** 异步通知地址（外网可达的完整URL，需包含 context-path） */
    private String notifyUrl = "https://www.zrws.cloud/approval/api/v1/payment/alipay/notify";

    /**
     * 支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                serverUrl, appId, privateKey, format, charset, alipayPublicKey, signType);
    }
}
