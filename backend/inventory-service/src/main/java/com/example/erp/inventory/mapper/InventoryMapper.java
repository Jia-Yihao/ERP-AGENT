package com.example.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InventoryMapper extends BaseMapper<Inventory> {

    List<Inventory> selectPageWithPart(Page<?> page,
                                       @Param("partName") String partName,
                                       @Param("warehouseLocation") String warehouseLocation);

    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
