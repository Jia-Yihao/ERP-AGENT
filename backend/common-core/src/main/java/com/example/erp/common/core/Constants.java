package com.example.erp.common.core;

/**
 * 跨模块共享常量：请求头、缓存键前缀、分页边界。
 */
public final class Constants {

    private Constants() {
    }

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    public static final String CACHE_PREFIX = "erp:";
    public static final String CACHE_SEPARATOR = ":";
    public static final String LOCK_PREFIX = "erp:lock:";
    public static final String IDEMPOTENT_PREFIX = "erp:idem:";
    public static final String BLACKLIST_PREFIX = "erp:auth:blacklist:";
    public static final String RATE_LIMIT_PREFIX = "erp:rate:";

    public static final long PAGE_CURRENT_DEFAULT = 1L;
    public static final long PAGE_SIZE_DEFAULT = 10L;
    public static final long PAGE_SIZE_MAX = 100L;

    public static final String TRACE_ID_MDC_KEY = "traceId";
}
