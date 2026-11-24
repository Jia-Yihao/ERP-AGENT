package com.example.erp.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.catalog.entity.Part;
import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.mapper.PartMapper;
import com.example.erp.catalog.mapper.SupplierMapper;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.Dtos;
import com.example.erp.common.core.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartService {
    private final PartMapper mapper;
    private final SupplierMapper supplierMapper;

    public Part get(Long id) {
        Part p = mapper.selectById(id);
        if (p == null) throw new BizException(404, "零部件不存在");
        return p;
    }

    public List<Part> search(String name) {
        return mapper.selectList(new LambdaQueryWrapper<Part>()
                .like(StringUtils.hasText(name), Part::getName, name));
    }

    public List<Part> bySupplier(Long supplierId) {
        return mapper.selectList(new LambdaQueryWrapper<Part>()
                .eq(Part::getSupplierId, supplierId));
    }

    public PageResult<Part> page(long current, long size, String name, String category, Long supplierId) {
        LambdaQueryWrapper<Part> w = new LambdaQueryWrapper<Part>()
                .like(StringUtils.hasText(name), Part::getName, name)
                .eq(StringUtils.hasText(category), Part::getCategory, category)
                .eq(supplierId != null, Part::getSupplierId, supplierId);
        Page<Part> p = mapper.selectPage(new Page<>(current, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    @Transactional
    public Part create(Part body) {
        if (!StringUtils.hasText(body.getPartCode()) || !StringUtils.hasText(body.getName())) {
            throw new BizException(400, "零件编码与名称必填");
        }
        if (body.getPurchasePrice() == null) throw new BizException(400, "采购价必填");
        mapper.insert(body);
        return body;
    }

    /** 供 order-service / inventory-service 通过 OpenFeign 批量取零件与供应商名称。 */
    public List<Dtos.PartBrief> briefs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Part> parts = mapper.selectBatchIds(ids);
        List<Long> supplierIds = parts.stream().map(Part::getSupplierId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> names = supplierIds.isEmpty() ? Map.of()
                : supplierMapper.selectBatchIds(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName, (a, b) -> a));
        return parts.stream().map(p -> {
            Dtos.PartBrief brief = new Dtos.PartBrief();
            BeanUtils.copyProperties(p, brief);
            brief.setSupplierName(names.get(p.getSupplierId()));
            return brief;
        }).toList();
    }
}
