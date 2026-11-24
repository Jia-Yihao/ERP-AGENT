package com.example.erp.report.client;

import com.example.erp.common.core.R;
import com.example.erp.common.feign.DegradedFallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/** inventory-service 不可用时返回空检查数据，汇总接口仍能给出其余维度数据。 */
@Component
public class InventoryCheckClientFallbackFactory extends DegradedFallbackFactory<InventoryCheckClient> {

    @Override
    public InventoryCheckClient create(Throwable cause) {
        degraded(cause);
        return () -> R.ok(Map.<String, Object>of());
    }
}
