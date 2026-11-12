package com.example.erp.common;

import lombok.Data;

import java.math.BigDecimal;

/** 跨服务传输对象：订单与库存服务通过 OpenFeign 从 catalog-service 取得零件与供应商信息。 */
public final class Dtos {

    private Dtos() {
    }

    @Data
    public static class PartBrief {
        private Long id;
        private String partCode;
        private String name;
        private String model;
        private String specification;
        private String unit;
        private BigDecimal purchasePrice;
        private BigDecimal suggestedRetailPrice;
        private Integer stockWarningValue;
        private Long supplierId;
        private String category;
        private String description;
        private String supplierName;
    }
}
