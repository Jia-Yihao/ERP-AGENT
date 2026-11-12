package com.example.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("inventory_log")
public class InventoryLog {
    private Long id;
    private Long partId;
    private String changeType;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
}
