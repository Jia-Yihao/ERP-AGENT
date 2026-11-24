package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outbox_event")
public class OutboxEvent extends BaseEntity {
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
}
