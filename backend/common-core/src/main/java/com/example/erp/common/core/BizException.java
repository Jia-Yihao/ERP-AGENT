package com.example.erp.common.core;

import lombok.Getter;

/**
 * 业务异常：由 GlobalExceptionHandler 转为 HTTP 200 + 非零 code。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
