package com.example.erp.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("part")
public class Part {
    private Long id;
    private String partCode;
    private String name;
    private String model;
    private String specification;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal suggestedRetailPrice;
    private Integer stockWarningValue;
    private Long supplierId;
    private String category;
    private String description;
}
