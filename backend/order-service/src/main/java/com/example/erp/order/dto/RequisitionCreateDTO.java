package com.example.erp.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequisitionCreateDTO {
    private String requisitionNo;
    @NotNull(message = "partId 不能为空")
    private Long partId;
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    private Integer quantity;
    @NotBlank(message = "reason 不能为空")
    private String reason;
    private String applicant;
}
