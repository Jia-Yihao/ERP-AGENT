package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("order_status_log")
public class OrderStatusLog extends BaseEntity {
    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String operator;
    private String remark;
}
