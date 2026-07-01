package com.zrws.approval.controller;

import com.zrws.approval.domain.dto.LoginRequest;
import com.zrws.approval.domain.dto.LoginResponse;
import com.zrws.approval.domain.dto.RegisterRequest;
import com.zrws.approval.service.AuthService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return R.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("登录失败", e);
            return R.fail("登录失败");
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return R.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return R.fail("注册失败");
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public R<String> sendVerifyCode(@RequestParam String phone) {
        try {
            String code = authService.sendVerifyCode(phone);
            return R.ok(code);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return R.fail("发送验证码失败");
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            String token = extractToken(authorization);
            if (token == null) {
                return R.fail("未提供有效的token");
            }
            authService.logout(token);
            return R.ok();
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return R.fail("退出登录失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<LoginResponse> info() {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return R.fail("未登录");
            }
            LoginResponse response = authService.getCurrentUserInfo(userId);
            return R.ok(response);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return R.fail("获取用户信息失败");
        }
    }

    /**
     * 从SecurityContext获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
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

    /**
     * 从Authorization头提取token
     */
    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
