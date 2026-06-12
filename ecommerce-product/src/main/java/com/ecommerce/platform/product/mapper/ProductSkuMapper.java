package com.ecommerce.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    @Update("UPDATE product_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE product_sku SET stock = stock + #{quantity} WHERE id = #{skuId} AND deleted = 0")
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE product_sku SET locked_stock = locked_stock + #{quantity} WHERE id = #{skuId} AND stock - locked_stock >= #{quantity} AND deleted = 0")
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE product_sku SET locked_stock = locked_stock - #{quantity} WHERE id = #{skuId} AND deleted = 0")
    int unlockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    List<ProductSku> selectBySpuIds(@Param("spuIds") List<Long> spuIds);
}
