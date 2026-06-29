package com.zrws.approval.service;

import com.zrws.approval.domain.entity.ApiKey;
import com.zrws.approval.mapper.ApiKeyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class OpenApiAuthService {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final long TIMESTAMP_VALIDITY_SECONDS = 300;
    private static final int DEFAULT_RATE_LIMIT = 100;
    private static final long RATE_LIMIT_WINDOW_MS = 60000;

    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    public boolean validateSignature(String apiKey, String timestamp, String signature,
                                     String method, String path, String body) {
        if (apiKey == null || timestamp == null || signature == null) {
            return false;
        }

        ApiKey key = apiKeyMapper.selectByApiKey(apiKey);
        if (key == null) {
            log.warn("API Key not found: {}", apiKey);
            return false;
        }

        if (!ApiKey.Status.ACTIVE.getValue().equals(key.getStatus())) {
            log.warn("API Key is not active: {}, status: {}", apiKey, key.getStatus());
            return false;
        }

        if (key.getExpireTime() != null && key.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("API Key has expired: {}", apiKey);
            return false;
        }

        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - ts) > TIMESTAMP_VALIDITY_SECONDS) {
                log.warn("Timestamp expired for apiKey: {}, timestamp: {}", apiKey, timestamp);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp format: {}", timestamp);
            return false;
        }

        String stringToSign = method + "\n" + path + "\n" + timestamp + "\n" + (body != null ? body : "");
        String calculatedSignature = hmacSha256(key.getApiSecret(), stringToSign);

        if (!MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                calculatedSignature.getBytes(StandardCharsets.UTF_8))) {
            log.warn("Signature mismatch for apiKey: {}", apiKey);
            return false;
        }

        key.setRequestCount(key.getRequestCount() == null ? 1 : key.getRequestCount() + 1);
        key.setLastRequestTime(LocalDateTime.now());
        apiKeyMapper.updateById(key);

        return true;
    }

    public boolean checkRateLimit(String apiKey) {
        ApiKey key = apiKeyMapper.selectByApiKey(apiKey);
        if (key == null) {
            return false;
        }

        int limit = key.getRateLimit() != null ? key.getRateLimit() : DEFAULT_RATE_LIMIT;

        RateLimitEntry entry = rateLimitMap.computeIfAbsent(apiKey, k -> new RateLimitEntry());

        long now = System.currentTimeMillis();
        if (now - entry.windowStart > RATE_LIMIT_WINDOW_MS) {
            entry.windowStart = now;
            entry.count.set(0);
        }

        return entry.count.incrementAndGet() <= limit;
    }

    public ApiKey generateApiKey(Long companyId, String companyName, String permissions) {
        ApiKey apiKey = new ApiKey();
        apiKey.setApiKey("ZRWS_" + generateRandomString(24));
        apiKey.setApiSecret(generateRandomString(48));
        apiKey.setCompanyId(companyId);
        apiKey.setCompanyName(companyName);
        apiKey.setPermissions(permissions);
        apiKey.setRateLimit(DEFAULT_RATE_LIMIT);
        apiKey.setRequestCount(0);
        apiKey.setStatus(ApiKey.Status.ACTIVE.getValue());
        apiKey.setCreatedTime(LocalDateTime.now());
        apiKey.setUpdatedTime(LocalDateTime.now());

        apiKeyMapper.insert(apiKey);
        return apiKey;
    }

    public void revokeApiKey(Long keyId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey != null) {
            apiKey.setStatus(ApiKey.Status.DISABLED.getValue());
            apiKey.setUpdatedTime(LocalDateTime.now());
            apiKeyMapper.updateById(apiKey);
        }
    }

    public List<ApiKey> listApiKeys() {
        return apiKeyMapper.selectList(null);
    }

    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("HMAC-SHA256 calculation failed", e);
            throw new RuntimeException("HMAC-SHA256 calculation failed", e);
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static class RateLimitEntry {
        long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
