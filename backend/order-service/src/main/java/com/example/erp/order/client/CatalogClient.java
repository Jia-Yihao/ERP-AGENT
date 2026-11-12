package com.example.erp.order.client;

import com.example.erp.common.Dtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/** 订单明细需要 partName / supplierName，通过服务发现调用 catalog-service 批量取得。 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @PostMapping("/internal/parts/batch")
    List<Dtos.PartBrief> briefs(@RequestBody List<Long> ids);
}
