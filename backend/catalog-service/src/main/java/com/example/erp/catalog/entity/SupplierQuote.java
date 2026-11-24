package com.example.erp.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("supplier_quote")
public class SupplierQuote extends BaseEntity {
    private Long supplierId;
    private Long partId;
    private BigDecimal unitPrice;
    private Integer leadDays;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer status;
}
