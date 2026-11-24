package com.example.erp.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailDTO {
    @NotNull(message = "partId 不能为空")
    private Long partId;
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    private Integer quantity;
    @NotNull(message = "unitPrice 不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "unitPrice 必须大于 0")
    private BigDecimal unitPrice;
    private String remark;
}
