package com.example.erp.common.data;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * MyBatis 慢 SQL 拦截：执行耗时超过阈值时以 WARN 打印单条 SQL。
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class SlowSqlInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger("SLOW_SQL");

    @Value("${erp.slow-sql.threshold-ms:200}")
    private long thresholdMs;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (elapsedMs >= thresholdMs) {
                Object[] args = invocation.getArgs();
                MappedStatement ms = (MappedStatement) args[0];
                BoundSql boundSql = ms.getBoundSql(args.length > 1 ? args[1] : null);
                log.warn("[{}ms] {} | {}", elapsedMs, ms.getId(),
                        boundSql.getSql().replaceAll("\\s+", " "));
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return org.apache.ibatis.plugin.Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
