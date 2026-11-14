package com.youguocang.security;

import com.youguocang.common.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 签发与校验（jjwt 0.12.x API）。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${youguocang.jwt.secret}") String secret,
                   @Value("${youguocang.jwt.expire-hours}") long expireHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT 密钥长度不足 32 字节，请修改 youguocang.jwt.secret");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expireMillis = expireHours * 3600_000L;
    }

    public String generate(LoginUser loginUser) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getId()))
                .claim("name", loginUser.getName())
                .claim("role", loginUser.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token；非法或过期返回 null，由调用方决定如何响应。
     */
    public LoginUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long id = Long.valueOf(claims.getSubject());
            String name = claims.get("name", String.class);
            RoleType role = RoleType.fromName(claims.get("role", String.class));
            return new LoginUser(id, name, role);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
