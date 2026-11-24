package com.example.erp.report.client;

import com.example.erp.common.core.R;
import com.example.erp.common.feign.DegradedFallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/** order-service 不可用时返回空统计，汇总接口仍能给出其余维度数据。 */
@Component
public class OrderStatsClientFallbackFactory extends DegradedFallbackFactory<OrderStatsClient> {

    @Override
    public OrderStatsClient create(Throwable cause) {
        degraded(cause);
        return () -> R.ok(Map.<String, Object>of());
    }
}
