package com.example.erp.catalog.controller;

import com.example.erp.catalog.dto.QuoteCompareDTO;
import com.example.erp.catalog.dto.QuoteCreateDTO;
import com.example.erp.catalog.entity.SupplierQuote;
import com.example.erp.catalog.service.QuoteService;
import com.example.erp.common.core.R;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService service;

    @PostMapping("/create")
    public R<SupplierQuote> create(@Valid @RequestBody QuoteCreateDTO body) {
        SupplierQuote quote = new SupplierQuote();
        quote.setSupplierId(body.getSupplierId());
        quote.setPartId(body.getPartId());
        quote.setUnitPrice(body.getUnitPrice());
        quote.setLeadDays(body.getLeadDays());
        quote.setValidFrom(body.getValidFrom());
        quote.setValidTo(body.getValidTo());
        quote.setStatus(body.getStatus());
        return R.ok(service.create(quote));
    }

    @GetMapping("/list")
    public R<List<SupplierQuote>> list(Long supplierId, Long partId, Integer status) {
        return R.ok(service.list(supplierId, partId, status));
    }

    @GetMapping("/compare/{partId}")
    public R<List<QuoteCompareDTO>> compare(@PathVariable Long partId) {
        return R.ok(service.compareByPart(partId));
    }
}
