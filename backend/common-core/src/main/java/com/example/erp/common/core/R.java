package com.example.erp.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一响应体，字段固定为 code / message / data。
 * 业务错误保持 HTTP 200，错误码放在 body 的 code 字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        return new R<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return new R<>(errorCode.getCode(), message, null);
    }
}
