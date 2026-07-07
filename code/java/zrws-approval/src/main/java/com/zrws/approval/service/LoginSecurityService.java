package com.zrws.approval.service;

import com.zrws.approval.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录安全服务 - 防止暴力破解和机器人刷登录
 */
@Slf4j
@Service
public class LoginSecurityService {

    @Autowired
    private RedisService redisService;

    private static final String IP_ATTEMPT_KEY_PREFIX = "login:ip:";
    private static final String USER_ATTEMPT_KEY_PREFIX = "login:user:";
    private static final String USER_LOCK_KEY_PREFIX = "login:lock:user:";
    private static final String IP_LOCK_KEY_PREFIX = "login:lock:ip:";

    private static final int MAX_IP_ATTEMPTS = 10;
    private static final int MAX_USER_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;
    private static final int ATTEMPT_WINDOW_MINUTES = 5;

    /**
     * 检查登录前的安全状态
     * @return null 表示通过检查，否则返回错误消息
     */
    public String checkBeforeLogin(String ip, String username) {
        if (isIpLocked(ip)) {
            return "当前IP登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试";
        }
        
        if (isUserLocked(username)) {
            return "账号已被锁定，请" + LOCK_MINUTES + "分钟后再试";
        }
        
        if (getIpAttemptCount(ip) >= MAX_IP_ATTEMPTS) {
            lockIp(ip);
            return "当前IP登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试";
        }
        
        if (getUserAttemptCount(username) >= MAX_USER_ATTEMPTS) {
            lockUser(username);
            return "账号登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试";
        }
        
        return null;
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFailure(String ip, String username) {
        int ipAttempts = incrementIpAttempt(ip);
        int userAttempts = incrementUserAttempt(username);
        
        log.warn("登录失败: ip={}, username={}, ipAttempts={}, userAttempts={}", 
                ip, username, ipAttempts, userAttempts);
        
        if (ipAttempts >= MAX_IP_ATTEMPTS) {
            lockIp(ip);
            log.warn("IP已锁定: {}", ip);
        }
        
        if (userAttempts >= MAX_USER_ATTEMPTS) {
            lockUser(username);
            log.warn("用户已锁定: {}", username);
        }
    }

    /**
     * 登录成功，重置计数
     */
    public void recordLoginSuccess(String ip, String username) {
        resetIpAttempt(ip);
        resetUserAttempt(username);
        unlockUser(username);
        log.info("登录成功: ip={}, username={}", ip, username);
    }

    /**
     * 获取IP尝试次数
     */
    private int getIpAttemptCount(String ip) {
        String count = redisService.get(IP_ATTEMPT_KEY_PREFIX + ip);
        return count == null ? 0 : Integer.parseInt(count);
    }

    /**
     * 增加IP尝试次数
     */
    private int incrementIpAttempt(String ip) {
        String key = IP_ATTEMPT_KEY_PREFIX + ip;
        String count = redisService.get(key);
        int newCount = count == null ? 1 : Integer.parseInt(count) + 1;
        redisService.setEx(key, String.valueOf(newCount), ATTEMPT_WINDOW_MINUTES * 60);
        return newCount;
    }

    /**
     * 重置IP尝试次数
     */
    private void resetIpAttempt(String ip) {
        redisService.del(IP_ATTEMPT_KEY_PREFIX + ip);
    }

    /**
     * IP是否被锁定
     */
    private boolean isIpLocked(String ip) {
        return redisService.exists(IP_LOCK_KEY_PREFIX + ip);
    }

    /**
     * 锁定IP
     */
    private void lockIp(String ip) {
        redisService.setEx(IP_LOCK_KEY_PREFIX + ip, "1", LOCK_MINUTES * 60);
    }

    /**
     * 获取用户尝试次数
     */
    private int getUserAttemptCount(String username) {
        String count = redisService.get(USER_ATTEMPT_KEY_PREFIX + username);
        return count == null ? 0 : Integer.parseInt(count);
    }

    /**
     * 增加用户尝试次数
     */
    private int incrementUserAttempt(String username) {
        String key = USER_ATTEMPT_KEY_PREFIX + username;
        String count = redisService.get(key);
        int newCount = count == null ? 1 : Integer.parseInt(count) + 1;
        redisService.setEx(key, String.valueOf(newCount), ATTEMPT_WINDOW_MINUTES * 60);
        return newCount;
    }

    /**
     * 重置用户尝试次数
     */
    private void resetUserAttempt(String username) {
        redisService.del(USER_ATTEMPT_KEY_PREFIX + username);
    }

    /**
     * 用户是否被锁定
     */
    private boolean isUserLocked(String username) {
        return redisService.exists(USER_LOCK_KEY_PREFIX + username);
    }

    /**
     * 锁定用户
     */
    private void lockUser(String username) {
        redisService.setEx(USER_LOCK_KEY_PREFIX + username, "1", LOCK_MINUTES * 60);
    }

    /**
     * 解锁用户
     */
    private void unlockUser(String username) {
        redisService.del(USER_LOCK_KEY_PREFIX + username);
    }
}
