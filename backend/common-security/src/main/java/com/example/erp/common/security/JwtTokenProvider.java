package com.example.erp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 访问令牌与刷新令牌的签发、解析和校验。
 */
@Component
public class JwtTokenProvider {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMS = "perms";
    public static final String CLAIM_CLIENT_ID = "client_id";
    public static final String CLAIM_TYPE = "type";

    private final JwtProperties properties;
    private volatile SecretKey cachedKey;
    private volatile String cachedSecret;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    public String issueAccess(Long userId, String username, List<String> roles,
                              List<String> perms, String clientId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLES, roles == null ? List.of() : roles)
                .claim(CLAIM_PERMS, perms == null ? List.of() : perms)
                .claim(CLAIM_CLIENT_ID, clientId)
                .claim(CLAIM_TYPE, "access")
                .id(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.getAccessTtlSeconds() * 1000))
                .signWith(signingKey())
                .compact();
    }

    public String issueRefresh(Long userId, String clientId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_CLIENT_ID, clientId)
                .claim(CLAIM_TYPE, "refresh")
                .id(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.getRefreshTtlSeconds() * 1000))
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }

    public String getClientId(Claims claims) {
        return claims.get(CLAIM_CLIENT_ID, String.class);
    }

    public String getType(Claims claims) {
        return claims.get(CLAIM_TYPE, String.class);
    }

    public Long getExpirationSeconds(Claims claims) {
        Date exp = claims.getExpiration();
        return exp == null ? 0L : Math.max(0L, (exp.getTime() - System.currentTimeMillis()) / 1000);
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private SecretKey signingKey() {
        String secret = properties.signingSecret();
        SecretKey key = cachedKey;
        if (key == null || !secret.equals(cachedSecret)) {
            key = Keys.hmacShaKeyFor(properties.signingKeyBytes());
            cachedKey = key;
            cachedSecret = secret;
        }
        return key;
    }
}
