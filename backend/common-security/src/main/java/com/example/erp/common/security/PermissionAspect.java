package com.example.erp.common.security;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.UserContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 校验当前用户上下文中的权限码。
 */
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(required) || @within(required)")
    public Object check(ProceedingJoinPoint joinPoint, RequiresPermission required) throws Throwable {
        UserContextHolder.UserContext context = UserContextHolder.get();
        List<String> perms = context == null ? null : context.perms();
        if (perms == null || Arrays.stream(required.value()).noneMatch(perms::contains)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }
}
