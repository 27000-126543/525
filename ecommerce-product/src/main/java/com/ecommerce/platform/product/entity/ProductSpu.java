package com.ecommerce.platform.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spu")
public class ProductSpu extends BaseEntity {

    private Long id;
    private String spuCode;
    private String spuName;
    private String subTitle;
    private Long categoryId;
    private Long brandId;
    private String description;
    private String mainImage;
    private String images;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
    private Integer status;
    private Integer sortOrder;
    private String unit;
    private Integer weight;
    private Integer isNew;
    private Integer isHot;
    private Integer isRecommend;
    private String specTemplate;
}
