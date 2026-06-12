package com.ecommerce.platform.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuName;
    private String skuImage;
    private String specValues;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Long activityId;
    private Integer activityType;
    private Integer status;
    private Integer isReview;
    private String remark;
}
