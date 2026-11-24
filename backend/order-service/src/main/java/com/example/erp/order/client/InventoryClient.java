package com.example.erp.order.client;

import com.example.erp.common.core.R;
import com.example.erp.common.feign.DegradedFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/** 收货时跨服务触发库存入库，由 Seata @GlobalTransactional 保证两库一致。 */
@FeignClient(name = "inventory-service", path = "/api/inventory",
        fallbackFactory = InventoryClient.Fallback.class)
public interface InventoryClient {

    @PostMapping("/inbound")
    R<Map<String, Object>> inbound(@RequestParam("partId") Long partId,
                                   @RequestParam("quantity") Integer quantity,
                                   @RequestParam(value = "warehouseLocation", required = false) String warehouseLocation);

    @Component
    class Fallback extends DegradedFallbackFactory<InventoryClient> {
        @Override
        public InventoryClient create(Throwable cause) {
            degraded(cause);
            return (partId, quantity, location) -> R.fail(com.example.erp.common.core.ErrorCode.SERVICE_UNAVAILABLE);
        }
    }
}
