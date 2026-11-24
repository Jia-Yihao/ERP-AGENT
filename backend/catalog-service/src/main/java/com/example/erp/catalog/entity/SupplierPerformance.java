package com.example.erp.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("supplier_performance")
public class SupplierPerformance extends BaseEntity {
    private Long supplierId;
    private LocalDate statDate;
    private BigDecimal onTimeRate;
    private BigDecimal qualityRate;
    private BigDecimal avgLeadDays;
    private BigDecimal score;
}
