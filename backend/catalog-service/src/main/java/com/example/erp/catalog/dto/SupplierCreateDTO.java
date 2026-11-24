package com.example.erp.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierCreateDTO {
    @NotBlank(message = "supplierCode 不能为空")
    private String supplierCode;
    @NotBlank(message = "name 不能为空")
    private String name;
    private String contactPerson;
    @Pattern(regexp = "^$|[0-9+\\-\\s]{5,32}", message = "phone 格式错误")
    private String phone;
    private String email;
    private String address;
    private String creditRating;
    private Integer status;
}
