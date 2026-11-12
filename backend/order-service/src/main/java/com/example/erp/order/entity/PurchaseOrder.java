package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("purchase_order")
public class PurchaseOrder {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime orderTime;
    @TableField(exist = false)
    private List<PurchaseOrderDetail> orderDetail;
}
