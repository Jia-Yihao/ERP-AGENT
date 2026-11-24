package com.example.erp.common.web;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常出口：所有分支保持 HTTP 200 + 非零 code。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::fieldMessage)
                .distinct()
                .collect(Collectors.joining("; "));
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), e.getParameterName() + " 必填");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public R<Void> handleMissingPart(MissingServletRequestPartException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), e.getRequestPartName() + " 必填");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), e.getName() + " 类型错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return R.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMethod() + " 方法不允许");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNotFound(NoResourceFoundException e) {
        return R.fail(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleAny(Exception e) {
        log.error("unhandled exception", e);
        return R.fail(ErrorCode.INTERNAL_ERROR.getCode(), String.valueOf(e.getMessage()));
    }

    private String fieldMessage(FieldError e) {
        return e.getField() + ": " + e.getDefaultMessage();
    }
}
