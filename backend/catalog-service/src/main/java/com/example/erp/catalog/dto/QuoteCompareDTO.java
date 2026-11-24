package com.example.erp.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuoteCompareDTO {
    private Long quoteId;
    private Long supplierId;
    private String supplierName;
    private String creditRating;
    private Long partId;
    private String partName;
    private BigDecimal unitPrice;
    private Integer leadDays;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer rank;
}
