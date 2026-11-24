package com.example.erp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.common.cache.Idempotent;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.Dtos;
import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.R;
import com.example.erp.order.client.CatalogClient;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseOrderDetail;
import com.example.erp.order.mapper.OrderDetailMapper;
import com.example.erp.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderDetailMapper detailMapper;
    private final CatalogClient catalogClient;
    private final OrderStateMachine stateMachine;

    @Idempotent(value = "order:create", ttlSeconds = 300)
    @Transactional
    public PurchaseOrder create(PurchaseOrder body) {
        validate(body);
        long exists = orderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getOrderNumber, body.getOrderNumber()));
        if (exists > 0) throw new BizException(409, "订单号已存在");
        body.setId(null);
        body.setTotalAmount(calcTotal(body.getOrderDetail()));
        if (body.getStatus() == null) body.setStatus(0);
        body.setOrderTime(LocalDateTime.now());
        orderMapper.insert(body);
        insertDetails(body.getId(), body.getOrderDetail());
        return get(body.getId());
    }

    @Transactional
    public PurchaseOrder update(Long id, PurchaseOrder body) {
        get(id);
        validate(body);
        body.setId(id);
        body.setTotalAmount(calcTotal(body.getOrderDetail()));
        orderMapper.updateById(body);
        detailMapper.delete(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .eq(PurchaseOrderDetail::getOrderId, id));
        insertDetails(id, body.getOrderDetail());
        return get(id);
    }

    public PageResult<PurchaseOrder> page(long current, long size, String orderNumber,
                                          Integer status, String startDate, String endDate) {
        LambdaQueryWrapper<PurchaseOrder> w = new LambdaQueryWrapper<PurchaseOrder>()
                .like(StringUtils.hasText(orderNumber), PurchaseOrder::getOrderNumber, orderNumber)
                .eq(status != null, PurchaseOrder::getStatus, status)
                .ge(toStart(startDate) != null, PurchaseOrder::getOrderTime, toStart(startDate))
                .le(toEnd(endDate) != null, PurchaseOrder::getOrderTime, toEnd(endDate));
        Page<PurchaseOrder> p = orderMapper.selectPage(new Page<>(current, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public PurchaseOrder get(Long id) {
        PurchaseOrder o = orderMapper.selectById(id);
        if (o == null) throw new BizException(404, "订单不存在");
        o.setOrderDetail(detailMapper.selectList(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .eq(PurchaseOrderDetail::getOrderId, id)));
        return o;
    }

    public List<PurchaseOrderDetail> searchDetails(String partName, String startDate, String endDate) {
        List<PurchaseOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                .ge(toStart(startDate) != null, PurchaseOrder::getOrderTime, toStart(startDate))
                .le(toEnd(endDate) != null, PurchaseOrder::getOrderTime, toEnd(endDate)));
        if (orders.isEmpty()) return List.of();
        Map<Long, PurchaseOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(PurchaseOrder::getId, o -> o, (a, b) -> a));
        List<PurchaseOrderDetail> details = detailMapper.selectList(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .in(PurchaseOrderDetail::getOrderId, orderMap.keySet()));
        if (details.isEmpty()) return List.of();
        List<Long> partIds = details.stream().map(PurchaseOrderDetail::getPartId).distinct().toList();
        Map<Long, Dtos.PartBrief> briefs = catalogClient.briefs(partIds).stream()
                .collect(Collectors.toMap(Dtos.PartBrief::getId, b -> b, (a, b) -> a));
        List<PurchaseOrderDetail> result = new ArrayList<>();
        for (PurchaseOrderDetail d : details) {
            Dtos.PartBrief b = briefs.get(d.getPartId());
            if (StringUtils.hasText(partName) && (b == null || b.getName() == null
                    || !b.getName().contains(partName))) continue;
            d.setPartName(b == null ? null : b.getName());
            d.setSupplierName(b == null ? null : b.getSupplierName());
            PurchaseOrder o = orderMap.get(d.getOrderId());
            d.setOrderNumber(o == null ? null : o.getOrderNumber());
            d.setOrderTime(o == null ? null : o.getOrderTime());
            result.add(d);
        }
        return result;
    }

    public Map<String, Object> statistics(String startDate, String endDate) {
        List<Map<String, Object>> rows = orderMapper.selectStatistics(toStart(startDate), toEnd(endDate));
        Map<String, Object> statusCounts = new LinkedHashMap<>();
        for (int i = 0; i <= 4; i++) statusCounts.put(String.valueOf(i), 0L);
        BigDecimal totalAmount = orderMapper.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                .ge(toStart(startDate) != null, PurchaseOrder::getOrderTime, toStart(startDate))
                .le(toEnd(endDate) != null, PurchaseOrder::getOrderTime, toEnd(endDate)))
                .stream().map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = 0;
        for (Map<String, Object> row : rows) {
            int status = ((Number) row.get("status")).intValue();
            long cnt = ((Number) row.get("cnt")).longValue();
            statusCounts.put(String.valueOf(status), cnt);
            totalOrders += cnt;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("totalAmount", totalAmount);
        result.put("statusCounts", statusCounts);
        result.put("partCount", detailMapper.selectCount(null));
        return result;
    }

    @Transactional
    public PurchaseOrder updateStatus(Long id, Integer status) {
        if (status == null) throw new BizException(400, "status 必填");
        PurchaseOrder o = get(id);
        stateMachine.assertTransition(o.getStatus(), status);
        o.setStatus(status);
        orderMapper.updateById(o);
        return get(id);
    }

    private void validate(PurchaseOrder body) {
        if (!StringUtils.hasText(body.getOrderNumber()) || body.getOrderDetail() == null
                || body.getOrderDetail().isEmpty()) {
            throw new BizException(400, "订单号与订单明细必填");
        }
        for (PurchaseOrderDetail d : body.getOrderDetail()) {
            if (d.getPartId() == null || d.getQuantity() == null || d.getUnitPrice() == null) {
                throw new BizException(400, "明细缺少 partId / quantity / unitPrice");
            }
        }
    }

    private BigDecimal calcTotal(List<PurchaseOrderDetail> details) {
        return details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void insertDetails(Long orderId, List<PurchaseOrderDetail> details) {
        for (PurchaseOrderDetail d : details) {
            d.setId(null);
            d.setOrderId(orderId);
            detailMapper.insert(d);
        }
    }

    private LocalDateTime toStart(String s) {
        if (!StringUtils.hasText(s)) return null;
        try {
            return s.length() <= 10 ? LocalDateTime.parse(s + "T00:00:00") : LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime toEnd(String s) {
        if (!StringUtils.hasText(s)) return null;
        try {
            return s.length() <= 10 ? LocalDateTime.parse(s + "T23:59:59") : LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
