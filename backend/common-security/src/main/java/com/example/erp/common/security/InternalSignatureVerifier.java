package com.example.erp.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 内部调用签名：X-Internal-Token = {epochSeconds}.{HMAC-SHA256(timestamp + body)}。
 * 服务端未配置 ERP_INTERNAL_SECRET 时一律拒绝，避免空密钥伪造。
 */
public final class InternalSignatureVerifier {

    private static final long WINDOW_SECONDS = 60L;
    private static final String ALGORITHM = "HmacSHA256";
    private static final String SECRET = System.getenv("ERP_INTERNAL_SECRET");

    private InternalSignatureVerifier() {
    }

    public static boolean verify(String header, String body) {
        if (header == null || header.isBlank() || body == null || isSecretMissing()) {
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
        String expected = sign(timestamp, body);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    public static String sign(String epochSeconds, String body) {
        if (isSecretMissing() || epochSeconds == null || body == null) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal((epochSeconds + body).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static boolean isSecretMissing() {
        return SECRET == null || SECRET.isBlank();
    }

    private static long normalizeSeconds(long value) {
        return value > 1_000_000_000_000L ? value / 1000L : value;
    }
}
