package com.example.erp.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReceiveCreateDTO {
    private String receiveNo;
    @NotNull(message = "orderId 不能为空")
    private Long orderId;
    @NotNull(message = "partId 不能为空")
    private Long partId;
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    private Integer quantity;
    private String receiver;
    private LocalDateTime receivedAt;
    private Integer status;
}
