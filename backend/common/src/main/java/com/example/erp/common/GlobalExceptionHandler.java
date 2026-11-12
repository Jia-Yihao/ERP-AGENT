package com.example.erp.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务错误保持 HTTP 200 + 非 200 code。
 * MCP 网关 http_base.py 只在 status_code == 200 时原样返回 data，
 * 非 200 会被包成 "HTTP Error" 嵌套结构，模型侧无法解析。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleAny(Exception e) {
        return R.fail(500, String.valueOf(e.getMessage()));
    }
}
