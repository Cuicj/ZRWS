package com.zrws.approval.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（基于 jjwt 0.12.5）
 * <p>负责 Token 的生成、解析、校验与用户标识提取
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 密钥（固定字符串，UTF-8 编码后至少 32 字节 = 256 位）
     */
    private static final String JWT_SECRET = "zrws-approval-jwt-secret-key-2024-very-long-and-secure-256bit-minimum!!!";

    /**
     * Token 有效期：24 小时（毫秒）
     */
    private static final long JWT_EXPIRATION_MS = 24L * 60 * 60 * 1000;

    private final SecretKey key;

    public JwtTokenProvider() {
        this.key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param orgId    当前组织ID
     * @param tenantId 租户ID
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, Long orgId, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("orgId", orgId)
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token，返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否合法且未过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }
}
