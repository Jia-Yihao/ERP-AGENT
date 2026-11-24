package com.example.erp.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotBlank(message = "orderNumber 不能为空")
    private String orderNumber;
    private Integer status;
    private String remark;
    @Valid
    @NotEmpty(message = "orderDetail 不能为空")
    private List<OrderDetailDTO> orderDetail;
}
