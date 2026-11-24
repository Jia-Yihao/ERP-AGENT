package com.example.erp.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuoteCreateDTO {
    @NotNull(message = "supplierId 不能为空")
    private Long supplierId;
    @NotNull(message = "partId 不能为空")
    private Long partId;
    @NotNull(message = "unitPrice 不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "unitPrice 必须大于 0")
    private BigDecimal unitPrice;
    @Min(value = 0, message = "leadDays 不能为负数")
    private Integer leadDays;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer status;
}
