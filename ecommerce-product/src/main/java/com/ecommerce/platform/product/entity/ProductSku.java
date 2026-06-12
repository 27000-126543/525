package com.ecommerce.platform.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_sku")
public class ProductSku extends BaseEntity {

    private Long id;
    private String skuCode;
    private Long spuId;
    private String skuName;
    private String specValues;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private String image;
    private Integer stock;
    private Integer lockedStock;
    private Integer sales;
    private Integer sortOrder;
    private Integer status;
    private Integer weight;
    private String barCode;
}
