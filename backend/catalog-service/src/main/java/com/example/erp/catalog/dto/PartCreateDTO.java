package com.example.erp.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartCreateDTO {
    @NotBlank(message = "partCode 不能为空")
    private String partCode;
    @NotBlank(message = "name 不能为空")
    private String name;
    private String model;
    private String specification;
    private String unit;
    @NotNull(message = "purchasePrice 不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "purchasePrice 必须大于 0")
    private BigDecimal purchasePrice;
    private BigDecimal suggestedRetailPrice;
    private Integer stockWarningValue;
    private Long supplierId;
    private String category;
    private String description;
}
