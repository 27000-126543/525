package com.ecommerce.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.product.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    Page<SeckillProduct> selectSeckillPage(Page<SeckillProduct> page, @Param("status") Integer status);

    List<SeckillProduct> selectActiveSeckills();

    @Update("UPDATE seckill_product SET stock = stock - #{quantity}, sold_count = sold_count + #{quantity} " +
            "WHERE id = #{seckillId} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("seckillId") Long seckillId, @Param("quantity") Integer quantity);

    @Update("UPDATE seckill_product SET stock = stock + #{quantity}, sold_count = sold_count - #{quantity} " +
            "WHERE id = #{seckillId} AND deleted = 0")
    int restoreStock(@Param("seckillId") Long seckillId, @Param("quantity") Integer quantity);
}
