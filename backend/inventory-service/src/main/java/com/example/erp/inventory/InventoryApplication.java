package com.example.erp.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.example.erp.common", "com.example.erp.inventory"})
@MapperScan("com.example.erp.inventory.mapper")
@EnableFeignClients(basePackages = "com.example.erp.inventory.client")
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
