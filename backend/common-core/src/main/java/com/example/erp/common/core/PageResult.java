package com.example.erp.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应固定五键：records / total / current / size / pages。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        long pages = size <= 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(records, total, current, size, pages);
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return new PageResult<>(List.of(), 0L, current, size, 0L);
    }
}
