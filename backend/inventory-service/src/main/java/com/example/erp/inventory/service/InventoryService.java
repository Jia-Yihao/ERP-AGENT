package com.example.erp.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.Dtos;
import com.example.erp.common.core.PageResult;
import com.example.erp.inventory.client.CatalogClient;
import com.example.erp.inventory.entity.Inventory;
import com.example.erp.inventory.entity.InventoryLog;
import com.example.erp.inventory.mapper.InventoryLogMapper;
import com.example.erp.inventory.mapper.InventoryMapper;
import com.example.erp.common.cache.LockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryMapper mapper;
    private final InventoryLogMapper logMapper;
    private final CatalogClient catalogClient;
    private final LockService lockService;

    public List<Inventory> warning() {
        List<Inventory> list = mapper.selectList(new LambdaQueryWrapper<Inventory>()
                .apply("current_quantity < safety_stock"));
        return withParts(list);
    }

    public PageResult<Inventory> page(long current, long size, String partName, String warehouseLocation) {
        Page<Inventory> page = new Page<>(current, size);
        mapper.selectPageWithPart(page, partName, warehouseLocation);
        withParts(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public Map<String, Object> check() {
        List<Inventory> all = mapper.selectList(null);
        Map<Long, Dtos.PartBrief> briefs = briefMap(all);
        long warningCount = all.stream()
                .filter(i -> i.getCurrentQuantity() < i.getSafetyStock()).count();
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Inventory inv : all) {
            Dtos.PartBrief b = briefs.get(inv.getPartId());
            if (b != null && b.getPurchasePrice() != null) {
                totalValue = totalValue.add(
                        b.getPurchasePrice().multiply(BigDecimal.valueOf(inv.getCurrentQuantity())));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSku", all.size());
        result.put("warningCount", warningCount);
        result.put("totalValue", totalValue);
        return result;
    }

    @Transactional
    public Inventory inbound(Long partId, Integer quantity, String warehouseLocation) {
        if (partId == null || quantity == null || quantity <= 0) {
            throw new BizException(400, "partId 必填且入库数量必须大于 0");
        }
        Inventory inv = getOrCreate(partId);
        int before = inv.getCurrentQuantity();
        inv.setCurrentQuantity(before + quantity);
        if (StringUtils.hasText(warehouseLocation)) inv.setWarehouseLocation(warehouseLocation);
        mapper.updateById(inv);
        writeLog(partId, "INBOUND", quantity, before, inv.getCurrentQuantity());
        return withOne(inv);
    }

    @Transactional
    public Inventory outbound(Long partId, Integer quantity) {
        if (partId == null || quantity == null || quantity <= 0) {
            throw new BizException(400, "partId 必填且出库数量必须大于 0");
        }
        String lockKey = "erp:lock:stock:" + partId;
        Inventory result = lockService.withLock(lockKey, java.time.Duration.ofSeconds(5), () -> {
            Inventory inv = getOrCreate(partId);
            int before = inv.getCurrentQuantity();
            if (mapper.deductStock(inv.getId(), quantity) == 0) {
                throw new BizException(409, "库存不足");
            }
            writeLog(partId, "OUTBOUND", quantity, before, before - quantity);
            inv.setCurrentQuantity(before - quantity);
            return inv;
        });
        if (result == null) throw new BizException(409, "库存操作繁忙，请重试");
        return withOne(result);
    }

    public Inventory get(Long id) {
        Inventory inv = mapper.selectById(id);
        if (inv == null) throw new BizException(404, "库存记录不存在");
        return withOne(inv);
    }

    private Inventory getOrCreate(Long partId) {
        Inventory inv = mapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getPartId, partId));
        if (inv == null) {
            inv = new Inventory();
            inv.setPartId(partId);
            inv.setCurrentQuantity(0);
            inv.setSafetyStock(0);
            mapper.insert(inv);
        }
        return inv;
    }

    private void writeLog(Long partId, String type, int quantity, int before, int after) {
        InventoryLog log = new InventoryLog();
        log.setPartId(partId);
        log.setChangeType(type);
        log.setQuantity(quantity);
        log.setBeforeQuantity(before);
        log.setAfterQuantity(after);
        logMapper.insert(log);
    }

    private Map<Long, Dtos.PartBrief> briefMap(List<Inventory> list) {
        if (list.isEmpty()) return Map.of();
        List<Long> ids = list.stream().map(Inventory::getPartId).distinct().toList();
        return catalogClient.briefs(ids).stream()
                .collect(Collectors.toMap(Dtos.PartBrief::getId, b -> b, (a, b) -> a));
    }

    private List<Inventory> withParts(List<Inventory> list) {
        if (list.isEmpty()) return list;
        Map<Long, Dtos.PartBrief> map = briefMap(list);
        for (Inventory inv : list) inv.setPartDetail(map.get(inv.getPartId()));
        return list;
    }

    private Inventory withOne(Inventory inv) {
        List<Dtos.PartBrief> briefs = catalogClient.briefs(List.of(inv.getPartId()));
        if (!briefs.isEmpty()) inv.setPartDetail(briefs.get(0));
        return inv;
    }
}
