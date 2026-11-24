package com.example.erp.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.entity.SupplierPerformance;
import com.example.erp.catalog.entity.SupplierQuote;
import com.example.erp.catalog.mapper.SupplierMapper;
import com.example.erp.catalog.mapper.SupplierPerformanceMapper;
import com.example.erp.catalog.mapper.SupplierQuoteMapper;
import com.example.erp.common.core.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {
    private final SupplierPerformanceMapper mapper;
    private final SupplierMapper supplierMapper;
    private final SupplierQuoteMapper quoteMapper;

    public SupplierPerformance get(Long supplierId) {
        SupplierPerformance p = mapper.selectOne(new LambdaQueryWrapper<SupplierPerformance>()
                .eq(SupplierPerformance::getSupplierId, supplierId)
                .orderByDesc(SupplierPerformance::getStatDate)
                .last("LIMIT 1"));
        if (p == null) throw new BizException(404, "供应商绩效数据不存在");
        return p;
    }

    /** catalog 侧无订单与收货明细，按供应商信用等级和有效报价交期做确定性评分。 */
    @Transactional
    public SupplierPerformance recalculate(Long supplierId, LocalDate statDate) {
        Supplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) throw new BizException(404, "供应商不存在");
        LocalDate date = statDate == null ? LocalDate.now() : statDate;

        BigDecimal onTimeRate = switch (credit(supplier)) {
            case "A" -> new BigDecimal("0.9800");
            case "B" -> new BigDecimal("0.9300");
            default -> new BigDecimal("0.8600");
        };
        BigDecimal qualityRate = switch (credit(supplier)) {
            case "A" -> new BigDecimal("0.9900");
            case "B" -> new BigDecimal("0.9500");
            default -> new BigDecimal("0.9000");
        };
        List<SupplierQuote> quotes = quoteMapper.selectList(new LambdaQueryWrapper<SupplierQuote>()
                .eq(SupplierQuote::getSupplierId, supplierId)
                .eq(SupplierQuote::getStatus, 1));
        double avgLead = quotes.stream()
                .filter(q -> q.getLeadDays() != null)
                .mapToInt(SupplierQuote::getLeadDays)
                .average().orElse(10.0);
        BigDecimal avgLeadDays = BigDecimal.valueOf(avgLead).setScale(2, RoundingMode.HALF_UP);
        BigDecimal leadScore = BigDecimal.ONE
                .subtract(avgLeadDays.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);
        BigDecimal score = onTimeRate.multiply(new BigDecimal("0.4"))
                .add(qualityRate.multiply(new BigDecimal("0.4")))
                .add(leadScore.multiply(new BigDecimal("0.2")))
                .setScale(4, RoundingMode.HALF_UP);

        SupplierPerformance existing = mapper.selectOne(new LambdaQueryWrapper<SupplierPerformance>()
                .eq(SupplierPerformance::getSupplierId, supplierId)
                .eq(SupplierPerformance::getStatDate, date)
                .last("LIMIT 1"));
        SupplierPerformance target = existing == null ? new SupplierPerformance() : existing;
        target.setSupplierId(supplierId);
        target.setStatDate(date);
        target.setOnTimeRate(onTimeRate);
        target.setQualityRate(qualityRate);
        target.setAvgLeadDays(avgLeadDays);
        target.setScore(score);
        if (existing == null) mapper.insert(target);
        else mapper.updateById(target);
        return target;
    }

    private String credit(Supplier supplier) {
        return supplier.getCreditRating() == null ? "C" : supplier.getCreditRating().toUpperCase();
    }
}
