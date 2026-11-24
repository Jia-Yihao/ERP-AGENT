package com.example.erp.order.convert;

import com.example.erp.order.dto.OrderCreateDTO;
import com.example.erp.order.dto.OrderDetailDTO;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseOrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderConvert {
    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    PurchaseOrder toOrder(OrderCreateDTO dto);

    PurchaseOrderDetail toDetail(OrderDetailDTO dto);
}
