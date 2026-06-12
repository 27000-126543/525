package com.ecommerce.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.product.entity.ProductSpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductSpuMapper extends BaseMapper<ProductSpu> {

    Page<ProductSpu> selectSpuPage(Page<ProductSpu> page, @Param("spu") ProductSpu spu);

    List<ProductSpu> selectHotProducts(@Param("limit") Integer limit);

    List<ProductSpu> selectRecommendProducts(@Param("limit") Integer limit);
}
