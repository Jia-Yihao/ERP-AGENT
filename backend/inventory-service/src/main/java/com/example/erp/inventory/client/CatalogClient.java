package com.example.erp.inventory.client;

import com.example.erp.common.core.Dtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/** 库存 VO 需要嵌套 partDetail，通过服务发现调用 catalog-service 批量取得。 */
@FeignClient(name = "catalog-service", fallbackFactory = CatalogClientFallbackFactory.class)
public interface CatalogClient {
    @PostMapping("/internal/parts/batch")
    List<Dtos.PartBrief> briefs(@RequestBody List<Long> ids);
}
