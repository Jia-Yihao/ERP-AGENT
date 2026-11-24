package com.example.erp.inventory.client;

import com.example.erp.common.core.Dtos;
import com.example.erp.common.feign.DegradedFallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/** catalog-service 不可用时返回空零件摘要，库存 VO 降级展示而不整体失败。 */
@Component
public class CatalogClientFallbackFactory extends DegradedFallbackFactory<CatalogClient> {

    @Override
    public CatalogClient create(Throwable cause) {
        degraded(cause);
        return ids -> List.<Dtos.PartBrief>of();
    }
}
