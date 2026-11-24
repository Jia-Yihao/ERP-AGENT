package com.example.erp.common.core;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 服务间调用签名：X-Internal-Token = {epochSeconds}.{HMAC-SHA256(epochSeconds + body)}。
 * 密钥取环境变量 ERP_INTERNAL_SECRET，未配置时签名能力关闭（调用方不加签，接收方不校验）。
 */
public final class InternalSignature {

    private static final String SECRET_ENV = "ERP_INTERNAL_SECRET";
    private static final String SECRET_PROPERTY = "erp.internal.secret";
    private static final long WINDOW_SECONDS = 60L;
    private static final String ALGORITHM = "HmacSHA256";

    private InternalSignature() {
    }

    public static boolean isConfigured() {
        String secret = secret();
        return secret != null && !secret.isBlank();
    }

    public static String sign(String epochSeconds, String body) {
        String secret = secret();
        if (secret == null || secret.isBlank()) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal((epochSeconds + (body == null ? "" : body)).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    public static boolean verify(String header, String body) {
        if (!isConfigured()) {
            return true;
        }
        if (header == null || header.isBlank()) {
            return false;
        }
        int dot = header.indexOf('.');
        if (dot <= 0 || dot >= header.length() - 1) {
            return false;
        }
        String timestamp = header.substring(0, dot);
        String signature = header.substring(dot + 1);
        long epochSeconds;
        try {
            epochSeconds = normalizeSeconds(Long.parseLong(timestamp));
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(System.currentTimeMillis() / 1000L - epochSeconds) > WINDOW_SECONDS) {
            return false;
        }
        return MessageDigest.isEqual(
                sign(timestamp, body).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String secret() {
        String fromEnv = System.getenv(SECRET_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return System.getProperty(SECRET_PROPERTY);
    }

    private static long normalizeSeconds(long value) {
        return value > 1_000_000_000_000L ? value / 1000L : value;
    }
}
