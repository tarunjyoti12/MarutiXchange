package com.marutixchange.bidding_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ GENERATE TOKEN (🔥 THIS WAS MISSING)
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ Extract email
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ✅ Extract userId
    public Long extractUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }

    // ✅ Validate token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}