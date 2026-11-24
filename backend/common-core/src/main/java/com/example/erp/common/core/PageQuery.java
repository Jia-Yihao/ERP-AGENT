package com.example.erp.common.core;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {
    @Min(value = 1, message = "current 必须大于等于 1")
    private long current = 1L;
    @Min(value = 1, message = "size 必须大于等于 1")
    @Max(value = 100, message = "size 不得超过 100")
    private long size = 10L;
}
