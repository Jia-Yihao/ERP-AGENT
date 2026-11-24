package com.example.erp.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.catalog.dto.QuoteCompareDTO;
import com.example.erp.catalog.entity.Part;
import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.entity.SupplierQuote;
import com.example.erp.catalog.mapper.PartMapper;
import com.example.erp.catalog.mapper.SupplierMapper;
import com.example.erp.catalog.mapper.SupplierQuoteMapper;
import com.example.erp.common.core.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final SupplierQuoteMapper mapper;
    private final SupplierMapper supplierMapper;
    private final PartMapper partMapper;

    public List<SupplierQuote> list(Long supplierId, Long partId, Integer status) {
        return mapper.selectList(new LambdaQueryWrapper<SupplierQuote>()
                .eq(supplierId != null, SupplierQuote::getSupplierId, supplierId)
                .eq(partId != null, SupplierQuote::getPartId, partId)
                .eq(status != null, SupplierQuote::getStatus, status)
                .orderByDesc(SupplierQuote::getId));
    }

    @Transactional
    public SupplierQuote create(SupplierQuote body) {
        Supplier supplier = supplierMapper.selectById(body.getSupplierId());
        if (supplier == null) throw new BizException(404, "供应商不存在");
        Part part = partMapper.selectById(body.getPartId());
        if (part == null) throw new BizException(404, "零部件不存在");
        if (body.getUnitPrice() == null || body.getUnitPrice().signum() <= 0) {
            throw new BizException(400, "unitPrice 必须大于 0");
        }
        if (body.getLeadDays() == null) body.setLeadDays(0);
        if (body.getLeadDays() < 0) throw new BizException(400, "leadDays 不能为负数");
        LocalDate today = LocalDate.now();
        if (body.getValidFrom() == null) body.setValidFrom(today);
        if (body.getValidTo() == null) body.setValidTo(body.getValidFrom().plusYears(1));
        if (body.getValidTo().isBefore(body.getValidFrom())) {
            throw new BizException(400, "validTo 不能早于 validFrom");
        }
        if (body.getStatus() == null) body.setStatus(1);
        mapper.insert(body);
        return body;
    }

    public List<QuoteCompareDTO> compareByPart(Long partId) {
        Part part = partMapper.selectById(partId);
        if (part == null) throw new BizException(404, "零部件不存在");
        List<SupplierQuote> quotes = mapper.selectActiveByPart(partId);
        if (quotes.isEmpty()) return List.of();
        List<Long> supplierIds = quotes.stream().map(SupplierQuote::getSupplierId).distinct().toList();
        Map<Long, Supplier> suppliers = supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                        .in(Supplier::getId, supplierIds))
                .stream().collect(Collectors.toMap(Supplier::getId, Function.identity(), (a, b) -> a));
        Comparator<SupplierQuote> comparator = Comparator
                .comparing(SupplierQuote::getUnitPrice, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierQuote::getLeadDays, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierQuote::getSupplierId, Comparator.nullsLast(Comparator.naturalOrder()));
        List<SupplierQuote> sorted = quotes.stream().sorted(comparator).toList();
        List<QuoteCompareDTO> result = new java.util.ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            SupplierQuote q = sorted.get(i);
            Supplier s = suppliers.get(q.getSupplierId());
            QuoteCompareDTO dto = new QuoteCompareDTO();
            dto.setQuoteId(q.getId());
            dto.setSupplierId(q.getSupplierId());
            dto.setSupplierName(s == null ? null : s.getName());
            dto.setCreditRating(s == null ? null : s.getCreditRating());
            dto.setPartId(partId);
            dto.setPartName(part.getName());
            dto.setUnitPrice(q.getUnitPrice());
            dto.setLeadDays(q.getLeadDays());
            dto.setValidFrom(q.getValidFrom());
            dto.setValidTo(q.getValidTo());
            dto.setRank(i + 1);
            result.add(dto);
        }
        return result;
    }

    public List<SupplierQuote> activeByPart(Long partId) {
        if (partId == null) throw new BizException(400, "partId 必填");
        return mapper.selectActiveByPart(partId);
    }
}
