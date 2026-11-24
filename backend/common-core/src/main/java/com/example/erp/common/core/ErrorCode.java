package com.example.erp.common.core;

import lombok.Getter;

/**
 * 错误码：1000 以内沿用 HTTP 语义，4000 段为业务冲突码。
 */
@Getter
public enum ErrorCode {

    OK(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证或凭证已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    CONFLICT(409, "业务冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "依赖服务不可用"),

    IDEMPOTENT_REJECTED(4091, "重复请求"),
    STOCK_NOT_ENOUGH(4092, "库存不足"),
    ORDER_STATUS_ILLEGAL(4093, "订单状态不允许该操作"),
    SUPPLIER_CREDIT_LOW(4094, "供应商信用不足");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
