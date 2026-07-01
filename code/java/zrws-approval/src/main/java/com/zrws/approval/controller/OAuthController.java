package com.zrws.approval.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth 授权回调控制器
 * 处理支付宝/微信第三方授权跳转与回调，将授权码回传给前端注册页
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    /** 支付宝应用 appId（application.yml 占位符配置） */
    @Value("${zrws.oauth.alipay.app-id:}")
    private String alipayAppId;

    /** 支付宝应用 secret（占位符，后续换取 access_token 时使用） */
    @Value("${zrws.oauth.alipay.app-secret:}")
    private String alipayAppSecret;

    /** 支付宝授权回调地址 */
    @Value("${zrws.oauth.alipay.redirect-uri:https://www.zrws.cloud/approval/api/v1/oauth/alipay/callback}")
    private String alipayRedirectUri;

    /** 微信公众号 appId（application.yml 占位符配置） */
    @Value("${zrws.oauth.wechat.app-id:}")
    private String wechatAppId;

    /** 微信公众号 secret（占位符，后续换取 access_token 时使用） */
    @Value("${zrws.oauth.wechat.app-secret:}")
    private String wechatAppSecret;

    /** 微信授权回调地址 */
    @Value("${zrws.oauth.wechat.redirect-uri:https://www.zrws.cloud/approval/api/v1/oauth/wechat/callback}")
    private String wechatRedirectUri;

    /** 前端注册页地址 */
    @Value("${zrws.oauth.frontend-register-url:https://www.zrws.cloud/register.html}")
    private String frontendRegisterUrl;

    /**
     * 支付宝授权跳转
     * 生成支付宝授权 URL，重定向用户到支付宝授权页面
     */
    @GetMapping("/alipay/auth")
    public void alipayAuth(HttpServletResponse response) throws IOException {
        String redirectUri = URLEncoder.encode(alipayRedirectUri, StandardCharsets.UTF_8);
        String url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm"
                + "?app_id=" + alipayAppId
                + "&scope=auth_user"
                + "&redirect_uri=" + redirectUri;
        log.info("支付宝授权跳转: {}", url);
        response.sendRedirect(url);
    }

    /**
     * 支付宝授权回调
     * 接收 auth_code 参数，返回 HTML 页面将 auth_code 传给前端注册页
     */
    @GetMapping(value = "/alipay/callback", produces = MediaType.TEXT_HTML_VALUE)
    public String alipayCallback(@RequestParam(value = "auth_code", required = false) String authCode,
                                 @RequestParam(value = "state", required = false) String state) {
        log.info("支付宝授权回调: authCode={}", authCode);
        return buildCallbackHtml("alipay", authCode, state);
    }

    /**
     * 微信授权跳转
     * 生成微信授权 URL，重定向用户到微信授权页面
     */
    @GetMapping("/wechat/auth")
    public void wechatAuth(HttpServletResponse response) throws IOException {
        String redirectUri = URLEncoder.encode(wechatRedirectUri, StandardCharsets.UTF_8);
        String state = String.valueOf(System.currentTimeMillis());
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + wechatAppId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=snsapi_userinfo"
                + "&state=" + state
                + "#wechat_redirect";
        log.info("微信授权跳转: {}", url);
        response.sendRedirect(url);
    }

    /**
     * 微信授权回调
     * 接收 code 参数，返回 HTML 页面将 code 传给前端注册页
     */
    @GetMapping(value = "/wechat/callback", produces = MediaType.TEXT_HTML_VALUE)
    public String wechatCallback(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "state", required = false) String state) {
        log.info("微信授权回调: code={}", code);
        return buildCallbackHtml("wechat", code, state);
    }

    /**
     * 构建回调 HTML 页面
     * 通过 URL 参数将授权码传递给前端注册页（自动跳转）
     */
    private String buildCallbackHtml(String channel, String code, String state) {
        String codeKey = "alipay".equals(channel) ? "alipay_code" : "wechat_code";
        StringBuilder redirectUrl = new StringBuilder(frontendRegisterUrl);
        redirectUrl.append(frontendRegisterUrl.contains("?") ? "&" : "?");
        redirectUrl.append(codeKey).append("=");
        if (code != null && !code.isBlank()) {
            redirectUrl.append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        }
        if (state != null && !state.isBlank()) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        String label = "alipay".equals(channel) ? "支付宝" : "微信";
        return "<!DOCTYPE html>"
                + "<html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,viewport-fit=cover\">"
                + "<title>授权回调</title>"
                + "<style>"
                + "body{margin:0;font-family:-apple-system,BlinkMacSystemFont,\"PingFang SC\",\"Microsoft YaHei\",sans-serif;"
                + "background:linear-gradient(180deg,#C9A96E 0%,#B89A5D 28%,#F0EBE2 70%,#FEFBF6 100%);"
                + "min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;"
                + "padding-top:calc(24px + env(safe-area-inset-top));}"
                + ".cb-card{max-width:320px;background:#FAFAF8;border-radius:24px;padding:40px 28px;text-align:center;"
                + "box-shadow:0 12px 40px rgba(93,78,55,.18);}"
                + ".cb-spinner{display:inline-block;width:28px;height:28px;border:3px solid #E8E2D9;"
                + "border-top-color:#C9A96E;border-radius:50%;animation:cb-spin .8s linear infinite;margin-bottom:16px}"
                + ".cb-title{font-size:16px;font-weight:600;color:#5D4E37;margin-bottom:6px}"
                + ".cb-desc{font-size:13px;color:#8B7355;line-height:1.6}"
                + "@keyframes cb-spin{to{transform:rotate(360deg)}}"
                + "</style></head><body>"
                + "<div class=\"cb-card\">"
                + "<div class=\"cb-spinner\"></div>"
                + "<div class=\"cb-title\">" + label + "授权成功</div>"
                + "<div class=\"cb-desc\">正在跳转到注册页...</div>"
                + "</div>"
                + "<script>window.location.replace('" + redirectUrl + "');</script>"
                + "</body></html>";
    }
}
