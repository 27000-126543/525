package com.ecommerce.platform.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_product")
public class SeckillProduct extends BaseEntity {

    private Long id;
    private Long activityId;
    private String activityName;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String productImage;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private Integer totalStock;
    private Integer stock;
    private Integer lockedStock;
    private Integer soldCount;
    private Integer limitPerUser;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
