package com.example.erp.common.web;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常出口：业务错误保持 HTTP 200 + 非 200 code。
 * MCP 网关仅在 status_code == 200 时原样返回 data，非 200 会被包成 HTTP Error 嵌套结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleAny(Exception e) {
        log.error("unhandled exception", e);
        return R.fail(ErrorCode.INTERNAL_ERROR.getCode(), String.valueOf(e.getMessage()));
    }
}
