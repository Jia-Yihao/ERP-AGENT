package com.example.erp.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.catalog.entity.Part;
import com.example.erp.catalog.mapper.PartMapper;
import com.example.erp.catalog.mapper.SupplierMapper;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.Dtos;
import com.example.erp.common.core.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

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
        Page<Part> p = mapper.selectPageWithSupplier(new Page<>(current, size), name, category, supplierId);
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
        return mapper.selectBriefs(ids);
    }
}
