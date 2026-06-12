package com.ecommerce.platform.product.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.product.entity.ProductSpu;

import java.util.List;

public interface ProductSpuService {

    PageResult<ProductSpu> page(PageQuery pageQuery, ProductSpu spu);

    ProductSpu getById(Long id);

    ProductSpu getDetail(Long id);

    void add(ProductSpu spu);

    void update(ProductSpu spu);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    List<ProductSpu> listByIds(List<Long> ids);

    List<ProductSpu> listHotProducts(Integer limit);

    List<ProductSpu> listRecommendProducts(Integer limit);
}
