package com.example.erp.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 契约不变量：响应体结构、分页五键、错误码分段。 */
class ContractTest {

    @Test
    void okResponseHasThreeKeys() {
        R<String> r = R.ok("x");
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("x", r.getData());
    }

    @Test
    void failResponseKeepsBusinessErrorInBody() {
        R<Void> r = R.fail(ErrorCode.STOCK_NOT_ENOUGH);
        assertEquals(4092, r.getCode());
        assertNull(r.getData());
    }

    @Test
    void pageResultHasFiveKeys() {
        PageResult<String> p = PageResult.of(List.of("a"), 11, 1, 10);
        assertEquals(5, p.getClass().getDeclaredFields().length);
        assertEquals(2, p.getPages());
        assertEquals(10, p.getSize());
    }

    @Test
    void errorCodesAreUnique() {
        long distinct = java.util.Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode).distinct().count();
        assertEquals(ErrorCode.values().length, distinct);
    }
}
