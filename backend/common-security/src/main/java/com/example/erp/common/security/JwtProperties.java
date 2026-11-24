package com.example.erp.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * JWT 签发参数。HS256 要求密钥至少 32 字节。
 */
@Data
@ConfigurationProperties(prefix = "erp.jwt")
public class JwtProperties {

    private static final String FALLBACK_SECRET = "erp-cloud-dev-jwt-secret-change-me-32bytes";

    private String secret = resolveEnvSecret();
    private Long accessTtlSeconds = 7200L;
    private Long refreshTtlSeconds = 604800L;
    private String issuer = "erp-cloud";

    private static String resolveEnvSecret() {
        String value = System.getenv("ERP_JWT_SECRET");
        if (value == null || value.isBlank()) {
            value = System.getenv("JWT_SECRET");
        }
        return value == null || value.isBlank() ? FALLBACK_SECRET : value;
    }

    public String signingSecret() {
        return secret == null || secret.isBlank() ? FALLBACK_SECRET : secret;
    }

    public byte[] signingKeyBytes() {
        return signingSecret().getBytes(StandardCharsets.UTF_8);
    }
}
