package com.example.erp.common.core;

import java.util.List;

/**
 * 当前请求的用户上下文，由网关过滤器或下游服务的安全过滤器写入。
 * 未鉴权请求返回 system，供审计字段自动填充使用。
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> LOCAL = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(Long userId, String username, List<String> roles, List<String> perms) {
        LOCAL.set(new UserContext(userId, username, roles, perms));
    }

    public static void clear() {
        LOCAL.remove();
    }

    public static UserContext get() {
        return LOCAL.get();
    }

    public static String currentUsername() {
        UserContext ctx = LOCAL.get();
        return ctx == null || ctx.username() == null || ctx.username().isBlank()
                ? "system" : ctx.username();
    }

    public record UserContext(Long userId, String username, List<String> roles, List<String> perms) {
    }
}
