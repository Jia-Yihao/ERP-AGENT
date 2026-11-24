package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("purchase_receive")
public class PurchaseReceive extends BaseEntity {
    private String receiveNo;
    private Long orderId;
    private Long partId;
    private Integer quantity;
    private String receiver;
    private LocalDateTime receivedAt;
    private Integer status;
}
