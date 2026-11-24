package com.example.erp.catalog.convert;

import com.example.erp.catalog.dto.PartCreateDTO;
import com.example.erp.catalog.dto.SupplierCreateDTO;
import com.example.erp.catalog.entity.Part;
import com.example.erp.catalog.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CatalogConvert {
    CatalogConvert INSTANCE = Mappers.getMapper(CatalogConvert.class);

    Supplier toSupplier(SupplierCreateDTO dto);

    Part toPart(PartCreateDTO dto);
}
